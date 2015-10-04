package com.rehivetech.beeeon.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.util.Log;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.household.device.units.NoiseUnit;
import com.rehivetech.beeeon.household.device.units.TemperatureUnit;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.household.user.User.Gender;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.util.ActualizationTime;
import com.rehivetech.beeeon.util.CacheHoldTime;
import com.rehivetech.beeeon.util.Language;
import com.rehivetech.beeeon.util.SettingsItem;
import com.rehivetech.beeeon.util.Timezone;
import com.rehivetech.beeeon.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Persistence service that handles caching data on this module.
 *
 * @author Robyer
 */
public class Persistence {
	private static final String TAG = Persistence.class.getSimpleName();

	/**
	 * Namespace of global preferences
	 */
	public static final String GLOBAL = "global";

	private final Context mContext;

	public Persistence(Context context) {
		mContext = context;
	}

	/**
	 * SHAREDPREFERENCES MANIPULATION *
	 */

	public static String getPreferencesFilename(String namespace) {
		return String.format(Constants.PERSISTENCE_PREF_FILENAME, namespace);
	}

	public static SharedPreferences getSettings(Context context, String namespace) {
		String name = getPreferencesFilename(namespace);
		return context.getSharedPreferences(name, 0);
	}

	public SharedPreferences getSettings(String namespace) {
		return getSettings(mContext, namespace);
	}

	/**
	 * INITIALIZATION OF DEFAULT SETTINGS *
	 */

	private void initItemPreference(String namespace, SettingsItem item, int id) {
		initializePreference(namespace, item.getPersistenceKey(), String.valueOf(id));
	}

	private void initItemDefaultPreference(String namespace, SettingsItem item) {
		initItemPreference(namespace, item, item.getDefaultId());
	}

	public void initializeDefaultSettings(String namespace) {


		initItemDefaultPreference(namespace, new Language());
		initItemDefaultPreference(namespace, new Timezone());

		initItemDefaultPreference(namespace, new ActualizationTime());
		initItemDefaultPreference(namespace, new CacheHoldTime());

		// TODO: use different units based on user Locale, right now we use default values from unit
		/*
		 * Locale locale = Locale.getDefault(); if (locale.getCountry() == "en") { initItemPreference(namespace, new TemperatureUnit(), TemperatureUnit.FAHRENHEIT); } else {
		 * initItemDefaultPreference(namespace, new TemperatureUnit()); }
		 */

		initItemDefaultPreference(namespace, new TemperatureUnit());
		initItemDefaultPreference(namespace, new NoiseUnit());
	}

	/**
	 * HELPERS *
	 */

	public void initializePreference(String namespace, String key, String value) {
		if (!getSettings(namespace).contains(key)) {
			setString(namespace, key, value);
		}
	}

	private void setInt(String namespace, String key, int value) {
		Editor settings = getSettings(namespace).edit();
		settings.putInt(key, value);
		settings.apply();
	}

	private void setString(String namespace, String key, String value) {
		Editor settings = getSettings(namespace).edit();
		settings.putString(key, value);
		settings.apply();
	}

	private void setOrRemoveString(String namespace, String key, String value) {
		Editor settings = getSettings(namespace).edit();

		if (value == null)
			settings.remove(key);
		else
			settings.putString(key, value);

		settings.apply();
	}

	/**
	 * Tries to get external cache dir and if it fails, then returns internal cache dir.
	 *
	 * @return File representing cache dir
	 */
	private File getCacheDir() {
		File dir = mContext.getExternalCacheDir();
		if (dir == null) {
			// If there is not present external sdcard, use internal one
			dir = mContext.getCacheDir();
		}
		return dir;
	}

