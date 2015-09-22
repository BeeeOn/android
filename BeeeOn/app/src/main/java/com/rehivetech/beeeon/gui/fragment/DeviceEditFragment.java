package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.Spinner;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.DeviceEditActivity;
import com.rehivetech.beeeon.gui.adapter.LocationArrayAdapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.location.Location;

/**
 * Created by david on 15.9.15.
 */
public class DeviceEditFragment extends BaseApplicationFragment {
	private DeviceEditActivity mActivity;

	private Device mDevice;


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mActivity = (DeviceEditActivity) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + "must be sublass of DeviceEditActivity");
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_device_edit, null);

		Spinner locationSpinner = (Spinner) view.findViewById(R.id.device_edit_location_spinner);
		EditText editText = (EditText) view.findViewById(R.id.device_edit_device_name);
		Controller controller = Controller.getInstance(getActivity());
		mDevice = controller.getDevicesModel().getDevice(mActivity.getmGateId(), mActivity.getmDeviceId());
		editText.setText(mDevice.getType().getNameRes());

		RefreshInterval refreshInterval;
		if ((refreshInterval = mDevice.getRefresh()) != null) {
			((ViewStub) view.findViewById(R.id.device_edit_fragment_refresh_view_stub)).inflate();
			EditText refeshTime = (EditText) view.findViewById(R.id.device_edit_refresh_edittext);
			refeshTime.setText(String.valueOf(refreshInterval.getInterval()));
		}

		LocationArrayAdapter locationAdapter = new LocationArrayAdapter(mActivity, R.layout.activity_module_edit_spinner_item);
		locationAdapter.setDropDownViewResource(R.layout.activity_module_edit_spinner_dropdown_item);
		locationSpinner.setAdapter(locationAdapter);
		// TODO dont know hot to set the active location :/


		return view;
	}

	public Device getNewDevice() {
		View view = getView();
		if (view == null)
			return null;

		Device newDevice = Device.createDeviceByType(mDevice.getType().getId(), mDevice.getGateId(), mDevice.getAddress());
		EditText newName = (EditText) view.findViewById(R.id.device_edit_device_name);
		newDevice.setName(newName.getText().toString());
		Spinner locatinSpinner = (Spinner) view.findViewById(R.id.device_edit_location_spinner);
		Location newLocation = (Location) locatinSpinner.getSelectedItem();
		newDevice.setLocationId(newLocation.getId());
		if (mDevice.getRefresh() != null) {
			EditText newRefresh = (EditText) view.findViewById(R.id.device_edit_refresh_edittext);
			int refreshTime = Integer.parseInt(newRefresh.getText().toString());
			newDevice.setRefresh(RefreshInterval.fromInterval(refreshTime));
		}
		return newDevice;
	}
}
