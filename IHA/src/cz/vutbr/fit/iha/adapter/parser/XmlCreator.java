/**
 * @brief Package for manipulation with XML and parsers
 */

package cz.vutbr.fit.iha.adapter.parser;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.location.Location;

/**
 * Class for creating XML file from Adapter object
 * @author ThinkDeep
 *
 */
public class XmlCreator {

	private Adapter mAdapter;
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
	public static final String GVER = "1.9";
	// states
	public static final String SIGNIN = "signin";
	public static final String SIGNUP = "signup";
	public static final String INIT = "init";
	public static final String REINIT = "reinit";
	public static final String LOGNAME = "logname";
	public static final String ADDCONACCOUNT = "addconaccount";
	public static final String DELCONACCOUNT = "delconaccount";
	public static final String GETCONACCOUNT = "getconaccount";
	public static final String CHANGECONACCOUNT = "changeconaccount";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String PARTIAL = "partial";
	public static final String GETADAPTERS = "getadapters";
	public static final String UPDATE = "update";
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
	public static final String GETXML = "getxml";
	public static final String ADAPTERLISTEN = "adapterlisten";
	public static final String SWITCH = "switch";
	public static final String DELDEVICE = "deldevice";
	
	public static final String GETALERTS = "getalerts";
	
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
	public static final String QUIET = "q";
	public static final String NORMAL = "n";
	public static final String ICON = "icon";
	public static final String TIME = "time";
	public static final String UTC = "utc";
	public static final String LOCALE = "locale";
	public static final String ERRCODE = "errcode";
	public static final String INTERVAL = "interval";
	
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
	 * Constructor
	 * @param cap
	 */
	public XmlCreator(Adapter cap){
		mAdapter = cap;
	}
	
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
	 * @param id is 0 if is NOT signed in, non-zero otherwise 
	 * @param gtoken token from google
	 * @param serialNumber of new adapter
	 * @param lokale language of APP {cs, en}
	 * @return XML message
	 */
	public static String createSignUp(String email, String id, String gtoken, String serialNumber, String lokale){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, SIGNUP);
			serializer.attribute(ns, VERSION, GVER);
				serializer.startTag(ns, SERIAL);
					serializer.text(serialNumber);
				serializer.endTag(ns, SERIAL);
				
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
				serializer.attribute(ns, ID, device.getAddress());
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
	 * @param device
	 * @return XML of DelDevice message
	 */
	public static String createDeleteDevice(String id, String adapterId, BaseDevice device){
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
				serializer.attribute(ns, ID, device.getAddress());
				serializer.attribute(ns, TYPE, formatType(device.getType()));
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
	public static String createGetXml(String id, String adapterId){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETXML);
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
	public static String createReInit(String id, String adapterIdOld, String adapterIdNew){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, REINIT);
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
	public static String createLogName(String id, String adapterId, String deviceId, int deviceType, String from, String to, String funcType, int interval){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, LOGNAME);
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
	public static String createAddConAccount(String id, String adapterId, HashMap<String, String> users){
		return createAddOrChangeConAccount(id, adapterId, users, true);
	}
	
	/**
	 * Method create XML for DelConAccount message
	 * @param id of user (superuser)
	 * @param adapterId
	 * @param userEmails of common users
	 * @return dellConAccount message
	 */
	public static String createDelConAccount(String id, String adapterId, List<String> userEmails){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, DELCONACCOUNT);
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
	 * Method create XML for GetConAccount message
	 * @param id of user
	 * @param adapterId
	 * @return GetConAccount message
	 */
	public static String createGetConAccount(String id, String adapterId){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETCONACCOUNT);
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
			serializer.attribute(ns, STATE, (ADD)?ADDCONACCOUNT:CHANGECONACCOUNT);
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
	public static String createChangeConAccount(String id, String adapterId, HashMap<String, String> users){
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
			serializer.attribute(ns, ERRCODE, errCode+"");
				
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
	//TODO: do eco mode
	public static String createPartial(String id, String adapterId, List<BaseDevice> devices){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, PARTIAL);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
			for(BaseDevice device : devices){
				serializer.startTag(ns, DEVICE);
				serializer.attribute(ns, INITIALIZED, (device.isInitialized())?INIT_1:INIT_0);
				serializer.attribute(ns, TYPE, formatType(device.getType()));
				serializer.attribute(ns, ID, device.getAddress());
				serializer.attribute(ns, VISIBILITY, device.getVisibility().getValue());
				
				if(device.getLocationId() != null){
					serializer.startTag(ns, LOCATION);
					serializer.attribute(ns, ID, device.getLocationId());
					serializer.endTag(ns, LOCATION);
				}
				if(device.getName() != null){
					serializer.startTag(ns, NAME);
					serializer.text(device.getName());
					serializer.endTag(ns, NAME);
				}
				if(device.getRefresh() != null){
					serializer.startTag(ns, REFRESH);
					serializer.text(Integer.toString(device.getRefresh().getInterval()));
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
	 * Method create XML of Update message
	 * @param id of user
	 * @param adapterId
	 * @param devicesId Id of devices to get update fields
	 * @return update message
	 */
	public static String createUpdate(String id, String adapterId, List<BaseDevice>devices){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, UPDATE);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);
			
			for(BaseDevice device : devices){
				serializer.startTag(ns, DEVICE);
				serializer.attribute(ns, ID, device.getAddress());
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
	public static String createUpdateView(String id, String viewName, int iconNum, HashMap<String, String> devices){
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
				serializer.attribute(ns, UTC, diffToGMT+"");
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
				serializer.attribute(ns, TYPE,location.getType()+"");
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
				serializer.attribute(ns, TYPE,location.getType()+"");
				serializer.text(location.getName());
			
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
	 * Method create XML of GetAlerts message
	 * @param id of user
	 * @return message GetAlerts
	 */
	public static String createGetAlerts(String id){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETALERTS);
			serializer.attribute(ns, VERSION, GVER);
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	@Deprecated
	/**
	 * Method for creating XML file (string)
	 * @return String contains XML file
	 */
 	public String create(){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			serializer.startTag(null, "adapter");
			serializer.attribute(null,"id", mAdapter.getId());
				serializer.startTag(null, "version");
				serializer.text(mAdapter.getVersion());
				serializer.endTag(null, "version");
			
				serializer.startTag(null, "capabilities");
					
				for (BaseDevice d : mAdapter.getDevices()) {
					serializer.startTag(null, "device");
					serializer.attribute(null, "initialized", (d.isInitialized() ? "1" : "0"));
					serializer.attribute(null, "type", formatType(d.getType()));
					if(!d.isInitialized())
						serializer.attribute(null, "involved", d.getInvolveTime());
					
						serializer.startTag(null, "location");
						serializer.text((d.getLocationId() != null) ? d.getLocationId() : "");
						serializer.endTag(null, "location");
						
						serializer.startTag(null, "name");
						serializer.text((d.getName() != null) ? d.getName() : "");
						serializer.endTag(null, "name");
						
						serializer.startTag(null, "refresh");
						serializer.text(Integer.toString(d.getRefresh().getInterval()));
						serializer.endTag(null, "refresh");
						
						serializer.startTag(null, "battery");
						serializer.text(Integer.toString(d.getBattery()));
						serializer.endTag(null, "battery");
						
						serializer.startTag(null, "network");
							serializer.startTag(null, "address");
							serializer.text(d.getAddress());
							serializer.endTag(null, "address");
							
							serializer.startTag(null, "quality");
							serializer.text(Integer.toString(d.getQuality()));
							serializer.endTag(null, "quality");
						serializer.endTag(null, "network");
						
						serializer.startTag(null, "value");
						serializer.text(d.getStringValue());
						serializer.endTag(null, "value");
							
						serializer.startTag(null, "logging");
						serializer.attribute(null, "enabled", (d.isLogging() ? "1" : "0"));
/*						if(d.isLogging())
							serializer.text(d.getLog());*/
						serializer.endTag(null, "logging");
					serializer.endTag(null, "device");
				}
				serializer.endTag(null, "capabilities");
			serializer.endTag(null, "adapter");
			
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
