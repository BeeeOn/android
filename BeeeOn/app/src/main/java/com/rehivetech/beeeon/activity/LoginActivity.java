package com.rehivetech.beeeon.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.base.BaseActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ErrorCode;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.network.authentication.DemoAuthProvider;
import com.rehivetech.beeeon.network.authentication.GoogleAuthProvider;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.util.BetterProgressDialog;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;

/**
 * First sign in class, controls first activity
 * 
 * @author ThinkDeep
 * @author Leopold Podmolik
 * @author Robyer
 */
public class LoginActivity extends BaseActivity {

	public static final String BUNDLE_REDIRECT = "isRedirect";
	
	private static final String TAG = LoginActivity.class.getSimpleName();
	
	private Controller mController;
	private BetterProgressDialog mProgress;

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
		mController = Controller.getInstance(this);


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

		// Demo button
		((ImageButton) findViewById(R.id.login_btn_demo)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mProgress.setMessageResource(R.string.progress_loading_demo);
				prepareLogin(new DemoAuthProvider());
			}
		});

		// Get btn for login
		ImageButton btnGoogle = (ImageButton) findViewById(R.id.login_btn_google);
		ImageButton btnMojeID = (ImageButton) findViewById(R.id.login_btn_mojeid);

		// Set onClickListener
		btnGoogle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				prepareLogin(new GoogleAuthProvider());
			}
		});
		btnMojeID.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(v.getContext(), "Not Implemented yet", Toast.LENGTH_LONG).show();
				// prepareLogin(new MojeIdAuthProvider());
			}
		});

		// Intro to app
		SharedPreferences prefs = getPreferences( MODE_PRIVATE);
		if(prefs != null && prefs.getBoolean(Constants.GUI_INTRO_PLAY,true)) {
			Log.d(TAG,"Go to INTRO");
			prefs.edit().putBoolean(Constants.GUI_INTRO_PLAY,false).commit();
			Intent intent = new Intent(this, IntroActivity.class);
			startActivity(intent);
			return;
		}

		// Check already logged in user
		if (mController.isLoggedIn()) {
			Log.d(TAG, "Already logged in, going to locations screen...");
			onLoggedIn(); // finishes this activity
			return;
		}

		// Do automatic login if we have remembered last logged in user
		IAuthProvider lastAuthProvider = mController.getLastAuthProvider();
		if (lastAuthProvider != null && !(lastAuthProvider instanceof DemoAuthProvider)) {
			// Automatic login with last used provider
			Log.d(TAG, String.format("Automatic login with last provider '%s' and user '%s'...", lastAuthProvider.getProviderName(), lastAuthProvider.getPrimaryParameter()));
			prepareLogin(lastAuthProvider);
		}
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
			default: {
				Log.e(TAG, String.format("Unknown requestCode (%d)", requestCode));
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

	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////// Custom METHODS
	// ////////////////////////////////////////////////////////////////////////////////////

	protected void setDemoMode(boolean demoMode) {
		// After changing demo mode must be controller reloaded
		Controller.setDemoMode(getApplicationContext(), demoMode);
		mController = Controller.getInstance(this);
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
		Log.d(TAG, "doLogin()");

		mProgress.setMessageResource(R.string.progress_signing);
		mProgress.show();

		new Thread(new Runnable() {
			@Override
			public void run() {
				setDemoMode(authProvider instanceof DemoAuthProvider);

				String errMessage = getString(R.string.toast_login_failed);
				boolean errFlag = true;

				try {
					mController.beginPersistentConnection();
					Log.i(TAG, "Login started");

					// Here is authProvider already filled with needed parameters so we can send them to the server
					if (mController.login(authProvider)) {
						Log.d(TAG, "Login successful");
						errFlag = false;

						// Load all adapters and data for active one on login
						mProgress.setMessageResource(R.string.progress_loading_adapters);
						mController.reloadAdapters(true);

						Adapter active = mController.getActiveAdapter();
						if (active != null) {
							// Load data for active adapter
							mProgress.setMessageResource(R.string.progress_loading_adapter);
							mController.reloadLocations(active.getId(), true);
							mController.reloadFacilitiesByAdapter(active.getId(), true);
						}

						if (mLoginCancel) {
							// User cancelled login so do logout() to be sure it won't try to login automatically next time
							mController.logout();
						} else {
							// Open MainActivity or just this LoginActivity and let it redirect back
							onLoggedIn(); // finishes this activity
							return;
						}
					}

					Log.i(TAG, "Login finished");
				} catch (AppException e) {
					ErrorCode errorCode = e.getErrorCode();

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
					errMessage = e.getTranslatedErrorMessage(getApplicationContext());
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					mController.endPersistentConnection();
				}

				if (errFlag) {
					Utils.showToastOnUiThread(LoginActivity.this, errMessage, Toast.LENGTH_LONG);
				}

				mProgress.dismiss();
			}
		}).start();
	}

	private void doRegister(final IAuthProvider authProvider) {
		Log.d(TAG, "doRegister()");

		mProgress.setMessageResource(R.string.progress_signup);
		mProgress.show();

		new Thread(new Runnable() {
			@Override
			public void run() {
				setDemoMode(authProvider instanceof DemoAuthProvider);

				String errMessage = getString(R.string.toast_registration_failed);
				boolean errFlag = true;

				try {
					// Here is authProvider already filled with needed parameters so we can send them to the server
					if (mController.register(authProvider)) {
						Log.d(TAG, "Register successful");
						errFlag = false;

						// Finish registration by start logging in
						if (!mLoginCancel) {
							doLogin(authProvider);
						}

						return;
					}
				} catch (AppException e) {
					ErrorCode errorCode = e.getErrorCode();

					// TODO: handle some known exceptions

					e.printStackTrace();
					errMessage = e.getTranslatedErrorMessage(getApplicationContext());
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
				switch (which){
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
