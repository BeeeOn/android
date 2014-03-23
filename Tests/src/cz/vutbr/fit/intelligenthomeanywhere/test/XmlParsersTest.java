/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import android.util.Log;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.ContentRow;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.FalseAnswer;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.ParsedMessage;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.UserData;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.XmlParsers;

/**
 * @author ThinkDeep
 *
 */
public class XmlParsersTest extends TestCase {
	
	
	private static String TAG = "XmlParsersTest";
//	private static String EMAIL = "testmail@domain.com";
//	private static String GTOKEN = "9845658";
	private static int ID = 425644;
	private static String GVERSION = "1.0";
	//FIXME: length of serial???
//	private static int SERIAL = 555645792;
//	private static int ADAPTERID = SERIAL;
//	private static int ADAPTERIDOLD = ADAPTERID++;
//	private static String DEVICEID = "120:07:ff:000:ffe";
//	private static String FROM = "2013-08-28-10:00:00";
//	private static String TO = "2013-08-28-10:09:00";
//	private static String ADDITIONALINFO = "changeconaccount";
//	private static String XML = "something";
//	private static String VIEWNAME = "custom_name";
	
	//messages
	private static String READY_1 = "<?xml version='1.0' encoding='UTF-8' ?><communication id=\""+ID+"\" state=\"ready\" version=\""+GVERSION+"\">";
	private static String READY_2 = "<adapter id=\"";
	private static String READY_3 = "\" name=\"";
	private static String READY_4 = "\" role=\"";
	private static String READY_5_1 = "\" /><adapter id=\"";
	private static String READY_5_END = "\" /></communication>";
	
	private static String CONTENT_1 = "<?xml version='1.0' encoding='UTF-8' ?><communication id=\""+ID+"\" state=\"content\" version=\""+GVERSION+"\">";
	private static String CONTENT_2 = "<row>";
	private static String CONTENT_3 = "</row>";
	private static String CONTENT_END = "</communication>";
	
	private static String CONACCOUNTLIST_1 = "<?xml version='1.0' encoding='UTF-8' ?><communication id=\""+ID+"\" state=\"conaccountlist\" version=\""+GVERSION+"\">";
	private static String CONACCOUNTLIST_2 = "<user email=\"";
	private static String CONACCOUNTLIST_3 = "\" role=\"";
	private static String CONACCOUNTLIST_4 = "\" />";
	private static String CONACCOUNTLIST_END = CONTENT_END;
	
	private static String FALSE_1 = "<?xml version='1.0' encoding='UTF-8' ?><communication id=\""+ID+"\" state=\"false\" version=\""+GVERSION+"\" additionalinfo=\"";
	private static String FALSE_1_2 = "\">";
	private static String FALSE_2 = CONACCOUNTLIST_2;
	private static String FALSE_3 = CONACCOUNTLIST_3;
	private static String FALSE_4 = CONACCOUNTLIST_4;
	private static String FALSE_END = CONTENT_END;
	
	private static String NOTREGA = "<?xml version='1.0' encoding='UTF-8' ?><communication id=\""+ID+"\" state=\"notreg-a\" version=\""+GVERSION+"\" />";
	
	private static String NOTREGB = "<?xml version='1.0' encoding='UTF-8' ?><communication id=\""+ID+"\" state=\"notreg-b\" version=\""+GVERSION+"\" />";
	
	private static String TRUE = "<?xml version='1.0' encoding='UTF-8' ?><communication id=\""+ID+"\" state=\"true\" version=\""+GVERSION+"\" additionalinfo=\"changeconaccount\" />";
	
	private static String RESIGN = "<?xml version='1.0' encoding='UTF-8' ?><communication id=\""+0+"\" state=\"resign\" version=\""+GVERSION+"\" />";
	
