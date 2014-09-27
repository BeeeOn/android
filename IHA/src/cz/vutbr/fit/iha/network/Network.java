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
import android.util.Log;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.device.BaseDevice.SaveDevice;
import cz.vutbr.fit.iha.adapter.device.DeviceLog;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataInterval;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataType;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.gcm.GcmHelper;
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
import cz.vutbr.fit.iha.network.xml.XmlCreator;
import cz.vutbr.fit.iha.network.xml.XmlParsers;
import cz.vutbr.fit.iha.network.xml.XmlParsers.State;

/**
 * Network service that handles communication with server.
 * 
 * @author ThinkDeep
 * @author Robyer
 */
public class Network {

	/**
	 * Action of View messages
	 * 
	 * @author ThinkDeep
	 * 
	 */
	public enum NetworkAction {
		REMOVE("remove"), ADD("add");

		private final String mAction;

		private NetworkAction(String action) {
			mAction = action;
		}

		public String getValue() {
			return mAction;
		}

		public static NetworkAction fromValue(String value) {
			for (NetworkAction item : values()) {
				if (value.equalsIgnoreCase(item.getValue()))
					return item;
			}
			throw new IllegalArgumentException("Invalid NetworkAction value");
		}
	}

	private static final String TAG = Network.class.getSimpleName();

	public static final String SIGNIN = "signin";
	public static final String SIGNUP = "signup";

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
	private static final int SERVER_PORT_DEBUG = 4565;

	/**
	 * Address and port of production server
	 */
	private static final String SERVER_ADDR_PRODUCTION = "ant-2.fit.vutbr.cz";
	private static final int SERVER_PORT_PRODUCTION = 4566;

	/**
	 * CN value to be verified in server certificate
	 */
	private static final String SERVER_CN_CERTIFICATE = "ant-2.fit.vutbr.cz";

	private static final String GoogleExcMessage = "Google token error";

	private Context mContext;
	private GoogleAuth mGoogleAuth;
	private ActualUser mUser;
	private String mSessionId;
	private boolean mUseDebugServer;
	private boolean mGoogleReinit;

