package cz.vutbr.fit.intelligenthomeanywhere;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.facebook.Session;


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
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	/**
	 * Open Demo - onClick
	 * @param v
	 */
	public void DemoMethod(View v){
		if(!checkDemoData())
			this.finish();
		Intent intent = new Intent(this, LocactionScreenActivity.class);
		intent.putExtra(Constants.LOGIN, Constants.LOGIN_DEMO);
    	startActivity(intent);
    	this.finish();
	}

	/**
	 * Check user login info, and open app - onClick
	 * @param v
	 */
	public void LoginMethod(View v){
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
			Intent intent = new Intent(this, LocactionScreenActivity.class);
			intent.putExtra(Constants.LOGIN, Constants.LOGIN_COMM);
			startActivity(intent);
		}else{
			Toast.makeText(v.getContext(), "Not Implemented yet", Toast.LENGTH_LONG).show();
			//Toast.makeText(v.getContext(), getString(R.string.toast_bad_password), Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Check for demodata on sdcard of phone
	 * @return true if everythink OK
	 */
	private boolean checkDemoData(){
		String path = Environment.getExternalStorageDirectory().toString() + "/IHA/";
		File dir = new File(path);
		if(!dir.exists()){
			if(!dir.mkdirs()) {
				Log.e("checkDemoData","ERROR: Creation of directory " + path + " on sdcard failed");
				return false;
			} else {
				Log.i("checkDemoData","Created directory " + path + " on sdcard");
			}
		}
		
		if(!(new File(Constants.DEMO_COMMUNICATION)).exists() || !(new File(Constants.DEMO_LOGFILE)).exists()){
			try{
				AssetManager assetManager = this.getAssets();
				InputStream in_xml = assetManager.open("komunikace.xml");
				OutputStream out_xml = new FileOutputStream(Constants.DEMO_COMMUNICATION);
				byte[] buf_xml = new byte[1024];
				int len;
				while((len = in_xml.read(buf_xml)) > 0){
					out_xml.write(buf_xml,0,len);
				}
				in_xml.close();
				out_xml.close();
				Log.i("DemoData","Communication file created");
				
				InputStream in_log = assetManager.open("sensor0.log");
				OutputStream out_log = new FileOutputStream(Constants.DEMO_LOGFILE);
				byte[] buf_log = new byte[1024];
				while((len = in_log.read(buf_log)) > 0){
					out_log.write(buf_log, 0, len);
				}
				in_log.close();
				out_log.close();
				Log.i("DemoData","Log file created");
				
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}
		}
		return true;
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
		Intent intent = new Intent(this, LoginFacebookActivity.class);
		startActivity(intent);
		
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
