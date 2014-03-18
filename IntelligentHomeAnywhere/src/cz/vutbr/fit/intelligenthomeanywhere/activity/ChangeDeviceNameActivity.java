package cz.vutbr.fit.intelligenthomeanywhere.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.R;

public class ChangeDeviceNameActivity extends Activity {

	private BaseDevice mDevice = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_change_device_name);
		initButtons();
		
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null){
			String id = bundle.getString(Constants.DEVICE_LONG_PRESS);
			mDevice = Constants.getAdapter().getDeviceById(id);
		} else {
			Toast.makeText(this, "Error: Given no device id.", Toast.LENGTH_LONG).show();
			finish();
		}
	}
	
	/**
	 * Initialize listeners
	 */
	private void initButtons() {
		// Change device name button
		((Button)findViewById(R.id.change_device_name_button)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText enewDevice = (EditText)findViewById(R.id.change_device_name_edittext);
				String snewDevice = enewDevice.getText().toString();
				mDevice.setName(snewDevice);
				
				Constants.getAdapter().setNewDeviceName(snewDevice);
				ChangeDeviceNameActivity.this.finish();
			}
		});
	}
	
}
