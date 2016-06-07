package com.rehivetech.beeeon.model.entity;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.rehivetech.beeeon.BeeeOnApplication;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ClientError;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * @author mlyko
 * @since 30.05.2016
 */
public class Server extends RealmObject implements IIdentifier {
	public static final long SERVER_ID_PRODUCTION = 1;
	public static final long SERVER_ID_DEVEL = 2;
	public static final int DEFAULT_PORT = 4565;

	public static final Server PRODUCTION_SERVER = new Server(SERVER_ID_PRODUCTION, R.string.server_production, "cloud.beeeon.com", 4565, "ant-2.fit.vutbr.cz");

	@PrimaryKey
	private long id;
	public String name;
	public String address;
	public int port = DEFAULT_PORT;
	public String certVerifyUrl;
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
	 * Setups certificate from input stream, this might be from file.
	 * If input null, loads default certificate
	 *
	 * @param certificateStream input stream containing certificate
	 * @throws AppException
	 */
	public void setCertificate(@Nullable InputStream certificateStream) throws AppException {

		if (certificateStream == null) {
			loadDefaultCertificate();
			return;
		}

		Scanner s = new Scanner(certificateStream).useDelimiter("\\A");
		if (!s.hasNext()) {
			throw new AppException(ClientError.CERTIFICATE);
		}
		this.certificate = s.next();
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
	 * @param certVerifyUrl verifying url address
	 */
	public Server(long id, @StringRes int nameRes, String address, int port, String certVerifyUrl) {
		Context appContext = BeeeOnApplication.getContext();

		this.id = id;
		this.name = appContext.getString(nameRes);
		this.address = address;
		this.port = port;
		this.certVerifyUrl = certVerifyUrl;

		// load certificate from raw file
		loadDefaultCertificate();
	}

	/**
	 * Loads default certificate in raw files
	 */
	private void loadDefaultCertificate() {
		try {
			InputStream inputStream = new BufferedInputStream(BeeeOnApplication.getContext().getResources().openRawResource(R.raw.cacert));
			setCertificate(inputStream);
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Server(long id) {
		setId(id);
	}

	public void setId(long id) {
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
