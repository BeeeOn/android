package com.rehivetech.beeeon.network.server;

import android.content.Context;
import android.support.annotation.StringRes;

import com.rehivetech.beeeon.BeeeOnApplication;
import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.R;

/**
 * @author Robyer
 */
public enum NetworkServer implements IIdentifier {
	SERVER_BEEEON("beeeon", R.string.server_production, "cloud.beeeon.com", 4565, "cacert.crt", "ant-2.fit.vutbr.cz"),
	SERVER_ANT2_ALPHA("ant-2-alpha", R.string.server_development, "ant-2.fit.vutbr.cz", 4566, "cacert.crt", "ant-2.fit.vutbr.cz");

	private final String mId;
	private final int mNameRes;
	public final String address;
	public final int port;
	public final String certAssetsFilename;
	public final String certVerifyUrl;

	NetworkServer(String id, @StringRes int nameRes, String address, int port, String certAssetsFilename, String certVerifyUrl) {
		mId = id;
		mNameRes = nameRes;
		this.address = address;
		this.port = port;
		this.certAssetsFilename = certAssetsFilename;
		this.certVerifyUrl = certVerifyUrl;
	}

	@Override
	public String getId() {
		return mId;
	}

	public String getTranslatedName(Context context) {
		String name = context.getString(mNameRes);
		if (this == getDefaultServer()) {
			return String.format("%s %s", name, context.getString(R.string.network_server_sufix_default));
		}
		return name;
	}

	@Override
	public String toString() {
		return getTranslatedName(BeeeOnApplication.getContext()); // TODO was mId here!
	}

	public static NetworkServer getDefaultServer() {
		return SERVER_BEEEON;
	}
}
