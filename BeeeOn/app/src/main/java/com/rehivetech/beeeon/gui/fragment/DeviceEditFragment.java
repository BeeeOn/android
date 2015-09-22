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
			RefreshInterval[] data = RefreshInterval.values();
			List<CharSequence> list = new ArrayList<>();
			for (RefreshInterval r : data) {
				list.add(r.getStringInterval(mActivity));
			}
			RefreshIntervalAdapter times = new RefreshIntervalAdapter(getActivity(), android.R.layout.simple_list_item_1, data);
			times.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mRefreshTimeSpinner.setAdapter(times);
			mRefreshTimeSpinner.setSelection(refreshInterval.getIntervalIndex());
		}

		mLocationArrayAdapter = new LocationArrayAdapter(mActivity, R.layout.activity_module_edit_spinner_item);
		mLocationArrayAdapter.setDropDownViewResource(R.layout.activity_module_edit_spinner_dropdown_item);
		mLocationSpinner.setAdapter(mLocationArrayAdapter);
		mLocationSpinner.setSelection(getLocationsIndexFromArray(mLocationArrayAdapter.getLocations(), mDevice.getLocationId()));
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

	private int getLocationsIndexFromArray(List<Location> locations, String locationId) {
		int index = 0;
		for (Location room : locations) {
			if (room.getId().equalsIgnoreCase(locationId)) {
				return index;
			}
			index++;
		}
		return index;
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
		mLocationSpinner.setSelection(getLocationsIndexFromArray(locations, mNewLocation.getId()));
	}

	private class RefreshIntervalAdapter extends ArrayAdapter<RefreshInterval> {
		private RefreshInterval[] items;
		private Context mContext;
		private int mDropDownLayoutResource;
		private int mViewLayoutResource;

		public RefreshIntervalAdapter(Context context, int resource, RefreshInterval[] objects) {
			super(context, resource, objects);
			items = objects;
			mContext = context;
			mViewLayoutResource = resource;
		}

		@Override
		public void setDropDownViewResource(int resource) {
			mDropDownLayoutResource = resource;
			super.setDropDownViewResource(resource);
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View row = inflater.inflate(mDropDownLayoutResource, parent, false);

			TextView textView = (TextView) row.findViewById(android.R.id.text1);
			textView.setText(items[position].getStringInterval(mContext));

			return row;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(mViewLayoutResource, parent, false);
			}
			TextView textView = (TextView) v.findViewById(android.R.id.text1);
			textView.setText(items[position].getStringInterval(mContext));

			return v;
		}

		@Override
		public RefreshInterval getItem(int position) {
			return items[position];
		}
	}
}
