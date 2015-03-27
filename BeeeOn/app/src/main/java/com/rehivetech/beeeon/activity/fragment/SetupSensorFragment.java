package com.rehivetech.beeeon.activity.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.MainActivity;
import com.rehivetech.beeeon.activity.SetupSensorActivity;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.device.Facility;
import com.rehivetech.beeeon.adapter.location.Location;
import com.rehivetech.beeeon.adapter.location.Location.DefaultLocation;
import com.rehivetech.beeeon.arrayadapter.SetupSensorListAdapter;
import com.rehivetech.beeeon.base.TrackFragment;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;

public class SetupSensorFragment extends TrackFragment {

	public SetupSensorActivity mActivity;
	private View mView;
	private Controller mController;

	private static final String TAG = MainActivity.class.getSimpleName();
	private static final int NAME_ITEM_HEIGHT = 56;

	private Adapter mAdapter;
	private List<Facility> mNewFacilities;
	
	private LinearLayout mLayout;


	private ListView mListOfName;
	private EditText mNewLocation;
	private TextView mOrLabel;
	private Spinner mSpinner;
	private Spinner mNewIconSpinner;
	private Button mPosButton;

	private boolean isError = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get activity and controller
		mActivity = (SetupSensorActivity) getActivity();
		mController = Controller.getInstance(mActivity.getApplicationContext());

		mAdapter = mController.getActiveAdapter();
		mNewFacilities = mController.getUninitializedFacilities(mAdapter.getId());

		// TODO: sent as parameter if we want first uninitialized device or some
		// device with particular id


		// Create the AlertDialog object and return it
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.activity_setup_sensor_activity_dialog, container, false);

		mLayout = (LinearLayout) mView.findViewById(R.id.container);
		
		initViews();
		
		return mView;
	}

	@Override
	public void onStart() {
		super.onStart();

	}
	

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
	    super.setUserVisibleHint(isVisibleToUser);
	    if (isVisibleToUser) {
	    	Log.d(TAG, "SETUP SENSOR fragment is visible");
	    	mActivity.setFragment(this);
	    }
	}

	

	private void initViews() {
		// Get GUI elements
		mListOfName = (ListView) mView.findViewById(R.id.setup_sensor_name_list);
		mSpinner = (Spinner) mView.findViewById(R.id.addsensor_spinner_choose_location);
		mNewLocation = (EditText) mView.findViewById(R.id.addsensor_new_location_name);
		TextView time = (TextView) mView.findViewById(R.id.setup_sensor_info_text);

		// Create adapter for setting names of new sensors
		SetupSensorListAdapter listAdapter = new SetupSensorListAdapter(mActivity, mNewFacilities.get(0));
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

		// UserSettings can be null when user is not logged in!
		SharedPreferences prefs = mController.getUserSettings();

		TimeHelper timeHelper = (prefs == null) ? null : new TimeHelper(prefs);

		// Set involved time of facility
		if (timeHelper != null) {
			Facility facility = mNewFacilities.get(0);
			Adapter adapter = mController.getAdapter(facility.getAdapterId());
			time.setText(String.format("%s %s", time.getText(), timeHelper.formatLastUpdate(facility.getInvolveTime(), adapter)));
		}

		// Set involved time of facility

		// Set adapter to ListView and to Spinner
		mListOfName.setAdapter(listAdapter);
		mSpinner.setAdapter(dataAdapter);
		// Set listview height, for all 
		float scale = mActivity.getResources().getDisplayMetrics().density;
		mListOfName.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) (scale*NAME_ITEM_HEIGHT*mNewFacilities.get(0).getDevices().size())));
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
		for (DefaultLocation room : DefaultLocation.values()) {
			String name = getString(room.getTitleResource());

			boolean found = false;
			for (Location location : locations) {
				if (location.getName().equals(name)) {
					found = true;
					break;
				}
			}

			if (!found) {
				locations.add(new Location(Location.NEW_LOCATION_ID, name, room.getTitleResource()));
			}
		}

		// Sort them
		Collections.sort(locations);

		// Add "New location" item
		locations.add(new Location(Location.NEW_LOCATION_ID, getString(R.string.addsensor_new_location_spinner), 0));

		return locations;
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
			mNewLocation = (EditText) mView.findViewById(R.id.addsensor_new_location_name);
		if (mOrLabel == null)
			mOrLabel = (TextView) mView.findViewById(R.id.addsensor_or);
		if (mNewIconSpinner == null) {
			mNewIconSpinner = (Spinner) mView.findViewById(R.id.addsensor_spinner_choose_new_location_icon);

			// Prepare list of icons
			List<Integer> iconsList = new ArrayList<Integer>();
			for (Location.LocationIcon icon : Location.LocationIcon.values()) {
				iconsList.add(icon.getIconResource());
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

	public Spinner getSpinner() {
		return mSpinner;
	}

	public ListView getListOfName() {
		return mListOfName;
	}

	public TextView getNewLocation() {
		return mNewLocation;
	}

	public Spinner getNewIconSpinner() {
		return mNewIconSpinner;
	}

}
