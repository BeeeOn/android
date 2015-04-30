package com.rehivetech.beeeon.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.rehivetech.beeeon.BuildConfig;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Device.SaveDevice;
import com.rehivetech.beeeon.household.device.DeviceLog;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.household.watchdog.WatchDog;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.network.xml.CustomViewPair;
import com.rehivetech.beeeon.network.xml.FalseAnswer;
import com.rehivetech.beeeon.network.xml.ParsedMessage;
import com.rehivetech.beeeon.network.xml.XmlCreator;
import com.rehivetech.beeeon.network.xml.XmlParsers;
import com.rehivetech.beeeon.network.xml.XmlParsers.State;
import com.rehivetech.beeeon.network.xml.action.ComplexAction;
import com.rehivetech.beeeon.network.xml.condition.Condition;
import com.rehivetech.beeeon.pair.LogDataPair;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.ConnectException;
import java.net.Socket;
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

	private final Context mContext;
	private String mBT = "";
	private final boolean mUseDebugServer;
	private static final int SSLTIMEOUT = 35000;

	private SSLSocket permaSocket = null;
	private PrintWriter permaWriter = null;
	private BufferedReader permaReader = null;
	private static final String EOF = "</com>";
	private int mMultiSessions = 0;

	/**
	 * Constructor.
	 *
	 * @param context
	 * @param useDebugServer
	 */
	public Network(Context context, boolean useDebugServer) {
		mContext = context;
		mUseDebugServer = useDebugServer;
	}

	/**
	 * Send request to server.
	 *
	 * @param w
	 * @param closeWriter
	 * @param request
	 * @throws AppException with error NetworkError.CL_SOCKET
	 */
	public void sendRequest(final Writer w, final String request, final boolean closeWriter) throws AppException {
		// Send request
		try {
			w.write(request, 0, request.length());
			w.flush();
		} catch (IOException e) {
			throw AppException.wrap(e, NetworkError.CL_SOCKET);
		} finally {
			if (closeWriter) {
				try {
					w.close();
				} catch (IOException e) {
					e.printStackTrace(); // Nothing we can do here
				}
			}
		}
	}

	/**
	 * Recive response from server.
	 *
	 * @param r
	 * @param closeReader
	 * @return response
	 * @throws AppException with error NetworkError.CL_SOCKET
	 */
	public String receiveResponse(final BufferedReader r, final boolean closeReader) throws AppException {
		// Receive response
		StringBuilder response = new StringBuilder();
		try {
			String actRecieved = null;
			while ((actRecieved = r.readLine()) != null) {
				response.append(actRecieved);
				if (actRecieved.endsWith(EOF))
					break;
			}
		} catch (IOException e) {
			throw AppException.wrap(e, NetworkError.CL_SOCKET);
		} finally {
			// If we use persistent connection, don't close the socket and objects
			if (closeReader) {
				try {
					r.close();
				} catch (IOException e) {
					e.printStackTrace(); // Nothing we can do here
				}
			}
		}

		return response.toString();
	}

	/**
	 * Method for communicating with server.
	 *
	 * @param request
	 * @return
	 * @throws AppException with error NetworkError.CL_UNKNOWN_HOST, NetworkError.CL_CERTIFICATE or NetworkError.CL_SOCKET
	 */
	private String startCommunication(String request) throws AppException {
		// Init socket
		Socket socket;
		if (mMultiSessions > 0) {
			socket = permaSocket; // Reuse existing socket
		} else {
			socket = initSocket(); // Init new socket for this request
		}

		// At this point SSLSocket performed certificate verification and
		// we have performed hostName verification, so it is safe to proceed.
		Writer w;
		BufferedReader r;
		if (mMultiSessions > 0) {
			// Reuse existing objects
			w = permaWriter;
			r = permaReader;
		} else {
			// Init new writer/reader for this request
			try {
				w = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				r = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (IOException e) {
				// Error when getting socket's output/input stream
				throw AppException.wrap(e, NetworkError.CL_SOCKET);
			}
		}
		// Send request (and close writer if not multi session)
		sendRequest(w, request, mMultiSessions == 0);

		// Receive response (and close reader if not multi session)
		String response = receiveResponse(r, mMultiSessions == 0);

		// Close socket if not multi session
		if (mMultiSessions == 0) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace(); // Nothing we can do here
			}
		}

		// Return server response
		return response;
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

	/**
	 * Method initialize perma-Socket/Reader/Writer for doing more requests to server with this single connection
	 * On success increment mMultiSessions, on failure call MultiSessionEnd and throw AppException
	 * @throws AppException with error NetworkError.CL_INTERNET_CONNECTION or NetworkError.CL_SOCKET
	 */
	public synchronized void multiSessionBegin() throws AppException {
		if (!isAvailable())
			throw new AppException(NetworkError.CL_INTERNET_CONNECTION);

		// If some session already existed, just return
		if (++mMultiSessions > 1)
			return;

		permaSocket = initSocket();

		try {
			// At this point SSLSocket performed certificate verification and
			// we have performed hostName verification, so it is safe to proceed.
			permaWriter = new PrintWriter(permaSocket.getOutputStream());
			permaReader = new BufferedReader(new InputStreamReader(permaSocket.getInputStream()));
		} catch (IOException e) {
			// Close any opened socket/writer/reader
			multiSessionEnd();

			throw AppException.wrap(e, NetworkError.CL_SOCKET);
		}
	}

	/**
	 * Method close any opened perma-Socket/Reader/Writer if it was opened by multiSessionBegin() before
	 * Also decrement mMultiSessions
	 */
	public synchronized void multiSessionEnd() {
		// If there is no active session, just return
		if (mMultiSessions == 0)
			return;

		// If there is still some active session, just return
		if (--mMultiSessions > 0)
			return;

		// Securely close socket
		if (permaSocket != null) {
			try {
				permaSocket.close();
			} catch (IOException e) {
				e.printStackTrace(); // Nothing we can do here
			} finally {
				permaSocket = null;
			}
		}

		// Close writer
		if (permaWriter != null) {
			permaWriter.close();
			permaWriter = null;
		}

		// Securely close reader
		if (permaReader != null) {
			try {
				permaReader.close();
			} catch (IOException e) {
				e.printStackTrace(); // Nothing we can do here
			} finally {
				permaReader = null;
			}
		}
	}

	/**
	 * Checks if Internet connection is available.
	 *
	 * @return true if available, false otherwise
	 */
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
	private ParsedMessage doRequest(String messageToSend) throws AppException {
		return doRequest(messageToSend, true);
	}

	/**
	 * Send request to server and return parsedMessage or throw exception on error.
	 *
	 * @param messageToSend
	 * @param checkBT - when true and BT is not present in Network, then throws AppException with NetworkError.SRV_BAD_BT
	 *                - this logically must be false for requests like register or login, which doesn't require BT for working
	 * @return
	 * @throws AppException with error NetworkError.CL_INTERNET_CONNECTION, NetworkError.SRV_BAD_BT, NetworkError.CL_XML, NetworkError.CL_UNKNOWN_HOST, NetworkError.CL_CERTIFICATE or NetworkError.CL_SOCKET
	 */
	private synchronized ParsedMessage doRequest(String messageToSend, boolean checkBT) throws AppException {
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
			Log.i(TAG + " << fromSrv", result);

			return new XmlParsers().parseCommunication(result, false);
		} catch (IOException | XmlPullParserException | ParseException e) {
			throw AppException.wrap(e, NetworkError.CL_XML);
		} finally {
			// Debug.stopMethodTracing();
			// ltime = new Date().getTime() - ltime;
			// android.util.Log.d("Support_231", ltime+"");
		}
	}

	/**
	 * Return new AppException based on error code from parsed False message from server.
	 *
	 * @param msg must be message with getState() State.FALSE
	 * @return AppException object with correct error code set
	 * @throws IllegalStateException when message is not of State.FALSE
	 */
	private AppException processFalse(ParsedMessage msg) throws IllegalStateException {
		// Check validity of this message
		if (msg.getState() != State.FALSE)
			throw new IllegalStateException("ParsedMessage is not State.FALSE");

		// Parse FalseAnswer data from this message
		FalseAnswer fa = (FalseAnswer) msg.data;

		// Delete BT when we receive error saying that it is invalid
		if (fa.getErrCode() == NetworkError.SRV_BAD_BT.getNumber())
			mBT = "";

		// Throw AppException for the caller
		return new AppException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////SIGNIN,SIGNUP,ADAPTERS//////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

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

	/**
	 * Method register adapter to server
	 *
	 * @param adapterID
	 *            adapter id
	 * @param adapterName
	 *            adapter name
	 * @return true if adapter has been registered, false otherwise
	 */
	@Override
	public boolean addAdapter(String adapterID, String adapterName) {
		ParsedMessage msg = doRequest(XmlCreator.createAddAdapter(mBT, adapterID, adapterName));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	/**
	 * Method ask for list of adapters. User has to be sign in before
	 *
	 * @return list of adapters or empty list
	 */
	@Override
	// http://stackoverflow.com/a/509288/1642090
	@SuppressWarnings("unchecked")
	public List<Adapter> getAdapters() {
		ParsedMessage msg = doRequest(XmlCreator.createGetAdapters(mBT));

		if (msg.getState() == State.ADAPTERS)
			return (List<Adapter>) msg.data;

		throw processFalse(msg);
	}

	/**
	 * Method ask for whole adapter data
	 *
	 * @param adapterID
	 *            of wanted adapter
	 * @return Adapter
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Facility> initAdapter(String adapterID){
		ParsedMessage msg = doRequest(XmlCreator.createGetAllDevices(mBT, adapterID));

		if (msg.getState() == State.ALLDEVICES)
			return (ArrayList<Facility>) msg.data;

		throw processFalse(msg);
	}

	/**
	 * Method change adapter id
	 *
	 * @param oldId
	 *            id to be changed
	 * @param newId
	 *            new id
	 * @return true if change has been successfully
	 */
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

	/**
	 * Method send updated fields of devices
	 *
	 * @param adapterID
	 * @param facilities
	 * @param toSave
	 * @return true if everything goes well, false otherwise
	 */
	@Override
	public boolean updateFacilities(String adapterID, List<Facility> facilities, EnumSet<SaveDevice> toSave){
		ParsedMessage msg = doRequest(XmlCreator.createSetDevs(mBT, adapterID, facilities, toSave));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	/**
	 * Method send wanted fields of device to server
	 *
	 * @param adapterID
	 *            id of adapter
	 * @param device
	 *            to save
	 * @param toSave
	 *            ENUMSET specified fields to save
	 * @return true if fields has been updated, false otherwise
	 */
	@Override
	public boolean updateDevice(String adapterID, Device device, EnumSet<SaveDevice> toSave){
		ParsedMessage msg = doRequest(XmlCreator.createSetDev(mBT, adapterID, device, toSave));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	/**
	 * Method toggle or set actor to new value
	 *
	 * @param adapterID
	 * @param device
	 * @return
	 */
	@Override
	public boolean switchState(String adapterID, Device device){
		ParsedMessage msg = doRequest(XmlCreator.createSwitch(mBT, adapterID, device));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	/**
	 * Method make adapter to special state, when listen for new sensors (e.g. 15s) and wait if some sensors has been
	 * shaken to connect
	 *
	 * @param adapterID
	 * @return
	 */
	@Override
	public boolean prepareAdapterToListenNewSensors(String adapterID){
		ParsedMessage msg = doRequest(XmlCreator.createAdapterScanMode(mBT, adapterID));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	/**
	 * Method delete facility from server
	 *
	 * @param facility
	 *            to be deleted
	 * @return true if is deleted, false otherwise
	 */
	@Override
	public boolean deleteFacility(Facility facility){
		ParsedMessage msg = doRequest(XmlCreator.createDeleteDevice(mBT, facility));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	/**
	 * Method ask for actual data of facilities
	 *
	 * @param facilities
	 *            list of facilities to which needed actual data
	 * @return list of updated facilities fields
	 */
	@Override
	// http://stackoverflow.com/a/509288/1642090
	@SuppressWarnings("unchecked")
	public List<Facility> getFacilities(List<Facility> facilities){
		ParsedMessage msg = doRequest(XmlCreator.createGetDevices(mBT, facilities));

		if (msg.getState() == State.DEVICES)
			return (List<Facility>) msg.data;

		throw processFalse(msg);
	}

	/**
	 * Method ask server for actual data of one facility
	 *
	 * @param facility
	 * @return
	 */
	@Override
	public Facility getFacility(Facility facility){

		ArrayList<Facility> list = new ArrayList<Facility>();
		list.add(facility);

		return getFacilities(list).get(0);
	}

	@Override
	public boolean updateFacility(String adapterID, Facility facility, EnumSet<SaveDevice> toSave) {

		ArrayList<Facility> list = new ArrayList<Facility>();
		list.add(facility);

		return updateFacilities(adapterID, list, toSave);
	}

	/**
	 * Method get new devices
	 * @param adapterID
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Facility> getNewFacilities(String adapterID) {
		ParsedMessage msg = doRequest(XmlCreator.createGetNewDevices(mBT, adapterID));

		if (msg.getState() == State.DEVICES)
			return (List<Facility>) msg.data;

		throw processFalse(msg);
	}

	/**
	 * Method ask for data of logs
	 *
	 * @param adapterID
	 *
	 * @param device
	 *            id of wanted device
	 * @param pair
	 *            data of log (from, to, type, interval)
	 * @return list of rows with logged data
	 */
	// http://stackoverflow.com/a/509288/1642090
	@Override
	public DeviceLog getLog(String adapterID, Device device, LogDataPair pair){
		String msgToSend = XmlCreator.createGetLog(mBT, adapterID, device.getFacility().getAddress(), device.getRawTypeId(),
				String.valueOf(pair.interval.getStartMillis() / 1000), String.valueOf(pair.interval.getEndMillis() / 1000),
				pair.type.getValue(), pair.gap.getValue());

		ParsedMessage msg = doRequest(msgToSend);

		if (msg.getState() == State.LOGDATA) {
			DeviceLog result = (DeviceLog) msg.data;
			result.setDataInterval(pair.gap);
			result.setDataType(pair.type);
			return result;
		}

		throw processFalse(msg);
	}

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////ROOMS///////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method call to server for actual list of locations
	 *
	 * @return List with locations
	 */
	@Override
	// http://stackoverflow.com/a/509288/1642090
	@SuppressWarnings("unchecked")
	public List<Location> getLocations(String adapterID){
		ParsedMessage msg = doRequest(XmlCreator.createGetRooms(mBT, adapterID));

		if (msg.getState() == State.ROOMS)
			return (List<Location>) msg.data;

		throw processFalse(msg);
	}

	/**
	 * Method call to server to update location
	 *
	 * @param locations
	 *            to update
	 * @return true if everything is OK, false otherwise
	 */
	@Override
	public boolean updateLocations(String adapterID, List<Location> locations){
		ParsedMessage msg = doRequest(XmlCreator.createSetRooms(mBT, adapterID, locations));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	/**
	 * Method call to server to update location
	 *
	 * @param location
	 * @return
	 */
	@Override
	public boolean updateLocation(Location location){

		List<Location> list = new ArrayList<Location>();
		list.add(location);

		return updateLocations(location.getAdapterId(), list);
	}

	/**
	 * Method call to server and delete location
	 *
	 * @param location
	 * @return true room is deleted, false otherwise
	 */
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

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////VIEWS///////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method send newly created custom view
	 *
	 * @param viewName
	 *            name of new custom view
	 * @param iconID
	 *            icon that is assigned to the new view
	 * @param devices
	 *            list of devices that are assigned to new view
	 * @return true if everything goes well, false otherwise
	 */
	@Override
	public boolean addView(String viewName, int iconID, List<Device> devices){
		ParsedMessage msg = doRequest(XmlCreator.createAddView(mBT, viewName, iconID, devices));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	/**
	 * Method ask for list of all custom views
	 *
	 * @return list of defined custom views
	 */
	@Override
	// http://stackoverflow.com/a/509288/1642090
	@SuppressWarnings("unchecked")
	// FIXME: will be edited by ROB demands
	public List<CustomViewPair> getViews(){
		ParsedMessage msg = doRequest(XmlCreator.createGetViews(mBT));

		if (msg.getState() == State.VIEWS)
			return (List<CustomViewPair>) msg.data;

		throw processFalse(msg);
	}

	/**
	 * Method delete whole custom view from server
	 *
	 * @param viewName
	 *            name of view to erase
	 * @return true if view has been deleted, false otherwise
	 */
	@Override
	public boolean deleteView(String viewName){
		ParsedMessage msg = doRequest(XmlCreator.createDelView(mBT, viewName));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	// FIXME: will be edited by ROB demands
	@Override
	public boolean updateView(String viewName, int iconId, Facility facility, NetworkAction action) {
		ParsedMessage msg = doRequest(XmlCreator.createSetView(mBT, viewName, iconId, null, action));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////ACCOUNTS////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean addAccounts(String adapterID, ArrayList<User> users){
		ParsedMessage msg = doRequest(XmlCreator.createAddAccounts(mBT, adapterID, users));

		if (msg.getState() == State.TRUE)
			return true;

		AppException e = processFalse(msg);

		// TODO: This should be made universal, but I don't understand how the troubleMakers work and on what it depends... (Robyer)
		FalseAnswer fa = (FalseAnswer) msg.data;
		String troubleUsers = "";
		for (User u : (ArrayList<User>) fa.troubleMakers){
			troubleUsers += u.toDebugString() + "\n";
		}

		throw AppException.wrap(e).set("Trouble users", troubleUsers);
	}

	/**
	 * Method add new user to adapter
	 *
	 * @param adapterID
	 * @param user
	 * @return
	 */
	@Override
	public boolean addAccount(String adapterID, User user) {

		ArrayList<User> list = new ArrayList<User>();
		list.add(user);

		return addAccounts(adapterID, list);
	}

	/**
	 * Method delete users from actual adapter
	 *
	 * @param users
	 *            email of user
	 * @return true if all users has been deleted, false otherwise
	 */
	@Override
	public boolean deleteAccounts(String adapterID, List<User> users){
		ParsedMessage msg = doRequest(XmlCreator.createDelAccounts(mBT, adapterID, users));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	/**
	 * Method delete on user from adapter
	 *
	 * @param adapterID
	 * @param user
	 * @return
	 */
	@Override
	public boolean deleteAccount(String adapterID, User user){

		ArrayList<User> list = new ArrayList<User>();
		list.add(user);

		return deleteAccounts(adapterID, list);
	}

	/**
	 * Method ask for list of users of current adapter
	 *
	 * @return Map of users where key is email and value is User object
	 */
	@Override
	// http://stackoverflow.com/a/509288/1642090
	@SuppressWarnings("unchecked")
	public ArrayList<User> getAccounts(String adapterID){
		ParsedMessage msg = doRequest(XmlCreator.createGetAccounts(mBT, adapterID));

		if (msg.getState() == State.ACCOUNTS)
			return (ArrayList<User>) msg.data;

		throw processFalse(msg);
	}

	/**
	 * Method update users roles on server on current adapter
	 *
	 * @param adapterID
	 * @param users
	 * @return true if all accounts has been changed false otherwise
	 */
	@Override
	public boolean updateAccounts(String adapterID, ArrayList<User> users){
		ParsedMessage msg = doRequest(XmlCreator.createSetAccounts(mBT, adapterID, users));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	/**
	 * Method update users role on adapter
	 *
	 * @param adapterID
	 * @param user
	 * @return
	 */
	@Override
	public boolean updateAccount(String adapterID, User user){

		ArrayList<User> list = new ArrayList<User>();
		list.add(user);

		return updateAccounts(adapterID, list);
	}

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////TIME////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method set wanted time zone to server
	 *
	 * @NOTE using difference from GMT (UTC+0),
	 *       https://merlin.fit.vutbr.cz/wiki-iot/index.php/Smarthome_cloud#SetTimeZone
	 * @param differenceToGMT
	 * @return
	 */
	@Override
	public boolean setTimeZone(String adapterID, int differenceToGMT){
		ParsedMessage msg = doRequest(XmlCreator.createSetTimeZone(mBT, adapterID, differenceToGMT));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	/**
	 * Method call to server to get actual time zone
	 *
	 * @return integer in range <-12,12>
	 */
	@Override
	public int getTimeZone(String adapterID){
		ParsedMessage msg = doRequest(XmlCreator.createGetTimeZone(mBT, adapterID));

		if (msg.getState() == State.TIMEZONE)
			return (Integer) msg.data;

		throw processFalse(msg);
	}

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////NOTIFICATIONS///////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

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

	/**
	 * Method set read flag to notification on server
	 *
	 * @param msgID
	 *            id of notification
	 * @return true if server took flag, false otherwise
	 */
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
	 * FIXME: after merge need to by rewrite
	 */
	public boolean setGCMID(String gcmID){
		ParsedMessage msg = doRequest(XmlCreator.createSetGCMID(mBT, gcmID));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////CONDITIONS,ACTIONS//////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	@Override
	public Condition setCondition(Condition condition) {
		String messageToSend = XmlCreator.createAddCondition(mBT, condition.getName(),
				XmlCreator.ConditionType.fromValue(condition.getType()), condition.getFuncs());
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.CONDITIONCREATED) {
			condition.setId((String) msg.data);
			return condition;
		}

		throw processFalse(msg);
	}

	@Override
	public boolean connectConditionWithAction(String conditionID, String actionID) {
		ParsedMessage msg = doRequest(XmlCreator.createConditionPlusAction(mBT, conditionID, actionID));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public Condition getCondition(Condition condition) {
		ParsedMessage msg = doRequest(XmlCreator.createGetCondition(mBT, condition.getId()));

		if (msg.getState() == State.CONDITIONCREATED) {
			Condition cond = (Condition) msg.data;

			condition.setType(cond.getType());
			condition.setFuncs(cond.getFuncs());
			return condition;
		}

		throw processFalse(msg);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Condition> getConditions() {
		ParsedMessage msg = doRequest(XmlCreator.createGetConditions(mBT));

		if (msg.getState() == State.CONDITIONS)
			return (List<Condition>) msg.data;

		throw processFalse(msg);
	}

	@Override
	public boolean updateCondition(Condition condition) {
		String messageToSend = XmlCreator.createSetCondition(mBT, condition.getName(),
				XmlCreator.ConditionType.fromValue(condition.getType()), condition.getId(), condition.getFuncs());
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean deleteCondition(Condition condition) {
		ParsedMessage msg = doRequest(XmlCreator.createDelCondition(mBT, condition.getId()));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public ComplexAction setAction(ComplexAction action) {
		ParsedMessage msg = doRequest(XmlCreator.createAddAction(mBT, action.getName(), action.getActions()));

		if (msg.getState() == State.ACTIONCREATED) {
			action.setId((String) msg.data);
			return action;
		}

		throw processFalse(msg);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ComplexAction> getActions() {
		ParsedMessage msg = doRequest(XmlCreator.createGetActions(mBT));

		if (msg.getState() == State.ACTIONS)
			return (List<ComplexAction>) msg.data;

		throw processFalse(msg);
	}

	@Override
	public ComplexAction getAction(ComplexAction action) {
		ParsedMessage msg = doRequest(XmlCreator.createGetCondition(mBT, action.getId()));

		if (msg.getState() == State.ACTION) {
			ComplexAction act = (ComplexAction) msg.data;
			action.setActions(act.getActions());
			return action;
		}

		throw processFalse(msg);
	}

	@Override
	public boolean updateAction(ComplexAction action) {
		String messageToSend = XmlCreator.createSetAction(mBT, action.getName(), action.getId(), action.getActions());
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean deleteAction(ComplexAction action) {
		ParsedMessage msg = doRequest(XmlCreator.createDelAction(mBT, action.getId()));

		if (msg.getState() == State.TRUE)
			return true;

		throw processFalse(msg);
	}

	@Override
	public boolean addWatchDog(WatchDog watchDog, String adapterId){
		ParsedMessage msg = doRequest(XmlCreator.createAddAlgor(mBT, watchDog.getName(), adapterId, watchDog.getType(), watchDog.getDevices(), watchDog.getParams(), watchDog.getGeoRegionId(), watchDog.getGeoDirectionType()));

		if (msg.getState() == State.ALGCREATED) {
			watchDog.setId((String) msg.data);
			return true;
		}

		throw processFalse(msg);
	}

	@Override
	public List<WatchDog> getWatchDogs(ArrayList<String> watchDogIds, String adapterId){
		ParsedMessage msg = doRequest(XmlCreator.createGetAlgs(mBT, adapterId, watchDogIds));

		if(msg.getState() == State.ALGORITHMS){
			return (ArrayList<WatchDog>) msg.data;
		}

		throw processFalse(msg);
	}

	@Override
	public List<WatchDog> getAllWatchDogs(String adapterID){
		ParsedMessage msg = doRequest(XmlCreator.createGetAllAlgs(mBT, adapterID));

		if(msg.getState() == State.ALGORITHMS){
			return (ArrayList<WatchDog>) msg.data;
		}

		throw processFalse(msg);
	}

	@Override
	public boolean updateWatchDog(WatchDog watchDog, String AdapterId){
		ParsedMessage msg = doRequest(XmlCreator.createSetAlgor(mBT, watchDog.getName(), watchDog.getId(), AdapterId, watchDog.getType(), watchDog.isEnabled(), watchDog.getDevices(), watchDog.getParams(), watchDog.getGeoRegionId(), watchDog.getGeoDirectionType()));

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

}
