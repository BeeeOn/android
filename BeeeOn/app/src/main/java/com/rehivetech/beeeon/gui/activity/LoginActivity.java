package com.rehivetech.beeeon.gui.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.IErrorCode;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.gui.dialog.BetterProgressDialog;
import com.rehivetech.beeeon.gui.dialog.InfoDialogFragment;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.network.NetworkServer;
import com.rehivetech.beeeon.network.authentication.DemoAuthProvider;
import com.rehivetech.beeeon.network.authentication.FacebookAuthProvider;
import com.rehivetech.beeeon.network.authentication.GoogleAuthProvider;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.persistence.Persistence;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;

/**
 * Default application activity, handles login or automatic redirect to MainActivity.
 *
 * @author Robyer
 */
public class LoginActivity extends BaseActivity {
	public static final String BUNDLE_REDIRECT = "isRedirect";
	private static final String TAG_DIALOG = "about_dialog";

	private static final String TAG = LoginActivity.class.getSimpleName();
	private BetterProgressDialog mProgress;

	private View mSelectServer;

	private boolean mLoginCancel = false;

	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////// Override METHODS
	// ////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		Log.i("BeeeOn app starting...", "___________________________________");

		// Get controller
		Controller controller = Controller.getInstance(this);

		// Prepare progressDialog
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

		// Set login buttons listeners
		prepareLoginButtons();

		// Initialize server related views
		prepareServerSpinner();

