/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.test;

import java.util.ArrayList;

import android.test.AndroidTestCase;
import android.util.Log;
import cz.vutbr.fit.intelligenthomeanywhere.activity.LoginActivity;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;
import cz.vutbr.fit.intelligenthomeanywhere.exception.CommunicationException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.NoConnectionException;
import cz.vutbr.fit.intelligenthomeanywhere.network.GetGoogleAuth;
import cz.vutbr.fit.intelligenthomeanywhere.network.Network;

/**
 * @author ThinkDeep
 *
 */
public class NetworkTest extends /*TestCase*/AndroidTestCase {

	private static final String TAG = "NetworkTests";
	private static final String USEREMAIL = "email@gmail.com";
	private static final String SERIAL = "445558745";
	
	
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
		//if(true)return;
		String emails = "email";
		
		Network net = new Network(getContext());
		LoginActivity la = new LoginActivity();
		GetGoogleAuth gga = new GetGoogleAuth(la, emails);
		gga.setDebugToken("token");
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
	
	public void testSignUp(){
		//if(true)return;
		String emails = "email";
		
		Network net = new Network(getContext());
		LoginActivity la = new LoginActivity();
		GetGoogleAuth gga = new GetGoogleAuth(la, emails);
		gga.setDebugToken("token");
		
		try {
			net.signUp(USEREMAIL, SERIAL, 0);
		} catch (CommunicationException e) {
			e.printStackTrace();
			Log.e(TAG, e.toString());
			assertTrue("NetworkTest: " + e.toString() ,false);
		} catch (NoConnectionException e) {
			e.printStackTrace();
			Log.e(TAG, e.toString());
			assertTrue("NetworkTest: " + e.toString() ,false);
		}
	}
	
	public void testGetAdapters(){
		//if(true)return; // if reply is resign, method block, because of not in new thread
		Network net = new Network(getContext());
		try {
			ArrayList<Adapter> result = net.getAdapters();
			
			if(result != null){
				for(Adapter adapter : result){
					Log.d(TAG, "Id:"+adapter.getId());
					Log.d(TAG, "name:"+adapter.getName());
					Log.d(TAG, "role:"+adapter.getRole());
				}
			}
			
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
	
	public void testInit(){
		//if(true)return; // if reply is resign, method block, because of not in new thread
		Network net = new Network(getContext());
		try {
			Adapter result = net.init("5844569");
			
			if(result != null){
				Log.d(TAG, result.toDebugString());
			}
			
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
