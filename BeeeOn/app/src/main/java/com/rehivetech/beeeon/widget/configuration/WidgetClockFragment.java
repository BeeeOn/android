package com.rehivetech.beeeon.widget.configuration;


import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.arrayadapter.DeviceArrayAdapter;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.Utils;
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

		setRefreshBounds(WidgetService.UPDATE_INTERVAL_WEATHER_MIN);
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

		mWidgetUpdateSeekBar = (SeekBar) mActivity.findViewById(R.id.widget_config_interval);
		initWidgetUpdateIntervalLayout(mWidgetUpdateSeekBar);

		mDeviceSpinnersWrapper = (LinearLayout) mActivity.findViewById(R.id.widget_config_devices);
		LinearLayout.LayoutParams spinnerLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		spinnerLayoutParams.setMargins(0, 0, 0, (int) mActivity.getResources().getDimension(R.dimen.widget_margin));

		for(WidgetDevicePersistence wDev : mWidgetDevices){
			Spinner deviceSpinner = new Spinner(mActivity);
			mDeviceSpinnersWrapper.addView(deviceSpinner, spinnerLayoutParams);
			mDeviceSpinners.add(deviceSpinner);
		}

		mColorSchemeGroup = (RadioGroup) mActivity.findViewById(R.id.widget_configuration_scheme);
		mColorSchemeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.scheme_white:
						mWidgetData.settings.setColorScheme(R.color.white, R.color.white);
						break;

					case R.id.scheme_black:
						mWidgetData.settings.setColorScheme(R.color.black, R.color.black);
						break;

					case R.id.scheme_pink_cyan:
					default:
						mWidgetData.settings.setColorScheme(R.color.beeeon_primary_cyan, R.color.beeeon_secundary_pink);
						break;
				}
			}
		});

		RelativeLayout locationChooseLine = (RelativeLayout) mActivity.findViewById(R.id.widget_config_location);
		locationChooseLine.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showLocationDialog();
			}
		});
	}

	private void showLocationDialog() {
		// TODO
		SimpleDialogFragment.createBuilder(mActivity, mActivity.getSupportFragmentManager())
			.setTitle("Vyberte lokaci")
			.setMessage("Bude použito Brno!")
			.setPositiveButtonText("Ok")
			.show();
		//*/

		//LocationPickerDialogFragment.show(mActivity);
	}

	@Override
	protected void onFragmentResume() {
		super.onFragmentResume();
		updateIntervalLayout(mWidgetUpdateSeekBar);

		EditText cityName = (EditText) mActivity.findViewById(R.id.city_name_test);
		cityName.setText(mWidgetData.weather.cityName);

		TextView cityLabel = (TextView) mActivity.findViewById(R.id.widget_config_location_label);
		cityLabel.setText(mWidgetData.weather.cityName);

		if(mWidgetData.settings.isColorSchemeEqual(R.color.white, R.color.white)){
			mColorSchemeGroup.check(R.id.scheme_white);
		}
		else if(mWidgetData.settings.isColorSchemeEqual(R.color.white, R.color.white)){
			mColorSchemeGroup.check(R.id.scheme_pink_cyan);
		}
		else if(mWidgetData.settings.isColorSchemeEqual(R.color.black, R.color.black)){
			mColorSchemeGroup.check(R.id.scheme_black);
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

			Location location = Utils.getFromList(device.getFacility().getLocationId(), mLocations);
			if(location != null) {
				wDev.configure(device, adapter, location);
			}
			else{
				wDev.configure(device, adapter);
			}

			index++;
		}

		EditText cityName = (EditText) mActivity.findViewById(R.id.city_name_test);
		mWidgetData.weather.cityName = cityName.getText().toString();

		mWidgetData.weather.getBitmapIcon(true);

		// TODO ZISKAT DATA ZE SERVERU a zavolat mWidgetData.weather.configure();

		mWidgetData.configure(mActivity.isAppWidgetEditing(), getRefreshSeconds(mWidgetUpdateSeekBar.getProgress()), adapter);

		return true;
	}
}


