package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.MapGeofenceActivity;
import com.rehivetech.beeeon.gui.activity.SettingsUnitActivity;
import com.rehivetech.beeeon.persistence.Persistence;
import com.rehivetech.beeeon.util.ActualizationTime;
import com.rehivetech.beeeon.util.CacheHoldTime;
import com.rehivetech.beeeon.util.Language;
import com.rehivetech.beeeon.util.SettingsItem;
import com.rehivetech.beeeon.util.Timezone;
import com.rehivetech.beeeon.util.Utils;

/**
 * Created by david on 26.8.15.
 */
public class MainSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

	private onPreferenceChangedListener mOnPreferenceChangedListener;
	private Timezone mTimezone;
	private Language mLanguage;
	private SharedPreferences mSharedPreferences;
	private ListPreference mLanguagePref;
	private ListPreference mTimeZonePref;
	private ListPreference mActualizationPreference;
	private ListPreference mCachePreference;
	private ActualizationTime mActualizationTime;
	private CacheHoldTime mCacheHoldTime;


	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		addPreferencesFromResource(R.xml.activity_settings_main_preferences);

		mSharedPreferences = Controller.getInstance(getActivity()).getUserSettings();
		if (mSharedPreferences == null) {
			getActivity().finish(); // TODO: use better way to exit
			return;
		}
		PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

		// Use own name for sharedPreferences
		getPreferenceManager().setSharedPreferencesName(Persistence.getPreferencesFilename(Controller.getInstance(getActivity()).getActualUser().getId()));

		mLanguagePref = (ListPreference) findPreference(Language.PERSISTENCE_PREF_LANGUAGE);
		mLanguage = new Language();

		mTimeZonePref = (ListPreference) findPreference(Constants.PERSISTENCE_PREF_TIMEZONE);
		mTimezone = new Timezone();

		mActualizationPreference = (ListPreference) findPreference(ActualizationTime.PERSISTENCE_ACTUALIZATON_KEY);
		mActualizationTime = new ActualizationTime();

		mCachePreference = (ListPreference) findPreference(CacheHoldTime.PERSISTENCE_CACHE_KEY);
		mCacheHoldTime = new CacheHoldTime();

		Context context = getActivity();
		initListPrefFromItem(mTimeZonePref, mTimezone, context);
		initListPrefFromItem(mLanguagePref, mLanguage, context);
		initListPrefFromItem(mActualizationPreference, mActualizationTime, context);
		initListPrefFromItem(mCachePreference, mCacheHoldTime, context);

		Preference units = findPreference(Constants.KEY_UNITS);
		Intent intentUnit = new Intent(getActivity(), SettingsUnitActivity.class);
		units.setIntent(intentUnit);

		Preference geofence = findPreference(Constants.KEY_GEOFENCE);
		if (Utils.isGooglePlayServicesAvailable(getActivity())) {
			Intent intentGeofence = new Intent(getActivity(), MapGeofenceActivity.class);
			geofence.setIntent(intentGeofence);
		} else {
			geofence.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Toast.makeText(getActivity(), R.string.settings_main_toast_no_google_play_services, Toast.LENGTH_LONG).show();
					return true;
				}
			});
		}

	}

	private void initListPrefFromItem(ListPreference listPreference, SettingsItem settingsItem, Context context) {
		SettingsItem.BaseItem item = settingsItem.fromSettings(mSharedPreferences);

		listPreference.setEntries(settingsItem.getEntries(context));
		listPreference.setEntryValues(settingsItem.getEntryValues());
		listPreference.setSummary(item.getSettingsName(context));
		listPreference.setValue(String.valueOf(item.getId()));
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mOnPreferenceChangedListener = (onPreferenceChangedListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement onViewSelected");
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(Language.PERSISTENCE_PREF_LANGUAGE)) {
			mOnPreferenceChangedListener.refreshActivity();
		} else if(key.equals(mTimezone.getPersistenceKey())) {
			setSummary(mTimeZonePref, mTimezone);
		} else if (key.equals(mCacheHoldTime.getPersistenceKey())){
			setSummary(mCachePreference, mCacheHoldTime);
		} else if(key.equals(mActualizationTime.getPersistenceKey())){
			setSummary(mActualizationPreference, mActualizationTime);
		}
	}

	private void setSummary(ListPreference listPreference, SettingsItem settingsItem) {
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

	public interface onPreferenceChangedListener {
		void refreshActivity();
	}
}
