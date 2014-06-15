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
import java.util.HashMap;
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
import cz.vutbr.fit.iha.User;
import cz.vutbr.fit.iha.activity.LoginActivity;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.parser.ContentRow;
import cz.vutbr.fit.iha.adapter.parser.CustomViewPair;
import cz.vutbr.fit.iha.adapter.parser.FalseAnswer;
import cz.vutbr.fit.iha.adapter.parser.ParsedMessage;
import cz.vutbr.fit.iha.adapter.parser.XmlCreator;
import cz.vutbr.fit.iha.adapter.parser.XmlParsers;
import cz.vutbr.fit.iha.exception.CommunicationException;
import cz.vutbr.fit.iha.exception.NoConnectionException;
import cz.vutbr.fit.iha.exception.NotImplementedException;
import cz.vutbr.fit.iha.exception.NotRegAException;
import cz.vutbr.fit.iha.exception.NotRegBException;
import cz.vutbr.fit.iha.listing.Location;

/**
 * Network service that handles communication with server.
 * 
 * @author ThinkDeep
 * @author Robyer
 */
public class Network {
	
	public static final String SIGNIN = "signin";
	public static final String SIGNUP = "signup";
	public static final String FALSE = "false";
	public static final String TRUE = "true";
	public static final String NOTREGA = "notreg-a";
	public static final String NOTREGB = "notreg-b";
	public static final String READY = "ready";
	public static final String RESIGN = "resign";
	public static final String XML = "xml";
	public static final String PARTIAL = "partial";
	public static final String CONTENT = "content";
	public static final String VIEWSLIST = "viewslist";
	public static final String CONACCOUNTLIST = "conaccountlist";
	public static final String TIMEZONE = "timezone";
	public static final String ROOMS = "rooms";
	
	/**
	 * Name of CA certificate located in assets
	 */
	private static final String ASSEST_CA_CERT = "cacert.crt";

	/**
	 * Alias (tag) for CA certificate
	 */
	private static final String ALIAS_CA_CERT = "ca";

	/**
	 * Address or hostName of server
	 */
	private static final String SERVER_ADDR = "ant-2.fit.vutbr.cz";

	/**
	 * Port of server
	 */
	private static final int SERVER_PORT = 4565;

	/**
	 * CN value to be verified in server certificate
	 */
	private static final String SERVER_CN_CERTIFICATE = "ant-2.fit.vutbr.cz";
	
	private static Context mContext;
	private static int mSessionId;
	
	/**
	 * Constructor.
	 * @param context
	 */
	public Network(Context context) {
		mContext = context;
	}
	
