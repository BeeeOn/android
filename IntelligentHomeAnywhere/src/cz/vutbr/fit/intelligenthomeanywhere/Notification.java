package cz.vutbr.fit.intelligenthomeanywhere;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;

public class Notification extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_notification);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.notification, menu);
		return true;
	}
	
	/**
	 * Method that close this notification - onClick
	 * @param v
	 */
	public void IgnoreMethod(View v){
		this.finish();
	}
	
	/**
	 * Method that open activity for adding new device - onClick
	 * @param v
	 */
	public void AddMethod(View v){
		Intent intent = new Intent(this,AddSensorActivity.class);
		startActivity(intent);
		this.finish();
	}

}
