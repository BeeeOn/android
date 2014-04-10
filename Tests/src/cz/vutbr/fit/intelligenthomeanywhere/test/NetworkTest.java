/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLHandshakeException;

import org.xmlpull.v1.XmlPullParserException;

import android.accounts.AccountManager;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import android.util.Log;
import cz.vutbr.fit.intelligenthomeanywhere.activity.LoginActivity;
import cz.vutbr.fit.intelligenthomeanywhere.exception.ComVerMisException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.CommunicationException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.NoConnectionException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.XmlVerMisException;
import cz.vutbr.fit.intelligenthomeanywhere.network.GetGoogleAuth;
import cz.vutbr.fit.intelligenthomeanywhere.network.Network;

/**
 * @author ThinkDeep
 *
 */
public class NetworkTest extends /*TestCase*/AndroidTestCase {

	private static final String TAG = "NetworkTests";
	private static final String USEREMAIL = "email@gmail.com";
	
	
	/**
	 * @param name
	 */
	public NetworkTest() {
		//super("cz.vutbr.fit.intelligenthomeanywhere.network");
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testSignIn(){
		
		String emails = "email";
		
		Network net = new Network(getContext());
		LoginActivity la = new LoginActivity();
		new GetGoogleAuth(la, emails);
		try {
			net.signIn(USEREMAIL);
		} catch (NoConnectionException e) {
			e.printStackTrace();
			Log.e(TAG, e.toString());
			assertTrue("NetworkTest: " + e.toString() ,false);
		} catch (CommunicationException e) {
			e.printStackTrace();
			Log.e(TAG, e.toString());
			assertTrue("NetworkTest: " + e.toString() ,false);
		}
	}

}