	/**
	 * Static function for sending data to server via TLS protocol using own
	 * TrustManger to be able to trust self-signed certificates. CA certificated
	 * must be located in assets folder. If no exception is thrown, it returns
	 * server response.
	 * 
	 * @param appContext
	 *            Application context to get CA certificate from assets
	 * @param request
	 *            Request to server to be sent
	 * @return Response from server
	 * @throws IOException
	 *             Can't read CA certificate from assets, can't read InputStream
	 *             or can't write OutputStream.
	 * @throws CertificateException
	 *             Unknown certificate format (default X.509), can't generate CA
	 *             certificate (it shouldn't occur)
	 * @throws KeyStoreException
	 *             Bad type of KeyStore, can't set CA certificate to KeyStore
	 * @throws NoSuchAlgorithmException
	 *             Unknown SSL/TLS protocol or unknown TrustManager algorithm
	 *             (it shouldn't occur)
	 * @throws KeyManagementException
	 *             general exception, thrown to indicate an exception during
	 *             processing an operation concerning key management
	 * @throws UnknownHostException
	 *             *IMPORTANT* Server address or hostName wasn't not found
	 * @throws SSLHandshakeException
	 *             *IMPORTANT* TLS handshake failed
	 */
	private static String startCommunication(String request)
			throws IOException, CertificateException, KeyStoreException,
					NoSuchAlgorithmException, KeyManagementException,
					UnknownHostException, SSLHandshakeException
	{

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
		SSLSocket socket = (SSLSocket) sslContext.getSocketFactory()
				.createSocket(SERVER_ADDR, SERVER_PORT);

		HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
		SSLSession s = socket.getSession();

		// Verify that the certificate hostName
		// This is due to lack of SNI support in the current SSLSocket.
		if (!hv.verify(SERVER_CN_CERTIFICATE, s)) {
			throw new SSLHandshakeException("Expected CN value:"
					+ SERVER_CN_CERTIFICATE + ", found " + s.getPeerPrincipal());
		}

		// At this point SSLSocket performed certificate verification and
		// we have performed hostName verification, so it is safe to proceed.
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
				socket.getOutputStream()));
		BufferedReader r = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));

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
	 * Checks if Internet connection is available.
	 * @return true if available, false otherwise
	 * @throws NotImplementedException
	 */
	public boolean isAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/**
	 * Method signIn user given by its email to server, BUT before calling must call GetGoogleAuth to get googleToken in it and init ActualUser
	 * @param userEmail of current user
	 * @return ActualUser
	 * @throws NoConnectionException if there is no Internet connection
	 * @throws CommunicationException if there is some problem with certificate, timeout, or other communication problem
	 * @throws NotRegAException if this user is not registered on server and on server is NO FREE ADAPTER (without its lord)
	 * @throws NotRegBException if this user is not registered on the server but there is FREE ADAPTER
	 */
	public ActualUser signIn(String userEmail) throws NoConnectionException, CommunicationException, NotRegAException, NotRegBException{
		
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String googleToken;
			
//			GetGoogleAuth.getGetGoogleAuth().execute();
			do{
				googleToken = GetGoogleAuth.getGetGoogleAuth().getToken();
				Log.d("IHA - Network - SignIn - token", googleToken);
			}while(googleToken.equalsIgnoreCase(""));
			
			String messageToSend = XmlCreator.createSignIn(userEmail, googleToken, Locale.getDefault().getLanguage());
			
			Log.d("IHA - Network - SignIn - fromApp", messageToSend);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network - SignIn - fromSrv", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getSessionId() != 0 && msg.getState().equals(TRUE) /*&& ((String)msg.data).equals(SIGNIN)*/){
			Log.d("IHA - Network", msg.getState());
			
			ActualUser aUser = ActualUser.getActualUser();
			aUser.setSessionId(Integer.toString(msg.getSessionId()));
			mSessionId = msg.getSessionId();
			
			return aUser;
		}
		if(msg.getState().equals(NOTREGA)){
			throw new NotRegAException();
		}
		if(msg.getState().equals(NOTREGB)){
			throw new NotRegBException();
		}
			
		return null;
	}
	
	/**
	 * Method sign user up to adapter with its email, serial number of adapter (user is in role superuser)
	 * @param email of registering user
	 * @param serialNumber number of adapter to register
	 * @param SessionId if is ID == 0 then needed google token, then the user is switch to work with new adapter, otherwise work with old
	 * @return true if everything goes well, false otherwise
	 * @throws CommunicationException including message from server
	 * @throws NoConnectionException
	 */
	public boolean signUp(String email, String serialNumber, int SessionId) throws CommunicationException, NoConnectionException{
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String googleToken;
			
			do{
				googleToken = GetGoogleAuth.getGetGoogleAuth().getToken();
			}while(googleToken.length() < 1);
			
			String messageToSend = XmlCreator.createSignUp(email, Integer.toString(SessionId), googleToken, serialNumber);
			
			Log.d("IHA - Network fromApp", messageToSend);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network fromSrv", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getSessionId() != 0 && msg.getState().equals(TRUE) && ((String)msg.data).equals(SIGNUP)){
			Log.d("IHA - Network", msg.getState());
			
			ActualUser aUser = ActualUser.getActualUser();
			aUser.setSessionId(Integer.toString(msg.getSessionId()));
			
			return true;
		}else if(msg.getState().equals(FALSE) && ((FalseAnswer)msg.data).getErrMessage().length() != 0){
				throw new CommunicationException(((FalseAnswer)msg.data).getErrMessage());
		}else
			return false;
	}
	
	/**
	 * Method ask for list of adapters. User has to be sign in before
	 * @return list of adapters or null
	 * @throws NoConnectionException
	 * @throws CommunicationException including message from server
	 */
	public ArrayList<Adapter> getAdapters() throws NoConnectionException, CommunicationException{
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String messageToSend = XmlCreator.createGetAdapters(mSessionId);
			
			Log.d("IHA - Network - fromApp", messageToSend);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network - fromSrv", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getState().equals(READY)){
			Log.d("IHA - Network", msg.getState());
			
			//http://stackoverflow.com/a/509288/1642090
			@SuppressWarnings("unchecked")
			ArrayList<Adapter> result = (ArrayList<Adapter>) msg.data;
			
			return result;
		}else if(msg.getState().equals(RESIGN)){
			//TODO: maybe use diffrenD way to resign, case stopping of thread, manage this after implement in the controller
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				e.printStackTrace();
				String tmp = null;
				new GetGoogleAuth(new LoginActivity(), tmp).execute();
				//return null;
			}
			signIn(ActualUser.getActualUser().getEmail());
			return getAdapters();
			
		}else if(msg.getState().equals(FALSE) && ((FalseAnswer)msg.data).getErrMessage().length() != 0){
			throw new CommunicationException(((FalseAnswer)msg.data).getErrMessage());
		}else
			return null;
	}
	
	/**
	 * Method ask for whole adapter data
	 * @param adapterId of wanted adapter
	 * @return Adapter
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public Adapter init(String adapterId) throws NoConnectionException, CommunicationException{
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String messageToSend = XmlCreator.createInit(Integer.toString(mSessionId), adapterId);
			
			Log.d("IHA - Network fromApp", messageToSend);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network fromSrv", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getState().equals(XML)){
			Log.d("IHA - Network", msg.getState());
			
			return (Adapter) msg.data;
		}else if(msg.getState().equals(RESIGN)){
			//TODO: maybe use diffrenD way to resign, case stopping of thread, manage this after implement in the controler
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				e.printStackTrace();
				String tmp = null;
				new GetGoogleAuth(new LoginActivity(), tmp).execute();
				//return null;
			}
			signIn(ActualUser.getActualUser().getEmail());
			return init(adapterId);
			
		}else if(msg.getState().equals(FALSE) && ((FalseAnswer)msg.data).getErrMessage().length() != 0){
			throw new CommunicationException(((FalseAnswer)msg.data).getErrMessage());
		}else
			return null;
	}
	
	/**
	 * Method change adapter id
	 * @param oldId id to be changed
	 * @param newId new id
	 * @return true if change has been successfully
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public boolean reInit(String oldId, String newId) throws NoConnectionException, CommunicationException{
		//TODO: test properly
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String messageToSend = XmlCreator.createReInit(Integer.toString(mSessionId), oldId, newId);
			
			Log.d("IHA - Network fromApp", messageToSend);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network fromSrv", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getState().equals(TRUE)){
			Log.d("IHA - Network", msg.getState());
			
			return true;
			
		}else if(msg.getState().equals(RESIGN)){
			//TODO: maybe use diffrenD way to resign, case stopping of thread, manage this after implement in the controler
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				e.printStackTrace();
				String tmp = null;
				new GetGoogleAuth(new LoginActivity(), tmp).execute();
				//return null;
			}
			signIn(ActualUser.getActualUser().getEmail());
			return reInit(oldId, newId);
			
		}else if(msg.getState().equals(FALSE) && ((FalseAnswer)msg.data).getErrMessage().length() != 0){
			throw new CommunicationException(((FalseAnswer)msg.data).getErrMessage());
		}else
			return false;
	}
	
	/**
	 * Method send updated fields of devices
	 * @param devices
	 * @return true if everything goes well, false otherwise
	 * @throws NoConnectionException 
	 * @throws CommunicationException 
	 */
	public boolean partial(ArrayList<BaseDevice> devices) throws NoConnectionException, CommunicationException{
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String messageToSend = XmlCreator.createPartial(Integer.toString(mSessionId), devices);
			
			Log.d("IHA - Network fromApp", messageToSend);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network fromSrv", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getState().equals(TRUE)){
			Log.d("IHA - Network", msg.getState());
			
			return true;
			
		}else if(msg.getState().equals(RESIGN)){
			//TODO: maybe use diffrenD way to resign, case stopping of thread, manage this after implement in the controler
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				e.printStackTrace();
				String tmp = null;
				new GetGoogleAuth(new LoginActivity(), tmp).execute();
				//return null;
			}
			signIn(ActualUser.getActualUser().getEmail());
			return partial(devices);
		}else if(msg.getState().equals(FALSE) && ((FalseAnswer)msg.data).getErrMessage().length() != 0){
			throw new CommunicationException(((FalseAnswer)msg.data).getErrMessage());
		}else
			return false;
	}
	
	/**
	 * Method ask for actual data devices
	 * @param devices list of devices to which needed actual data
	 * @return list of updated devices fields
	 * @throws NoConnectionException 
	 * @throws CommunicationException 
	 */
	public ArrayList<BaseDevice> update(ArrayList<BaseDevice> devices) throws NoConnectionException, CommunicationException{
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String messageToSend = XmlCreator.createUpdate(Integer.toString(mSessionId), devices);
			
			Log.d("IHA - Network fromApp", messageToSend);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network fromSrv", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getState().equals(PARTIAL)){
			Log.d("IHA - Network", msg.getState());
			
			//http://stackoverflow.com/a/509288/1642090
			@SuppressWarnings("unchecked")
			ArrayList<BaseDevice> result = (ArrayList<BaseDevice>) msg.data; 
			
			return result;
			
		}else if(msg.getState().equals(RESIGN)){
			//TODO: maybe use diffrenD way to resign, case stopping of thread, manage this after implement in the controler
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				e.printStackTrace();
				String tmp = null;
				new GetGoogleAuth(new LoginActivity(), tmp).execute();
				//return null;
			}
			signIn(ActualUser.getActualUser().getEmail());
			return update(devices);
		}else if(msg.getState().equals(FALSE) && ((FalseAnswer)msg.data).getErrMessage().length() != 0){
			throw new CommunicationException(((FalseAnswer)msg.data).getErrMessage());
		}else
			return null;
	}
	
	/**
	 * Method ask for data of logs
	 * @param deviceId id of wanted device
	 * @param from date from log begin. Based of format YYYY-MM-DD-HH:MM:SS or empty string when wanted the oldest
	 * @param to date to log end. Based of format YYYY-MM-DD-HH:MM:SS or empty string when wanted the newest
	 * @return list of rows with logged data
	 * @throws NoConnectionException 
	 * @throws CommunicationException 
	 */
	public ArrayList<ContentRow> logName(String deviceId, String from, String to) throws NoConnectionException, CommunicationException{
		// TODO: test properly
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String messageToSend = XmlCreator.createLogName(Integer.toString(mSessionId), deviceId, from, to);
			
			Log.d("IHA - Network fromApp", messageToSend);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network fromSrv", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getState().equals(CONTENT)){
			Log.d("IHA - Network", msg.getState());
			
			//http://stackoverflow.com/a/509288/1642090
			@SuppressWarnings("unchecked")
			ArrayList<ContentRow> result = (ArrayList<ContentRow>) msg.data; 
			
			return result;
			
		}else if(msg.getState().equals(RESIGN)){
			//TODO: maybe use diffrenD way to resign, case stopping of thread, manage this after implement in the controler
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				e.printStackTrace();
				String tmp = null;
				new GetGoogleAuth(new LoginActivity(), tmp).execute();
				//return null;
			}
			signIn(ActualUser.getActualUser().getEmail());
			return logName(deviceId, from, to);
			
		}else if(msg.getState().equals(FALSE) && ((FalseAnswer)msg.data).getErrMessage().length() != 0){
			throw new CommunicationException(((FalseAnswer)msg.data).getErrMessage());
		}else
			return null;
	}
	
	/**
	 * Wrapper for logName method as protocol name. That name is a little bit misleading when no name of log file
	 * is sending anymore.
	 * @param deviceId id of wanted device
	 * @param from date from log begin. Based of format YYYY-MM-DD-HH:MM:SS or empty string when wanted the oldest
	 * @param to date to log end. Based of format YYYY-MM-DD-HH:MM:SS or empty string when wanted the newest
	 * @return list of rows with logged data
	 * @throws CommunicationException 
	 * @throws NoConnectionException 
	 */
	public ArrayList<ContentRow> getLog(String deviceId, String from, String to) throws NoConnectionException, CommunicationException{
		return logName(deviceId, from, to);
	}
	
	/**
	 * Method send newly created custom view
	 * @param nameOfView name of new custom view
	 * @param iconId icon that is assigned to the new view
	 * @param deviceIds list of devices that are assigned to new view
	 * @return true if everything goes well, false otherwise 
	 * @throws NoConnectionException 
	 * @throws CommunicationException 
	 */
	public boolean addView(String nameOfView, int iconId, ArrayList<String> deviceIds) throws NoConnectionException, CommunicationException{
		// TODO: test properly
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String messageToSend = XmlCreator.createAddView(Integer.toString(mSessionId), nameOfView, iconId, deviceIds);
			
			Log.d("IHA - Network fromApp", messageToSend);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network fromSrv", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getState().equals(TRUE)){
			Log.d("IHA - Network", msg.getState());
			
			return true;
			
		}else if(msg.getState().equals(RESIGN)){
			//TODO: maybe use diffrenD way to resign, case stopping of thread, manage this after implement in the controler
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				e.printStackTrace();
				String tmp = null;
				new GetGoogleAuth(new LoginActivity(), tmp).execute();
				//return null;
			}
			signIn(ActualUser.getActualUser().getEmail());
			return addView(nameOfView, iconId, deviceIds);
			
		}else if(msg.getState().equals(FALSE) && ((FalseAnswer)msg.data).getErrMessage().length() != 0){
			throw new CommunicationException(((FalseAnswer)msg.data).getErrMessage());
		}else
			return false;
	}
	
	/**
	 * Method ask for list of all custom views
	 * @return list of defined custom views
	 * @throws NoConnectionException 
	 * @throws CommunicationException 
	 */
	public ArrayList<CustomViewPair> getViews() throws NoConnectionException, CommunicationException{
		// TODO: test properly
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String messageToSend = XmlCreator.createGetViews(Integer.toString(mSessionId));
			
			Log.d("IHA - Network fromApp", messageToSend);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network fromSrv", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getState().equals(VIEWSLIST)){
			Log.d("IHA - Network", msg.getState());
			
			//http://stackoverflow.com/a/509288/1642090
			@SuppressWarnings("unchecked")
			ArrayList<CustomViewPair> result = (ArrayList<CustomViewPair>) msg.data;
			
			return result;
			
		}else if(msg.getState().equals(RESIGN)){
			//TODO: maybe use diffrenD way to resign, case stopping of thread, manage this after implement in the controler
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				e.printStackTrace();
				String tmp = null;
				new GetGoogleAuth(new LoginActivity(), tmp).execute();
				//return null;
			}
			signIn(ActualUser.getActualUser().getEmail());
			return getViews();
			
		}else if(msg.getState().equals(FALSE) && ((FalseAnswer)msg.data).getErrMessage().length() != 0){
			throw new CommunicationException(((FalseAnswer)msg.data).getErrMessage());
		}else
			return null;
	}
	
	/**
	 * Method delete whole custom view from server
	 * @param viewName name of view to erase
	 * @return true if view has been deleted, false otherwise
	 * @throws NoConnectionException 
	 * @throws CommunicationException 
	 */
	public boolean deleteView(String viewName) throws NoConnectionException, CommunicationException{
		// TODO: test properly
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String messageToSend = XmlCreator.createDelView(Integer.toString(mSessionId), viewName);
			
			Log.d("IHA - Network fromApp", messageToSend);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network fromSrv", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getState().equals(TRUE)){
			Log.d("IHA - Network", msg.getState());
			
			return true;
			
		}else if(msg.getState().equals(RESIGN)){
			//TODO: maybe use diffrenD way to resign, case stopping of thread, manage this after implement in the controler
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				e.printStackTrace();
				String tmp = null;
				new GetGoogleAuth(new LoginActivity(), tmp).execute();
				//return null;
			}
			signIn(ActualUser.getActualUser().getEmail());
			return deleteView(viewName);
			
		}else if(msg.getState().equals(FALSE) && ((FalseAnswer)msg.data).getErrMessage().length() != 0){
			throw new CommunicationException(((FalseAnswer)msg.data).getErrMessage());
		}else
			return false;
	}
	
	/**
	 * Method update custom view.
	 * @param viewName name of view to be updated
	 * @param devices map contains device id as key and action as value action={remove, add}
	 * @return true if all devices has been updated, false otherwise
	 * @throws NoConnectionException 
	 * @throws CommunicationException 
	 */
	public boolean updateView(String viewName, int iconId, HashMap<String, String> devices) throws NoConnectionException, CommunicationException{
		//TODO: test properly
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String messageToSend = XmlCreator.createUpdateView(Integer.toString(mSessionId), viewName, iconId, devices);
			
			Log.d("IHA - Network fromApp", messageToSend);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network fromSrv", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getState().equals(TRUE)){
			Log.d("IHA - Network", msg.getState());
			
			return true;
			
		}else if(msg.getState().equals(RESIGN)){
			//TODO: maybe use diffrenD way to resign, case stopping of thread, manage this after implement in the controler
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				e.printStackTrace();
				String tmp = null;
				new GetGoogleAuth(new LoginActivity(), tmp).execute();
				//return null;
			}
			signIn(ActualUser.getActualUser().getEmail());
			return updateView(viewName, iconId, devices);
		}else if(msg.getState().equals(FALSE) && ((FalseAnswer)msg.data).getErrMessage().length() != 0){
			throw new CommunicationException(((FalseAnswer)msg.data).getErrMessage());
		}else
			return false;
	}
	
	/**
	 * Method add new users to current adapter
	 * @param userNrole map contains email as key and role as value
	 * @return true if all users has been added, false otherwise
	 * @throws NoConnectionException 
	 * @throws CommunicationException 
	 */
	public boolean addConnectionAccount(HashMap<String, String> userNrole) throws NoConnectionException, CommunicationException{
		//TODO: test properly
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String messageToSend = XmlCreator.createAddConAccount(Integer.toString(mSessionId), userNrole);
			
			Log.d("IHA - Network fromApp", messageToSend);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network fromSrv", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getState().equals(TRUE)){
			Log.d("IHA - Network", msg.getState());
			
			return true;
			
		}else if(msg.getState().equals(RESIGN)){
			//TODO: maybe use diffrenD way to resign, case stopping of thread, manage this after implement in the controler
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				e.printStackTrace();
				String tmp = null;
				new GetGoogleAuth(new LoginActivity(), tmp).execute();
				//return null;
			}
			signIn(ActualUser.getActualUser().getEmail());
			return addConnectionAccount(userNrole);
		}else if(msg.getState().equals(FALSE) && ((FalseAnswer)msg.data).getErrMessage().length() != 0){
			throw new CommunicationException(((FalseAnswer)msg.data).getErrMessage());
		}else
			return false;
	}
	
	/**
	 * Method delete users from actual adapter
	 * @param users email of user
	 * @return true if all users has been deleted, false otherwise
	 * @throws NoConnectionException 
	 * @throws CommunicationException 
	 */
	public boolean deleteConnectionAccount(ArrayList<String> users) throws NoConnectionException, CommunicationException{
		//TODO: test properly
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String messageToSend = XmlCreator.createDelConAccount(Integer.toString(mSessionId), users);
			
			Log.d("IHA - Network fromApp", messageToSend);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network fromSrv", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getState().equals(TRUE)){
			Log.d("IHA - Network", msg.getState());
			
			return true;
			
		}else if(msg.getState().equals(RESIGN)){
			//TODO: maybe use diffrenD way to resign, case stopping of thread, manage this after implement in the controler
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				e.printStackTrace();
				String tmp = null;
				new GetGoogleAuth(new LoginActivity(), tmp).execute();
				//return null;
			}
			signIn(ActualUser.getActualUser().getEmail());
			return deleteConnectionAccount(users);
		}else if(msg.getState().equals(FALSE) && ((FalseAnswer)msg.data).getErrMessage().length() != 0){
			throw new CommunicationException(((FalseAnswer)msg.data).getErrMessage());
		}else
			return false;
	}
	
	/**
	 * Method ask for list of users of current adapter
	 * @return Map of users where key is email and value is User object
	 * @throws NoConnectionException 
	 * @throws CommunicationException 
	 */
	public HashMap<String, User> getConnectionAccountList() throws NoConnectionException, CommunicationException{
		//TODO: test properly
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String messageToSend = XmlCreator.createGetConAccount(Integer.toString(mSessionId));
			
			Log.d("IHA - Network fromApp", messageToSend);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network fromSrv", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getState().equals(CONACCOUNTLIST)){
			Log.d("IHA - Network", msg.getState());
			
			//http://stackoverflow.com/a/509288/1642090
			@SuppressWarnings("unchecked")
			HashMap<String, User> result = (HashMap<String, User>) msg.data;
			
			return result;
			
		}else if(msg.getState().equals(RESIGN)){
			//TODO: maybe use diffrenD way to resign, case stopping of thread, manage this after implement in the controler
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				e.printStackTrace();
				String tmp = null;
				new GetGoogleAuth(new LoginActivity(), tmp).execute();
				//return null;
			}
			signIn(ActualUser.getActualUser().getEmail());
			return getConnectionAccountList();
			
		}else if(msg.getState().equals(FALSE) && ((FalseAnswer)msg.data).getErrMessage().length() != 0){
			throw new CommunicationException(((FalseAnswer)msg.data).getErrMessage());
		}else
			return null;
	}
	
	/**
	 * Method update users roles on server on current adapter
	 * @param userNrole map with email as key and role as value
	 * @return true if all accounts has been changed false otherwise
	 * @throws NoConnectionException 
	 * @throws CommunicationException 
	 */
	public boolean changeConnectionAccount(HashMap<String, String> userNrole) throws NoConnectionException, CommunicationException{
		//TODO: test properly
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String messageToSend = XmlCreator.createChangeConAccount(Integer.toString(mSessionId), userNrole);
			
			Log.d("IHA - Network fromApp", messageToSend);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network fromSrv", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getState().equals(TRUE)){
			Log.d("IHA - Network", msg.getState());
			
			return true;
			
		}else if(msg.getState().equals(RESIGN)){
			//TODO: maybe use diffrenD way to resign, case stopping of thread, manage this after implement in the controler
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				e.printStackTrace();
				String tmp = null;
				new GetGoogleAuth(new LoginActivity(), tmp).execute();
				//return null;
			}
			signIn(ActualUser.getActualUser().getEmail());
			return changeConnectionAccount(userNrole);
		}else if(msg.getState().equals(FALSE) && ((FalseAnswer)msg.data).getErrMessage().length() != 0){
			throw new CommunicationException(((FalseAnswer)msg.data).getErrMessage());
		}else
			return false;
	}
	
	/**
	 * Method set wanted time zone to server
	 * @NOTE using difference from GMT (UTC+0), reduced range <-12,12>
	 * @param differenceToGMT
	 * @return
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public boolean setTimeZone(int differenceToGMT) throws NoConnectionException, CommunicationException{
		//TODO: test properly
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String messageToSend = XmlCreator.createSetTimeZone(Integer.toString(mSessionId), differenceToGMT);
			
			Log.d("IHA - Network fromApp", messageToSend);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network fromSrv", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getState().equals(TRUE)){
			Log.d("IHA - Network", msg.getState());
			
			return true;
			
		}else if(msg.getState().equals(RESIGN)){
			//TODO: maybe use diffrenD way to resign, case stopping of thread, manage this after implement in the controler
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				e.printStackTrace();
				String tmp = null;
				new GetGoogleAuth(new LoginActivity(), tmp).execute();
				//return null;
			}
			signIn(ActualUser.getActualUser().getEmail());
			return setTimeZone(differenceToGMT);
			
		}else if(msg.getState().equals(FALSE) && ((FalseAnswer)msg.data).getErrMessage().length() != 0){
			throw new CommunicationException(((FalseAnswer)msg.data).getErrMessage());
		}else
			return false;
	}
	
	/**
	 * Method call to server to get actual time zone
	 * @return integer in range <-12,12>
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public int getTimeZone() throws NoConnectionException, CommunicationException{
		//TODO: test properly
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String messageToSend = XmlCreator.createGetTimeZone(Integer.toString(mSessionId));
			
			Log.d("IHA - Network fromApp", messageToSend);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network fromSrv", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getState().equals(TIMEZONE)){
			Log.d("IHA - Network", msg.getState());
			
			return (Integer)msg.data;
			
		}else if(msg.getState().equals(RESIGN)){
			//TODO: maybe use diffrenD way to resign, case stopping of thread, manage this after implement in the controler
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				e.printStackTrace();
				String tmp = null;
				new GetGoogleAuth(new LoginActivity(), tmp).execute();
				//return null;
			}
			signIn(ActualUser.getActualUser().getEmail());
			return getTimeZone();
			
		}else if(msg.getState().equals(FALSE) && ((FalseAnswer)msg.data).getErrMessage().length() != 0){
			throw new CommunicationException(((FalseAnswer)msg.data).getErrMessage());
		}else
			return 0;
	}

	/**
	 * Method call to server for actual list of locations 
	 * @return ArrayList with locations
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public ArrayList<Location> getLocations() throws NoConnectionException, CommunicationException{
		//TODO: test properly
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String messageToSend = XmlCreator.createGetRooms(Integer.toString(mSessionId));
			
			Log.d("IHA - Network fromApp", messageToSend);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network fromSrv", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getState().equals(ROOMS)){
			Log.d("IHA - Network", msg.getState());
			
			//http://stackoverflow.com/a/509288/1642090
			@SuppressWarnings("unchecked")
			ArrayList<Location> result = (ArrayList<Location>) msg.data;
			
			return result;
			
		}else if(msg.getState().equals(RESIGN)){
			//TODO: maybe use diffrenD way to resign, case stopping of thread, manage this after implement in the controler
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				e.printStackTrace();
				String tmp = null;
				new GetGoogleAuth(new LoginActivity(), tmp).execute();
				//return null;
			}
			signIn(ActualUser.getActualUser().getEmail());
			return getLocations();
			
		}else if(msg.getState().equals(FALSE) && ((FalseAnswer)msg.data).getErrMessage().length() != 0){
			throw new CommunicationException(((FalseAnswer)msg.data).getErrMessage());
		}else
			return null;
	}

	public boolean updateLocations(ArrayList<Location> locations) throws NoConnectionException, CommunicationException{
		//TODO: test properly
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String messageToSend = XmlCreator.createUpdateRooms(Integer.toString(mSessionId), locations);
			
			Log.d("IHA - Network fromApp", messageToSend);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network fromSrv", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getState().equals(TRUE)){
			Log.d("IHA - Network", msg.getState());
			
			return true;
			
		}else if(msg.getState().equals(RESIGN)){
			//TODO: maybe use diffrenD way to resign, case stopping of thread, manage this after implement in the controler
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				e.printStackTrace();
				String tmp = null;
				new GetGoogleAuth(new LoginActivity(), tmp).execute();
				//return null;
			}
			signIn(ActualUser.getActualUser().getEmail());
			return updateLocations(locations);
			
		}else if(msg.getState().equals(FALSE) && ((FalseAnswer)msg.data).getErrMessage().length() != 0){
			throw new CommunicationException(((FalseAnswer)msg.data).getErrMessage());
		}else
			return false;
	}

	//TODO: GetAlerts
	//TODO: Alerts
}
