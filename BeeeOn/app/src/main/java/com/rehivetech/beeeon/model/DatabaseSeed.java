package com.rehivetech.beeeon.model;

import android.content.Context;

import com.rehivetech.beeeon.BeeeOnApplication;
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
		Context appContext = BeeeOnApplication.getContext();

		// seeding production server
		Server production = new Server(Server.SERVER_ID_PRODUCTION);
		production.name = appContext.getString(R.string.server_production);
		production.address = "cloud.beeeon.com";
		production.port = 4565;
		production.certAssetsFilename = "cacert.crt";
		production.certVerifyUrl = "ant-2.fit.vutbr.cz";
		realm.copyToRealm(production);

		// seeding devel server
		Server devel = new Server(Server.SERVER_ID_DEVEL);
		devel.name = appContext.getString(R.string.server_development);
		devel.address = "ant-2.fit.vutbr.cz";
		devel.port = 4566;
		devel.certAssetsFilename = "cacert.crt";
		devel.certVerifyUrl = "ant-2.fit.vutbr.cz";
		realm.copyToRealm(devel);
	}
}
