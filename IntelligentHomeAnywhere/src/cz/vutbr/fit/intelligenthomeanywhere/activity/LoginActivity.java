package cz.vutbr.fit.intelligenthomeanywhere.activity;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
//import com.facebook.Session;

/**
 * First logining class, controls first activity
 * @author ThinkDeep
 *
 */
public class LoginActivity extends Activity {
	
	private static final int USER_RECOVERABLE_AUTH = 5;
	String token = null;
	TextView txtToken;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		// Get btn for login
		ImageButton btnGoogle = (ImageButton) findViewById(R.id.login_app_btn_google);
		ImageButton btnMojeID = (ImageButton) findViewById(R.id.login_app_btn_mojeid);
		txtToken = (TextView) findViewById(R.id.txtToken);
		
		// Set onClickListener
		btnGoogle.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				getGoogleAccessFromServer();
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
        //Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }
	
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}*/

	/**
	 * Check user login info, and open app - onClick
	 * @param v
	 */
	public void loginMethod(View v){
		boolean access = false;
		/*
		String name = ((EditText)findViewById(R.id.login_user_name)).getText().toString();
		String password = ((EditText)findViewById(R.id.login_password)).getText().toString();
		
		if(name == null || name.length() < 1 || password == null || password.length() < 1){ // check social networks
			RadioGroup radioGroup = (RadioGroup)findViewById(R.id.login_radioGroup);
			int checked = radioGroup.getCheckedRadioButtonId();
			switch(checked){
				case R.id.login_radio_facebook:
					access = getFacebokAccessFromServer();
					break;
				case R.id.login_radio_google:
					access = getGoogleAccessFromServer();
					break;
				case R.id.login_radio_mojeid:
					access = getMojeIDAccessFromServer();
					break;
			}
		}else{ // check name and password
			access = getNameAccessFromServer(name, password);
		}
		*/
		
		
		
		
		if(access){
			Intent intent = new Intent(this, LocationScreenActivity.class);
			intent.putExtra(Constants.LOGIN, Constants.LOGIN_COMM);
			startActivity(intent);
		}else{
			Toast.makeText(v.getContext(), "Not Implemented yet", Toast.LENGTH_LONG).show();
			//Toast.makeText(v.getContext(), getString(R.string.toast_bad_password), Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Check for access from server
	 * @param name username to access
	 * @param password
	 * @return true if access granted, else false
	 */
	private boolean getNameAccessFromServer(String name, String password){
		//TODO: get access from server for name and password
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	private boolean getFacebokAccessFromServer(){
		//TODO: get access via facebook
		//Intent intent = new Intent(this, LoginFacebookActivity.class);
		//startActivity(intent);
		
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	private boolean getGoogleAccessFromServer(){
		//TODO: get access via google
		
		if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext()) == ConnectionResult.SUCCESS) {
			// On this device is Google Play, we can proceed
			String[] Accounts = this.getAccountNames();
			if(Accounts.length == 1) {
				// Only one account on device
				String mScope =  "oauth2:https://www.googleapis.com/auth/userinfo.profile";
				
				
				new GetAuthToken(this, Accounts[0]).execute();
				
				/*try {
					 token = GoogleAuthUtil.getToken(this, Accounts[0].toString(), mScope);
				} catch (UserRecoverableAuthException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (GoogleAuthException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				//Log.i("AUTH", token);

			}
			
		}
		else {
			// Google Play is missing
		}
		
		return false;
	}
	
	private String[] getAccountNames() {
	    AccountManager mAccountManager = AccountManager.get(this);
	    Account[] accounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
	    String[] names = new String[accounts.length];
	    for (int i = 0; i < names.length; i++) {
	        names[i] = accounts[i].name;
	        Log.i("ACCOUNT", accounts[i].name);
	    }
	    return names;
	}
	
	
	/**
	 * 
	 * @return
	 */
	private boolean getMojeIDAccessFromServer(View v){
		//TODO: get access via mojeID
		Toast.makeText(v.getContext(), "Not Implemented yet", Toast.LENGTH_LONG).show();
		return false;
	}
	
	class GetAuthToken extends AsyncTask<Void, Void, String> {

		private LoginActivity mActivity;
		private String mEmail;
		
		public GetAuthToken(LoginActivity mActivity, String mEmail) {
		this.mActivity = mActivity;
		this.mEmail = mEmail;
		}
		
		@Override
		protected void onPreExecute() {
		}
		
		@Override
		protected String doInBackground(Void... params) {
		try {
		Log.i("MainActivity", mEmail);
		String token = GoogleAuthUtil.getToken(mActivity, mEmail,"oauth2:https://www.googleapis.com/auth/userinfo.profile");
		Log.i("MainActivity", token);
		return token;
		
		} catch (UserRecoverableAuthException userRecoverableException) {
		mActivity.startActivityForResult(userRecoverableException.getIntent(),USER_RECOVERABLE_AUTH);
		} catch (Exception e) {
		e.printStackTrace();
		}
		return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
		if (result != null) {}
			txtToken.setText(result);
		}
		
		}
}