	/**
	 * Constructor.
	 * 
	 * @param context
	 */
	public Network(Context context, ActualUser user, boolean useDebugServer) {
		mContext = context;
		mUser = user;
		mUseDebugServer = useDebugServer;
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
//		Log.i(TAG, ""+socket.set);
		SSLSession s = socket.getSession();
		//FIXME
		if (!s.isValid())
			Log.e("Network", "sslshiiit");

		// Verify that the certificate hostName
		// This is due to lack of SNI support in the current SSLSocket.
		if (!hv.verify(SERVER_CN_CERTIFICATE, s)) {
			Log.e("Network", "rict pavlovi ze to opravil :)");
			
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

	/**
	 * Method check background data under API 14
	 * 
	 * @see APP works without demo data on (2.3.4 tested)
	 * @return true if is allowed
	 */
	@SuppressWarnings("deprecation")
	public boolean checkBackgroundData() {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		return connectivityManager.getBackgroundDataSetting();
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
		signIn(mUser.getEmail(), GcmHelper.getGCMRegistrationId(mContext)); //FIXME: gcmid
	}

	private ParsedMessage doRequest(String messageToSend) {
		// NOTE: This is not needed anymore
		if (!checkBackgroundData())
			Log.e("Network", "backgrounddata");

		if (!isAvailable())
			throw new NoConnectionException();

		ParsedMessage msg = null;

		try {
			String result = startCommunication(messageToSend);
			Log.d(TAG + " - fromApp", messageToSend);

			Log.d(TAG + " - fromSrv", result);

			msg = new XmlParsers().parseCommunication(result, false);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}

		return msg;
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
				Log.d(TAG + " - SignIn - token", googleToken);
			} while (googleToken.equalsIgnoreCase(""));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return googleToken;
	}

	// //////////////////////////////////////// prihlaseni, registrace adaptery

	/**
	 * Method signIn user given by its email to server, BUT before calling must call GoogleAuth to get googleToken in it and init ActualUser
	 * 
	 * @param userEmail
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
	public boolean signIn(String userEmail, String gcmid) throws NoConnectionException, CommunicationException, NotRegAException, NotRegBException, FalseException {
		String googleToken = getGoogleToken();
		if (googleToken.length() == 0)
			throw new CommunicationException(GoogleExcMessage);

		String messageToSend = XmlCreator.createSignIn(userEmail, googleToken, Locale.getDefault().getLanguage(), gcmid);
		ParsedMessage msg = doRequest(messageToSend);

		if (!msg.getSessionId().isEmpty() && msg.getState() == State.TRUE && ((String) msg.data).equals(SIGNIN)) {
			Log.i(TAG, msg.getState().getValue());

			mUser.setSessionId(msg.getSessionId());
			mSessionId = msg.getSessionId();

			return true;
		}
		if (msg.getState() == State.NOTREGA) {
			throw new NotRegAException();
		}
		if (msg.getState() == State.NOTREGB) {
			throw new NotRegBException();
		}
		if (msg.getState() == State.FALSE) {
			mGoogleAuth.invalidateToken();
			throw new FalseException(((FalseAnswer) msg.data));
		}

		return false;
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
	public boolean signUp(String email) throws CommunicationException, NoConnectionException, FalseException {

		String googleToken = getGoogleToken();

		if (googleToken.length() == 0)
			throw new CommunicationException(GoogleExcMessage);

		String messageToSend = XmlCreator.createSignUp(email, googleToken);
		ParsedMessage msg = doRequest(messageToSend);

		if (!msg.getSessionId().isEmpty() && msg.getState() == State.TRUE && ((String) msg.data).equals(SIGNUP)) {
			Log.i(TAG, msg.getState().getValue());

			mUser.setSessionId(msg.getSessionId());
			mSessionId = msg.getSessionId();

			return true;
		}
		if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
	}

	/**
	 * Method register adapter to server
	 * 
	 * @param serialNumber
	 *            adapter id
	 * @param adapterName
	 *            adapter name
	 * @return true if adapter has been registered, false otherwise
	 */
	public boolean addAdapter(String serialNumber, String adapterName) {
		String messageToSend = XmlCreator.createAddAdapter(mSessionId, serialNumber, adapterName);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.i(TAG, msg.getState().getValue());

			return true;
		}
		if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
	}

	/**
	 * Method ask for list of adapters. User has to be sign in before
	 * 
	 * @return list of adapters or null
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 *             including message from server including message from server
	 */
	// http://stackoverflow.com/a/509288/1642090
	@SuppressWarnings("unchecked")
	public List<Adapter> getAdapters() throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createGetAdapters(mSessionId);
		ParsedMessage msg = doRequest(messageToSend);

		List<Adapter> result = new ArrayList<Adapter>();

