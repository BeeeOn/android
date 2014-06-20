package cz.vutbr.fit.iha.activity.dialog;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
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
	
	private EditText mNewLocation;
	private TextView mOrLabel;
	private Spinner mSpinner;
	
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
		mSpinner = (Spinner)findViewById(R.id.spinner_choose_location);
		
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if(position == mSpinner.getCount()-1){
					// show new location
					if(!hideInputForNewLocation(false) && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
						shringSpinner(true);
					}
				}else{
					// hide input for new location
					if(hideInputForNewLocation(true) && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
						shringSpinner(false);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				hideInputForNewLocation(true);
			}
		});
		

		
        CustomArrayAdapter dataAdapter = new CustomArrayAdapter(this, R.layout.custom_spinner_item, mController.getLocationsForAddSensorDialog());
        dataAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        
        mSpinner.setAdapter(dataAdapter);
        
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
	
	/**
	 * Method take needed inputs and switch visibility
	 * @param hide items is hidden if true, visible otherwise
	 * @return true if is item hidden
	 */
	private boolean hideInputForNewLocation(boolean hide){
		if(mNewLocation == null)
			mNewLocation = (EditText)findViewById(R.id.addsensor_new_location_hint);
		if(mOrLabel == null)
			mOrLabel = (TextView)findViewById(R.id.addsensor_or);
		
		if(hide){
			mNewLocation.setVisibility(View.GONE);
			mOrLabel.setVisibility(View.GONE);
			return true;
		}else{
			mNewLocation.setVisibility(View.VISIBLE);
			mOrLabel.setVisibility(View.VISIBLE);
			return false;
		}
	}
	
	private boolean shringSpinner(boolean shrink){
		LayoutParams params = (LayoutParams) mSpinner.getLayoutParams();
		if(shrink)
			params.width = 180;
		else
			params.width = LayoutParams.MATCH_PARENT;
		mSpinner.setLayoutParams(params);
		return false;
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
    		if (mController.saveDevice(device)) {
    			mController.reloadAdapters();
    			return device;
    		}
    		return null;
    	}

    	@Override
    	protected void onPostExecute(BaseDevice device) {
    		Toast.makeText(getApplicationContext(), getString(device != null ? R.string.toast_new_sensor_added : R.string.toast_new_sensor_not_added), Toast.LENGTH_LONG).show();
    		mProgress.cancel();
    		AddSensorActivityDialog.this.finish();
    		
    		LocationScreenActivity.healActivity();
    	}
	}

	private class CustomArrayAdapter extends ArrayAdapter<LocationListing>{
		
		private List<LocationListing> mLocations;
		private int mLayoutResource;
		private int mDropDownLayoutResource;

		public CustomArrayAdapter(Context context, int resource, List<LocationListing> objects) {
			super(context, resource, objects);
			mLayoutResource = resource;
			mLocations = objects;
		}
		
		@Override
		public void setDropDownViewResource(int resource){
			mDropDownLayoutResource = resource;
		}
		
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent){
			LayoutInflater inflater=getLayoutInflater();
            View row = inflater.inflate(mDropDownLayoutResource, parent, false);
            
            CheckedTextView label = (CheckedTextView)row.findViewById(R.id.custom_spinner_dropdown_label);
            label.setText(mLocations.get(position).getName());
            
            ImageView icon = (ImageView)row.findViewById(R.id.custom_spinner_dropdown_icon);
            int id = mLocations.get(position).getIconResource();
            icon.setImageResource(id);
            
            return row;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			LayoutInflater inflater=getLayoutInflater();
            View row = inflater.inflate(mLayoutResource, parent, false);
            
            TextView label = (TextView)row.findViewById(R.id.custom_spinner_label);
            label.setText(mLocations.get(position).getName());
            
            ImageView icon = (ImageView)row.findViewById(R.id.custom_spinner_icon);
            int id = mLocations.get(position).getIconResource();
            icon.setImageResource(id);
            
            return row;
		}
	}
}
