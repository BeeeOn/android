package cz.vutbr.fit.iha.gcm;

import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.util.Utils;

public class GcmHelper {
	public static final String TAG_GCM = "IHA_GCM";

	/**
	 * Maximum milliseconds which device will wait in thread for reparation GCM ID. If it isn't registered after this time, new thread will be created.
	 */
	private static final int MAX_MILLSEC_GCM = 1000;

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private static SharedPreferences getGCMPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences,
		// but
		// how you store the regID in your app is up to you.
		return context.getSharedPreferences(Constants.SHARED_PREF_GCM_NAME, Context.MODE_PRIVATE);
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's shared preferences.
	 */
	public static void registerGCMInBackground(Context context) {
		new GcmRegisterAsyncTask().execute(context, null, null);
	}

	public static void registerGCMInForeground(Context context) {
		try {
			new GcmRegisterAsyncTask().execute(context, null, null).get(MAX_MILLSEC_GCM, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			Log.e(TAG_GCM, "Couldnt get GCM ID in given time.");
			e.printStackTrace();
		}
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing registration ID.
	 */
	public static String getGCMRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(Constants.PREF_GCM_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG_GCM, "GCM: Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(Constants.PREF_GCM_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = Utils.getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG_GCM, "GCM: App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Stores the registration ID and app versionCode in the application's {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	public static void storeGCMRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = Utils.getAppVersion(context);
		Log.i(TAG_GCM, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Constants.PREF_GCM_REG_ID, regId);
		editor.putInt(Constants.PREF_GCM_APP_VERSION, appVersion);
		editor.commit();
	}
}
