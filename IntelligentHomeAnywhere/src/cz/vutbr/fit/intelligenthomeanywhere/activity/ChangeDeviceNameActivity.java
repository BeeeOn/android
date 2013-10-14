package cz.vutbr.fit.intelligenthomeanywhere.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;

public class ChangeDeviceNameActivity extends Activity {

	private String _oldName = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_change_device_name);
		initButtons();
		
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null){
			_oldName = bundle.getString(Constants.DEVICE_LONG_PRESS);
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
				BaseDevice device = Constants.getAdapter().getDeviceByName(_oldName);
				EditText enewDevice = (EditText)findViewById(R.id.change_device_name_edittext);
				String snewDevice = enewDevice.getText().toString();
				device.setName(snewDevice);
				
				Constants.getAdapter().setNewDeviceName(snewDevice);
				ChangeDeviceNameActivity.this.finish();
			}
		});
	}
	
}
