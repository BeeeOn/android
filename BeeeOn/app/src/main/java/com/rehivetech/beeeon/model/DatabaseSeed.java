package com.rehivetech.beeeon.model;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.model.entity.Server;

import io.realm.Realm;

/**
 * @author mlyko
 * @since 31.05.2016
 */
public class DatabaseSeed implements Realm.Transaction {
	@Override
	public void execute(Realm realm) {
		// deleting all servers
		realm.delete(Server.class);

		// seeding production server
		Server production = Server.PRODUCTION_SERVER;
		realm.copyToRealm(production);

		// seeding devel server
		Server devel = new Server(
				Server.SERVER_ID_DEVEL,
				R.string.server_development,
				"antwork.fit.vutbr.cz",
				8011
		);
		realm.copyToRealm(devel);
	}
}
