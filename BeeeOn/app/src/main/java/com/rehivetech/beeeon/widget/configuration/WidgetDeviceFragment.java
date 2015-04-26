package com.rehivetech.beeeon.widget.configuration;


import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.arrayadapter.DeviceArrayAdapter;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.data.WidgetDeviceData;
import com.rehivetech.beeeon.widget.persistence.WidgetDevicePersistence;
import com.rehivetech.beeeon.widget.service.WidgetService;

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
		mWidgetDevice = mWidgetData.widgetDevice;
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
		initWidgetUpdateIntervalLayout();

		mDeviceSpinner = (Spinner) mActivity.findViewById(R.id.widget_config_device);
	}

	@Override
	protected void onFragmentResume() {
		super.onFragmentResume();

		updateIntervalLayout();
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

	/**
	 * Initializes widget update interval seekbar and text
	 */
	protected void initWidgetUpdateIntervalLayout() {
		// Set Max value by length of array with values
		mWidgetUpdateSeekBar.setMax(RefreshInterval.values().length - 1);
		// set interval
		mWidgetUpdateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				setIntervalWidgetText(progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// Nothing to do here
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// Nothing to do here
			}
		});
	}

	protected void updateIntervalLayout(){
		int interval = Math.max(mWidgetData.widgetInterval, WidgetService.UPDATE_INTERVAL_MIN);
		int intervalIndex = RefreshInterval.fromInterval(interval).getIntervalIndex();
		mWidgetUpdateSeekBar.setProgress(intervalIndex);
		// set text of seekbar
		setIntervalWidgetText(intervalIndex);
	}

	/**
	 * Sets widget interval text
	 * @param intervalIndex index in seekbar
	 */
	protected void setIntervalWidgetText(int intervalIndex) {
		TextView intervalText = (TextView) mActivity.findViewById(R.id.widget_config_interval_text);
		String interval = RefreshInterval.values()[intervalIndex].getStringInterval(mActivity);
		intervalText.setText(interval);
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
		RefreshInterval refresh = RefreshInterval.values()[mWidgetUpdateSeekBar.getProgress()];
		mWidgetData.configure(mActivity.isAppWidgetEditing(), Math.max(refresh.getInterval(), WidgetService.UPDATE_INTERVAL_MIN), adapter);

		return true;
	}
}
