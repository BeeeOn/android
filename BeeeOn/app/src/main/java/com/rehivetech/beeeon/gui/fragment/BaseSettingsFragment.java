package com.rehivetech.beeeon.gui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragmentCompat;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.persistence.Persistence;
import com.rehivetech.beeeon.util.SettingsItem;

/**
 * Created by david on 14.9.15.
 */
public abstract class BaseSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
	private SharedPreferences mSharedPreferences;

	@Override
	public final void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

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

		initSettings();

	}

	/**
	 * @brief: int this method you need to add preferences and ititialize them
	 */
	protected abstract void initSettings();

	protected void initListPrefFromItem(ListPreference listPreference, SettingsItem settingsItem, Context context) {
		SettingsItem.BaseItem item = settingsItem.fromSettings(mSharedPreferences);

		listPreference.setEntries(settingsItem.getEntries(context));
		listPreference.setEntryValues(settingsItem.getEntryValues());
		listPreference.setSummary(item.getSettingsName(context));
		listPreference.setValue(String.valueOf(item.getId()));
	}

	protected void setSummary(ListPreference listPreference, SettingsItem settingsItem) {
		if (settingsItem != null && listPreference != null) {
			String summary = settingsItem.fromSettings(mSharedPreferences).getSettingsName(getActivity());
			listPreference.setSummary(summary);
		}
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
	public abstract void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key);
}
