package com.rehivetech.beeeon.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;

/**
 * @author martin
 * @since 15/10/2016.
 */

public class PreferencesHelper {

	private PreferencesHelper() {

	}

	public static int getInt(Context context, SharedPreferences preferences, @StringRes int key) {
		String value = preferences.getString(context.getString(key), "0");
		return Utils.parseIntSafely(value, 0);
	}

	public static String getString(Context context, SharedPreferences preferences, @StringRes int key) {
		return preferences.getString(context.getString(key), "");
	}

	public static boolean getBoolean(Context context, SharedPreferences preferences, @StringRes int key) {
		return preferences.getBoolean(context.getString(key), false);
	}
}
