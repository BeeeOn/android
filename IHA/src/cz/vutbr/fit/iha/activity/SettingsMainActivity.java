package cz.vutbr.fit.iha.activity;

import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.persistence.Persistence;
import cz.vutbr.fit.iha.settings.Timezone;

/**
 * The control preference activity handles the preferences for the control
 * extension.
 */
public class SettingsMainActivity extends SherlockPreferenceActivity implements
		OnSharedPreferenceChangeListener {
	/**
	 * keys which are defined in res/xml/preferences.xml
	 */

	private ListPreference mListPrefAdapter, mListPrefLocation, mListPrefTimezone;
	private Preference mPrefUnits;
	private Controller mController;
	private SharedPreferences mPrefs;

	// added suppressWarnings because of support of lower version
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mController = Controller.getInstance(this);
		
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher_white);
		
		// Use own name for sharedPreferences
		getPreferenceManager().setSharedPreferencesName(Persistence.getPreferencesFilename(mController.getActualUser().getEmail()));

		mPrefs = mController.getUserSettings();

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.main_preferences);

		mListPrefAdapter = (ListPreference) findPreference(Constants.PREF_SW2_ADAPTER);
		mListPrefLocation = (ListPreference) findPreference(Constants.PREF_SW2_LOCATION);
		
		mListPrefTimezone = (ListPreference) findPreference(Constants.PREF_TIMEZONE);
		mListPrefTimezone.setEntries(Timezone.getEntries(this));
		mListPrefTimezone.setEntryValues(Timezone.getEntryValues());
		mListPrefTimezone.setSummary(Timezone.getSharedPreferenceOption(this).getName(this));
		
		mPrefUnits = findPreference(Constants.KEY_UNITS);
        Intent intentUnit = new Intent(this, SettingsUnitActivity.class);
		mPrefUnits.setIntent(intentUnit);
		
		// mAdapterListPref.set
		// mAdapterListPref.setOnPreferenceChangeListener(this);
		// mLocationListPref.setOnPreferenceChangeListener(this);

		redraw();
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

	private void redraw() {
		setDefaultLocAndAdap();
		mListPrefTimezone.setSummary(Timezone.getSharedPreferenceOption(this).getName(this));
	}
	
	// FIXME: This method must use same ActiveAdapter from application,
	// we can't have in controller more activeAdapters for getting locations and devices...
	// Use mController.setActiveAdapter(...) to switch to another adapter 
	private void setDefaultLocAndAdap() {
		List<Adapter> adapters = mController.getAdapters();

		// no adapter, disable adapter and location ListPreference
		if (adapters.size() < 1) {
			mListPrefAdapter.setEnabled(false);
			mListPrefLocation.setEnabled(false);

			mListPrefLocation.setSummary(R.string.no_location_available);
			mListPrefAdapter.setSummary(R.string.no_location_available);
		}
		// only 1 adapter available, disable adapter choice
		else if (adapters.size() < 2) {
			
			mListPrefAdapter.setEnabled(false);
			Adapter adapter = adapters.get(0);

			mListPrefAdapter.setSummary(adapter.getName());

			setLocationList();
		}
		// 2 or more adapters available
		else {
			mListPrefAdapter.setEnabled(true);

			// fill lists with data
			CharSequence[] entries = new CharSequence[adapters.size() + 1];
			CharSequence[] entryValues = new CharSequence[adapters.size() + 1];

			// add first item as "None" with null value
			entries[0] = getString(R.string.none);
			entryValues[0] = "";

			// fill the rest
			for (int i = 0; i < adapters.size(); i++) {
				entries[i + 1] = adapters.get(i).getName();
				entryValues[i + 1] = adapters.get(i).getId();
			}

			mListPrefAdapter.setEntries(entries);
			mListPrefAdapter.setEntryValues(entryValues);
			mListPrefAdapter.setDefaultValue("");

			Adapter adapter = mController.getActiveAdapter();
			if (adapter != null) {
				mListPrefAdapter.setSummary(adapter.getName());
				setLocationList();
			} else {
				// set "None" as summary
				mListPrefAdapter.setSummary(R.string.none);
				mListPrefLocation.setEnabled(false);
				mListPrefLocation.setSummary(R.string.none);
			}
		}

	}

	private void setLocationList() {
		mListPrefLocation.setEnabled(true);

		String locId = mPrefs.getString(Constants.PREF_SW2_LOCATION, "");
		Location loc;
		// valid
		if (locId != "" && (loc = mController.getLocation(locId)) != null) {
			mListPrefLocation.setSummary(loc.getName());
		} else {
			mListPrefLocation.setSummary(R.string.none);
			mPrefs.edit().putString(Constants.PREF_SW2_LOCATION, "");
			mPrefs.edit().commit();
		}
		List<Location> locations = mController.getLocations();
		// no location available
		if (locations.size() < 1) {
			mListPrefLocation.setEnabled(false);
			mListPrefLocation.setSummary(R.string.none);
		}
		// 1 and more locations available
		else {
			// fill lists with data
			CharSequence[] entries = new CharSequence[locations.size() + 1];
			CharSequence[] entryValues = new CharSequence[locations.size() + 1];

			// add first item as "None" with null value
			entries[0] = getString(R.string.none);
			entryValues[0] = "";

			// fill the rest
			for (int i = 0; i < locations.size(); i++) {
				entries[i + 1] = locations.get(i).getName();
				entryValues[i + 1] = locations.get(i).getId();
			}

			mListPrefLocation.setEntries(entries);
			mListPrefLocation.setEntryValues(entryValues);
			mListPrefLocation.setDefaultValue("");
		}
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
		redraw();
	}

}
