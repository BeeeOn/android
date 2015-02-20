package com.rehivetech.beeeon.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
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
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.Device.SaveDevice;
import com.rehivetech.beeeon.adapter.device.DeviceLog;
import com.rehivetech.beeeon.adapter.device.Facility;
import com.rehivetech.beeeon.adapter.location.Location;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.IhaException;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.household.User;
import com.rehivetech.beeeon.network.GoogleAuthHelper.GoogleUserInfo;
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
	private static final String SERVER_ADDR_PRODUCTION = "ant-2.fit.vutbr.cz";
	private static final int SERVER_PORT_PRODUCTION = 4565;

	/**
	 * CN value to be verified in server certificate
	 */
	private static final String SERVER_CN_CERTIFICATE = "ant-2.fit.vutbr.cz";

	private final Context mContext;
	private String mUserID = "";
	private final boolean mUseDebugServer;
	private static final int SSLTIMEOUT = 35000;
	
	private SSLSocket permaSocket = null;
	private PrintWriter permaWriter = null;
	private BufferedReader permaReader = null;
	private static final String EOF = "</com>"; //FIXME: temporary solution
	private boolean mIsMulti = false;

	/**
	 * Constructor.
	 * 
	 * @param context
	 */
	public Network(Context context, Controller controller, boolean useDebugServer) {
		mContext = context;
		mUseDebugServer = useDebugServer;
	}
	
	@Override
	public void setUID(String userId) {
		mUserID = userId;
	}
	
	@Override
	public GoogleUserInfo getUserInfo() {
		// FIXME
		return null;
	}

	/**
	 * Method for sending data to server via TLS protocol using own TrustManger to be able to trust self-signed
	 * certificates. CA certificated must be located in assets folder. If no exception is thrown, it returns server
	 * response.
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
	 *             general exception, thrown to indicate an exception during processing an operation concerning key
	 *             management
	 * @throws UnknownHostException
	 *             *IMPORTANT* Server address or hostName wasn't not found
	 * @throws SSLHandshakeException
	 *             *IMPORTANT* TLS handshake failed
	 */
	private String startCommunication(String request) throws IOException, CertificateException, KeyStoreException,
			NoSuchAlgorithmException, KeyManagementException, UnknownHostException, SSLHandshakeException {

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
		socket.setSoTimeout(SSLTIMEOUT);
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
			if(actRecieved.endsWith(EOF))
				break;
		}

		// close socket, writer and reader
		w.close();
		r.close();
		socket.close();

		// return server response
		return response.toString();
	}
	
	private boolean multiSend(String request) throws IOException{
		if(permaWriter == null)
			return false;
		
		permaWriter.write(request, 0, request.length());
		permaWriter.flush();
		
		return true;
	}
	
	private String multiRecv() throws IOException{
		if(permaReader == null)
			return "";
		
		StringBuilder response = new StringBuilder();
		String actRecieved = null;
		while ((actRecieved = permaReader.readLine()) != null) {
			response.append(actRecieved);
			if(actRecieved.endsWith(EOF))
				break;
		}
		
		return response.toString();
	}
	
	//TODO: remove
	public void test(){
		
		try {
			String uid = "101";
			String aid = "10";
			
			mUserID = uid;
			
			multiSessionBegin();
			
			Log.d("test // get 1", getTimeZone(aid)+"");
			Log.d("test // set 2", setTimeZone(aid, 360)+"");
			Log.d("test // get 3", getTimeZone(aid)+"");
			
//			multiSend(XmlCreator.createGetTimeZone(uid, aid));
//			String resp = multiRecv();
//			
//			Log.d("TEST one", resp);
//			multiSend(XmlCreator.createSetTimeZone(uid, aid, 180));
//			
//			resp = multiRecv();
//			Log.d("TEST two", resp);
//			
//			multiSend(XmlCreator.createGetTimeZone(uid, aid));
//			resp = multiRecv();
//			Log.e("baf", resp);

//			permaSocket.close();
			multiSessionEnd();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void multiInit() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException, UnknownHostException,SSLHandshakeException {
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
		if (mUseDebugServer) {
			permaSocket = (SSLSocket) sslContext.getSocketFactory().createSocket(SERVER_ADDR_DEBUG, SERVER_PORT_DEBUG);
		} else {
			permaSocket = (SSLSocket) sslContext.getSocketFactory().createSocket(SERVER_ADDR_PRODUCTION, SERVER_PORT_PRODUCTION);
		}
		//perma2 = new Socket("192.168.1.150", 4444);
		
		HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
		//permaSocket.setKeepAlive(true);
		permaSocket.setSoTimeout(35000);
		SSLSession s = permaSocket.getSession();
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
		permaWriter = new PrintWriter(permaSocket.getOutputStream());
		permaReader = new BufferedReader(new InputStreamReader(permaSocket.getInputStream()));

	}

	/**
	 * Method initiate session for multiple use
	 * @throws IhaException
	 */
	public void multiSessionBegin() throws IhaException {
		if (!isAvailable())
			throw new IhaException(NetworkError.NO_CONNECTION);

		try {
			multiInit();
			mIsMulti = true;
		} catch (Exception e) {
			throw IhaException.wrap(e, NetworkError.COM_PROBLEMS);
		}
	}
	
	/**
	 * Method close session to server if it was opened by multiSessionBegin method before
	 * @throws IhaException
	 */
	public void multiSessionEnd() throws IhaException{
		if (!mIsMulti)
			return;
		
		try {
			permaWriter.close();
			permaReader.close();
			permaSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw IhaException.wrap(e, NetworkError.CLOSING_ERROR);
		}
		
		permaWriter = null;
		permaReader = null;
		permaSocket = null;
		mIsMulti = false;
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
	 * Method return Mac address of device
	 * @return
	 */
	public String getMAC(){
		WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

		if(wifiManager.isWifiEnabled()) {
		    // WIFI ALREADY ENABLED. GRAB THE MAC ADDRESS HERE
		    WifiInfo info = wifiManager.getConnectionInfo();
		    return info.getMacAddress();
		} else {
		    // ENABLE THE WIFI FIRST
		    wifiManager.setWifiEnabled(true);

		    // WIFI IS NOW ENABLED. GRAB THE MAC ADDRESS HERE
		    WifiInfo info = wifiManager.getConnectionInfo();
		    String address = info.getMacAddress();
		    
		    wifiManager.setWifiEnabled(false);
		    
		    return address;
		}
	}
	
	private ParsedMessage doRequest(String messageToSend) {
		if (!isAvailable())
			throw new IhaException(NetworkError.NO_CONNECTION);

		// ParsedMessage msg = null;
		// Debug.startMethodTracing("Support_231");
		// long ltime = new Date().getTime();
		try {
			String result = "";
			if(mIsMulti){
				if(multiSend(messageToSend))
					result = multiRecv();
			}else
				result = startCommunication(messageToSend);

			Log.d(TAG + " - fromApp", messageToSend);
			Log.i(TAG + " - fromSrv", result);

			return new XmlParsers().parseCommunication(result, false);

		} catch (Exception e) {
			throw IhaException.wrap(e, NetworkError.COM_PROBLEMS);
		} finally {
			// Debug.stopMethodTracing();
			// ltime = new Date().getTime() - ltime;
			// android.util.Log.d("Support_231", ltime+"");
		}

	}

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////SIGNIN,SIGNUP,ADAPTERS//////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Return actual UID used for communication (= active session)
	 * @return UID for actual communication
	 */
	@Override
	public String getUID() {
		return mUserID;
	}

	/**
	 * Method load UID from server
	 * @return true if everything successful, false otherwise
	 */
	@Override
	public boolean loadUID(GoogleUserInfo googleUserInfo) {
		TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		
		String phoneId = tm.getDeviceId();
		if (phoneId == null)
			phoneId = getMAC();
		
		Log.i(TAG, String.format("HW ID (IMEI or MAC): %s", phoneId));

		ParsedMessage msg = doRequest(XmlCreator.createGetUID(googleUserInfo.id, googleUserInfo.token, Locale.getDefault().getLanguage(), phoneId));

		if (!msg.getUserId().isEmpty() && msg.getState() == State.UID) {
			mUserID = msg.getUserId();
			return true;
		}
		
		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
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
		ParsedMessage msg = doRequest(XmlCreator.createAddAdapter(mUserID, adapterID, adapterName));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
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
		ParsedMessage msg = doRequest(XmlCreator.createGetAdapters(mUserID));

		if (msg.getState() == State.ADAPTERS)
			return (List<Adapter>) msg.data;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
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
		ParsedMessage msg = doRequest(XmlCreator.createGetAllDevices(mUserID, adapterID));

		if (msg.getState() == State.ALLDEVICES)
			return (ArrayList<Facility>) msg.data;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
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
		ParsedMessage msg = doRequest(XmlCreator.createReInitAdapter(mUserID, oldId, newId));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////DEVICES,LOGS////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method send updated fields of devices
	 * 
	 * @param devices
	 * @return true if everything goes well, false otherwise
	 */
	@Override
	public boolean updateFacilities(String adapterID, List<Facility> facilities, EnumSet<SaveDevice> toSave){
		ParsedMessage msg = doRequest(XmlCreator.createSetDevs(mUserID, adapterID, facilities, toSave));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode())).set(fa.getInfo(), fa.troubleMakers);
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
		ParsedMessage msg = doRequest(XmlCreator.createSetDev(mUserID, adapterID, device, toSave));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode())).set(fa.getInfo(), fa.troubleMakers);
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
		ParsedMessage msg = doRequest(XmlCreator.createSwitch(mUserID, adapterID, device));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode())).set(fa.getInfo(), fa.troubleMakers);
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
		ParsedMessage msg = doRequest(XmlCreator.createAdapterScanMode(mUserID, adapterID));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode())).set(fa.getInfo(), fa.troubleMakers);
	}

	/**
	 * Method delete facility from server
	 * 
	 * @param adapterID
	 * @param facility
	 *            to be deleted
	 * @return true if is deleted, false otherwise
	 */
	@Override
	public boolean deleteFacility(String adapterID, Facility facility){
		ParsedMessage msg = doRequest(XmlCreator.createDeleteDevice(mUserID, adapterID, facility));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode())).set(fa.getInfo(), fa.troubleMakers);
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
		ParsedMessage msg = doRequest(XmlCreator.createGetDevices(mUserID, facilities));

		if (msg.getState() == State.DEVICES)
			return (List<Facility>) msg.data;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode())).set(fa.getInfo(), fa.troubleMakers);
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
	 * @param facilities
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Facility> getNewFacilities(String adapterID) {
		ParsedMessage msg = doRequest(XmlCreator.createGetNewDevices(mUserID, adapterID));

		if (msg.getState() == State.DEVICES)
			return (List<Facility>) msg.data;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode())).set(fa.getInfo(), fa.troubleMakers);
	}

	/**
	 * Method ask for data of logs
	 * 
	 * @param deviceId
	 *            id of wanted device
	 * @param pair
	 *            data of log (from, to, type, interval)
	 * @return list of rows with logged data
	 */
	// http://stackoverflow.com/a/509288/1642090
	@Override
	public DeviceLog getLog(String adapterID, Device device, LogDataPair pair){
		String msgToSend = XmlCreator.createGetLog(mUserID, adapterID, device.getFacility().getAddress(), device.getType().getTypeId(),
				String.valueOf(pair.interval.getStartMillis() / 1000), String.valueOf(pair.interval.getEndMillis() / 1000),
				pair.type.getValue(), pair.gap.getValue());

		ParsedMessage msg = doRequest(msgToSend);

		if (msg.getState() == State.LOGDATA) {
			DeviceLog result = (DeviceLog) msg.data;
			result.setDataInterval(pair.gap);
			result.setDataType(pair.type);
			return result;
		}
		
		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode())).set(fa.getInfo(), fa.troubleMakers);
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
		ParsedMessage msg = doRequest(XmlCreator.createGetRooms(mUserID, adapterID));

		if (msg.getState() == State.ROOMS)
			return (List<Location>) msg.data;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
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
		ParsedMessage msg = doRequest(XmlCreator.createSetRooms(mUserID, adapterID, locations));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

	/**
	 * Method call to server to update location
	 * 
	 * @param adapterID
	 * @param location
	 * @return
	 */
	@Override
	public boolean updateLocation(String adapterID, Location location){

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
	@Override
	public boolean deleteLocation(String adapterID, Location location){
		ParsedMessage msg = doRequest(XmlCreator.createDeleteRoom(mUserID, adapterID, location));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

	@Override
	public Location createLocation(String adapterID, Location location){
		ParsedMessage msg = doRequest(XmlCreator.createAddRoom(mUserID, adapterID, location));

		if (msg.getState() == State.ROOMCREATED) {
			location.setId((String) msg.data);
			return location;
		}
		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
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
	 */
	@Override
	public boolean addView(String viewName, int iconID, List<Device> devices){
		ParsedMessage msg = doRequest(XmlCreator.createAddView(mUserID, viewName, iconID, devices));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
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
		ParsedMessage msg = doRequest(XmlCreator.createGetViews(mUserID));

		if (msg.getState() == State.VIEWS)
			return (List<CustomViewPair>) msg.data;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
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
		ParsedMessage msg = doRequest(XmlCreator.createDelView(mUserID, viewName));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

	// FIXME: will be edited by ROB demands
	@Override
	public boolean updateView(String viewName, int iconId, Facility facility, NetworkAction action) {
		ParsedMessage msg = doRequest(XmlCreator.createSetView(mUserID, viewName, iconId, null, action));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////ACCOUNTS////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean addAccounts(String adapterID, ArrayList<User> users){
		ParsedMessage msg = doRequest(XmlCreator.createAddAccounts(mUserID, adapterID, users));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		String message = fa.getErrMessage() + "\n";
		for (User u : (ArrayList<User>) fa.troubleMakers){
			message += u.toDebugString();
		}
		throw new IhaException(message, NetworkError.fromValue(fa.getErrCode()));
	}

	/**
	 * Method add new user to adapter
	 * 
	 * @param adapterID
	 * @param email
	 * @param role
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
		ParsedMessage msg = doRequest(XmlCreator.createDelAccounts(mUserID, adapterID, users));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
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
		ParsedMessage msg = doRequest(XmlCreator.createGetAccounts(mUserID, adapterID));

		if (msg.getState() == State.ACCOUNTS)
			return (ArrayList<User>) msg.data;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

	/**
	 * Method update users roles on server on current adapter
	 * 
	 * @param userNrole
	 *            map with email as key and role as value
	 * @return true if all accounts has been changed false otherwise
	 */
	@Override
	public boolean updateAccounts(String adapterID, ArrayList<User> users){
		ParsedMessage msg = doRequest(XmlCreator.createSetAccounts(mUserID, adapterID, users));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

	/**
	 * Method update users role on adapter
	 * 
	 * @param adapterID
	 * @param user
	 * @param role
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
		ParsedMessage msg = doRequest(XmlCreator.createSetTimeZone(mUserID, adapterID, differenceToGMT));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

	/**
	 * Method call to server to get actual time zone
	 * 
	 * @return integer in range <-12,12>
	 */
	@Override
	public int getTimeZone(String adapterID){
		ParsedMessage msg = doRequest(XmlCreator.createGetTimeZone(mUserID, adapterID));

		if (msg.getState() == State.TIMEZONE)
			return (Integer) msg.data;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
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
	 */
	public boolean deleteGCMID(String email, String gcmID){
		ParsedMessage msg = doRequest(XmlCreator.createDeLGCMID(email, gcmID));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
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
		ParsedMessage msg = doRequest(XmlCreator.createNotificaionRead(mUserID, msgID));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

	/**
	 * Method set gcmID to server
	 * @param email of user
	 * @param gcmID to be set
	 * @return true if id has been updated, false otherwise
	 * FIXME: after merge need to by rewrite
	 */
	public boolean setGCMID(String email, String gcmID){
		ParsedMessage msg = doRequest(XmlCreator.createSetGCMID(mUserID, gcmID));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}
	
	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////CONDITIONS,ACTIONS//////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	@Override
	public Condition setCondition(Condition condition) {
		String messageToSend = XmlCreator.createAddCondition(mUserID, condition.getName(),
				XmlCreator.ConditionType.fromValue(condition.getType()), condition.getFuncs());
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.CONDITIONCREATED) {
			condition.setId((String) msg.data);
			return condition;
		}
		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

	@Override
	public boolean connectConditionWithAction(String conditionID, String actionID) {
		ParsedMessage msg = doRequest(XmlCreator.createConditionPlusAction(mUserID, conditionID, actionID));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

	@Override
	public Condition getCondition(Condition condition) {
		ParsedMessage msg = doRequest(XmlCreator.createGetCondition(mUserID, condition.getId()));

		if (msg.getState() == State.CONDITIONCREATED) {
			Condition cond = (Condition) msg.data;

			condition.setType(cond.getType());
			condition.setFuncs(cond.getFuncs());
			return condition;
		}
		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Condition> getConditions() {
		ParsedMessage msg = doRequest(XmlCreator.createGetConditions(mUserID));

		if (msg.getState() == State.CONDITIONS)
			return (List<Condition>) msg.data;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

	@Override
	public boolean updateCondition(Condition condition) {
		String messageToSend = XmlCreator.createSetCondition(mUserID, condition.getName(),
				XmlCreator.ConditionType.fromValue(condition.getType()), condition.getId(), condition.getFuncs());
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

	@Override
	public boolean deleteCondition(Condition condition) {
		ParsedMessage msg = doRequest(XmlCreator.createDelCondition(mUserID, condition.getId()));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

	@Override
	public ComplexAction setAction(ComplexAction action) {
		ParsedMessage msg = doRequest(XmlCreator.createAddAction(mUserID, action.getName(), action.getActions()));

		if (msg.getState() == State.ACTIONCREATED) {
			action.setId((String) msg.data);
			return action;
		}
		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ComplexAction> getActions() {
		ParsedMessage msg = doRequest(XmlCreator.createGetActions(mUserID));

		if (msg.getState() == State.ACTIONS)
			return (List<ComplexAction>) msg.data;
		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

	@Override
	public ComplexAction getAction(ComplexAction action) {
		ParsedMessage msg = doRequest(XmlCreator.createGetCondition(mUserID, action.getId()));

		if (msg.getState() == State.ACTION) {
			ComplexAction act = (ComplexAction) msg.data;
			action.setActions(act.getActions());
			return action;
		}
		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

	@Override
	public boolean updateAction(ComplexAction action) {
		String messageToSend = XmlCreator.createSetAction(mUserID, action.getName(), action.getId(), action.getActions());
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

	@Override
	public boolean deleteAction(ComplexAction action) {
		ParsedMessage msg = doRequest(XmlCreator.createDelAction(mUserID, action.getId()));

		if (msg.getState() == State.TRUE)
			return true;

		FalseAnswer fa = (FalseAnswer) msg.data;
		throw new IhaException(fa.getErrMessage(), NetworkError.fromValue(fa.getErrCode()));
	}

}
