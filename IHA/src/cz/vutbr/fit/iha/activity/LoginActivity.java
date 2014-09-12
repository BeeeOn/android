package cz.vutbr.fit.iha.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.exception.NotImplementedException;
import cz.vutbr.fit.iha.gcm.GcmHelper;
import cz.vutbr.fit.iha.network.exception.CommunicationException;
import cz.vutbr.fit.iha.network.exception.NoConnectionException;
import cz.vutbr.fit.iha.network.exception.NotRegException;
import cz.vutbr.fit.iha.thread.ToastMessageThread;

/**
 * First sign in class, controls first activity
 * 
 * @author ThinkDeep
 * @author Leopold Podmolik
 * 
 */
public class LoginActivity extends BaseActivity {

	private Controller mController;
	private LoginActivity mActivity;
	private ProgressDialog mProgress;
	private Thread mDoGoogleLoginThread;
	private StoppableRunnable mDoGoogleLoginRunnable;

	private static final String TAG = LoginActivity.class.getSimpleName();
	public static final int USER_RECOVERABLE_AUTH = 5;
	private static final int GET_GOOGLE_ACCOUNT = 6;

	private boolean mIgnoreChange = false;
	private boolean mSignUp = false;

	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1;

	private Context mContext;

	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////// Override METHODS
	// ///////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// Get Activity
		mActivity = this;
		mContext = this;

		// Get controller
		mController = Controller.getInstance(this);

		// Prepare progress dialog
		mProgress = new ProgressDialog(mActivity);
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

