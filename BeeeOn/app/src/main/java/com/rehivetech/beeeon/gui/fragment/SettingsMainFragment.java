package com.rehivetech.beeeon.gui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.avast.android.dialogs.iface.INegativeButtonDialogListener;
import com.avast.android.dialogs.iface.IPositiveButtonDialogListener;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;
import com.rehivetech.beeeon.gui.activity.MainActivity;
import com.rehivetech.beeeon.gui.activity.SettingsUnitActivity;
import com.rehivetech.beeeon.util.ActualizationTime;
import com.rehivetech.beeeon.util.CacheHoldTime;
import com.rehivetech.beeeon.util.Language;
import com.rehivetech.beeeon.util.Timezone;

/**
 * Created by david on 26.8.15.
 */
public class SettingsMainFragment extends BaseSettingsFragment implements SharedPreferences.OnSharedPreferenceChangeListener, IPositiveButtonDialogListener, INegativeButtonDialogListener {

	private final int REQUEST_CODE_LANGUAGE_WARNING = 50;

	private Timezone mTimezone;
	private Language mLanguage;
	private ListPreference mLanguagePref;
	private ListPreference mTimeZonePref;
	private ListPreference mActualizationPreference;
	private ListPreference mCachePreference;
	private ActualizationTime mActualizationTime;
	private CacheHoldTime mCacheHoldTime;

	private String mPreviousLanguage;

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

		mPreviousLanguage = mLanguagePref.getValue();

		Preference units = findPreference(Constants.KEY_UNITS);
		Intent intentUnit = new Intent(getActivity(), SettingsUnitActivity.class);
		units.setIntent(intentUnit);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(Language.PERSISTENCE_PREF_LANGUAGE)) {
			processLanguageChange();
		} else if (key.equals(mTimezone.getPersistenceKey())) {
			setSummary(mTimeZonePref, mTimezone);
		} else if (key.equals(mCacheHoldTime.getPersistenceKey())) {
			setSummary(mCachePreference, mCacheHoldTime);
		} else if (key.equals(mActualizationTime.getPersistenceKey())) {
			setSummary(mActualizationPreference, mActualizationTime);
		}
	}

	@Override
	public void onPositiveButtonClicked(int requestCode) {
		if (requestCode == REQUEST_CODE_LANGUAGE_WARNING) {

			BaseApplicationActivity.activeLocale = null;

			Intent appIntent = new Intent(getActivity(), MainActivity.class);
			appIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			getActivity().startActivity(appIntent);
			getActivity().finish();
		}
	}

	@Override
	public void onNegativeButtonClicked(int requestCode) {
		if (requestCode == REQUEST_CODE_LANGUAGE_WARNING) {
			mLanguagePref.setValue(mPreviousLanguage);
		}

	}

	private void processLanguageChange() {
		SimpleDialogFragment.createBuilder(getContext(), getActivity().getSupportFragmentManager())
				.setMessage(getString(R.string.settings_language_dialog_message))
				.setPositiveButtonText(R.string.dialog_ok)
				.setNegativeButtonText(R.string.dialog_cancel)
				.setTargetFragment(this, REQUEST_CODE_LANGUAGE_WARNING)
				.show();

	}
}
