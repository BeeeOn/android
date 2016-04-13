package com.rehivetech.beeeon;

/**
 * Created by martin on 16.10.15.
 */

import android.app.Application;

import com.google.android.gms.analytics.Tracker;
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * the {@link Tracker}.
 */
public class BeeeOnApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		GoogleAnalyticsManager.getInstance().init(getApplicationContext(), getString(R.string.api_keys_google_analytics_tracking_id));
	}
}

