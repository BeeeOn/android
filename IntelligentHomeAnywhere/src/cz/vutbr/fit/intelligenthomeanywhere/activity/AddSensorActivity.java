package cz.vutbr.fit.intelligenthomeanywhere.activity;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.Device;

public class AddSensorActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_sensor);
		
		List<String> ListLocation = Constants.getCapabilities().getLocations(true);
		
		Spinner spinner = (Spinner)findViewById(R.id.spinner_choose_location);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ListLocation);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        
        Device device = (Device)Constants.getCapabilities().getNewOne();
        TextView type = (TextView)findViewById(R.id.addsensor_type);
        type.setText(type.getText() + " " + Constants.GetNameOfType(device.getType()));
        
        TextView time = (TextView)findViewById(R.id.addsensor_involved_time);
        time.setText(time.getText() + " " + device.getInvolveTime());
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_sensor, menu);
		return true;
	}
	
	/**
	 * Method that add new name and location of new sensor
	 * @param v
	 */
	public void addSensorMethod(View v){
		Adapter newadapter = Constants.getCapabilities().getNewOne();
		if(newadapter != null){
			EditText name = (EditText)findViewById(R.id.addsensor_sensor_name_hint);
			EditText elocation = (EditText)findViewById(R.id.addsensor_new_location_hint);
			String location;
			if(elocation != null && elocation.length() < 1){
				Spinner slocation = (Spinner)findViewById(R.id.spinner_choose_location);
				location = slocation.getSelectedItem().toString();
			}else {
				location = elocation.getText().toString();
			}
			if(name == null || name.length() < 1){
				Toast.makeText(this.getApplicationContext(), getString(R.string.toast_need_sensor_name), Toast.LENGTH_LONG).show();
				return;
			}
			newadapter.setInit(true);
			newadapter.setName(name.getText().toString());
			newadapter.setLocation(location);

			Constants.getCapabilities().setNewInit();
			Toast.makeText(this.getApplicationContext(), getString(R.string.toast_new_sensor_added), Toast.LENGTH_LONG).show();
			this.finish();
		}
	}

}
