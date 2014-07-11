package cz.vutbr.fit.iha.activity.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.adapter.location.Location.DefaultRoom;
import cz.vutbr.fit.iha.controller.Controller;

public class AddSensorActivityDialog extends BaseActivityDialog {

	private Controller mController;

	private BaseDevice mNewDevice;

	private ProgressDialog mProgress;

	private EditText mNewLocation;
	private TextView mOrLabel;
	private Spinner mSpinner;
	private Spinner mNewIconSpinner;

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

		// TODO: sent as parameter if we want first uninitialized device or some
		// device with particular id

		List<BaseDevice> devices = mController.getUninitializedDevices();
		if (devices.size() > 0) {
			mNewDevice = devices.get(0);
		} else {
			Toast.makeText(this, "There are no uninitialized devices.",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		initButtons();
		initViews();
	}

	private void initViews() {
		mSpinner = (Spinner) findViewById(R.id.addsensor_spinner_choose_location);

		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == mSpinner.getCount() - 1) {
					// show new location
					if (!hideInputForNewLocation(false)
							&& getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
						shringSpinner(true);
					}
				} else {
					// hide input for new location
					if (hideInputForNewLocation(true)
							&& getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
						shringSpinner(false);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				hideInputForNewLocation(true);
			}
		});

		CustomArrayAdapter dataAdapter = new CustomArrayAdapter(this, R.layout.custom_spinner_item, getLocationsForAddSensorDialog());
		dataAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);

		mSpinner.setAdapter(dataAdapter);

		int typeStringRes = mNewDevice.getTypeStringResource();
		TextView type = (TextView) findViewById(R.id.addsensor_type);

		if (type != null && typeStringRes > 0) {
			type.setText(type.getText() + " " + getString(typeStringRes));
		}

		TextView time = (TextView) findViewById(R.id.addsensor_involved_time);
		time.setText(time.getText() + " " + mNewDevice.getInvolveTime());
	}
	
	/**
	 * Helper method to prepare list of locations (from adapter + defaults)
	 * 
	 * @return list of Location
	 */
	public List<Location> getLocationsForAddSensorDialog(){
		// Get locations from adapter
		List<Location> locations = mController.getLocations();
		
		// Add "missing" default rooms
		for (DefaultRoom room : Location.defaults) {
			String name = getString(room.rName);
			
			boolean found = false;
			for (Location location : locations) {
				if (location.getName().equals(name)) {
					found = true;
					break;
				}
			}
			
			if (!found) {
				locations.add(new Location(Location.NEW_LOCATION_ID, name, room.type));	
			}
		}
		
		// Sort them
		Collections.sort(locations);
		
		// Add "New location" item
		locations.add(new Location(Location.NEW_LOCATION_ID, getString(R.string.addsensor_new_location_spinner), 0));
		
		return locations;
	}

	/**
	 * Represents pair of device and location for saving it to server
	 */
	private class DeviceLocationPair {
		public final BaseDevice device;
		public final Location location;
		
		public DeviceLocationPair(final BaseDevice device, final Location location) {
			this.device = device;
			this.location = location;
		}
	}
	
	/**
	 * Initialize listeners
	 */
	private void initButtons() {
		// Add sensor button - add new name and location for new sensor
		((Button) findViewById(R.id.addsensor_add))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mNewDevice != null) {
							EditText name = (EditText) findViewById(R.id.addsensor_sensor_name_hint);
							Location location = null;
							// last location - means new one
							if (mSpinner.getSelectedItemPosition() == mSpinner.getCount() - 1) {
								EditText elocation = (EditText) findViewById(R.id.addsensor_new_location_name);

								// if(elocation != null && elocation.length() <
								// 1){
								// Spinner slocation =
								// (Spinner)findViewById(R.id.addsensor_spinner_choose_location);
								// locationName =
								// slocation.getSelectedItem().toString();
								// check new location name
								if (elocation != null && elocation.length() < 1) {
									Toast.makeText(
											getApplicationContext(),
											getString(R.string.toast_need_sensor_location_name),
											Toast.LENGTH_LONG).show();
									return;
								}

								location = new Location(
										Location.NEW_LOCATION_ID,
										elocation.getText().toString(),
										mNewIconSpinner.getSelectedItemPosition());
							} else {
								location = (Location) mSpinner.getSelectedItem();
							}

							// check name
							if (name == null || name.length() < 1) {
								Toast.makeText(
										getApplicationContext(),
										getString(R.string.toast_need_sensor_name),
										Toast.LENGTH_LONG).show();
								return;
							}

							mNewDevice.setInitialized(true);
							mNewDevice.setName(name.getText().toString());
							mNewDevice.setLocationId(location.getId());

							mProgress.show();
							
							SaveDeviceTask task = new SaveDeviceTask();
							task.execute(new DeviceLocationPair[] { new DeviceLocationPair(mNewDevice, location) });
						}
					}
				});
	}

	/**
	 * Method take needed inputs and switch visibility
	 * 
	 * @param hide
	 *            items is hidden if true, visible otherwise
	 * @return true if is item hidden
	 */
	private boolean hideInputForNewLocation(boolean hide) {
		if (mNewLocation == null)
			mNewLocation = (EditText) findViewById(R.id.addsensor_new_location_name);
		if (mOrLabel == null)
			mOrLabel = (TextView) findViewById(R.id.addsensor_or);
		if (mNewIconSpinner == null) {
			mNewIconSpinner = (Spinner) findViewById(R.id.addsensor_spinner_choose_new_location_icon);

			// Prepare list of icons
			ArrayList<Integer> iconsList = new ArrayList<Integer>(); 
			for (int rIcon : Location.icons) {
				iconsList.add(Integer.valueOf(rIcon));
			}
			
			// first call need to add adapter
			CustomIconArrayAdapter iconAdapter = new CustomIconArrayAdapter(this, R.layout.custom_spinner_icon_item, iconsList);
			iconAdapter.setDropDownViewResource(R.layout.custom_spinner_icon_dropdown_item);
			mNewIconSpinner.setAdapter(iconAdapter);
		}

		int visibility = (hide ? View.GONE : View.VISIBLE);
		mNewLocation.setVisibility(visibility);
		mOrLabel.setVisibility(visibility);
		mNewIconSpinner.setVisibility(visibility);

		return hide;
	}

	private boolean shringSpinner(boolean shrink) {
		LayoutParams params = (LayoutParams) mSpinner.getLayoutParams();
		if (shrink)
			params.width = 180;
		else
			params.width = LayoutParams.MATCH_PARENT;
		mSpinner.setLayoutParams(params);
		return false;
	}

	@Override
	public void onBackPressed() {
		LocationScreenActivity.healActivity();
		this.finish();
	}

	private class SaveDeviceTask extends
			AsyncTask<DeviceLocationPair, Void, DeviceLocationPair> {
		@Override
		protected DeviceLocationPair doInBackground(DeviceLocationPair... pairs) {
			DeviceLocationPair pair = pairs[0]; // expects only one device at a time is sent there

			if (pair.location.getId().equals(Location.NEW_LOCATION_ID)) {
				// We need to save new location to server first
				if (!mController.saveLocation(pair.location))
					return null;
				
				pair.device.setLocationId(pair.location.getId());
			}

			if (mController.saveDevice(pair.device)) {
				mController.reloadAdapters();
				return pair;
			}

			return null;
		}

		@Override
		protected void onPostExecute(DeviceLocationPair pair) {
			Toast.makeText(
					getApplicationContext(),
					getString(pair != null ? R.string.toast_new_sensor_added : R.string.toast_new_sensor_not_added),
					Toast.LENGTH_LONG).show();
			mProgress.cancel();

			if (pair != null) {
				// Successfuly saved, close this dialog and return back
				AddSensorActivityDialog.this.finish();
				LocationScreenActivity.healActivity();
			}
		}
	}

	private class CustomArrayAdapter extends ArrayAdapter<Location> {

		private List<Location> mLocations;
		private int mLayoutResource;
		private int mDropDownLayoutResource;

		public CustomArrayAdapter(Context context, int resource, List<Location> objects) {
			super(context, resource, objects);
			mLayoutResource = resource;
			mLocations = objects;
		}

		@Override
		public void setDropDownViewResource(int resource) {
			mDropDownLayoutResource = resource;
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(mDropDownLayoutResource, parent, false);

			CheckedTextView label = (CheckedTextView) row
					.findViewById(R.id.custom_spinner_dropdown_label);
			label.setText(mLocations.get(position).getName());

			ImageView icon = (ImageView) row
					.findViewById(R.id.custom_spinner_dropdown_icon);
			int id = mLocations.get(position).getIconResource();
			icon.setImageResource(id);

			return row;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(mLayoutResource, parent, false);

			TextView label = (TextView) row
					.findViewById(R.id.custom_spinner_label);
			label.setText(mLocations.get(position).getName());

			ImageView icon = (ImageView) row
					.findViewById(R.id.custom_spinner_icon);
			icon.setImageResource(mLocations.get(position).getIconResource());

			return row;
		}
	}

	private class CustomIconArrayAdapter extends ArrayAdapter<Integer> {

		private List<Integer> mIcons;
		private int mLayoutResource;
		private int mDropDownLayoutResource;

		public CustomIconArrayAdapter(Context context, int resource, List<Integer> objects) {
			super(context, resource, objects);
			mLayoutResource = resource;
			mIcons = objects;
		}

		@Override
		public void setDropDownViewResource(int resource) {
			mDropDownLayoutResource = resource;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(mDropDownLayoutResource, parent, false);

			ImageView icon = (ImageView) row.findViewById(R.id.custom_spinner_icon_dropdown_icon);
			icon.setImageResource(mIcons.get(position));

			return row;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(mLayoutResource, parent, false);

			ImageView icon = (ImageView) row.findViewById(R.id.custom_spinner_icon_icon);
			icon.setImageResource(mIcons.get(position));

			return row;
		}

	}

}
