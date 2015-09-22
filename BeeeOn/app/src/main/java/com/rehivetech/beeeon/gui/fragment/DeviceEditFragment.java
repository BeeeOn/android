package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.DeviceEditActivity;
import com.rehivetech.beeeon.gui.adapter.LocationArrayAdapter;
import com.rehivetech.beeeon.gui.adapter.RefreshIntervalAdapter;
import com.rehivetech.beeeon.gui.dialog.AddLocationDialog;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.location.Location;

import java.util.EnumSet;
import java.util.List;

/**
 * Created by david on 15.9.15.
 */
public class DeviceEditFragment extends BaseApplicationFragment implements AddLocationDialog.OnSaveClicked {
	private DeviceEditActivity mActivity;

	private Device mDevice;

	private Location mNewLocation = null;
	private Spinner mLocationSpinner;
	private Spinner mRefreshTimeSpinner;
	private LocationArrayAdapter mLocationArrayAdapter;


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

		mLocationSpinner = (Spinner) view.findViewById(R.id.device_edit_location_spinner);
		EditText editText = (EditText) view.findViewById(R.id.device_edit_device_name);
		Controller controller = Controller.getInstance(getActivity());
		mDevice = controller.getDevicesModel().getDevice(mActivity.getmGateId(), mActivity.getmDeviceId());
		editText.setText(mDevice.getCustomName());
		editText.setHint(mDevice.getType().getNameRes());

		RefreshInterval refreshInterval;
		if ((refreshInterval = mDevice.getRefresh()) != null) {
			((ViewStub) view.findViewById(R.id.device_edit_fragment_refresh_view_stub)).inflate();
			mRefreshTimeSpinner = (Spinner) view.findViewById(R.id.device_edit_refresh_spinner);
			mRefreshTimeSpinner.setAdapter(new RefreshIntervalAdapter(getActivity()));
			mRefreshTimeSpinner.setSelection(refreshInterval.getIntervalIndex());
		}

		mLocationArrayAdapter = new LocationArrayAdapter(mActivity, R.layout.activity_module_edit_spinner_item);
		mLocationArrayAdapter.setDropDownViewResource(R.layout.activity_module_edit_spinner_dropdown_item);
		mLocationSpinner.setAdapter(mLocationArrayAdapter);
		mLocationSpinner.setSelection(Utils.getObjectIndexFromList(mDevice.getLocationId(), mLocationArrayAdapter.getLocations()));
		mLocationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				if (i == mLocationArrayAdapter.getCount() - 1) {
					// the last item of the list is the new room, the new room will be stored in mNewLocation
					AddLocationDialog.show(getActivity());
				} else {
					//if something else is chosen, this variable is null
					mNewLocation = null;
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});
		return view;
	}

	public Device.DataPair getNewDataPair() {
		View view = getView();
		if (view == null)
			return null;

		EditText newName = (EditText) view.findViewById(R.id.device_edit_device_name);
		mDevice.setCustomName(newName.getText().toString());
		mLocationSpinner = (Spinner) view.findViewById(R.id.device_edit_location_spinner);
		if (mNewLocation == null)
			mNewLocation = (Location) mLocationSpinner.getAdapter().getItem(mLocationSpinner.getSelectedItemPosition());
		mDevice.setLocationId(((Location) mLocationSpinner.getAdapter().getItem(mLocationSpinner.getSelectedItemPosition())).getId());

		if (mDevice.getRefresh() != null) {
			RefreshInterval newRefresh = (RefreshInterval) mRefreshTimeSpinner.getAdapter().getItem(mRefreshTimeSpinner.getSelectedItemPosition());
			mDevice.setRefresh(newRefresh);
		}

		EnumSet<Module.SaveModule> what = EnumSet.noneOf(Module.SaveModule.class);
		what.add(Module.SaveModule.SAVE_LOCATION);
		what.add(Module.SaveModule.SAVE_NAME);
		what.add(Module.SaveModule.SAVE_REFRESH);

		return new Device.DataPair(mDevice, mNewLocation, what);
	}

	@Override
	public void saveNewDevice(String name, Location.LocationIcon icon) {
		List<Location> locations = mLocationArrayAdapter.getLocations();
		mLocationArrayAdapter.clear();
		mNewLocation = new Location(Location.NEW_LOCATION_ID, name, mActivity.getmGateId(), icon.getId());
		locations.add(0, mNewLocation);
		mLocationArrayAdapter = new LocationArrayAdapter(mActivity, R.layout.activity_module_edit_spinner_item, locations);
		mLocationArrayAdapter.setDropDownViewResource(R.layout.activity_module_edit_spinner_dropdown_item);
		mLocationSpinner.setAdapter(mLocationArrayAdapter);
		mLocationSpinner.setSelection(Utils.getObjectIndexFromList(mNewLocation.getId(), locations));
	}
}
