/**
 * @brief Package for manipulation with XML and parsers
 */

package cz.vutbr.fit.iha.network.xml;

import java.io.StringWriter;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.device.BaseDevice.SaveDevice;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.household.User;
import cz.vutbr.fit.iha.network.Network.NetworkAction;

/**
 * Class for creating XML file from Adapter object
 * @author ThinkDeep
 *
 */
public class XmlCreator {

	/**
	 * NameSpace
	 */
	public static final String ns = null;
	public static final String COM_ROOT = "communication";
	public static final String ID = "id";
	public static final String INIT_ID = "0";
	public static final String STATE = "state";
	public static final String HEX = "0x";
	public static final String VERSION = "version";
	/**
	 * Version of communication protocol for google/android device 
	 */
	public static final String GVER = "2.0";
	// states
	public static final String SIGNIN = "signin";
	public static final String SIGNUP = "signup";
	public static final String ADDADAPTER = "addadapter";
	public static final String INIT = "init";
	public static final String REINITADAPTER = "reinitadapter";
	public static final String GETLOG = "getlog";
	public static final String ADDACCOUNT = "addaccount";
	public static final String DELACCOUNT = "delaccount";
	public static final String GETACCOUNTS = "getaccounts";
	public static final String UPDATEACCOUNT = "updateaccount";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String DEVICES = "devices";
	public static final String GETADAPTERS = "getadapters";
	public static final String GETDEVICES = "getdevices";
	public static final String ADDVIEW = "addview";
	public static final String DELVIEW = "delview";
	public static final String UPDATEVIEW = "updateview";
	public static final String GETVIEWS = "getviews";
	public static final String SETTIMEZONE = "settimezone";
	public static final String GETTIMEZONE = "gettimezone";
	public static final String GETROOMS = "getrooms";
	public static final String UPDATEROOMS = "updaterooms";
	public static final String ADDROOM = "addroom";
	public static final String DELROOM = "delroom";
	public static final String GETALLDEVICES = "getalldevices";
	public static final String ADAPTERLISTEN = "adapterlisten";
	public static final String SWITCH = "switch";
	public static final String DELDEVICE = "deldevice";
	public static final String DELGCMID = "delgcmid";
	public static final String GETNOTIFICATIONS = "getnotifications";
	public static final String NOTIFICATIONREAD = "notificationread";
	public static final String GETNEWDEVICES = "getnewdevices";
	
	// end of states
	public static final String USER = "user";
	public static final String EMAIL = "email";
	public static final String GTOKEN = "gtoken";
	public static final String MODE = "mode";
	public static final String SERIAL = "serialnumber";
	public static final String NEXT = "next";
	public static final String ADAPTER = "adapter";
	public static final String NEW = "new";
	public static final String OLD = "old";
	public static final String OLDID = "oldid";
	public static final String NEWID = "newid";
	public static final String ROLE = "role";
	public static final String ADDITIONALINFO = "additionalinfo";
	public static final String FROM = "from";
	public static final String TO = "to";
	public static final String ACTION = "action";
	public static final String ICON = "icon";
	public static final String TIME = "time";
	public static final String UTC = "utc";
	public static final String LOCALE = "locale";
	public static final String ERRCODE = "errcode";
	public static final String INTERVAL = "interval";
	public static final String GCMID = "gcmid";
	public static final String NOTIFICAION = "notification";
	public static final String MSGID = "msgid";
	
	//partial
	public static final String DEVICE = "device";
	public static final String INITIALIZED = "initialized";
	public static final String TYPE = "type";
	public static final String VISIBILITY = "visibility";
	public static final String LOCATION = "location";
	public static final String NAME = "name";
	public static final String REFRESH = "refresh";
	public static final String BATTERY = "battery";
	public static final String QUALITY = "quality";
	public static final String VALUE = "value";
	public static final String LOGGING = "logging";
	public static final String ENABLED = "enabled";
	public static final String INIT_1 = "1";
	public static final String INIT_0 = "0";
	
	
	/**
	 * Method create XML for signIn message
	 * @param email of user
	 * @param gtoken token from google
	 * @param lokale language of App {cs, en}
	 * @return XML message
	 */
	public static String createSignIn(String email, String gtoken, String lokale){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, INIT_ID);
			serializer.attribute(ns, STATE, SIGNIN);
			serializer.attribute(ns, VERSION, GVER);
				serializer.startTag(ns, USER);
				serializer.attribute(ns, EMAIL, email);
				serializer.attribute(ns, GTOKEN, gtoken);
				serializer.attribute(ns, LOCALE, lokale);
				serializer.endTag(ns, USER);
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML for singUp message
	 * @param email of user 
	 * @param gtoken token from google
	 * @return XML message
	 */
	public static String createSignUp(String email, String gtoken){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, STATE, SIGNUP);
			serializer.attribute(ns, VERSION, GVER);
				
