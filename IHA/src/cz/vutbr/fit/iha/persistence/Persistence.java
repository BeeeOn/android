package cz.vutbr.fit.iha.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.adapter.device.units.NoiseUnit;
import cz.vutbr.fit.iha.adapter.device.units.TemperatureUnit;
import cz.vutbr.fit.iha.household.ActualUser;
import cz.vutbr.fit.iha.household.User.Gender;
import cz.vutbr.fit.iha.util.SettingsItem;
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

	/** INITIALIZATION OF DEFAULT SETTINGS **/

	private void initItemPreference(String namespace, SettingsItem item, int id) {
		initializePreference(namespace, item.getPersistenceKey(), String.valueOf(id));
	}

	private void initItemDefaultPreference(String namespace, SettingsItem item) {
		initItemPreference(namespace, item, item.getDefaultId());
	}

	public void initializeDefaultSettings(String namespace) {
		initItemDefaultPreference(namespace, new Timezone());

		// TODO: use different units based on user Locale, right now we use default values from unit
		/*
		 * Locale locale = Locale.getDefault(); if (locale.getCountry() == "en") { initItemPreference(namespace, new TemperatureUnit(), TemperatureUnit.FAHRENHEIT); } else {
		 * initItemDefaultPreference(namespace, new TemperatureUnit()); }
		 */

		initItemDefaultPreference(namespace, new TemperatureUnit());
		initItemDefaultPreference(namespace, new NoiseUnit());
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
	
	private void saveBitmap(Bitmap picture, String filename) {
		File file = new File(mContext.getExternalCacheDir(), filename + ".jpg");
		
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
			picture.compress(Bitmap.CompressFormat.JPEG, 95, os);
			os.flush();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null)
					os.close();
			} catch (IOException e) {}
		}
	}
	
	private Bitmap loadBitmap(String filename) {
		File file = new File(mContext.getExternalCacheDir(), filename + ".jpg");
		return BitmapFactory.decodeFile(file.getAbsolutePath());
	}

	/** DATA MANIPULATION **/

	// Last user

	public void saveLastEmail(String email) {
		setOrRemoveString(GLOBAL, Constants.PERSISTENCE_PREF_LAST_USER, email);
	}

	public String loadLastEmail() {
		return getSettings(GLOBAL).getString(Constants.PERSISTENCE_PREF_LAST_USER, "");
	}
	
	public void saveLastUID(String email, String UID) {
		setOrRemoveString(email, Constants.PERSISTENCE_PREF_UID, UID);
	}

	public String loadLastUID(String email) {
		return getSettings(email).getString(Constants.PERSISTENCE_PREF_UID, "");
	}
	
	public void saveUserDetails(String email, ActualUser user) {
		getSettings(email) //
			.edit() //
			.putString(Constants.PERSISTENCE_PREF_USER_EMAIL, user.getEmail()) //
			.putString(Constants.PERSISTENCE_PREF_USER_NAME, user.getName()) //
			.putString(Constants.PERSISTENCE_PREF_USER_GENDER, user.getGender().toString()) //
			.putString(Constants.PERSISTENCE_PREF_USER_PICTURE, user.getPictureUrl()) //
			.commit();
		
		Bitmap picture = user.getPicture();
		if (picture != null)
			saveBitmap(picture, email);
	}

	public void loadUserDetails(String email, ActualUser user) {
		SharedPreferences prefs = getSettings(email);
		
		user.setEmail(
			prefs.getString(Constants.PERSISTENCE_PREF_USER_EMAIL, ""));
		user.setName(
			prefs.getString(Constants.PERSISTENCE_PREF_USER_NAME, ""));
		user.setGender(Gender.fromString(
			prefs.getString(Constants.PERSISTENCE_PREF_USER_GENDER, "")));
		user.setPictureUrl(
			prefs.getString(Constants.PERSISTENCE_PREF_USER_PICTURE, ""));
		
		user.setPicture(loadBitmap(email));
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
