package cz.vutbr.fit.iha.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.base.BaseActivity;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.exception.ErrorCode;
import cz.vutbr.fit.iha.exception.IhaException;
import cz.vutbr.fit.iha.exception.NetworkError;
import cz.vutbr.fit.iha.exception.NotImplementedException;
import cz.vutbr.fit.iha.network.DemoNetwork;
import cz.vutbr.fit.iha.network.GoogleAuthHelper;
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
	
	private static final String TAG = LoginActivity.class.getSimpleName();	
	
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1;
	private static final int RESULT_DO_RECOVERABLE_AUTH = 5;
	private static final int RESULT_GET_GOOGLE_ACCOUNT = 6;
	
	private Controller mController;
	private LoginActivity mActivity;
	private ProgressDialog mProgress;
	
	private boolean mIgnoreChange = false;

	private boolean mLoginCancel = false;
	private boolean mIsRedirect = false;

	private ShowcaseView mSV;
	private RelativeLayout.LayoutParams lps;
	private int mTutorialClick = 0;
	
	// For tutorial
	private boolean mFirstUse = true;

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
		
		mActivity= this;

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

		String lastEmail = mController.getLastEmail();
		if (lastEmail.length() > 0 && lastEmail != DemoNetwork.DEMO_EMAIL) {
			// Automatic login with last used e-mail
			Log.d(TAG, String.format("Automatic login with last used e-mail (%s)...", lastEmail));
			doLogin(false, lastEmail);
		}

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
		
		
		// TUTORIAL 
		// Need use default preferences because user isnt ready
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		if (prefs != null) {
			mFirstUse = prefs.getBoolean(Constants.TUTORIAL_LOGIN_SHOWED, true);
		}
		
		if(mFirstUse){
			showTutorial();
			if(prefs != null) {
				prefs.edit().putBoolean(Constants.TUTORIAL_LOGIN_SHOWED, false).commit();
			}
		}

		Log.i("IHA app starting...", "___________________________________");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_CANCELED) {
			mLoginCancel = true;
			progressDismiss();
			return;
		}

		if (resultCode == RESULT_OK && (requestCode == RESULT_DO_RECOVERABLE_AUTH || requestCode == RESULT_GET_GOOGLE_ACCOUNT)) {
			String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			if (email == null) {
				Log.d(TAG, "onActivityResult: no email");
				return;
			}
			try {
				progressChangeText(getString(R.string.loading_data));
				Log.i(TAG, "Do Google login");
				doLogin(false, email);
			} catch (Exception e) {
				e.printStackTrace();
			}
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
	
	
	private void showTutorial() {
		// TODO Auto-generated method stub
		lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		int marginPixel = 0;
		int currentOrientation = getResources().getConfiguration().orientation;
		if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			marginPixel = 15;
		}
		else{
			lps.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			marginPixel= 40;
		}
		
		
		int margin = ((Number) (getResources().getDisplayMetrics().density * marginPixel)).intValue();
		lps.setMargins(margin, margin, margin, margin);
		ViewTarget target_google = new ViewTarget(R.id.login_btn_google, this);
		
		OnShowcaseEventListener	listener = new OnShowcaseEventListener() {
			
			@Override
			public void onShowcaseViewShow(ShowcaseView showcaseView) {
				Log.d(TAG, "OnShowCase show");
				
			}
			
			@Override
			public void onShowcaseViewHide(ShowcaseView showcaseView) {
				Log.d(TAG, "OnShowCase hide");
				if(mTutorialClick == 1){
					ViewTarget target = new ViewTarget(R.id.login_btn_mojeid, mActivity);
					mSV = new ShowcaseView.Builder(mActivity, true)
					.setTarget(target)
					.setContentTitle("MojeID account")
					.setContentText("You can login by your MojeID account.")
					.setStyle(R.style.CustomShowcaseTheme_Next)
					.setShowcaseEventListener(this)
					.build();
					mSV.setButtonPosition(lps);
					mTutorialClick++;
					// TODO: Save that Google acount was clicked
					
				}
				else if (mTutorialClick==2){
					ViewTarget target = new ViewTarget(R.id.login_btn_demo, mActivity);
					mSV = new ShowcaseView.Builder(mActivity, true)
					.setTarget(target)
					.setContentTitle("Demo mode")
					.setContentText("You can try Demo house.")
					.setStyle(R.style.CustomShowcaseTheme)
					.setShowcaseEventListener(this)
					.build();
					mSV.setButtonPosition(lps);
					mTutorialClick++;
					// TODO: Save that MojeID acount was clicked
				}
				else if(mTutorialClick == 3) {
					// TODO: Save that Demo mode was clicked
					
				}
				
			}
			
			@Override
			public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
				Log.d(TAG, "OnShowCase did hide");
				
			}
		}; 
		
		mTutorialClick = 1;
		mSV = new ShowcaseView.Builder(mActivity, true)
		.setTarget(target_google)
		.setContentTitle("Google account")
		.setContentText("You can login by your Google account.")
		.setStyle(R.style.CustomShowcaseTheme_Next)
		.setShowcaseEventListener(listener)
		.build();
		mSV.setButtonPosition(lps);
		mSV.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Showcase click");
			}
		});
	}
	

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
	
	/**
	 * Method start routine to access trough google after button click
	 */
	private void beginGoogleAuthRoutine() {
		if (!checkInternetConnection())
			return;
		
		Log.d(TAG, "Start GoogleAuthRoutine");
		mProgress.show();
				
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
		if (resultCode == ConnectionResult.SUCCESS) {
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
		} else {
			// Google Play is missing
			Log.d(TAG, "Google Play Services is missing or not allowed");

			GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			// 20.8. 2014 Martin changed it to system supported dialog which should solve the problem
			mProgress.dismiss();
		}
		Log.d(TAG, "Finish GoogleAuthRoutine");
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
		
							finish();
						}
					}
					
					Log.i(TAG, "Login finished");
					mController.endPersistentConnection();
				} catch (IhaException e) {
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
				}

				progressDismiss();
				if (errFlag) {
					new ToastMessageThread(LoginActivity.this, errMessage).start();
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
