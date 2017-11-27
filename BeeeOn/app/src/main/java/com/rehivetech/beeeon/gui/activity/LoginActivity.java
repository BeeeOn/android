package com.rehivetech.beeeon.gui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.WorkerThread;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.avast.android.dialogs.fragment.ProgressDialogFragment;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.rehivetech.beeeon.BuildConfig;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ClientError;
import com.rehivetech.beeeon.exception.IErrorCode;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.gui.adapter.ServerAdapter;
import com.rehivetech.beeeon.gui.adapter.base.ClickableRecyclerViewAdapter;
import com.rehivetech.beeeon.gui.dialog.BaseBeeeOnDialog;
import com.rehivetech.beeeon.gui.dialog.ServerDetailDialog;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.model.DatabaseSeed;
import com.rehivetech.beeeon.model.entity.Server;
import com.rehivetech.beeeon.network.authentication.DemoAuthProvider;
import com.rehivetech.beeeon.network.authentication.FacebookAuthProvider;
import com.rehivetech.beeeon.network.authentication.GoogleAuthProvider;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.network.authentication.PresentationAuthProvider;
import com.rehivetech.beeeon.network.server.Network;
import com.rehivetech.beeeon.persistence.Persistence;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.util.Validator;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.cert.CertificateException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import timber.log.Timber;

/**
 * Default application activity, handles login or automatic redirect to MainActivity.
 *
 * @author Robyer
 */
public class LoginActivity extends BaseActivity implements BaseBeeeOnDialog.IPositiveButtonDialogListener, BaseBeeeOnDialog.IDeleteButtonDialogListener {

	public static final String BUNDLE_REDIRECT = "isRedirect";

	/**
	 * Defines whether choose server spinner will be showed by default or not
	 */
	private static final boolean SERVER_SELECTION_SHOWN_DEFAULT = false;
	private static final int DIALOG_REQUEST_SERVER_DETAIL = 1;
	private static final double ICON_SLIDE_THRESHOLD = 0.2;

	@BindView(android.R.id.content)
	View mRootView;
	@BindView(R.id.login_select_server)
	RecyclerView mLoginSelectServerRecyclerView;
	@BindView(R.id.login_bottom_sheet)
	NestedScrollView mBottomSheet;
	@BindView(R.id.login_select_server_icon)
	ImageView mSelectServerIcon;

	private DialogFragment mProgress;
	private boolean mLoginCancel = false;
	private IAuthProvider mAuthProvider;
	private Realm mRealm;
	private BottomSheetBehavior<NestedScrollView> mBottomSheetBehavior;
	private ServerAdapter mServersAdapter;
	private boolean mChooseServerEnabled;

	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////// Override METHODS
	// ////////////////////////////////////////////////////////////////////////////////////


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mRealm = Realm.getDefaultInstance();

		// Get controller
		Controller controller = Controller.getInstance(this);

		// Check already logged in user (but ignore demo mode)
		if (controller.isLoggedIn() && !controller.isDemoMode()) {
			Timber.d( "Already logged in, going to next activity...");
			onLoggedIn(); // finishes this activity
			return;
		}

		setContentView(R.layout.activity_login);
		ButterKnife.bind(this);
		setupToolbar("", INDICATOR_NONE);

		mChooseServerEnabled = Persistence.Global.getSettings().getBoolean(Constants.PERSISTENCE_PREF_LOGIN_CHOOSE_SERVER_MANUALLY, SERVER_SELECTION_SHOWN_DEFAULT);

		// show intro if was not shown
		showIntroFirstTime();
		// Initialize server related views
		prepareServers();

