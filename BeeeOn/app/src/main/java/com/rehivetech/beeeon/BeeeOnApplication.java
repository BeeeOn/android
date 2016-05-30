package com.rehivetech.beeeon;

/**
 * @author martin
 * @since 24.05.2016
 */

import android.app.Application;
import android.content.Context;

import com.google.android.gms.analytics.Tracker;
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * the {@link Tracker}.
 */
public class BeeeOnApplication extends Application {
	private static Context sContext;

	@Override
	public void onCreate() {
		super.onCreate();
		GoogleAnalyticsManager.getInstance().init(getApplicationContext(), getString(R.string.api_keys_google_analytics_tracking_id));
		sContext = getApplicationContext();
	}

	/**
	 * Returns application context so that it's possible to get app specifi resources/show toast etc
	 *
	 * @return Application context
	 */
	public static Context getContext() {
		return sContext;
	}
}

