package cz.vutbr.fit.intelligenthomeanywhere.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;

//import com.facebook.Session;

/**
 * First logining class, controls first activity
 * @author ThinkDeep
 *
 */
public class LoginActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
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
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	private boolean getMojeIDAccessFromServer(){
		//TODO: get access via mojeID
		return false;
	}
}