		// try to register GCM
		if (GcmHelper.getGCMRegistrationId(mContext).isEmpty() && GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext()) == ConnectionResult.SUCCESS) {
			GcmHelper.registerGCMInBackground(mContext);
		}

		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		if (mController.isLoggedIn()) {
			// If we're already logged in, continue to location screen
			Log.d(TAG, "Already logged in, going to locations screen...");
			mController.initGoogle(this, mController.getLastEmail());

			Intent intent = new Intent(mActivity, LocationScreenActivity.class);

			mActivity.startActivity(intent);
			mActivity.finish();
			return;
		}

		String lastEmail = mController.getLastEmail();
		if (lastEmail.length() > 0) {
			// Automatic login with last used e-mail
			Log.d(TAG, String.format("Automatic login with last used e-mail (%s)...", lastEmail));

			mActivity.setDemoMode(false);
			mProgress.show();
			doGoogleLogin(lastEmail);
		}

		// Demo button
		((ImageButton) findViewById(R.id.login_btn_demo)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mActivity.setDemoMode(true);

				Intent intent = new Intent(LoginActivity.this, LocationScreenActivity.class);
				startActivity(intent);
				LoginActivity.this.finish();
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
				mActivity.setDemoMode(false);
				mProgress.show();
				beginGoogleAuthRutine(v);
				mIgnoreChange = false;
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
			ProgressDismiss();
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
				ProgressChangeText(getString(R.string.loading_data));
				Log.d(TAG, "user aproved, and token is tried to retake.");
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
		mActivity.finish();
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
	// /////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////

	protected void setDemoMode(boolean demoMode) {
		// After changing demo mode must be controller reloaded
		Controller.setDemoMode(this, demoMode);
		mController = Controller.getInstance(this);
	}

	/**
	 * Method cancel running progressBar, thread-safe
	 */
	public void ProgressDismiss() {
		new Thread() {
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (mProgress != null && mProgress.isShowing())
							mProgress.dismiss();
					}
				});
			}
		}.start();
	}

	/**
	 * Method show progress, thread-safe
	 */
	private void ProgressShow() {
		new Thread() {
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (mProgress != null)
							mProgress.show();
					}
				});
			}
		}.start();
	}

	/**
	 * Method set new text to progress, thread-safe
	 * 
	 * @param message
	 *            to show
	 */
	public void ProgressChangeText(final String message) {
		new Thread() {
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (mProgress != null)
							mProgress.setMessage(message);
					}
				});
			}
		}.start();
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
			// Toast.makeText(v.getContext(), getString(R.string.toast_play_missing), Toast.LENGTH_LONG).show();
			// Uri marketUri = Uri.parse("market://details?id=com.google.android.gms");
			// Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
			// startActivity(marketIntent);

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
		ProgressShow();
		if (!mController.isInternetAvailable()) {
			Toast.makeText(mActivity, getString(R.string.toast_internet_connection), Toast.LENGTH_LONG).show();
			ProgressDismiss();
			return;
		}
		// final GoogleAuth ggAuth = new GoogleAuth(this, email);
		mController.initGoogle(this, email);
		try {
			Log.d(TAG, "call google auth execute");

			mDoGoogleLoginRunnable = new StoppableRunnable() {

				@Override
				public void run() {
					String gcmId = GcmHelper.getGCMRegistrationId(mContext);
					// try to register again in 1 second, otherwise register in
					// separate thread
					if (gcmId.isEmpty()) {
						GcmHelper.registerGCMInForeground(mContext);
						gcmId = GcmHelper.getGCMRegistrationId(mContext);
						// if it still doesn't have GCM ID, try it repeatedly in
						// new thread
						if (gcmId.isEmpty()) {
							GcmHelper.registerGCMInBackground(mContext);
							Log.e(GcmHelper.TAG_GCM, "GCM ID is not accesible, creating new thread for ");
						}
					}

					Log.i(GcmHelper.TAG_GCM, "GCM ID: " + gcmId);

					// if(!ggAuth.doInForeground(true)){
					if (!mController.startGoogle(true, true)) { // returned value is from doInForeground only
						Log.e("Login", "exception in ggAuth");
						return;
					}

					// GoogleAuth ggAuth = GoogleAuth.getGetGoogleAuth();
					// ActualUser user = mController.getActualUser();
					// user.setName(ggAuth.getUserName());
					// user.setEmail(ggAuth.getEmail());
					// user.setPicture(ggAuth.getPictureIMG());

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
				// try {
				// GoogleAuth ggAuth = GoogleAuth.getGetGoogleAuth();
				// ActualUser user = mController.getActualUser();
				// user.setName(ggAuth.getUserName());
				// user.setEmail(ggAuth.getEmail());
				// user.setPicture(ggAuth.getPictureIMG());
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
				ProgressDismiss();
				if (!mDoGoogleLoginRunnable.isStopped()) {
					Intent intent = new Intent(mActivity, LocationScreenActivity.class);
					if (mSignUp) {
						Bundle bundle = new Bundle();
						bundle.putBoolean(Constants.NOADAPTER, true);
						intent.putExtras(bundle);
					}
					mActivity.startActivity(intent);
					mActivity.finish();
				}
			} else {
				Log.d(TAG, "Login: false");
				errFlag = true;
				errMessage = "Login failed";
			}
		} catch (NotRegException e) {
			e.printStackTrace();
			if (!mSignUp)
				doRegisterUser(email);

			// try { //FIXME: this is 2x here, fix this after demo
			// GoogleAuth ggAuth = GoogleAuth.getGetGoogleAuth();
			// ActualUser user = mController.getActualUser();
			// user.setName(ggAuth.getUserName());
			// user.setEmail(ggAuth.getEmail());
			// user.setPicture(ggAuth.getPictureIMG());
			// } catch (Exception e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }

			// there is unregistered adapter and we go to register it
			// FIXME: repair this after de
			// Intent intent = new Intent(LoginActivity.this, AddAdapterActivityDialog.class);
			// Intent intent = new Intent(LoginActivity.this, LocationScreenActivity.class);
			// Bundle bundle = new Bundle();
			// bundle.putBoolean(Constants.CANCEL, false);
			// intent.putExtras(bundle);
			// startActivity(intent);
			// } catch (NotRegBException e) {
			// e.printStackTrace();
			//
			// errFlag = true;
			// errMessage = getString(R.string.toast_no_unregistered_adapter);
		} catch (NoConnectionException e) {
			e.printStackTrace();

			errFlag = true;
			errMessage = getString(R.string.toast_internet_connection);
		} catch (CommunicationException e) {
			e.printStackTrace();

			errFlag = true;
			errMessage = getString(R.string.toast_communication_error);
		} catch (NotImplementedException e) {
			e.printStackTrace();
			// FIXME: remove in final version
			errFlag = true;
			errMessage = getString(R.string.toast_not_implemented);
		} finally {
			ProgressDismiss();
			if (errFlag) {
				// alternate form: //mActivity.runOnUiThread(new ToastMessageThread(mActivity, errMessage));
				new ToastMessageThread(mActivity, errMessage).start();
			}
		}
	}

	private void doRegisterUser(final String email) {
		mSignUp = true;
		ProgressChangeText(getString(R.string.progress_signup));
		ProgressShow();
		if (mController.registerUser(email)) {
			doLogin(email);
		} else {
			ProgressDismiss();
			new ToastMessageThread(mActivity, R.string.toast_something_wrong);
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////// TODO METHODS
	// /////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////

	private boolean getMojeIDAccessFromServer(View v) {
		// TODO: get access via mojeID
		Toast.makeText(v.getContext(), "Not Implemented yet", Toast.LENGTH_LONG).show();
		return false;
	}
}
