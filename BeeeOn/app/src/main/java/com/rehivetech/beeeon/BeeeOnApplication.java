package com.rehivetech.beeeon;

/**
 * @author martin
 * @author Tomas Mlynaric
 * @since 24.05.2016
 */

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.analytics.Tracker;
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;
import com.rehivetech.beeeon.model.DatabaseSeed;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * the {@link Tracker}.
 */
public class BeeeOnApplication extends Application {
	private static Context sContext;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("BeeeOn app starting...", "___________________________________");
		sContext = getApplicationContext();
		// TODO setup locale not in activity
		GoogleAnalyticsManager.getInstance().init(sContext, getString(R.string.api_keys_google_analytics_tracking_id));
		// initialize database
		RealmConfiguration config = new RealmConfiguration.Builder(this)
				.deleteRealmIfMigrationNeeded() 			// TODO put away later !!!!
				.schemaVersion(1)
				.name("beeeon.realm")
				.initialData(new DatabaseSeed())
				.build();
		Realm.setDefaultConfiguration(config);
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

