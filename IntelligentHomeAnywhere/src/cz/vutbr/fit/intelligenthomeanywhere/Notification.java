package cz.vutbr.fit.intelligenthomeanywhere;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class Notification extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_notification);
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
