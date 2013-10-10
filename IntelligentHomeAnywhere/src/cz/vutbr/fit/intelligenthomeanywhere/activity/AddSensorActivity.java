package cz.vutbr.fit.intelligenthomeanywhere.activity;

import java.util.List;

import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.R.id;
import cz.vutbr.fit.intelligenthomeanywhere.R.layout;
import cz.vutbr.fit.intelligenthomeanywhere.R.menu;
import cz.vutbr.fit.intelligenthomeanywhere.R.string;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.Device;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddSensorActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_sensor);
		
		List<String> ListLocation = Constants.GetCapabilities().getLocations(true);
		
		Spinner spinner = (Spinner)findViewById(R.id.spinner_choose_location);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ListLocation);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        
        Device device = (Device)Constants.GetCapabilities().GetNewOne();
        TextView type = (TextView)findViewById(R.id.addsensor_type);
        type.setText(type.getText() + " " + Constants.GetNameOfType(device.GetType()));
        
        TextView time = (TextView)findViewById(R.id.addsensor_involved_time);
        time.setText(time.getText() + " " + device.GetInvolveTime());
		
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
	public void AddSensorMethod(View v){
		Adapter newadapter = Constants.GetCapabilities().GetNewOne();
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
			newadapter.SetInit(true);
			newadapter.SetName(name.getText().toString());
			newadapter.SetLocation(location);

			Constants.GetCapabilities().SetNewInit();
			Toast.makeText(this.getApplicationContext(), getString(R.string.toast_new_sensor_added), Toast.LENGTH_LONG).show();
			this.finish();
		}
	}

}