	private void saveBitmap(Bitmap picture, String filename) {
		OutputStream os = null;
		try {
			File file = new File(getCacheDir(), filename + ".jpg");
			os = new FileOutputStream(file);
			//picture.reconfigure(150, 150, Bitmap.Config.ARGB_8888);
			picture.compress(Bitmap.CompressFormat.JPEG, 90, os);
			os.flush();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null)
					os.close();
			} catch (IOException e) {
			}
		}
	}

	@Nullable
	private Bitmap loadBitmap(String filename) {
		File file = new File(getCacheDir(), filename + ".jpg");
		return file.exists() ? BitmapFactory.decodeFile(file.getAbsolutePath()) : null;
	}

	/**
	 * DATA MANIPULATION *
	 */

	// Last demo mode
	public static boolean loadLastDemoMode(Context context) {
		return getSettings(context, GLOBAL).getBoolean(Constants.PERSISTENCE_PREF_LAST_DEMO_MODE, false);
	}

	public static void saveLastDemoMode(Context context, Boolean demoMode) {
		SharedPreferences.Editor editor = getSettings(context, GLOBAL).edit();

		if (demoMode == null)
			editor.remove(Constants.PERSISTENCE_PREF_LAST_DEMO_MODE);
		else
			editor.putBoolean(Constants.PERSISTENCE_PREF_LAST_DEMO_MODE, demoMode);

		editor.commit();
	}

	// Last server
	public static String loadLoginServerId(Context context) {
		return getSettings(context, GLOBAL).getString(Constants.PERSISTENCE_PREF_LOGIN_SERVER, "");
	}

	public static void saveLoginServerId(Context context, String server) {
		SharedPreferences.Editor editor = getSettings(context, GLOBAL).edit();

		if (server == null)
			editor.remove(Constants.PERSISTENCE_PREF_LOGIN_SERVER);
		else
			editor.putString(Constants.PERSISTENCE_PREF_LOGIN_SERVER, server);

		editor.commit();
	}

	// Last user

	public void saveLastUserId(String userId) {
		setOrRemoveString(GLOBAL, Constants.PERSISTENCE_PREF_LAST_USER_ID, userId);
	}

	public String loadLastUserId() {
		return getSettings(GLOBAL).getString(Constants.PERSISTENCE_PREF_LAST_USER_ID, "");
	}

	public void saveLastAuthProvider(IAuthProvider authProvider) {
		setOrRemoveString(GLOBAL, Constants.PERSISTENCE_PREF_LAST_AUTH_PROVIDER, authProvider != null ? authProvider.getClass().getName() : null);
		setOrRemoveString(GLOBAL, Constants.PERSISTENCE_PREF_LAST_AUTH_PARAMETER, authProvider != null ? authProvider.getPrimaryParameter() : null);
	}

	public IAuthProvider loadLastAuthProvider() {
		String className = getSettings(GLOBAL).getString(Constants.PERSISTENCE_PREF_LAST_AUTH_PROVIDER, "");
		String parameter = getSettings(GLOBAL).getString(Constants.PERSISTENCE_PREF_LAST_AUTH_PARAMETER, "");

		// Return null if we have no className found
		if (className == null || className.isEmpty())
			return null;

		IAuthProvider provider = null;
		try {
			// Try to create provider class from last saved provider
			Class<?> cl = Class.forName(className);
			Constructor<?> ctor = cl.getConstructor((Class<?>[]) null);

			// Create instance and set primary parameter
			provider = (IAuthProvider) ctor.newInstance();
			provider.setPrimaryParameter(parameter);
		} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException | ClassCastException e) {
			Log.e(TAG, String.format("Cant create IAuthProvider class '%s' with parameter '%s'", className, parameter));
		}

		return provider;
	}

	public void saveLastBT(String userId, String BT) {
		setOrRemoveString(userId, Constants.PERSISTENCE_PREF_USER_BT, BT);
	}

	public String loadLastBT(String userId) {
		return getSettings(userId).getString(Constants.PERSISTENCE_PREF_USER_BT, "");
	}

	public void saveUserDetails(String userId, User user) {
		getSettings(userId) //
				.edit() //
				.putString(Constants.PERSISTENCE_PREF_USER_ID, user.getId()) //
				.putString(Constants.PERSISTENCE_PREF_USER_EMAIL, user.getEmail()) //
				.putString(Constants.PERSISTENCE_PREF_USER_NAME, user.getName()) //
				.putString(Constants.PERSISTENCE_PREF_USER_SURNAME, user.getSurname()) //
				.putString(Constants.PERSISTENCE_PREF_USER_GENDER, user.getGender().toString()) //
				.putString(Constants.PERSISTENCE_PREF_USER_PICTURE, user.getPictureUrl()) //
				.apply();

		Bitmap picture = user.getPicture();
		if (picture != null)
			saveBitmap(picture, userId);
	}

	public void loadUserDetails(String userId, User user) {
		SharedPreferences prefs = getSettings(userId);

		user.setId(prefs.getString(Constants.PERSISTENCE_PREF_USER_ID, user.getId()));
		user.setEmail(prefs.getString(Constants.PERSISTENCE_PREF_USER_EMAIL, user.getEmail()));
		user.setName(prefs.getString(Constants.PERSISTENCE_PREF_USER_NAME, user.getName()));
		user.setSurname(prefs.getString(Constants.PERSISTENCE_PREF_USER_SURNAME, user.getSurname()));
		user.setGender(Utils.getEnumFromId(Gender.class, prefs.getString(Constants.PERSISTENCE_PREF_USER_GENDER, user.getGender().toString()), Gender.UNKNOWN));
		user.setPictureUrl(prefs.getString(Constants.PERSISTENCE_PREF_USER_PICTURE, user.getPictureUrl()));

		user.setPicture(loadBitmap(userId));
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
