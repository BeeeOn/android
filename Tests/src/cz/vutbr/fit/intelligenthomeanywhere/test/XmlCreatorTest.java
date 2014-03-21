/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.test;

import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.TestCase;
import android.util.Log;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.EmissionDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.HumidityDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.IlluminationDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.NoiseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.PressureDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.SwitchDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.XmlCreator;

/**
 * @author ThinkDeep
 *
 */
public class XmlCreatorTest extends TestCase {
	
	private XmlCreator mCreator;
	
	private static String TAG = "XmlCreatorTest";
	private static String EMAIL = "testmail@domain.com";
	private static String GTOKEN = "9845658";
	private static int ID = 425644;
	private static String GVERSION = "1.0";
	//FIXME: length of serial???
	private static int SERIAL = 555645792;
	private static int ADAPTERID = SERIAL;
	private static int ADAPTERIDOLD = ADAPTERID++;
	private static String DEVICEID = "120:07:ff:000:ffe";
	private static String FROM = "2013-08-28-10:00:00";
	private static String TO = "2013-08-28-10:09:00";
	private static String ADDITIONALINFO = "changeconaccount";
	private static String XML = "something";
	private static String VIEWNAME = "custom_name";
	
	//messages
	private static String SIGNIN_1 = "<?xml version='1.0' encoding='UTF-8' ?><communication id=\"0\" state=\"signin\" version=\""+GVERSION+"\"><user email=\"";
	private static String SINGIN_1_2 = "\" gtoken=\"";
	private static String SIGNIN_2 = "\" /></communication>";
	private static String SIGNUP_1 = "<?xml version='1.0' encoding='UTF-8' ?><communication id=\"";
	private static String SIGNUP_2 = "\" state=\"signup\" version=\""+GVERSION+"\"><serialnumber>";
	private static String SIGNUP_3_USER_OLD = "</serialnumber><next adapter=\"old\" /></communication>";
	private static String SIGNUP_3_USER_NEW = "</serialnumber><next adapter=\"new\" /></communication>";
	private static String SIGNUP_3_EMAIL_1 = "</serialnumber><user email=\"";
	private static String SIGNUP_3_EMAIL_1_2 = SINGIN_1_2;
	private static String SIGNUP_3_EMAIL_2 =  SIGNIN_2;
	private static String INIT_1 = SIGNUP_1;
	private static String INIT_2 = "\" state=\"init\" version=\""+GVERSION+"\"><adapter id=\"";
	private static String INIT_3 = SIGNIN_2;
	private static String REINIT_1 = SIGNUP_1;
	private static String REINIT_2 = "\" state=\"reinit\" version=\""+GVERSION+"\"><adapter oldid=\"";
	private static String REINIT_3 = "\" newid=\"";
	private static String REINIT_4 = SIGNIN_2;
	private static String LOGNAME_1 = SIGNUP_1;
	private static String LOGNAME_2 = "\" state=\"logname\" version=\""+GVERSION+"\" from=\"";
	private static String LOGNAME_3 = "\" to=\"";
	private static String LOGNAME_4 = "\"><device id=\"";
	private static String LOGNAME_5 = SIGNIN_2;
	private static String ADDCONACCOUNT_1 = SIGNUP_1;
	private static String ADDCONACCOUNT_2 = "\" state=\"addconaccount\" version=\""+GVERSION+"\"><user email=\"";
	private static String ADDCONACCOUNT_2_2 = "\" /><user email=\"";
	private static String ADDCONACCOUNT_3 = "\" role=\"";
	private static String ADDCONACCOUNT_4 = SIGNIN_2;
	private static String DELCONACCOUNT_1 = SIGNUP_1;
	private static String DELCONACCOUNT_2 = "\" state=\"delconaccount\" version=\""+GVERSION+"\"><user email=\"";
	private static String DELCONACCOUNT_2_2 = "\" /><user email=\"";
	private static String DELCONACCOUNT_3 = SIGNIN_2;
	private static String GETCONACCOUNT_1 = SIGNUP_1;
	private static String GETCONACCOUNT_2 = "\" state=\"getconaccount\" version=\""+GVERSION+"\" />";
	private static String CHANGECONACCOUNT_1 = SIGNUP_1;
	private static String CHANGECONACCOUNT_2 = "\" state=\"changeconaccount\" version=\""+GVERSION+"\"><user email=\"";
	private static String CHANGECONACCOUNT_2_2 = ADDCONACCOUNT_2_2;
	private static String CHANGECONACCOUNT_3 = ADDCONACCOUNT_3;
	private static String CHANGECONACCOUNT_4 = SIGNIN_2;
	private static String TRUE_1 = SIGNUP_1;
	private static String TRUE_2 = "\" state=\"true\" version=\""+GVERSION+"\" additionalinfo=\"";
	private static String TRUE_3 = "\" />";
	private static String FALSE_1 = SIGNUP_1;
	private static String FALSE_2 = "\" state=\"false\" version=\""+GVERSION+"\" additionalinfo=\"";
	private static String FALSE_3 = "\">";
	private static String FALSE_4 = "</communication>";
	private static String GETADAPTERS_1 = SIGNUP_1;
	private static String GETADAPTERS_2 = "\" state=\"getadapters\" version=\""+GVERSION+"\" />";
	private static String UPDATE_1 = SIGNUP_1;
	private static String UPDATE_2 = "\" state=\"update\" version=\""+GVERSION+"\"><device id=\"";
	private static String UPDATE_2_2 = "\" /><device id=\"";
	private static String Update_3 = SIGNIN_2;
	private static String ADDVIEW_1 = SIGNUP_1;
	private static String ADDVIEW_2 = "\" state=\"addview\" version=\""+GVERSION+"\" name=\"";
	private static String ADDVIEW_3 = "\"><device id=\"";
	private static String ADDVIEW_3_3 = "\" /><device id=\"";
	private static String ADDVIEW_4 = SIGNIN_2;
	private static String DELVIEW_1 = SIGNUP_1;
	private static String DELVIEW_2 = "\" state=\"delview\" version=\""+GVERSION+"\" name=\"";
	private static String DELVIEW_3 = TRUE_3;
	private static String UPDATEVIEW_1 = SIGNUP_1;
	private static String UPDATEVIEW_2 = "\" state=\"updateview\" version=\""+GVERSION+"\" name=\"";
	private static String UPDATEVIEW_3 = "\"><device id=\"";
	private static String UPDATEVIEW_4 = "\" action=\"";
	private static String UPDATEVIEW_4_2 = "\" /><device id=\"";
	private static String UPDATEVIEW_5 = SIGNIN_2;
	
