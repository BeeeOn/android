package cz.vutbr.fit.iha.activity.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.SetupSensorListAdapter;
import cz.vutbr.fit.iha.activity.LocationScreenActivity;
import cz.vutbr.fit.iha.activity.TrackDialogFragment;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.adapter.location.Location.DefaultRoom;
import cz.vutbr.fit.iha.asynctask.CallbackTask.CallbackTaskListener;
import cz.vutbr.fit.iha.asynctask.SaveDeviceTask;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.pair.DeviceLocationPair;

public class SetupSensorFragmentDialog extends TrackDialogFragment {

	public LocationScreenActivity mActivity;
	private View mView;
	private Controller mController;

	private static final String TAG = LocationScreenActivity.class.getSimpleName();

	private Adapter mAdapter;
	private List<Facility> mNewFacilities;

	private ProgressDialog mProgress;

	private ListView mListOfName;
	private EditText mNewLocation;
	private TextView mOrLabel;
	private Spinner mSpinner;
	private Spinner mNewIconSpinner;
	private Button mPosButton;
	
	private boolean isError = false;


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Get activity and controller
		mActivity = (LocationScreenActivity)getActivity();
		mController = Controller.getInstance(mActivity.getApplicationContext());
		
		
		// Prepare progress dialog
		mProgress = new ProgressDialog(mActivity);
		mProgress.setMessage("Saving data...");
		mProgress.setCancelable(false);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				
				// TODO: sent as parameter if we want first uninitialized device or some
				// device with particular id

		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

		LayoutInflater inflater = mActivity.getLayoutInflater();

		// Get View
		mView = inflater.inflate(R.layout.activity_setup_sensor_activity_dialog, null);

