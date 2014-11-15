package cz.vutbr.fit.iha.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Debug;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.Device;
import cz.vutbr.fit.iha.adapter.device.Device.SaveDevice;
import cz.vutbr.fit.iha.adapter.device.DeviceLog;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.household.ActualUser;
import cz.vutbr.fit.iha.household.User;
import cz.vutbr.fit.iha.network.exception.CommunicationException;
import cz.vutbr.fit.iha.network.exception.FalseException;
import cz.vutbr.fit.iha.network.exception.NoConnectionException;
import cz.vutbr.fit.iha.network.exception.NotRegAException;
import cz.vutbr.fit.iha.network.exception.NotRegBException;
import cz.vutbr.fit.iha.network.xml.CustomViewPair;
import cz.vutbr.fit.iha.network.xml.FalseAnswer;
import cz.vutbr.fit.iha.network.xml.ParsedMessage;
import cz.vutbr.fit.iha.network.xml.Xconstants;
import cz.vutbr.fit.iha.network.xml.XmlCreator;
import cz.vutbr.fit.iha.network.xml.XmlParsers;
import cz.vutbr.fit.iha.network.xml.XmlParsers.State;
import cz.vutbr.fit.iha.network.xml.action.ComplexAction;
import cz.vutbr.fit.iha.network.xml.condition.Condition;
import cz.vutbr.fit.iha.pair.LogDataPair;
import cz.vutbr.fit.iha.util.Log;

/**
 * Network service that handles communication with server.
 * 
 * @author ThinkDeep
 * @author Robyer
 */
public class Network implements INetwork {

	private static final String TAG = Network.class.getSimpleName();;

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
	private static final String SERVER_ADDR_PRODUCTION = "ant-2.fit.vutbr.cz";
	private static final int SERVER_PORT_PRODUCTION = 4565;

	/**
	 * CN value to be verified in server certificate
	 */
	private static final String SERVER_CN_CERTIFICATE = "ant-2.fit.vutbr.cz";

	private static final String GoogleExcMessage = "Google token error";
	//TODO: delete this
	private static final int BADTOKENCODE = 2;

	private Context mContext;
	private GoogleAuth mGoogleAuth;
	private ActualUser mUser;
	private String mUserID = "";
	private String mSecretVar;
	private boolean mUseDebugServer;
	private boolean mGoogleReinit;
	private Controller mController; // FIXME: remove this dependency on controller?

	/**
	 * Constructor.
	 * 
	 * @param context
	 */
	public Network(Context context, Controller controller, String userID, boolean useDebugServer) {
		mContext = context;
		mController = controller;
		mUseDebugServer = useDebugServer;
		mUserID = userID;
	}

	public void setUser(ActualUser user) {
		mUser = user;
	}

	/**
	 * Method for sending data to server via TLS protocol using own TrustManger to be able to trust self-signed certificates. CA certificated must be located in assets folder. If no exception is
	 * thrown, it returns server response.
	 * 
	 * @param appContext
	 *            Application context to get CA certificate from assets
	 * @param request
	 *            Request to server to be sent
	 * @return Response from server
	 * @throws IOException
	 *             Can't read CA certificate from assets, can't read InputStream or can't write OutputStream.
	 * @throws CertificateException
	 *             Unknown certificate format (default X.509), can't generate CA certificate (it shouldn't occur)
	 * @throws KeyStoreException
	 *             Bad type of KeyStore, can't set CA certificate to KeyStore
	 * @throws NoSuchAlgorithmException
	 *             Unknown SSL/TLS protocol or unknown TrustManager algorithm (it shouldn't occur)
	 * @throws KeyManagementException
	 *             general exception, thrown to indicate an exception during processing an operation concerning key management
	 * @throws UnknownHostException
	 *             *IMPORTANT* Server address or hostName wasn't not found
	 * @throws SSLHandshakeException
	 *             *IMPORTANT* TLS handshake failed
	 */
	private String startCommunication(String request) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException, UnknownHostException,
			SSLHandshakeException {

		/*
		 * opening CA certificate from assets
		 */
		InputStream inStreamCertTmp = null;

		inStreamCertTmp = mContext.getAssets().open(ASSEST_CA_CERT);

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
		SSLSocket socket;
		if (mUseDebugServer) {
			socket = (SSLSocket) sslContext.getSocketFactory().createSocket(SERVER_ADDR_DEBUG, SERVER_PORT_DEBUG);
		} else {
			socket = (SSLSocket) sslContext.getSocketFactory().createSocket(SERVER_ADDR_PRODUCTION, SERVER_PORT_PRODUCTION);
		}

		HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
		socket.setSoTimeout(10000);
		SSLSession s = socket.getSession();
		// FIXME: nobody knows why
		if (!s.isValid())
			Log.e(TAG, "Socket is NOT valid!!!!");

		// Verify that the certificate hostName
		// This is due to lack of SNI support in the current SSLSocket.
		if (!hv.verify(SERVER_CN_CERTIFICATE, s)) {
			Log.e(TAG, "Certificate is not VERIFIED!!!");

			throw new SSLHandshakeException("Expected CN value:" + SERVER_CN_CERTIFICATE + ", found " + s.getPeerPrincipal());
		}

		// At this point SSLSocket performed certificate verification and
		// we have performed hostName verification, so it is safe to proceed.
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		w.write(request, 0, request.length());
		w.flush();

		StringBuilder response = new StringBuilder();
		String actRecieved = null;
		while ((actRecieved = r.readLine()) != null) {
			response.append(actRecieved);
		}

		// close socket, writer and reader
		w.close();
		r.close();
		socket.close();

		// return server response
		return response.toString();
	}

