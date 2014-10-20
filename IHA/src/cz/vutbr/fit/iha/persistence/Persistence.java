package cz.vutbr.fit.iha.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.adapter.device.units.DefaultUnitPackages;
import cz.vutbr.fit.iha.util.Timezone;

/**
 * Persistence service that handles caching data on this device.
 * 
 * @author Robyer
 */
public class Persistence {
	/**
	 * Namespace of global preferences
	 */
	private static final String GLOBAL = "global";

	private final Context mContext;

	public Persistence(Context context) {
		mContext = context;
	}

	/** SHAREDPREFERENCES MANIPULATION **/

	public static String getPreferencesFilename(String namespace) {
		return String.format(Constants.PERSISTENCE_PREF_FILENAME, namespace);
	}

	public static String getPreferencesLastLocation(String adapterId) {
		return String.format(Constants.PERSISTENCE_PREF_FILENAME, adapterId);
	}

	public SharedPreferences getSettings(String namespace) {
		String name = getPreferencesFilename(namespace);
		return mContext.getSharedPreferences(name, 0);
	}

	public void initializeDefaultSettings(String namespace) {
		// String name = getPreferencesFilename(namespace);
		// PreferenceManager.setDefaultValues(mContext, name, Context.MODE_PRIVATE, R.xml.main_preferences, true);
		// PreferenceManager.setDefaultValues(mContext, name, Context.MODE_PRIVATE, R.xml.unit_preferences, true);

		initializePreference(namespace, Constants.PERSISTENCE_PREF_TIMEZONE, String.valueOf(Timezone.getDefault().getId()));

		DefaultUnitPackages.setDefaultUnits(this, namespace);
	}

	/** HELPERS **/

	public void initializePreference(String namespace, String key, String value) {
		if (!getSettings(namespace).contains(key)) {
			setString(namespace, key, value);
		}
	}

	private void setInt(String namespace, String key, int value) {
		Editor settings = getSettings(namespace).edit();
		settings.putInt(key, value);
		settings.commit();
	}
	
	private void setString(String namespace, String key, String value) {
		Editor settings = getSettings(namespace).edit();
		settings.putString(key, value);
		settings.commit();
	}

	private void setOrRemoveString(String namespace, String key, String value) {
		Editor settings = getSettings(namespace).edit();

		if (value == null)
			settings.remove(key);
		else
			settings.putString(key, value);

		settings.commit();
	}

	/** DATA MANIPULATION **/

	// Last user
	
	public void saveLastEmail(String email) {
		setOrRemoveString(GLOBAL, Constants.PERSISTENCE_PREF_LAST_USER, email);
	}

	public String loadLastEmail() {
		return getSettings(GLOBAL).getString(Constants.PERSISTENCE_PREF_LAST_USER, "");
	}
	
	
	// GCM
	
	public void saveGCMRegistrationId(String regId) {
		setString(GLOBAL, Constants.PREF_GCM_REG_ID, regId);
	}
	
	public String loadGCMRegistrationId() { 
		return getSettings(GLOBAL).getString(Constants.PREF_GCM_REG_ID, "");
	}
	
	public void saveLastApplicationVersion(int appVersion) {
		setInt(GLOBAL, Constants.PREF_GCM_APP_VERSION, appVersion);
	}
	
	public int loadLastApplicationVersion() {
		return getSettings(GLOBAL).getInt(Constants.PREF_GCM_APP_VERSION, Integer.MIN_VALUE);
	}

}
