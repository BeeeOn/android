package cz.vutbr.fit.intelligenthomeanywhere.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.Device;

public class ChangeLocationNameActivity extends Activity {
	private String _oldLocation = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_change_location_name);
		initButtons();
		
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null){
			_oldLocation = bundle.getString(Constants.LOCATION_LONG_PRESS);
		}		
	}
	
	/**
	 * Initialize listeners
	 */
	private void initButtons() {
		// Change location name button
		((Button)findViewById(R.id.change_location_name_button)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ArrayList<Device> devices = Constants.getCapabilities().getDevicesByLocation(_oldLocation);
				EditText enewLocation = (EditText)findViewById(R.id.change_location_name_edittext);
				String snewLocation = enewLocation.getText().toString();
				for(Device d : devices){
					d.setLocation(snewLocation);
				}
				Constants.getCapabilities().setNewLocationName();
				ChangeLocationNameActivity.this.finish();
			}
		});
	}
	
}
