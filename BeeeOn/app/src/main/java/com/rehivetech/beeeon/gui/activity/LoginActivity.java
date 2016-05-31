package com.rehivetech.beeeon.gui.activity;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.IErrorCode;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.gui.adapter.ServerAdapter;
import com.rehivetech.beeeon.gui.dialog.BaseFragmentDialog;
import com.rehivetech.beeeon.gui.dialog.BetterProgressDialog;
import com.rehivetech.beeeon.gui.dialog.ServerDetailDialog;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.model.entity.Server;
import com.rehivetech.beeeon.network.authentication.DemoAuthProvider;
import com.rehivetech.beeeon.network.authentication.FacebookAuthProvider;
import com.rehivetech.beeeon.network.authentication.GoogleAuthProvider;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.network.server.NetworkServer;
import com.rehivetech.beeeon.persistence.Persistence;
import com.rehivetech.beeeon.util.Utils;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;

/**
 * Default application activity, handles login or automatic redirect to MainActivity.
 *
 * @author Robyer
 */
public class LoginActivity extends BaseActivity implements BaseFragmentDialog.IPositiveButtonDialogListener<ServerDetailDialog> {
	private static final String TAG = LoginActivity.class.getSimpleName();

	public static final String BUNDLE_REDIRECT = "isRedirect";

	/**
	 * Defines whether choose server spinner will be showed by default or not
	 */
	private static final boolean SERVER_ENABLED_DEFAULT = true;

	@Bind(android.R.id.content) View mRootView;
	@Bind(R.id.login_select_server_spinner) Spinner mLoginSelectServerSpinner;
	@Bind(R.id.login_select_server_layout) LinearLayout mLoginSelectServerLayout;
	@Bind(R.id.login_select_server) RecyclerView mLoginSelectServerRecyclerView;
	@Bind(R.id.login_bottom_sheet) View mBottomSheet;

	private BetterProgressDialog mProgress;
	private boolean mLoginCancel = false;
	private IAuthProvider mAuthProvider;
	private Realm mRealm;
	private BottomSheetBehavior<View> mBottomSheetBehavior;

	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////// Override METHODS
	// ////////////////////////////////////////////////////////////////////////////////////


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		mRealm = Realm.getDefaultInstance();
		ButterKnife.bind(this);
		setupToolbar("", INDICATOR_NONE);

		// Get controller
		Controller controller = Controller.getInstance(this);

		// Check already logged in user (but ignore demo mode)
		if (controller.isLoggedIn() && !controller.isDemoMode()) {
			Log.d(TAG, "Already logged in, going to next activity...");
			onLoggedIn(); // finishes this activity
			return;
		}

		// Do automatic login if we have remembered last logged in user
		IAuthProvider lastAuthProvider = controller.getLastAuthProvider();
		if (lastAuthProvider != null && !(lastAuthProvider instanceof DemoAuthProvider)) {
			// Automatic login with last used provider
			Log.d(TAG, String.format("Automatic login with last provider '%s' and user '%s'...", lastAuthProvider.getProviderName(), lastAuthProvider.getPrimaryParameter()));
			loginWithPermissionCheck(lastAuthProvider);
		}

		// show intro if was not shown
		showIntroFirstTime();
		// Prepare progressDialog
		prepareProgressDialog();
		// Initialize server related views
		prepareServers();
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