		DialogInterface.OnClickListener dummyListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Do nothing here because we override this button later to change the close behaviour. 
                // However, we still need this because on older versions of Android unless we 
                // pass a handler the button doesn't get instantiated
			}
		};

		builder.setView(mView)
			.setPositiveButton(R.string.notification_add, dummyListener)
			.setNegativeButton(R.string.notification_cancel, dummyListener);

		// Send request
		mAdapter = mController.getActiveAdapter();
		if (mAdapter == null) {
			Toast.makeText(mActivity, getResources().getString(R.string.toast_no_adapter), Toast.LENGTH_LONG).show();
			//TODO: Ukoncit dialog ?
			isError = true;
		}
		
		//mUnInitDevices = new ArrayList<BaseDevice>();

		mNewFacilities = (List<Facility>) mController.getUninitializedFacilities(mAdapter.getId(), false);
		//TODO: add control if is only one new facility
		
		if (mNewFacilities.isEmpty()){
			Toast.makeText(mActivity, "There are no uninitialized devices.", Toast.LENGTH_LONG).show();
			//TODO: Kontrolovat jestli se dialog spustil spatne
			isError = true;
		}
				
		
		
		if(savedInstanceState != null) {
			 // Neco na ulozeni ?
		}
		
		// Create the AlertDialog object and return it
		return builder.create();

	}
	
	@Override
	public void onStart() {
	    super.onStart();
	    
	    // Get dialog
	    final AlertDialog dialog = (AlertDialog)getDialog();
	    
	    if(isError){
	    	dialog.dismiss();
	    	return;
	    }
	    
	    // Init GUI elements 
	    initViews(dialog);
		
	    mPosButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
	    mPosButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Facility newFacility = mNewFacilities.get(0);
				
				// Controll if Names arent empty
				for (int i = 0 ; i < newFacility.getDevices().size();i++) {
					// Get new names from EditText
					String name = ((EditText) mListOfName.getChildAt(i).findViewById(R.id.setup_sensor_item_name)).getText().toString();
					Log.d(TAG, "Name of "+i+" is"+name);
					if(name.isEmpty()) {
						Toast.makeText(mActivity, getString(R.string.toast_empty_sensor_name), Toast.LENGTH_LONG).show();
						return;
					}
					// Set this new name to sensor
					newFacility.getDevices().get(i).setName(name);

				}
				
				Location location = null;
				// last location - means new one
				if (mSpinner.getSelectedItemPosition() == mSpinner.getCount() - 1) {

					// check new location name
					if (mNewLocation != null && mNewLocation.length() < 1) {
						Toast.makeText(mActivity, getString(R.string.toast_need_sensor_location_name), Toast.LENGTH_LONG).show();
						return;
					}

					location = new Location(Location.NEW_LOCATION_ID, mNewLocation.getText().toString(), mNewIconSpinner.getSelectedItemPosition());

				} else {
					location = (Location) mSpinner.getSelectedItem();
				}
				
				// Set location to facility
				newFacility.setLocationId(location.getId());
				
				// Set that facility was inicialized
				newFacility.setInitialized(true);
				// Show progress bar for saving
				mProgress.show();
				
				// Save that facility
				SaveDeviceTask task = new SaveDeviceTask(getActivity().getApplicationContext());
				task.setListener(new CallbackTaskListener() {

					@Override
					public void onExecute(boolean success) {
						
						AlertDialog dialog = (AlertDialog) getDialog();
						if(dialog != null)
						{
							Toast.makeText(mActivity, getString(success ? R.string.toast_new_sensor_added : R.string.toast_new_sensor_not_added), Toast.LENGTH_LONG).show();
							mProgress.cancel();
							dialog.dismiss();
							mActivity.redrawMenu();
							mActivity.redrawDevices();
						}
						
						/*if (success) {
							// Successfuly saved, close this dialog and return back
							//SetupSensorActivityDialog.this.finish();
							// controll if more sensor is uninit
							if (mUnInitDevices.size() > 1) {
								Bundle bundle = new Bundle();
								bundle.putInt(Constants.ADDSENSOR_COUNT_SENSOR, mCountOfSensor);
								// go to setup uninit sensor
								Intent intent = new Intent(SetupSensorActivityDialog.this, SetupSensorActivityDialog.class);
								intent.putExtras(bundle);
								startActivity(intent);
								return;
							}
							if (mUnInitDevices.size() == 1) { // last one
								// TODO: this only when going from loginscreen, need to review
								if (mController.isLoggedIn()) {
									Intent intent = new Intent(SetupSensorActivityDialog.this, LocationScreenActivity.class);
									startActivity(intent);
									return;
								}
							}
						}*/
					}
					
				});
				
				Log.d(TAG, String.format("SaveDevice - device: %s, loc: %s", newFacility.getId(), location.getId()));
				task.execute(new DeviceLocationPair[] { new DeviceLocationPair(newFacility, location) });
			}
		});
	    
	}
		

		

	private void initViews(AlertDialog dialog) {
		// Get GUI elements
		mListOfName = (ListView) dialog.findViewById(R.id.setup_sensor_name_list);
		mSpinner = (Spinner) dialog.findViewById(R.id.addsensor_spinner_choose_location);
		mNewLocation = (EditText)  dialog.findViewById(R.id.addsensor_new_location_name);
		TextView time = (TextView) dialog.findViewById(R.id.setup_sensor_info_text);
				
		// Create adapter for setting names of new sensors
		SetupSensorListAdapter listAdapter = new SetupSensorListAdapter(mActivity,mNewFacilities.get(0));
		CustomArrayAdapter dataAdapter = new CustomArrayAdapter(mActivity, R.layout.custom_spinner_item, getLocationsForAddSensorDialog());
		
		// Set layout to DataAdapter for locations
		dataAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
		
		// Set listener for hide or unhide layout for add new location
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position == mSpinner.getCount() - 1) {
					// show new location
					if (!hideInputForNewLocation(false) && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
						shringSpinner(true);
					}
				} else {
					// hide input for new location
					if (hideInputForNewLocation(true) && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
						shringSpinner(false);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				hideInputForNewLocation(true);
			}
		});
		
		// Set involved time of facility
		time.setText(String.format("%s %s", time.getText(), mNewFacilities.get(0).getInvolveTime()));

		// Set adapter to ListView and to Spinner
		mListOfName.setAdapter(listAdapter);
		mSpinner.setAdapter(dataAdapter);
	}

	/**
	 * Helper method to prepare list of locations (from adapter + defaults)
	 * 
	 * @return list of Location
	 */
	public List<Location> getLocationsForAddSensorDialog() {
		// Get locations from adapter
		List<Location> locations = new ArrayList<Location>();
		
		Adapter adapter = mController.getActiveAdapter();
		if (adapter != null) {
			locations = mController.getLocations(adapter.getId());
		}

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
	 * Initialize listeners
	 *//*
	private void initButtons() {
		// Add sensor button - add new name and location for new sensor
		mAddButton = (Button) findViewById(R.id.addsensor_add);
		mAddButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mNewDevice != null) {

					Location location = null;
					// last location - means new one
					if (mSpinner.getSelectedItemPosition() == mSpinner.getCount() - 1) {

						// check new location name
						if (mNewLocation != null && mNewLocation.length() < 1) {
							Toast.makeText(getApplicationContext(), getString(R.string.toast_need_sensor_location_name), Toast.LENGTH_LONG).show();
							return;
						}

						location = new Location(Location.NEW_LOCATION_ID, mNewLocation.getText().toString(), mNewIconSpinner.getSelectedItemPosition());

					} else {
						location = (Location) mSpinner.getSelectedItem();
					}

					// check name
					if (mName == null || mName.length() < 1) {
						Toast.makeText(getApplicationContext(), getString(R.string.toast_need_sensor_name), Toast.LENGTH_LONG).show();
						return;
					}

					mNewDevice.getFacility().setInitialized(true);
					mNewDevice.setName(mName.getText().toString());
					mNewDevice.getFacility().setLocationId(location.getId());

					mProgress.show();

					SaveDeviceTask task = new SaveDeviceTask();
					task.execute(new DeviceLocationPair[] { new DeviceLocationPair(mNewDevice, location) });
				}
			}
		});
		mCancelButton = (Button) findViewById(R.id.addsensor_cancel);
		mCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SetupSensorActivityDialog.this.finish();
			}
		});
	}*/

	/**
	 * Method take needed inputs and switch visibility
	 * 
	 * @param hide
	 *            items is hidden if true, visible otherwise
	 * @return true if is item hidden
	 */
	private boolean hideInputForNewLocation(boolean hide) {
		if (mNewLocation == null)
			mNewLocation = (EditText) getDialog().findViewById(R.id.addsensor_new_location_name);
		if (mOrLabel == null)
			mOrLabel = (TextView) getDialog().findViewById(R.id.addsensor_or);
		if (mNewIconSpinner == null) {
			mNewIconSpinner = (Spinner) getDialog().findViewById(R.id.addsensor_spinner_choose_new_location_icon);

			// Prepare list of icons
			List<Integer> iconsList = new ArrayList<Integer>();
			for (int rIcon : Location.icons) {
				iconsList.add(Integer.valueOf(rIcon));
			}

			// first call need to add adapter
			CustomIconArrayAdapter iconAdapter = new CustomIconArrayAdapter(mActivity, R.layout.custom_spinner_icon_item, iconsList);
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


	


	class CustomArrayAdapter extends ArrayAdapter<Location> {

		private List<Location> mLocations;
		private int mLayoutResource;
		private int mDropDownLayoutResource;
		private Context mContext;

		public CustomArrayAdapter(Context context, int resource, List<Location> objects) {
			super(context, resource, objects);
			mContext = context;
			mLayoutResource = resource;
			mLocations = objects;
		}

		@Override
		public void setDropDownViewResource(int resource) {
			mDropDownLayoutResource = resource;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View row = inflater.inflate(mDropDownLayoutResource, parent, false);

			CheckedTextView label = (CheckedTextView) row.findViewById(R.id.custom_spinner_dropdown_label);
			label.setText(mLocations.get(position).getName());

			ImageView icon = (ImageView) row.findViewById(R.id.custom_spinner_dropdown_icon);
			int id = mLocations.get(position).getIconResource();
			icon.setImageResource(id);

			return row;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View row = inflater.inflate(mLayoutResource, parent, false);

			TextView label = (TextView) row.findViewById(R.id.custom_spinner_label);
			label.setText(mLocations.get(position).getName());

			ImageView icon = (ImageView) row.findViewById(R.id.custom_spinner_icon);
			icon.setImageResource(mLocations.get(position).getIconResource());

			return row;
		}
	}

	class CustomIconArrayAdapter extends ArrayAdapter<Integer> {

		private List<Integer> mIcons;
		private int mLayoutResource;
		private int mDropDownLayoutResource;
		private Context mContext;

		public CustomIconArrayAdapter(Context context, int resource, List<Integer> objects) {
			super(context, resource, objects);
			mLayoutResource = resource;
			mIcons = objects;
			mContext = context;
		}

		@Override
		public void setDropDownViewResource(int resource) {
			mDropDownLayoutResource = resource;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View row = inflater.inflate(mDropDownLayoutResource, parent, false);

			ImageView icon = (ImageView) row.findViewById(R.id.custom_spinner_icon_dropdown_icon);
			icon.setImageResource(mIcons.get(position));

			return row;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View row = inflater.inflate(mLayoutResource, parent, false);

			ImageView icon = (ImageView) row.findViewById(R.id.custom_spinner_icon_icon);
			icon.setImageResource(mIcons.get(position));

			return row;
		}

	}

}
