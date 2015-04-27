package com.rehivetech.beeeon.widget.configuration;


import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.arrayadapter.DeviceArrayAdapter;
import com.rehivetech.beeeon.asynctask.ReloadAdapterDataTask;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceLog;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.pair.LogDataPair;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.data.WidgetDeviceData;
import com.rehivetech.beeeon.widget.data.WidgetGraphData;
import com.rehivetech.beeeon.widget.persistence.WidgetDevicePersistence;
import com.rehivetech.beeeon.widget.service.WidgetService;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * @author mlyko
 */
public class WidgetGraphFragment extends WidgetConfigurationFragment {
	private static final String TAG = WidgetGraphFragment.class.getSimpleName();


	protected WidgetGraphData mWidgetData;

	private WidgetDevicePersistence mWidgetDevice;

	private Spinner mDeviceSpinner;
	private RadioGroup mGapGroup;
	private RefreshInterval mWidgetRefreshInterval;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGeneralWidgetdata = new WidgetGraphData(mActivity.getWidgetId(), mActivity, null, null);
		mWidgetData = (WidgetGraphData) mGeneralWidgetdata;
		mWidgetDevice = mWidgetData.widgetDevice;
	}

	protected int getFragmentLayoutResource(){
		return R.layout.fragment_widget_graph;
	}
	protected int getFragmentTitle(){
		return R.string.widget_configuration_widget_graph;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mDeviceSpinner = (Spinner) mActivity.findViewById(R.id.widget_config_device);
		mGapGroup = (RadioGroup) mActivity.findViewById(R.id.widget_config_graph_gap);
		mGapGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId){
					case R.id.widget_gap_daily:
						mWidgetData.widgetLogData.gap = DeviceLog.DataInterval.HOUR.getValue();
						mWidgetData.widgetLogData.intervalStart = DateTime.now(DateTimeZone.UTC).minusDays(1).getMillis();
						mWidgetRefreshInterval = RefreshInterval.MIN_30;
						break;

					case R.id.widget_gap_monthly:
						mWidgetData.widgetLogData.gap = DeviceLog.DataInterval.DAY.getValue();
						mWidgetData.widgetLogData.intervalStart = DateTime.now(DateTimeZone.UTC).minusMonths(1).getMillis();
						mWidgetRefreshInterval = RefreshInterval.HOUR_24;	 // TODO maybe could be longer
						break;

					default:
					case R.id.widget_gap_weekly:
						mWidgetData.widgetLogData.gap = DeviceLog.DataInterval.HOUR.getValue();
						mWidgetData.widgetLogData.intervalStart = DateTime.now(DateTimeZone.UTC).minusWeeks(1).getMillis();
						mWidgetRefreshInterval = RefreshInterval.HOUR_12;
						break;
				}

				mWidgetData.widgetLogData.gapRadioId = checkedId;
			}
		});
	}

	@Override
	protected void onFragmentResume() {
		super.onFragmentResume();

		mGapGroup.check(mWidgetData.widgetLogData.gapRadioId);
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
		mWidgetData.configure(mActivity.isAppWidgetEditing(), mWidgetRefreshInterval.getInterval(), adapter);
		return true;
	}
}
