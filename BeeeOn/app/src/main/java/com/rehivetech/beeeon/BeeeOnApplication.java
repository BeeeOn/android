package com.rehivetech.beeeon;

/**
 * @author martin
 * @author Tomas Mlynaric
 * @since 24.05.2016
 */

import android.app.Application;
import android.content.Context;

import com.google.android.gms.analytics.Tracker;
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;
import com.rehivetech.beeeon.model.DatabaseSeed;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;
import timber.log.Timber;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * the {@link Tracker}.
 */
public class BeeeOnApplication extends Application {
	private static Context sContext;

	@Override
	public void onCreate() {
		super.onCreate();
		Timber.plant(new Timber.DebugTree());

		Timber.i("BeeeOn app starting...___________________________________");
		sContext = getApplicationContext();
		GoogleAnalyticsManager.getInstance().init(sContext, getString(R.string.api_keys_google_analytics_tracking_id));
		// initialize database
		RealmConfiguration config = new RealmConfiguration.Builder(this)
//				.deleteRealmIfMigrationNeeded() // only for developing purposes
				.schemaVersion(2)
				.name("beeeon.realm")
				.initialData(new DatabaseSeed())
				.migration(new RealmMigration() {
					@Override
					public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
						RealmSchema schema = realm.getSchema();

						if (oldVersion == 1) {
							schema.create("VentilationItem");
							schema.create("VentilationItem");
							schema.create("DewingItem");
						}
					}
				})
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