		if (msg.getState() == State.ADAPTERSREADY) {
			Log.i(TAG, msg.getState().getValue());

			result.addAll((List<Adapter>) msg.data);
		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return getAdapters();

		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		}
		return result;
	}

	/**
	 * Method ask for whole adapter data
	 * 
	 * @param adapterId
	 *            of wanted adapter
	 * @return Adapter
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	@SuppressWarnings("unchecked")
	public List<Facility> init(String adapterId) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createGetAllDevices(mSessionId, adapterId);
		ParsedMessage msg = doRequest(messageToSend);
		ArrayList<Facility> result = new ArrayList<Facility>();

		if (msg.getState() == State.ALLDEVICES) {
			Log.i(TAG, msg.getState().getValue());

			result = (ArrayList<Facility>) msg.data;
		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return init(adapterId);
		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		}
		return result;
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
		String messageToSend = XmlCreator.createReInitAdapter(mSessionId, oldId, newId);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.i(TAG, msg.getState().getValue());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return reInitAdapter(oldId, newId);

		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
	}

	// ////////////////////////////////////// zarizeni, logy

	/**
	 * Method send updated fields of devices
	 * 
	 * @param devices
	 * @return true if everything goes well, false otherwise
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public boolean setDevices(String adapterId, List<BaseDevice> devices) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createDevices(mSessionId, adapterId, devices);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().getValue());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return setDevices(adapterId, devices);
		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
	}

	/**
	 * Method send wanted fields of device to server
	 * 
	 * @param adapterId
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
	public boolean setDevice(String adapterId, BaseDevice device, EnumSet<SaveDevice> toSave) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createDevice(mSessionId, adapterId, device, toSave);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().getValue());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return setDevice(adapterId, device, toSave);
		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
	}

	/**
	 * Method toggle or set actor to new value
	 * 
	 * @param adapterId
	 * @param device
	 * @return
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	public boolean switchState(String adapterId, BaseDevice device) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createSwitch(mSessionId, adapterId, device);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().getValue());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return switchState(adapterId, device);
		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
	}

	/**
	 * Method make adapter to special state, when listen for new sensors (e.g. 15s) and wait if some sensors has been shaken to connect
	 * 
	 * @param adapterId
	 * @return
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	public boolean prepareAdapterToListenNewSensors(String adapterId) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createAdapterListen(mSessionId, adapterId);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().getValue());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return prepareAdapterToListenNewSensors(adapterId);
		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;

	}

	/**
	 * Method delete facility from server
	 * 
	 * @param adapterId
	 * @param facility
	 *            to be deleted
	 * @return true if is deleted, false otherwise
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	public boolean deleteFacility(String adapterId, Facility facility) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createDeleteFacility(mSessionId, adapterId, facility);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().getValue());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return deleteFacility(adapterId, facility);
		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
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
	public List<Facility> getFacilities(String adapterId, List<Facility> facilities) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createGetDevices(mSessionId, adapterId, facilities);
		ParsedMessage msg = doRequest(messageToSend);

		List<Facility> result = new ArrayList<Facility>();

		if (msg.getState() == State.DEVICES) {
			Log.i(TAG, msg.getState().getValue());

			result.addAll((List<Facility>) msg.data);
		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return getFacilities(adapterId, facilities);
		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		}
		return result;
	}

	/**
	 * Method ask server for actual data of one facility
	 * 
	 * @param adapterId
	 * @param facility
	 * @return
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	public Facility getFacility(String adapterId, Facility facility) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createGetDevice(mSessionId, adapterId, facility);
		ParsedMessage msg = doRequest(messageToSend);

		Facility result = null;

		if (msg.getState() == State.DEVICES) {
			Log.i(TAG, msg.getState().getValue());

			@SuppressWarnings("unchecked")
			List<Facility> devices = (List<Facility>) msg.data;
			if (devices.size() != 0)
				result = devices.get(0);
		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return getFacility(adapterId, facility);
		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		}
		return result;
	}

	/**
	 * TODO: need to test
	 * 
	 * @param adapterId
	 * @param facilities
	 * @return
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	@SuppressWarnings("unchecked")
	public List<Facility> getNewFacilities(String adapterId) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createGetNewDevices(mSessionId, adapterId);
		ParsedMessage msg = doRequest(messageToSend);

		List<Facility> result = new ArrayList<Facility>();

		if (msg.getState() == State.DEVICES) {
			Log.i(TAG, msg.getState().getValue());

			result.addAll((List<Facility>) msg.data);
		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return getNewFacilities(adapterId);
		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		}
		return result;
	}

	/**
	 * Method ask for data of logs
	 * 
	 * @param deviceId
	 *            id of wanted device
	 * @param from
	 *            date from log begin. Based of format YYYY-MM-DD-HH:MM:SS or empty string when wanted the oldest
	 * @param to
	 *            date to log end. Based of format YYYY-MM-DD-HH:MM:SS or empty string when wanted the newest
	 * @return list of rows with logged data
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	// http://stackoverflow.com/a/509288/1642090
	public DeviceLog getLog(String adapterId, BaseDevice device, String from, String to, DataType type, DataInterval interval) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createGetLog(mSessionId, adapterId, device.getFacility().getAddress(), device.getType(), from, to, type.getValue(), interval.getValue());

		ParsedMessage msg = doRequest(messageToSend);

		DeviceLog result = null;

		if (msg.getState() == State.LOGDATA) {
			Log.i(TAG, msg.getState().getValue());

			result = (DeviceLog) msg.data;
			result.setDataInterval(interval);
			result.setDataType(type);

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return getLog(adapterId, device, from, to, type, interval);

		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		}

		return result;
	}

	// //////////////////////////////////////// mistnosti

	/**
	 * Method call to server for actual list of locations
	 * 
	 * @return List with locations
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	// http://stackoverflow.com/a/509288/1642090
	@SuppressWarnings("unchecked")
	public List<Location> getLocations(String adapterId) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createGetRooms(mSessionId, adapterId);
		ParsedMessage msg = doRequest(messageToSend);

		List<Location> result = new ArrayList<Location>();

		if (msg.getState() == State.ROOMS) {
			Log.i(TAG, msg.getState().getValue());

			result.addAll((List<Location>) msg.data);
		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return getLocations(adapterId);

		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		}
		return result;
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
	public boolean updateLocations(String adapterId, List<Location> locations) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createUpdateRooms(mSessionId, adapterId, locations);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().getValue());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return updateLocations(adapterId, locations);

		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
	}

	/**
	 * Method call to server to update location
	 * 
	 * @param adapterId
	 * @param location
	 * @return
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	public boolean updateLocation(String adapterId, Location location) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createUpdateRoom(mSessionId, adapterId, location);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().getValue());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return updateLocation(adapterId, location);

		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
	}

	/**
	 * Method call to server and delete location
	 * 
	 * @param location
	 *            to delete
	 * @return true room is deleted, false otherwise
	 */
	public boolean deleteLocation(String adapterId, Location location) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createDelRooms(mSessionId, adapterId, location);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().getValue());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return deleteLocation(adapterId, location);

		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
	}

	public Location createLocation(String adapterId, Location location) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createAddRooms(mSessionId, adapterId, location);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.ROOMCREATED) {
			Log.i(TAG, msg.getState().getValue());

			location.setId((String) msg.data);
		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return createLocation(adapterId, location);

		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		}
		return location;
	}

	// /////////////////////////////////////// pohledy

	/**
	 * Method send newly created custom view
	 * 
	 * @param nameOfView
	 *            name of new custom view
	 * @param iconId
	 *            icon that is assigned to the new view
	 * @param deviceIds
	 *            list of devices that are assigned to new view
	 * @return true if everything goes well, false otherwise
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public boolean addView(String nameOfView, int iconId, List<BaseDevice> devices) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createAddView(mSessionId, nameOfView, iconId, devices);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().getValue());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return addView(nameOfView, iconId, devices);

		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
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
	public List<CustomViewPair> getViews() throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createGetViews(mSessionId);
		ParsedMessage msg = doRequest(messageToSend);

		List<CustomViewPair> result = new ArrayList<CustomViewPair>();

		if (msg.getState() == State.VIEWSLIST) {
			Log.i(TAG, msg.getState().getValue());

			result.addAll((List<CustomViewPair>) msg.data);
		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return getViews();

		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		}
		return result;
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
		String messageToSend = XmlCreator.createDelView(mSessionId, viewName);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().getValue());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return deleteView(viewName);

		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
	}

	/**
	 * Method update custom view.
	 * 
	 * @param viewName
	 *            name of view to be updated
	 * @param devices
	 *            map contains device id as key and action as value action={remove, add}
	 * @return true if all devices has been updated, false otherwise
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public boolean updateViews(String viewName, int iconId, HashMap<String, String> devices) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createUpdateViews(mSessionId, viewName, iconId, devices);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().getValue());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return updateViews(viewName, iconId, devices);
		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
	}

	public boolean updateView(String viewName, int iconId, Facility facility, NetworkAction action) {
		String messageToSend = XmlCreator.createUpdateView(mSessionId, viewName, iconId, facility, action);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().getValue());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return updateView(viewName, iconId, facility, action);
		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
	}

	// ///////////////////////////////////////// ucty

	/**
	 * Method add new users to current adapter
	 * 
	 * @param userNrole
	 *            map contains email as key and role as value
	 * @return true if all users has been added, false otherwise
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public boolean addConnectionAccounts(String adapterId, HashMap<String, String> userNrole) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createAddAccounts(mSessionId, adapterId, userNrole);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().getValue());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return addConnectionAccounts(adapterId, userNrole);
		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
	}

	/**
	 * Method add new user to adapter
	 * 
	 * @param adapterId
	 * @param email
	 * @param role
	 * @return
	 */
	public boolean addConnectionAccount(String adapterId, String email, User.Role role) {
		String messageToSend = XmlCreator.createAddAccount(mSessionId, adapterId, email, role);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().getValue());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return addConnectionAccount(adapterId, email, role);
		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
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
	public boolean deleteConnectionAccounts(String adapterId, List<String> users) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createDelAccounts(mSessionId, adapterId, users);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().getValue());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return deleteConnectionAccounts(adapterId, users);
		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
	}

	/**
	 * Method delete on user from adapter
	 * 
	 * @param adapterId
	 * @param user
	 * @return
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	public boolean deleteConnectionAccount(String adapterId, User user) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createDelAccount(mSessionId, adapterId, user);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().getValue());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return deleteConnectionAccount(adapterId, user);
		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
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
	public HashMap<String, User> getConnectionAccountList(String adapterId) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createGetAccount(mSessionId, adapterId);
		ParsedMessage msg = doRequest(messageToSend);

		HashMap<String, User> result = new HashMap<String, User>();

		if (msg.getState() == State.ACCOUNTSLIST) {
			Log.i(TAG, msg.getState().toString());

			result.putAll((HashMap<String, User>) msg.data);
		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return getConnectionAccountList(adapterId);

		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		}
		return result;
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
	public boolean changeConnectionAccounts(String adapterId, HashMap<String, String> userNrole) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createUpdateAccounts(mSessionId, adapterId, userNrole);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().toString());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return changeConnectionAccounts(adapterId, userNrole);
		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
	}

	/**
	 * Method update users role on adapter
	 * 
	 * @param adapterId
	 * @param user
	 * @param role
	 * @return
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	public boolean changeConnectionAccount(String adapterId, User user, User.Role role) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createUpdateAccount(mSessionId, adapterId, user, role);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().toString());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return changeConnectionAccount(adapterId, user, role);
		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
	}

	// //////////////////////////////////////// cas

	/**
	 * Method set wanted time zone to server
	 * 
	 * @NOTE using difference from GMT (UTC+0), https://merlin.fit.vutbr.cz/wiki-iot/index.php/Smarthome_cloud#SetTimeZone
	 * @param differenceToGMT
	 * @return
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public boolean setTimeZone(String adapterId, int differenceToGMT) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createSetTimeZone(mSessionId, adapterId, differenceToGMT);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().getValue());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return setTimeZone(adapterId, differenceToGMT);

		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
	}

	/**
	 * Method call to server to get actual time zone
	 * 
	 * @return integer in range <-12,12>
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public int getTimeZone(String adapterId) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createGetTimeZone(mSessionId, adapterId);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TIMEZONE) {
			Log.i(TAG, msg.getState().getValue());

			return (Integer) msg.data;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return getTimeZone(adapterId);

		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return 0;
	}

	// /////////////////////////////////////////// notifikace

	/**
	 * Method delete old gcmid to avoid fake notifications
	 * 
	 * @param email
	 *            of old/last user of gcmid (app+device id)
	 * @param gcmid
	 *            - google cloud message id
	 * @return true if id has been deleted, false otherwise
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	public boolean deleteGCMID(String email, String gcmid) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createDeLGCMID(mSessionId, email, gcmid);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().getValue());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return deleteGCMID(email, gcmid);

		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
	}

	/**
	 * Method set read flag to notification on server
	 * 
	 * @param msgid
	 *            id of notification
	 * @return true if server took flag, false otherwise
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 * @throws FalseException
	 */
	public boolean NotificationRead(String msgid) throws NoConnectionException, CommunicationException, FalseException {
		String messageToSend = XmlCreator.createNotificaionRead(mSessionId, msgid);
		ParsedMessage msg = doRequest(messageToSend);

		if (msg.getState() == State.TRUE) {
			Log.d(TAG, msg.getState().getValue());

			return true;

		} else if (msg.getState() == State.RESIGN) {
			doResign();
			return NotificationRead(msgid);

		} else if (msg.getState() == State.FALSE) {
			throw new FalseException(((FalseAnswer) msg.data));
		} else
			return false;
	}

}