	private static String PARTIAL_ALL = "<?xml version='1.0' encoding='UTF-8' ?><communication id=\""+ID+"\" state=\"partial\" version=\""+GVERSION+"\">"
			+ "<device initialized=\"1\" type=\"0x7\" id=\"120:07:ff:000:ffeem\" visibility=\"i\"><location type=\"1\">obyvak</location><logging endabled=\"0\" /></device>"
			+ "<device initialized=\"1\" type=\"0x1\" id=\"120:07:ff:000:ffehu\" visibility=\"x\"><name>vlhkomer</name><logging endabled=\"0\" /></device>"
			+ "<device initialized=\"1\" type=\"0x5\" id=\"120:07:ff:000:ffeil\" visibility=\"x\"><refresh>5</refresh><logging endabled=\"0\" /></device>"
			+ "<device initialized=\"1\" type=\"0x4\" id=\"120:07:ff:000:ffesw\" visibility=\"i\"><logging endabled=\"0\" /></device>"
			+ "<device initialized=\"1\" type=\"0x6\" id=\"120:07:ff:000:ffeno\" visibility=\"i\"><logging endabled=\"1\" /></device>"
			+ "<device initialized=\"0\" type=\"0x2\" id=\"120:07:ff:000:ffepr\" visibility=\"o\"><value>50</value><logging endabled=\"0\" /></device></communication>";
	
 	public XmlCreatorTest() {
		super("cz.vutbr.fit.intelligenthomeanywhere.parser");
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mCreator = new XmlCreator(null);
	}

