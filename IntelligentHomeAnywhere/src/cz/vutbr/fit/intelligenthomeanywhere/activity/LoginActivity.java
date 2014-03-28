package cz.vutbr.fit.intelligenthomeanywhere.activity;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
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

import cz.vutbr.fit.intelligenthomeanywhere.GetGoogleAuth;
import cz.vutbr.fit.intelligenthomeanywhere.R;

/**
 * First sign in class, controls first activity
 * @author ThinkDeep
 * @author Leopold Podmolík
 *
 */
public class LoginActivity extends Activity {
	
	public static final int USER_RECOVERABLE_AUTH = 5;
	private static final int GET_GOOGLE_ACCOUNT = 6;
	private static final String TAG = "LOGIN";
	private String acEmail;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		// Get btn for login
		ImageButton btnGoogle = (ImageButton) findViewById(R.id.login_btn_google);
		ImageButton btnMojeID = (ImageButton) findViewById(R.id.login_btn_mojeid);

		// Set onClickListener
		btnGoogle.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try {
					getGoogleAccessFromServer(v);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == USER_RECOVERABLE_AUTH && resultCode == RESULT_OK) {
        	new GetGoogleAuth(this, this.acEmail).execute();
        }
        
        
        if (requestCode == GET_GOOGLE_ACCOUNT && resultCode == RESULT_OK) {
        	this.acEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        	// Get acces token
			new GetGoogleAuth(this, this.acEmail).execute();
        }
    }
	
	/**
	 * 
	 * @return
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	private boolean getGoogleAccessFromServer(View v) throws InterruptedException, ExecutionException, TimeoutException{
		//TODO: get access via google
		Log.d(TAG, "BEG: Google access func");
		if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext()) == ConnectionResult.SUCCESS) {
			// On this device is Google Play, we can proceed
			Log.d(TAG, "On this device is Google Play, we can proceed");
			String[] Accounts = this.getAccountNames();
			if(Accounts.length == 1) {
				Log.d(TAG, "On this device is one account");
				this.acEmail = Accounts[0];
				// Get acces token
				new GetGoogleAuth(this, this.acEmail).execute();
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
	
	/**
	 * TODO: 
	 * @return
	 */
	private boolean getMojeIDAccessFromServer(View v){
		//TODO: get access via mojeID
		Toast.makeText(v.getContext(), "Not Implemented yet", Toast.LENGTH_LONG).show();
		return false;
	}
	

}