	/**
	 * Must be called on start or on reinit
	 * 
	 * @param googleAuth
	 */
	public void initGoogle(GoogleAuth googleAuth) {
		mGoogleAuth = googleAuth;
		mGoogleReinit = false;
	}

	/**
	 * Method start downloading data from google
	 * 
	 * @param blocking
	 *            true is running in same thread, false for start new thread
	 * @param fetchPhoto
	 *            true if want download user photo, false if not
	 * @return true if everything Ok, false when you need to reinit object via call initGoogle(GoogleAuth), or some error
	 */
	public boolean startGoogleAuth(boolean blocking, boolean fetchPhoto) {
		if (blocking) {
			if (mGoogleAuth.doInForeground(fetchPhoto)) {
				mUser.setName(mGoogleAuth.getUserName());
				mUser.setEmail(mGoogleAuth.getEmail());
				mUser.setPicture(mGoogleAuth.getPictureIMG());
				mUser.setPictureUrl(mGoogleAuth.getPicture());
				return true;
			}
			return false;
		} else {
			if (mGoogleReinit)
				return false;
			mGoogleAuth.execute();
		}
		return true;
	}

	/**
	 * Checks if Internet connection is available.
	 * 
	 * @return true if available, false otherwise
	 */
	public boolean isAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	private void doResign() {
		// TODO: maybe use diffrenD way to resign, case stopping of thread,
		// manage this after implement in the controller
		try {
			// GoogleAuth.getGoogleAuth().doInForeground(false);
			startGoogleAuth(true, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mSecretVar = mUserID;
		signIn(mUser.getEmail(), mController.getGCMRegistrationId()); // FIXME: gcmid
	}

	private ParsedMessage doRequest(String messageToSend) {
		if (!isAvailable())
			throw new NoConnectionException();

		ParsedMessage msg = null;
//		Debug.startMethodTracing("Support_231");
//		long ltime = new Date().getTime();
		try {
			String result = startCommunication(messageToSend);

			Log.d(TAG + " - fromApp", messageToSend);
			Log.i(TAG + " - fromSrv", result);

			msg = new XmlParsers().parseCommunication(result, false);
			if (msg.getState() == State.FALSE && ((FalseAnswer) msg.data).getErrCode() == Constants.ERR_BAD_UID) {
				doResign();
				// try it one more time
				result = startCommunication(messageToSend.replace(Xconstants.SID + "=\"" + mSecretVar + "\"", Xconstants.SID + "=\"" + mUserID + "\"")); // FIXME: hot fix

				Log.d(TAG + " - fromApp", messageToSend);
				Log.i(TAG + " - fromSrv", result);

				msg = new XmlParsers().parseCommunication(result, false);
			}
			
			return msg;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}finally{
//			Debug.stopMethodTracing();
//			ltime = new Date().getTime() - ltime;
//			android.util.Log.d("Support_231", ltime+"");
		}
		
	}

	/**
	 * Blocking way to get token
	 * 
	 * @return google token
	 */
	private String getGoogleToken() {
		if (!isAvailable())
			throw new NoConnectionException();

		String googleToken = "";
		try {
			do {
				googleToken = mGoogleAuth.getToken();
			} while (googleToken.equalsIgnoreCase(""));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return googleToken;
	}

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////SIGNIN,SIGNUP,ADAPTERS//////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method signIn user given by its email to server, BUT before calling must call GoogleAuth to get googleToken in it and init ActualUser
	 * 
	 * @param email
	 *            of current user
	 * @return boolean
	 * @throws NoConnectionException
	 *             if there is no Internet connection
	 * @throws CommunicationException
	 *             if there is some problem with certificate, timeout, or other communication problem
	 * @throws NotRegAException
	 *             if this user is not registered on server and on server is NO FREE ADAPTER (without its lord)
	 * @throws NotRegBException
	 *             if this user is not registered on the server but there is FREE ADAPTER
	 */
	@Deprecated
	public boolean signIn(String email, String gcmid) throws NoConnectionException, CommunicationException, FalseException {

		String googleToken = getGoogleToken();
		if (googleToken.length() == 0)
			throw new CommunicationException(GoogleExcMessage);

		ParsedMessage msg = doRequest(XmlCreator.createSignIn(email, googleToken, Locale.getDefault().getLanguage(), gcmid));

		if (!msg.getUserId().isEmpty() && msg.getState() == State.TRUE) {
			mUser.setUserId(msg.getUserId());
			mUserID = msg.getUserId();
			return true;
		}
		if (msg.getState() == State.FALSE && ((FalseAnswer) msg.data).getErrCode() == BADTOKENCODE)
			mGoogleAuth.invalidateToken();

		throw new FalseException(((FalseAnswer) msg.data));
	}

	/**
	 * Method sign user to server with its email up
	 * 
	 * @param email
	 *            of registering user
	 * @return true if everything goes well, false otherwise
	 * @throws CommunicationException
	 *             including message from server
	 * @throws NoConnectionException
	 */
	@Deprecated
	public boolean signUp(String email) throws CommunicationException, NoConnectionException, FalseException {

		String googleToken = getGoogleToken();
		if (googleToken.length() == 0)
			throw new CommunicationException(GoogleExcMessage);

		ParsedMessage msg = doRequest(XmlCreator.createSignUp(email, googleToken));

		if (msg.getState() == State.TRUE)
			return true;

		if (msg.getState() == State.FALSE && ((FalseAnswer) msg.data).getErrCode() == BADTOKENCODE)
			mGoogleAuth.invalidateToken();

		throw new FalseException(((FalseAnswer) msg.data));
	}

	public boolean getUID(String email) throws NoConnectionException, CommunicationException, FalseException {
		String googleToken = getGoogleToken();
		String googleID = mGoogleAuth.getId(); // TODO: check this - GOOGLE ID
		if (googleToken.length() == 0)
			throw new CommunicationException(GoogleExcMessage);

		ParsedMessage msg = doRequest(XmlCreator.createGetUID(googleID, googleToken, Locale.getDefault().getLanguage()));

		if (!msg.getUserId().isEmpty() && msg.getState() == State.UID) {
			mUser.setUserId(msg.getUserId());
			mUserID = msg.getUserId();
			return true;
		}
		if (msg.getState() == State.FALSE && ((FalseAnswer) msg.data).getErrCode() == Constants.ERR_NOT_VALID_USER)
			mGoogleAuth.invalidateToken();

		throw new FalseException(((FalseAnswer) msg.data));
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
	public boolean addAdapter(String adapterID, String adapterName) {
		ParsedMessage msg = doRequest(XmlCreator.createAddAdapter(mUserID, adapterID, adapterName));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	/**
	 * Method ask for list of adapters. User has to be sign in before
	 * 
	 * @return list of adapters or empty list
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 *             including message from server including message from server
	 */
	// http://stackoverflow.com/a/509288/1642090
	@SuppressWarnings("unchecked")
	public List<Adapter> getAdapters() throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createGetAdapters(mUserID));

		if (msg.getState() == State.ADAPTERS)
			return (List<Adapter>) msg.data;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	/**
	 * Method ask for whole adapter data
	 * 
	 * @param adapterID
	 *            of wanted adapter
	 * @return Adapter
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	@SuppressWarnings("unchecked")
	public List<Facility> initAdapter(String adapterID) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createGetAllDevices(mUserID, adapterID));

		if (msg.getState() == State.ALLDEVICES)
			return (ArrayList<Facility>) msg.data;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	/**
	 * Method change adapter id
	 * 
	 * @param oldId
	 *            id to be changed
	 * @param newId
	 *            new id
	 * @return true if change has been successfully
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public boolean reInitAdapter(String oldId, String newId) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createReInitAdapter(mUserID, oldId, newId));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////DEVICES,LOGS////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method send updated fields of devices
	 * 
	 * @param devices
	 * @return true if everything goes well, false otherwise
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public boolean updateFacilities(String adapterID, List<Facility> facilities, EnumSet<SaveDevice> toSave) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createSetDevs(mUserID, adapterID, facilities, toSave));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
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
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	public boolean updateDevice(String adapterID, Device device, EnumSet<SaveDevice> toSave) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createSetDev(mUserID, adapterID, device, toSave));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	/**
	 * Method toggle or set actor to new value
	 * 
	 * @param adapterID
	 * @param device
	 * @return
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	public boolean switchState(String adapterID, Device device) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createSwitch(mUserID, adapterID, device));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	/**
	 * Method make adapter to special state, when listen for new sensors (e.g. 15s) and wait if some sensors has been shaken to connect
	 * 
	 * @param adapterID
	 * @return
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	public boolean prepareAdapterToListenNewSensors(String adapterID) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createAdapterScanMode(mUserID, adapterID));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));

	}

	/**
	 * Method delete facility from server
	 * 
	 * @param adapterID
	 * @param facility
	 *            to be deleted
	 * @return true if is deleted, false otherwise
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	public boolean deleteFacility(String adapterID, Facility facility) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createDeleteDevice(mUserID, adapterID, facility));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));

	}

	/**
	 * Method ask for actual data of facilities
	 * 
	 * @param facilities
	 *            list of facilities to which needed actual data
	 * @return list of updated facilities fields
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	// http://stackoverflow.com/a/509288/1642090
	@SuppressWarnings("unchecked")
	public List<Facility> getFacilities(List<Facility> facilities) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createGetDevices(mUserID, facilities));

		if (msg.getState() == State.DEVICES)
			return (List<Facility>) msg.data;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	/**
	 * Method ask server for actual data of one facility
	 * 
	 * @param facility
	 * @return
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	public Facility getFacility(Facility facility) throws NoConnectionException, CommunicationException, FalseException {

		ArrayList<Facility> list = new ArrayList<Facility>();
		list.add(facility);

		return getFacilities(list).get(0);
	}

	public boolean updateFacility(String adapterID, Facility facility, EnumSet<SaveDevice> toSave) {

		ArrayList<Facility> list = new ArrayList<Facility>();
		list.add(facility);

		return updateFacilities(adapterID, list, toSave);
	}

	/**
	 * TODO: need to test
	 * 
	 * @param adapterID
	 * @param facilities
	 * @return
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	@SuppressWarnings("unchecked")
	public List<Facility> getNewFacilities(String adapterID) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createGetNewDevices(mUserID, adapterID));

		if (msg.getState() == State.DEVICES)
			return (List<Facility>) msg.data;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	/**
	 * Method ask for data of logs
	 * 
	 * @param deviceId
	 *            id of wanted device
	 * @param pair
	 *            data of log (from, to, type, interval)
	 * @return list of rows with logged data
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	// http://stackoverflow.com/a/509288/1642090
	public DeviceLog getLog(String adapterID, Device device, LogDataPair pair) throws NoConnectionException, CommunicationException, FalseException {
		String msgToSend = XmlCreator.createGetLog(mUserID, adapterID, device.getFacility().getAddress(), device.getType().getTypeId(), String.valueOf(pair.interval.getStartMillis() / 1000),
				String.valueOf(pair.interval.getEndMillis() / 1000), pair.type.getValue(), pair.gap.getValue());

		ParsedMessage msg = doRequest(msgToSend);

		if (msg.getState() == State.LOGDATA) {
			DeviceLog result = (DeviceLog) msg.data;
			result.setDataInterval(pair.gap);
			result.setDataType(pair.type);
			return result;
		}
		throw new FalseException(((FalseAnswer) msg.data));
	}

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////ROOMS///////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method call to server for actual list of locations
	 * 
	 * @return List with locations
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	// http://stackoverflow.com/a/509288/1642090
	@SuppressWarnings("unchecked")
	public List<Location> getLocations(String adapterID) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createGetRooms(mUserID, adapterID));

		if (msg.getState() == State.ROOMS)
			return (List<Location>) msg.data;

		throw new FalseException(((FalseAnswer) msg.data));

	}

	/**
	 * Method call to server to update location
	 * 
	 * @param locations
	 *            to update
	 * @return true if everything is OK, false otherwise
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public boolean updateLocations(String adapterID, List<Location> locations) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createSetRooms(mUserID, adapterID, locations));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	/**
	 * Method call to server to update location
	 * 
	 * @param adapterID
	 * @param location
	 * @return
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	public boolean updateLocation(String adapterID, Location location) throws NoConnectionException, CommunicationException, FalseException {

		List<Location> list = new ArrayList<Location>();
		list.add(location);

		return updateLocations(adapterID, list);
	}

	/**
	 * Method call to server and delete location
	 * 
	 * @param location
	 *            to delete
	 * @return true room is deleted, false otherwise
	 */
	public boolean deleteLocation(String adapterID, Location location) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createDeleteRoom(mUserID, adapterID, location));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	public Location createLocation(String adapterID, Location location) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createAddRoom(mUserID, adapterID, location));

		if (msg.getState() == State.ROOMCREATED) {
			location.setId((String) msg.data);
			return location;
		}
		throw new FalseException(((FalseAnswer) msg.data));
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
	 * @param deviceIds
	 *            list of devices that are assigned to new view
	 * @return true if everything goes well, false otherwise
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public boolean addView(String viewName, int iconID, List<Device> devices) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createAddView(mUserID, viewName, iconID, devices));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	/**
	 * Method ask for list of all custom views
	 * 
	 * @return list of defined custom views
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	// http://stackoverflow.com/a/509288/1642090
	@SuppressWarnings("unchecked")
	// FIXME: will be edited by ROB demands
	public List<CustomViewPair> getViews() throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createGetViews(mUserID));

		if (msg.getState() == State.VIEWS)
			return (List<CustomViewPair>) msg.data;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	/**
	 * Method delete whole custom view from server
	 * 
	 * @param viewName
	 *            name of view to erase
	 * @return true if view has been deleted, false otherwise
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public boolean deleteView(String viewName) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createDelView(mUserID, viewName));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	// FIXME: will be edited by ROB demands
	public boolean updateView(String viewName, int iconId, Facility facility, NetworkAction action) {
		ParsedMessage msg = doRequest(XmlCreator.createSetView(mUserID, viewName, iconId, null, action));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////ACCOUNTS////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	public boolean addAccounts(String adapterID, ArrayList<User> users) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createAddAccounts(mUserID, adapterID, users));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	/**
	 * Method add new user to adapter
	 * 
	 * @param adapterID
	 * @param email
	 * @param role
	 * @return
	 */
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
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public boolean deleteAccounts(String adapterID, List<User> users) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createDelAccounts(mUserID, adapterID, users));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	/**
	 * Method delete on user from adapter
	 * 
	 * @param adapterID
	 * @param user
	 * @return
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	public boolean deleteAccount(String adapterID, User user) throws NoConnectionException, CommunicationException, FalseException {

		ArrayList<User> list = new ArrayList<User>();
		list.add(user);

		return deleteAccounts(adapterID, list);
	}

	/**
	 * Method ask for list of users of current adapter
	 * 
	 * @return Map of users where key is email and value is User object
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	// http://stackoverflow.com/a/509288/1642090
	@SuppressWarnings("unchecked")
	public HashMap<String, User> getAccounts(String adapterID) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createGetAccounts(mUserID, adapterID));

		if (msg.getState() == State.ACCOUNTS)
			return (HashMap<String, User>) msg.data;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	/**
	 * Method update users roles on server on current adapter
	 * 
	 * @param userNrole
	 *            map with email as key and role as value
	 * @return true if all accounts has been changed false otherwise
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public boolean updateAccounts(String adapterID, ArrayList<User> users) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createSetAccounts(mUserID, adapterID, users));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	/**
	 * Method update users role on adapter
	 * 
	 * @param adapterID
	 * @param user
	 * @param role
	 * @return
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	public boolean updateAccount(String adapterID, User user) throws NoConnectionException, CommunicationException, FalseException {

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
	 * @NOTE using difference from GMT (UTC+0), https://merlin.fit.vutbr.cz/wiki-iot/index.php/Smarthome_cloud#SetTimeZone
	 * @param differenceToGMT
	 * @return
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public boolean setTimeZone(String adapterID, int differenceToGMT) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createSetTimeZone(mUserID, adapterID, differenceToGMT));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	/**
	 * Method call to server to get actual time zone
	 * 
	 * @return integer in range <-12,12>
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public int getTimeZone(String adapterID) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createGetTimeZone(mUserID, adapterID));

		if (msg.getState() == State.TIMEZONE)
			return (Integer) msg.data;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////NOTIFICATIONS///////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method delete old gcmid to avoid fake notifications
	 * 
	 * @param email
	 *            of old/last user of gcmid (app+device id)
	 * @param gcmID
	 *            - google cloud message id
	 * @return true if id has been deleted, false otherwise
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	public boolean deleteGCMID(String email, String gcmID) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createDeLGCMID(mUserID, email, gcmID));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	/**
	 * Method set read flag to notification on server
	 * 
	 * @param msgID
	 *            id of notification
	 * @return true if server took flag, false otherwise
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	public boolean NotificationsRead(ArrayList<String> msgID) throws NoConnectionException, CommunicationException, FalseException {
		ParsedMessage msg = doRequest(XmlCreator.createNotificaionRead(mUserID, msgID));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////CONDITIONS,ACTIONS//////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	public Condition setCondition(Condition condition) {
		String messageToSend = XmlCreator.createAddCondition(mUserID, condition.getName(), XmlCreator.ConditionType.fromValue(condition.getType()), condition.getFuncs());
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.CONDITIONCREATED) {
			condition.setId((String) msg.data);
			return condition;
		}
		throw new FalseException(((FalseAnswer) msg.data));
	}

	public boolean connectConditionWithAction(String conditionID, String actionID) {
		ParsedMessage msg = doRequest(XmlCreator.createConditionPlusAction(mUserID, conditionID, actionID));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	public Condition getCondition(Condition condition) {
		ParsedMessage msg = doRequest(XmlCreator.createGetCondition(mUserID, condition.getId()));

		if (msg.getState() == State.CONDITIONCREATED) {
			Condition cond = (Condition) msg.data;

			condition.setType(cond.getType());
			condition.setFuncs(cond.getFuncs());
			return condition;
		}
		throw new FalseException(((FalseAnswer) msg.data));
	}

	@SuppressWarnings("unchecked")
	public List<Condition> getConditions() {
		ParsedMessage msg = doRequest(XmlCreator.createGetConditions(mUserID));

		if (msg.getState() == State.CONDITIONS)
			return (List<Condition>) msg.data;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	public boolean updateCondition(Condition condition) {
		String messageToSend = XmlCreator.createSetCondition(mUserID, condition.getName(), XmlCreator.ConditionType.fromValue(condition.getType()), condition.getId(), condition.getFuncs());
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	public boolean deleteCondition(Condition condition) {
		ParsedMessage msg = doRequest(XmlCreator.createDelCondition(mUserID, condition.getId()));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	public ComplexAction setAction(ComplexAction action) {
		ParsedMessage msg = doRequest(XmlCreator.createAddAction(mUserID, action.getName(), action.getActions()));

		if (msg.getState() == State.ACTIONCREATED) {
			action.setId((String) msg.data);
			return action;
		}
		throw new FalseException(((FalseAnswer) msg.data));
	}

	@SuppressWarnings("unchecked")
	public List<ComplexAction> getActions() {
		ParsedMessage msg = doRequest(XmlCreator.createGetActions(mUserID));

		if (msg.getState() == State.ACTIONS)
			return (List<ComplexAction>) msg.data;
		throw new FalseException(((FalseAnswer) msg.data));
	}

	public ComplexAction getAction(ComplexAction action) {
		ParsedMessage msg = doRequest(XmlCreator.createGetCondition(mUserID, action.getId()));

		if (msg.getState() == State.ACTION) {
			ComplexAction act = (ComplexAction) msg.data;
			action.setActions(act.getActions());
			return action;
		}
		throw new FalseException(((FalseAnswer) msg.data));
	}

	public boolean updateAction(ComplexAction action) {
		String messageToSend = XmlCreator.createSetAction(mUserID, action.getName(), action.getId(), action.getActions());
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
	}

	public boolean deleteAction(ComplexAction action) {
		ParsedMessage msg = doRequest(XmlCreator.createDelAction(mUserID, action.getId()));

		if (msg.getState() == State.TRUE)
			return true;

		throw new FalseException(((FalseAnswer) msg.data));
	}

}