 	public XmlParsersTest() {
		super("cz.vutbr.fit.intelligenthomeanywhere.parser");
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	@SuppressWarnings("unchecked")
	public void testReady(){
		String xmlMessage = READY_1 + READY_2 + "4565616" + READY_3 + "some_funny_name" + READY_4 + "admin"
									+ READY_5_1 + "5645456" + READY_3 + "other_name" + READY_4 + "superuser"
									+ READY_5_1 + "464415" + READY_3 + "last_name" + READY_4 + "guest" + READY_5_END;
		String state = "ready";
		
		Log.i(TAG, "ReadyTest1");
		
		try {
			//TODO: remove commented code
//			HashMap<String, HashMap<Integer, Object>> result = XmlParsers.parseCommunication_old(xmlMessage, false);
//			for(Map.Entry<String, HashMap<Integer, Object>> entry : result.entrySet()){
//				assertTrue("ReadyTest1: bad state", entry.getKey().equals(state));
//				
//				for(Map.Entry<Integer, Object> data : entry.getValue().entrySet()){
//					int id = data.getKey();
//					ArrayList<Adapter> adapters = (ArrayList<Adapter>) data.getValue();
//					Log.d(TAG, "Communication id: " + id);
//					for(Adapter adapter : adapters){
//						Log.d(TAG, "Ready: id: " + adapter.getId());
//						Log.d(TAG, "Ready: name: " + adapter.getName());
//						Log.d(TAG, "Ready: role: " + adapter.getRole());
//					}
//					
//				}
//			}
			ParsedMessage result = XmlParsers.parseCommunication(xmlMessage, false);
			assertTrue("ReadyTest1: bad state", result.getState().equals(state));
			Log.d(TAG, "Communication id: " + result.getSessionId());
			
			ArrayList<Adapter> adapters = (ArrayList<Adapter>) result.data;
			for(Adapter adapter : adapters){
				Log.d(TAG, "Ready: id: " + adapter.getId());
				Log.d(TAG, "Ready: name: " + adapter.getName());
				Log.d(TAG, "Ready: role: " + adapter.getRole());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue("ReadyTest1: error",false);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void testContent(){
		String xmlMessage = CONTENT_1 + CONTENT_2 + "2013-08-28 10:00:00  30 100" + CONTENT_3
									  + CONTENT_2 + "2013-08-28 10:01:00  30 100" + CONTENT_3
									  + CONTENT_2 + "2013-08-28 10:09:00  33 98" + CONTENT_3 + CONTENT_END;
		
		String state = "content";
		
		Log.i(TAG, "ContentTest1");
		
		try {
//			HashMap<String, HashMap<Integer, Object>> result = XmlParsers.parseCommunication_old(xmlMessage, false);
//			for(Map.Entry<String, HashMap<Integer, Object>> entry : result.entrySet()){
//				assertTrue("ContentTest1: bad state", entry.getKey().equals(state));
//				
//				for(Map.Entry<Integer, Object> data : entry.getValue().entrySet()){
//					int id = data.getKey();
//					ArrayList<String> rows = (ArrayList<String>) data.getValue();
//					Log.d(TAG, "Communication id: " + id);
//					for(String row : rows){
//						Log.d(TAG, "Content: row -> " + row);
//					}
//				}
//			}
			ParsedMessage result = XmlParsers.parseCommunication(xmlMessage, false);
			assertTrue("ContentyTest1: bad state", result.getState().equals(state));
			Log.d(TAG, "Communication id: " + result.getSessionId());
			
			ArrayList<ContentRow> rows = (ArrayList<ContentRow>) result.data;
			for(ContentRow row : rows){
				Log.d(TAG, "Content: row -> " + row.debugString());
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue("ContentTest1: " + e.toString() ,false);
		}
	}

	@SuppressWarnings("unchecked")
	public void testConAccountList(){
		String xmlMessage = CONACCOUNTLIST_1 + CONACCOUNTLIST_2 + "user@gmail.com" + CONACCOUNTLIST_3 + "user" + CONACCOUNTLIST_4
											 + CONACCOUNTLIST_2 + "user1@gmail.com" + CONACCOUNTLIST_3 + "admin" + CONACCOUNTLIST_4
											 + CONACCOUNTLIST_2 + "user2@gmail.com" + CONACCOUNTLIST_3 + "superuser" + CONACCOUNTLIST_4 + CONACCOUNTLIST_END;
							
		String state = "conaccountlist";
		
		Log.i(TAG, "ConAccountListTest1");
		
		try {
//			HashMap<String, HashMap<Integer, Object>> result = XmlParsers.parseCommunication_old(xmlMessage, false);
//			for(Map.Entry<String, HashMap<Integer, Object>> entry : result.entrySet()){
//				assertTrue("ConAccountListTest1: bad state", entry.getKey().equals(state));
//				
//				for(Map.Entry<Integer, Object> data : entry.getValue().entrySet()){
//					int id = data.getKey();
//					HashMap<String, String> users = (HashMap<String, String>) data.getValue();
//					Log.d(TAG, "Communication id: " + id);
//					for(Map.Entry<String, String> user : users.entrySet()){
//						Log.d(TAG, "ConAccountList: email: " + user.getKey());
//						Log.d(TAG, "ConAccountList: role: " + user.getValue());
//					}
//				}
//			}
			ParsedMessage result = XmlParsers.parseCommunication(xmlMessage, false);
			assertTrue("ConAccountListTest1: bad state", result.getState().equals(state));
			Log.d(TAG, "Communication id: " + result.getSessionId());
			
			HashMap<String, UserData> users =  (HashMap<String, UserData>) result.data;
			for(Map.Entry<String, UserData> user : users.entrySet()){
				//Log.d(TAG, "ConAccountList: email: " + user.getKey());
				Log.d(TAG, "ConAccountList: userdata: " + user.getValue().debugString());
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue("ConAccountListTest1: " + e.toString() ,false);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void testFalse(){
		String xmlMessage = FALSE_1 + "addconaccount" + FALSE_1_2
									+ FALSE_2 + "jahoda@gmail.com" + FALSE_3 + "admin" + FALSE_4
									+ FALSE_2 + "banan@gmail.com" + FALSE_3 + "guest" + FALSE_4 + FALSE_END;
		
		String state = "false";
		
		Log.i(TAG, "FalseTest1");
		
		try {
//			HashMap<String, HashMap<Integer, Object>> result = XmlParsers.parseCommunication_old(xmlMessage, false);
//			for(Map.Entry<String, HashMap<Integer, Object>> entry : result.entrySet()){
//				assertTrue("FalseTest1: bad state", entry.getKey().equals(state));
//				
//				for(Map.Entry<Integer, Object> data : entry.getValue().entrySet()){
//					int id = data.getKey();
//					HashMap<String, HashMap<String, String>> info = (HashMap<String, HashMap<String, String>>) data.getValue();
//					Log.d(TAG, "Communication id: " + id);
//					
//					for(Map.Entry<String, HashMap<String, String>> users : info.entrySet()){
//						Log.d(TAG, "False: additionalinfo: " + users.getKey());
//						
//						for(Map.Entry<String, String> user : users.getValue().entrySet()){
//							Log.d(TAG, "False: email: " + user.getKey());
//							Log.d(TAG, "False: role: " + user.getValue());
//						}
//					}
//				}
//			}
			
			ParsedMessage result = XmlParsers.parseCommunication(xmlMessage, false);
			assertTrue("FalseTest1: bad state", result.getState().equals(state));
			Log.d(TAG, "Communication id: " + result.getSessionId());
			
			FalseAnswer falseData = (FalseAnswer) result.data;
			Log.d(TAG, "False: additionalinfo: " + falseData.getInfo());
			
			HashMap<String, String> users = (HashMap<String, String>) falseData.data;
			for(Map.Entry<String, String> user : users.entrySet()){
				Log.d(TAG, "False: email: " + user.getKey());
				Log.d(TAG, "False: role: " + user.getValue());
			}
			
		
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue("FalseTest1: " + e.toString() ,false);
		}
		
		xmlMessage = FALSE_1 + "changeconaccount" + FALSE_1_2
				+ FALSE_2 + "merunka@gmail.com" + FALSE_3 + "admin" + FALSE_4
				+ FALSE_2 + "zeli@gmail.com" + FALSE_3 + "guest" + FALSE_4 + FALSE_END;

		Log.i(TAG, "FalseTest2");
		
		try {
//			HashMap<String, HashMap<Integer, Object>> result = XmlParsers.parseCommunication_old(xmlMessage, false);
//			for(Map.Entry<String, HashMap<Integer, Object>> entry : result.entrySet()){
//				assertTrue("FalseTest2: bad state", entry.getKey().equals(state));
//				
//				for(Map.Entry<Integer, Object> data : entry.getValue().entrySet()){
//					int id = data.getKey();
//					HashMap<String, HashMap<String, String>> info = (HashMap<String, HashMap<String, String>>) data.getValue();
//					Log.d(TAG, "Communication id: " + id);
//					
//					for(Map.Entry<String, HashMap<String, String>> users : info.entrySet()){
//						Log.d(TAG, "False: additionalinfo: " + users.getKey());
//						
//						for(Map.Entry<String, String> user : users.getValue().entrySet()){
//							Log.d(TAG, "False: email: " + user.getKey());
//							Log.d(TAG, "False: role: " + user.getValue());
//						}
//					}
//				}
//			}
			
			ParsedMessage result = XmlParsers.parseCommunication(xmlMessage, false);
			assertTrue("FalseTest2: bad state", result.getState().equals(state));
			Log.d(TAG, "Communication id: " + result.getSessionId());
			
			FalseAnswer falseData = (FalseAnswer) result.data;
			Log.d(TAG, "False: additionalinfo: " + falseData.getInfo());
			
			HashMap<String, String> users = (HashMap<String, String>) falseData.data;
			for(Map.Entry<String, String> user : users.entrySet()){
				Log.d(TAG, "False: email: " + user.getKey());
				Log.d(TAG, "False: role: " + user.getValue());
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue("FalseTest2: " + e.toString() ,false);
		}
		
		xmlMessage = FALSE_1 + "delconaccount" + FALSE_1_2
				+ FALSE_2 + "houba@gmail.com" + FALSE_4
				+ FALSE_2 + "plisen@gmail.com" + FALSE_4 + FALSE_END;

		
		Log.i(TAG, "FalseTest3");
		
		try {
//			HashMap<String, HashMap<Integer, Object>> result = XmlParsers.parseCommunication_old(xmlMessage, false);
//			for(Map.Entry<String, HashMap<Integer, Object>> entry : result.entrySet()){
//				assertTrue("FalseTest3: bad state", entry.getKey().equals(state));
//				
//				for(Map.Entry<Integer, Object> data : entry.getValue().entrySet()){
//					int id = data.getKey();
//					HashMap<String, HashMap<String, String>> info = (HashMap<String, HashMap<String, String>>) data.getValue();
//					Log.d(TAG, "Communication id: " + id);
//					
//					for(Map.Entry<String, HashMap<String, String>> users : info.entrySet()){
//						Log.d(TAG, "False: additionalinfo: " + users.getKey());
//						
//						for(Map.Entry<String, String> user : users.getValue().entrySet()){
//							Log.d(TAG, "False: email: " + user.getKey());
//						}
//					}
//				}
//			}
			
			ParsedMessage result = XmlParsers.parseCommunication(xmlMessage, false);
			assertTrue("FalseTest3: bad state", result.getState().equals(state));
			Log.d(TAG, "Communication id: " + result.getSessionId());
			
			FalseAnswer falseData = (FalseAnswer) result.data;
			Log.d(TAG, "False: additionalinfo: " + falseData.getInfo());
			
			HashMap<String, String> users = (HashMap<String, String>) falseData.data;
			for(Map.Entry<String, String> user : users.entrySet()){
				Log.d(TAG, "False: email: " + user.getKey());
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue("FalseTest3: " + e.toString() ,false);
		}
	}
	
	public void testNotReg(){
		String xmlMessage = NOTREGA;
		
		String state = "notreg-a";
		
		Log.i(TAG, "NotRegATest1");
		
		try {
//			HashMap<String, HashMap<Integer, Object>> result = XmlParsers.parseCommunication_old(xmlMessage, false);
//			for(Map.Entry<String, HashMap<Integer, Object>> entry : result.entrySet()){
//				assertTrue("NotRegATest1: bad state", entry.getKey().equals(state));
//				
//				for(Map.Entry<Integer, Object> data : entry.getValue().entrySet()){
//					Log.d(TAG, "Communication id: " + data.getKey());
//				}
//			}
			
			ParsedMessage result = XmlParsers.parseCommunication(xmlMessage, false);
			assertTrue("NotRegATest1: bad state", result.getState().equals(state));
			Log.d(TAG, "Communication id: " + result.getSessionId());
		
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue("NotRegATest1: " + e.toString() ,false);
		}
		
		xmlMessage = NOTREGB;
		state = "notreg-b";

		Log.i(TAG, "NotRegBTest1");
		
		try {
//			HashMap<String, HashMap<Integer, Object>> result = XmlParsers.parseCommunication_old(xmlMessage, false);
//			for(Map.Entry<String, HashMap<Integer, Object>> entry : result.entrySet()){
//				assertTrue("NotRegBTest1: bad state", entry.getKey().equals(state));
//				
//				for(Map.Entry<Integer, Object> data : entry.getValue().entrySet()){
//					Log.d(TAG, "Communication id: " + data.getKey());
//				}
//			}
			
			ParsedMessage result = XmlParsers.parseCommunication(xmlMessage, false);
			assertTrue("NotRegBTest1: bad state", result.getState().equals(state));
			Log.d(TAG, "Communication id: " + result.getSessionId());
		
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue("NotRegBTest1: " + e.toString() ,false);
		}
	}
	
	public void testReSign(){
		String xmlMessage = RESIGN;
		
		String state = "resign";
		
		Log.i(TAG, "ReSignTest1");
		
		try {
			ParsedMessage result = XmlParsers.parseCommunication(xmlMessage, false);
			assertTrue("ReSignBTest1: bad state", result.getState().equals(state));
			Log.d(TAG, "Communication id: " + result.getSessionId());
		
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue("ReSignTest1: " + e.toString() ,false);
		}
	}
	
	public void testTrue(){
		String xmlMessage = TRUE;
		
		String state = "true";
		
		Log.i(TAG, "TrueTest1");
		
		try {
//			HashMap<String, HashMap<Integer, Object>> result = XmlParsers.parseCommunication_old(xmlMessage, false);
//			for(Map.Entry<String, HashMap<Integer, Object>> entry : result.entrySet()){
//				assertTrue("TrueTest1: bad state", entry.getKey().equals(state));
//				
//				for(Map.Entry<Integer, Object> data : entry.getValue().entrySet()){
//					Log.d(TAG, "Communication id: " + data.getKey());
//					Log.d(TAG, "Communication additionalifno: " + data.getValue());
//				}
//			}
			
			ParsedMessage result = XmlParsers.parseCommunication(xmlMessage, false);
			assertTrue("NotRegBTest1: bad state", result.getState().equals(state));
			Log.d(TAG, "Communication id: " + result.getSessionId());
		
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue("TrueTest1: " + e.toString() ,false);
		}
	}
	
	public void testPartial(){
		String xmlMessage =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><communication version=\"1.0\" id=\""+ID+"\" state=\"partial\">"
				+	"<device initialized=\"1\" type=\"0x00\" id=\"120:00:FF:000:FFE\" visibility=\"I\">"
				+		"<location type=\"1\">Obývací pokoj</location>"
				+		"<name>Teplota u dverí</name>"
				+		"<refresh>12</refresh>"
				+		"<battery>70</battery>"
				+		"<quality>52</quality>"
				+		"<value>120</value>"
				+		"<logging enabled=\"0\"/>"
				+	"</device>"
				+	"<device initialized=\"1\" type=\"0x01\" id=\"120:00:FF:000:F0E\" visibility=\"O\">"
				+		"<location type=\"2\">Jiny pokoj</location>"
				+		"<name>Neco u dverí</name>"
				+		"<refresh>10</refresh>"
				+		"<battery>74</battery>"
				+		"<quality>57</quality>"
				+		"<value>10</value>"
				+		"<logging enabled=\"1\"/>"
				+	"</device>"
				+	"<device initialized=\"1\" type=\"0x03\" id=\"120:00:FF:010:FFE\" visibility=\"I\">"
				+		"<location type=\"3\">Kuchyn</location>"
				+		"<name>Aha u dverí</name>"
				+		"<refresh>72</refresh>"
				+		"<battery>50</battery>"
				+		"<quality>59</quality>"
				+		"<value>120</value>"
				+		"<logging enabled=\"0\"/>"
				+	"</device>"
				+"</communication>";

		
		String state = "partial";
		
		Log.i(TAG, "PartialTest1");
		
		try {
//			HashMap<String, HashMap<Integer, Object>> result = XmlParsers.parseCommunication_old(xmlMessage, false);
//			for(Map.Entry<String, HashMap<Integer, Object>> entry : result.entrySet()){
//				assertTrue("PartialTest1: bad state", entry.getKey().equals(state));
//				
//				for(Map.Entry<Integer, Object> data : entry.getValue().entrySet()){
//					int id = data.getKey();
//					ArrayList<BaseDevice> devices =  (ArrayList<BaseDevice>) data.getValue();
//					Log.d(TAG, "Communication id: " + id);
//					
//					for(BaseDevice device : devices){
//						Log.d(TAG, "Partial: " + device.toString(false));
//					}
//				}
//			}
			
			ParsedMessage result = XmlParsers.parseCommunication(xmlMessage, false);
			assertTrue("ReSignBTest1: bad state", result.getState().equals(state));
			Log.d(TAG, "Communication id: " + result.getSessionId());
			
			ArrayList<BaseDevice> devices = (ArrayList<BaseDevice>) result.data;
			
			for(BaseDevice device : devices){
				Log.d(TAG, "Partial: " + device.debugString(false));
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue("PartialTest1: " + e.toString() ,false);
		}
	}

	public void testXml(){
		String xmlMessage =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><communication version=\"1.0\" id=\""+ID+"\" state=\"xml\" role=\"admin\">"
				+	"<adapter id=\"9999\">"
				+		"<version>1.0.0</version>"
				+		"<capabilities>"
				+			"<device initialized=\"1\" type=\"0x00\" id=\"120:00:FF:000:FFE\" visibility=\"I\">"
				+				"<location type=\"1\">Obývací pokoj</location>"
				+				"<name>Teplota u dverí</name>"
				+				"<refresh>12</refresh>"
				+				"<battery>70</battery>"
				+				"<quality>52</quality>"
				+				"<value>120</value>"
				+				"<logging enabled=\"0\"/>"
				+			"</device>"
				+			"<device initialized=\"1\" type=\"0x01\" id=\"120:00:FF:000:F0E\" visibility=\"O\">"
				+				"<location type=\"2\">Jiny pokoj</location>"
				+				"<name>Neco u dverí</name>"
				+				"<refresh>10</refresh>"
				+				"<battery>74</battery>"
				+				"<quality>57</quality>"
				+				"<value>10</value>"
				+				"<logging enabled=\"1\"/>"
				+			"</device>"
				+			"<device initialized=\"1\" type=\"0x03\" id=\"120:00:FF:010:FFE\" visibility=\"I\">"
				+				"<location type=\"3\">Kuchyn</location>"
				+				"<name>Aha u dverí</name>"
				+				"<refresh>72</refresh>"
				+				"<battery>50</battery>"
				+				"<quality>59</quality>"
				+				"<value>120</value>"
				+				"<logging enabled=\"0\"/>"
				+			"</device>"
				+		"</capabilities>"
				+	"</adapter>"
				+"</communication>";

		
		String state = "xml";
		
		Log.i(TAG, "XmlTest1");
		
		try {
//			HashMap<String, HashMap<Integer, Object>> result = XmlParsers.parseCommunication_old(xmlMessage, false);
//			for(Map.Entry<String, HashMap<Integer, Object>> entry : result.entrySet()){
//				assertTrue("XmlTest1: bad state", entry.getKey().equals(state));
//				
//				for(Map.Entry<Integer, Object> data : entry.getValue().entrySet()){
//					int id = data.getKey();
//					Adapter adapter =  (Adapter) data.getValue();
//					Log.d(TAG, "Communication id: " + id);
//					
//					Log.d(TAG, "Xml: "+adapter.toString());
//				}
//			}
			
			ParsedMessage result = XmlParsers.parseCommunication(xmlMessage, false);
			assertTrue("XmlTest1: bad state", result.getState().equals(state));
			Log.d(TAG, "Communication id: " + result.getSessionId());
			
			Adapter adapter = (Adapter) result.data;
			Log.d(TAG, "Xml: "+adapter.debugString());
		
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue("XmlTest1: " + e.toString() ,false);
		}
	}
}
