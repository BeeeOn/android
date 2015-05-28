package com.rehivetech.beeeon.gui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.rehivetech.beeeon.ActionBarPreferenceActivity;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.persistence.Persistence;
import com.rehivetech.beeeon.util.Timezone;
import com.rehivetech.beeeon.util.Utils;

/**
 * The control preference activity handles the preferences for the control extension.
 */
public class SettingsMainActivity extends ActionBarPreferenceActivity implements OnSharedPreferenceChangeListener {
	/**
	 * keys which are defined in res/xml/preferences.xml
	 */

	// private ListPreference mListPrefAdapter, mListPrefLocation;

	private ListPreference mTimezoneListPref;
	private Timezone mTimezone;

	private Preference mPrefUnits;
	private Preference mPrefGeofence;
	private Controller mController;
	private SharedPreferences mPrefs;

	@Override
	protected int getPreferencesXmlId() {
		return R.xml.main_preferences;
	}

	// added suppressWarnings because of support of lower version
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mController = Controller.getInstance(this);

		final Toolbar toolbar = getToolbar();
		toolbar.setTitle(R.string.settings);


		// Use own name for sharedPreferences
		getPreferenceManager().setSharedPreferencesName(Persistence.getPreferencesFilename(mController.getActualUser().getId()));

		// UserSettings can be null when user is not logged in!
		mPrefs = mController.getUserSettings();
		if (mPrefs == null) {
			finish();
			return;
		}

		// mListPrefAdapter = (ListPreference) findPreference(Constants.PERSISTENCE_PREF_SW2_ADAPTER);
		// mListPrefLocation = (ListPreference) findPreference(Constants.PERSISTENCE_PREF_SW2_LOCATION);

		mTimezone = new Timezone();
		mTimezoneListPref = (ListPreference) findPreference(mTimezone.getPersistenceKey());
		mTimezoneListPref.setEntries(mTimezone.getEntries(this));
		mTimezoneListPref.setEntryValues(mTimezone.getEntryValues());
		mTimezoneListPref.setSummary(mTimezone.fromSettings(mPrefs).getSettingsName(this));

		mPrefUnits = findPreference(Constants.KEY_UNITS);
		Intent intentUnit = new Intent(this, SettingsUnitActivity.class);
		mPrefUnits.setIntent(intentUnit);

		// mAdapterListPref.set
		// mAdapterListPref.setOnPreferenceChangeListener(this);
		// mLocationListPref.setOnPreferenceChangeListener(this);

		mPrefGeofence = findPreference(Constants.KEY_GEOFENCE);
		if (Utils.isGooglePlayServicesAvailable(this)) {
			Intent intentGeofence = new Intent(this, MapGeofenceActivity.class);
			mPrefGeofence.setIntent(intentGeofence);
		} else {
			mPrefGeofence.setEnabled(false);
		}


	}

	@Override
	protected void onResume() {
		super.onResume();
		mPrefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mPrefs.unregisterOnSharedPreferenceChangeListener(this);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return false;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (mTimezone != null && key.equals(mTimezone.getPersistenceKey())) {
			ListPreference pref = mTimezoneListPref;

			if (pref != null) {
				String summary = mTimezone.fromSettings(sharedPreferences).getSettingsName(this);
				pref.setSummary(summary);
			}
		}
	}

}
