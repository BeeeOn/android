package cz.vutbr.fit.intelligenthomeanywhere;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class ChangeDeviceNameActivity extends Activity {

	private String _oldName = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_change_device_name);
		
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null){
			_oldName = bundle.getString(Constants.DEVICE_LONG_PRESS);
		}
	}

	/**
	 * Method for editing name of sensor - onClick
	 * @param v
	 */
	public void changeMethod(View v){
		Device device = (Device)Constants.GetCapabilities().GetDeviceByName(_oldName);
		EditText enewDevice = (EditText)findViewById(R.id.change_device_name_edittext);
		String snewDevice = enewDevice.getText().toString();
		device.SetName(snewDevice);
		
		Constants.GetCapabilities().SetNewDeviceName(snewDevice);
		this.finish();
	}
	
}
