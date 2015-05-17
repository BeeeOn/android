package com.rehivetech.beeeon.widget.configuration;


import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.arrayadapter.DeviceArrayAdapter;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.data.WidgetDeviceData;
import com.rehivetech.beeeon.widget.persistence.WidgetDevicePersistence;

/**
 * @author mlyko
 */
public class WidgetDeviceFragment extends WidgetConfigurationFragment {
	private static final String TAG = WidgetDeviceFragment.class.getSimpleName();

	protected SeekBar mWidgetUpdateSeekBar;

	protected WidgetDeviceData mWidgetData;

	private WidgetDevicePersistence mWidgetDevice;

	private Spinner mDeviceSpinner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGeneralWidgetdata = new WidgetDeviceData(mActivity.getWidgetId(), mActivity, null, null);
		mWidgetData = (WidgetDeviceData) mGeneralWidgetdata;
		mWidgetDevice = mWidgetData.widgetDevices.get(0);
	}

	protected int getFragmentLayoutResource(){
		return R.layout.fragment_widget_device;
	}
	protected int getFragmentTitle(){
		return R.string.widget_configuration_widget_device;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mWidgetUpdateSeekBar = (SeekBar) mActivity.findViewById(R.id.widget_config_interval);
		initWidgetUpdateIntervalLayout(mWidgetUpdateSeekBar);

		mDeviceSpinner = (Spinner) mActivity.findViewById(R.id.widget_config_device);
	}

	@Override
	protected void onFragmentResume() {
		super.onFragmentResume();

		updateIntervalLayout(mWidgetUpdateSeekBar);
	}

	/**
	 * Updates layout and expects to have all data fresh
	 */
	protected void updateLayout() {
		// fill sensor spinner
		DeviceArrayAdapter dataAdapter = new DeviceArrayAdapter(mActivity, R.layout.custom_spinner2_item, mDevices, mLocations);
		dataAdapter.setLayoutInflater(mActivity.getLayoutInflater());
		dataAdapter.setDropDownViewResource(R.layout.custom_spinner2_dropdown_item);

		mDeviceSpinner.setAdapter(dataAdapter);
		int foundIndex = Utils.getObjectIndexFromList(mWidgetDevice.getId(), mDevices);
		if(foundIndex != -1) mDeviceSpinner.setSelection(foundIndex);
	}

	@Override
	protected boolean saveSettings() {
		Adapter adapter = (Adapter) mAdapterSpinner.getSelectedItem();
		if (adapter == null) {
			Toast.makeText(mActivity, R.string.widget_configuration_select_adapter, Toast.LENGTH_LONG).show();
			return false;
		}

		Device device = (Device) mDeviceSpinner.getSelectedItem();
		if (device == null) {
			Toast.makeText(mActivity, R.string.widget_configuration_select_device, Toast.LENGTH_LONG).show();
			return false;
		}

		mWidgetDevice.configure(device, adapter);
		//sets widgetdata
		mWidgetData.configure(
				mActivity.isAppWidgetEditing(),
				getRefreshSeconds(mWidgetUpdateSeekBar.getProgress()),
				mWidgetUpdateWiFiCheckBox.isChecked(),
				adapter);

		return true;
	}
}
