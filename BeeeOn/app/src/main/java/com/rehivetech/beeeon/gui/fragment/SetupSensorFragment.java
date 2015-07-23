package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.MainActivity;
import com.rehivetech.beeeon.gui.activity.SetupSensorActivity;
import com.rehivetech.beeeon.gui.adapter.LocationArrayAdapter;
import com.rehivetech.beeeon.gui.adapter.LocationIconAdapter;
import com.rehivetech.beeeon.gui.adapter.SetupSensorListAdapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class SetupSensorFragment extends TrackFragment {

	public SetupSensorActivity mActivity;
	private View mView;

	private static final String TAG = MainActivity.class.getSimpleName();
	private static final int NAME_ITEM_HEIGHT = 56;

	private List<Device> mNewDevices;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mActivity = (SetupSensorActivity) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must be subclass of SetupSensorActivity");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get activity and controller

		Controller controller = Controller.getInstance(mActivity);

		Gate gate = controller.getActiveGate();
		if (gate == null) {
			// CHYBA
			return;
		}
		mNewDevices = controller.getUninitializedDevicesModel().getUninitializedDevicesByGate(gate.getId());

		// TODO: sent as parameter if we want first uninitialized module or some
		// module with particular id


		// Create the AlertDialog object and return it
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_module_setup, container, false);

		initViews();

		return mView;
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

		Controller controller = Controller.getInstance(mActivity);
		// Get GUI elements
		final ListView listOfName = (ListView) mView.findViewById(R.id.setup_sensor_name_list);
		final Spinner spinner = (Spinner) mView.findViewById(R.id.addsensor_spinner_choose_location);
		final TextView time = (TextView) mView.findViewById(R.id.setup_sensor_info_text);

		// Create gate for setting names of new sensors
		SetupSensorListAdapter listAdapter = new SetupSensorListAdapter(mActivity, mNewDevices.get(0));
		LocationArrayAdapter dataAdapter = new LocationArrayAdapter(mActivity, R.layout.activity_module_edit_spinner_item);

		// Set layout to DataAdapter for locations
		dataAdapter.setDropDownViewResource(R.layout.activity_module_edit_spinner_dropdown_item);

		// Set listener for hide or unhide layout for add new location
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position == spinner.getCount() - 1) {
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
		SharedPreferences prefs = controller.getUserSettings();

		TimeHelper timeHelper = (prefs == null) ? null : new TimeHelper(prefs);

		// Set involved time of mDevice
		if (timeHelper != null) {
			Device device = mNewDevices.get(0);
			Gate gate = controller.getGatesModel().getGate(device.getGateId());
			time.setText(String.format("%s %s", time.getText(), timeHelper.formatLastUpdate(device.getPairedTime(), gate)));
		}

		// Set involved time of mDevice

		// Set gate to ListView and to Spinner
		listOfName.setAdapter(listAdapter);
		spinner.setAdapter(dataAdapter);
		// Set listview height, for all
		float scale = mActivity.getResources().getDisplayMetrics().density;
		int heightPx = Utils.convertDpToPixel(NAME_ITEM_HEIGHT * mNewDevices.get(0).getAllModules().size());
		listOfName.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, heightPx));
	}

	/**
	 * Method take needed inputs and switch visibility
	 *
	 * @param hide items is hidden if true, visible otherwise
	 * @return true if is item hidden
	 */
	private boolean hideInputForNewLocation(boolean hide) {
		EditText newLocation = (EditText) mView.findViewById(R.id.addsensor_new_location_name);
		TextView orLabel = (TextView) mView.findViewById(R.id.addsensor_or);

		Spinner newIconSpinner = (Spinner) mView.findViewById(R.id.addsensor_spinner_choose_new_location_icon);

		// Prepare list of icons
		List<Integer> iconsList = new ArrayList<>();
		for (Location.LocationIcon icon : Location.LocationIcon.values()) {
			iconsList.add(icon.getIconResource(IconResourceType.DARK));
		}

		// first call need to add gate
		LocationIconAdapter iconAdapter = new LocationIconAdapter(mActivity, R.layout.activity_module_edit_custom_spinner_icon_item);
		iconAdapter.setDropDownViewResource(R.layout.activity_module_edit_spinner_icon_dropdown_item);
		newIconSpinner.setAdapter(iconAdapter);


		int visibility = (hide ? View.GONE : View.VISIBLE);
		newLocation.setVisibility(visibility);
		orLabel.setVisibility(visibility);
		newIconSpinner.setVisibility(visibility);

		return hide;
	}

	private boolean shringSpinner(boolean shrink) {
		Spinner spinner = (Spinner) mView.findViewById(R.id.addsensor_spinner_choose_location);
		LayoutParams params = (LayoutParams) spinner.getLayoutParams();
		if (shrink)
			params.width = 180;
		else
			params.width = LayoutParams.MATCH_PARENT;
		spinner.setLayoutParams(params);
		return false;
	}


	public Spinner getSpinner() {
		return ((Spinner) mView.findViewById(R.id.addsensor_spinner_choose_location));
	}

	public ListView getListOfName() {
		return ((ListView) mView.findViewById(R.id.setup_sensor_name_list));
	}

	public TextView getNewLocation() {
		return ((EditText) mView.findViewById(R.id.addsensor_new_location_name));
	}

	public Spinner getNewIconSpinner() {
		return ((Spinner) mView.findViewById(R.id.addsensor_spinner_choose_new_location_icon));
	}

}
