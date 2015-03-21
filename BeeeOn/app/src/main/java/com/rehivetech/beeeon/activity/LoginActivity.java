package com.rehivetech.beeeon.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.base.BaseActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ErrorCode;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.exception.NotImplementedException;
import com.rehivetech.beeeon.network.DemoNetwork;
import com.rehivetech.beeeon.network.GoogleAuthHelper;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;

/**
 * First sign in class, controls first activity
 * 
 * @author ThinkDeep
 * @author Leopold Podmolik
 * 
 */
public class LoginActivity extends BaseActivity {

	public static final String BUNDLE_REDIRECT = "isRedirect";
	
	private static final String TAG = LoginActivity.class.getSimpleName();	
	
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1;
	private static final int RESULT_DO_RECOVERABLE_AUTH = 5;
	private static final int RESULT_GET_GOOGLE_ACCOUNT = 6;
	private static final int RESULT_DO_WEBLOGIN = 7;
	
	private Controller mController;
	private LoginActivity mActivity;
	private ProgressDialog mProgress;
	
	private boolean mIgnoreChange = false;

	private boolean mLoginCancel = false;
	private boolean mIsRedirect = false;

	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////// Override METHODS
	// ////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// Check if this is redirect (e.g., after connection loss) or classic start
		Bundle bundle = getIntent().getExtras();
		mIsRedirect = (bundle != null && bundle.getBoolean(BUNDLE_REDIRECT, false));

		// Get controller
		mController = Controller.getInstance(getApplicationContext());
		//mController = new Controller(getApplicationContext());
		
		mActivity = this;

