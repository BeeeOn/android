package cz.vutbr.fit.iha.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
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
import cz.vutbr.fit.iha.thread.ToastMessageThread;
import cz.vutbr.fit.iha.activity.dialog.AddAdapterActivityDialog;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.exception.CommunicationException;
import cz.vutbr.fit.iha.exception.NoConnectionException;
import cz.vutbr.fit.iha.exception.NotImplementedException;
import cz.vutbr.fit.iha.exception.NotRegAException;
import cz.vutbr.fit.iha.exception.NotRegBException;
import cz.vutbr.fit.iha.network.ActualUser;
import cz.vutbr.fit.iha.network.GetGoogleAuth;

/**
 * First sign in class, controls first activity
 * @author ThinkDeep
 * @author Leopold Podmolík
 *
 */
public class LoginActivity extends Activity {

	private Controller mController;
	private LoginActivity mActivity;
	private ProgressDialog mProgress;
	
	private static final String TAG = "LOGIN";
	
	public static final int USER_RECOVERABLE_AUTH = 5;
	private static final int GET_GOOGLE_ACCOUNT = 6;
	
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
		mProgress.setCancelable(false);
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
			Log.d(TAG, String.format("Automatic login with last used e-mail (%s)...", lastEmail));
			
			Controller.setDemoMode(LoginActivity.this, false);
			mProgress.show();
			doGoogleLogin(lastEmail);
		}
		
		// Demo button
		((CheckBox)findViewById(R.id.login_btn_demo)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Controller.setDemoMode(LoginActivity.this, true);

				Intent intent = new Intent(LoginActivity.this, LocationScreenActivity.class);
	    		startActivity(intent);
		    	LoginActivity.this.finish();
			}
		});

		// Get btn for login
		ImageButton btnGoogle = (ImageButton) findViewById(R.id.login_btn_google);
		ImageButton btnMojeID = (ImageButton) findViewById(R.id.login_btn_mojeid);
		
		// Set onClickListener
		btnGoogle.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Controller.setDemoMode(LoginActivity.this, false);
				mProgress.show();
				beginGoogleAuthRutine(v);
			}
		});
		btnMojeID.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				getMojeIDAccessFromServer(v);
			}
		});
	}
    
    @Override
    public void onBackPressed(){
    	super.onBackPressed();
    	mActivity.finish();
    }
	
    /**
     * Method start routine to access trough google after button click
     * @param v
     * @return only false (Leo or Rob legacy)
     */
	private boolean beginGoogleAuthRutine(View v){
		Log.d(TAG, "BEG: Google access func");
		if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext()) == ConnectionResult.SUCCESS) {
			// On this device is Google Play, we can proceed
			Log.d(TAG, "On this device is Google Play, we can proceed");
			String[] Accounts = this.getAccountNames();
			if(Accounts.length == 1) {
				Log.d(TAG, "On this device is one account");
				doGoogleLogin(Accounts[0]);
			}
			else {
				Log.d(TAG, "On this device are more accounts");
				Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, false, null, null, null, null);
				startActivityForResult(intent, GET_GOOGLE_ACCOUNT);
			}
		}
		else {
			// Google Play is missing
			Log.d(TAG, "Google Play is missing");
			Toast.makeText(v.getContext(), "Google acount not found", Toast.LENGTH_LONG).show();
		}
		Log.d(TAG, "END: Google access func");
		return false;
	}
	
	/**
	 * Method mine users account names
	 * @return array of names to choose
	 */
	private String[] getAccountNames() {
	    AccountManager mAccountManager = AccountManager.get(this);
	    Account[] accounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
	    String[] names = new String[accounts.length];
	    for (int i = 0; i < names.length; i++) {
	        names[i] = accounts[i].name;
	        //Log.i(TAG, accounts[i].name);
	    }
	    return names;
	}
	
	/**
	 * Method create (finally only) one thread to get google token and than call last logging method
	 * @param email of user
	 */
	private void doGoogleLogin(final String email) {
		final GetGoogleAuth ggAuth = new GetGoogleAuth(this, email);
		try {
			Log.d(TAG, "call google auth execute");
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					ggAuth.execute();
					ActualUser.setActualUser(ggAuth.getUserName(), ggAuth.getEmail());
					doLogin(email);
					Log.d(TAG, "Finish google auth");
				}
			}).start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Last logging method that call controller to proceed access to server
	 * @param email of user
	 */
	private void doLogin(final String email) {
		String errMessage = null;
		boolean errFlag = false;
		
		try {
			if(mController.login(email)) {
				Log.d(TAG, "Login: true");
				mProgress.dismiss();
				Intent intent = new Intent(mActivity, LocationScreenActivity.class);
				mActivity.startActivity(intent);
				mActivity.finish();
	        }
			else{
				Log.d(TAG, "Login: false");
				errFlag = true;
				errMessage = "Login failed";
			}
			
		} catch (NotRegAException e) {
			e.printStackTrace();
			//there is unregistered adapter and we go to register it
			Intent intent = new Intent(LoginActivity.this, AddAdapterActivityDialog.class);
	    	startActivity(intent);
		} catch (NotRegBException e) {
			e.printStackTrace();
			
			errFlag = true;
			errMessage = "There is no unregistered adapter";
		} catch (NoConnectionException e) {
			e.printStackTrace();
			
			errFlag = true;
			errMessage = "Please turn internet connection on";
		} catch (CommunicationException e) {
			e.printStackTrace();
			
			errFlag = true;
			errMessage = "Sorry, some error on server side";
		} catch (NotImplementedException e){
			e.printStackTrace();
			
			errFlag = true;
			errMessage = "Not implemented yet";
		}
		finally{
			mProgress.dismiss();
			if(errFlag){
				//alternate form: //mActivity.runOnUiThread(new ToastMessageThread(mActivity, errMessage));
				new ToastMessageThread(mActivity, errMessage).start();
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	private boolean getMojeIDAccessFromServer(View v){
		//TODO: get access via mojeID
		Toast.makeText(v.getContext(), "Not Implemented yet", Toast.LENGTH_LONG).show();
		return false;
	}
	
	@Override
	//FIXME: check connections
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*
        if (requestCode == USER_RECOVERABLE_AUTH && resultCode == RESULT_OK) {
        	new GetGoogleAuth(this, this.acEmail).execute();
        }
        */
        
        
        if (requestCode == GET_GOOGLE_ACCOUNT && resultCode == RESULT_OK) {
        	String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        	if (email == null) {
        		Log.d(TAG, "onActivityResult: no email");
        		return;
        	}
        	
        	doLogin(email);
        	// Get acces token
			//new GetGoogleAuth(this, this.acEmail).execute();
        }
    }
	
}
