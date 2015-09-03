package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
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
import com.rehivetech.beeeon.util.Timezone;
import com.rehivetech.beeeon.util.Utils;

/**
 * Created by david on 26.8.15.
 */
public class MainSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

	private onPreferenceChangedListener mOnPreferenceChangedListener;
	private Timezone mTimezone;
	private SharedPreferences mSharedPreferences;
	private ListPreference mLanguagePref;
	private ListPreference mTimeZonePref;

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

		mTimezone = new Timezone();
		mTimeZonePref = (ListPreference) findPreference(mTimezone.getPersistenceKey());
		mTimeZonePref.setEntries(mTimezone.getEntries(getActivity()));
		mTimeZonePref.setEntryValues(mTimezone.getEntryValues());
		mTimeZonePref.setSummary(mTimezone.fromSettings(mSharedPreferences).getSettingsName(getActivity()));

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
		if (key.equals("pref_language")) {
			mOnPreferenceChangedListener.setLocale(sharedPreferences.getString(key, null));
		}
		if (mTimezone != null && key.equals(mTimezone.getPersistenceKey())) {
			if (mTimeZonePref != null) {
				String summary = mTimezone.fromSettings(sharedPreferences).getSettingsName(getActivity());
				mTimeZonePref.setSummary(summary);
			}
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
		void setLocale(String lang);
	}
}
