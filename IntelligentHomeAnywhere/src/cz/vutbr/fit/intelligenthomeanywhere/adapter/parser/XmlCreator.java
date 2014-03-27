/**
 * @brief Package for manipulation with XML and parsers
 */

package cz.vutbr.fit.intelligenthomeanywhere.adapter.parser;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;

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
	public static final String GVER = "1.0";
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
	public static final String ENABLED = "endabled";
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
	 * @return XML message
	 */
	public String createSignIn(String email, String gtoken, boolean quiet){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, INIT_ID);
			serializer.attribute(ns, STATE, SIGNIN);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, MODE, (quiet) ? QUIET : NORMAL);
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
	 * Method create XML for singUp message
	 * @param email of user
	 * @param id is 0 if is NOT signed in, non-zero otherwise 
	 * @param gtoken token from google
	 * @param serialNumber of new adapter
	 * @param next if id is non-zero and user is signed in, that TRUE is using new Adapter, old otherwise
	 * @return XML message
	 */
	public String createSignUp(String email, String id, String gtoken, String serialNumber, boolean next){
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
				
				if(id.equals(INIT_ID)){
					serializer.startTag(ns, USER);
					serializer.attribute(ns, EMAIL, email);
					serializer.attribute(ns, GTOKEN, gtoken);
					serializer.endTag(ns, USER);
				}else{
					serializer.startTag(ns, NEXT);
					serializer.attribute(ns, ADAPTER, (next)?NEW:OLD);
					serializer.endTag(ns, NEXT);
				}
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
	 * @param next if id is non-zero and user is signed in, that TRUE is using new Adapter, old otherwise
	 * @return XML message
	 */
	public String createSignUp(String email, int id, String gtoken, int serialNumber, boolean next){
		return createSignUp(email, Integer.toString(id), gtoken, Integer.toString(serialNumber), next);
	}
	
	/**
	 * Method create XML for init[x1] message
	 * @param id of user
	 * @param adapterId id of adapter to work with
	 * @return init message
	 */
	public String createInit(String id, String adapterId){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, INIT);
			serializer.attribute(ns, VERSION, GVER);
				
				serializer.startTag(ns, ADAPTER);
				serializer.attribute(ns, ID, adapterId);
				serializer.endTag(ns, ADAPTER);
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML for init[x1] message
	 * @param id of user
	 * @param adapterId id of adapter to work with
	 * @return init message
	 */
	public String createInit(int id, int adapterId){
		return createInit(Integer.toString(id), Integer.toString(adapterId));
	}
	
	/**
	 * Method create XML for ReInit message
	 * @param id of user
	 * @param adapterIdOld old id of adapter
	 * @param adapterIdNew new id of adapter
	 * @return ReInit message
	 */
	public String createReInit(String id, String adapterIdOld, String adapterIdNew){
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
	 * Method create XML for ReInit message
	 * @param id of user
	 * @param adapterIdOld old id of adapter
	 * @param adapterIdNew new id of adapter
	 * @return ReInit message
	 */
	public String createReInit(int id, int adapterIdOld, int adapterIdNew){
		return createReInit(Integer.toString(id), Integer.toString(adapterIdOld), Integer.toString(adapterIdNew));
	}

	/**
	 * Method create XML for LogName message
	 * @param id of user
	 * @param deviceId id of sensor
	 * @param from date from probably based of format YYYY-MM-DD-HH:MM:SS
	 * @return logName message
	 */
	public String createLogName(String id, String deviceId, String from, String to){
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
			
				serializer.startTag(ns, DEVICE);
				serializer.attribute(ns, ID, deviceId);
				serializer.endTag(ns, DEVICE);
				
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
	 * @param deviceId id of sensor
	 * @param from date from probably based of format YYYY-MM-DD-HH:MM:SS
	 * @return logName message
	 */
	public String createLogName(int id, String deviceId, String from, String to){
		return createLogName(Integer.toString(id), deviceId, from, to);
	}
	
	/**
	 * Method create XML for AddConAccount message
	 * @param id of user (superuser)
	 * @param users map with pairs e-mail of common user (key) and its role (value) 
	 * @return addConAccount message
	 */
	public String createAddConAccount(String id, HashMap<String, String> users){
		return createAddOrChangeConAccount(id, users, true);
	}
	
	/**
	 * Method create XML for AddConAccount message
	 * @param id of user (superuser)
	 * @param users map with pairs e-mail of common user (key) and its role (value) 
	 * @return addConAccount message
	 */
	public String createAddConAccount(int id, HashMap<String, String> users){
		return createAddConAccount(Integer.toString(id), users);
	}
	
	/**
	 * Method create XML for DelConAccount message
	 * @param id of user (superuser)
	 * @param userEmails of common users
	 * @return dellConAccount message
	 */
	public String createDelConAccount(String id, ArrayList<String> userEmails){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, DELCONACCOUNT);
			serializer.attribute(ns, VERSION, GVER);
			
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
	 * Method create XML for DelConAccount message
	 * @param id of user (superuser)
	 * @param userEmails of common users
	 * @return dellConAccount message
	 */
	public String createDelConAccount(int id, ArrayList<String> userEmails){
		return createDelConAccount(Integer.toString(id),userEmails);
	}

	/**
	 * Method create XML for GetConAccount message
	 * @param id of user
	 * @return GetConAccount message
	 */
	public String createGetConAccount(String id){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETCONACCOUNT);
			serializer.attribute(ns, VERSION, GVER);
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
	 * @return GetConAccount message
	 */
	public String createGetConAccount(int id){
		return createGetConAccount(Integer.toString(id));
	}
	
	/**
	 * Private method create add or change - conAccount message
	 * @param id of user (superuser)
	 * @param users map with pairs e-mail of common user (key) and its role (value)
	 * @param ADD if is TRUE, addConAccount is chosen, changeConAccount otherwise
	 * @return Add/ChangeConAccount message
	 */
	private String createAddOrChangeConAccount(String id, HashMap<String, String> users, boolean ADD){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, (ADD)?ADDCONACCOUNT:CHANGECONACCOUNT);
			serializer.attribute(ns, VERSION, GVER);
			
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
	 * @param users map with pairs e-mail of common user (key) and its role (value)
	 * @return changeConAccount message
	 */
	public String createChangeConAccount(String id, HashMap<String, String> users){
		return createAddOrChangeConAccount(id, users, false);
	}
	
	/**
	 * Method create XML of ChangeConAccount message
	 * @param id of user (superuser)
	 * @param users map with pairs e-mail of common user (key) and its role (value)
	 * @return changeConAccount message
	 */
	public String createChangeConAccount(int id, HashMap<String, String> users){
		return createChangeConAccount(Integer.toString(id), users);
	}

	/**
	 * Method create XML of TRUE message
	 * @param id of user
	 * @param additionalInfo contains state of recieved message
	 * @return TRUE message
	 */
	public String createTRUE(String id, String additionalInfo){
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
	 * Method create XML of TRUE message
	 * @param id of user
	 * @param additionalInfo contains state of recieved message
	 * @return TRUE message
	 */
	public String createTRUE(int id, String additionalInfo){
		return createTRUE(Integer.toString(id), additionalInfo);
	}

	/**
	 * Method create XML of FALSE message, the additional info and its description
	 * (XMLedSubMessage) is probably NOT used in android client app for any known
	 * messages, but it can passed it if some other method create sub XML
	 * @param id of user
	 * @param additionalInfo contains state of recieved message
	 * @param XMLedSubMessage contains string with XML of subMessage of error description 
	 * @return FALSE message
	 */
	//FIXME: XMLedSubMessage need to by repaired
	public String createFALSE(String id, String additionalInfo, String XMLedSubMessage){
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
				
			if(XMLedSubMessage != null)
				serializer.text(XMLedSubMessage);
			
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();
			
			return writer.toString();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	//FIXME: XMLedSubMessage need to by repaired
	/**
	 * Method create XML of FALSE message, the additional info and its description
	 * (XMLedSubMessage) is probably NOT used in android client app for any known
	 * messages, but it can passed it if some other method create sub XML
	 * @param id of user
	 * @param additionalInfo contains state of recieved message
	 * @param XMLedSubMessage contains string with XML of subMessage of error description 
	 * @return FALSE message
	 */	
	public String createFALSE(int id, String additionalInfo, String XMLedSubMessage){
		return createFALSE(Integer.toString(id), additionalInfo, XMLedSubMessage);
	}
	
	/**
	 * Method create XML of PARTIAL message. Almost all fields are optional.
	 * Use field only if it is NOT null or ZERO.
	 * Android app can NOT set filename of logging file, only turn on/off its existence =>
	 * this method switch logging by value getting from isLogging(), but check this only if
	 * some NON-null value is in getLog() field
	 * @param id of user
	 * @param devices with changed fields only (use only NON-null and NON-zero values)
	 * @return Partial message
	 */
	public String createPartial(String id, ArrayList<BaseDevice> devices){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, PARTIAL);
			serializer.attribute(ns, VERSION, GVER);
			
			for(BaseDevice device : devices){
				serializer.startTag(ns, DEVICE);
				serializer.attribute(ns, INITIALIZED, (device.isInitialized())?INIT_1:INIT_0);
				serializer.attribute(ns, TYPE, HEX+Integer.toHexString(device.getType()));
				serializer.attribute(ns, ID, device.getId());
				serializer.attribute(ns, VISIBILITY, Character.toString(device.getVisibility()));
				
				if(device.getLocation() != null){
					serializer.startTag(ns, LOCATION);
					serializer.attribute(ns, TYPE, device.getLocationType()+"");
					serializer.text(device.getLocation());
					serializer.endTag(ns, LOCATION);
				}
				if(device.getName() != null){
					serializer.startTag(ns, NAME);
					serializer.text(device.getName());
					serializer.endTag(ns, NAME);
				}
				if(device.getRefresh() != 0){
					serializer.startTag(ns, REFRESH);
					serializer.text(Integer.toString(device.getRefresh()));
					serializer.endTag(ns, REFRESH);
				}
				if(device.getRawIntValue() != Integer.MAX_VALUE){
					serializer.startTag(ns, VALUE);
					serializer.text(device.getStringValue());
					serializer.endTag(ns, VALUE);
				}
				// TODO: do it better
				
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
	 * Method create XML of PARTIAL message. Almost all fields are optional.
	 * Use field only if it is NOT null or ZERO.
	 * Android app can NOT set filename of logging file, only turn on/off its existence =>
	 * this method switch logging by value getting from isLogging(), but check this only if
	 * some NON-null value is in getLog() field
	 * @param id of user
	 * @param devices with changed fields only (use only NON-null and NON-zero values)
	 * @return Partial message
	 */
	public String createPartial(int id, ArrayList<BaseDevice> devices){
		return createPartial(Integer.toString(id), devices);
	}
	
	/**
	 * Method create XML of GetAdapters message
	 * @param id of user
	 * @return GetAdapters message
	 */
	public String createGetAdapters(String id){
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
	public String createGetAdapters(int id){
		return createGetAdapters(Integer.toString(id));
	}
	
	/**
	 * Method create XML of Update message
	 * @param id of user
	 * @param devicesId Id of devices to get update fields
	 * @return update message
	 */
	public String createUpdate(String id, ArrayList<String>devicesId){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);
			
			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, UPDATE);
			serializer.attribute(ns, VERSION, GVER);
			
			for(String deviceId : devicesId){
				serializer.startTag(ns, DEVICE);
				serializer.attribute(ns, ID, deviceId);
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
	 * Method create XML of Update message
	 * @param id of user
	 * @param devicesId Id of devices to get update fields
	 * @return update message
	 */
	public String createUpdate(int id, ArrayList<String>devicesId){
		return createUpdate(Integer.toString(id), devicesId);
	}
	
	/**
	 * Method create XML of AddView message
	 * @param id of user
	 * @param viewName name of custom view
	 * @param devicesId list of devices id
	 * @return addView message
	 */
	public String createAddView(String id, String viewName, int iconNum, ArrayList<String>devicesId){
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
			
			for(String deviceId : devicesId){
				serializer.startTag(ns, DEVICE);
				serializer.attribute(ns, ID, deviceId);
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
	 * @param viewName name of custom view
	 * @param devicesId list of devices id
	 * @return addView message
	 */
	public String createAddView(int id, String viewName, int iconNum, ArrayList<String>devicesId){
		return createAddView(Integer.toString(id), viewName, iconNum, devicesId);
	}
	
	/**
	 * Method create XML of DelVIew message
	 * @param id of user
	 * @param viewName of custom view
	 * @return DelVIew message
	 */
	public String createDelView(String id, String viewName){
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
	 * Method create XML of DelVIew message
	 * @param id of user
	 * @param viewName of custom view
	 * @return DelVIew message
	 */
	public String createDelView(int id, String viewName){
		return createDelView(Integer.toString(id), viewName);
	}
	
	/**
	 * Method create XML of UpdateView message
	 * @param id of user
	 * @param viewName of custom view
	 * @param devices hashMap with device id as key, and action as value
	 * @return UpdateValue message
	 */
	public String createUpdateView(String id, String viewName, int iconNum, HashMap<String, String> devices){
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
	 * Method create XML of UpdateView message
	 * @param id of user
	 * @param viewName of custom view
	 * @param devices hashMap with device id as key, and action as value
	 * @return UpdateValue message
	 */
	public String createUpdateView(int id, String viewName, int iconNum, HashMap<String, String> devices){
		return createUpdateView(Integer.toString(id), viewName, iconNum, devices);
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
					
				for (BaseDevice d : mAdapter.getDevices().values()) {
					serializer.startTag(null, "device");
					serializer.attribute(null, "initialized", (d.isInitialized() ? "1" : "0"));
					serializer.attribute(null, "type", "0x" + Integer.toHexString(d.getType()));
					if(!d.isInitialized())
						serializer.attribute(null, "involved", d.getInvolveTime());
					
						serializer.startTag(null, "location");
						serializer.text((d.getLocation() != null) ? d.getLocation() : "");
						serializer.endTag(null, "location");
						
						serializer.startTag(null, "name");
						serializer.text((d.getName() != null) ? d.getName() : "");
						serializer.endTag(null, "name");
						
						serializer.startTag(null, "refresh");
						serializer.text(Integer.toString(d.getRefresh()));
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
	
	@Deprecated
	/**
	 * Method saving XML file with filename on a phone to folder
	 * @param filename name of new writing XML file
	 * @param dir is path to the file
	 */
	public void saveXml(String dir, String filename){
		try{
			byte[] buffer = this.create().getBytes("UTF-8");
			File file = new File(dir,filename);
			DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
			out.flush();
			out.write(buffer,0,buffer.length);
			out.close();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
