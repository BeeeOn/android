package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragmentCompat;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;

/**
 * Created by david on 26.8.15.
 */
public class MainSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

	private onPreferenceChangedListener mOnPreferenceChangedListener;
	private ListPreference mLanguagePref;
	private ListPreference mTimeZonePref;

	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		addPreferencesFromResource(R.xml.activity_settings_main_preferences);

		SharedPreferences prefs = Controller.getInstance(getActivity()).getUserSettings();
		if (prefs == null) {
			getActivity().finish(); // TODO: use better way to exit
			return;
		}
		PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

		mLanguagePref = (ListPreference) findPreference("pref_language");
		mTimeZonePref = (ListPreference) findPreference("timezone_pref_array");


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
	}

	public interface onPreferenceChangedListener {
		void setLocale(String lang);
	}
}