	public void testSignIn(){
		String result = mCreator.createSignIn(EMAIL, GTOKEN);
		String goal = SIGNIN_1+EMAIL+SINGIN_1_2+GTOKEN+SIGNIN_2;
		
		Log.i(TAG, "SignInTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("SignInTest1: messages are not equal",result.equals(goal));
	}
	
	public void testSignUp(){
		String result = mCreator.createSignUp(EMAIL, ID, GTOKEN, SERIAL, false);
		String goal = SIGNUP_1+ID+SIGNUP_2+SERIAL+SIGNUP_3_USER_OLD;
		
		Log.i(TAG, "SignUpTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("SignUpTest1: messages are not equal",result.equals(goal));
		
		result = mCreator.createSignUp(EMAIL, ID, GTOKEN, SERIAL, true);
		goal = SIGNUP_1+ID+SIGNUP_2+SERIAL+SIGNUP_3_USER_NEW;
		
		Log.i(TAG, "SignUpTest2");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("SignUpTest2: messages are not equal",result.equals(goal));
		
		result = mCreator.createSignUp(EMAIL, 0, GTOKEN, SERIAL, false);
		goal = SIGNUP_1+0+SIGNUP_2+SERIAL+SIGNUP_3_EMAIL_1+EMAIL+SIGNUP_3_EMAIL_1_2+GTOKEN+SIGNUP_3_EMAIL_2;
		
		Log.i(TAG, "SignUpTest3");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("SignUpTest3: messages are not equal",result.equals(goal));
	}
	
	public void testInit(){
		String result = mCreator.createInit(ID, ADAPTERID);
		String goal = INIT_1+ID+INIT_2+ADAPTERID+INIT_3; 
		
		Log.i(TAG, "InitTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("InitTest1: messages are not equal",result.equals(goal));
	}
	
	public void testReInit(){
		String result = mCreator.createReInit(ID,ADAPTERIDOLD,ADAPTERID);
		String goal = REINIT_1+ID+REINIT_2+ADAPTERIDOLD+REINIT_3+ADAPTERID+REINIT_4;
		
		Log.i(TAG, "ReInitTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("ReInitTest1: messages are not equal",result.equals(goal));
	}
	
	public void testLogName(){
		String result = mCreator.createLogName(ID, DEVICEID, FROM, TO);
		String goal = LOGNAME_1+ID+LOGNAME_2+FROM+LOGNAME_3+TO+LOGNAME_4+DEVICEID+LOGNAME_5;
		
		Log.i(TAG, "LogNameTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("LogNameTest1: messages are not equal",result.equals(goal));
	}

	public void testAddConAccount(){
		HashMap<String,String> users = new HashMap<String, String>();
		users.put(EMAIL+"x", "admin");
		users.put(EMAIL, "user");
		
		String result = mCreator.createAddConAccount(ID, users);
		String goal = ADDCONACCOUNT_1+ID+ADDCONACCOUNT_2+EMAIL+"x"+ADDCONACCOUNT_3+users.get(EMAIL+"x")+ADDCONACCOUNT_2_2+EMAIL+ADDCONACCOUNT_3+users.get(EMAIL)+ADDCONACCOUNT_4;
		
		Log.i(TAG, "AddConAccountTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("AddConAccountTest1: messages are not equal",result.equals(goal));
	}
	
	public void testDelConAccount(){
		ArrayList<String>users = new ArrayList<String>();
		users.add(EMAIL);
		users.add(EMAIL+"x");
		
		String result = mCreator.createDelConAccount(ID, users);
		String goal = DELCONACCOUNT_1+ID+DELCONACCOUNT_2+users.get(0)+DELCONACCOUNT_2_2+users.get(1)+DELCONACCOUNT_3;
		
		Log.i(TAG, "DelConAccountTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("DelConAccountTest1: messages are not equal",result.equals(goal));
	}
	
	public void testGetConAccount(){
		String result = mCreator.createGetConAccount(ID);
		String goal = GETCONACCOUNT_1+ID+GETCONACCOUNT_2;
		
		Log.i(TAG, "GetConAccountTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("GetConAccountTest1: messages are not equal",result.equals(goal));
	}
	
	public void testChangeConAccount(){
		HashMap<String,String> users = new HashMap<String, String>();
		users.put(EMAIL+"x", "admin");
		users.put(EMAIL, "user");
		
		String result = mCreator.createChangeConAccount(ID, users);
		String goal = CHANGECONACCOUNT_1+ID+CHANGECONACCOUNT_2+EMAIL+"x"+CHANGECONACCOUNT_3+users.get(EMAIL+"x")+CHANGECONACCOUNT_2_2+EMAIL+
				CHANGECONACCOUNT_3+users.get(EMAIL)+CHANGECONACCOUNT_4;
		
		Log.i(TAG, "ChagneConAccountTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("ChangeConAccountTest1: messages are not equal",result.equals(goal));
	}
	
	public void testTRUE(){
		String result = mCreator.createTRUE(ID, ADDITIONALINFO);
		String goal = TRUE_1+ID+TRUE_2+ADDITIONALINFO+TRUE_3;
		
		Log.i(TAG, "TRUETest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("TRUETest1: messages are not equal",result.equals(goal));
	}
	
	public void testFALSE(){
		String result = mCreator.createFALSE(ID, ADDITIONALINFO, XML);
		String goal = FALSE_1+ID+FALSE_2+ADDITIONALINFO+FALSE_3+XML+FALSE_4;
		
		Log.i(TAG, "FALSETest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("FALSETest1: messages are not equal",result.equals(goal));
	}
	
	public void testGetAdapters(){
		String result = mCreator.createGetAdapters(ID);
		String goal = GETADAPTERS_1+ID+GETADAPTERS_2;
		
		Log.i(TAG, "GetAdaptersTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("GetAdaptersTest1: messages are not equal",result.equals(goal));
	}
	
	public void testUpdate(){
		ArrayList<String>devices = new ArrayList<String>();
		devices.add(DEVICEID);
		devices.add(DEVICEID);
		
		String result = mCreator.createUpdate(ID, devices);
		String goal = UPDATE_1+ID+UPDATE_2+devices.get(0)+UPDATE_2_2+devices.get(1)+Update_3;
		
		Log.i(TAG, "UpdateTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("UpdateTest1: messages are not equal",result.equals(goal));
	}
	
	public void testAddView(){
		ArrayList<String>devices = new ArrayList<String>();
		devices.add(DEVICEID);
		devices.add(DEVICEID);
		
		String result = mCreator.createAddView(ID, VIEWNAME, devices);
		String goal = ADDVIEW_1+ID+ADDVIEW_2+VIEWNAME+ADDVIEW_3+devices.get(0)+ADDVIEW_3_3+devices.get(1)+ADDVIEW_4;
		
		Log.i(TAG, "AddViewTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("AddViewTest1: messages are not equal",result.equals(goal));
	}
	
	public void testDelView(){
		String result = mCreator.createDelView(ID, VIEWNAME);
		String goal = DELVIEW_1+ID+DELVIEW_2+VIEWNAME+DELVIEW_3;
		
		Log.i(TAG, "DelViewTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("DelViewTest1: messages are not equal",result.equals(goal));
	}
	
	public void testUpdateView(){
		HashMap<String,String> devices = new HashMap<String, String>();
		devices.put(DEVICEID, "remove");
		devices.put(DEVICEID+"x", "add");
		
		String result = mCreator.createUpdateView(ID, VIEWNAME, devices);
		String goal = UPDATEVIEW_1+ID+UPDATEVIEW_2+VIEWNAME+UPDATEVIEW_3+DEVICEID+"x"+UPDATEVIEW_4+devices.get(DEVICEID+"x")+UPDATEVIEW_4_2+DEVICEID+UPDATEVIEW_4
				+devices.get(DEVICEID)+UPDATEVIEW_5;
		
		Log.i(TAG, "UpdateViewTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("UpdateViewTest1: messages are not equal",result.equals(goal));
	}
	
	public void testPartial(){
		ArrayList<BaseDevice> devices = new ArrayList<BaseDevice>();
		
		EmissionDevice em = new EmissionDevice();
		em.setInitialized(true);
		em.setAddress(DEVICEID+"em");
		em.setVisibility('i');
		em.setLocation("obyvak");
		em.setLocationType(1);
		devices.add(em);
		
		HumidityDevice hu = new HumidityDevice();
		hu.setInitialized(true);
		hu.setAddress(DEVICEID+"hu");
		hu.setVisibility('x');
		hu.setName("vlhkomer");
		devices.add(hu);
		
		IlluminationDevice il = new IlluminationDevice();
		il.setInitialized(true);
		il.setAddress(DEVICEID+"il");
		il.setVisibility('x');
		il.setRefresh(5);
		devices.add(il);
		
		SwitchDevice sw = new SwitchDevice();
		sw.setInitialized(true);
		sw.setAddress(DEVICEID+"sw");
		sw.setVisibility('i');
		sw.setValue("ON");
		devices.add(sw);
		
		NoiseDevice no = new NoiseDevice();
		no.setInitialized(true);
		no.setVisibility('i');
		no.setAddress(DEVICEID+"no");
		no.setLogging(true);
		devices.add(no);
		
		PressureDevice pr = new PressureDevice();
		pr.setInitialized(false);
		pr.setVisibility('o');
		pr.setAddress(DEVICEID+"pr");
		pr.setValue(50);
		pr.setLogging(false);
		devices.add(pr);
		
		
		String result = mCreator.createPartial(ID, devices);
		String goal = PARTIAL_ALL;
		
		Log.i(TAG, "PartialTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("PartialTest1: messages are not equal",result.equals(goal));
	}
}
