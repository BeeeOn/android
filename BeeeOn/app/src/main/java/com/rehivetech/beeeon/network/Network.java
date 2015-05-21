package com.rehivetech.beeeon.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.rehivetech.beeeon.BuildConfig;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.gamification.AchievementListItem;
import com.rehivetech.beeeon.gcm.notification.VisibleNotification;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Device.SaveDevice;
import com.rehivetech.beeeon.household.device.DeviceLog;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.household.watchdog.WatchDog;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.network.xml.FalseAnswer;
import com.rehivetech.beeeon.network.xml.ParsedMessage;
import com.rehivetech.beeeon.network.xml.XmlCreator;
import com.rehivetech.beeeon.network.xml.XmlParsers;
import com.rehivetech.beeeon.network.xml.XmlParsers.State;
import com.rehivetech.beeeon.pair.LogDataPair;
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

	/**
	 * Number of retries when we receive no response from server (e.g. because persistent connection expires from server side).
	 */
	private static final int RETRIES_COUNT = 2;

	/**
	 * Name of CA certificate located in assets
	 */
	private static final String ASSEST_CA_CERT = "cacert.crt";

	/**
	 * Alias (tag) for CA certificate
	 */
	private static final String ALIAS_CA_CERT = "ca";

	/**
	 * Address and port of debug server
	 */
	private static final String SERVER_ADDR_DEBUG = "ant-2.fit.vutbr.cz";
	private static final int SERVER_PORT_DEBUG = 4566;

	/**
	 * Address and port of production server
	 */
	private static final String SERVER_ADDR_PRODUCTION = "iotpro.fit.vutbr.cz";
	private static final int SERVER_PORT_PRODUCTION = 4565;

	/**
	 * CN value to be verified in server certificate
	 */
	private static final String SERVER_CN_CERTIFICATE = "ant-2.fit.vutbr.cz";

	/**
	 * Marks end of communication messages
	 */
	private static final String EOF = "</com>";

	private final Context mContext;
	private String mBT = "";
	private static final int SSLTIMEOUT = 35000;

	private final Object mSocketLock = new Object();
	private SSLSocket mSocket = null;
	private PrintWriter mSocketWriter = null;
	private BufferedReader mSocketReader = null;

	/**
	 * Constructor.
	 *
	 * @param context of app
	 */
	public Network(Context context) {
		mContext = context;
	}

	/**
	 * Method for communicating with server.
	 *
	 * @param request is message to send
	 * @return response from server
	 * @throws AppException with error NetworkError.CL_UNKNOWN_HOST, NetworkError.CL_CERTIFICATE or NetworkError.CL_SOCKET
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
					throw AppException.wrap(e, NetworkError.CL_SOCKET);
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
			throw AppException.wrap(e, NetworkError.CL_SOCKET);
		}

		// Return server response
		return response.toString();
	}

	private SSLSocket createSocket(SSLContext sslContext) throws IOException {
		if (BuildConfig.BUILD_TYPE.equals("debug") || BuildConfig.BUILD_TYPE.equals("alpha")) {
			return (SSLSocket) sslContext.getSocketFactory().createSocket(SERVER_ADDR_DEBUG, SERVER_PORT_DEBUG);
		} else if (BuildConfig.BUILD_TYPE.equals("beta_ant2")) {
			return (SSLSocket) sslContext.getSocketFactory().createSocket(SERVER_ADDR_DEBUG, SERVER_PORT_PRODUCTION);
		} else {
			return (SSLSocket) sslContext.getSocketFactory().createSocket(SERVER_ADDR_PRODUCTION, SERVER_PORT_PRODUCTION);
		}
	}

	/**
	 * Method for initializing socket for sending data to server via TLS protocol using own TrustManger to be able to trust self-signed
	 * certificates. CA certificated must be located in assets folder.
	 *
	 * @return Initialized socket or throws exception
	 * @throws AppException with error NetworkError.CL_UNKNOWN_HOST, NetworkError.CL_CERTIFICATE or NetworkError.CL_SOCKET
	 */
	private SSLSocket initSocket() {
		try {
			// Open CA certificate from assets
			InputStream inStreamCertTmp = mContext.getAssets().open(ASSEST_CA_CERT);
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
			SSLSocket socket = createSocket(sslContext);


			HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
			//socket.setKeepAlive(true);
			socket.setSoTimeout(SSLTIMEOUT);
			SSLSession s = socket.getSession();
			if (!s.isValid())
				Log.e(TAG, "Socket is not valid! TLS handshake failed.");

			// Verify that the certificate hostName
			// This is due to lack of SNI support in the current SSLSocket.
			if (!hv.verify(SERVER_CN_CERTIFICATE, s)) {
				throw new AppException("Certificate is not verified!", NetworkError.CL_CERTIFICATE)
						.set("Expected CN", SERVER_CN_CERTIFICATE)
						.set("Found CN", s.getPeerPrincipal());
			}
			return socket;
		} catch (UnknownHostException e) {
			// UnknownHostException - Server address or hostName wasn't not found
			throw AppException.wrap(e, NetworkError.CL_UNKNOWN_HOST);
		} catch (ConnectException e) {
			// ConnectException - Connection refused, timeout, etc.
			throw AppException.wrap(e, NetworkError.CL_SERVER_CONNECTION);
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException e) {
			// IOException - Can't read CA certificate from assets or can't create new socket
			// CertificateException - Unknown certificate format (default X.509), can't generate CA certificate (it shouldn't occur)
			// KeyStoreException - Bad type of KeyStore, can't set CA certificate to KeyStore
			// NoSuchAlgorithmException - Unknown SSL/TLS protocol or unknown TrustManager algorithm (it shouldn't occur)
			// KeyManagementException - general exception, thrown to indicate an exception during processing an operation concerning key management
			throw AppException.wrap(e, NetworkError.CL_SOCKET);
		}
	}

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
	 * @param checkBT - when true and BT is not present in Network, then throws AppException with NetworkError.SRV_BAD_BT
	 *                - this logically must be false for requests like register or login, which doesn't require BT for working
	 * @param retries - number of retries to do the request and receive response
	 * @return object with parsed data
	 * @throws AppException with error NetworkError.CL_INTERNET_CONNECTION, NetworkError.SRV_BAD_BT, NetworkError.CL_XML,
	 * 			NetworkError.CL_UNKNOWN_HOST, NetworkError.CL_CERTIFICATE, NetworkError.CL_SOCKET or NetworkError.CL_NO_RESPONSE
	 */
	private synchronized ParsedMessage doRequest(String messageToSend, boolean checkBT, int retries) throws AppException {
		// Check internet connection
		if (!isAvailable())
			throw new AppException(NetworkError.CL_INTERNET_CONNECTION);

		// Check existence of BT
		if (checkBT && !hasBT())
			throw new AppException(NetworkError.SRV_BAD_BT);

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
					throw new AppException("No response from server.", NetworkError.CL_NO_RESPONSE);
				}

				// Probably connection is lost so we need to reinit socket at next call of doRequest
				closeCommunicationSocket();

				// Try to do this request again (with decremented retries)
				Log.d(TAG, String.format("Try to repeat request (retries remaining: %d)", retries - 1));
				return doRequest(messageToSend, checkBT, retries - 1);
			}

			return new XmlParsers().parseCommunication(result, false);
		} catch (IOException | XmlPullParserException | ParseException e) {
			throw AppException.wrap(e, NetworkError.CL_XML);
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
			throw new AppException("ParsedMessage is not State.FALSE", NetworkError.CL_UNEXPECTED_RESPONSE)
					.set("State", msg.getState())
					.set("Data", msg.data);

		// Parse FalseAnswer data from this message
		FalseAnswer fa = (FalseAnswer) msg.data;

		// Delete BT when we receive error saying that it is invalid
		if (fa.getErrCode() == NetworkError.SRV_BAD_BT.getNumber())
			mBT = "";

		// Throw AppException for the caller
		return new AppException(fa.getErrMessage(), Utils.getEnumFromId(NetworkError.class, String.valueOf(fa.getErrCode())));
	}

	// /////////////////////////////////////SIGNIN,SIGNUP,ADAPTERS//////////////////////

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

		ParsedMessage msg = doRequest(XmlCreator.createSignIn(Locale.getDefault().getLanguage(), Utils.getPhoneID(mContext), authProvider), false);

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
	public boolean addProvider(IAuthProvider authProvider){
		ParsedMessage msg = doRequest(XmlCreator.createJoinAccount(mBT, authProvider));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean removeProvider(String providerName){
		ParsedMessage msg = doRequest(XmlCreator.createCutAccount(mBT, providerName));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean deleteMyAccount(){
		ParsedMessage msg = doRequest(XmlCreator.createCutAccount(mBT, "all"));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public User loadUserInfo(){
		ParsedMessage msg = doRequest(XmlCreator.createGetUserInfo(mBT));

		if (msg.getState() == State.USERINFO)
			return (User)msg.data;

		throw processFalse(msg);
	}

	@Override
	public boolean addAdapter(String adapterID, String adapterName) {
		ParsedMessage msg = doRequest(XmlCreator.createAddAdapter(mBT, adapterID, adapterName));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	// http://stackoverflow.com/a/509288/1642090
	@SuppressWarnings("unchecked")
	public List<Adapter> getAdapters() {
		ParsedMessage msg = doRequest(XmlCreator.createGetAdapters(mBT));

		if (msg.getState() == State.ADAPTERS)
			return (List<Adapter>) msg.data;

		throw processFalse(msg);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Facility> initAdapter(String adapterID){
		ParsedMessage msg = doRequest(XmlCreator.createGetAllDevices(mBT, adapterID));

		if (msg.getState() == State.ALLDEVICES)
			return (ArrayList<Facility>) msg.data;

		throw processFalse(msg);
	}

	@Override
	public boolean reInitAdapter(String oldId, String newId){
		ParsedMessage msg = doRequest(XmlCreator.createReInitAdapter(mBT, oldId, newId));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////DEVICES,LOGS////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean updateFacilities(String adapterID, List<Facility> facilities, EnumSet<SaveDevice> toSave){
		ParsedMessage msg = doRequest(XmlCreator.createSetDevs(mBT, adapterID, facilities, toSave));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean updateDevice(String adapterID, Device device, EnumSet<SaveDevice> toSave){
		ParsedMessage msg = doRequest(XmlCreator.createSetDev(mBT, adapterID, device, toSave));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean switchState(String adapterID, Device device){
		ParsedMessage msg = doRequest(XmlCreator.createSwitch(mBT, adapterID, device));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean prepareAdapterToListenNewSensors(String adapterID){
		ParsedMessage msg = doRequest(XmlCreator.createAdapterScanMode(mBT, adapterID));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean deleteFacility(Facility facility){
		ParsedMessage msg = doRequest(XmlCreator.createDeleteDevice(mBT, facility));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	// http://stackoverflow.com/a/509288/1642090
	@SuppressWarnings("unchecked")
	public List<Facility> getFacilities(List<Facility> facilities){
		ParsedMessage msg = doRequest(XmlCreator.createGetDevices(mBT, facilities));

		if (msg.getState() == State.DEVICES)
			return (List<Facility>) msg.data;

		throw processFalse(msg);
	}

	@Override
	public Facility getFacility(Facility facility){

		ArrayList<Facility> list = new ArrayList<>();
		list.add(facility);

		return getFacilities(list).get(0);
	}

	@Override
	public boolean updateFacility(String adapterID, Facility facility, EnumSet<SaveDevice> toSave) {

		ArrayList<Facility> list = new ArrayList<>();
		list.add(facility);

		return updateFacilities(adapterID, list, toSave);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Facility> getNewFacilities(String adapterID) {
		ParsedMessage msg = doRequest(XmlCreator.createGetNewDevices(mBT, adapterID));

		if (msg.getState() == State.DEVICES)
			return (List<Facility>) msg.data;

		throw processFalse(msg);
	}

	// http://stackoverflow.com/a/509288/1642090
	@Override
	public DeviceLog getLog(String adapterID, Device device, LogDataPair pair){
		String msgToSend = XmlCreator.createGetLog(mBT, adapterID, device.getFacility().getAddress(), device.getRawTypeId(),
				String.valueOf(pair.interval.getStartMillis() / 1000), String.valueOf(pair.interval.getEndMillis() / 1000),
				pair.type.getId(), pair.gap.getSeconds());

		ParsedMessage msg = doRequest(msgToSend);

		if (msg.getState() == State.LOGDATA) {
			DeviceLog result = (DeviceLog) msg.data;
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
	public List<Location> getLocations(String adapterID){
		ParsedMessage msg = doRequest(XmlCreator.createGetRooms(mBT, adapterID));

		if (msg.getState() == State.ROOMS)
			return (List<Location>) msg.data;

		throw processFalse(msg);
	}

	@Override
	public boolean updateLocations(String adapterID, List<Location> locations){
		ParsedMessage msg = doRequest(XmlCreator.createSetRooms(mBT, adapterID, locations));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean updateLocation(Location location){

		List<Location> list = new ArrayList<>();
		list.add(location);

		return updateLocations(location.getAdapterId(), list);
	}

	@Override
	public boolean deleteLocation(Location location){
		ParsedMessage msg = doRequest(XmlCreator.createDeleteRoom(mBT, location));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public Location createLocation(Location location){
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
	public boolean addAccounts(String adapterID, ArrayList<User> users){
		ParsedMessage msg = doRequest(XmlCreator.createAddAccounts(mBT, adapterID, users));

		if (msg.getState() == State.TRUE)
			return true;

		AppException e = processFalse(msg);

		FalseAnswer fa = (FalseAnswer) msg.data;
		String troubleUsers = "";
		for (User u : (ArrayList<User>) fa.troubleMakers){
			troubleUsers += u.toDebugString() + "\n";
		}

		throw AppException.wrap(e).set("Trouble users", troubleUsers);
	}

	@Override
	public boolean addAccount(String adapterID, User user) {

		ArrayList<User> list = new ArrayList<>();
		list.add(user);

		return addAccounts(adapterID, list);
	}

	@Override
	public boolean deleteAccounts(String adapterID, List<User> users){
		ParsedMessage msg = doRequest(XmlCreator.createDelAccounts(mBT, adapterID, users));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean deleteAccount(String adapterID, User user){

		ArrayList<User> list = new ArrayList<>();
		list.add(user);

		return deleteAccounts(adapterID, list);
	}

	@Override
	// http://stackoverflow.com/a/509288/1642090
	@SuppressWarnings("unchecked")
	public List<User> getAccounts(String adapterID){
		ParsedMessage msg = doRequest(XmlCreator.createGetAccounts(mBT, adapterID));

		if (msg.getState() == State.ACCOUNTS)
			return (ArrayList<User>) msg.data;

		throw processFalse(msg);
	}

	@Override
	public boolean updateAccounts(String adapterID, ArrayList<User> users){
		ParsedMessage msg = doRequest(XmlCreator.createSetAccounts(mBT, adapterID, users));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean updateAccount(String adapterID, User user){

		ArrayList<User> list = new ArrayList<>();
		list.add(user);

		return updateAccounts(adapterID, list);
	}

	// /////////////////////////////////////TIME////////////////////////////////////////

	public boolean setTimeZone(String adapterID, int offsetInMinutes){
		ParsedMessage msg = doRequest(XmlCreator.createSetTimeZone(mBT, adapterID, offsetInMinutes));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public int getTimeZone(String adapterID){
		ParsedMessage msg = doRequest(XmlCreator.createGetTimeZone(mBT, adapterID));

		if (msg.getState() == State.TIMEZONE)
			return (Integer) msg.data;

		throw processFalse(msg);
	}

	// /////////////////////////////////////NOTIFICATIONS///////////////////////////////

	/**
	 * Method delete old gcmid to avoid fake notifications
	 *
	 * @param userId
	 *            of old/last user of gcmid (app+device id)
	 * @param gcmID
	 *            - google cloud message id
	 * @return true if id has been deleted, false otherwise
	 */
	public boolean deleteGCMID(String userId, String gcmID){
		ParsedMessage msg = doRequest(XmlCreator.createDeLGCMID(userId, gcmID));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean NotificationsRead(ArrayList<String> msgID){
		ParsedMessage msg = doRequest(XmlCreator.createNotificaionRead(mBT, msgID));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	/**
	 * Method set gcmID to server
	 * @param gcmID to be set
	 * @return true if id has been updated, false otherwise
	 */
	public boolean setGCMID(String gcmID){
		ParsedMessage msg = doRequest(XmlCreator.createSetGCMID(mBT, gcmID));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	/**
	 * TODO: method need to be checked online
	 * @return
	 */
	 @SuppressWarnings("unchecked")
	public List<VisibleNotification> getNotifications(){
		ParsedMessage msg = doRequest(XmlCreator.createGetNotifications(mBT));

		if (msg.getState() == State.NOTIFICATIONS)
			return (List<VisibleNotification>) msg.data;

		throw processFalse(msg);
	}

	@Override
	public boolean addWatchDog(WatchDog watchDog, String adapterId){
		ParsedMessage msg = doRequest(XmlCreator.createAddAlgor(mBT, watchDog.getName(), adapterId, watchDog.getType(), watchDog.getDevices(), watchDog.getParams(), watchDog.getGeoRegionId()));

		if (msg.getState() == State.ALGCREATED) {
			watchDog.setId((String) msg.data);
			return true;
		}

		throw processFalse(msg);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<WatchDog> getWatchDogs(ArrayList<String> watchDogIds, String adapterId){
		ParsedMessage msg = doRequest(XmlCreator.createGetAlgs(mBT, adapterId, watchDogIds));

		if(msg.getState() == State.ALGORITHMS){
			return (ArrayList<WatchDog>) msg.data;
		}

		throw processFalse(msg);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<WatchDog> getAllWatchDogs(String adapterID){
		ParsedMessage msg = doRequest(XmlCreator.createGetAllAlgs(mBT, adapterID));

		if(msg.getState() == State.ALGORITHMS){
			return (ArrayList<WatchDog>) msg.data;
		}

		throw processFalse(msg);
	}

	@Override
	public boolean updateWatchDog(WatchDog watchDog, String AdapterId){
		ParsedMessage msg = doRequest(XmlCreator.createSetAlgor(mBT, watchDog.getName(), watchDog.getId(), AdapterId, watchDog.getType(), watchDog.isEnabled(), watchDog.getDevices(), watchDog.getParams(), watchDog.getGeoRegionId()));

		if(msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean deleteWatchDog(WatchDog watchDog){
		ParsedMessage msg = doRequest(XmlCreator.createDelAlg(mBT, watchDog.getId()));

		if(msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean passBorder(String regionId, String type){
		ParsedMessage msg = doRequest(XmlCreator.createPassBorder(mBT, regionId, type));

		if(msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<AchievementListItem> getAllAchievements(String adapterID){
		ParsedMessage msg = doRequest(XmlCreator.createGetAllAchievements(mBT, adapterID));

		if(msg.getState() == State.ACHIEVEMENTS){
			return (ArrayList<AchievementListItem>) msg.data;
		}

		throw processFalse(msg);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> setProgressLvl(String adapterId, String achievementId){
		ParsedMessage msg = doRequest(XmlCreator.createSetProgressLvl(mBT, adapterId, achievementId));

		if(msg.getState() == State.PROGRESS) {
			return (List<String>) msg.data;
		}

		throw processFalse(msg);
	}

}
