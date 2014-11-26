package cz.vutbr.fit.iha.activity;

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

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.base.BaseActivity;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.exception.ErrorCode;
import cz.vutbr.fit.iha.exception.IhaException;
import cz.vutbr.fit.iha.exception.NetworkError;
import cz.vutbr.fit.iha.exception.NotImplementedException;
import cz.vutbr.fit.iha.network.DemoNetwork;
import cz.vutbr.fit.iha.thread.ToastMessageThread;
import cz.vutbr.fit.iha.util.Log;

/**
 * First sign in class, controls first activity
 * 
 * @author ThinkDeep
 * @author Leopold Podmolik
 * 
 */
public class LoginActivity extends BaseActivity {

	public static final String BUNDLE_REDIRECT = "isRedirect";

	private Controller mController;
	private ProgressDialog mProgress;
	private Thread mDoGoogleLoginThread;
	private StoppableRunnable mDoGoogleLoginRunnable;

	private static final String TAG = LoginActivity.class.getSimpleName();
	public static final int USER_RECOVERABLE_AUTH = 5;
	private static final int GET_GOOGLE_ACCOUNT = 6;

	private boolean mIgnoreChange = false;
	private boolean mSignUp = false;

	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1;

	private boolean isRedirect = false;

	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////// Override METHODS
	// ////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// Check if this is redirect (e.g., after connection loss) or classic start
		Bundle bundle = getIntent().getExtras();
		isRedirect = (bundle != null && bundle.getBoolean(BUNDLE_REDIRECT, false));

		// Get controller
		mController = Controller.getInstance(getApplicationContext());

