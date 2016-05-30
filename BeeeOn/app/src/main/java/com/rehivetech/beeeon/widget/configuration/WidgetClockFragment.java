package com.rehivetech.beeeon.widget.configuration;


import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.avast.android.dialogs.core.BaseDialogFragment;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.adapter.ModuleArrayAdapter;
import com.rehivetech.beeeon.gui.dialog.EditTextDialog;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.util.Validator;
import com.rehivetech.beeeon.widget.data.WidgetClockData;
import com.rehivetech.beeeon.widget.persistence.WidgetModulePersistence;
import com.rehivetech.beeeon.widget.service.WeatherProvider;
import com.rehivetech.beeeon.widget.service.WidgetService;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mlyko
 */
public class WidgetClockFragment extends WidgetConfigurationFragment implements EditTextDialog.IPositiveButtonDialogListener {
	private static final String TAG = WidgetClockFragment.class.getSimpleName();
	public static final int REQUEST_LOCATION_DIALOG = 5;
	private static final int SPACE_BETWEEN_MODULE_SPINNERS = 8;

	protected WidgetClockData mWidgetData;

	protected List<WidgetModulePersistence> mWidgetModules;
	protected List<Spinner> mModuleSpinners;

	private RadioGroup mColorSchemeGroup;
	private TextView mCityLabel;
	private WeatherProvider mWeatherProvider;
	private Handler mHandler;
	private WeatherProvider.City mWeatherCity;

	public WidgetClockFragment() {
		mHandler = new Handler();
	}

	@Override
	protected int getFragmentLayoutResource() {
		return R.layout.fragment_widget_clock;
	}

