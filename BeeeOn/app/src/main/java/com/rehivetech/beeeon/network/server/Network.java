package com.rehivetech.beeeon.network.server;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ClientError;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.gcm.notification.VisibleNotification;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.gate.GateInfo;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.network.server.xml.XmlCreator;
import com.rehivetech.beeeon.network.server.xml.XmlParser;
import com.rehivetech.beeeon.network.server.xml.XmlParser.State;
import com.rehivetech.beeeon.util.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

/**
 * Network class that handles communication with server.
 *
 * @author ThinkDeep
 * @author Robyer
 */
public class Network implements INetwork {

	private static final String TAG = Network.class.getSimpleName();

	/** Number of retries when we receive no response from server (e.g. because persistent connection expires from server side) */
	private static final int RETRIES_COUNT = 2;

	/** Alias (tag) for CA certificate */
	private static final String ALIAS_CA_CERT = "ca";

	/** Marks end of communication messages */
	private static final String EOF = "</com>";

	private static final int SSL_TIMEOUT = 35000;

	private final Context mContext;
	private final NetworkServer mServer;
	private String mSessionId = "";

	private final Object mSocketLock = new Object();
	private SSLSocket mSocket = null;
	private PrintWriter mSocketWriter = null;
	private BufferedReader mSocketReader = null;

	public Network(Context context, NetworkServer server) {
		mContext = context;
		mServer = server;
	}

	/**
	 * Method for communicating with server.
	 *
	 * @param request is message to send
	 * @return response from server
	 * @throws AppException with error ClientError.UNKNOWN_HOST, ClientError.CERTIFICATE or ClientError.SOCKET
	 */
	private String startCommunication(String request) throws AppException {
		// Init socket objects if not exists
		synchronized (mSocketLock) {
			if (mSocket == null || mSocketReader == null || mSocketWriter == null) {
				mSocket = initSocket();

				try {
					// At this point SSLSocket performed certificate verification and
					// we have performed hostName verification, so it is safe to proceed.
					mSocketWriter = new PrintWriter(mSocket.getOutputStream());
					mSocketReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
				} catch (IOException e) {
					closeCommunicationSocket();
					throw AppException.wrap(e, ClientError.SOCKET);
				}
			}
		}

		// Send request
		mSocketWriter.write(request, 0, request.length());
		mSocketWriter.flush();

		// Receive response
		StringBuilder response = new StringBuilder();
		try {
			String actRecieved;
			while ((actRecieved = mSocketReader.readLine()) != null) {
				response.append(actRecieved);
				if (actRecieved.endsWith(EOF))
					break;
			}
		} catch (IOException e) {
			throw AppException.wrap(e, ClientError.SOCKET);
		}

		// Return server response
		return response.toString();
	}

