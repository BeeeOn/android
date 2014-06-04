package cz.vutbr.fit.intelligenthomeanywhere.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.DemoData;
import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.controller.Controller;
import cz.vutbr.fit.intelligenthomeanywhere.exception.CommunicationException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.NoConnectionException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.NotImplementedException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.NotRegAException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.NotRegBException;
import cz.vutbr.fit.intelligenthomeanywhere.network.ActualUser;
import cz.vutbr.fit.intelligenthomeanywhere.network.GetGoogleAuth;
import cz.vutbr.fit.intelligenthomeanywhere.thread.ToastMessageThread;
//import android.os.AsyncTask;

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
			//intent.putExtra(name, value);
			//intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			mActivity.startActivity(intent);
			mActivity.finish();
			return;
		}
		
		String lastEmail = mController.getLastEmail();
		if (lastEmail.length() > 0) {
			// Automatic login with last used e-mail
			Log.d(TAG, String.format("Automatic login with last used e-mail (%s)...", lastEmail));
			
			mController.setDemoMode(LoginActivity.this, false);
			mProgress.show();
			doGoogleLogin(lastEmail);
		}
		
		// Demo button
		((CheckBox)findViewById(R.id.login_btn_demo)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mController.setDemoMode(LoginActivity.this, true);

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
				mController.setDemoMode(LoginActivity.this, false);
				mProgress.show();
				getGoogleAccessFromServer(v);
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
	 * 
	 * @return
	 * @throws NotRegBException 
	 * @throws NotRegAException 
	 * @throws CommunicationException 
	 * @throws NoConnectionException 
	 */
	private boolean getGoogleAccessFromServer(View v){
		//TODO: get access via google
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
		}
		Log.d(TAG, "END: Google access func");
		return false;
	}
	
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
	
	private void doGoogleLogin(final String email) {
		final GetGoogleAuth ggAuth = new GetGoogleAuth(this, email);
		try {
//			Log.d(TAG, "Prepare google auth");
//			ggAuth = GetGoogleAuth.getGetGoogleAuth();
			Log.d(TAG, "call google auth execute");
			Thread ggTh = new Thread(new Runnable() {
				@Override
				public void run() {
					ggAuth.execute();
				}
			});
			ggTh.start();
			Log.d(TAG, "Finish google auth");
			//while(ggAuth.getStatus() != AsyncTask.Status.FINISHED);
			ActualUser AUser = new ActualUser(ggAuth.getUserName(), ggAuth.getEmail(), null, null);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				doLogin(email);
			}
		});
		th.start();
		
//		try {
////			synchronized(this) {
//				th.join();
////			}
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		Log.d(TAG, "Login: HERE?");
		//FIXME: implement this in upper thread and notify gui thread somehow
//		
		// Get acces token
		//new GetGoogleAuth(this, this.acEmail).execute();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * TODO: 
	 * @return
	 */
	private boolean getMojeIDAccessFromServer(View v){
		//TODO: get access via mojeID
		Toast.makeText(v.getContext(), "Not Implemented yet", Toast.LENGTH_LONG).show();
		return false;
	}
	
	@Override
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

	private void doLogin(final String email) {
		String errMessage = null;
		boolean errFlag = false;
		
		try {
			if(mController.login(email)) {
				Log.d(TAG, "Login: true");
				mProgress.dismiss();
				Intent intent = new Intent(mActivity, LocationScreenActivity.class);
				//intent.putExtra(name, value);
				//intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				mActivity.startActivity(intent);
				mActivity.finish();
	        }
			else{
				Log.d(TAG, "Login: false");
				errFlag = true;
				errMessage = "Please turn internet connection on";
			}
			
		} catch (NotRegAException e) {
			e.printStackTrace();
			
			Intent intent = new Intent(LoginActivity.this, AddAdapterActivity.class);
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
				mActivity.runOnUiThread(new ToastMessageThread(mActivity, errMessage));
			}
		}
	}
	
}
