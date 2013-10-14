package cz.vutbr.fit.intelligenthomeanywhere.activity;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;

public class AddSensorActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_sensor);
		initButtons();
		
		List<String> ListLocation = Constants.getAdapter().getLocations(true);
		
		Spinner spinner = (Spinner)findViewById(R.id.spinner_choose_location);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ListLocation);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        
        BaseDevice device = Constants.getAdapter().getNewOne();
        TextView type = (TextView)findViewById(R.id.addsensor_type);
        type.setText(type.getText() + " " + getApplicationContext().getString(device.getTypeString()));
        
        TextView time = (TextView)findViewById(R.id.addsensor_involved_time);
        time.setText(time.getText() + " " + device.getInvolveTime());
		
	}
	
	/**
	 * Initialize listeners
	 */
	private void initButtons() {
		// Add sensor button - add new name and location for new sensor
		((Button)findViewById(R.id.addsensor_add)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BaseDevice newdevice = Constants.getAdapter().getNewOne();
				if(newdevice != null){
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
						Toast.makeText(getApplicationContext(), getString(R.string.toast_need_sensor_name), Toast.LENGTH_LONG).show();
						return;
					}
					newdevice.setInitialized(true);
					newdevice.setName(name.getText().toString());
					newdevice.setLocation(location);

					Constants.getAdapter().setNewInit();
					Toast.makeText(getApplicationContext(), getString(R.string.toast_new_sensor_added), Toast.LENGTH_LONG).show();
					AddSensorActivity.this.finish();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_sensor, menu);
		return true;
	}

}