		// Do automatic login if we have remembered last logged in user
		mAuthProvider = Persistence.Global.getLastAuthProvider();
		if (mAuthProvider != null && !mAuthProvider.isDemo()) {
			// Automatic login with last used provider
			Timber.d("Automatic login with last provider %s", mAuthProvider.getProviderName());
			login(mAuthProvider);
		}
	}

	@Override
	public void onBackPressed() {
		if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
			mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mRealm.close();
	}

	/**
	 * Shows intro to the app if was not previously shown
	 */
	private void showIntroFirstTime() {
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		if (!prefs.getBoolean(Constants.GUI_INTRO_PLAY, true)) return;

		Timber.d( "Go to INTRO");
		prefs.edit().putBoolean(Constants.GUI_INTRO_PLAY, false).apply();
		Intent intent = new Intent(this, IntroActivity.class);
		startActivity(intent);
	}

	/**
	 * Shows progress dialog
	 */
	private void showProgressDialog(@StringRes int message) {
		mProgress = ProgressDialogFragment.createBuilder(this, getSupportFragmentManager())
				.setMessage(getString(message))
				.setCancelableOnTouchOutside(false)
				.setCancelable(true)
				.showAllowingStateLoss();
	}

	public void hideProgressDialog() {
		if (mProgress != null) {
			mProgress.dismiss();
			mProgress = null;
		}
	}

	/**
	 * Prepares UI for server spinner
	 */
	private void prepareServers() {
		// Set choose server visibility
		setSelectServerVisibility(mChooseServerEnabled);

		mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
		mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {
				if (slideOffset > ICON_SLIDE_THRESHOLD) {
					mSelectServerIcon.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
				} else {
					mSelectServerIcon.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
				}
			}
		});
		mLoginSelectServerRecyclerView.setLayoutManager(new LinearLayoutManager(this));

		OrderedRealmCollection<Server> serversData = mRealm.where(Server.class).findAll();
		if (serversData.isEmpty() || serversData.size() < Server.DEFAULT_SERVERS_COUNT) {
			// if lower than 2 items we assume there are not default server and seed the DB
			mRealm.executeTransaction(new DatabaseSeed());
		}

		mServersAdapter = new ServerAdapter(this, serversData);

		// click on server item --> SELECTS AS SERVER
		mServersAdapter.setOnItemClickListener(new ClickableRecyclerViewAdapter.OnItemClickListener() {
			@Override
			public void onRecyclerViewItemClick(ClickableRecyclerViewAdapter.ViewHolder viewHolder, int position, int viewType) {
				Server server = mServersAdapter.getItem(position);

				ServerAdapter.ServerViewHolder holder = (ServerAdapter.ServerViewHolder) viewHolder;
				holder.radio.setChecked(true);
				mServersAdapter.setSelectedPosition(position);

				// Remember login server
				Persistence.Global.saveLoginServerId(server.getId());

				// need to be handled this way because otherwise not finished clicking
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
					}
				}, 100);
			}
		});

		// long click on server item --> EDITING
		mServersAdapter.setOnItemLongClickListener(new ClickableRecyclerViewAdapter.OnItemLongClickListener() {
			@Override
			public boolean onRecyclerViewItemLongClick(ClickableRecyclerViewAdapter.ViewHolder viewHolder, int position, int viewType) {
				Server server = mServersAdapter.getItem(position);
				// check if is editable
				if (!server.isEditable()) return false;
				ServerDetailDialog.showEdit(LoginActivity.this, getSupportFragmentManager(), DIALOG_REQUEST_SERVER_DETAIL, server.getId());
				return true;
			}
		});
		mLoginSelectServerRecyclerView.setAdapter(mServersAdapter);

		Long serverId = Persistence.Global.loadLoginServerId();
		int index = Utils.getIndexFromList(serverId, serversData);
		if (index > Constants.NO_INDEX) {
			mServersAdapter.setSelectedPosition(index);
		}
	}

	/**
	 * When other activity (registering by different providers) results
	 *
	 * @param requestCode code we called it
	 * @param resultCode  code we get
	 * @param data        data we get
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Prepare correct authProvider object
		final IAuthProvider authProvider;

		if ((requestCode & 0xffff) == ServerDetailDialog.REQUEST_CERTIFICATE_PICK) {
			// ignoring, because handled in dialog!
			// NOTE: quite not best handling -> this is how handles it FragmentActivity
			return;
		}

		// RequestCode uniquely identifies authProvider - all providers must respect that and use it in startActivityForResult(providerId)
		switch (requestCode) {
			case GoogleAuthProvider.PROVIDER_ID: {
				authProvider = new GoogleAuthProvider();
				break;
			}
			case DemoAuthProvider.PROVIDER_ID: {
				authProvider = new DemoAuthProvider();
				break;
			}
			case FacebookAuthProvider.PROVIDER_ID: {
				authProvider = new FacebookAuthProvider();
				break;
			}
			case PresentationAuthProvider.PROVIDER_ID: {
				authProvider = new PresentationAuthProvider(this);
				break;
			}
			default: {
//				if (FacebookAuthProvider.isFacebookRequestCode(requestCode)) {
					authProvider = new FacebookAuthProvider();
					((FacebookAuthProvider) authProvider).processResult(this, requestCode, resultCode, data);
//				} else {
//					Timber.e( String.format("Unknown requestCode (%d)", requestCode));
//					mLoginCancel = true;
//					mProgress.dismiss();
//				}
				return;
			}
		}

		// Process the result
		switch (resultCode) {
			case Activity.RESULT_CANCELED:
			case IAuthProvider.RESULT_CANCEL: {
				Timber.d( "Received RESULT_CANCEL from authProvider");
				mLoginCancel = true;
				mProgress.dismiss();
				break;
			}
			case IAuthProvider.RESULT_ERROR: {
				Timber.e( "Received RESULT_ERROR from authProvider");
				mLoginCancel = true;
				mProgress.dismiss();

				// Show common error message
				String message = getString(R.string.login_toast_auth_provider_error);
				Utils.showToastOnUiThread(this, message, Toast.LENGTH_LONG);
				break;
			}
			case Activity.RESULT_OK: {
				// This result can go from Google Play Services intent as the choose of user account
				if (authProvider instanceof GoogleAuthProvider) {

					GoogleSignInAccount account = Auth.GoogleSignInApi.getSignInResultFromIntent(data).getSignInAccount();

                    if (account != null) {


                        authProvider.setTokenParameter(account.getServerAuthCode());
                        ((GoogleAuthProvider) mAuthProvider).getGoogleApiClient().disconnect();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // After changing demo mode must be controller reloaded
                                Controller.setDemoMode(LoginActivity.this, authProvider.isDemo());
                                loggingBackgroundAction(authProvider);
                            }
                        }).start();
                    }

				}
				break;
			}
			case IAuthProvider.RESULT_AUTH: {
				if (!authProvider.loadAuthIntent(this, data)) {
					Timber.e( "Received RESULT_AUTH but authProvider can't load the required data");
					mProgress.dismiss();
					return;
				}

				// Authorization parameters are prepared
				showProgressDialog(R.string.login_progress_signing);

				new Thread(new Runnable() {
					@Override
					public void run() {
						// After changing demo mode must be controller reloaded
						Controller.setDemoMode(LoginActivity.this, authProvider.isDemo());
						loggingBackgroundAction(authProvider);
					}
				}).start();
				break;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_login_menu, menu);

		// Set choose server item (un)checked
		MenuItem item = menu.findItem(R.id.login_menu_action_choose_server_manually);
		item.setChecked(mChooseServerEnabled);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
			case R.id.login_menu_action_choose_server_manually:
				boolean checked = !item.isChecked();
				item.setChecked(checked);
				if (!checked) {
					// default server should be first
					mServersAdapter.setSelectedPosition(0);
					// Remember login server
					Persistence.Global.saveLoginServerId(Server.SERVER_ID_PRODUCTION);
				}

				Persistence.Global.getSettings().edit().putBoolean(Constants.PERSISTENCE_PREF_LOGIN_CHOOSE_SERVER_MANUALLY, checked).apply();
				setSelectServerVisibility(checked);
				return true;

			case R.id.login_menu_action_about:
				showAboutDialog();
				return true;
		}

		return false;
	}

	/**
	 * In case it's shown/viewed selecting servers
	 *
	 * @param isVisible wheter visible or not
	 */
	private void setSelectServerVisibility(boolean isVisible) {
		mBottomSheet.setVisibility(isVisible ? View.VISIBLE : View.GONE);
	}


	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////// Custom METHODS
	// ////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Last loggingAction method that call controller to proceed access to server
	 *
	 * @param authProvider to login with
	 */
	private void login(final IAuthProvider authProvider) {
		if (!authProvider.isDemo() && !Utils.isInternetAvailable()) {
			mProgress.dismiss();
			Toast.makeText(this, getString(R.string.login_toast_internet_connection), Toast.LENGTH_LONG).show();
			return;
		}

		mAuthProvider = authProvider; // note: need to have it as field because we use it in onRequestPermissionsResult
		startAuthenticating();
	}

	/**
	 * Shows dialog with login and starts authenticating by specified auth provider
	 */
	private void startAuthenticating() {
		mLoginCancel = false;
		showProgressDialog(R.string.login_progress_signing);
		mAuthProvider.prepareAuth(LoginActivity.this);
	}

	/**
	 * @param authProvider which will be set to login
	 */
	@WorkerThread
	private void loggingBackgroundAction(final IAuthProvider authProvider) {
		String errMessage = getString(R.string.login_toast_login_failed);
		boolean errFlag = true;
		Controller controller = Controller.getInstance(LoginActivity.this);

		try {
			// Here is authProvider already filled with needed parameters so we can send them to the server
			if (controller.login(authProvider)) {
				Timber.d( "Login successfull");
				errFlag = false;

				// Load all gates and data for active one on login
				controller.getGatesModel().reloadGates(true);

				Gate active = controller.getActiveGate();
				if (active != null) {
					// Load data for active gate
					controller.getLocationsModel().reloadLocationsByGate(active.getId(), true);
					controller.getDevicesModel().reloadDevicesByGate(active.getId(), true);
				}

				if (mLoginCancel) {
					// User cancelled login so do logout() to be sure it won't try to login automatically next time
					controller.logout(false);
				} else {
					// Open MainActivity or just this LoginActivity and let it redirect back
					onLoggedIn(); // finishes this activity
					return;
				}
			}

			Timber.i( "Login finished");
		} catch (AppException e) {
			IErrorCode errorCode = e.getErrorCode();

			if (errorCode instanceof NetworkError) {
				switch ((NetworkError) errorCode) {
					case USER_NOT_EXISTS: {
						registeringBackgroundAction(authProvider);
					}
					case NOT_VALID_USER: {
						// Server denied our credentials (e.g. Google token, or email+password)
						if (authProvider instanceof GoogleAuthProvider) {
							// Probably wrong Google token so invalidate the token and then try it again
							if (Utils.isGooglePlayServicesAvailable(LoginActivity.this)) {
								((GoogleAuthProvider) authProvider).invalidateToken();

								// FIXME: try it again somehow (if we haven't tried it yet)
							}
						}

						break;
					}
				}
			}

			e.printStackTrace();
			errMessage = e.getTranslatedErrorMessage(LoginActivity.this);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (errFlag) {
			Utils.showToastOnUiThread(LoginActivity.this, errMessage, Toast.LENGTH_LONG);
		}

		mProgress.dismiss();
	}

	/**
	 * @param authProvider which will be set to login
	 */
    @WorkerThread
	private void registeringBackgroundAction(final IAuthProvider authProvider) {
		String errMessage = getString(R.string.login_toast_registration_failed);
		boolean errFlag = true;
		Controller controller = Controller.getInstance(LoginActivity.this);

		try {
			// Here is authProvider already filled with needed parameters so we can send them to the server
			if (controller.register(authProvider)) {
				Timber.d( "Register successful");
				errFlag = false;

				// Finish registration by start loggingAction in
				if (!mLoginCancel) {
					loggingBackgroundAction(authProvider);
				}

				return;
			}
		} catch (AppException e) {
			IErrorCode errorCode = e.getErrorCode();

			// TODO: handle some known exceptions

			e.printStackTrace();
			errMessage = e.getTranslatedErrorMessage(LoginActivity.this);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (errFlag) {
			Utils.showToastOnUiThread(LoginActivity.this, errMessage, Toast.LENGTH_LONG);
			mProgress.dismiss();
		}
	}

	/**
	 * Finish this activity, dismiss progressDialog and if it's not redirect (set in Intent as BUNDLE_REDIRECT) then also start MainActivity
	 */
	private void onLoggedIn() {
		if (mProgress != null) mProgress.dismiss();

		// Check whether this activity started as redirect (e.g., after connection loss) or it is task's root (probably classic start)
		Bundle bundle = getIntent().getExtras();
		if (bundle == null || !bundle.getBoolean(BUNDLE_REDIRECT, false) || isTaskRoot()) {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		}

		finish();
	}

	/**
	 * Click listeners for tour and about app
	 *
	 * @param view which view was clicked
	 */
	@OnClick(R.id.login_take_tour)
	public void onClickTakeTour(View view) {
		Intent intent = new Intent(this, IntroActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.left_in, R.anim.left_out);
	}

	@OnClick(R.id.login_select_server_wrapper)
	public void onClickBottomSheet() {
		mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
	}

	@OnClick(R.id.login_select_server_icon)
	public void onClickServerSelectIcon() {
		if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
			mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
		} else if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
			mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
		}
	}

	@OnClick(R.id.login_server_add)
	public void onClickServerAddButton(View view) {
		ServerDetailDialog.showCreate(this, getSupportFragmentManager(), DIALOG_REQUEST_SERVER_DETAIL);
	}

	/**
	 * Click listeners for login buttons
	 *
	 * @param view which button was clicked
	 */
	@OnClick({R.id.login_demo_button, R.id.login_google_button, R.id.login_facebook_button})
	public void onClickLoginButton(View view) {
		IAuthProvider authProvider = null;

		switch (view.getId()) {
			case R.id.login_demo_button:
				authProvider = new DemoAuthProvider();
				break;

			case R.id.login_google_button:
				if (BuildConfig.BUILD_TYPE.equals("presentation")) {
					authProvider = new PresentationAuthProvider(this);
				} else {
					authProvider = new GoogleAuthProvider();
				}
				break;

			case R.id.login_facebook_button:
				if (BuildConfig.BUILD_TYPE.equals("presentation")) {
					authProvider = new PresentationAuthProvider(this);
				} else {
					authProvider = new FacebookAuthProvider();
				}
				break;
		}

		login(authProvider);
		mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
	}

	/**
	 * Deleting from server dialog
	 *
	 * @param requestCode in case other dialogs in this activity
	 * @param view        root view
	 * @param baseDialog  so can get other data
	 */
	@Override
	public void onDeleteButtonClicked(int requestCode, View view, final BaseBeeeOnDialog baseDialog) {
		if (requestCode != DIALOG_REQUEST_SERVER_DETAIL) {
			return;
		}

		mRealm.executeTransaction(new Realm.Transaction() {
			@Override
			public void execute(Realm realm) {
				Server serverToDelete = ((ServerDetailDialog) baseDialog).getServer();
				if (serverToDelete.isValid() && Server.isEditable(serverToDelete.getId())) {
					Server selectedServer = mServersAdapter.getItem(mServersAdapter.getSelectedPosition());
					// need to set some id of server (default)
					if (selectedServer != null && selectedServer.equals(serverToDelete)) {
						Persistence.Global.saveLoginServerId(Server.SERVER_ID_PRODUCTION);
						mServersAdapter.setSelectedPosition(0);    // we assume first server is production (always will be!)
					}

					int index = Utils.getIndexFromList(serverToDelete.getId(), mServersAdapter.getData());
					if (index > Constants.NO_INDEX) {
						mServersAdapter.getData().deleteFromRealm(index);
					} else {
						// delete server from db
						serverToDelete.deleteFromRealm();
					}


				} else {
					Timber.e( "Trying to delete non deletable server!");
				}
			}
		});
	}

	/**
	 * Server dialog was submitted
	 *
	 * @param requestCode in case other dialogs are here specify request code
	 * @param view        root view of dialog
	 * @param baseDialog  dialog itself so its possible not to dissmiss on error
	 */
	@Override
	public void onPositiveButtonClicked(int requestCode, View view, final BaseBeeeOnDialog baseDialog) {
		if (requestCode != DIALOG_REQUEST_SERVER_DETAIL) {
			return;
		}

		final ServerDetailDialog dialog = (ServerDetailDialog) baseDialog;
		final TextInputLayout serverNameView = ButterKnife.findById(view, R.id.server_name);
		final TextInputLayout serverHostView = ButterKnife.findById(view, R.id.server_host);
		final TextInputLayout serverPortView = ButterKnife.findById(view, R.id.server_port);
		if (!Validator.validateAll(serverNameView, serverHostView) || !Validator.validate(serverPortView, Validator.PORT)) {
			return;
		}

		// checking if edit text (because of lint, checked already in validation)
		if (serverNameView.getEditText() == null || serverHostView.getEditText() == null || serverPortView.getEditText() == null) {
			Timber.e( "There is none EditText inside TextInputLayout!");
			return;
		}

		final Server server = dialog.getServer();

		String certificate = null;
		if (dialog.mCertificateUri == null || dialog.mCertificateUri.isEmpty()) {
			// check if server has certificate
			if (server.getCertificate() == null) {
				dialog.setCertificateError(getString(R.string.server_detail_certificate_not_selected));
				return;
			}
		} else {
			certificate = loadCertificate(Uri.parse(dialog.mCertificateUri), dialog);
			// something went wrong with loading, don't continue
			if (certificate == null) return;
		}

		// hide error message
		dialog.setCertificateError(null);

		final String finalCertificate = certificate;
		mRealm.executeTransaction(new Realm.Transaction() {
			@Override
			public void execute(Realm realm) {
				server.name = serverNameView.getEditText().getText().toString();
				server.address = serverHostView.getEditText().getText().toString();
				server.port = Integer.parseInt(serverPortView.getEditText().getText().toString());
				if (finalCertificate != null) {
					server.setCertificate(finalCertificate);
				}

				realm.copyToRealmOrUpdate(server);
			}
		});


		dialog.dismiss();
	}

	/**
	 * Loads certificate from device's disk and handles errors (with showing toast)
	 *
	 * @param fileUri specified file
	 * @return certificate string
	 */
	private String loadCertificate(Uri fileUri, ServerDetailDialog dialog) {
		try {
			InputStream inputStream = getContentResolver().openInputStream(fileUri);
			String certificate = Utils.convertInputStreamToString(inputStream);
			if (certificate == null) {
				throw new AppException(ClientError.CERTIFICATE);
			}
			// checks if certificate is ok
			Network.checkAndGetCertificate(new ByteArrayInputStream(certificate.getBytes()));
			return certificate;
		} catch (FileNotFoundException e) {
			// this should never happen because we select it properly, but just in case
			dialog.setCertificateError(getString(R.string.server_detail_certificate_file_not_found));
			return null;
		} catch (CertificateException e) {
			dialog.setCertificateError(getString(R.string.server_detail_certificate_wrong_type));
			return null;
		} catch (AppException e) {
			dialog.setCertificateError(e.getTranslatedErrorMessage(this));
			return null;
		}
	}
}
