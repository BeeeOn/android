/**
 * 
 */
package cz.vutbr.fit.iha.test;

import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.TestCase;
import android.util.Log;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.device.EmissionDevice;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.adapter.device.HumidityDevice;
import cz.vutbr.fit.iha.adapter.device.IlluminationDevice;
import cz.vutbr.fit.iha.adapter.device.NoiseDevice;
import cz.vutbr.fit.iha.adapter.device.PressureDevice;
import cz.vutbr.fit.iha.adapter.device.RefreshInterval;
import cz.vutbr.fit.iha.adapter.device.SwitchDevice;
import cz.vutbr.fit.iha.adapter.device.TemperatureDevice;
import cz.vutbr.fit.iha.household.User;
import cz.vutbr.fit.iha.network.xml.XmlCreator;

/**
 * @author ThinkDeep
 *
 */
public class XmlCreatorTest extends TestCase {
	
	private static String TAG = "XmlCreatorTest";
	private static String EMAIL = "testmail@domain.com";
	private static String GTOKEN = "9845658";
	private static String ID = "5";
	private static String GVERSION = "2.2";
	private static String SERIAL = "555645792";
	private static String ADAPTERNAME = "home";
	private static String ADAPTERID = SERIAL;
	private static String ADAPTERIDOLD = ADAPTERID+"1";
	
	private static String DEVICEID = "120:07:ff:000:ffe";
	private static String FROM = "2013-08-28-10:00:00";
	private static String TO = "2013-08-28-10:09:00";
	private static String VIEWNAME = "custom_name";
	private static int TIMEZONE = 2;
	private static String LOCALE = "cs";
	private static int DEVICETYPE = 1;
	private static String FUNCTYPE = "avg";
	private static int INTERVAL = 3600;
	
	//messages
	private static String SIGNIN_2 = "\" /></communication>";
	private static String SIGNUP_1 = "<?xml version='1.0' encoding='UTF-8' ?><communication id=\"";
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
	private static String TRUE_3 = "\" />";
	private static String FALSE_4 = "</communication>";
	private static String ADDVIEW_1 = SIGNUP_1;
	private static String ADDVIEW_2 = "\" state=\"addview\" version=\""+GVERSION+"\" name=\"";
	private static String ADDVIEW_2_1 = "\" icon=\"";
	private static String ADDVIEW_3 = "\"><device id=\"";
	private static String ADDVIEW_3_3 = "\" /><device id=\"";
	private static String ADDVIEW_4 = SIGNIN_2;
	private static String DELVIEW_1 = SIGNUP_1;
	private static String DELVIEW_2 = "\" state=\"delview\" version=\""+GVERSION+"\" name=\"";
	private static String DELVIEW_3 = TRUE_3;
	private static String UPDATEVIEW_1 = SIGNUP_1;
	private static String UPDATEVIEW_2 = "\" state=\"updateview\" version=\""+GVERSION+"\" name=\"";
	private static String UPDATEVIEW_2_1 = ADDVIEW_2_1;
	private static String UPDATEVIEW_3 = ADDVIEW_3;//"\"><device id=\"";
	private static String UPDATEVIEW_4 = "\" action=\"";
	private static String UPDATEVIEW_4_2 = "\" /><device id=\"";
	private static String UPDATEVIEW_5 = SIGNIN_2;
	private static String GETVIEWS_1 = SIGNUP_1;
	private static String GETVIEWS_2 = "\" state=\"getviews\" version=\""+GVERSION+"\" />";
	private static String SETTIMEZONE_1 = SIGNUP_1;
	private static String SETTIMEZONE_2 = "\" state=\"settimezone\" version=\""+GVERSION+"\"><time utc=\"";
	private static String SETTIMEZONE_3 = "\" />";
	private static String SETTIMEZONE_4 = FALSE_4;
	private static String GETTIMEZONE_1 = SIGNUP_1;
	private static String GETTIMEZONE_2 = "\" state=\"gettimezone\" version=\""+GVERSION+"\" />";
	
	
	private static String PARTIAL_ALL = "<?xml version='1.0' encoding='UTF-8' ?><communication id=\""+ID+"\" state=\"partial\" version=\""+GVERSION+"\">"
			+ "<device initialized=\"1\" type=\"0x07\" id=\"120:07:ff:000:ffeem\" visibility=\"i\"><location type=\"1\">obyvak</location><name>sen1</name><logging enabled=\"0\" /></device>"
			+ "<device initialized=\"1\" type=\"0x01\" id=\"120:07:ff:000:ffehu\" visibility=\"x\"><name>vlhkomer</name><logging enabled=\"0\" /></device>"
			+ "<device initialized=\"1\" type=\"0x05\" id=\"120:07:ff:000:ffeil\" visibility=\"x\"><name>sen2</name><refresh>5</refresh><logging enabled=\"0\" /></device>"
			+ "<device initialized=\"1\" type=\"0x04\" id=\"120:07:ff:000:ffesw\" visibility=\"i\"><name>sen3</name><logging enabled=\"0\" /></device>"
			+ "<device initialized=\"1\" type=\"0x06\" id=\"120:07:ff:000:ffeno\" visibility=\"i\"><name>sen4</name><logging enabled=\"1\" /></device>"
			+ "<device initialized=\"0\" type=\"0x02\" id=\"120:07:ff:000:ffepr\" visibility=\"o\"><name>sen5</name><value>50</value><logging enabled=\"0\" /></device>"
			+ "</communication>";
	
