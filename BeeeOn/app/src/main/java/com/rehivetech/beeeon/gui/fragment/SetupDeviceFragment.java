package com.rehivetech.beeeon.gui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.MainActivity;
import com.rehivetech.beeeon.gui.adapter.LocationArrayAdapter;
import com.rehivetech.beeeon.gui.adapter.LocationIconAdapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.SaveDeviceTask;
import com.rehivetech.beeeon.util.TimeHelper;

import java.util.List;

public class SetupDeviceFragment extends BaseApplicationFragment {
	private static final String TAG = MainActivity.class.getSimpleName();

	private static final String KEY_GATE_ID = "gate_id";
	private static final String KEY_DEVICE_INDEX = "device_index";

	private Device mNewDevice;
	private String mGateId;
	private int mDeviceIndex;

	public static SetupDeviceFragment newInstance(String gateId, int deviceIndex) {

		Bundle args = new Bundle();
		args.putInt(KEY_DEVICE_INDEX, deviceIndex);
		args.putString(KEY_GATE_ID, gateId);
		SetupDeviceFragment fragment = new SetupDeviceFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();

		if (args != null) {
			mGateId = args.getString(KEY_GATE_ID);
			mDeviceIndex = args.getInt(KEY_DEVICE_INDEX);
		}

		mNewDevice = Controller.getInstance(mActivity).getUninitializedDevicesModel().getUninitializedDevicesByGate(mGateId).get(mDeviceIndex);
	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_device_setup, container, false);

		NestedScrollView scrollView = (NestedScrollView) view.findViewById(R.id.device_setup_scrollview);
		View deviceSetupView;

		switch (mNewDevice.getType()) {

			case TYPE_1:
			case TYPE_6:
				break;
			default:
				deviceSetupView = inflater.inflate(R.layout.device_setup_other_devices, null);
				scrollView.addView(deviceSetupView);
				initViews(view);
				break;
		}
		return view;
	}

	private void initViews(View view) {
		final EditText newLocation = (EditText) view.findViewById(R.id.device_setup_new_location_name);
		final Spinner newIconSpinner = (Spinner) view.findViewById(R.id.device_setup_spinner_choose_new_location_icon);
		newIconSpinner.setAdapter(new LocationIconAdapter(mActivity));
		setVisibleNewLocation(newIconSpinner, newLocation, false);
		// Set listener for hide or unhide layout for add new location
		final Spinner spinner = (Spinner) view.findViewById(R.id.device_setup_spinner_choose_location);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position == spinner.getCount() - 1) {
					// show new location
					setVisibleNewLocation(newIconSpinner, newLocation, true);
				} else {
					// hide input for new location
					setVisibleNewLocation(newIconSpinner, newLocation, false);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				setVisibleNewLocation(newIconSpinner, newLocation, false);
			}
		});

		Controller controller = Controller.getInstance(mActivity);

		// Get locations list containing gate locations and also special default locations
		List<Location> locations = LocationArrayAdapter.getLocations(controller.getLocationsModel(), mActivity, mGateId);

		// Set layout to DataAdapter for locations
		LocationArrayAdapter dataAdapter = new LocationArrayAdapter(mActivity, locations);
		spinner.setAdapter(dataAdapter);

		TextView name = (TextView) view.findViewById(R.id.device_setup_header_name);
		name.setText(mNewDevice.getName(mActivity));

		TextView manufacturer = (TextView) view.findViewById(R.id.device_setup_header_manufacturer);
		manufacturer.setText(getString(mNewDevice.getType().getManufacturerRes()));

		// UserSettings can be null when user is not logged in!
		SharedPreferences prefs = controller.getUserSettings();

		if (prefs != null) {
			TimeHelper timeHelper = new TimeHelper(prefs);
			Gate gate = controller.getGatesModel().getGate(mNewDevice.getGateId());

			// Set involved time of device
			TextView time = (TextView) view.findViewById(R.id.device_setup_info_text);
			time.setText(String.format("%s %s", time.getText(), timeHelper.formatLastUpdate(mNewDevice.getPairedTime(), gate)));
		}


		Button button = (Button) view.findViewById(R.id.device_setup_save_button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Location location;
				if (spinner.getSelectedItemPosition() == spinner.getCount() - 1) {
					// last location - means new one, so check its name
					if (newLocation == null || newLocation.length() < 1) {
						Toast.makeText(mActivity, getString(R.string.device_setup_toast_need_module_location_name), Toast.LENGTH_LONG).show();
						return;
					}
					if ((newIconSpinner.getAdapter().getItem(newIconSpinner.getSelectedItemPosition())).equals(Location.LocationIcon.UNKNOWN)) {
						Toast.makeText(mActivity, getString(R.string.activity_module_edit_setup_device_toast_location_icon), Toast.LENGTH_LONG).show();
						return;
					}

					location = new Location(Location.NEW_LOCATION_ID, newLocation.getText().toString(), mGateId, ((Location.LocationIcon) newIconSpinner.getAdapter().getItem(newIconSpinner.getSelectedItemPosition())).getId());
				} else {
					location = (Location) spinner.getSelectedItem();
				}

				// Set location to device
				mNewDevice.setLocationId(location.getId());

				// Save that device
				Log.d(TAG, String.format("InitializeDevice - device: %s, loc: %s", mNewDevice.getId(), location.getId()));
				doInitializeDeviceTask(new Device.DataPair(mNewDevice, location, true));
			}
		});
	}

	/**
	 * Method to switch visibility new location spinner and editText
	 *
	 * @param visible items is visible if true, hidden otherwise
	 */
	private void setVisibleNewLocation(Spinner newIconSpinner, EditText newLocation, boolean visible) {
		int visibility = visible ? View.VISIBLE : View.GONE;
		newLocation.setVisibility(visibility);
		newIconSpinner.setVisibility(visibility);
	}

	private void doInitializeDeviceTask(final Device.DataPair pair) {
		SaveDeviceTask initializeDeviceTask = new SaveDeviceTask(mActivity);

		initializeDeviceTask.setListener(new CallbackTask.ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (success) {
					Toast.makeText(mActivity, R.string.device_setup_toast_new_module_added, Toast.LENGTH_LONG).show();

					Intent intent = new Intent();
					intent.putExtra(Constants.SETUP_DEVICE_ACT_LOC, pair.location.getId());
					mActivity.setResult(Activity.RESULT_OK, intent);
					//HIDE keyboard
					InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
					mActivity.finish();
				}
			}

		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(initializeDeviceTask, pair, CallbackTaskManager.ProgressIndicator.PROGRESS_DIALOG);
	}
}
