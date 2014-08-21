package cz.vutbr.fit.iha.activity;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.dialog.AddAdapterActivityDialog;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.exception.NotImplementedException;
import cz.vutbr.fit.iha.household.ActualUser;
import cz.vutbr.fit.iha.network.GetGoogleAuth;
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
	private static final String TAG_GCM = "IHA_GCM";
	public static final int USER_RECOVERABLE_AUTH = 5;
	private static final int GET_GOOGLE_ACCOUNT = 6;

	private boolean mIgnoreChange = false;

	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1;
	
    private GoogleCloudMessaging mGcm;
    private Context mContext;
    private String mRegid;
	
	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////// Override METHODS
	// ///////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////

	protected void setDemoMode(boolean demoMode) {
		// After changing demo mode must be controller reloaded
		Controller.setDemoMode(this, demoMode);
		mController = Controller.getInstance(this);
	}

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
		mGcm = GoogleCloudMessaging.getInstance(this);
		if (getGCMRegistrationId().isEmpty() && GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext())== ConnectionResult.SUCCESS) {
			registerGCMInBackground();
		}
		
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		if (mController.isLoggedIn()) {
			// If we're already logged in, continue to location screen
			Log.d(TAG, "Already logged in, going to locations screen...");
			new GetGoogleAuth(this, mController.getLastEmail());

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
		
		Log.i("IHA app starting...","___________________________________");
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
				new GetGoogleAuth(this, email);
				GetGoogleAuth.getGetGoogleAuth().execute();
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
	 * @param message
	 * 				to show
	 */
	public void ProgressChangeText(final String message){
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
			
			GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                    PLAY_SERVICES_RESOLUTION_REQUEST).show();
			// 20.8. 2014 Martin changed it to system supported dialog which should solve the problem
//			Toast.makeText(v.getContext(), getString(R.string.toast_play_missing), Toast.LENGTH_LONG).show();
//			Uri marketUri = Uri.parse("market://details?id=com.google.android.gms");
//			Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
//			startActivity(marketIntent);
			
			
			
			mProgress.dismiss();
		}
		Log.d(TAG, "END: Google access func");
	}

	/**
	 * Method create (finally only) one thread to get google token and than call
	 * last logging method
	 * 
	 * @param email
	 *            of user
	 */
	private void doGoogleLogin(final String email) {
		ProgressShow();
		if(!mController.isInternetAvailable()){
			Toast.makeText(mActivity, getString(R.string.toast_internet_connection), Toast.LENGTH_LONG).show();
			ProgressDismiss();
			return;
		}
		final GetGoogleAuth ggAuth = new GetGoogleAuth(this, email);
		try {
			Log.d(TAG, "call google auth execute");

			mDoGoogleLoginRunnable = new StoppableRunnable() {

				@Override
				public void run() {
					if(!ggAuth.doInForeground(true)){
						Log.e("Login", "exception in ggAuth");
						return;
					}
					// FIXME: I think name and email should be saved on IHA
					// server and loaded from there. It should be used from
					// google only in registration, no?
					
					doLogin(email);
					
					String gcmId = getGCMRegistrationId();
					if (!gcmId.isEmpty()) {
						Log.i(TAG_GCM, "GCM ID: " + gcmId);
						
						// TODO: register in server
					}
					
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
				try {
					GetGoogleAuth ggAuth = GetGoogleAuth.getGetGoogleAuth();
					ActualUser user = mController.getActualUser();
					user.setName(ggAuth.getUserName());
					user.setEmail(ggAuth.getEmail());
					user.setPicture(ggAuth.getPictureIMG());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				ProgressDismiss();
				if (!mDoGoogleLoginRunnable.isStopped()) {
					Intent intent = new Intent(mActivity, LocationScreenActivity.class);
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
			try { //FIXME: this is 2x here, fix this after demo
				GetGoogleAuth ggAuth = GetGoogleAuth.getGetGoogleAuth();
				ActualUser user = mController.getActualUser();
				user.setName(ggAuth.getUserName());
				user.setEmail(ggAuth.getEmail());
				user.setPicture(ggAuth.getPictureIMG());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			
			// there is unregistered adapter and we go to register it
			//FIXME: repair this after de
			Intent intent = new Intent(LoginActivity.this, AddAdapterActivityDialog.class);
//			Intent intent = new Intent(LoginActivity.this, LocationScreenActivity.class);
			Bundle bundle = new Bundle();
			bundle.putBoolean(Constants.CANCEL, false);
			intent.putExtras(bundle);
			startActivity(intent);
//		} catch (NotRegBException e) {
//			e.printStackTrace();
//
//			errFlag = true;
//			errMessage = getString(R.string.toast_no_unregistered_adapter);
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
	
	// //////////////////////////////////////////////////////////////////////////////////
	//////////////////////  GCM METHODS
	/////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	protected SharedPreferences getGCMPreferences() {
	    // This sample app persists the registration ID in shared preferences, but
	    // how you store the regID in your app is up to you.
	    return getSharedPreferences(Constants.SHARED_PREF_GCM_NAME,
	            Context.MODE_PRIVATE);
	}
	
	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getGCMRegistrationId() {
	    final SharedPreferences prefs = getGCMPreferences();
	    String registrationId = prefs.getString(Constants.PREF_GCM_REG_ID, "");
	    if (registrationId.isEmpty()) {
	        Log.i(TAG_GCM, "GCM: Registration not found.");
	        return "";
	    }
	    // Check if app was updated; if so, it must clear the registration ID
	    // since the existing regID is not guaranteed to work with the new
	    // app version.
	    int registeredVersion = prefs.getInt(Constants.PREF_GCM_APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion(mContext);
	    if (registeredVersion != currentVersion) {
	        Log.i(TAG_GCM, "GCM: App version changed.");
	        return "";
	    }
	    return registrationId;
	}
	
	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}
	
	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerGCMInBackground() {
	    new AsyncTask<Void, Void, String>() {
	        @Override
	        protected String doInBackground(Void... params) {
	            String msg = "";
	            try {
	                if (mGcm == null) {
	                    mGcm = GoogleCloudMessaging.getInstance(mContext);
	                }
	                mRegid = mGcm.register(Constants.PROJECT_NUMBER);
	                msg = "Device registered, registration ID=" + mRegid;

	                // You should send the registration ID to your server over HTTP,
	                // so it can use GCM/HTTP or CCS to send messages to your app.
	                // The request to your server should be authenticated if your app
	                // is using accounts.
	                sendGCMRegistrationIdToBackend();

	                // For this demo: we don't need to send it because the device
	                // will send upstream messages to a server that echo back the
	                // message using the 'from' address in the message.

	                // Persist the regID - no need to register again.
	                storeGCMRegistrationId(mContext, mRegid);
	            } catch (IOException ex) {
	                msg = "Error :" + ex.getMessage();
	                // If there is an error, don't just keep trying to register.
	                // Require the user to click a button again, or perform
	                // exponential back-off.
	            }
	            return msg;
	        }

	        @Override
	        protected void onPostExecute(String msg) {
	            Log.i(TAG_GCM, msg);
	        }
	    }.execute(null, null, null);
	}
	
	/**
	 * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
	 * or CCS to send messages to your app. Not needed for this demo since the
	 * device sends upstream messages to a server that echoes back the message
	 * using the 'from' address in the message.
	 */
	private void sendGCMRegistrationIdToBackend() {
	    // Your implementation here.
	}
	
	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context application's context.
	 * @param regId registration ID
	 */
	private void storeGCMRegistrationId(Context context, String regId) {
	    final SharedPreferences prefs = getGCMPreferences();
	    int appVersion = getAppVersion(context);
	    Log.i(TAG_GCM, "Saving regId on app version " + appVersion);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(Constants.PREF_GCM_REG_ID, regId);
	    editor.putInt(Constants.PREF_GCM_APP_VERSION, appVersion);
	    editor.commit();
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
