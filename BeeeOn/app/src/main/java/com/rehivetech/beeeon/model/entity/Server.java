package com.rehivetech.beeeon.model.entity;

import com.rehivetech.beeeon.BeeeOnApplication;
import com.rehivetech.beeeon.R;

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
	public static final int DEFAULT_PORT = 4565;

	@PrimaryKey
	private long id;
	public String name;
	public String address;
	public int port;
	public String certAssetsFilename;
	public String certVerifyUrl;
	private boolean mIsDeletable = true;

	public Server() {
	}

	public Server(long id) {
		setId(id);
	}

	public void setId(long id) {
		if (id == SERVER_ID_PRODUCTION || id == SERVER_ID_DEVEL) {
			mIsDeletable = false;
		}
		this.id = id;
	}

	public long getId() {
		return id;
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
