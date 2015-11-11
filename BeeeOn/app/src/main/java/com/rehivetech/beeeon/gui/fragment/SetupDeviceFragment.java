package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.MainActivity;
import com.rehivetech.beeeon.gui.activity.SetupDeviceActivity;
import com.rehivetech.beeeon.gui.adapter.LocationArrayAdapter;
import com.rehivetech.beeeon.gui.adapter.LocationIconAdapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.TimeHelper;

import java.util.ArrayList;
import java.util.List;

public class SetupDeviceFragment extends TrackFragment {
	private static final String TAG = MainActivity.class.getSimpleName();

	public SetupDeviceActivity mActivity;
	private View mView;

	private List<Device> mNewDevices;

	private String mGateId;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mActivity = (SetupDeviceActivity) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must be subclass of SetupDeviceActivity");
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
		mGateId = gate.getId();
		mNewDevices = controller.getUninitializedDevicesModel().getUninitializedDevicesByGate(mGateId);

		// TODO: sent as parameter if we want first uninitialized module or some module with particular id

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
			Log.d(TAG, "SETUP MODULE fragment is visible");
			mActivity.setFragment(this);
		}
	}


	private void initViews() {
		// Set listener for hide or unhide layout for add new location
		final Spinner spinner = (Spinner) mView.findViewById(R.id.module_setup_spinner_choose_location);
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

		Controller controller = Controller.getInstance(mActivity);

		// Get locations list containing gate locations and also special default locations
		List<Location> locations = LocationArrayAdapter.getLocations(controller.getLocationsModel(), mActivity, mGateId);

		// Set layout to DataAdapter for locations
		LocationArrayAdapter dataAdapter = new LocationArrayAdapter(mActivity, locations);
		spinner.setAdapter(dataAdapter);

		Device device = mNewDevices.get(0);

		TextView name = (TextView) mView.findViewById(R.id.module_setup_header_name);
		name.setText(device.getName(mActivity));

		TextView manufacturer = (TextView) mView.findViewById(R.id.module_setup_header_manufacturer);
		manufacturer.setText(getString(device.getType().getManufacturerRes()));

		// UserSettings can be null when user is not logged in!
		SharedPreferences prefs = controller.getUserSettings();

		if (prefs != null) {
			TimeHelper timeHelper = new TimeHelper(prefs);
			Gate gate = controller.getGatesModel().getGate(device.getGateId());

			// Set involved time of mDevice
			TextView time = (TextView) mView.findViewById(R.id.module_setup_info_text);
			time.setText(String.format("%s %s", time.getText(), timeHelper.formatLastUpdate(device.getPairedTime(), gate)));
		}
	}

	/**
	 * Method take needed inputs and switch visibility
	 *
	 * @param hide items is hidden if true, visible otherwise
	 * @return true if is item hidden
	 */
	private boolean hideInputForNewLocation(boolean hide) {
		EditText newLocation = (EditText) mView.findViewById(R.id.module_setup_new_location_name);

		Spinner newIconSpinner = (Spinner) mView.findViewById(R.id.module_setup_spinner_choose_new_location_icon);

		// first call need to add gate
		newIconSpinner.setAdapter(new LocationIconAdapter(mActivity));

		int visibility = (hide ? View.GONE : View.VISIBLE);
		newLocation.setVisibility(visibility);
		newIconSpinner.setVisibility(visibility);

		return hide;
	}

	private boolean shringSpinner(boolean shrink) {
		Spinner spinner = (Spinner) mView.findViewById(R.id.module_setup_spinner_choose_location);
		LayoutParams params = (LayoutParams) spinner.getLayoutParams();
		if (shrink)
			params.width = 180;
		else
			params.width = LayoutParams.MATCH_PARENT;
		spinner.setLayoutParams(params);
		return false;
	}

	public Spinner getSpinner() {
		return ((Spinner) mView.findViewById(R.id.module_setup_spinner_choose_location));
	}

	public TextView getNewLocation() {
		return ((EditText) mView.findViewById(R.id.module_setup_new_location_name));
	}

	public Spinner getNewIconSpinner() {
		return ((Spinner) mView.findViewById(R.id.module_setup_spinner_choose_new_location_icon));
	}

}
