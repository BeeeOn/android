package cz.vutbr.fit.iha.activity;

import java.util.List;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.controller.Controller;

/**
 * The control preference activity handles the preferences for the control
 * extension.
 */
public class SettingsActivity extends SherlockPreferenceActivity implements
		 OnSharedPreferenceChangeListener {
	/**
	 * keys which are defined in res/xml/preferences.xml
	 */

	private ListPreference mAdapterListPref, mLocationListPref;
	private Controller mController;
	private SharedPreferences prefs;

	// added suppressWarnings because of support of lower version
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		mController = Controller.getInstance(this);

		mAdapterListPref = (ListPreference) findPreference(Constants.PREF_SW2_ADAPTER);
		mLocationListPref = (ListPreference) findPreference(Constants.PREF_SW2_LOCATION);

		
//		mAdapterListPref.set
//		mAdapterListPref.setOnPreferenceChangeListener(this);
//		mLocationListPref.setOnPreferenceChangeListener(this);
		
		setDefaultLocAndAdap();
	}

	@Override
	protected void onResume() {
		super.onResume();
		prefs.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	private void setDefaultLocAndAdap() {
		List<Adapter> adapters = mController.getAdapters();

		

		// no adapter, disable adapter and location ListPreference
		if (adapters.size() < 1) {
			mAdapterListPref.setEnabled(false);
			mLocationListPref.setEnabled(false);

			mLocationListPref.setSummary(R.string.no_location_available);
			mAdapterListPref.setSummary(R.string.no_location_available);

			return;
		}

		// only 1 adapter available, set as default and disable adapter choice
		if (adapters.size() < 2) {
			mAdapterListPref.setEnabled(false);
			Adapter adapter = adapters.get(0);

			// save default adapter
			prefs.edit().putString(Constants.PREF_SW2_ADAPTER, adapter.getId());
			prefs.edit().commit();

			mAdapterListPref.setSummary(adapter.getName());

			setLocationList(adapter);
		} else {
			mAdapterListPref.setEnabled(true);

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

			mAdapterListPref.setEntries(entries);
			mAdapterListPref.setEntryValues(entryValues);
			mAdapterListPref.setDefaultValue("");

			String adapterId = prefs
					.getString(Constants.PREF_SW2_ADAPTER, "");

			Adapter adapter;
			// valid default adapter
			if (adapterId != ""
					&& (adapter = mController.getAdapter(adapterId, false)) != null) {
				mAdapterListPref.setSummary(adapter.getName());
				setLocationList(adapter);
			}
			// invalid adapter or it hasn't been chosen yet
			else {
				// in case of invalid adapter - rewrite
				prefs.edit().putString(Constants.PREF_SW2_ADAPTER, "");
				prefs.edit().commit();

				// set "None" as summary
				mAdapterListPref.setSummary(R.string.none);
				mLocationListPref.setEnabled(false);
				mLocationListPref.setSummary(R.string.none);
			}
		}

	}

	private void setLocationList(Adapter adapter) {
		mLocationListPref.setEnabled(true);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		String locId = prefs.getString(Constants.PREF_SW2_LOCATION, "");
		Location loc;
		// valid
		if (locId != "" && (loc = adapter.getLocation(locId)) != null) {
			mLocationListPref.setSummary(loc.getName());
		} else {
			mLocationListPref.setSummary(R.string.none);
			prefs.edit().putString(Constants.PREF_SW2_LOCATION, "");
			prefs.edit().commit();
		}
		List<Location> locations = adapter.getLocations();
		// no location available
		if (locations.size() < 1) {
			mLocationListPref.setEnabled(false);
			mLocationListPref.setSummary(R.string.none);
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

			mLocationListPref.setEntries(entries);
			mLocationListPref.setEntryValues(entryValues);
			mLocationListPref.setDefaultValue("");
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

//	@Override
//	public boolean onPreferenceChange(Preference preference, Object newValue) {
//
//		setDefaultLocAndAdap();
//		
//		return true;
//	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		setDefaultLocAndAdap();
	}
	

}
