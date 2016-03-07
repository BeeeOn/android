package com.rehivetech.beeeon.util;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.Constants;

/**
 * @author Tomas Mlynaric
 */
public class UnavailableModules {
	public static final String persistenceKey = Constants.PERSISTENCE_PREF_UNAVAILABLE_MODULES;

	/**
	 * Returns value which is saved in sharedPreferences
	 * @param prefs shared preferences for specified user
	 * @return true if unavailable modules should be hidden, false otherwise
	 */
	public static boolean fromSettings(@Nullable SharedPreferences prefs) {
		return prefs != null && prefs.getBoolean(persistenceKey, false);
	}
}
