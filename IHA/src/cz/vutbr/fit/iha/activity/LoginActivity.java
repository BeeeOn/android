package cz.vutbr.fit.iha.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
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

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.dialog.AddAdapterActivityDialog;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.exception.CommunicationException;
import cz.vutbr.fit.iha.exception.NoConnectionException;
import cz.vutbr.fit.iha.exception.NotImplementedException;
import cz.vutbr.fit.iha.exception.NotRegAException;
import cz.vutbr.fit.iha.exception.NotRegBException;
import cz.vutbr.fit.iha.network.ActualUser;
import cz.vutbr.fit.iha.network.GetGoogleAuth;
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

	private static final String TAG = "LOGIN";
	public static final int USER_RECOVERABLE_AUTH = 5;
	private static final int GET_GOOGLE_ACCOUNT = 6;

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

		// Get controller
		mController = Controller.getInstance(this);

		// Prepare progress dialog
		mProgress = new ProgressDialog(mActivity);
		mProgress.setMessage("Signing to server...");
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

		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		if (mController.isLoggedIn()) {
			// If we're already logged in, continue to location screen
			Log.d(TAG, "Already logged in, going to locations screen...");

			Intent intent = new Intent(mActivity, LocationScreenActivity.class);

			mActivity.startActivity(intent);
			mActivity.finish();
			return;
		}

		String lastEmail = mController.getLastEmail();
		if (lastEmail.length() > 0) {
			// Automatic login with last used e-mail
			Log.d(TAG, String.format(
					"Automatic login with last used e-mail (%s)...", lastEmail));

			Controller.setDemoMode(LoginActivity.this, false);
			mProgress.show();
			doGoogleLogin(lastEmail);
		}

		// Demo button
		((CheckBox) findViewById(R.id.login_btn_demo))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Controller.setDemoMode(LoginActivity.this, true);

						Intent intent = new Intent(LoginActivity.this,
								LocationScreenActivity.class);
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
				Controller.setDemoMode(LoginActivity.this, false);
				mProgress.show();
				beginGoogleAuthRutine(v);
			}
		});
		btnMojeID.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getMojeIDAccessFromServer(v);
			}
		});
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
	 * Method mine users account names
	 * 
	 * @return array of names to choose
	 */
	private String[] getAccountNames() {
		AccountManager mAccountManager = AccountManager.get(this);
		Account[] accounts = mAccountManager
				.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
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
		if (GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getBaseContext()) == ConnectionResult.SUCCESS) {
			// On this device is Google Play, we can proceed
			Log.d(TAG, "On this device is Google Play, we can proceed");
			String[] Accounts = this.getAccountNames();
			if (Accounts.length == 1) {
				Log.d(TAG, "On this device is one account");
				doGoogleLogin(Accounts[0]);
			} else {
				Log.d(TAG, "On this device are more accounts");
				Intent intent = AccountPicker.newChooseAccountIntent(null,
						null, new String[] { "com.google" }, false, null, null,
						null, null);
				startActivityForResult(intent, GET_GOOGLE_ACCOUNT);
			}
		} else {
			// Google Play is missing
			Log.d(TAG, "Google Play is missing");
			// TODO: maybe show customAlertDialog with possibility actualize
			// play
			Toast.makeText(v.getContext(),
					getString(R.string.toast_play_missing), Toast.LENGTH_LONG)
					.show();
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
		final GetGoogleAuth ggAuth = new GetGoogleAuth(this, email);
		try {
			Log.d(TAG, "call google auth execute");

			mDoGoogleLoginRunnable = new StoppableRunnable() {

				@Override
				public void run() {
					ggAuth.execute();
					ActualUser.setActualUser(ggAuth.getUserName(),
							ggAuth.getEmail());
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
				ProgressDismiss();
				if (!mDoGoogleLoginRunnable.isStopped()) {
					Intent intent = new Intent(mActivity,
							LocationScreenActivity.class);
					mActivity.startActivity(intent);
					mActivity.finish();
				}
			} else {
				Log.d(TAG, "Login: false");
				errFlag = true;
				errMessage = "Login failed";
			}

		} catch (NotRegAException e) {
			e.printStackTrace();
			// there is unregistered adapter and we go to register it
			Intent intent = new Intent(LoginActivity.this,
					AddAdapterActivityDialog.class);
			Bundle bundle = new Bundle();
			bundle.putBoolean("Cancel", false);
			intent.putExtras(bundle);
			startActivity(intent);
		} catch (NotRegBException e) {
			e.printStackTrace();

			errFlag = true;
			errMessage = getString(R.string.toast_no_unregistered_adapter);
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
			errMessage = "Not implemented yet";
		} finally {
			ProgressDismiss();
			if (errFlag) {
				// alternate form: //mActivity.runOnUiThread(new
				// ToastMessageThread(mActivity, errMessage));
				new ToastMessageThread(mActivity, errMessage).start();
			}
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// ///////////////// TODO METHODS
	// /////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////

	private boolean getMojeIDAccessFromServer(View v) {
		// TODO: get access via mojeID
		Toast.makeText(v.getContext(), "Not Implemented yet", Toast.LENGTH_LONG)
				.show();
		return false;
	}
}
