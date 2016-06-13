package com.rehivetech.beeeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.controller.Controller;

/**
 * @author mlyko
 * @since 13.06.16
 */
public class Migration {
	/**
	 * Migrates settings for dashboard
	 *
	 * @param context for getting user data
	 */
	public static void migrateDashboard(Context context) {
		Controller controller = Controller.getInstance(context);
		SharedPreferences preferences = controller.getUserSettings();
		if (preferences == null) return;

		boolean isDashboardMigrated = preferences.getBoolean(Constants.PERSISTENCE_KEY_DASHBOARD_MIGRATE, false);

		if (!isDashboardMigrated) {
			controller.migrateDashboard();
			preferences.edit().putBoolean(Constants.PERSISTENCE_KEY_DASHBOARD_MIGRATE, true).apply();
		}
	}
}
