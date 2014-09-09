package cz.vutbr.fit.iha.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.DefaultUnitPackages;
import cz.vutbr.fit.iha.settings.Timezone;

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
		String name = getPreferencesFilename(namespace);
		PreferenceManager.setDefaultValues(mContext, name, Context.MODE_PRIVATE, R.xml.main_preferences, true);
		PreferenceManager.setDefaultValues(mContext, name, Context.MODE_PRIVATE, R.xml.unit_preferences, true);
		
		setString(namespace, Constants.PREF_TIMEZONE, Timezone.getDefault().getId());
		
		DefaultUnitPackages.setDefaultUnits(this, namespace);
	}
	
	
	/** HELPERS **/
	
	public void setString(String namespace, String key, String value) {
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
	
	private String getString(String namespace, String key, String defValue) {
		return getSettings(namespace).getString(key, defValue);
	}


	/** DATA MANIPULATION **/
	
	public void saveLastEmail(String email) {
		setOrRemoveString(GLOBAL, Constants.PERSISTENCE_PREF_LAST_USER, email);
	}
	
	public String loadLastEmail() {
		return getSettings(GLOBAL).getString(Constants.PERSISTENCE_PREF_LAST_USER, "");
	}
	
	public void saveActiveAdapter(String userId, String adapterId) {
		setString(userId, Constants.PERSISTENCE_PREF_ACTIVE_ADAPTER, adapterId);
	}
	
	public String loadActiveAdapter(String userId) {
		return getString(userId, Constants.PERSISTENCE_PREF_ACTIVE_ADAPTER, "");
	}
	
}
