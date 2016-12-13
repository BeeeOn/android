package com.rehivetech.beeeon.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.BeeeOnApplication;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.household.user.User.Gender;
import com.rehivetech.beeeon.model.entity.Server;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.util.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import timber.log.Timber;


/**
 * Persistence service that handles caching data on this module.
 *
 * @author Robyer
 * @author Tomas Mlynaric
 */
public class Persistence {

	/**
	 * Gets formatted filename for shared preferences
	 * @param namespace postfix for the filename
	 * @return forrmated filename as string
	 */
	public static String getPreferencesFilename(String namespace) {
		return String.format(Constants.PERSISTENCE_PREF_FILENAME, namespace);
	}

	/**
	 * Gets settings by namespace (shared preferences file name is formatted)
	 *
	 * @param context   might be app context
	 * @param namespace file postfix
	 * @return opened shared prefs
	 */
	public static SharedPreferences getSettings(Context context, String namespace) {
		String name = getPreferencesFilename(namespace);
		return context.getSharedPreferences(name, Context.MODE_PRIVATE);
	}


	/**
	 * User specific settings.
	 * Contains data for one user, might be more users in the app.
	 */
	public static class UserSettings {

		public static String loadLastBT(Context context, String userId) {
			return getSettings(context, userId).getString(Constants.PERSISTENCE_PREF_USER_BT, "");
		}


		public static void saveLastBT(Context context, String userId, @Nullable String beeeonToken) {
			Editor settings = getSettings(context, userId).edit();

			if(beeeonToken == null) {
				settings.remove(Constants.PERSISTENCE_PREF_USER_BT);
			} else {
				settings.putString(Constants.PERSISTENCE_PREF_USER_BT, beeeonToken);
			}

			settings.apply();
		}


		public static void loadProfile(Context context, String userId, User user) {
			SharedPreferences prefs = getSettings(context, userId);

			user.setId(prefs.getString(Constants.PERSISTENCE_PREF_USER_ID, user.getId()));
			user.setEmail(prefs.getString(Constants.PERSISTENCE_PREF_USER_EMAIL, user.getEmail()));
			user.setName(prefs.getString(Constants.PERSISTENCE_PREF_USER_NAME, user.getName()));
			user.setSurname(prefs.getString(Constants.PERSISTENCE_PREF_USER_SURNAME, user.getSurname()));
			user.setGender(Utils.getEnumFromId(Gender.class, prefs.getString(Constants.PERSISTENCE_PREF_USER_GENDER, user.getGender().toString()), Gender.UNKNOWN));
			user.setPictureUrl(prefs.getString(Constants.PERSISTENCE_PREF_USER_PICTURE, user.getPictureUrl()));
		}


		public static void saveProfile(Context context, String userId, User user) {
			getSettings(context, userId)
					.edit()
					.putString(Constants.PERSISTENCE_PREF_USER_ID, user.getId())
					.putString(Constants.PERSISTENCE_PREF_USER_EMAIL, user.getEmail())
					.putString(Constants.PERSISTENCE_PREF_USER_NAME, user.getName())
					.putString(Constants.PERSISTENCE_PREF_USER_SURNAME, user.getSurname())
					.putString(Constants.PERSISTENCE_PREF_USER_GENDER, user.getGender().toString())
					.putString(Constants.PERSISTENCE_PREF_USER_PICTURE, user.getPictureUrl())
					.apply();
		}
	}


	/**
	 * Global settings of application, only one for whole app.
	 */
	public static class Global {
		/**
		 * Namespace of global preferences
		 */
		public static final String NAMESPACE = "global";


		/**
		 * Get global SharedPreferences for whole application
		 */
		@NonNull
		public static SharedPreferences getSettings() {
			return Persistence.getSettings(BeeeOnApplication.getContext(), NAMESPACE);
		}


