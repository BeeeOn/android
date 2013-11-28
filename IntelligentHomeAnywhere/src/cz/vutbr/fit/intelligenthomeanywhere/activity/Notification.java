package cz.vutbr.fit.intelligenthomeanywhere.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import cz.vutbr.fit.intelligenthomeanywhere.R;

public class Notification extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_notification);		
		initButtons();
	}
	
	/**
	 * Initialize listeners
	 */
	private void initButtons() {
		// Ignore button - close this notification
		((Button)findViewById(R.id.notification_ingore)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Notification.this.finish();
			}
		});
		
		// Add button - open activity for adding new device
		((Button)findViewById(R.id.notification_add)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Notification.this, AddSensorActivity.class);
				startActivity(intent);
				Notification.this.finish();
			}
		});
	}

}
