package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.DeviceEditActivity;
import com.rehivetech.beeeon.gui.adapter.LocationArrayAdapter;
import com.rehivetech.beeeon.gui.adapter.RefreshIntervalAdapter;
import com.rehivetech.beeeon.gui.dialog.AddLocationDialog;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.Utils;

import java.util.List;

/**
 * Created by david on 15.9.15.
 */
public class DeviceEditFragment extends BaseApplicationFragment {
	private static final String KEY_GATE_ID = "gateId";
	private static final String KEY_DEVICE_ID = "deviceId";

	private DeviceEditActivity mActivity;

	@Nullable
	private Location mNewLocation = null;
	@Nullable
	private Device mDevice = null;

	private String mGateId;
	private String mDeviceId;

	private EditText mDeviceNameText;
	private Spinner mLocationSpinner;
	private Spinner mRefreshTimeSpinner;

	private LocationArrayAdapter mLocationArrayAdapter;

	public static DeviceEditFragment newInstance(String gateId, String deviceId) {
		Bundle args = new Bundle();
		args.putString(KEY_GATE_ID, gateId);
		args.putString(KEY_DEVICE_ID, deviceId);

		DeviceEditFragment fragment = new DeviceEditFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mActivity = (DeviceEditActivity) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + "must be subclass of DeviceEditActivity");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGateId = getArguments().getString(KEY_GATE_ID);
		mDeviceId = getArguments().getString(KEY_DEVICE_ID);

		Controller controller = Controller.getInstance(mActivity);
		mDevice = controller.getDevicesModel().getDevice(mGateId, mDeviceId);

		// Get locations list containing gate locations and also special default locations
		List<Location> locations = LocationArrayAdapter.getLocations(controller.getLocationsModel(), mActivity, mGateId);
		mLocationArrayAdapter = new LocationArrayAdapter(mActivity, locations);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_device_edit, null);

		mDeviceNameText = (EditText) view.findViewById(R.id.device_edit_device_name);
		mLocationSpinner = (Spinner) view.findViewById(R.id.device_edit_location_spinner);

		if (mDevice != null && mDevice.getRefresh() != null) {
			// Inflate refreshInterval views
			((ViewStub) view.findViewById(R.id.device_edit_fragment_refresh_view_stub)).inflate();
			mRefreshTimeSpinner = (Spinner) view.findViewById(R.id.device_edit_refresh_spinner);
			mRefreshTimeSpinner.setAdapter(new RefreshIntervalAdapter(mActivity));
		}

		mLocationSpinner.setAdapter(mLocationArrayAdapter);
		mLocationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position == mLocationArrayAdapter.getCount() - 1) {
					// the last item of the list is the new room, the callback will call saveNewDevice method which will store store it in mNewLocation
					AddLocationDialog.show(mActivity);
				} else {
					// set the actually selected location
					mNewLocation = (Location) parent.getItemAtPosition(position);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				mNewLocation = null;
			}
		});
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		fillData();
	}

	public void fillData() {
		Controller controller = Controller.getInstance(mActivity);
		mDevice = controller.getDevicesModel().getDevice(mGateId, mDeviceId);
		if (mDevice == null)
			return;

		mDeviceNameText.setText(mDevice.getCustomName());
		mDeviceNameText.setHint(mDevice.getType().getNameRes());

		reloadLocationSpinner();
		selectLocation(mDevice.getLocationId());

		RefreshInterval refreshInterval = mDevice.getRefresh();
		if (refreshInterval != null && mRefreshTimeSpinner != null) {
			mRefreshTimeSpinner.setSelection(refreshInterval.getIntervalIndex());
		}
	}

	public Device.DataPair getNewDataPair() {
		if (mDevice == null)
			return null;

		mDevice.setCustomName(mDeviceNameText.getText().toString());

		if (mNewLocation != null) {
			mDevice.setLocationId(mNewLocation.getId());
		} else {
			mDevice.setLocationId(Location.NO_LOCATION_ID);
		}

		if (mDevice.getRefresh() != null) {
			RefreshInterval refresh = (RefreshInterval) mRefreshTimeSpinner.getSelectedItem();
			mDevice.setRefresh(refresh);
		}

		return new Device.DataPair(mDevice, mNewLocation); // TODO: remove the newLocation parameter and rework creating of default locations
	}

	public void reloadLocationSpinner() {
		Controller controller = Controller.getInstance(mActivity);
		// Get locations list containing gate locations and also special default locations
		List<Location> locations = LocationArrayAdapter.getLocations(controller.getLocationsModel(), mActivity, mGateId);
		mLocationArrayAdapter.setLocations(locations);
	}

	public void selectLocation(@NonNull String locationId) {
		List<Location> locations = mLocationArrayAdapter.getLocationsList();
		mLocationSpinner.setSelection(Utils.getObjectIndexFromList(locationId, locations));
	}
}