		// Set logo on click listener to show about dialog
		findViewById(R.id.logo).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showAboutDialog();
			}
		});

		// Set choose server visibility
		boolean chooseServerEnabled = Controller.getInstance(this).getGlobalSettings().getBoolean(Constants.PERSISTENCE_PREF_LOGIN_CHOOSE_SERVER_MANUALLY, false);
		setSelectServerVisibility(chooseServerEnabled);

		// Intro to app
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		if (prefs != null && prefs.getBoolean(Constants.GUI_INTRO_PLAY, true)) {
			Log.d(TAG, "Go to INTRO");
			prefs.edit().putBoolean(Constants.GUI_INTRO_PLAY, false).apply();
			Intent intent = new Intent(this, IntroActivity.class);
			startActivity(intent);
			return;
		}

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
			prepareLogin(lastAuthProvider);
		}
	}

	private void prepareLoginButtons() {
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.login_btn_demo:
					{
						mProgress.setMessageResource(R.string.progress_loading_demo);
						prepareLogin(new DemoAuthProvider());
						return;
					}
					case R.id.login_btn_google:
					{
						prepareLogin(new GoogleAuthProvider());
						return;
					}
					case R.id.login_btn_facebook:
					{
						prepareLogin(new FacebookAuthProvider());
						return;
					}
					case R.id.login_btn_direct:
					{
						Toast.makeText(LoginActivity.this, R.string.toast_error_not_supported_yet, Toast.LENGTH_SHORT).show();
						return;
					}
					case R.id.login_btn_choose:
					{
						// Show choose dialog for other providers
						AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
						builder.setTitle(R.string.dialog_choose_provider_title);
						builder.setItems(new String[]{"MojeID"}, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (which == 0) {
									// MojeID
									Toast.makeText(LoginActivity.this, R.string.toast_error_not_supported_yet, Toast.LENGTH_SHORT).show();
									// prepareLogin(new MojeIdAuthProvider());
								}
							}
						});
						builder.show();
					}
				}
			}
		};

		findViewById(R.id.login_btn_demo).setOnClickListener(onClickListener);
		findViewById(R.id.login_btn_direct).setOnClickListener(onClickListener);
		findViewById(R.id.login_btn_google).setOnClickListener(onClickListener);
		findViewById(R.id.login_btn_facebook).setOnClickListener(onClickListener);
		findViewById(R.id.login_btn_choose).setOnClickListener(onClickListener);
	}

	private void prepareServerSpinner() {
		// Set server spinner items
		Spinner spinner = (Spinner) findViewById(R.id.spinner_select_server);

		// TODO: This is a bit dirty way to set the name. Use own adapter to have translated "(default)" sufix (and perhaps also server names itself) properly
		ArrayAdapter adapter = new ArrayAdapter<NetworkServer>(this, android.R.layout.simple_spinner_item, NetworkServer.values()) {
			private View changeText(View view, int position) {
				NetworkServer item = getItem(position);
				String name = item.getTranslatedName(LoginActivity.this);
				((TextView) view).setText(name);
				return view;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				return changeText(super.getView(position, convertView, parent), position);
			}

			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				return changeText(super.getDropDownView(position, convertView, parent), position);
			}
		};
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		String serverId = Persistence.loadLoginServerId(this);
		NetworkServer server = Utils.getEnumFromId(NetworkServer.class, serverId, NetworkServer.getDefaultServer());
		spinner.setSelection(server.ordinal());

		// Set choose server visibility
		boolean chooseServerEnabled = Controller.getInstance(this).getGlobalSettings().getBoolean(Constants.PERSISTENCE_PREF_LOGIN_CHOOSE_SERVER_MANUALLY, false);
		setSelectServerVisibility(chooseServerEnabled);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Prepare correct authProvider object
		IAuthProvider authProvider = null;

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
				String message = getString(R.string.toast_auth_provider_error);
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
				doLogin(authProvider);
				break;
			}
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.login_menu, menu);

		// Set choose server item (un)checked
		boolean checked = Controller.getInstance(this).getGlobalSettings().getBoolean(Constants.PERSISTENCE_PREF_LOGIN_CHOOSE_SERVER_MANUALLY, false);
		MenuItem item = menu.findItem(R.id.action_choose_server_manually);
		item.setChecked(checked);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
			case R.id.action_choose_server_manually:
			{
				boolean checked = !item.isChecked();
				item.setChecked(checked);
				Controller.getInstance(this).getGlobalSettings().edit().putBoolean(Constants.PERSISTENCE_PREF_LOGIN_CHOOSE_SERVER_MANUALLY, checked).apply();
				setSelectServerVisibility(checked);
				return true;
			}
			case R.id.action_about:
			{
				showAboutDialog();
				return true;
			}
		}

		return false;
	}

	private void setSelectServerVisibility(boolean visible) {
		if (mSelectServer == null) {
			mSelectServer = findViewById(R.id.select_server);
		}
		mSelectServer.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	private void showAboutDialog() {
		InfoDialogFragment dialog = new InfoDialogFragment();
		dialog.show(getSupportFragmentManager(), TAG_DIALOG);
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
		if (mSelectServer.getVisibility() == View.VISIBLE) {
			Spinner spinner = (Spinner) mSelectServer.findViewById(R.id.spinner_select_server);
			NetworkServer server = (NetworkServer) spinner.getSelectedItem();
			serverId = server != null ? server.getId() : "";
		}

		// After changing demo mode must be controller reloaded
		Controller.setDemoMode(this, demoMode, serverId);
	}

	/**
	 * Last logging method that call controller to proceed access to server
	 *
	 * @param authProvider to login with
	 */
	private void prepareLogin(final IAuthProvider authProvider) {
		final boolean demoMode = (authProvider instanceof DemoAuthProvider);
		if (!demoMode && !Utils.isInternetAvailable(this)) {
			mProgress.dismiss();
			Toast.makeText(this, getString(R.string.toast_internet_connection), Toast.LENGTH_LONG).show();
			return;
		}

		mLoginCancel = false;
		mProgress.setMessageResource(R.string.progress_signing);
		mProgress.show();

		authProvider.prepareAuth(LoginActivity.this);
	}

	private void doLogin(final IAuthProvider authProvider) {
		mProgress.setMessageResource(R.string.progress_signing);
		mProgress.show();

		new Thread(new Runnable() {
			@Override
			public void run() {
				setDemoMode(authProvider instanceof DemoAuthProvider);

				String errMessage = getString(R.string.toast_login_failed);
				boolean errFlag = true;

				try {
					Controller controller = Controller.getInstance(LoginActivity.this);

					// Here is authProvider already filled with needed parameters so we can send them to the server
					if (controller.login(authProvider)) {
						errFlag = false;

						// Load all gates and data for active one on login
						mProgress.setMessageResource(R.string.progress_loading_gates);
						controller.getGatesModel().reloadGates(true);

						Gate active = controller.getActiveGate();
						if (active != null) {
							// Load data for active gate
							mProgress.setMessageResource(R.string.progress_loading_gate);
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
								// User is not registered on server yet, show registration question dialog
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										showRegisterDialog(authProvider);
									}
								});
								return;
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
		}).start();
	}

	private void doRegister(final IAuthProvider authProvider) {
		mProgress.setMessageResource(R.string.progress_signup);
		mProgress.show();

		new Thread(new Runnable() {
			@Override
			public void run() {
				setDemoMode(authProvider instanceof DemoAuthProvider);

				String errMessage = getString(R.string.toast_registration_failed);
				boolean errFlag = true;

				try {
					Controller controller = Controller.getInstance(LoginActivity.this);

					// Here is authProvider already filled with needed parameters so we can send them to the server
					if (controller.register(authProvider)) {
						Log.d(TAG, "Register successful");
						errFlag = false;

						// Finish registration by start logging in
						if (!mLoginCancel) {
							doLogin(authProvider);
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
				}

				mProgress.dismiss();
			}
		}).start();
	}

	private void showRegisterDialog(final IAuthProvider authProvider) {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						doRegister(authProvider);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						mLoginCancel = true;
						mProgress.dismiss();
						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder
				.setTitle(R.string.dialog_register_new_account_title)
				.setMessage(R.string.dialog_register_new_account_message)
				.setPositiveButton(android.R.string.yes, dialogClickListener)
				.setNegativeButton(android.R.string.no, dialogClickListener)
				.show();
	}

	/**
	 * Finish this activity, dismiss progressDialog and if it's not redirect (set in Intent as BUNDLE_REDIRECT) then also start MainActivity
	 */
	private void onLoggedIn() {
		mProgress.dismiss();

		// Check if this is redirect (e.g., after connection loss) or classic start
		Bundle bundle = getIntent().getExtras();
		if (bundle == null || !bundle.getBoolean(BUNDLE_REDIRECT, false)) {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		}

		finish();
	}

}
