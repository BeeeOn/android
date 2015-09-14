package com.rehivetech.beeeon.gui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragmentCompat;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.units.NoiseUnit;
import com.rehivetech.beeeon.household.device.units.TemperatureUnit;
import com.rehivetech.beeeon.persistence.Persistence;
import com.rehivetech.beeeon.util.SettingsItem;

/**
 * Created by david on 14.9.15.
 */
public class SettingsUnitFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
	private SharedPreferences mSharedPreferences;

	private ListPreference mTemperaturePref;
	private ListPreference mNoisePref;

	private TemperatureUnit mTemperatureUnit;
	private NoiseUnit mNoiseUnit;

	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		addPreferencesFromResource(R.xml.activity_settings_unit_preferences);

		String userId = Controller.getInstance(getActivity()).getActualUser().getId();
		if (userId.isEmpty()) {
			// We can't work without userId
			return;
		}

		// Use own name for sharedPreferences
		PreferenceManager manager = getPreferenceManager();
		manager.setSharedPreferencesName(Persistence.getPreferencesFilename(userId));

		mSharedPreferences = manager.getSharedPreferences();
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);


		mTemperatureUnit = new TemperatureUnit();
		mTemperaturePref = (ListPreference) findPreference(mTemperatureUnit.getPersistenceKey());

		mNoiseUnit = new NoiseUnit();
		mNoisePref = (ListPreference) findPreference(mNoiseUnit.getPersistenceKey());


		Context context = getActivity();
		initListPrefFromItem(mTemperaturePref, mTemperatureUnit, context);
		initListPrefFromItem(mNoisePref, mNoiseUnit, context);
	}

	private void initListPrefFromItem(ListPreference listPreference, SettingsItem settingsItem, Context context) {
		SettingsItem.BaseItem item = settingsItem.fromSettings(mSharedPreferences);

		listPreference.setEntries(settingsItem.getEntries(context));
		listPreference.setEntryValues(settingsItem.getEntryValues());
		listPreference.setSummary(item.getSettingsName(context));
		listPreference.setValue(String.valueOf(item.getId()));
	}

	@Override
	public void onResume() {
		super.onResume();
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(mTemperatureUnit.getPersistenceKey())) {
			setSummary(mTemperaturePref, mTemperatureUnit);
		} else if (key.equals(mNoiseUnit.getPersistenceKey())) {
			setSummary(mNoisePref, mNoiseUnit);
		}
	}

	private void setSummary(ListPreference listPreference, SettingsItem settingsItem) {
		if (settingsItem != null && listPreference != null) {
			String summary = settingsItem.fromSettings(mSharedPreferences).getSettingsName(getActivity());
			listPreference.setSummary(summary);

			// inform about settings being changed
			//Intent broadcastIntent = new Intent(Constants.BROADCAST_PREFERENCE_CHANGED);
			//getActivity().sendBroadcast(broadcastIntent);
		}
	}
}
