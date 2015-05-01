package com.rehivetech.beeeon.activity;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;


import com.rehivetech.beeeon.ActionBarPreferenceActivity;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.units.BaseUnit;
import com.rehivetech.beeeon.household.device.units.NoiseUnit;
import com.rehivetech.beeeon.household.device.units.TemperatureUnit;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.persistence.Persistence;

/**
 * The control preference activity handles the preferences for the control extension.
 */
public class SettingsUnitActivity extends ActionBarPreferenceActivity implements OnSharedPreferenceChangeListener {
	/**
	 * keys which are defined in res/xml/preferences.xml
	 */

	private final Map<String, BaseUnit> mUnits = new HashMap<String, BaseUnit>();
	private final Map<String, ListPreference> mPreferences = new HashMap<String, ListPreference>();

	private Controller mController;
	private SharedPreferences mPrefs;

	@SuppressWarnings("deprecation")
	private void initUnit(BaseUnit unit) {
		mUnits.put(unit.getPersistenceKey(), unit);

		ListPreference pref = (ListPreference) findPreference(unit.getPersistenceKey());
		pref.setEntries(unit.getEntries(this));
		pref.setEntryValues(unit.getEntryValues());
		pref.setSummary(unit.fromSettings(mPrefs).getSettingsName(this));

		mPreferences.put(unit.getPersistenceKey(), pref);
	}

    @Override
    protected int getPreferencesXmlId() {
        return R.xml.unit_preferences;
    }

    // added suppressWarnings because of support of lower version
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mController = Controller.getInstance(this);

        final Toolbar toolbar=getToolbar();
        toolbar.setTitle(R.string.units);

		// Use own name for sharedPreferences
		getPreferenceManager().setSharedPreferencesName(Persistence.getPreferencesFilename(mController.getActualUser().getId()));


		// UserSettings can be null when user is not logged in!
		mPrefs = mController.getUserSettings();
		if (mPrefs == null) {
			finish();
			return;
		}

		initUnit(new TemperatureUnit());
		initUnit(new NoiseUnit());
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

	// @Override
	// public boolean onPreferenceChange(Preference preference, Object newValue)
	// {
	//
	// setDefaultLocAndAdap();
	//
	// return true;
	// }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		ListPreference pref = mPreferences.get(key);
		BaseUnit unit = mUnits.get(key);

		if (pref != null && unit != null) {
			String summary = unit.fromSettings(sharedPreferences).getSettingsName(this);
			pref.setSummary(summary);

			// inform about settings being changed
			Intent broadcastIntent = new Intent(Constants.BROADCAST_PREFERENCE_CHANGED);
			sendBroadcast(broadcastIntent);
		}
	}

}
