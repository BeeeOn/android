package cz.vutbr.fit.iha.activity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.Temperature;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.persistence.Persistence;

/**
 * The control preference activity handles the preferences for the control extension.
 */
public class SettingsUnitActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {
	/**
	 * keys which are defined in res/xml/preferences.xml
	 */

	private ListPreference mListPrefTemperature;
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
		addPreferencesFromResource(R.xml.unit_preferences);

		mListPrefTemperature = (ListPreference) findPreference(Constants.PERSISTANCE_PREF_TEMPERATURE);
		mListPrefTemperature.setEntries(Temperature.getEntries(this));
		mListPrefTemperature.setEntryValues(Temperature.getEntryValues());
		Temperature actTemp = Temperature.getSharedPreferencesOption(mController.getUserSettings());
		mListPrefTemperature.setSummary(actTemp.getFullNameWithShortName(this));
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
		Temperature actTemp = Temperature.getSharedPreferencesOption(mController.getUserSettings());
		mListPrefTemperature.setSummary(actTemp.getFullNameWithShortName(this));
	}

}
