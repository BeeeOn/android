package cz.vutbr.fit.intelligenthomeanywhere;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class ChangeLocationNameActivity extends Activity {
	private String _oldLocation = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_change_location_name);
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null){
			_oldLocation = bundle.getString(Constants.LOCATION_LONG_PRESS);
		}
		
	}
	
	/**
	 * Evaluate change of location name - onClick
	 * @param v
	 */
	public void changeMethod(View v){
		ArrayList<Device> devices = Constants.GetCapabilities().GetDevicesByLocation(_oldLocation);
		EditText enewLocation = (EditText)findViewById(R.id.change_location_name_edittext);
		String snewLocation = enewLocation.getText().toString();
		for(Device d : devices){
			d.SetLocation(snewLocation);
		}
		Constants.GetCapabilities().SetNewLocationName();
		this.finish();
	}
}