	/**
	 * Method for initializing socket for sending data to server via TLS protocol using own TrustManger to be able to trust self-signed
	 * certificates. CA certificated must be located in assets folder.
	 *
	 * @return Initialized socket or throws exception
	 * @throws AppException with error ClientError.UNKNOWN_HOST, ClientError.CERTIFICATE or ClientError.SOCKET
	 */
	private SSLSocket initSocket() {
		try {
			// Open CA certificate from assets
			InputStream inStreamCertTmp = mContext.getAssets().open(mServer.certAssetsFilename);
			InputStream inStreamCert = new BufferedInputStream(inStreamCertTmp);
			Certificate ca;
			try {
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				ca = cf.generateCertificate(inStreamCert);
			} finally {
				inStreamCert.close();
			}
			// Create a KeyStore containing our trusted CAs
			String keyStoreType = KeyStore.getDefaultType();
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(null, null);
			keyStore.setCertificateEntry(ALIAS_CA_CERT, ca);

			// Create a TrustManager that trusts the CAs in our KeyStore
			String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
			tmf.init(keyStore);

			// Create an SSLContext that uses our TrustManager
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tmf.getTrustManagers(), null);

			// Open SSLSocket directly to server
			SSLSocket socket = (SSLSocket) sslContext.getSocketFactory().createSocket(mServer.address, mServer.port);

			HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
			//socket.setKeepAlive(true);
			socket.setSoTimeout(SSL_TIMEOUT);
			SSLSession s = socket.getSession();
			if (!s.isValid())
				Log.e(TAG, "Socket is not valid! TLS handshake failed.");

			// Verify that the certificate hostName
			// This is due to lack of SNI support in the current SSLSocket.
			if (!hv.verify(mServer.certVerifyUrl, s)) {
				throw new AppException("Certificate is not verified!", ClientError.CERTIFICATE)
						.set("Expected CN", mServer.certVerifyUrl)
						.set("Found CN", s.getPeerPrincipal());
			}
			return socket;
		} catch (UnknownHostException e) {
			// UnknownHostException - Server address or hostName wasn't not found
			throw AppException.wrap(e, ClientError.UNKNOWN_HOST);
		} catch (ConnectException e) {
			// ConnectException - Connection refused, timeout, etc.
			throw AppException.wrap(e, ClientError.SERVER_CONNECTION);
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException e) {
			// IOException - Can't read CA certificate from assets or can't create new socket
			// CertificateException - Unknown certificate format (default X.509), can't generate CA certificate (it shouldn't occur)
			// KeyStoreException - Bad type of KeyStore, can't set CA certificate to KeyStore
			// NoSuchAlgorithmException - Unknown SSL/TLS protocol or unknown TrustManager algorithm (it shouldn't occur)
			// KeyManagementException - general exception, thrown to indicate an exception during processing an operation concerning key management
			throw AppException.wrap(e, ClientError.SOCKET);
		}
	}

	/**
	 * Closes actual connection (opened socket).
	 */
	public void interruptConnection() {
		Log.w(TAG, "Interrupting connection.");
		closeCommunicationSocket();
	}

	/**
	 * Method close socket, writer and reader
	 */
	private void closeCommunicationSocket() {
		synchronized (mSocketLock) {
			// Securely close socket
			if (mSocket != null) {
				try {
					mSocket.close();
				} catch (IOException e) {
					e.printStackTrace(); // Nothing we can do here
				} finally {
					mSocket = null;
				}
			}

			// Close writer
			if (mSocketWriter != null) {
				mSocketWriter.close();
				mSocketWriter = null;
			}

			// Securely close reader
			if (mSocketReader != null) {
				try {
					mSocketReader.close();
				} catch (IOException e) {
					e.printStackTrace(); // Nothing we can do here
				} finally {
					mSocketReader = null;
				}
			}
		}
	}

	@Override
	public boolean isAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/**
	 * Just calls {@link #doRequest(String, boolean, int)} with retries = RETRIES_COUNT
	 *
	 * @see {#doRequest}
	 */
	private synchronized String doRequest(String messageToSend, boolean checkBT) throws AppException {
		return doRequest(messageToSend, checkBT, RETRIES_COUNT);
	}

	/**
	 * Send request to server and return parsedMessage or throw exception on error.
	 *
	 * @param messageToSend message in xml
	 * @param checkBT       - when true and sessionId is not present in Network, then throws AppException with NetworkError.BAD_BT
	 *                      - this logically must be false for requests like register or login, which doesn't require sessionId for working
	 * @param retries       - number of retries to do the request and receive response
	 * @return String Result from server
	 * @throws AppException with error ClientError.INTERNET_CONNECTION, NetworkError.BAD_BT, ClientError.XML,
	 *                      ClientError.UNKNOWN_HOST, ClientError.CERTIFICATE, ClientError.SOCKET or ClientError.NO_RESPONSE
	 */
	private synchronized String doRequest(String messageToSend, boolean checkBT, int retries) throws AppException {
		// Check internet connection
		if (!isAvailable())
			throw new AppException(ClientError.INTERNET_CONNECTION);

		// Check existence of sessionId
		if (checkBT && !hasSessionId())
			throw new AppException(NetworkError.BAD_BT);

		Log.i(TAG + " fromApp >>", messageToSend);
		String result = startCommunication(messageToSend);
		Log.i(TAG + " << fromSrv", result.isEmpty() ? "- no response -" : result);

		// Check if we received no response and try it again eventually
		if (result.isEmpty()) {
			if (retries <= 0) {
				// We can't try again anymore, just throw error
				throw new AppException("No response from server.", ClientError.NO_RESPONSE);
			}

			// Probably connection is lost so we need to reinit socket at next call of doRequest
			closeCommunicationSocket();

			// Try to do this request again (with decremented retries)
			Log.d(TAG, String.format("Try to repeat request (retries remaining: %d)", retries - 1));
			return doRequest(messageToSend, checkBT, retries - 1);
		}

		return result;
	}

	/**
	 * Just call's {@link #processCommunication(String, State, boolean)} with checkBT = true
	 *
	 * @see {#processCommunication}
	 */
	private synchronized XmlParser processCommunication(String request, State expectedState) throws AppException {
		return processCommunication(request, expectedState, true);
	}

	private XmlParser processCommunication(String request, State expectedState, boolean checkBT) throws AppException {
		String response = doRequest(request, checkBT);
		XmlParser parser = XmlParser.parse(response);

		// Check communication protocol version
		String version = parser.getVersion();
		if (version.isEmpty()) {
			throw new AppException("Get no protocol version from response.", ClientError.XML);
		} else if (!version.equals(Constants.PROTOCOL_VERSION)) {
			String srv[] = version.split("\\.");
			String app[] = Constants.PROTOCOL_VERSION.split("\\.");

			if (srv.length >= 2 && app.length >= 2) {
				try {
					int srv_major = Integer.parseInt(srv[0]);
					int srv_minor = Integer.parseInt(srv[1]);
					int app_major = Integer.parseInt(app[0]);
					int app_minor = Integer.parseInt(app[1]);

					if (srv_major != app_major || srv_minor < app_minor) {
						// Server must have same major version as app and same or greater minor version than app
						throw new AppException(NetworkError.COM_VER_MISMATCH)
								.set(NetworkError.PARAM_COM_VER_LOCAL, Constants.PROTOCOL_VERSION)
								.set(NetworkError.PARAM_COM_VER_SERVER, version);
					}
				} catch (NumberFormatException e) {
					throw new AppException("Get invalid protocol version from response.", ClientError.XML);
				}
			} else {
				throw new AppException("Get invalid protocol version from response.", ClientError.XML);
			}
		}

		// Check expected state
		if (parser.getState() != expectedState) {
			if (parser.getState() != State.FALSE)
				throw new AppException("ParsedMessage is not State.FALSE", ClientError.UNEXPECTED_RESPONSE)
						.set("ExpectedState", expectedState)
						.set("State", parser.getState());

			// Delete COM_SESSION_ID when we receive error saying that it is invalid
			if (parser.getErrorCode() == NetworkError.BAD_BT.getNumber())
				mSessionId = "";

			// Throw AppException
			throw new AppException(Utils.getEnumFromId(NetworkError.class, String.valueOf(parser.getErrorCode()), NetworkError.UNKNOWN));
		}

		return parser;
	}

	@Override
	public String getSessionId() {
		return mSessionId;
	}

	@Override
	public void setSessionId(String token) {
		mSessionId = token;
	}

	@Override
	public boolean hasSessionId() {
		return !mSessionId.isEmpty();
	}


	/**************************************************************************
	 * ACCOUNTS
	 */

	@Override
	public boolean accounts_login(IAuthProvider authProvider) {
		// Check existence of authProvider parameters
		Map<String, String> parameters = authProvider.getParameters();
		if (parameters == null || parameters.isEmpty())
			throw new IllegalArgumentException(String.format("IAuthProvider '%s' provided no parameters.", authProvider.getProviderName()));

		XmlParser parser = processCommunication(
				XmlCreator.Accounts.login(Utils.getPhoneName(), authProvider),
				State.SESSION_ID,
				false);

		mSessionId = parser.parseSessionId();
		return true;
	}

	@Override
	public boolean accounts_register(IAuthProvider authProvider) {
		// Check existence of authProvider parameters
		Map<String, String> parameters = authProvider.getParameters();
		if (parameters == null || parameters.isEmpty())
			throw new IllegalArgumentException(String.format("IAuthProvider '%s' provided no parameters.", authProvider.getProviderName()));

		processCommunication(
				XmlCreator.Accounts.register(authProvider),
				State.TRUE,
				false);

		return true;
	}

	@Override
	public boolean accounts_logout() {
		processCommunication(
				XmlCreator.Accounts.logout(mSessionId),
				State.TRUE);

		return true;
	}

	@Override
	public User accounts_getMyProfile() {
		XmlParser parser = processCommunication(
				XmlCreator.Accounts.getMyProfile(mSessionId),
				State.USERINFO);

		return parser.parseUserInfo();
	}

	@Override
	public boolean accounts_connectAuthProvider(IAuthProvider authProvider) {
		processCommunication(
				XmlCreator.Accounts.connectAuthProvider(mSessionId, authProvider),
				State.TRUE);

		return true;
	}

	@Override
	public boolean accounts_disconnectAuthProvider(String providerName) {
		processCommunication(
				XmlCreator.Accounts.disconnectAuthProvider(mSessionId, providerName),
				State.TRUE);

		return true;
	}


	/**************************************************************************
	 * DEVICES
	 */

	@Override
	public List<Device> devices_getAll(String gateId) {
		XmlParser parser = processCommunication(
				XmlCreator.Devices.getAll(mSessionId, gateId),
				State.ALLDEVICES);

		return parser.parseDevices();
	}

	@Override
	public List<Device> devices_getNew(String gateId) {
		XmlParser parser = processCommunication(
				XmlCreator.Devices.getNew(mSessionId, gateId),
				State.DEVICES);

		return parser.parseDevices();
	}

	@Override
	public List<Device> devices_get(List<Device> devices) {
		XmlParser parser = processCommunication(
				XmlCreator.Devices.get(mSessionId, devices),
				State.DEVICES);

		return parser.parseDevices();
	}

	@Override
	public Device devices_get(Device device) {
		List<Device> devices = devices_get(Collections.singletonList(device));
		return devices.isEmpty() ? null : devices.get(0);
	}

	@Override
	// FIXME: Use ModuleId instead
	public ModuleLog devices_getLog(String gateId, Module module, ModuleLog.DataPair pair) {
		String request = XmlCreator.Devices.getLog(
				mSessionId,
				gateId,
				module.getDevice().getAddress(),
				module.getId(),
				String.valueOf(pair.interval.getStartMillis() / 1000),
				String.valueOf(pair.interval.getEndMillis() / 1000),
				pair.type.getId(),
				pair.gap.getSeconds());

		XmlParser parser = processCommunication(
				request,
				State.LOGDATA);

		ModuleLog result = parser.parseLogData();
		result.setDataInterval(pair.gap);
		result.setDataType(pair.type);
		return result;
	}

	@Override
	public boolean devices_update(String gateId, List<Device> devices) {
		processCommunication(
				XmlCreator.Devices.update(mSessionId, gateId, devices),
				State.TRUE);

		return true;
	}

	@Override
	public boolean devices_update(String gateId, Device device) {
		ArrayList<Device> list = new ArrayList<>();
		list.add(device);

		return devices_update(gateId, list);
	}

	@Override
	public boolean devices_setState(String gateId, Module module) {
		processCommunication(
				XmlCreator.Devices.setState(mSessionId, gateId, module),
				State.TRUE);

		return true;
	}

	@Override
	public boolean devices_unregister(Device device) {
		processCommunication(
				XmlCreator.Devices.unregister(mSessionId, device),
				State.TRUE);

		return true;
	}


	/**************************************************************************
	 * GATES
	 */

	@Override
	public List<Gate> gates_getAll() {
		XmlParser parser = processCommunication(
				XmlCreator.Gates.getAll(mSessionId),
				State.GATES);

		return parser.parseGates();
	}

	@Override
	public GateInfo gates_get(String gateId) {
		XmlParser parser = processCommunication(
				XmlCreator.Gates.get(mSessionId, gateId),
				State.GATEINFO);

		return parser.parseGateInfo();
	}

	@Override
	public boolean gates_register(String gateId, String gateName, int offsetInMinutes) {
		processCommunication(
				XmlCreator.Gates.register(mSessionId, gateId, gateName, offsetInMinutes),
				State.TRUE);

		return true;
	}

	@Override
	public boolean gates_unregister(String gateId) {
		processCommunication(
				XmlCreator.Gates.unregister(mSessionId, gateId),
				State.TRUE);

		return true;
	}

	@Override
	public boolean gates_startListen(String gateId) {
		processCommunication(
				XmlCreator.Gates.startListen(mSessionId, gateId),
				State.TRUE);

		return true;
	}

	@Override
	public boolean gates_update(Gate gate) {
		processCommunication(
				XmlCreator.Gates.update(mSessionId, gate),
				State.TRUE);

		return true;
	}


	/**************************************************************************
	 * GATEUSERS
	 */

	@Override
	public List<User> gateusers_getAll(String gateId) {
		XmlParser parser = processCommunication(
				XmlCreator.GateUsers.getAll(mSessionId, gateId),
				State.ACCOUNTS);

		List<User> users = parser.parseGateUsers();
		for (User user : users) {
			if (!user.getPictureUrl().isEmpty()) {
				user.setPicture(Utils.fetchImageFromUrl(user.getPictureUrl()));
			}
		}

		return users;
	}

	@Override
	public boolean gateusers_invite(String gateId, ArrayList<User> users) {
		processCommunication(
				XmlCreator.GateUsers.invite(mSessionId, gateId, users),
				State.TRUE);

		return true;
	}

	@Override
	public boolean gateusers_invite(String gateId, User user) {
		ArrayList<User> list = new ArrayList<>();
		list.add(user);

		return gateusers_invite(gateId, list);
	}

	@Override
	public boolean gateusers_remove(String gateId, List<User> users) {
		processCommunication(
				XmlCreator.GateUsers.remove(mSessionId, gateId, users),
				State.TRUE);

		return true;
	}

	@Override
	public boolean gateusers_remove(String gateId, User user) {
		ArrayList<User> list = new ArrayList<>();
		list.add(user);

		return gateusers_remove(gateId, list);
	}

	@Override
	public boolean gateusers_updateAccess(String gateId, ArrayList<User> users) {
		processCommunication(
				XmlCreator.GateUsers.updateAccess(mSessionId, gateId, users),
				State.TRUE);

		return true;
	}

	@Override
	public boolean gateusers_updateAccess(String gateId, User user) {
		ArrayList<User> list = new ArrayList<>();
		list.add(user);

		return gateusers_updateAccess(gateId, list);
	}


	/**************************************************************************
	 * LOCATIONS
	 */

	@Override
	public Location locations_create(Location location) {
		XmlParser parser = processCommunication(
				XmlCreator.Locations.create(mSessionId, location),
				State.LOCATIONID);

		location.setId(parser.parseNewLocationId());
		return location;
	}

	@Override
	public boolean locations_update(Location location) {
		processCommunication(
				XmlCreator.Locations.update(mSessionId, location),
				State.TRUE);

		return true;
	}

	@Override
	public boolean locations_delete(Location location) {
		processCommunication(
				XmlCreator.Locations.delete(mSessionId, location),
				State.TRUE);

		return true;
	}

	@Override
	public List<Location> locations_getAll(String gateId) {
		XmlParser parser = processCommunication(
				XmlCreator.Locations.getAll(mSessionId, gateId),
				State.LOCATIONS);

		return parser.parseLocations();
	}


	/**************************************************************************
	 * NOTIFICATIONS
	 */

	@Override
	public List<VisibleNotification> notifications_getLatest() {
		XmlParser parser = processCommunication(
				XmlCreator.Notifications.getLatest(mSessionId),
				State.NOTIFICATIONS);

		return parser.parseNotifications();
	}

	@Override
	public boolean notifications_read(ArrayList<String> notificationIds) {
		processCommunication(
				XmlCreator.Notifications.read(mSessionId, notificationIds),
				State.TRUE);

		return true;
	}


	/**************************************************************************
	 * GCM - TODO: Rename to notifications and methods to use some NotificationProvider classes
	 */

	/**
	 * Method delete old gcmid to avoid fake notifications
	 *
	 * @param userId of old/last user of gcmid (app+module id)
	 * @param gcmID  - google cloud message id
	 * @return true if id has been deleted, false otherwise
	 */
	public boolean deleteGCMID(String userId, String gcmID) {
		processCommunication(
				XmlCreator.Notifications.deleteGCMID(userId, gcmID),
				State.TRUE);

		return true;
	}

	/**
	 * Method set gcmID to server
	 *
	 * @param gcmID to be set
	 * @return true if id has been updated, false otherwise
	 */
	public boolean setGCMID(String gcmID) {
		processCommunication(
				XmlCreator.Notifications.setGCMID(mSessionId, gcmID),
				State.TRUE);

		return true;
	}

}
