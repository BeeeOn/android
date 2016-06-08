package com.rehivetech.beeeon;

import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import io.realm.Realm;

/**
 * @author mlyko
 * @since 01.06.2016
 */
public class DebugApplication extends BeeeOnApplication {
	@Override
	public void onCreate() {
		super.onCreate();

		Stetho.initialize(
				Stetho.newInitializerBuilder(this)
						.enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
						.enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
						.build()
		);


//		RealmInspectorModulesProvider.builder(this)
//				.withFolder(getCacheDir())
//				.withEncryptionKey("encrypted.realm", key)
//				.withMetaTables()
//				.withDescendingOrder()
//				.withLimit(1000)
//				.databaseNamePattern(Pattern.compile(".+\\.realm"))
//				.build()
	}
}
