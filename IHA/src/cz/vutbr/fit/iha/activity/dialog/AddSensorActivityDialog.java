package cz.vutbr.fit.iha.activity.dialog;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.LocationScreenActivity;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.listing.Location;
import cz.vutbr.fit.iha.listing.LocationListing;

public class AddSensorActivityDialog extends Activity {

	private Controller mController;
	
	private BaseDevice mNewDevice;
	
	private ProgressDialog mProgress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_add_sensor_activity_dialog);
		
		mController = Controller.getInstance(this);
		
		// Prepare progress dialog
		mProgress = new ProgressDialog(this);
		mProgress.setMessage("Saving data...");
		mProgress.setCancelable(false);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			
		// TODO: sent as parameter if we want first uninitialized device or some device with particular id
			
		
		List<BaseDevice> devices = mController.getUninitializedDevices();
		if (devices.size() > 0) {
			mNewDevice = devices.get(0);
		} else {
			Toast.makeText(this, "There are no uninitialized devices.", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		initButtons();
		initViews();
	}
	
	private void initViews() {
		Spinner spinner = (Spinner)findViewById(R.id.spinner_choose_location);
        ArrayAdapter<LocationListing> dataAdapter = new ArrayAdapter<LocationListing>(this, android.R.layout.simple_spinner_item, mController.getLocations());
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        
        int typeStringRes = mNewDevice.getTypeStringResource(); 
        TextView type = (TextView)findViewById(R.id.addsensor_type); 
        if (type != null && typeStringRes > 0) { 
        	type.setText(type.getText() + " " + getString(typeStringRes)); 
        }
        
        TextView time = (TextView)findViewById(R.id.addsensor_involved_time);
        time.setText(time.getText() + " " + mNewDevice.getInvolveTime());
	}
	
	/**
	 * Initialize listeners
	 */
	private void initButtons() {
		// Add sensor button - add new name and location for new sensor
		((Button)findViewById(R.id.addsensor_add)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mNewDevice != null) {
					EditText name = (EditText)findViewById(R.id.addsensor_sensor_name_hint);
					EditText elocation = (EditText)findViewById(R.id.addsensor_new_location_hint);
					String locationName;
					if(elocation != null && elocation.length() < 1){
						Spinner slocation = (Spinner)findViewById(R.id.spinner_choose_location);
						locationName = slocation.getSelectedItem().toString();
					}else {
						locationName = elocation.getText().toString();
					}
					if(name == null || name.length() < 1){
						Toast.makeText(getApplicationContext(), getString(R.string.toast_need_sensor_name), Toast.LENGTH_LONG).show();
						return;
					}
					
					mNewDevice.setInitialized(true);
					mNewDevice.setName(name.getText().toString());
					mNewDevice.setLocation(new Location(locationName, locationName, 0)); // TODO: set location icon
					// TODO: show loading while saving device

					mProgress.show();
					
					SaveDeviceTask task = new SaveDeviceTask();
				    task.execute(new BaseDevice[] { mNewDevice });
				}
			}
		});
	}
	
	@Override
	public void onBackPressed(){
		LocationScreenActivity.healActivity();
		this.finish();
	}
	
	private class SaveDeviceTask extends AsyncTask<BaseDevice, Void, BaseDevice> {
    	@Override
    	protected BaseDevice doInBackground(BaseDevice... devices) {
    		BaseDevice device = devices[0]; // expect only one device at a time is sent there
    		mController.saveDevice(device);
    		mController.reloadAdapters();
    		return device;
    	}

    	@Override
    	protected void onPostExecute(BaseDevice device) {
    		Toast.makeText(getApplicationContext(), getString(R.string.toast_new_sensor_added), Toast.LENGTH_LONG).show();    		
    		mProgress.cancel();
    		AddSensorActivityDialog.this.finish();
    		
    		LocationScreenActivity.healActivity();
    	}
	}
}
