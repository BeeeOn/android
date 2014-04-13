package cz.vutbr.fit.intelligenthomeanywhere.activity;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLHandshakeException;

import org.xmlpull.v1.XmlPullParserException;

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
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.DemoData;
import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.controller.Controller;
import cz.vutbr.fit.intelligenthomeanywhere.exception.ComVerMisException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.CommunicationException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.NoConnectionException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.NotImplementedException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.NotRegAException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.NotRegBException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.XmlVerMisException;
import cz.vutbr.fit.intelligenthomeanywhere.network.ActualUser;
import cz.vutbr.fit.intelligenthomeanywhere.network.GetGoogleAuth;

/**
 * First sign in class, controls first activity
 * @author ThinkDeep
 * @author Leopold Podmolík
 *
 */
public class LoginActivity extends Activity {

	private String acEmail;
	private Controller mController;
	
	private static final String TAG = "LOGIN";
	
	public static final int USER_RECOVERABLE_AUTH = 5;
	private static final int GET_GOOGLE_ACCOUNT = 6;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		// Get controller
		mController = Controller.getInstance(this);
		
		// Get btn for login
		ImageButton btnGoogle = (ImageButton) findViewById(R.id.login_btn_google);
		ImageButton btnMojeID = (ImageButton) findViewById(R.id.login_btn_mojeid);

		// Set onClickListener
		btnGoogle.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {

				try {
					getGoogleAccessFromServer(v);
				} catch (NotRegAException e) {
					Toast.makeText(v.getContext(), "Not registered", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				} catch (NotRegBException e) {
					Toast.makeText(v.getContext(), "Not registered", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				} catch (NoConnectionException e) {
					Toast.makeText(v.getContext(), "No Connection to server", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				} catch (CommunicationException e) {
					Toast.makeText(v.getContext(), "Ooops, something went wrong", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				} catch(NotImplementedException e){
					e.printStackTrace();
					Toast.makeText(v.getContext(), "Not Implemented yet", Toast.LENGTH_LONG).show();
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
        /*
        if (requestCode == USER_RECOVERABLE_AUTH && resultCode == RESULT_OK) {
        	new GetGoogleAuth(this, this.acEmail).execute();
        }
        */
        
        if (requestCode == GET_GOOGLE_ACCOUNT && resultCode == RESULT_OK) {
        	this.acEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        	
        	try {
				if(this.mController.login(this.acEmail)) {
					Intent intent = new Intent(this, LocationScreenActivity.class);
					intent.putExtra(Constants.LOGIN, Constants.LOGIN_DEMO);
					//intent.putExtra(name, value);
					this.startActivity(intent);
				}
			} catch (NotRegAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotRegBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CommunicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	// Get acces token
			//new GetGoogleAuth(this, this.acEmail).execute();
        }
    }
	
	/**
	 * 
	 * @return
	 * @throws NotRegBException 
	 * @throws NotRegAException 
	 * @throws CommunicationException 
	 * @throws NoConnectionException 
	 */
	private boolean getGoogleAccessFromServer(View v) throws NotRegAException, NotRegBException, NoConnectionException, CommunicationException{
		//TODO: get access via google
		Log.d(TAG, "BEG: Google access func");
		if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext()) == ConnectionResult.SUCCESS) {
			// On this device is Google Play, we can proceed
			Log.d(TAG, "On this device is Google Play, we can proceed");
			String[] Accounts = this.getAccountNames();
			if(Accounts.length == 1) {
				Log.d(TAG, "On this device is one account");
				this.acEmail = Accounts[0];
				
				new GetGoogleAuth(this, this.acEmail);
				GetGoogleAuth ggAuth = null;
				try {
					ggAuth = GetGoogleAuth.getGetGoogleAuth();
					ggAuth.execute();
					//while(ggAuth.getStatus() != AsyncTask.Status.FINISHED);
					ActualUser AUser = new ActualUser(ggAuth.getUserName(), ggAuth.getEmail(), null, null);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							mController.login(acEmail);
						} catch (NotRegAException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NotRegBException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoConnectionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (CommunicationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NotImplementedException e){
							e.printStackTrace();
							//Toast.makeText(getApplicationContext(), "Not implemented yet", Toast.LENGTH_LONG).show();
						}
						
					}
				}).start();
				//FIXME: implement this in upper thread and notify gui thread somehow
				/*if(this.mController.login(this.acEmail)) {
					Intent intent = new Intent(this, LocationScreenActivity.class);
					intent.putExtra(Constants.LOGIN, Constants.LOGIN_DEMO);
					//intent.putExtra(name, value);
					this.startActivity(intent);
	            }*/
				// Get acces token
				//new GetGoogleAuth(this, this.acEmail).execute();
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
