package com.rehivetech.beeeon.network.server;

import android.content.Context;
import android.support.annotation.StringRes;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.INameIdentifier;
import com.rehivetech.beeeon.R;

/**
 * @author Robyer
 */
public enum NetworkServer implements IIdentifier {
	SERVER_BEEEON("beeeon", R.string.server_production, "cloud.beeeon.com", 4565),
	SERVER_ANT2_ALPHA("ant-2-alpha", R.string.server_development, "ant-2.fit.vutbr.cz", 4566);

	/** Name of default CA certificate located in assets */
	private static final String ASSETS_CA_CERT = "cacert.crt";

	/** Default CN value to be verified in server certificate */
	private static final String SERVER_CN_CERTIFICATE = "ant-2.fit.vutbr.cz";

	private final String mId;
	private final int mNameRes;
	public final String address;
	public final int port;
	public final String certAssetsFilename;
	public final String certVerifyUrl;

	NetworkServer(String id, @StringRes int nameRes, String address, int port) {
		this(id, nameRes, address, port, ASSETS_CA_CERT, SERVER_CN_CERTIFICATE);
	}

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
		return mId;
	}

	public static NetworkServer getDefaultServer() {
		return SERVER_BEEEON;
	}
}