	@Override
	protected int getFragmentTitle() {
		return R.string.widget_clock_widget_configuration_clock;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mGeneralWidgetdata = new WidgetClockData(mActivity.getWidgetId(), mActivity, null, null);
		mWidgetData = (WidgetClockData) mGeneralWidgetdata;

		mWidgetModules = mWidgetData.widgetModules;
		mModuleSpinners = new ArrayList<>();

		setRefreshBounds(WidgetService.UPDATE_INTERVAL_WEATHER_MIN);

		mWeatherProvider = new WeatherProvider(mActivity);

		mWidgetUpdateSeekBar = (SeekBar) mActivity.findViewById(R.id.widget_config_interval);
		initWidgetUpdateIntervalLayout(mWidgetUpdateSeekBar);

		LinearLayout moduleSpinnersWrapper = (LinearLayout) mActivity.findViewById(R.id.widget_config_devices_layout);
		LinearLayout.LayoutParams spinnerLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		int marginBottomPx = Utils.convertDpToPixel(SPACE_BETWEEN_MODULE_SPINNERS);
		spinnerLayoutParams.setMargins(0, 0, 0, marginBottomPx);

		TextView moduleEmptyView = (TextView) mActivity.findViewById(R.id.widget_config_device_emptyview);

		for (WidgetModulePersistence ignored : mWidgetModules) {
			Spinner moduleSpinner = new Spinner(mActivity);
			if (moduleEmptyView != null) {
				moduleSpinner.setEmptyView(moduleEmptyView);
			}

			moduleSpinnersWrapper.addView(moduleSpinner, spinnerLayoutParams);
			mModuleSpinners.add(moduleSpinner);
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
						mWidgetData.settings.setColorScheme(R.color.beeeon_primary, R.color.beeeon_accent);
						break;
				}
			}
		});

		mCityLabel = (TextView) mActivity.findViewById(R.id.widget_config_location_label);

		RelativeLayout locationChooseLine = (RelativeLayout) mActivity.findViewById(R.id.widget_config_location_layout);
		locationChooseLine.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showLocationPickerDialog();
			}
		});
	}

	@Override
	protected void onBeforeGateChanged() {
		super.onBeforeGateChanged();
		int index = 0;
		for (WidgetModulePersistence ignored : mWidgetModules) {
			Spinner spinner = mModuleSpinners.get(index);
			spinner.setAdapter(null);
			index++;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// setup weather location if provided
		if (!mWidgetData.weather.cityName.isEmpty())
			mCityLabel.setText(mWidgetData.weather.cityName);

		// setup color scheme
		if (mWidgetData.settings.isColorSchemeEqual(R.color.white, R.color.white)) {
			mColorSchemeGroup.check(R.id.scheme_white);
		} else if (mWidgetData.settings.isColorSchemeEqual(R.color.white, R.color.white)) {
			mColorSchemeGroup.check(R.id.scheme_pink_cyan);
		} else if (mWidgetData.settings.isColorSchemeEqual(R.color.black, R.color.black)) {
			mColorSchemeGroup.check(R.id.scheme_black);
		}
	}

	private void showLocationPickerDialog() {
		EditTextDialog
				.createBuilder(mActivity, mActivity.getSupportFragmentManager())
				.setTitle(mActivity.getString(R.string.widget_clock_dialog_location_select))
				.setEditTextValue(mWeatherCity != null ? mWeatherCity.name : mWidgetData.weather.cityName)
				.setHint(mActivity.getString(R.string.widget_clock_dialog_location_country))
				.setPositiveButtonText(mActivity.getString(R.string.fragment_configuration_widget_dialog_btn_ok))
				.setNegativeButtonText(mActivity.getString(R.string.activity_fragment_btn_cancel))
				.showKeyboard()
				.setTargetFragment(this, REQUEST_LOCATION_DIALOG)
				.show();
	}

	@Override
	public void onPositiveButtonClicked(int requestCode, View view, final BaseDialogFragment dialog) {
		TextInputLayout cityTextInputLayout = (TextInputLayout) view.findViewById(R.id.dialog_edit_text_input_layout);
		if (!Validator.validate(cityTextInputLayout)) {
			return;
		}

		// city name
		EditText editText = cityTextInputLayout.getEditText();
		if (editText == null)
			return;

		final String cityInput = editText.getText().toString();

		// show dialog of loading
		if (mActivity.getDialog() != null)
			mActivity.getDialog(mActivity.getString(R.string.widget_clock_progress_checking_location)).show();

		// load city data in background
		new Thread() {
			public void run() {
				final JSONObject data = mWeatherProvider.getLocations(cityInput);
				if (data == null) {
					mHandler.post(new Runnable() {
						public void run() {
							loadingCityFail();
						}
					});
				} else {
					final List<WeatherProvider.City> foundCities = mWeatherProvider.parseCities(data);
					mHandler.post(new Runnable() {
						public void run() {
							if (foundCities == null || foundCities.size() == 0) {
								loadingCityFail();
								return;
							}

							WeatherProvider.City city = foundCities.get(0);
							loadingCitySuccess(city, dialog);
						}
					});
				}
			}
		}.start();
	}

	private void loadingCityFail() {
		Toast.makeText(mActivity, mActivity.getString(R.string.widget_clock_weather_place_not_found), Toast.LENGTH_LONG).show();
		if (mActivity.getDialog() != null) mActivity.getDialog().dismiss();
	}

	private void loadingCitySuccess(WeatherProvider.City city, BaseDialogFragment dialog) {
		mWeatherCity = city;
		// setup city label
		mCityLabel.setText(city.name);

		// hide location picker dialog
		if (dialog != null) dialog.dismiss();
		// hide progress dialog
		if (mActivity.getDialog() != null) mActivity.getDialog().dismiss();
	}

	/**
	 * Updates layout and expects to have all data fresh
	 */
	protected void updateLayout() {
		// fill sensor spinner
		ModuleArrayAdapter dataAdapter = new ModuleArrayAdapter(mActivity, R.layout.item_spinner_module_icon_twoline, mModules, mLocations);
		dataAdapter.setLayoutInflater(mActivity.getLayoutInflater());
		dataAdapter.setDropDownViewResource(R.layout.item_spinner_module_icon_twoline_dropdown);

		int index = 0;
		for (WidgetModulePersistence wDev : mWidgetModules) {
			Spinner spinner = mModuleSpinners.get(index);
			spinner.setAdapter(dataAdapter);

			int foundIndex = getModuleIndexFromList(wDev.getId(), mModules);
			if (foundIndex != -1) spinner.setSelection(foundIndex);

			index++;
		}
	}

	@Override
	protected boolean saveSettings() {
		Gate gate = (Gate) mGateSpinner.getSelectedItem();
		if (gate == null) {
			Toast.makeText(mActivity, R.string.widget_clock_location_module_widget_toast_select_gate, Toast.LENGTH_LONG).show();
			return false;
		}

		int index = 0;
		for (WidgetModulePersistence wDev : mWidgetModules) {
			Spinner spinner = mModuleSpinners.get(index);

			Module module = (Module) spinner.getSelectedItem();
			if (module == null) {
				Toast.makeText(mActivity, R.string.widget_clock_module_widget_toast_select_device, Toast.LENGTH_LONG).show();
				return false;
			}

			Location location = Utils.getFromList(module.getDevice().getLocationId(), mLocations);
			if (location != null) {
				wDev.configure(module, gate, location);
			} else {
				wDev.configure(module, gate);
			}

			index++;
		}

		if (mWeatherCity != null) {
			// setup weather persistence
			mWidgetData.weather.id = mWeatherCity.id;
			mWidgetData.weather.cityName = mWeatherCity.name;
			mWidgetData.weather.country = mWeatherCity.countryId;
			mWidgetData.weather.configure(mWeatherCity.json, null);
		}

		// setup widget
		mWidgetData.configure(mActivity.isAppWidgetEditing(), getRefreshSeconds(mWidgetUpdateSeekBar.getProgress()), false, gate);
		return true;
	}
}


