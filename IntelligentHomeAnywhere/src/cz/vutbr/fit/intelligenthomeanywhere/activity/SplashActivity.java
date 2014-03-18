/**
 * @brief Package for android GUI sources (activities)
 */
package cz.vutbr.fit.intelligenthomeanywhere.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.DemoData;
import cz.vutbr.fit.intelligenthomeanywhere.R;

/**
 * Class that handle long processes via GUI
 * @author ThinkDeep
 *
 */
public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_splash);
		initButtons();
		
		Bundle bundle = getIntent().getExtras();
		int switcher = 0;
		if(bundle != null)
			switcher = bundle.getInt(Constants.SPLASH); 
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
	 * Initialize listeners
	 */
	private void initButtons() {
		// Demo button - open demo
		((Button)findViewById(R.id.splash_demo)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DemoData demo = new DemoData(SplashActivity.this);
				if (demo.checkDemoData()) {
					Intent intent = new Intent(SplashActivity.this, LocationScreenActivity.class);
					intent.putExtra(Constants.LOGIN, Constants.LOGIN_DEMO);
		    		startActivity(intent);
				}
		    	SplashActivity.this.finish();
			}
		});
		
		// Refresh button - try to refresh splash
		((Button)findViewById(R.id.splash_refresh)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SplashActivity.this, SplashActivity.class);
				startActivity(intent);
				SplashActivity.this.finish();
			}
		});
	}
	
	/**
	 * Check if is Internet connection On or Off
	 * @return true if On, otherwise Off
	 */
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