		// Prepare progress dialog
		mProgress = new ProgressDialog(this);
		mProgress.setMessage(getString(R.string.progress_signing));
		mProgress.setCancelable(true);
		mProgress.setCanceledOnTouchOutside(false);
		mProgress.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				if (mDoGoogleLoginRunnable != null) {
					mDoGoogleLoginRunnable.stop();
				}
			}
		});

		// FIXME commented for release
		// // try to register GCM
		// if (mController.getGCMRegistrationId().isEmpty() && GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext()) == ConnectionResult.SUCCESS) {
		// GcmHelper.registerGCMInBackground(this);
		// }

		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		if (mController.isLoggedIn()) {
			// If we're already logged in, continue to location screen
			Log.d(TAG, "Already logged in, going to locations screen...");
			mController.initGoogle(this, mController.getLastEmail());

			if (!isRedirect) {
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
			}

			finish();
			return;
		}

		String lastEmail = mController.getLastEmail();
		if (lastEmail.length() > 0) {
			// Automatic login with last used e-mail
			Log.d(TAG, String.format("Automatic login with last used e-mail (%s)...", lastEmail));

			setDemoMode(false);
			mProgress.show();
			doGoogleLogin(lastEmail);
		}

		// Demo button
		((ImageButton) findViewById(R.id.login_btn_demo)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mProgress.setMessage(LoginActivity.this.getString(R.string.progress_loading_demo));
				mProgress.show();

				new Thread(new Runnable() {
					@Override
					public void run() {
						setDemoMode(true);
						doLogin(DemoNetwork.DEMO_EMAIL);

						if (!isRedirect) {
							Intent intent = new Intent(LoginActivity.this, MainActivity.class);
							startActivity(intent);
						}

						mProgress.dismiss();
						finish();
					}
				}).start();
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
				setDemoMode(false);
				mProgress.show();
				beginGoogleAuthRutine(v);
				// mIgnoreChange = false;
			}
		});
		btnMojeID.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getMojeIDAccessFromServer(v);
			}
		});

		Log.i("IHA app starting...", "___________________________________");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_CANCELED) {
			if (mDoGoogleLoginRunnable != null) {
				mDoGoogleLoginRunnable.stop();
			}
			progressDismiss();
			return;
		}

		if (requestCode == USER_RECOVERABLE_AUTH && resultCode == RESULT_OK) {
			String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			if (email == null) {
				Log.d(TAG, "onActivityResult: no email");
				return;
			}
			try {
				mController.initGoogle(this, email);
				mController.startGoogle(false, true); // do NOT need check returned value, init is called line before
				progressChangeText(getString(R.string.loading_data));
				Log.d(TAG, "user aproved, and token is tried to retake.");
				doGoogleLogin(email);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (requestCode == GET_GOOGLE_ACCOUNT && resultCode == RESULT_OK) {
			String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			if (email == null) {
				Log.d(TAG, "onActivityResult: no email");
				return;
			}
			Log.i(TAG, "Go do google login again.");
			doGoogleLogin(email);
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

	/**
	 * Method start routine to access trough google after button click
	 * 
	 * @param v
	 */
	private void beginGoogleAuthRutine(View v) {
		Log.d(TAG, "BEG: Google access func");
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
		if (resultCode == ConnectionResult.SUCCESS) {
			// On this device is Google Play, we can proceed
			Log.d(TAG, "On this device is Google Play, we can proceed");
			String[] Accounts = this.getAccountNames();
			if (Accounts.length == 1) {
				Log.d(TAG, "On this device is one account");
				doGoogleLogin(Accounts[0]);
			} else {
				Log.d(TAG, "On this device are more accounts");
				Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[] { "com.google" }, false, null, null, null, null);
				startActivityForResult(intent, GET_GOOGLE_ACCOUNT);
			}
		} else {
			// Google Play is missing
			Log.d(TAG, "Google Play Services is missing or not allowed");

			GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			// 20.8. 2014 Martin changed it to system supported dialog which should solve the problem
			mProgress.dismiss();
		}
		Log.d(TAG, "END: Google access func");
	}

	/**
	 * Method create one thread to get google token and than call last logging method
	 * 
	 * @param email
	 *            of user
	 */
	private void doGoogleLogin(final String email) {
		progressShow();
		if (!mController.isInternetAvailable()) {
			Toast.makeText(this, getString(R.string.toast_internet_connection), Toast.LENGTH_LONG).show();
			progressDismiss();
			return;
		}

		mController.initGoogle(this, email);
		try {
			Log.d(TAG, "call google auth execute");

			mDoGoogleLoginRunnable = new StoppableRunnable() {

				@Override
				public void run() {
					// String gcmId = mController.getGCMRegistrationId();
					// // try to register again in 1 second, otherwise register in
					// // separate thread
					// if (gcmId.isEmpty()) {
					// GcmHelper.registerGCMInForeground(LoginActivity.this);
					// gcmId = mController.getGCMRegistrationId();
					// // if it still doesn't have GCM ID, try it repeatedly in
					// // new thread
					// if (gcmId.isEmpty()) {
					// GcmHelper.registerGCMInBackground(LoginActivity.this);
					// Log.e(GcmHelper.TAG_GCM, "GCM ID is not accesible, creating new thread for ");
					// }
					// }
					//
					// Log.i(GcmHelper.TAG_GCM, "GCM ID: " + gcmId);

					if (!mController.startGoogle(true, true)) { // returned value is from doInForeground only
						Log.e("Login", "exception in ggAuth");
						return;
					}

					doLogin(email);

					Log.d(TAG, "Finish google auth");
				}
			};

			mDoGoogleLoginThread = new Thread(mDoGoogleLoginRunnable);
			mDoGoogleLoginThread.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private abstract class StoppableRunnable implements Runnable {
		private volatile boolean mIsStopped = false;

		public boolean isStopped() {
			return mIsStopped;
		}

		private void setStopped(boolean isStop) {
			if (mIsStopped != isStop)
				mIsStopped = isStop;
		}

		public void stop() {
			setStopped(true);
		}
	}

	/**
	 * Last logging method that call controller to proceed access to server
	 * 
	 * @param email
	 *            of user
	 */
	private void doLogin(final String email) {
		String errMessage = null;
		boolean errFlag = false;

		try {
			if (mController.login(email)) {
				Log.d(TAG, "Login: true");

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

				if (mDoGoogleLoginRunnable != null && !mDoGoogleLoginRunnable.isStopped()) {
					if (!isRedirect) {
						Intent intent = new Intent(this, MainActivity.class);
						startActivity(intent);
					}

					finish();
				}
			} else {
				Log.d(TAG, "Login: false");
				errFlag = true;
				errMessage = "Login failed";
			}
		} catch (IhaException e) {
			e.printStackTrace();
			
			ErrorCode code = e.getErrorCode();
			if (code instanceof NetworkError && code == NetworkError.NOT_VALID_USER && !mSignUp) {
				doRegisterUser(email);
			} else {
				errFlag = true;
				errMessage = e.getTranslatedErrorMessage(this);
			}
		} catch (NotImplementedException e) {
			e.printStackTrace();

			// FIXME: remove in final version
			errFlag = true;
			errMessage = getString(R.string.toast_not_implemented);
		} finally {
			progressDismiss();
			if (errFlag) {
				// alternate form: //mActivity.runOnUiThread(new ToastMessageThread(mActivity, errMessage));
				new ToastMessageThread(this, errMessage).start();
			}
		}
	}

	private void doRegisterUser(final String email) {
		mSignUp = true;
		progressChangeText(getString(R.string.progress_signup));
		progressShow();
		if (mController.registerUser(email)) {
			doLogin(email);
		} else {
			progressDismiss();
			new ToastMessageThread(this, R.string.toast_something_wrong);
		}
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