				serializer.startTag(ns, USER);
				serializer.attribute(ns, EMAIL, email);
				serializer.attribute(ns, GTOKEN, gtoken);
				serializer.endTag(ns, USER);

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML for AddAdapter message
	 * @param id of user
	 * @param serialNumber of adapter
	 * @param adapterName name of adapter
	 * @return XML message
	 */
	public static String createAddAdapter(String id, String serialNumber, String adapterName){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, ADDADAPTER);
			serializer.attribute(ns, VERSION, GVER);
			
				serializer.startTag(ns, ADAPTER);
				serializer.attribute(ns, ID, serialNumber);
				serializer.attribute(ns, NAME, adapterName);
				serializer.endTag(ns, ADAPTER);
				
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method create XML for AdapterListen message
	 * @param id of user
	 * @param adapterId
	 * @return XML of AdapterListen message
	 */
	public static String createAdapterListen(String id, String adapterId){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, ADAPTERLISTEN);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML for Switch message
	 * @param id of user
	 * @param adapterId
	 * @param device with data to change
	 * @return XML of Switch message
	 */
	public static String createSwitch(String id, String adapterId, BaseDevice device){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, SWITCH);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
				serializer.startTag(ns, DEVICE);
				serializer.attribute(ns, ID, device.getFacility().getAddress());
				serializer.attribute(ns, TYPE, formatType(device.getType()));
				
					serializer.startTag(ns, VALUE);
					serializer.text(device.getStringValue());
					serializer.endTag(ns, VALUE);
				
			serializer.endTag(ns, DEVICE);
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of DelDevice message
	 * @param id
	 * @param adapterId
	 * @param facility
	 * @return XML of DelDevice message
	 */
	public static String createDeleteFacility(String id, String adapterId, Facility facility){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, DELDEVICE);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
//				for (BaseDevice device : facility.getDevices()) {
//					serializer.startTag(ns, DEVICE);
//					serializer.attribute(ns, ID, facility.getAddress());
//					serializer.attribute(ns, TYPE, formatType(device.getType()));
//					serializer.endTag(ns, DEVICE);
//				}
				serializer.startTag(ns, DEVICE);
				serializer.attribute(ns, ID, facility.getAddress());
				serializer.endTag(ns, DEVICE);
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML for GetXml message
	 * @param id of user
	 * @param adapterId
	 * @return XML of GetXml message
	 */
	public static String createGetAllDevices(String id, String adapterId){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETALLDEVICES);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML for getting uninitialized devices
	 * @param id of user
	 * @param adapterId of wanted adapter
	 * @return XML of GetNewDevices message
	 */
	public static String createGetNewDevices(String id, String adapterId){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETNEWDEVICES);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML for ReInit message
	 * @param id of user
	 * @param adapterIdOld old id of adapter
	 * @param adapterIdNew new id of adapter
	 * @return ReInit message
	 */
	public static String createReInitAdapter(String id, String adapterIdOld, String adapterIdNew){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, REINITADAPTER);
			serializer.attribute(ns, VERSION, GVER);
				
