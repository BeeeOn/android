package cz.vutbr.fit.intelligenthomeanywhere.activity;

import java.util.List;

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

/**
 * Class that handle screen to changing name of location
 * @author ThinkDeep
 */
public class ChangeLocationNameActivity extends Activity {
	private String mOldLocation = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_change_location_name);
		initButtons();
		
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null){
			mOldLocation = bundle.getString(Constants.LOCATION_LONG_PRESS);
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
				List<BaseDevice> devices = Constants.getAdapter().getDevicesByLocation(mOldLocation);
				EditText enewLocation = (EditText)findViewById(R.id.change_location_name_edittext);
				String snewLocation = enewLocation.getText().toString();
				for(BaseDevice d : devices){
					d.setLocation(snewLocation);
				}
				Constants.getAdapter().setNewLocationName();
				ChangeLocationNameActivity.this.finish();
			}
		});
	}
	
}
