package com.rehivetech.beeeon.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.network.xml.XmlCreator;
import com.rehivetech.beeeon.network.xml.XmlParsers;
import com.rehivetech.beeeon.network.xml.XmlParsers.State;
import com.rehivetech.beeeon.util.Log;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

/**
 * Network service that handles communication with server.
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
	 * Just call's {@link #doRequest(String, boolean, int)} with retrues = RETRIES_COUNT
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
	private synchronized XmlParsers processCommunication(String request, State expectedState) throws AppException {
		return processCommunication(request, expectedState, true);
	}

	private XmlParsers processCommunication(String request, State expectedState, boolean checkBT) throws AppException {
		String response = doRequest(request, checkBT);
		XmlParsers parser = XmlParsers.parse(response);

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
			// FIXME: in specification there is also content of false - which is plaintext inside the <com> tags </com>

			// Delete COM_SESSION_ID when we receive error saying that it is invalid
			if (parser.getErrorCode() == NetworkError.BAD_BT.getNumber())
				mSessionId = "";

			// Throw AppException
			throw new AppException(Utils.getEnumFromId(NetworkError.class, String.valueOf(parser.getErrorCode()), NetworkError.UNKNOWN));
		}

		return parser;
	}

	// /////////////////////////////////////SIGNIN,SIGNUP,GATES//////////////////////

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

	@Override
	public boolean login(IAuthProvider authProvider) {
		// Check existence of authProvider parameters
		Map<String, String> parameters = authProvider.getParameters();
		if (parameters == null || parameters.isEmpty())
			throw new IllegalArgumentException(String.format("IAuthProvider '%s' provided no parameters.", authProvider.getProviderName()));

		XmlParsers parser = processCommunication(
				XmlCreator.createLogin(Utils.getPhoneName(), authProvider),
				State.SESSION_ID,
				false);

		mSessionId = parser.parseSessionId();
		return true;
	}

	@Override
	public boolean register(IAuthProvider authProvider) {
		// Check existence of authProvider parameters
		Map<String, String> parameters = authProvider.getParameters();
		if (parameters == null || parameters.isEmpty())
			throw new IllegalArgumentException(String.format("IAuthProvider '%s' provided no parameters.", authProvider.getProviderName()));

		processCommunication(
				XmlCreator.createRegister(authProvider),
				State.TRUE,
				false);

		return true;
	}

	@Override
	public boolean logout() {
		processCommunication(
				XmlCreator.createLogout(mSessionId),
				State.TRUE);

		return true;
	}

	@Override
	public boolean addProvider(IAuthProvider authProvider) {
		processCommunication(
				XmlCreator.createJoinAccount(mSessionId, authProvider),
				State.TRUE);

		return true;
	}

	@Override
	public boolean removeProvider(String providerName) {
		processCommunication(
				XmlCreator.createCutAccount(mSessionId, providerName),
				State.TRUE);

		return true;
	}

	@Override
	public boolean deleteMyAccount() {
		processCommunication(
				XmlCreator.createCutAccount(mSessionId, "all"),
				State.TRUE);

		return true;
	}

	@Override
	public User loadUserInfo() {
		XmlParsers parser = processCommunication(
				XmlCreator.createGetUserInfo(mSessionId),
				State.USERINFO);

		return parser.parseUserInfo();
	}

	@Override
	public boolean addGate(String gateId, String gateName) {
		processCommunication(
				XmlCreator.createAddGate(mSessionId, gateId, gateName),
				State.TRUE);

		return true;
	}

	@Override
	public List<Gate> getGates() {
		XmlParsers parser = processCommunication(
				XmlCreator.createGetGates(mSessionId),
				State.GATES);

		return parser.parseGates();
	}

	@Override
	public GateInfo getGateInfo(String gateId) {
		XmlParsers parser = processCommunication(
				XmlCreator.createGetGateInfo(mSessionId, gateId),
				State.GATEINFO);

		return parser.parseGateInfo();
	}

	@Override
	public List<Device> initGate(String gateId) {
		XmlParsers parser = processCommunication(
				XmlCreator.createGetAllDevices(mSessionId, gateId),
				State.ALLDEVICES);

		return parser.parseAllDevices();
	}

	@Override
	public boolean updateGate(Gate gate) {
		processCommunication(
				XmlCreator.createUpdateGate(mSessionId, gate),
				State.TRUE);

		return true;
	}

	@Override
	public boolean deleteGate(String gateId){
		processCommunication(
				XmlCreator.createDelGate(mSessionId, gateId),
				State.TRUE);

		return true;
	}

	// /////////////////////////////////////DEVICES,LOGS////////////////////////////////

	@Override
	public boolean updateDevices(String gateId, List<Device> devices) {
		processCommunication(
				XmlCreator.createUpdateDevice(mSessionId, gateId, devices),
				State.TRUE);

		return true;
	}

	@Override
	public boolean switchState(String gateId, Module module) {
		processCommunication(
				XmlCreator.createSwitchState(mSessionId, gateId, module),
				State.TRUE);

		return true;
	}

	@Override
	public boolean prepareGateToListenNewDevices(String gateId) {
		processCommunication(
				XmlCreator.createGateScanMode(mSessionId, gateId),
				State.TRUE);

		return true;
	}

	@Override
	public boolean deleteDevice(Device device) {
		processCommunication(
				XmlCreator.createDeleteDevice(mSessionId, device),
				State.TRUE);

		return true;
	}

	@Override
	public List<Device> getDevices(List<Device> devices) {
		XmlParsers parser = processCommunication(
				XmlCreator.createGetDevices(mSessionId, devices),
				State.DEVICES);

		return parser.parseDevices();
	}

	@Override
	public Device getDevice(Device device) {
		List<Device> devices = getDevices(Arrays.asList(device));
		return devices.isEmpty() ? null : devices.get(0);
	}

	@Override
	public boolean updateDevice(String gateId, Device device) {
		ArrayList<Device> list = new ArrayList<>();
		list.add(device);

		return updateDevices(gateId, list);
	}

	@Override
	public List<Device> getNewDevices(String gateId) {
		XmlParsers parser = processCommunication(
				XmlCreator.createGetNewDevices(mSessionId, gateId),
				State.DEVICES);

		return parser.parseNewDevices();
	}

	@Override
	// FIXME: Use ModuleId instead
	public ModuleLog getLog(String gateId, Module module, ModuleLog.DataPair pair) {
		String request = XmlCreator.createGetLog(
				mSessionId,
				gateId,
				module.getDevice().getAddress(),
				module.getId(),
				String.valueOf(pair.interval.getStartMillis() / 1000),
				String.valueOf(pair.interval.getEndMillis() / 1000),
				pair.type.getId(),
				pair.gap.getSeconds());

		XmlParsers parser = processCommunication(
				request,
				State.LOGDATA);

		ModuleLog result = parser.parseLogData();
		result.setDataInterval(pair.gap);
		result.setDataType(pair.type);
		return result;
	}

	// /////////////////////////////////////LOCATIONS///////////////////////////////////

	@Override
	public List<Location> getLocations(String gateId) {
		XmlParsers parser = processCommunication(
				XmlCreator.createGetLocations(mSessionId, gateId),
				State.LOCATIONS);

		return parser.parseLocations();
	}

	@Override
	public boolean updateLocation(Location location) {
		processCommunication(
				XmlCreator.createUpdateLocation(mSessionId, location),
				State.TRUE);

		return true;
	}

	@Override
	public boolean deleteLocation(Location location) {
		processCommunication(
				XmlCreator.createDeleteLocation(mSessionId, location),
				State.TRUE);

		return true;
	}

	@Override
	public Location createLocation(Location location) {
		XmlParsers parser = processCommunication(
				XmlCreator.createAddLocation(mSessionId, location),
				State.LOCATIONID);

		location.setId((String) parser.parseNewLocationId());
		return location;
	}

	// /////////////////////////////////////PROVIDERS////////////////////////////////////

	@Override
	public boolean addAccounts(String gateId, ArrayList<User> users) {
		processCommunication(
				XmlCreator.createInviteGateUser(mSessionId, gateId, users),
				State.TRUE);

		return true;
	}

	@Override
	public boolean addAccount(String gateId, User user) {
		ArrayList<User> list = new ArrayList<>();
		list.add(user);

		return addAccounts(gateId, list);
	}

	@Override
	public boolean deleteAccounts(String gateId, List<User> users) {
		processCommunication(
				XmlCreator.createDeleteGateUser(mSessionId, gateId, users),
				State.TRUE);

		return true;
	}

	@Override
	public boolean deleteAccount(String gateId, User user) {
		ArrayList<User> list = new ArrayList<>();
		list.add(user);

		return deleteAccounts(gateId, list);
	}

	@Override
	public List<User> getAccounts(String gateId) {
		XmlParsers parser = processCommunication(
				XmlCreator.createGetGateUsers(mSessionId, gateId),
				State.ACCOUNTS);

		return parser.parseGateUsers();
	}

	@Override
	public boolean updateAccounts(String gateId, ArrayList<User> users) {
		processCommunication(
				XmlCreator.createUpdateGateUser(mSessionId, gateId, users),
				State.TRUE);

		return true;
	}

	@Override
	public boolean updateAccount(String gateId, User user) {
		ArrayList<User> list = new ArrayList<>();
		list.add(user);

		return updateAccounts(gateId, list);
	}

	// /////////////////////////////////////NOTIFICATIONS///////////////////////////////

	/**
	 * Method delete old gcmid to avoid fake notifications
	 *
	 * @param userId of old/last user of gcmid (app+module id)
	 * @param gcmID  - google cloud message id
	 * @return true if id has been deleted, false otherwise
	 */
	public boolean deleteGCMID(String userId, String gcmID) {
		processCommunication(
				XmlCreator.createDelGcmid(userId, gcmID),
				State.TRUE);

		return true;
	}

	@Override
	public boolean NotificationsRead(ArrayList<String> msgID) {
		processCommunication(
				XmlCreator.createNotificaionRead(mSessionId, msgID),
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
				XmlCreator.createSetGCMID(mSessionId, gcmID),
				State.TRUE);

		return true;
	}

	@Override
	public List<VisibleNotification> getNotifications() {
		XmlParsers parser = processCommunication(
				XmlCreator.createGetNotifications(mSessionId),
				State.NOTIFICATIONS);

		return parser.parseNotifications();
	}

}