		Log.d(TAG, "Go to INTRO");
		prefs.edit().putBoolean(Constants.GUI_INTRO_PLAY, false).apply();
		Intent intent = new Intent(this, IntroActivity.class);
		startActivity(intent);
	}

	/**
	 * Prepares UI for progress dialog
	 */
	private void prepareProgressDialog() {
		mProgress = new BetterProgressDialog(this);
		mProgress.setCancelable(true);
		mProgress.setCanceledOnTouchOutside(false);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				mLoginCancel = true;
			}
		});
	}

	/**
	 * Prepares UI for server spinner
	 */
	private void prepareServers() {
		ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.overlay_spinner_item, Arrays.asList(NetworkServer.values()));
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mLoginSelectServerSpinner.setAdapter(adapter);

		String serverId = Persistence.loadLoginServerId(this);
		NetworkServer server = Utils.getEnumFromId(NetworkServer.class, serverId, NetworkServer.getDefaultServer());
		mLoginSelectServerSpinner.setSelection(server.ordinal());

		// Set choose server visibility
		boolean chooseServerEnabled = Controller.getInstance(this).getGlobalSettings().getBoolean(Constants.PERSISTENCE_PREF_LOGIN_CHOOSE_SERVER_MANUALLY, SERVER_ENABLED_DEFAULT);
		setSelectServerVisibility(chooseServerEnabled);

		mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
		mLoginSelectServerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		OrderedRealmCollection<Server> serversData = mRealm.where(Server.class).findAllAsync();

		ServerAdapter serverAdapter = new ServerAdapter(this, serversData);
		mLoginSelectServerRecyclerView.setAdapter(serverAdapter);
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
			default: {
				if (FacebookAuthProvider.isFacebookRequestCode(requestCode)) {
					authProvider = new FacebookAuthProvider();
					((FacebookAuthProvider) authProvider).processResult(this, requestCode, resultCode, data);
				} else {
					Log.e(TAG, String.format("Unknown requestCode (%d)", requestCode));
					mLoginCancel = true;
					mProgress.dismiss();
				}
				return;
			}
		}

		// Process the result
		switch (resultCode) {
			case Activity.RESULT_CANCELED:
			case IAuthProvider.RESULT_CANCEL: {
				Log.d(TAG, "Received RESULT_CANCEL from authProvider");
				mLoginCancel = true;
				mProgress.dismiss();
				break;
			}
			case IAuthProvider.RESULT_ERROR: {
				Log.e(TAG, "Received RESULT_ERROR from authProvider");
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
					String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
					if (email == null) {
						Log.w(TAG, "Received RESULT_OK from GoogleAuthProvider but without email");
						mLoginCancel = true;
						mProgress.dismiss();
						return;
					}

					// Set given e-mail as parameter and repeat authProcess of getting token
					authProvider.setPrimaryParameter(email);
					authProvider.prepareAuth(this);
				}
				break;
			}
			case IAuthProvider.RESULT_AUTH: {
				if (!authProvider.loadAuthIntent(data)) {
					Log.e(TAG, "Received RESULT_AUTH but authProvider can't load the required data");
					mProgress.dismiss();
					return;
				}

				// Authorization parameters are prepared
				mProgress.setMessageResource(R.string.login_progress_signing);
				mProgress.show();

				new Thread(new Runnable() {
					@Override
					public void run() {
						setDemoMode(authProvider.isDemo());
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
		boolean checked = Controller.getInstance(this).getGlobalSettings().getBoolean(Constants.PERSISTENCE_PREF_LOGIN_CHOOSE_SERVER_MANUALLY, SERVER_ENABLED_DEFAULT);
		MenuItem item = menu.findItem(R.id.login_menu_action_choose_server_manually);
		item.setChecked(checked);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
			case R.id.login_menu_action_choose_server_manually:
				boolean checked = !item.isChecked();
				item.setChecked(checked);
				Controller.getInstance(this).getGlobalSettings().edit().putBoolean(Constants.PERSISTENCE_PREF_LOGIN_CHOOSE_SERVER_MANUALLY, checked).apply();
				setSelectServerVisibility(checked);
				return true;

			case R.id.login_menu_action_about:
				showAboutDialog();
				return true;
		}

		return false;
	}

	private void setSelectServerVisibility(boolean visible) {
		mLoginSelectServerLayout.setVisibility(visible ? View.VISIBLE : View.GONE); // TODO put away
		mBottomSheet.setVisibility(visible ? View.VISIBLE : View.GONE);
	}


	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////// Custom METHODS
	// ////////////////////////////////////////////////////////////////////////////////////

	/**
	 * This internally creates new instance of Controller with changed mode (e.g. demoMode or normal).
	 * You MUST call getInstance() again to get fresh instance and DON'T remember or use the previous.
	 *
	 * @param demoMode
	 */
	protected void setDemoMode(boolean demoMode) {
		// Get selected server
		String serverId = "";
		if (mLoginSelectServerLayout.getVisibility() == View.VISIBLE) {
			NetworkServer server = (NetworkServer) mLoginSelectServerSpinner.getSelectedItem();
			serverId = server != null ? server.getId() : "";
		}

		// After changing demo mode must be controller reloaded
		Controller.setDemoMode(this, demoMode, serverId);
	}

	/**
	 * Last loggingAction method that call controller to proceed access to server
	 *
	 * @param authProvider to login with
	 */
	private void loginWithPermissionCheck(final IAuthProvider authProvider) {
		if (!authProvider.isDemo() && !Utils.isInternetAvailable()) {
			mProgress.dismiss();
			Toast.makeText(this, getString(R.string.login_toast_internet_connection), Toast.LENGTH_LONG).show();
			return;
		}

		mAuthProvider = authProvider; // note: need to have it as field because we use it in onRequestPermissionsResult
		if (checkAccountsPermission()) {
			startAuthenticating();
		}
	}

	/**
	 * Checks if app has permission to check user's accounts
	 *
	 * @return success
	 */
	private boolean checkAccountsPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
			// explanation shown
			ActivityCompat.requestPermissions(this, new String[]{
					Manifest.permission.GET_ACCOUNTS,
			}, Constants.PERMISSION_CODE_GET_ACCOUNTS);
			return false;
		}
		return true;
	}

	/**
	 * When request dialog was confirmed/canceled
	 *
	 * @param requestCode  which request was managed
	 * @param permissions  array of checking permissions
	 * @param grantResults array of flags (granted/denied) for specified permission
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case Constants.PERMISSION_CODE_GET_ACCOUNTS:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission granted
					startAuthenticating();
				} else {
					// permission denied
					Snackbar.make(mRootView, R.string.permission_accounts_warning, Snackbar.LENGTH_LONG).show();
				}
				break;
		}
	}

	/**
	 * Shows dialog with login and starts authenticating by specified auth provider
	 */
	private void startAuthenticating() {
		mLoginCancel = false;
		mProgress.setMessageResource(R.string.login_progress_signing);
		mProgress.show();
		mAuthProvider.prepareAuth(LoginActivity.this);
	}

	/**
	 * MUST BE CALLED IN BACKGROUND THREAD
	 *
	 * @param authProvider which will be set to login
	 */
	private void loggingBackgroundAction(final IAuthProvider authProvider) {
		String errMessage = getString(R.string.login_toast_login_failed);
		boolean errFlag = true;
		Controller controller = Controller.getInstance(LoginActivity.this);

		try {
			// Here is authProvider already filled with needed parameters so we can send them to the server
			if (controller.login(authProvider)) {
				Log.d(TAG, "Login successfull");
				errFlag = false;

				// Load all gates and data for active one on login
				mProgress.setMessageResource(R.string.login_progress_loading_gates);
				controller.getGatesModel().reloadGates(true);

				Gate active = controller.getActiveGate();
				if (active != null) {
					// Load data for active gate
					mProgress.setMessageResource(R.string.login_progress_loading_gate);
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

			Log.i(TAG, "Login finished");
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
								((GoogleAuthProvider) authProvider).invalidateToken(LoginActivity.this);

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
	 * MUST BE CALLED IN BACKGROUND THREAD
	 *
	 * @param authProvider which will be set to login
	 */
	private void registeringBackgroundAction(final IAuthProvider authProvider) {
		mProgress.setMessageResource(R.string.login_progress_signup);

		String errMessage = getString(R.string.login_toast_registration_failed);
		boolean errFlag = true;
		Controller controller = Controller.getInstance(LoginActivity.this);

		try {
			// Here is authProvider already filled with needed parameters so we can send them to the server
			if (controller.register(authProvider)) {
				Log.d(TAG, "Register successful");
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
	}

	// TODO not working
//	@OnClick(R.id.login_bottom_sheet)
//	public void onClickBottomSheet(){
//		mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//	}

	@OnClick(R.id.login_server_add)
	public void onClickServerAddButton(View view) {
		ServerDetailDialog.showCreate(this, getSupportFragmentManager());
	}

	/**
	 * Click listeners for login buttons
	 *
	 * @param view which button was clicked
	 */
	@OnClick({R.id.login_demo_button, R.id.login_google_button, R.id.login_facebook_button})
	public void onClickLoginButton(View view) {
		switch (view.getId()) {
			case R.id.login_demo_button:
				mProgress.setMessageResource(R.string.login_progress_loading_demo);
				loginWithPermissionCheck(new DemoAuthProvider());
				break;

			case R.id.login_google_button:
				loginWithPermissionCheck(new GoogleAuthProvider());
				break;

			case R.id.login_facebook_button:
				loginWithPermissionCheck(new FacebookAuthProvider());
				break;
		}
	}

	@Override
	public void onPositiveButtonClicked(int requestCode, View view, ServerDetailDialog dialog) {
		Toast.makeText(this, "ahoj", Toast.LENGTH_LONG).show();

		//		mRealm.executeTransaction(new Realm.Transaction() {
//			@Override
//			public void execute(Realm realm) {
//				long nextID = Utils.autoIncrement(realm, Server.class);
//				Server myServer = new Server();
//				myServer.setId(nextID);
//				myServer.setName("První server");
//				realm.copyToRealm(myServer);
//			}
//		});
	}
}
