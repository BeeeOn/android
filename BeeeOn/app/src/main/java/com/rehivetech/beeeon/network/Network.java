package com.rehivetech.beeeon.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ClientError;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.gcm.notification.VisibleNotification;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.Module.SaveModule;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.household.watchdog.Watchdog;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.network.xml.FalseAnswer;
import com.rehivetech.beeeon.network.xml.ParsedMessage;
import com.rehivetech.beeeon.network.xml.XmlCreator;
import com.rehivetech.beeeon.network.xml.XmlParsers;
import com.rehivetech.beeeon.network.xml.XmlParsers.State;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;

import org.xmlpull.v1.XmlPullParserException;

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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
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
	private String mBT = "";

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
			String actRecieved = null;
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
	 * Just call's {@link #doRequest(String, boolean)} with checkBT = true
	 *
	 * @see {#doRequest}
	 */
	private synchronized ParsedMessage doRequest(String messageToSend) throws AppException {
		return doRequest(messageToSend, true);
	}

	/**
	 * Just call's {@link #doRequest(String, boolean, int)} with retrues = RETRIES_COUNT
	 *
	 * @see {#doRequest}
	 */
	private synchronized ParsedMessage doRequest(String messageToSend, boolean checkBT) throws AppException {
		return doRequest(messageToSend, checkBT, RETRIES_COUNT);
	}

	/**
	 * Send request to server and return parsedMessage or throw exception on error.
	 *
	 * @param messageToSend message in xml
	 * @param checkBT       - when true and BT is not present in Network, then throws AppException with NetworkError.BAD_BT
	 *                      - this logically must be false for requests like register or login, which doesn't require BT for working
	 * @param retries       - number of retries to do the request and receive response
	 * @return object with parsed data
	 * @throws AppException with error ClientError.INTERNET_CONNECTION, NetworkError.BAD_BT, ClientError.XML,
	 *                      ClientError.UNKNOWN_HOST, ClientError.CERTIFICATE, ClientError.SOCKET or ClientError.NO_RESPONSE
	 */
	private synchronized ParsedMessage doRequest(String messageToSend, boolean checkBT, int retries) throws AppException {
		// Check internet connection
		if (!isAvailable())
			throw new AppException(ClientError.INTERNET_CONNECTION);

		// Check existence of BT
		if (checkBT && !hasBT())
			throw new AppException(NetworkError.BAD_BT);

		// ParsedMessage msg = null;
		// Debug.startMethodTracing("Support_231");
		// long ltime = new Date().getTime();
		try {
			Log.d(TAG + " fromApp >>", messageToSend);
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

			return new XmlParsers().parseCommunication(result, false);
		} catch (IOException | XmlPullParserException | ParseException e) {
			throw AppException.wrap(e, ClientError.XML);
		} /*finally {
			// Debug.stopMethodTracing();
			// ltime = new Date().getTime() - ltime;
			// android.util.Log.d("Support_231", ltime+"");
		}*/
	}

	/**
	 * Return new AppException based on error code from parsed False message from server.
	 *
	 * @param msg must be message with getState() State.FALSE
	 * @return AppException object with correct error code set
	 * @throws AppException with NetworkError.UNEXPECTED_RESPONSE when message is not of State.FALSE
	 */
	private AppException processFalse(ParsedMessage msg) throws IllegalStateException {
		// Check validity of this message
		if (msg.getState() != State.FALSE)
			throw new AppException("ParsedMessage is not State.FALSE", ClientError.UNEXPECTED_RESPONSE)
					.set("State", msg.getState())
					.set("Data", msg.data);

		// Parse FalseAnswer data from this message
		FalseAnswer fa = (FalseAnswer) msg.data;

		// Delete BT when we receive error saying that it is invalid
		if (fa.getErrCode() == NetworkError.BAD_BT.getNumber())
			mBT = "";

		// Throw AppException for the caller
		return new AppException(fa.getErrMessage(), Utils.getEnumFromId(NetworkError.class, String.valueOf(fa.getErrCode()), NetworkError.UNKNOWN));
	}

	// /////////////////////////////////////SIGNIN,SIGNUP,GATES//////////////////////

	@Override
	public String getBT() {
		return mBT;
	}

	@Override
	public void setBT(String token) {
		mBT = token;
	}

	@Override
	public boolean hasBT() {
		return !mBT.isEmpty();
	}

	@Override
	public boolean loginMe(IAuthProvider authProvider) {
		// Check existence of authProvider parameters
		Map<String, String> parameters = authProvider.getParameters();
		if (parameters == null || parameters.isEmpty())
			throw new IllegalArgumentException(String.format("IAuthProvider '%s' provided no parameters.", authProvider.getProviderName()));

		ParsedMessage msg = doRequest(XmlCreator.createSignIn(Locale.getDefault().getLanguage(), Utils.getPhoneName(), authProvider), false);

		if (msg.getState() == State.BT) {
			mBT = (String) msg.data;
			return true;
		}

		throw processFalse(msg);
	}

	@Override
	public boolean registerMe(IAuthProvider authProvider) {
		// Check existence of authProvider parameters
		Map<String, String> parameters = authProvider.getParameters();
		if (parameters == null || parameters.isEmpty())
			throw new IllegalArgumentException(String.format("IAuthProvider '%s' provided no parameters.", authProvider.getProviderName()));

		ParsedMessage msg = doRequest(XmlCreator.createSignUp(authProvider), false);

		if (msg.getState() == State.TRUE) {
			return true;
		}

		throw processFalse(msg);
	}

	@Override
	public boolean logout() {
		ParsedMessage msg = doRequest(XmlCreator.createLogout(mBT));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean addProvider(IAuthProvider authProvider) {
		ParsedMessage msg = doRequest(XmlCreator.createJoinAccount(mBT, authProvider));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean removeProvider(String providerName) {
		ParsedMessage msg = doRequest(XmlCreator.createCutAccount(mBT, providerName));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean deleteMyAccount() {
		ParsedMessage msg = doRequest(XmlCreator.createCutAccount(mBT, "all"));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public User loadUserInfo() {
		ParsedMessage msg = doRequest(XmlCreator.createGetUserInfo(mBT));

		if (msg.getState() == State.USERINFO)
			return (User) msg.data;

		throw processFalse(msg);
	}

	@Override
	public boolean addGate(String gateId, String gateName) {
		ParsedMessage msg = doRequest(XmlCreator.createAddGate(mBT, gateId, gateName));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	// http://stackoverflow.com/a/509288/1642090
	@SuppressWarnings("unchecked")
	public List<Gate> getGates() {
		ParsedMessage msg = doRequest(XmlCreator.createGetGates(mBT));

		if (msg.getState() == State.GATES)
			return (List<Gate>) msg.data;

		throw processFalse(msg);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Device> initGate(String gateId) {
		ParsedMessage msg = doRequest(XmlCreator.createGetAllDevices(mBT, gateId));

		if (msg.getState() == State.ALLDEVICES)
			return (ArrayList<Device>) msg.data;

		throw processFalse(msg);
	}

	@Override
	public boolean reInitGate(String oldId, String newId) {
		ParsedMessage msg = doRequest(XmlCreator.createReInitGate(mBT, oldId, newId));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean updateGate(Gate gate) {
		// FIXME someone should implement this method in the future when the server is ready for it
		return false;
	}

	@Override
	public boolean deleteGate(String gateId){
		ParsedMessage msg = doRequest(XmlCreator.createDelGate(mBT, gateId));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	// /////////////////////////////////////DEVICES,LOGS////////////////////////////////

	@Override
	public boolean updateDevices(String gateId, List<Device> devices, EnumSet<SaveModule> toSave) {
		ParsedMessage msg = doRequest(XmlCreator.createSetDevs(mBT, gateId, devices, toSave));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean updateModule(String gateId, Module module, EnumSet<Module.SaveModule> toSave) {
		ParsedMessage msg = doRequest(XmlCreator.createSetDev(mBT, gateId, module, toSave));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean switchState(String gateId, Module module) {
		ParsedMessage msg = doRequest(XmlCreator.createSwitch(mBT, gateId, module));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean prepareGateToListenNewSensors(String gateId) {
		ParsedMessage msg = doRequest(XmlCreator.createGateScanMode(mBT, gateId));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean deleteDevice(Device device) {
		ParsedMessage msg = doRequest(XmlCreator.createDeleteDevice(mBT, device));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	// http://stackoverflow.com/a/509288/1642090
	@SuppressWarnings("unchecked")
	public List<Device> getDevices(List<Device> devices) {
		ParsedMessage msg = doRequest(XmlCreator.createGetDevices(mBT, devices));

		if (msg.getState() == State.DEVICES)
			return (List<Device>) msg.data;

		throw processFalse(msg);
	}

	@Override
	public Device getDevice(Device device) {

		ArrayList<Device> list = new ArrayList<>();
		list.add(device);

		return getDevices(list).get(0);
	}

	@Override
	public boolean updateDevice(String gateId, Device device, EnumSet<SaveModule> toSave) {

		ArrayList<Device> list = new ArrayList<>();
		list.add(device);

		return updateDevices(gateId, list, toSave);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Device> getNewDevices(String gateId) {
		ParsedMessage msg = doRequest(XmlCreator.createGetNewDevices(mBT, gateId));

		if (msg.getState() == State.DEVICES)
			return (List<Device>) msg.data;

		throw processFalse(msg);
	}

	// http://stackoverflow.com/a/509288/1642090
	@Override
	public ModuleLog getLog(String gateId, Module module, ModuleLog.DataPair pair) {
		String msgToSend = XmlCreator.createGetLog(mBT, gateId, module.getDevice().getAddress(), module.getRawTypeId(),
				String.valueOf(pair.interval.getStartMillis() / 1000), String.valueOf(pair.interval.getEndMillis() / 1000),
				pair.type.getId(), pair.gap.getSeconds());

		ParsedMessage msg = doRequest(msgToSend);

		if (msg.getState() == State.LOGDATA) {
			ModuleLog result = (ModuleLog) msg.data;
			result.setDataInterval(pair.gap);
			result.setDataType(pair.type);
			return result;
		}

		throw processFalse(msg);
	}

	// /////////////////////////////////////ROOMS///////////////////////////////////////

	@Override
	// http://stackoverflow.com/a/509288/1642090
	@SuppressWarnings("unchecked")
	public List<Location> getLocations(String gateId) {
		ParsedMessage msg = doRequest(XmlCreator.createGetRooms(mBT, gateId));

		if (msg.getState() == State.ROOMS)
			return (List<Location>) msg.data;

		throw processFalse(msg);
	}

	@Override
	public boolean updateLocations(String gateId, List<Location> locations) {
		ParsedMessage msg = doRequest(XmlCreator.createSetRooms(mBT, gateId, locations));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean updateLocation(Location location) {

		List<Location> list = new ArrayList<>();
		list.add(location);

		return updateLocations(location.getGateId(), list);
	}

	@Override
	public boolean deleteLocation(Location location) {
		ParsedMessage msg = doRequest(XmlCreator.createDeleteRoom(mBT, location));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public Location createLocation(Location location) {
		ParsedMessage msg = doRequest(XmlCreator.createAddRoom(mBT, location));

		if (msg.getState() == State.ROOMCREATED) {
			location.setId((String) msg.data);
			return location;
		}

		throw processFalse(msg);
	}

	// /////////////////////////////////////ACCOUNTS////////////////////////////////////

	@Override
	@SuppressWarnings("unchecked")
	public boolean addAccounts(String gateId, ArrayList<User> users) {
		ParsedMessage msg = doRequest(XmlCreator.createAddAccounts(mBT, gateId, users));

		if (msg.getState() == State.TRUE)
			return true;

		AppException e = processFalse(msg);

		FalseAnswer fa = (FalseAnswer) msg.data;
		String troubleUsers = "";
		for (User u : (ArrayList<User>) fa.troubleMakers) {
			troubleUsers += u.toDebugString() + "\n";
		}

		throw AppException.wrap(e).set("Trouble users", troubleUsers);
	}

	@Override
	public boolean addAccount(String gateId, User user) {

		ArrayList<User> list = new ArrayList<>();
		list.add(user);

		return addAccounts(gateId, list);
	}

	@Override
	public boolean deleteAccounts(String gateId, List<User> users) {
		ParsedMessage msg = doRequest(XmlCreator.createDelAccounts(mBT, gateId, users));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean deleteAccount(String gateId, User user) {

		ArrayList<User> list = new ArrayList<>();
		list.add(user);

		return deleteAccounts(gateId, list);
	}

	@Override
	// http://stackoverflow.com/a/509288/1642090
	@SuppressWarnings("unchecked")
	public List<User> getAccounts(String gateId) {
		ParsedMessage msg = doRequest(XmlCreator.createGetAccounts(mBT, gateId));

		if (msg.getState() == State.ACCOUNTS)
			return (ArrayList<User>) msg.data;

		throw processFalse(msg);
	}

	@Override
	public boolean updateAccounts(String gateId, ArrayList<User> users) {
		ParsedMessage msg = doRequest(XmlCreator.createSetAccounts(mBT, gateId, users));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean updateAccount(String gateId, User user) {

		ArrayList<User> list = new ArrayList<>();
		list.add(user);

		return updateAccounts(gateId, list);
	}

	// /////////////////////////////////////TIME////////////////////////////////////////

	public boolean setTimeZone(String gateId, int offsetInMinutes) {
		ParsedMessage msg = doRequest(XmlCreator.createSetTimeZone(mBT, gateId, offsetInMinutes));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public int getTimeZone(String gateId) {
		ParsedMessage msg = doRequest(XmlCreator.createGetTimeZone(mBT, gateId));

		if (msg.getState() == State.TIMEZONE)
			return (Integer) msg.data;

		throw processFalse(msg);
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
		ParsedMessage msg = doRequest(XmlCreator.createDeLGCMID(userId, gcmID));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean NotificationsRead(ArrayList<String> msgID) {
		ParsedMessage msg = doRequest(XmlCreator.createNotificaionRead(mBT, msgID));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	/**
	 * Method set gcmID to server
	 *
	 * @param gcmID to be set
	 * @return true if id has been updated, false otherwise
	 */
	public boolean setGCMID(String gcmID) {
		ParsedMessage msg = doRequest(XmlCreator.createSetGCMID(mBT, gcmID));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	/**
	 * TODO: method need to be checked online
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<VisibleNotification> getNotifications() {
		ParsedMessage msg = doRequest(XmlCreator.createGetNotifications(mBT));

		if (msg.getState() == State.NOTIFICATIONS)
			return (List<VisibleNotification>) msg.data;

		throw processFalse(msg);
	}

	@Override
	public boolean addWatchdog(Watchdog watchdog, String gateId) {
		ParsedMessage msg = doRequest(XmlCreator.createAddAlgor(mBT, watchdog.getName(), gateId, watchdog.getType(), watchdog.getModules(), watchdog.getParams(), watchdog.getGeoRegionId()));

		if (msg.getState() == State.ALGCREATED) {
			watchdog.setId((String) msg.data);
			return true;
		}

		throw processFalse(msg);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Watchdog> getWatchdogs(ArrayList<String> watchdogIds, String gateId) {
		ParsedMessage msg = doRequest(XmlCreator.createGetAlgs(mBT, gateId, watchdogIds));

		if (msg.getState() == State.ALGORITHMS) {
			return (ArrayList<Watchdog>) msg.data;
		}

		throw processFalse(msg);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Watchdog> getAllWatchdogs(String gateId) {
		ParsedMessage msg = doRequest(XmlCreator.createGetAllAlgs(mBT, gateId));

		if (msg.getState() == State.ALGORITHMS) {
			return (ArrayList<Watchdog>) msg.data;
		}

		throw processFalse(msg);
	}

	@Override
	public boolean updateWatchdog(Watchdog watchdog, String gateId) {
		ParsedMessage msg = doRequest(XmlCreator.createSetAlgor(mBT, watchdog.getName(), watchdog.getId(), gateId, watchdog.getType(), watchdog.isEnabled(), watchdog.getModules(), watchdog.getParams(), watchdog.getGeoRegionId()));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean deleteWatchdog(Watchdog watchdog) {
		ParsedMessage msg = doRequest(XmlCreator.createDelAlg(mBT, watchdog.getId()));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean passBorder(String regionId, String type) {
		ParsedMessage msg = doRequest(XmlCreator.createPassBorder(mBT, regionId, type));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}
}
