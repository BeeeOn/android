package com.rehivetech.beeeon.model.entity;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Log;

import com.rehivetech.beeeon.BeeeOnApplication;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.Utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * @author mlyko
 * @since 30.05.2016
 */
public class Server extends RealmObject implements IIdentifier {
	private static final String TAG = Server.class.getSimpleName();

	public static final long SERVER_ID_PRODUCTION = 1;
	public static final long SERVER_ID_DEVEL = 2;
	public static final int DEFAULT_PORT = 4565;
	public static final String DEFAULT_VERIFY = "ant-2.fit.vutbr.cz";

	// default production server
	public static final Server PRODUCTION_SERVER = new Server(SERVER_ID_PRODUCTION, R.string.server_production, "cloud.beeeon.com", DEFAULT_PORT, DEFAULT_VERIFY);

	@PrimaryKey
	private long id;
	public String name;
	public String address;
	public int port = DEFAULT_PORT;
	public String verifyHostname;
	private String certificate;

	/**
	 * Getting certificate stream for network usage
	 * Although it's stream, does not have to be closed (because it's String stream)
	 *
	 * @return input stream containing certificate data
	 */
	@Nullable
	public ByteArrayInputStream getCertificate() {
		if (certificate == null) return null;
		return new ByteArrayInputStream(certificate.getBytes());
	}

	/**
	 * Set certificate from string (this hould be loaded from file, etc.)
	 * If certificate nullable passed, loads default one
	 */
	public void setCertificate(@Nullable String certificate) {
		if (certificate == null) {
			certificate = loadDefaultCertificate(BeeeOnApplication.getContext());
		}

		this.certificate = certificate;
	}

	/**
	 * Constructor for Realm
	 */
	public Server() {
	}

	/**
	 * Constructor for default servers
	 *
	 * @param id            server id, might be {@link #SERVER_ID_PRODUCTION} or {@link #SERVER_ID_DEVEL}
	 * @param nameRes       resource for name
	 * @param address       server host address
	 * @param port          port
	 * @param verifyHostname verifying url address
	 */
	public Server(long id, @StringRes int nameRes, String address, int port, String verifyHostname) {
		Context appContext = BeeeOnApplication.getContext();

		this.id = id;
		this.name = appContext.getString(nameRes);
		this.address = address;
		this.port = port;
		this.verifyHostname = verifyHostname;
		this.certificate = loadDefaultCertificate(appContext);
	}

	/**
	 * Loads default certificate in raw files
	 */
	private String loadDefaultCertificate(Context context) {
		try {
			InputStream inputStream = new BufferedInputStream(context.getResources().openRawResource(R.raw.cacert));
			String certificate = Utils.convertInputStreamToString(inputStream);
			inputStream.close();
			return certificate;
		} catch (IOException e) {
			Log.e(TAG, "Problem with loading default server certificate!");
			return null;
		}
	}

	public Server(long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public boolean isEditable() {
		return Server.isEditable(getId());
	}

	public static boolean isEditable(long id) {
		return !(id == SERVER_ID_PRODUCTION || id == SERVER_ID_DEVEL);
	}

	@Override
	public String toString() {
		if (this.id == SERVER_ID_PRODUCTION) {
			return String.format("%s %s", name, BeeeOnApplication.getContext().getString(R.string.network_server_sufix_default));
		}
		return name;
	}

	/*	 * Safely returns server by its id or default server if no server found
	 *
	 * @param serverId id which will be searched for
	 * @return server instance

	 */
	public static Server getServerSafeById(long serverId) {
		Server server;
		Realm realm = Realm.getDefaultInstance();
		Server serverInDb = realm.where(Server.class).equalTo("id", serverId).findFirst();
		if (serverInDb != null) {
			server = realm.copyFromRealm(serverInDb);
		} else {
			server = Server.PRODUCTION_SERVER;
		}
		realm.close();

		return server;
	}

}
