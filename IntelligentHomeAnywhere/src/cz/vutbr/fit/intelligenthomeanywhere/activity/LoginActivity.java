package cz.vutbr.fit.intelligenthomeanywhere.activity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
//import com.facebook.Session;

/**
 * First logining class, controls first activity
 * @author ThinkDeep
 *
 */
public class LoginActivity extends Activity {
	
	private static final int USER_RECOVERABLE_AUTH = 5;
	private static final String TAG = "LOGIN";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		// Get btn for login
		ImageButton btnGoogle = (ImageButton) findViewById(R.id.login_app_btn_google);
		ImageButton btnMojeID = (ImageButton) findViewById(R.id.login_app_btn_mojeid);

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
        //Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

	/**
	 * Check user login info, and open app - onClick
	 * @param v
	 */
	public void loginMethod(View v){
		boolean access = false;

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
	 * 
	 * @return
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	private boolean getGoogleAccessFromServer(View v) throws InterruptedException, ExecutionException, TimeoutException{
		//TODO: get access via google
		
		if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext()) == ConnectionResult.SUCCESS) {
			// On this device is Google Play, we can proceed
			String[] Accounts = this.getAccountNames();
			if(Accounts.length == 1) {
				// Get acces token
				new GetAuthToken(this, Accounts[0]).execute();
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
	        //Log.i(TAG, accounts[i].name);
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
		private static final String TAG = "AUTH";
		private LoginActivity mActivity;
		private String mEmail;
		private String mToken;
		private String mUserName;
		
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
			//Log.i(TAG, mEmail);
			mToken = GoogleAuthUtil.getToken(mActivity, mEmail,"oauth2:https://www.googleapis.com/auth/userinfo.profile");
			//Log.i(TAG, token);
		return mToken;
		
		} catch (UserRecoverableAuthException userRecoverableException) {
			mActivity.startActivityForResult(userRecoverableException.getIntent(),USER_RECOVERABLE_AUTH);
		} catch (Exception e) {
		e.printStackTrace();
		}
		return null;
		}
		
		@Override
		protected void onPostExecute(final String result) {
			super.onPostExecute(result);
			if (result != null) {
				Thread fetchName = new Thread(new Runnable(){
				    @Override
				    public void run() {
				        try {
				        	fetchNameFromProfileServer(result);
				        } catch (Exception e) {
				            e.printStackTrace();
				        }
				    }
				});
				// Fetch user name and surname
				fetchName.start(); 
				// Wait for user name
				try {
					synchronized(this) {
						this.wait();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// Log - Token and Name
				Log.i(TAG, "Token: "+result);
				Log.i(TAG, "User name: "+this.mUserName);
				Toast.makeText(mActivity.getBaseContext(), "Welcome "+this.mUserName, Toast.LENGTH_LONG).show();
				Intent intent = new Intent(mActivity, LocationScreenActivity.class);
				intent.putExtra(Constants.LOGIN, Constants.LOGIN_DEMO);
				//intent.putExtra(name, value);
		    	startActivity(intent);
		    	mActivity.finish();
		    	Log.i(TAG, "FINISH");
			}
		}
		
		/****************************************************************************************/
		/* Prevzato z SDK - EXTRAS - SAMPLE - AUTH  											*/
		/****************************************************************************************/
		
		private void fetchNameFromProfileServer(String token) throws IOException, JSONException {
			
			URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + token);
	        HttpURLConnection con = (HttpURLConnection) url.openConnection();
	        int sc = con.getResponseCode();
	        if (sc == 200) {
	          InputStream is = con.getInputStream();
	          String name = getName(readResponse(is));
	          Log.i("MAinActivity","Hello " + name + "!");
	          this.mUserName = name;
	          is.close();
	          synchronized(this) {
	        	  this.notify();
	          }
	          return;
	        } else if (sc == 401) {
	            GoogleAuthUtil.invalidateToken(mActivity, token);
	            return;
	        } else {
	          return;
	        }
	    }
		
		/**
	     * Reads the response from the input stream and returns it as a string.
	     */
	    private  String readResponse(InputStream is) throws IOException {
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        byte[] data = new byte[2048];
	        int len = 0;
	        while ((len = is.read(data, 0, data.length)) >= 0) {
	            bos.write(data, 0, len);
	        }
	        return new String(bos.toByteArray(), "UTF-8");
	    }

	    /**
	     * Parses the response and returns the first name of the user.
	     * @throws JSONException if the response is not JSON or if first name does not exist in response
	     */
	    private String getName(String jsonResponse) throws JSONException {
	      JSONObject profile = new JSONObject(jsonResponse);
	      return profile.getString("name");
	    }
	}
}
