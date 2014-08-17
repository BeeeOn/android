/**
 * 
 */
package cz.vutbr.fit.iha.test;

import java.util.ArrayList;

import android.test.AndroidTestCase;
import android.util.Log;
import cz.vutbr.fit.iha.activity.LoginActivity;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.network.GetGoogleAuth;
import cz.vutbr.fit.iha.network.Network;
import cz.vutbr.fit.iha.network.exception.CommunicationException;
import cz.vutbr.fit.iha.network.exception.NoConnectionException;

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
		//super("cz.vutbr.fit.iha.network");
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	//TODO: repair to 1.9(2.0)
	public void testSignIn(){
		//if(true)return;
		String emails = "email";
		
		Network net = new Network(getContext(), null);
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
	
	//TODO: repair to 1.9(2.0)
	public void testSignUp(){
		//if(true)return;
		String emails = "email";
		
		Network net = new Network(getContext(), null);
		LoginActivity la = new LoginActivity();
		GetGoogleAuth gga = new GetGoogleAuth(la, emails);
		gga.setDebugToken("token");
		
		try {
			net.signUp(USEREMAIL);
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
	
	//TODO: repair to 1.9(2.0)
	public void testGetAdapters(){
		//if(true)return; // if reply is resign, method block, because of not in new thread
		Network net = new Network(getContext(), null);
		try {
			ArrayList<Adapter> result = (ArrayList<Adapter>) net.getAdapters();
			
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

	//TODO: repair to 1.9(2.0)
	public void testInit(){
		//if(true)return; // if reply is resign, method block, because of not in new thread
		Network net = new Network(getContext(), null);
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