 	public XmlCreatorTest() {
		super("cz.vutbr.fit.iha.parser");
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testSignIn(){
		String result = XmlCreator.createSignIn(EMAIL, GTOKEN, LOCALE, GTOKEN);
		String goal = String.format(//"<?xml version='1.0' encoding='UTF-8' ?>" +
				"<com " +
					"ver=\"%s\" " +
					"state=\"signin\" " +
					"email=\"%s\" " +
					"gt=\"%s\" " +
					"loc=\"%s\" " +
					"gcmid=\"%s\" " +
				"/>",
				GVERSION, EMAIL, GTOKEN, "cs", GTOKEN);
		
		Log.i(TAG, "SignInTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("SignInTest1: messages are not equal",result.equals(goal));
	}
	
	public void testSignUp(){
		String result = XmlCreator.createSignUp(EMAIL, GTOKEN);
		String goal = String.format(
				"<com " +
				"ver=\"%s\" " +
				"state=\"signup\" " +
				"email=\"%s\" " +
				"gt=\"%s\" " +
			"/>",
			GVERSION, EMAIL, GTOKEN);
		
		Log.i(TAG, "SignUpTest");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("SignUpTest: messages are not equal",result.equals(goal));
	}
	
	public void testAddAdapter(){
		String result = XmlCreator.createAddAdapter(ID, SERIAL, ADAPTERNAME);
		String goal = String.format(
				"<com " +
				"ver=\"%s\" " +
				"state=\"addadapter\" " +
				"sid=\"%s\" " +
				"aid=\"%s\" " +
				"name=\"%s\" " +
			"/>",
			GVERSION, ID, SERIAL, ADAPTERNAME);
		
		Log.i(TAG, "addadapter");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("addadapter: messages are not equal",result.equals(goal));
	}
	
	public void testGetAdapters(){
		String result = XmlCreator.createGetAdapters(ID);
		String goal = String.format(
				"<com " +
				"ver=\"%s\" " +
				"state=\"getadapters\" " +
				"sid=\"%s\" " +
			"/>",
			GVERSION, ID);
		
		Log.i(TAG, "GetAdaptersTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("GetAdaptersTest1: messages are not equal",result.equals(goal));
	}

	public void testReInit(){
		String result = XmlCreator.createReInitAdapter(ID,ADAPTERIDOLD,ADAPTERID);
		String goal = String.format(
				"<com " +
				"ver=\"%s\" " +
				"state=\"reinitadapter\" " +
				"sid=\"%s\" " +
				"oaid=\"%s\" " +
				"naid=\"%s\" " +
			"/>",
			GVERSION, ID, ADAPTERIDOLD, ADAPTERID);
		
		Log.i(TAG, "ReInitTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("ReInitTest1: messages are not equal",result.equals(goal));
	}
	
	public void testScanMode(){
		String result = XmlCreator.createAdapterScanMode(ID, SERIAL);
		String goal = String.format(
				"<com " +
				"ver=\"%s\" " +
				"state=\"scanmode\" " +
				"sid=\"%s\" " +
				"aid=\"%s\" " +
			"/>",
			GVERSION, ID, SERIAL);
		
		Log.i(TAG, "scanmode");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("scanmode: messages are not equal",result.equals(goal));
	}
	
	public void testGetAllDevices(){
		String result = XmlCreator.createGetAllDevices(ID, SERIAL);
		String goal = String.format(
				"<com " +
				"ver=\"%s\" " +
				"state=\"getalldevs\" " +
				"sid=\"%s\" " +
				"aid=\"%s\" " +
			"/>",
			GVERSION, ID, SERIAL);
		
		Log.i(TAG, "getalldevices");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("getalldevices: messages are not equal",result.equals(goal));
	}
	
	public void testGetNewDevices(){
		String result = XmlCreator.createGetNewDevices(ID, SERIAL);
		String goal = String.format(
				"<com " +
				"ver=\"%s\" " +
				"state=\"getnewdevs\" " +
				"sid=\"%s\" " +
				"aid=\"%s\" " +
			"/>",
			GVERSION, ID, SERIAL);
		
		Log.i(TAG, "getnewdevices");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("getnewdevices: messages are not equal",result.equals(goal));
	}
	
	public void testSwitch(){
		BaseDevice device = new TemperatureDevice();
		device.setValue(5);
		Facility facility = new Facility();
		facility.setAddress("120:00");
		device.setFacility(facility);
		String result = XmlCreator.createSwitch(ID, SERIAL, device);
		String goal = String.format(
				"<com " +
				"ver=\"%s\" " +
				"state=\"switch\" " +
				"sid=\"%s\" " +
				"aid=\"%s\" " +
				"did=\"%s\" " +
				"type=\"%s\" " +
				"val=\"%s\" " +
			"/>",
			GVERSION, ID, SERIAL, facility.getAddress(), Integer.toString(device.getType()), device.getStringValue());
		
		Log.i(TAG, "switch");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("switch: messages are not equal",result.equals(goal));
	}
	
	//TODO: repair to 1.9(2.0)
	public void xtestLogName(){
		String result = XmlCreator.createGetLog(ID, SERIAL, DEVICEID, DEVICETYPE, FROM, TO, FUNCTYPE, INTERVAL);
		String goal = LOGNAME_1+ID+LOGNAME_2+FROM+LOGNAME_3+TO+LOGNAME_4+DEVICEID+LOGNAME_5;
		
		Log.i(TAG, "LogNameTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("LogNameTest1: messages are not equal",result.equals(goal));
	}

	//TODO: repair to 1.9(2.0)
	public void xtestAddConAccount(){
		HashMap<String,String> users = new HashMap<String, String>();
		users.put(EMAIL+"x", "admin");
		users.put(EMAIL, "user");
		
		String result = "";//XmlCreator.createAddAccounts(ID, SERIAL, users);
		String goal = ADDCONACCOUNT_1+ID+ADDCONACCOUNT_2+EMAIL+"x"+ADDCONACCOUNT_3+users.get(EMAIL+"x")+ADDCONACCOUNT_2_2+EMAIL+ADDCONACCOUNT_3+users.get(EMAIL)+ADDCONACCOUNT_4;
		
		Log.i(TAG, "AddConAccountTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("AddConAccountTest1: messages are not equal",result.equals(goal));
	}
	
	//TODO: repair to 1.9(2.0)
	public void xtestDelConAccount(){
		User user = new User();
//		users.add(EMAIL);
//		users.add(EMAIL+"x");
		
		String result = "";//XmlCreator.createDelAccount(ID, SERIAL, user);
		String goal = DELCONACCOUNT_1+ID+DELCONACCOUNT_2+user.getEmail()+DELCONACCOUNT_2_2+user.getName()+DELCONACCOUNT_3;
		
		Log.i(TAG, "DelConAccountTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("DelConAccountTest1: messages are not equal",result.equals(goal));
	}
	
	//TODO: repair to 1.9(2.0)
	public void xtestGetConAccount(){
		String result = XmlCreator.createGetAccounts(ID, SERIAL);
		String goal = GETCONACCOUNT_1+ID+GETCONACCOUNT_2;
		
		Log.i(TAG, "GetConAccountTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("GetConAccountTest1: messages are not equal",result.equals(goal));
	}
	
	//TODO: repair to 1.9(2.0)
	public void xtestChangeConAccount(){
		HashMap<String,String> users = new HashMap<String, String>();
		users.put(EMAIL+"x", "admin");
		users.put(EMAIL, "user");
		
		String result = "";//XmlCreator.createUpdateAccounts(ID, SERIAL, users);
		String goal = CHANGECONACCOUNT_1+ID+CHANGECONACCOUNT_2+EMAIL+"x"+CHANGECONACCOUNT_3+users.get(EMAIL+"x")+CHANGECONACCOUNT_2_2+EMAIL+
				CHANGECONACCOUNT_3+users.get(EMAIL)+CHANGECONACCOUNT_4;
		
		Log.i(TAG, "ChagneConAccountTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("ChangeConAccountTest1: messages are not equal",result.equals(goal));
	}

	//TODO: repair to 1.9(2.0)
	public void xtestUpdate(){
		ArrayList<BaseDevice>devices = new ArrayList<BaseDevice>();

		BaseDevice a = new NoiseDevice();
//		a.setAddress(DEVICEID);

		devices.add(a);
		devices.add(a);
		
		String result = "";//XmlCreator.createGetDevices(ID, SERIAL, devices);
		String goal = "";//UPDATE_1+ID+UPDATE_2+devices.get(0).getAddress()+UPDATE_2_1+"0x0"+devices.get(0).getType()+UPDATE_2_2+
//				devices.get(1).getAddress()+UPDATE_2_1+"0x0"+devices.get(1).getType()+UPDATE_3;
		
		Log.i(TAG, "UpdateTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("UpdateTest1: messages are not equal",result.equals(goal));
	}
	
	//TODO: repair to 1.9(2.0)
	public void xtestAddView(){
		ArrayList<BaseDevice>devices = new ArrayList<BaseDevice>();
		devices.add(new TemperatureDevice());
		devices.add(new HumidityDevice());
		
		String result = XmlCreator.createAddView(ID, VIEWNAME, 1, devices);
		String goal = ADDVIEW_1+ID+ADDVIEW_2+VIEWNAME+ADDVIEW_2_1+1+ADDVIEW_3+devices.get(0)+ADDVIEW_3_3+devices.get(1)+ADDVIEW_4;
		
		Log.i(TAG, "AddViewTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("AddViewTest1: messages are not equal",result.equals(goal));
	}
	
	//TODO: repair to 1.9(2.0)
	public void xtestDelView(){
		String result = XmlCreator.createDelView(ID, VIEWNAME);
		String goal = DELVIEW_1+ID+DELVIEW_2+VIEWNAME+DELVIEW_3;
		
		Log.i(TAG, "DelViewTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("DelViewTest1: messages are not equal",result.equals(goal));
	}
	
	//TODO: repair to 1.9(2.0)
	public void xtestUpdateView(){
		HashMap<String,String> devices = new HashMap<String, String>();
		devices.put(DEVICEID, "remove");
		devices.put(DEVICEID+"x", "add");
		
		String result = "";//XmlCreator.createUpdateViews(ID, VIEWNAME, 0, devices);
		String goal = UPDATEVIEW_1+ID+UPDATEVIEW_2+VIEWNAME+UPDATEVIEW_2_1+0+UPDATEVIEW_3+DEVICEID+"x"+UPDATEVIEW_4+devices.get(DEVICEID+"x")+UPDATEVIEW_4_2+DEVICEID+UPDATEVIEW_4
				+devices.get(DEVICEID)+UPDATEVIEW_5;
		
		Log.i(TAG, "UpdateViewTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("UpdateViewTest1: messages are not equal",result.equals(goal));
	}
	
	//TODO: repair to 1.9(2.0)
	public void xtestGetViews(){
		String result = XmlCreator.createGetViews(ID);
		String goal = GETVIEWS_1+ID+GETVIEWS_2;
		
		Log.i(TAG, "GetViewsTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("GetViewsTest1: messages are not equal",result.equals(goal));
	}
	
	//TODO: repair to 1.9(2.0)
	public void xtestSetTimeZone(){
		String result = XmlCreator.createSetTimeZone(ID, SERIAL, TIMEZONE);
		String goal = SETTIMEZONE_1+ID+SETTIMEZONE_2+TIMEZONE+SETTIMEZONE_3+SETTIMEZONE_4;
		
		Log.i(TAG, "SetTimeZoneTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("SetTimeZoneTest1: messages are not equal",result.equals(goal));
	}
	
	//TODO: repair to 1.9(2.0)
	public void xtestGetTimeZone(){
		String result = XmlCreator.createGetTimeZone(ID, SERIAL);
		String goal = GETTIMEZONE_1+ID+GETTIMEZONE_2;
		
		Log.i(TAG, "GetTimeZoneTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("GetTimeZoneTest1: messages are not equal",result.equals(goal));
	}

	//TODO: repair to 1.9(2.0)
	public void xtestPartial(){
		ArrayList<BaseDevice> devices = new ArrayList<BaseDevice>();
		
		EmissionDevice em = new EmissionDevice();
//		em.setInitialized(true);
//		em.setAddress(DEVICEID+"em");
//		em.setVisibility(VisibilityState.VISIBLE);
		//em.setLocation(new Location("obyvak", "obyvak", 1));
		em.setName("sen1");
		devices.add(em);
		
		HumidityDevice hu = new HumidityDevice();
//		hu.setInitialized(true);
//		hu.setAddress(DEVICEID+"hu");
//		hu.setVisibility(VisibilityState.DELETE);
		hu.setName("vlhkomer");
		devices.add(hu);
		
		IlluminationDevice il = new IlluminationDevice();
//		il.setInitialized(true);
//		il.setAddress(DEVICEID+"il");
		il.setName("sen2");
//		il.setVisibility(VisibilityState.DELETE);
//		il.setRefresh(RefreshInterval.HOUR_1);
		devices.add(il);
		
		SwitchDevice sw = new SwitchDevice();
//		sw.setInitialized(true);
//		sw.setAddress(DEVICEID+"sw");
//		sw.setVisibility(VisibilityState.VISIBLE);
		sw.setValue("ON");
		sw.setName("sen3");
		devices.add(sw);
		
		NoiseDevice no = new NoiseDevice();
//		no.setInitialized(true);
//		no.setVisibility(VisibilityState.VISIBLE);
//		no.setAddress(DEVICEID+"no");
		no.setLogging(true);
		no.setName("sen4");
		devices.add(no);
		
		PressureDevice pr = new PressureDevice();
//		pr.setInitialized(false);
//		pr.setVisibility(VisibilityState.HIDDEN);
//		pr.setAddress(DEVICEID+"pr");
		pr.setName("sen5");
		pr.setValue(50);
		pr.setLogging(false);
		devices.add(pr);
		
		
		String result = "";//XmlCreator.createDevices(ID, SERIAL, devices);
		String goal = PARTIAL_ALL;
		
		Log.i(TAG, "PartialTest1");
		Log.d(TAG, result);
		if(!result.equals(goal))
			Log.e(TAG, goal);
		assertTrue("PartialTest1: messages are not equal",result.equals(goal));
	}
}
