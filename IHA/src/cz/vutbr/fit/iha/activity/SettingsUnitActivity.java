package cz.vutbr.fit.iha.activity;

import java.util.HashMap;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.BaseUnit;
import cz.vutbr.fit.iha.adapter.device.units.NoiseUnit;
import cz.vutbr.fit.iha.adapter.device.units.TemperatureUnit;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.persistence.Persistence;

/**
 * The control preference activity handles the preferences for the control extension.
 */
public class SettingsUnitActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {
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
		pref.setSummary(unit.fromSettings(mPrefs).getNameWithUnit(this));
		
		mPreferences.put(unit.getPersistenceKey(), pref);
	}
	
	// added suppressWarnings because of support of lower version
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mController = Controller.getInstance(getApplicationContext());

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher_white);

		// Use own name for sharedPreferences
		getPreferenceManager().setSharedPreferencesName(Persistence.getPreferencesFilename(mController.getActualUser().getEmail()));

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.unit_preferences);

		mPrefs = mController.getUserSettings();
		
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
			String summary = unit.fromSettings(sharedPreferences).getNameWithUnit(this);
			pref.setSummary(summary);
		}
	}

}