		// Prepare progress dialog
		mProgress = new ProgressDialog(this);
		mProgress.setMessage(getString(R.string.progress_signing));
		mProgress.setCancelable(true);
		mProgress.setCanceledOnTouchOutside(false);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgress.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				mLoginCancel = true;
			}
		});

		// Demo button
		((ImageButton) findViewById(R.id.login_btn_demo)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mIgnoreChange = true;
				mProgress.setMessage(LoginActivity.this.getString(R.string.progress_loading_demo));
				doLogin(true, DemoNetwork.DEMO_EMAIL);
				// mIgnoreChange = false;
			}
		});

		// Get btn for login
		ImageButton btnGoogle = (ImageButton) findViewById(R.id.login_btn_google);
		ImageButton btnMojeID = (ImageButton) findViewById(R.id.login_btn_mojeid);

		// Set onClickListener
		btnGoogle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mIgnoreChange = true;
				beginGoogleAuthRoutine();
				// mIgnoreChange = false;
			}
		});
		btnMojeID.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getMojeIDAccessFromServer(v);
			}
		});
		


		// ALREADY LOGGED IN
		if (mController.isLoggedIn()) {
			// If we're already logged in, continue to location screen
			Log.d(TAG, "Already logged in, going to locations screen...");

			if (!mIsRedirect) {
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
			}

			finish();
			return;
		}

		// AUTOMATIC LOGIN
		String lastEmail = mController.getLastEmail();
		if (lastEmail.length() > 0 && !lastEmail.equals(DemoNetwork.DEMO_EMAIL)) {
			// Automatic login with last used e-mail
			Log.d(TAG, String.format("Automatic login with last used e-mail (%s)...", lastEmail));
			doLogin(false, lastEmail);
		}
		
		Log.i("BeeeOn app starting...", "___________________________________");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.d(TAG, String.format("onActivityResult: %d, %d", requestCode, resultCode));

		if (resultCode == RESULT_CANCELED) {
			mLoginCancel = true;
			progressDismiss();
			return;
		}

		if (resultCode == RESULT_OK && requestCode == RESULT_DO_WEBLOGIN) {
			String token = data.getStringExtra(WebLoginActivity.TOKEN_VALUE);
			if (token == null) {
				Log.d(TAG, "no token received");
				progressDismiss();
				return;
			}

			progressChangeText(getString(R.string.loading_data));
			Log.i(TAG, "Access Google by token");
			doLoginByToken(token);
		}

		if (resultCode == RESULT_OK && (requestCode == RESULT_DO_RECOVERABLE_AUTH || requestCode == RESULT_GET_GOOGLE_ACCOUNT)) {
			String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			if (email == null) {
				Log.d(TAG, "onActivityResult: no email");
				progressDismiss();
				return;
			}

			progressChangeText(getString(R.string.loading_data));
			Log.i(TAG, "Do Google login");
			doLogin(false, email);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(TAG, "Ignore change orientation ?");
		// ignore orientation change
		if (!mIgnoreChange) {
			super.onConfigurationChanged(newConfig);
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////// Custom METHODS
	// ////////////////////////////////////////////////////////////////////////////////////

	protected void setDemoMode(boolean demoMode) {
		// After changing demo mode must be controller reloaded
		Controller.setDemoMode(getApplicationContext(), demoMode);
		mController = Controller.getInstance(getApplicationContext());
	}

	/**
	 * Method cancel running progressBar, thread-safe
	 */
	public void progressDismiss() {
		if (mProgress != null && mProgress.isShowing()) {
			try {
				mProgress.dismiss();
			} catch (Exception e) {
				Log.d(TAG, "Dialog is not showing, but dialog say that is show :/");
				e.printStackTrace();
			}
		}

		// Enable orientation change again
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}

	/**
	 * Method show progress, thread-safe
	 */
	private void progressShow() {
		// Disable orientation change
		int currentOrientation = getResources().getConfiguration().orientation;
		if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		}

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mProgress != null)
					mProgress.show();
			}
		});
	}

	/**
	 * Method set new text to progress, thread-safe
	 * 
	 * @param message
	 *            to show
	 */
	public void progressChangeText(final String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mProgress != null)
					mProgress.setMessage(message);
			}
		});
	}

	/**
	 * Method mine users account names
	 * 
	 * @return array of names to choose
	 */
	private String[] getAccountNames() {
		AccountManager mAccountManager = AccountManager.get(this);
		Account[] accounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
		String[] names = new String[accounts.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = accounts[i].name;
		}
		return names;
	}
	
	private boolean checkInternetConnection() {
		boolean available = mController.isInternetAvailable(); 
		if (!available) {
			progressDismiss();
			Toast.makeText(this, getString(R.string.toast_internet_connection), Toast.LENGTH_LONG).show();			
		}
		return available;
	}

	private boolean isGoogleLoginAvailable() {
		if (Utils.isBlackBerry())
			return false;

		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
		return resultCode == ConnectionResult.SUCCESS;
	}
	
	/**
	 * Method start routine to access trough google after button click
	 */
	private void beginGoogleAuthRoutine() {
		if (!checkInternetConnection())
			return;
		
		if (!isGoogleLoginAvailable())
			beginWebLoginAuth();
		else
			beginAndroidGoogleAuth();
	}

	private void beginWebLoginAuth() {
		Log.d(TAG, "Start WebLoginAuth");
		mProgress.show();

		final String redirect = "http://localhost";
		final String googleId = "863203863728-i8u7m601c85uq70v7g5jtdcjesr8dnqm.apps.googleusercontent.com";
		final String googleSecret = "ZEv4V6XBqCSRDbPtmHLZDLoR";
		final String tokenUrl = "https://accounts.google.com/o/oauth2/token";

		StringBuilder url = new StringBuilder();
		url.append("https://accounts.google.com/o/oauth2/auth?client_id=");
		url.append(Utils.uriEncode(googleId));
		url.append("&scope=openid%20email%20profile");
		url.append("&redirect_uri=");
		url.append(Utils.uriEncode(redirect));
		url.append("&state=foobar");
		url.append("&response_type=code");

		final Intent intent = new Intent(getApplicationContext(), WebLoginActivity.class);
		intent.putExtra(WebLoginActivity.LOGIN_URL, url.toString());
		intent.putExtra(WebLoginActivity.TOKEN_URL, tokenUrl);
		intent.putExtra(WebLoginActivity.CLIENT_ID, googleId);
		intent.putExtra(WebLoginActivity.CLIENT_SECRET, googleSecret);
		intent.putExtra(WebLoginActivity.REDIRECT_URI, redirect);
		intent.putExtra(WebLoginActivity.GRANT_TYPE, "authorization_code");
		startActivityForResult(intent, RESULT_DO_WEBLOGIN);

		Log.d(TAG, "Finish WebLoginAuth");
	}

	private void beginAndroidGoogleAuth() {
		Log.d(TAG, "Start GoogleAuthRoutine");
		mProgress.show();
				
		// On this device is Google Play, we can proceed
		Log.d(TAG, "On this device is Google Play, we can proceed");
		String[] Accounts = this.getAccountNames();
		Log.d(TAG, String.format("Number of accounts on this device: %d", Accounts.length));

		if (Accounts.length == 1) {
			doLogin(false, Accounts[0]);
		} else {
			Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[] { "com.google" }, false, null, null, null, null);
			startActivityForResult(intent, RESULT_GET_GOOGLE_ACCOUNT);
		}

		Log.d(TAG, "Finish GoogleAuthRoutine");
	}

	private void doLoginByToken(final String token) {
		Log.i(TAG, "LoginByToken started");

		mLoginCancel = false;
		progressShow();

		new Thread(new Runnable() {
			@Override
			public void run() {
				mController.beginPersistentConnection();
				mController.assignToken(token);
				progressChangeText(getString(R.string.progress_loading_adapters));
				mController.reloadAdapters(true);

				Adapter active = mController.getActiveAdapter();
				if (active != null) {
					// Load data for active adapter
					progressChangeText(getString(R.string.progress_loading_adapter));
					mController.reloadLocations(active.getId(), true);
					mController.reloadFacilitiesByAdapter(active.getId(), true);
				}

				if (!mIsRedirect) {
					Intent intent = new Intent(getApplicationContext(), MainActivity.class);
					startActivity(intent);
				}

				Log.i(TAG, "Login finished");
				progressDismiss();
				finish();
			}
		}).start();
	}

	/**
	 * Last logging method that call controller to proceed access to server
	 * 
	 * @param demoMode
	 * @param email
	 *            of user
	 */
	private void doLogin(final boolean demoMode, final String email) {
		if (!demoMode && !checkInternetConnection())
			return;

		mLoginCancel = false;
		progressShow();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				setDemoMode(demoMode);

				String errMessage = "Login failed";
				boolean errFlag = true;
		
				try {
					mController.beginPersistentConnection();
					Log.i(TAG, "Login started");

					if (mController.login(email)) {
						Log.d(TAG, "Login: true");
						errFlag = false;

						// Load all adapters and data for active one on login
						progressChangeText(getString(R.string.progress_loading_adapters));
						mController.reloadAdapters(true);

						Adapter active = mController.getActiveAdapter();
						if (active != null) {
							// Load data for active adapter
							progressChangeText(getString(R.string.progress_loading_adapter));
							mController.reloadLocations(active.getId(), true);
							mController.reloadFacilitiesByAdapter(active.getId(), true);
						}

						if (mLoginCancel) {
							// User cancelled login so do logout() to be sure it won't try to login automatically next time
							mController.logout();
						} else {
							// Open MainActivity or just this LoginActivity and let it redirect back
							if (!mIsRedirect) {
								Intent intent = new Intent(getApplicationContext(), MainActivity.class);
								startActivity(intent);
							}

							progressDismiss();
							finish();
							return;
						}
					}

					Log.i(TAG, "Login finished");
				} catch (AppException e) {
					ErrorCode errorCode = e.getErrorCode();
					if (errorCode instanceof NetworkError && errorCode == NetworkError.GOOGLE_TRY_AGAIN) {
						Intent intent = e.get(GoogleAuthHelper.RECOVERABLE_INTENT);
						if (intent != null) {
							startActivityForResult(intent, LoginActivity.RESULT_DO_RECOVERABLE_AUTH);
							return;
						}
					}
					
					e.printStackTrace();
					errMessage = e.getTranslatedErrorMessage(getApplicationContext());
				} catch (NotImplementedException e) {
					e.printStackTrace();
					errMessage = getString(R.string.toast_not_implemented);
				} catch (Exception e) {
					e.printStackTrace();
					errMessage = getString(R.string.toast_login_failed);
				} finally {
                    mController.endPersistentConnection();
                }

				progressDismiss();
				if (errFlag) {
					final Toast toast = Toast.makeText(LoginActivity.this, errMessage, Toast.LENGTH_LONG);

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							toast.show();
						}
					});
				}
			}
		}).start();
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////// TODO METHODS
	// ////////////////////////////////////////////////////////////////////////////////////

	private boolean getMojeIDAccessFromServer(View v) {
		// TODO: get access via mojeID
		Toast.makeText(v.getContext(), "Not Implemented yet", Toast.LENGTH_LONG).show();
		return false;
	}
}
