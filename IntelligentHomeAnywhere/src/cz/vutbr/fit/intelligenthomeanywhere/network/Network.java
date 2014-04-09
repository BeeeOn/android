package cz.vutbr.fit.intelligenthomeanywhere.network;

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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import cz.vutbr.fit.intelligenthomeanywhere.User;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.ParsedMessage;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.XmlCreator;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.XmlParsers;
import cz.vutbr.fit.intelligenthomeanywhere.exception.ComVerMisException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.NoConnectionException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.NotImplementedException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.XmlVerMisException;

/**
 * Network service that handles communication with server.
 * 
 * @author ThinkDeep
 * @author Robyer
 */
public class Network {
	
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
	private static String startCommunication(/*Context appContext,*/ String request)
			throws IOException, CertificateException, KeyStoreException,
					NoSuchAlgorithmException, KeyManagementException,
					UnknownHostException, SSLHandshakeException
	{

		/*
		 * opening CA certificate from assets
		 */
		InputStream inStreamCertTmp = null;

		inStreamCertTmp = /*appContext*/mContext.getAssets().open(ASSEST_CA_CERT);

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

	public User signIn(String userEmail)
			throws NoConnectionException, KeyManagementException, SSLHandshakeException,
				  	CertificateException, KeyStoreException, NoSuchAlgorithmException,
				  	UnknownHostException, IOException, XmlPullParserException,
				  	ComVerMisException, XmlVerMisException 
	{
		if(!isAvailable())
			throw new NoConnectionException();
		
		//FIXME: get google token somewhere - GetGoogleAuth.mToken
		String googleToken = 54855555+"";
			
		String messageToSend = XmlCreator.createSignIn(userEmail, googleToken);
		
		String result = startCommunication(messageToSend);
		//ParsedMessage msg = XmlParsers.parseCommunication(result, false);
		
		Log.d("IHA - Network", result);
		
		return null;
	}
	
}
