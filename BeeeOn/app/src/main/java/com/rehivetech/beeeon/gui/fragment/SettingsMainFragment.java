package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.activity.SettingsUnitActivity;
import com.rehivetech.beeeon.util.ActualizationTime;
import com.rehivetech.beeeon.util.CacheHoldTime;
import com.rehivetech.beeeon.util.Language;
import com.rehivetech.beeeon.util.Timezone;

/**
 * Created by david on 26.8.15.
 */
public class SettingsMainFragment extends BaseSettingsFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

	private OnPreferenceChangedListener mOnPreferenceChangedListener;
	private Timezone mTimezone;
	private Language mLanguage;
	private ListPreference mLanguagePref;
	private ListPreference mTimeZonePref;
	private ListPreference mActualizationPreference;
	private ListPreference mCachePreference;
	private ActualizationTime mActualizationTime;
	private CacheHoldTime mCacheHoldTime;


	protected void initSettings() {
		addPreferencesFromResource(R.xml.activity_settings_main_preferences);

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
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mOnPreferenceChangedListener = (OnPreferenceChangedListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnPreferenceChangedListener");
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(Language.PERSISTENCE_PREF_LANGUAGE)) {
			mOnPreferenceChangedListener.refreshActivity();
		} else if (key.equals(mTimezone.getPersistenceKey())) {
			setSummary(mTimeZonePref, mTimezone);
		} else if (key.equals(mCacheHoldTime.getPersistenceKey())) {
			setSummary(mCachePreference, mCacheHoldTime);
		} else if (key.equals(mActualizationTime.getPersistenceKey())) {
			setSummary(mActualizationPreference, mActualizationTime);
		}
	}

	public interface OnPreferenceChangedListener {
		void refreshActivity();
	}
}
