package cz.vutbr.fit.intelligenthomeanywhere.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.R.id;
import cz.vutbr.fit.intelligenthomeanywhere.R.layout;
import cz.vutbr.fit.intelligenthomeanywhere.R.string;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_splash);
		
		Bundle bundle = getIntent().getExtras();
		int switcher = 0;
		if(bundle != null)
			switcher =bundle.getInt(Constants.SPLASH); 
		switch(switcher){
			case 0:
				//TODO: call to server for init info
				Toast.makeText(this, getString(R.string.toast_connecting_to_server), Toast.LENGTH_LONG).show();
				if(isNetworkAvailable()){
					Log.i("net","jede");
					int serverState = CheckAdapterState(); 
					if(serverState == Constants.ADAPTER_READY){
						Intent intent = new Intent(this, LoginActivity.class);
						startActivity(intent);
						this.finish();
					}else if(serverState == Constants.ADAPTER_NOT_REGISTERED){
						Intent intent = new Intent(this, AddAdapterActivity.class);
						startActivity(intent);
						this.finish();
					} else {
						TextView label = (TextView)findViewById(R.id.splash_label);
						label.setText(getString(R.string.splash_no_adapter));
						label.setVisibility(View.VISIBLE);
						
						ProgressBar progressbar = (ProgressBar)findViewById(R.id.splash_progressBar);
						progressbar.setVisibility(View.GONE);
						
						Button demo = (Button)findViewById(R.id.splash_demo);
						demo.setVisibility(View.VISIBLE);
					}
				}else {
					Log.i("net","NEjede");
					TextView label = (TextView)findViewById(R.id.splash_label);
					label.setText(getString(R.string.splash_no_connection));
					label.setVisibility(View.VISIBLE);
					
					ProgressBar progressbar = (ProgressBar)findViewById(R.id.splash_progressBar);
					progressbar.setVisibility(View.GONE);
					
					Button refresh = (Button)findViewById(R.id.splash_refresh);
					refresh.setVisibility(View.VISIBLE);
					
					Button demo = (Button)findViewById(R.id.splash_demo);
					demo.setVisibility(View.VISIBLE);
				}
				break;
				
			case 1:
				break;
			default:
				this.finish();
				break;
		}
		
	}
	
	/**
	 * Check if is internet connection On or Off
	 * @return true if On, otherwise Off
	 */
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	/**
	 * Open Demo - onClick
	 * @param v
	 */
	public void DemoMethod(View v){
		if(!checkDemoData())
			this.finish();
		Intent intent = new Intent(this, LocationScreenActivity.class);
		intent.putExtra(Constants.LOGIN, Constants.LOGIN_DEMO);
    	startActivity(intent);
    	this.finish();
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
	 * Try to refresh splash - onClick
	 * @param v
	 */
	public void refreshMethod(View v){
		Intent intent = new Intent(this,SplashActivity.class);
		startActivity(intent);
		this.finish();
	}
	
	/**
	 * Call to server and check state of adapter
	 * @return
	 */
	//TODO: CheckAdapterState(){
	private int CheckAdapterState(){
		//return Constants.ADAPTER_NOT_REGISTERED;
		//return Constants.ADAPTER_OFFLINE;
		return Constants.ADAPTER_READY;
	}
}