		/**
		 * Saves auth provider
		 *
		 * @param authProvider this will be saved by its classname + primary parameter (e-mail,..)
		 */
		public static void saveLastAuthProvider(@Nullable IAuthProvider authProvider) {
			SharedPreferences globalSettings = getSettings();
			if(authProvider == null) {
				globalSettings.edit()
						.remove(Constants.PERSISTENCE_PREF_LAST_AUTH_PROVIDER)
						.remove(Constants.PERSISTENCE_PREF_LAST_AUTH_PARAMETER)
						.apply();
			} else {
				globalSettings.edit()
						.putString(Constants.PERSISTENCE_PREF_LAST_AUTH_PROVIDER, authProvider.getClass().getName())
						.putString(Constants.PERSISTENCE_PREF_LAST_AUTH_PARAMETER, authProvider.getPrimaryParameter())
						.apply();
			}
		}


		/**
		 * Returns last used login provider
		 *
		 * @return provider which was last used for login
		 */
		@Nullable
		public static IAuthProvider getLastAuthProvider() {
			SharedPreferences globalSettings = getSettings();
			String className = globalSettings.getString(Constants.PERSISTENCE_PREF_LAST_AUTH_PROVIDER, "");
			String parameter = globalSettings.getString(Constants.PERSISTENCE_PREF_LAST_AUTH_PARAMETER, "");

			// Return null if we have no className found
			if(className.isEmpty()) return null;

			IAuthProvider provider = null;
			try {
				// Try to create provider class from last saved provider
				Class<?> cl = Class.forName(className);
				Constructor<?> ctor = cl.getConstructor((Class<?>[]) null);

				// Create instance and set primary parameter
				provider = (IAuthProvider) ctor.newInstance();
				provider.setPrimaryParameter(parameter);
			} catch(ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException | ClassCastException e) {
				Timber.e("Cant create IAuthProvider class '%s' with parameter '%s'", className, parameter);
			}

			return provider;
		}


		/**
		 * Saves application version to persistence
		 *
		 * @param appVersion version of application
		 */
		public static void saveLastApplicationVersion(int appVersion) {
			getSettings()
					.edit()
					.putInt(Constants.PREF_GCM_APP_VERSION, appVersion)
					.apply();
		}


		public static int loadLastApplicationVersion() {
			return getSettings().getInt(Constants.PREF_GCM_APP_VERSION, Integer.MIN_VALUE);
		}


		public static String loadGCMRegistrationId() {
			return getSettings().getString(Constants.PREF_GCM_REG_ID, "");
		}


		public static void saveGCMRegistrationId(String regId) {
			getSettings()
					.edit()
					.putString(Constants.PREF_GCM_REG_ID, regId)
					.apply();
		}


		// ----------- Last user


		public static void saveLastUserId(@Nullable String userId) {
			Editor editor = getSettings().edit();
			if(userId == null) {
				editor.remove(Constants.PERSISTENCE_PREF_LAST_USER_ID);
			} else {
				editor.putString(Constants.PERSISTENCE_PREF_LAST_USER_ID, userId);
			}

			editor.apply();
		}


		public static String loadLastUserId() {
			return getSettings().getString(Constants.PERSISTENCE_PREF_LAST_USER_ID, "");
		}

		// ----------- Last server


		public static long loadLoginServerId() {
			return getSettings().getLong(Constants.PERSISTENCE_PREF_LOGIN_SERVER, Server.SERVER_ID_PRODUCTION);
		}


		public static void saveLoginServerId(@Nullable Long server) {
			Editor editor = getSettings().edit();

			if(server == null)
				editor.remove(Constants.PERSISTENCE_PREF_LOGIN_SERVER);
			else
				editor.putLong(Constants.PERSISTENCE_PREF_LOGIN_SERVER, server);

			editor.apply();
		}


		public static void saveLastDemoMode(@Nullable Boolean demoMode) {
			Editor editor = getSettings().edit();

			if(demoMode == null)
				editor.remove(Constants.PERSISTENCE_PREF_LAST_DEMO_MODE);
			else
				editor.putBoolean(Constants.PERSISTENCE_PREF_LAST_DEMO_MODE, demoMode);

			editor.apply();
		}


		/**
		 * DATA MANIPULATION *
		 */

		public static boolean loadLastDemoMode() {
			return getSettings().getBoolean(Constants.PERSISTENCE_PREF_LAST_DEMO_MODE, false);
		}
	}

}
