package com.rehivetech.beeeon.model.entity;

import com.rehivetech.beeeon.BeeeOnApplication;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.Utils;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * @author mlyko
 * @since 30.05.2016
 */
@RealmClass
public class Server implements RealmModel {
	public static final long SERVER_ID_PRODUCTION = 1;
	public static final long SERVER_ID_DEVEL = 2;

	@PrimaryKey
	public long id;
	public String name;
	public String address;
	public int port;
	public String certAssetsFilename;
	public String certVerifyUrl;
	private boolean mIsDeletable = false;

	public Server() {
	}

	public Server(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDeletable() {
		return mIsDeletable;
	}

	@Override
	public String toString() {
		if (this.id == SERVER_ID_PRODUCTION) {
			return String.format("%s %s", name, BeeeOnApplication.getContext().getString(R.string.network_server_sufix_default));
		}
		return name;
	}
}