				serializer.startTag(ns, ADAPTER);
				serializer.attribute(ns, OLDID, adapterIdOld);
				serializer.attribute(ns, NEWID, adapterIdNew);
				serializer.endTag(ns, ADAPTER);
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML for LogName message
	 * @param id of user
	 * @param adapterId
	 * @param deviceId id of sensor
	 * @param deviceType is type of sensor
	 * @param from date from probably based of format YYYY-MM-DD-HH:MM:SS
	 * @param funcType is aggregation function type {avg, median, ...}
	 * @param interval is time value in seconds that represents nicely e.g. month, week, day, 10 hours, 1 hour, ...
	 * @return logName message
	 */
	public static String createGetLog(String id, String adapterId, String deviceId, int deviceType, String from, String to, String funcType, int interval){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETLOG);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, FROM, from);
			serializer.attribute(ns, TO, to);
			serializer.attribute(ns, TYPE, funcType);
			serializer.attribute(ns, INTERVAL, String.valueOf(interval));
			serializer.attribute(ns, ADAPTER, adapterId);
			
				serializer.startTag(ns, DEVICE);
				serializer.attribute(ns, ID, deviceId);
				serializer.attribute(ns, TYPE, formatType(deviceType));
				serializer.endTag(ns, DEVICE);
				
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML for AddConAccount message
	 * @param id of user (superuser)
	 * @param adapterId
	 * @param users map with pairs e-mail of common user (key) and its role (value) 
	 * @return addConAccount message
	 */
	public static String createAddAccounts(String id, String adapterId, HashMap<String, String> users){
		return createAddOrChangeConAccount(id, adapterId, users, true);
	}
	
	/**
	 * Method create XML for AddAcount message
	 * @param id
	 * @param adapterId
	 * @param email
	 * @param role
	 * @return
	 */
	public static String createAddAccount(String id, String adapterId, String email, User.Role role){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, ADDACCOUNT);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
				serializer.startTag(ns, USER);
				serializer.attribute(ns, EMAIL, email);
				serializer.attribute(ns, ROLE, role.getValue());
				serializer.endTag(ns, USER);
				
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of UpdateAccount message
	 * @param id
	 * @param adapterId
	 * @param user
	 * @param role
	 * @return
	 */
	public static String createUpdateAccount(String id, String adapterId, User user, User.Role role){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, UPDATEACCOUNT);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
				serializer.startTag(ns, USER);
				serializer.attribute(ns, EMAIL, user.getEmail());
				serializer.attribute(ns, ROLE, role.getValue());
				serializer.endTag(ns, USER);
				
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML for DelConAccount message
	 * @param id of user (superuser)
	 * @param adapterId
	 * @param userEmails of common users
	 * @return dellConAccount message
	 */
	public static String createDelAccounts(String id, String adapterId, List<String> userEmails){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, DELACCOUNT);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
			for(String userEmail : userEmails){
				serializer.startTag(ns, USER);
				serializer.attribute(ns, EMAIL, userEmail);
				serializer.endTag(ns, USER);
			}
				
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of DelAccount message
	 * @param id
	 * @param adapterId
	 * @param user
	 * @return
	 */
	public static String createDelAccount(String id, String adapterId, User user){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, DELACCOUNT);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
				serializer.startTag(ns, USER);
				serializer.attribute(ns, EMAIL, user.getEmail());
				serializer.endTag(ns, USER);
				
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML for GetConAccount message
	 * @param id of user
	 * @param adapterId
	 * @return GetConAccount message
	 */
	public static String createGetAccount(String id, String adapterId){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETACCOUNTS);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Private method create add or change - conAccount message
	 * @param id of user (superuser)
	 * @param adapterId
	 * @param users map with pairs e-mail of common user (key) and its role (value)
	 * @param ADD if is TRUE, addConAccount is chosen, changeConAccount otherwise
	 * @return Add/ChangeConAccount message
	 */
	private static String createAddOrChangeConAccount(String id, String adapterId, HashMap<String, String> users, boolean ADD){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, (ADD)?ADDACCOUNT:UPDATEACCOUNT);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
			for(Entry<String, String> user : users.entrySet()){
				serializer.startTag(ns, USER);
				serializer.attribute(ns, EMAIL, user.getKey());
				serializer.attribute(ns, ROLE, user.getValue());
				serializer.endTag(ns, USER);
			}
				
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of ChangeConAccount message
	 * @param id of user (superuser)
	 * @param adapterId
	 * @param users map with pairs e-mail of common user (key) and its role (value)
	 * @return changeConAccount message
	 */
	public static String createUpdateAccounts(String id, String adapterId, HashMap<String, String> users){
		return createAddOrChangeConAccount(id, adapterId, users, false);
	}
	
	/**
	 * Method create XML of TRUE message
	 * @param id of user
	 * @param additionalInfo contains state of recieved message
	 * @return TRUE message
	 */
	public static String createTRUE(String id, String additionalInfo){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, TRUE);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADDITIONALINFO, additionalInfo);
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method create XML of FALSE message, the additional info and its description
	 * is probably NOT used in android client App for any known
	 * messages, but it can passed it if some other method create sub XML
	 * @param id of user
	 * @param additionalInfo contains state of received message
	 * @param planeText contains string with error description 
	 * @return FALSE message
	 */
	@Deprecated
	public static String createFALSE(String id, String additionalInfo, int errCode, String planeText){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, FALSE);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADDITIONALINFO, additionalInfo);
			serializer.attribute(ns, ERRCODE, Integer.toString(errCode));
				
			if(planeText != null)
				serializer.text(planeText);
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of PARTIAL message. Almost all fields are optional.
	 * Use field only if it is NOT null or ZERO.
	 * Android app can NOT set filename of logging file, only turn on/off its existence =>
	 * this method switch logging by value getting from isLogging(), but check this only if
	 * some NON-null value is in getLog() field
	 * @param id of user
	 * @param adapterId
	 * @param devices with changed fields only (use only NON-null and NON-zero values)
	 * @return Partial message
	 */
	public static String createDevices(String id, String adapterId, List<BaseDevice> devices){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, DEVICES);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
			for(BaseDevice device : devices){
				Facility facility = device.getFacility();
				
				serializer.startTag(ns, DEVICE);
				serializer.attribute(ns, INITIALIZED, (facility.isInitialized())?INIT_1:INIT_0);
				serializer.attribute(ns, TYPE, formatType(device.getType()));
				serializer.attribute(ns, ID, facility.getAddress());
				serializer.attribute(ns, VISIBILITY, (facility.getVisibility())?INIT_1:INIT_0);
				
				if(facility.getLocationId() != null){
					serializer.startTag(ns, LOCATION);
					serializer.attribute(ns, ID, facility.getLocationId());
					serializer.endTag(ns, LOCATION);
				}
				if(device.getName() != null){
					serializer.startTag(ns, NAME);
					serializer.text(device.getName());
					serializer.endTag(ns, NAME);
				}
				if(facility.getRefresh() != null){
					serializer.startTag(ns, REFRESH);
					serializer.text(Integer.toString(facility.getRefresh().getInterval()));
					serializer.endTag(ns, REFRESH);
				}
				if(device.getRawIntValue() != Integer.MAX_VALUE){
					serializer.startTag(ns, VALUE);
					serializer.text(device.getStringValue());
					serializer.endTag(ns, VALUE);
				}
				
				serializer.startTag(ns, LOGGING);
				serializer.attribute(ns, ENABLED, (device.isLogging())?INIT_1:INIT_0);
				serializer.endTag(ns, LOGGING);
				
				serializer.endTag(ns, DEVICE);
			}
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * New method create XML of devices message with only one device in it.
	 * To save device field must by not null and toSave parameter must by set properly.
	 * @param id of user
	 * @param adapterId id of adapter
	 * @param device to save
	 * @param toSave ECO mode to save only wanted fields
	 * @return Devices message
	 */
	public static String createDevice(String id, String adapterId, BaseDevice device, EnumSet<SaveDevice> toSave){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			Facility facility = device.getFacility();
			
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, DEVICES);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
				serializer.startTag(ns, DEVICE);
				serializer.attribute(ns, INITIALIZED, (facility.isInitialized())?INIT_1:INIT_0);
				serializer.attribute(ns, TYPE, formatType(device.getType()));
				serializer.attribute(ns, ID, facility.getAddress());
				
				if(toSave.contains(SaveDevice.SAVE_VISIBILITY))
					serializer.attribute(ns, VISIBILITY, (facility.getVisibility())?INIT_1:INIT_0);
				
				if(toSave.contains(SaveDevice.SAVE_LOCATION) && facility.getLocationId() != null){
					serializer.startTag(ns, LOCATION);
					serializer.attribute(ns, ID, facility.getLocationId());
					serializer.endTag(ns, LOCATION);
				}
				if(toSave.contains(SaveDevice.SAVE_NAME) && device.getName() != null){
					serializer.startTag(ns, NAME);
					serializer.text(device.getName());
					serializer.endTag(ns, NAME);
				}
				if(toSave.contains(SaveDevice.SAVE_REFRESH) && facility.getRefresh() != null){
					serializer.startTag(ns, REFRESH);
					serializer.text(Integer.toString(facility.getRefresh().getInterval()));
					serializer.endTag(ns, REFRESH);
				}
				if(toSave.contains(SaveDevice.SAVE_VALUE) && device.getStringValue() != null && device.getStringValue().length() > 0){
					serializer.startTag(ns, VALUE);
					serializer.text(device.getStringValue());
					serializer.endTag(ns, VALUE);
				}
				
				if(toSave.contains(SaveDevice.SAVE_LOGGING)){
					serializer.startTag(ns, LOGGING);
					serializer.attribute(ns, ENABLED, (device.isLogging())?INIT_1:INIT_0);
					serializer.endTag(ns, LOGGING);
				}
				
				serializer.endTag(ns, DEVICE);
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of GetAdapters message
	 * @param id of user
	 * @return GetAdapters message
	 */
	public static String createGetAdapters(String id){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETADAPTERS);
			serializer.attribute(ns, VERSION, GVER);
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of GetAdapters message
	 * @param id of user
	 * @return GetAdapters message
	 */
	public static String createGetAdapters(int id){
		return createGetAdapters(Integer.toString(id));
	}
	
	/**
	 * Method create XML of GetDevices message
	 * @param id of user
	 * @param adapterId
	 * @param devicesId Id of devices to get update fields
	 * @return update message
	 */
	public static String createGetFacilities(String id, String adapterId, List<Facility>facilities){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETDEVICES);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
			for(Facility facility : facilities){
				for (BaseDevice device : facility.getDevices()) {
					serializer.startTag(ns, DEVICE);
					serializer.attribute(ns, ID, facility.getAddress());
					serializer.attribute(ns, TYPE, formatType(device.getType()));
					serializer.endTag(ns, DEVICE);
				}
			}
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of GetDevices message
	 * @param id
	 * @param adapterId
	 * @param facility
	 * @return
	 */
	public static String createGetFacility(String id, String adapterId, Facility facility){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETDEVICES);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
			for (BaseDevice device : facility.getDevices()) {
				serializer.startTag(ns, DEVICE);
				serializer.attribute(ns, ID, facility.getAddress());
				serializer.attribute(ns, TYPE, formatType(device.getType()));
				serializer.endTag(ns, DEVICE);
			}
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of AddView message
	 * @param id of user
	 * @param adapterId
	 * @param viewName name of custom view
	 * @param devicesId list of devices id
	 * @return addView message
	 */
	public static String createAddView(String id, String viewName, int iconNum, List<BaseDevice>devices){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, ADDVIEW);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, NAME, viewName);
			serializer.attribute(ns, ICON, Integer.toString(iconNum));
			
			for(BaseDevice device : devices){
				serializer.startTag(ns, DEVICE);
				serializer.attribute(ns, ID, device.getId());
				serializer.attribute(ns, TYPE, formatType(device.getType()));
				serializer.endTag(ns, DEVICE);
			}
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of GetViews message (method added in 1.6 version)
	 * @param id of user
	 * @param adapterId
	 * @return getViews message
	 */
	public static String createGetViews(String id){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETVIEWS);
			serializer.attribute(ns, VERSION, GVER);
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of DelVIew message
	 * @param id of user
	 * @param adapterId
	 * @param viewName of custom view
	 * @return DelVIew message
	 */
	public static String createDelView(String id, String viewName){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, DELVIEW);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, NAME, viewName);
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of UpdateView message
	 * @param id of user
	 * @param adapterId
	 * @param viewName of custom view
	 * @param devices hashMap with device id as key, and action as value
	 * @return UpdateValue message
	 */
	public static String createUpdateViews(String id, String viewName, int iconNum, HashMap<String, String> devices){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, UPDATEVIEW);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, NAME, viewName);
			serializer.attribute(ns, ICON, Integer.toString(iconNum));
			
			for(Entry<String, String> device : devices.entrySet()){
				serializer.startTag(ns, DEVICE);
				serializer.attribute(ns, ID, device.getKey());
				serializer.attribute(ns, ACTION, device.getValue());
				serializer.endTag(ns, DEVICE);
			}
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create one view to update message
	 * @param id
	 * @param viewName
	 * @param iconNum
	 * @param device
	 * @param action
	 * @return
	 */
	public static String createUpdateView(String id, String viewName, int iconNum, Facility device, NetworkAction action){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, UPDATEVIEW);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, NAME, viewName);
			serializer.attribute(ns, ICON, Integer.toString(iconNum));
			
				serializer.startTag(ns, DEVICE);
				serializer.attribute(ns, ID, device.getId());
				serializer.attribute(ns, ACTION, action.getValue());
				serializer.endTag(ns, DEVICE);
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of SetTimeZone message
	 * @param id of user
	 * @param adapterId
	 * @param diffToGMT difference to GMT (UTC+0), range <-12,12> 
	 * @return SetTimeZone message
	 */
	public static String createSetTimeZone(String id, String adapterId, int diffToGMT){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, SETTIMEZONE);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
				serializer.startTag(ns, TIME);
				serializer.attribute(ns, UTC, Integer.toString(diffToGMT));
				serializer.endTag(ns, TIME);
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of GetTimeZone message
	 * @param id of user
	 * @param adapterId
	 * @return GetTimeZone message
	 */
	public static String createGetTimeZone(String id, String adapterId){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETTIMEZONE);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of GetRooms message
	 * @param id of user
	 * @param adapterId
	 * @return message GetRooms
	 */
	public static String createGetRooms(String id, String adapterId){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETROOMS);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of UpdateRooms message
	 * @param id of user
	 * @param adapterId
	 * @param locations list of location object to update
	 * @return message UpdateRooms
	 */
	public static String createUpdateRooms(String id, String adapterId, List<Location> locations){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, UPDATEROOMS);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
			for(Location location : locations){
				serializer.startTag(ns, LOCATION);
				serializer.attribute(ns, ID, location.getId());
				serializer.attribute(ns, TYPE, Integer.toString(location.getType()));
				serializer.text(location.getName());
				serializer.endTag(ns, LOCATION);
			}
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of UpdateRooms message
	 * @param id
	 * @param adapterId
	 * @param location
	 * @return
	 */
	public static String createUpdateRoom(String id, String adapterId, Location location){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, UPDATEROOMS);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
				serializer.startTag(ns, LOCATION);
				serializer.attribute(ns, ID, location.getId());
				serializer.attribute(ns, TYPE, Integer.toString(location.getType()));
				serializer.text(location.getName());
				serializer.endTag(ns, LOCATION);
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of AddRoom message
	 * @param id of user
	 * @param adapterId
	 * @param location to create
	 * @return created message
	 */
	public static String createAddRooms(String id, String adapterId, Location location){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, ADDROOM);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
				serializer.startTag(ns, LOCATION);
				serializer.attribute(ns, TYPE, Integer.toString(location.getType()));
				serializer.text(location.getName());
				serializer.endTag(ns, LOCATION);
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of DelRoom message
	 * @param id
	 * @param adapterId
	 * @param location
	 * @return
	 */
	public static String createDelRooms(String id, String adapterId, Location location){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, DELROOM);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
				serializer.startTag(ns, LOCATION);
				serializer.attribute(ns, ID, location.getId());
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of DelGCMID message (delete google cloud message id)
	 * @param id of user logged in now
	 * @param email of last logged user
	 * @param gcmid id of google messaging
	 * @return message DelGCMID
	 */
	public static String createDeLGCMID(String id, String email, String gcmid){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, DELGCMID);
			serializer.attribute(ns, VERSION, GVER);
			
				serializer.startTag(ns, USER);
				serializer.attribute(ns, EMAIL, email);
				serializer.attribute(ns, GCMID, gcmid);
				serializer.endTag(ns, USER);
				
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of GetNotifications message
	 * @param id of user
	 * @return message GetNotifications
	 */
	public static String createGetNotifications(String id){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETNOTIFICATIONS);
			serializer.attribute(ns, VERSION, GVER);
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of NotificationRead message
	 * @param id of user
	 * @param msgid id of read notification
	 * @return message NotificationRead
	 */
	public static String createNotificaionRead(String id, String msgid){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, NOTIFICATIONREAD);
			serializer.attribute(ns, VERSION, GVER);
			
				serializer.startTag(ns, NOTIFICAION);
				serializer.attribute(ns, MSGID, msgid);
				serializer.endTag(ns, NOTIFICAION);
				
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private static String formatType(int type) {
		String hex = Integer.toHexString(type);
		if (hex.length() == 1)
			hex = "0" + hex;
		
		return "0x" + hex;
	}
	
}
