package com.rehivetech.beeeon.widget.configuration;


import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.WidgetSettings;
import com.rehivetech.beeeon.widget.data.WidgetClockData;
import com.rehivetech.beeeon.widget.persistence.WidgetDevicePersistence;
import com.rehivetech.beeeon.widget.service.WidgetService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mlyko
 */
public class WidgetClockFragment extends WidgetConfigurationFragment {
	private static final String TAG = WidgetClockFragment.class.getSimpleName();

	protected Spinner mAdapterSpinner;
	protected SeekBar mWidgetUpdateSeekBar;

	protected WidgetClockData mWidgetData;

	protected List<WidgetDevicePersistence> mWidgetDevices;
	protected List<Spinner> mDeviceSpinners;
	private LinearLayout mDeviceSpinnersWrapper;

	private RadioGroup mColorSchemeGroup;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGeneralWidgetdata = new WidgetClockData(mActivity.getWidgetId(), mActivity, null, null);
		mWidgetData = (WidgetClockData) mGeneralWidgetdata;

		mWidgetDevices = mWidgetData.widgetDevices;
		mDeviceSpinners = new ArrayList<>();
	}

	protected int getFragmentLayoutResource(){
		return R.layout.fragment_widget_clock;
	}
	protected int getFragmentTitle(){
		return R.string.widget_configuration_widget_clock;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAdapterSpinner = (Spinner) mActivity.findViewById(R.id.widget_config_gateway);
		mWidgetUpdateSeekBar = (SeekBar) mActivity.findViewById(R.id.widget_config_interval);
		initWidgetUpdateIntervalLayout();

		mDeviceSpinnersWrapper = (LinearLayout) mActivity.findViewById(R.id.widget_config_devices);

		for(WidgetDevicePersistence wDev : mWidgetDevices){
			Spinner deviceSpinner = new Spinner(mActivity);
			mDeviceSpinnersWrapper.addView(deviceSpinner);
			mDeviceSpinners.add(deviceSpinner);
		}

		mAdapterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Adapter adapter = mAdapters.get(position);
				if (adapter == null) return;

				doChangeAdapter(adapter.getId(), ReloadAdapterDataTask.ReloadWhat.FACILITIES);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		mColorSchemeGroup = (RadioGroup) mActivity.findViewById(R.id.widget_configuration_scheme);
		mColorSchemeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.scheme_black_white:
						mWidgetData.settings.setColorScheme(R.color.white, R.color.white);
						break;

					case R.id.scheme_pink_cyan:
					default:
						mWidgetData.settings.setColorScheme(R.color.beeeon_primary_cyan, R.color.beeeon_secundary_pink);
						break;
				}
			}
		});
	}

	@Override
	protected void onAllAdaptersReload() {
		super.onAllAdaptersReload();
		// adapter spinner
		ArrayAdapter<?> arrayAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_dropdown_item, mAdapters);
		mAdapterSpinner.setAdapter(arrayAdapter);
	}

	@Override
	protected void onFragmentResume() {
		super.onFragmentResume();
		int selectedAdapterIndex = selectAdapter(mWidgetData.adapterId);
		if(selectedAdapterIndex == mAdapterSpinner.getSelectedItemPosition()){
			doChangeAdapter(mActiveAdapter.getId(), ReloadAdapterDataTask.ReloadWhat.FACILITIES);
		}
		else {
			mAdapterSpinner.setSelection(selectedAdapterIndex);
		}

		updateIntervalLayout();

		RadioButton schemeRadio;
		if(mWidgetData.settings.isColorSchemeEqual(R.color.white, R.color.white)){
			schemeRadio = (RadioButton) mActivity.findViewById(R.id.scheme_black_white);
			schemeRadio.setChecked(true);
		}
		else if(mWidgetData.settings.isColorSchemeEqual(R.color.white, R.color.white)){
			schemeRadio = (RadioButton) mActivity.findViewById(R.id.scheme_pink_cyan);
			schemeRadio.setChecked(true);
		}
	}

	/**
	 * Updates layout and expects to have all data fresh
	 */
	protected void updateLayout() {
		// fill sensor spinner
		DeviceArrayAdapter dataAdapter = new DeviceArrayAdapter(mActivity, R.layout.custom_spinner2_item, mDevices, mLocations);
		dataAdapter.setLayoutInflater(mActivity.getLayoutInflater());
		dataAdapter.setDropDownViewResource(R.layout.custom_spinner2_dropdown_item);

		int index = 0;
		for(WidgetDevicePersistence wDev : mWidgetDevices){
			Spinner spinner = mDeviceSpinners.get(index);
			spinner.setAdapter(dataAdapter);

			int foundIndex = Utils.getObjectIndexFromList(wDev.getId(), mDevices);
			if(foundIndex != -1) spinner.setSelection(foundIndex);

			index++;
		}
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

		int index = 0;
		for(WidgetDevicePersistence wDev : mWidgetDevices) {
			Spinner spinner = mDeviceSpinners.get(index);

			Device device = (Device) spinner.getSelectedItem();
			if (device == null) {
				Toast.makeText(mActivity, R.string.widget_configuration_select_device, Toast.LENGTH_LONG).show();
				return false;
			}

			wDev.configure(device, adapter);
			index++;
		}

		//sets widgetdata
		RefreshInterval refresh = RefreshInterval.values()[mWidgetUpdateSeekBar.getProgress()];
		mWidgetData.configure(mActivity.isAppWidgetEditing(), Math.max(refresh.getInterval(), WidgetService.UPDATE_INTERVAL_MIN), adapter);

		return true;
	}

}
