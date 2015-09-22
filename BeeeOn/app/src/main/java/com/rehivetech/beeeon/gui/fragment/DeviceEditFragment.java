package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.os.Bundle;
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
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.Utils;

import java.util.EnumSet;
import java.util.List;

/**
 * Created by david on 15.9.15.
 */
public class DeviceEditFragment extends BaseApplicationFragment implements AddLocationDialog.OnSaveClicked {
	private DeviceEditActivity mActivity;

	private Device mDevice;

	@Nullable
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
			throw new ClassCastException(activity.toString() + "must be subclass of DeviceEditActivity");
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
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position == mLocationArrayAdapter.getCount() - 1) {
					// the last item of the list is the new room, the callback will call saveNewDevice method which will store store it in mNewLocation
					AddLocationDialog.show(getActivity());
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

	public Device.DataPair getNewDataPair() {
		View view = getView();
		if (view == null)
			return null;

		EditText newName = (EditText) view.findViewById(R.id.device_edit_device_name);
		mDevice.setCustomName(newName.getText().toString());

		if (mNewLocation != null) {
			mDevice.setLocationId(mNewLocation.getId());
		} else {
			mDevice.setLocationId(Location.NO_LOCATION_ID);
		}

		if (mDevice.getRefresh() != null) {
			RefreshInterval newRefresh = (RefreshInterval) mRefreshTimeSpinner.getAdapter().getItem(mRefreshTimeSpinner.getSelectedItemPosition());
			mDevice.setRefresh(newRefresh);
		}

		return new Device.DataPair(mDevice, mNewLocation);
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
