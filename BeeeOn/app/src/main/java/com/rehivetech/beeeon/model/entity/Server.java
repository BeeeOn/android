package com.rehivetech.beeeon.model.entity;

import com.rehivetech.beeeon.BeeeOnApplication;
import com.rehivetech.beeeon.R;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * @author mlyko
 * @since 30.05.2016
 */
public class Server extends RealmObject {
	public static final long SERVER_ID_PRODUCTION = 1;
	public static final long SERVER_ID_DEVEL = 2;
	public static final int DEFAULT_PORT = 4565;

	public static final Server PRODUCTION_SERVER = new Server(SERVER_ID_PRODUCTION, BeeeOnApplication.getContext().getString(R.string.server_production), "cloud.beeeon.com", 4565, "cacert.crt", "ant-2.fit.vutbr.cz");

	@PrimaryKey
	private long id;
	public String name;
	public String address;
	public int port;
	public String certAssetsFilename;
	public String certVerifyUrl;

	public Server() {
	}

	public Server(long id, String name, String address, int port, String certAssetsFilename, String certVerifyUrl) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.port = port;
		this.certAssetsFilename = certAssetsFilename;
		this.certVerifyUrl = certVerifyUrl;
	}

	public Server(long id) {
		setId(id);
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public boolean isDeletable() {
		return Server.isDeletable(getId());
	}

	public static boolean isDeletable(long id) {
		return !(id == SERVER_ID_PRODUCTION || id == SERVER_ID_DEVEL);
	}

	@Override
	public String toString() {
		if (this.id == SERVER_ID_PRODUCTION) {
			return String.format("%s %s", name, BeeeOnApplication.getContext().getString(R.string.network_server_sufix_default));
		}
		return name;
	}


}
