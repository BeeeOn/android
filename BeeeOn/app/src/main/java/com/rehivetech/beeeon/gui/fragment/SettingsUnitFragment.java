package com.rehivetech.beeeon.gui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.units.NoiseUnit;
import com.rehivetech.beeeon.household.device.units.TemperatureUnit;

/**
 * Created by david on 14.9.15.
 */
public class SettingsUnitFragment extends BaseSettingsFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
	private ListPreference mTemperaturePref;
	private ListPreference mNoisePref;

	private TemperatureUnit mTemperatureUnit;
	private NoiseUnit mNoiseUnit;


	protected void initSettings() {
		addPreferencesFromResource(R.xml.activity_settings_unit_preferences);

		mTemperatureUnit = new TemperatureUnit();
		mTemperaturePref = (ListPreference) findPreference(mTemperatureUnit.getPersistenceKey());

		mNoiseUnit = new NoiseUnit();
		mNoisePref = (ListPreference) findPreference(mNoiseUnit.getPersistenceKey());


		Context context = getActivity();
		initListPrefFromItem(mTemperaturePref, mTemperatureUnit, context);
		initListPrefFromItem(mNoisePref, mNoiseUnit, context);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(mTemperatureUnit.getPersistenceKey())) {
			setSummary(mTemperaturePref, mTemperatureUnit);
		} else if (key.equals(mNoiseUnit.getPersistenceKey())) {
			setSummary(mNoisePref, mNoiseUnit);
		}
	}
}
