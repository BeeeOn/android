/**
 * @brief Package for manipulation with XML and parsers
 */

package cz.vutbr.fit.iha.network.xml;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.adapter.device.AdapterAddressComparator;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.device.BaseDevice.SaveDevice;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.household.User;
import cz.vutbr.fit.iha.network.Network.NetworkAction;
import cz.vutbr.fit.iha.network.xml.action.Action;
import cz.vutbr.fit.iha.network.xml.condition.BetweenFunc;
import cz.vutbr.fit.iha.network.xml.condition.ConditionFunction;
import cz.vutbr.fit.iha.network.xml.condition.DewPointFunc;
import cz.vutbr.fit.iha.network.xml.condition.EqualFunc;
import cz.vutbr.fit.iha.network.xml.condition.GreaterEqualFunc;
import cz.vutbr.fit.iha.network.xml.condition.GreaterThanFunc;
import cz.vutbr.fit.iha.network.xml.condition.LesserEqualFunc;
import cz.vutbr.fit.iha.network.xml.condition.LesserThanFunc;
import cz.vutbr.fit.iha.network.xml.condition.TimeFunc;

/**
 * Class for creating XML file from Adapter object
 * 
 * @author ThinkDeep
 * 
 */
public class XmlCreator {

	/**
	 * NameSpace
	 */
	public static final String ns = null;

	public static final String COM_ROOT = "com";
	public static final String ID = "id";
	public static final String SID = "sid";
	public static final String AID = "aid";
	public static final String LID = "lid";
	public static final String DID = "did";
	public static final String ZERO = "0";
	public static final String ONE = "1";
	public static final String INIT_ID = "0";
	public static final String STATE = "state";
	public static final String HEX = "0x";
	public static final String VERSION = "ver";
	/**
	 * Version of communication protocol for google/android device
	 */
	public static final String GVER = Constants.COM_VER;
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
	public static final String SETDEVS = "setdevs";
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
	public static final String GETALLDEVICES = "getalldevs";
	public static final String SCANMODE = "scanmode";
	public static final String SWITCH = "switch";
	public static final String DELDEVICE = "deldev";
	public static final String DELGCMID = "delgcmid";
	public static final String GETNOTIFICATIONS = "getnotifications";
	public static final String NOTIFICATIONREAD = "notificationread";
	public static final String GETNEWDEVICES = "getnewdevices";
	public static final String SETCONDITION = "setcondition";
	public static final String SETLOCALE = "setlocale";
	public static final String CONDITIONPLUSACTION = "conditionplusaction";
	public static final String GETCONDITION = "getcondition";
	public static final String GETCONDITIONS = "getconditions";
	public static final String UPDATECONDITION = "updatecondition";
	public static final String DELCONDITION = "delcondition";
	public static final String SETACTION = "setaction";
	public static final String GETACTIONS = "getactions";
	public static final String GETACTION = "getaction";
	public static final String UPDATEACTION = "updateaction";
	public static final String DELACTION = "delaction";

	// end of states
	public static final String USER = "user";
	public static final String EMAIL = "email";
	public static final String GTOKEN = "gt";
	public static final String MODE = "mode";
	public static final String SERIAL = "serialnumber";
	public static final String NEXT = "next";
	public static final String ADAPTER = "adapter";
	public static final String NEW = "new";
	public static final String OLD = "old";
	public static final String OLDID = "oaid";
	public static final String NEWID = "naid";
	public static final String ROLE = "role";
	public static final String ADDITIONALINFO = "additionalinfo";
	public static final String FROM = "from";
	public static final String TO = "to";
	public static final String ACTION = "action";
	public static final String ICON = "icon";
	public static final String TIME = "time";
	public static final String UTC = "utc";
	public static final String LOCALE = "loc";
	public static final String ERRCODE = "errcode";
	public static final String INTERVAL = "interval";
	public static final String GCMID = "gcmid";
	public static final String NOTIFICAION = "notification";
	public static final String MSGID = "msgid";
	public static final String CONDITION = "condition";
	public static final String FUNC = "func";
	public static final String COMPLEXACTION = "complexaction";
	public static final String VAL = "val";
	public static final String PART = "part";
	public static final String DEVICE = "dev";
	public static final String INITIALIZED = "init";
	public static final String TYPE = "type";
	public static final String FTYPE = "ftype";
	public static final String LTYPE = "ltype";
	public static final String DTYPE = "dtype";
	public static final String VISIBILITY = "vis";
	public static final String LOCATION = "loc";
	public static final String NAME = "name";
	public static final String REFRESH = "refresh";
	public static final String BATTERY = "bat";
	public static final String QUALITY = "quality";
	public static final String VALUE = "value";
	public static final String LOGGING = "logging";
	public static final String ENABLED = "enabled";
	public static final String INIT_1 = "1";
	public static final String INIT_0 = "0";

	// new drop
	public enum ConditionType {
		AND("and"),
		OR("or");

		private final String mValue;

		private ConditionType(String value) {
			mValue = value;
		}

		public String getValue() {
			return mValue;
		}

		public static ConditionType fromValue(String value) {
			for (ConditionType item : values()) {
				if (value.equalsIgnoreCase(item.getValue()))
					return item;
			}
			throw new IllegalArgumentException("Invalid ConditionType value");
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////SIGNIN,SIGNUP,ADAPTERS/////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Method create XML for signIn message
	 * 
	 * @param email
	 *            of user
	 * @param gtoken
	 *            token from google
	 * @param lokale
	 *            language of App {cs, en, sk}
	 * @param gcmid
	 *            google cloud messaging id
	 * @return XML message
	 * @since 2.2
	 */
	public static String createSignIn(String email, String gtoken, String lokale, String gcmid) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);

			serializer.attribute(ns, STATE, SIGNIN);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, EMAIL, email);
			serializer.attribute(ns, GTOKEN, gtoken);
			serializer.attribute(ns, LOCALE, lokale);
			serializer.attribute(ns, GCMID, gcmid);

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method create XML for singUp message
	 * 
	 * @param email
	 *            of user
	 * @param gtoken
	 *            token from google
	 * @return XML message
	 * @since 2.2
	 */
	public static String createSignUp(String email, String gtoken) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, STATE, SIGNUP);
			serializer.attribute(ns, VERSION, GVER);

			serializer.attribute(ns, EMAIL, email);
			serializer.attribute(ns, GTOKEN, gtoken);

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method create XML for AddAdapter message
	 * 
	 * @param sid
	 *            sessionID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @param adapterName
	 *            name of adapter
	 * @return AddAdapter message
	 * @since 2.2
	 */
	public static String createAddAdapter(String sid, String aid, String adapterName) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, SID, sid);
			serializer.attribute(ns, STATE, ADDADAPTER);
			serializer.attribute(ns, VERSION, GVER);

			serializer.attribute(ns, AID, aid);
			serializer.attribute(ns, NAME, adapterName);

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method create XML of GetAdapters message
	 * 
	 * @param sid
	 *            sessionID of user
	 * @return GetAdapters message
	 * @since 2.2
	 */
	public static String createGetAdapters(String sid) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);

			serializer.attribute(ns, SID, sid);
			serializer.attribute(ns, STATE, GETADAPTERS);
			serializer.attribute(ns, VERSION, GVER);

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML for ReInit message
	 * 
	 * @param sid
	 *            sessionID of user
	 * @param adapterIdOld
	 *            old id of adapter
	 * @param adapterIdNew
	 *            new id of adapter
	 * @return ReInit message
	 * @since 2.2
	 */
	public static String createReInitAdapter(String sid, String adapterIdOld, String adapterIdNew) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);

			serializer.attribute(ns, SID, sid);
			serializer.attribute(ns, STATE, REINITADAPTER);
			serializer.attribute(ns, VERSION, GVER);

			serializer.attribute(ns, OLDID, adapterIdOld);
			serializer.attribute(ns, NEWID, adapterIdNew);

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
		
	//////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////DEVICES,LOGS///////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Method create XML for AdapterListen message
	 * 
	 * @param sid
	 *            sessionID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @return XML of AdapterListen message
	 * @since 2.2
	 */
	public static String createAdapterScanMode(String sid, String aid) {
		return createFromSidAid(SCANMODE, sid, aid);
	}

	/**
	 * Method create XML for GetAllDevices message
	 * 
	 * @param sid
	 *            sessionID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @return XML of GetAllDevices message
	 * @since 2.2
	 */
	public static String createGetAllDevices(String sid, String aid) {
		return createFromSidAid(GETALLDEVICES, sid, aid);
	}
	
	/**
	 * Method create XML for getting uninitialized devices
	 * 
	 * @param sid
	 *            sessionID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @return XML of GetNewDevices message
	 * @since 2.2
	 */
	public static String createGetNewDevices(String sid, String aid) {
		return createFromSidAid(GETNEWDEVICES, sid, aid);
	}
	
	/**
	 * Method create XML of GetDevices message
	 * 
	 * @param sid
	 *            sessionID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @param facilities
	 *            facilities with devices to update
	 * @return update message
	 * @since 2.2
	 */
	public static String createGetDevices(String sid, List<Facility> facilities) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, SID, sid);
			serializer.attribute(ns, STATE, GETDEVICES);
			serializer.attribute(ns, VERSION, GVER);
			
			//sort by adapter address
			Collections.sort(facilities, new AdapterAddressComparator());
			
			String aid = "";
			for (Facility facility : facilities) {
				
				boolean isSameAdapter = aid.equals(facility.getAdapterId()); 
				if(!isSameAdapter){ // new adapter
					if(aid.length() > 0)
						serializer.endTag(ns, ADAPTER);
					aid = facility.getAdapterId();
					serializer.startTag(ns, ADAPTER);
					serializer.attribute(ns, ID, aid);
				}
				serializer.startTag(ns, DEVICE);
				serializer.attribute(ns, ID, facility.getAddress());
				
				for (BaseDevice device : facility.getDevices()) {
					serializer.startTag(ns, PART);
					serializer.attribute(ns, TYPE, Integer.toString(device.getType().getTypeId()));
					serializer.endTag(ns, PART);
				}
				serializer.endTag(ns, DEVICE);
			}
			serializer.endTag(ns, ADAPTER);
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML for GetLog message
	 * 
	 * @param sid
	 *            sessionID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @param did
	 *            deviceID of wanted device
	 * @param deviceType
	 *            is type of sensor
	 * @param from
	 *            date in unix timestamp
	 * @param to
	 *            date in unix timestamp
	 * @param funcType
	 *            is aggregation function type {avg, median, ...}
	 * @param interval
	 *            is time value in seconds that represents nicely e.g. month, week, day, 10 hours, 1 hour, ...
	 * @return GetLog message
	 * @since 2.2
	 */
	public static String createGetLog(String sid, String aid, String did, int deviceType, String from, String to,
			String funcType, int interval) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);

			serializer.attribute(ns, SID, sid);
			serializer.attribute(ns, STATE, GETLOG);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, FROM, from);
			serializer.attribute(ns, TO, to);
			serializer.attribute(ns, FTYPE, funcType);
			serializer.attribute(ns, INTERVAL, String.valueOf(interval));
			serializer.attribute(ns, AID, aid);
			serializer.attribute(ns, DID, did);
			serializer.attribute(ns, DTYPE, formatType(deviceType));

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of SetDevs message. Almost all fields are optional
	 * 
	 * @param sid
	 *            sessionID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @param facilities
	 *            with changed fields
	 * @return Partial message
	 * @since 2.2
	 */
	public static String createSetDevs(String sid, String aid, List<Facility> facilities) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);

			serializer.attribute(ns, SID, sid);
			serializer.attribute(ns, STATE, SETDEVS);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, AID, aid);

			for (Facility facility : facilities) {
				serializer.startTag(ns, DEVICE);

				serializer.attribute(ns, INITIALIZED, (facility.isInitialized()) ? ONE : ZERO);
				serializer.attribute(ns, DID, facility.getAddress());
				serializer.attribute(ns, VISIBILITY, (facility.getVisibility()) ? ONE : ZERO);
				serializer.attribute(ns, LID, facility.getLocationId());
				serializer.attribute(ns, REFRESH, Integer.toString(facility.getRefresh().getInterval()));

				for (BaseDevice device : facility.getDevices()) {
					serializer.startTag(ns, PART);

					serializer.attribute(ns, TYPE, Integer.toString(device.getType().getTypeId()));
					serializer.attribute(ns, NAME, device.getName());
					serializer.attribute(ns, VALUE, String.valueOf(device.getValue().getDoubleValue()));

					serializer.endTag(ns, PART);
				}
				serializer.endTag(ns, DEVICE);
			}

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * New method create XML of SetDevs message with only one device in it. toSave parameter must by set properly.
	 * 
	 * @param sid
	 *            sessionID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @param device
	 *            to save
	 * @param toSave
	 *            ECO mode to save only wanted fields
	 * @return SetDevs message
	 * @since 2.2
	 */
	public static String createSetDev(String sid, String aid, BaseDevice device, EnumSet<SaveDevice> toSave) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			Facility facility = device.getFacility();

			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);

			serializer.attribute(ns, SID, sid);
			serializer.attribute(ns, STATE, SETDEVS);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, AID, aid);

			serializer.startTag(ns, DEVICE);

			// if(toSave.contains(SaveDevice.SAVE_INITIALIZED))
			serializer.attribute(ns, INITIALIZED, (facility.isInitialized()) ? ONE : ZERO);
			// send always
			serializer.attribute(ns, DID, facility.getAddress());
			if (toSave.contains(SaveDevice.SAVE_VISIBILITY))
				serializer.attribute(ns, VISIBILITY, (facility.getVisibility()) ? ONE : ZERO);
			if (toSave.contains(SaveDevice.SAVE_LOCATION))
				serializer.attribute(ns, LID, facility.getLocationId());
			if (toSave.contains(SaveDevice.SAVE_REFRESH))
				serializer.attribute(ns, REFRESH, Integer.toString(facility.getRefresh().getInterval()));

			if (toSave.contains(SaveDevice.SAVE_NAME) || toSave.contains(SaveDevice.SAVE_VALUE)) {
				serializer.startTag(ns, PART);
				// send always if sensor changed
				serializer.attribute(ns, TYPE, Integer.toString(device.getType().getTypeId()));
				if (toSave.contains(SaveDevice.SAVE_NAME))
					serializer.attribute(ns, NAME, device.getName());
				if (toSave.contains(SaveDevice.SAVE_VALUE))
					serializer.attribute(ns, VAL, String.valueOf(device.getValue().getDoubleValue()));

				serializer.endTag(ns, PART);
			}

			serializer.endTag(ns, DEVICE);

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML for Switch message
	 * 
	 * @param sid
	 *            sessionID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @param device
	 *            to switch value
	 * @return XML of Switch message
	 * @since 2.2
	 */
	public static String createSwitch(String sid, String aid, BaseDevice device) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);

			serializer.attribute(ns, SID, sid);
			serializer.attribute(ns, STATE, SWITCH);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, AID, aid);
			serializer.attribute(ns, DID, device.getFacility().getAddress());
			serializer.attribute(ns, TYPE, formatType(device.getType().getTypeId()));
			serializer.attribute(ns, VAL, String.valueOf(device.getValue().getDoubleValue()));

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method create XML of DelDevice message
	 * 
	 * @param sid
	 *            sessionID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @param facility
	 *            to be removed
	 * @return XML of DelDevice message
	 * @since 2.2
	 */
	public static String createDeleteDevice(String sid, String aid, Facility facility) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);

			serializer.attribute(ns, SID, sid);
			serializer.attribute(ns, STATE, DELDEVICE);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, AID, aid);
			serializer.attribute(ns, DID, facility.getAddress());

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////ROOMS//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method create XML of AddRoom message
	 * 
	 * @param sid
	 *            sessionID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @param location
	 *            to create
	 * @return created message
	 * @since 2.2
	 */
	public static String createAddRoom(String sid, String aid, Location location) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);

			serializer.attribute(ns, SID, sid);
			serializer.attribute(ns, STATE, ADDROOM);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, AID, aid);

			serializer.attribute(ns, TYPE, Integer.toString(location.getType()));
			serializer.attribute(ns, NAME, location.getName());

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method create XML of SetRooms message
	 * 
	 * @param sid
	 *            sessionID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @param locations
	 *            list of location object to update
	 * @return message SetRooms
	 * @since 2.2
	 */
	public static String createSetRooms(String sid, String aid, List<Location> locations) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);

			serializer.attribute(ns, SID, sid);
			serializer.attribute(ns, STATE, UPDATEROOMS);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, AID, aid);

			for (Location location : locations) {
				serializer.startTag(ns, LOCATION);

				serializer.attribute(ns, ID, location.getId());
				serializer.attribute(ns, TYPE, Integer.toString(location.getType()));
				serializer.attribute(ns, NAME, location.getName());

				serializer.endTag(ns, LOCATION);
			}
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of DelRoom message
	 * 
	 * @param sid
	 *            sessionID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @param location
	 *            to delete
	 * @return DelRoom message
	 * @since 2.2
	 */
	public static String createDeleteRoom(String sid, String aid, Location location) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);

			serializer.attribute(ns, SID, sid);
			serializer.attribute(ns, STATE, DELROOM);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, AID, aid);
			serializer.attribute(ns, LID, location.getId());

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method create XML of GetRooms message
	 * 
	 * @param sid
	 *            sessionID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @return message GetRooms
	 * @since 2.2
	 */
	public static String createGetRooms(String sid, String aid) {
		return createFromSidAid(GETROOMS, sid, aid);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////VIEWS//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Method create XML of AddView message
	 * 
	 * @param id
	 *            of user
	 * @param adapterId
	 * @param viewName
	 *            name of custom view
	 * @param devicesId
	 *            list of devices id
	 * @return addView message
	 */
	// TODO: IS NOT REFORMED
	public static String createAddView(String id, String viewName, int iconNum, List<BaseDevice> devices) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, ADDVIEW);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, NAME, viewName);
			serializer.attribute(ns, ICON, Integer.toString(iconNum));

			for (BaseDevice device : devices) {
				serializer.startTag(ns, DEVICE);
				serializer.attribute(ns, ID, device.getId());
				serializer.attribute(ns, TYPE, formatType(device.getType().getTypeId()));
				serializer.endTag(ns, DEVICE);
			}

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of UpdateView message
	 * 
	 * @param id
	 *            of user
	 * @param adapterId
	 * @param viewName
	 *            of custom view
	 * @param devices
	 *            hashMap with device id as key, and action as value
	 * @return UpdateValue message
	 */
	// TODO: IS NOT REFORMED
	public static String createUpdateViews(String id, String viewName, int iconNum, HashMap<String, String> devices) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, UPDATEVIEW);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, NAME, viewName);
			serializer.attribute(ns, ICON, Integer.toString(iconNum));

			for (Entry<String, String> device : devices.entrySet()) {
				serializer.startTag(ns, DEVICE);
				serializer.attribute(ns, ID, device.getKey());
				serializer.attribute(ns, ACTION, device.getValue());
				serializer.endTag(ns, DEVICE);
			}

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method create one view to update message
	 * 
	 * @param id
	 * @param viewName
	 * @param iconNum
	 * @param device
	 * @param action
	 * @return
	 */
	// TODO: IS NOT REFORMED
	public static String createUpdateView(String id, String viewName, int iconNum, Facility device, NetworkAction action) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
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
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of DelVIew message
	 * 
	 * @param id
	 *            of user
	 * @param adapterId
	 * @param viewName
	 *            of custom view
	 * @return DelVIew message
	 */
	// TODO: IS NOT REFORMED
	public static String createDelView(String id, String viewName) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
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
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method create XML of GetViews message (method added in 1.6 version)
	 * 
	 * @param id
	 *            of user
	 * @param adapterId
	 * @return getViews message
	 */
	// TODO: IS NOT REFORMED
	public static String createGetViews(String id) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETVIEWS);
			serializer.attribute(ns, VERSION, GVER);

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////ACCOUNTS///////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Method create XML for AddConAccount message
	 * 
	 * @param id
	 *            of user (superuser)
	 * @param adapterId
	 * @param users
	 *            map with pairs e-mail of common user (key) and its role (value)
	 * @return addConAccount message
	 */
	// TODO: IS NOT REFORMED
	public static String createAddAccounts(String id, String adapterId, HashMap<String, String> users) {
		return createAddOrChangeConAccount(id, adapterId, users, true);
	}

	/**
	 * Method create XML for AddAcount message
	 * 
	 * @param id
	 * @param adapterId
	 * @param email
	 * @param role
	 * @return
	 */
	// TODO: IS NOT REFORMED
	public static String createAddAccount(String id, String adapterId, String email, User.Role role) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
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
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method create XML of UpdateAccount message
	 * 
	 * @param id
	 * @param adapterId
	 * @param user
	 * @param role
	 * @return
	 */
	// TODO: IS NOT REFORMED
	public static String createUpdateAccount(String id, String adapterId, User user, User.Role role) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
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
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML of ChangeConAccount message
	 * 
	 * @param id
	 *            of user (superuser)
	 * @param adapterId
	 * @param users
	 *            map with pairs e-mail of common user (key) and its role (value)
	 * @return changeConAccount message
	 */
	// TODO: IS NOT REFORMED
	public static String createUpdateAccounts(String id, String adapterId, HashMap<String, String> users) {
		return createAddOrChangeConAccount(id, adapterId, users, false);
	}
	
	/**
	 * Method create XML for DelConAccount message
	 * 
	 * @param id
	 *            of user (superuser)
	 * @param adapterId
	 * @param userEmails
	 *            of common users
	 * @return dellConAccount message
	 */
	// TODO: IS NOT REFORMED
	public static String createDelAccounts(String id, String adapterId, List<String> userEmails) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, DELACCOUNT);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);

			for (String userEmail : userEmails) {
				serializer.startTag(ns, USER);
				serializer.attribute(ns, EMAIL, userEmail);
				serializer.endTag(ns, USER);
			}

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method create XML of DelAccount message
	 * 
	 * @param id
	 * @param adapterId
	 * @param user
	 * @return
	 */
	// TODO: IS NOT REFORMED
	public static String createDelAccount(String id, String adapterId, User user) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
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
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Method create XML for GetConAccount message
	 * 
	 * @param id
	 *            of user
	 * @param adapterId
	 * @return GetConAccount message
	 */
	// TODO: IS NOT REFORMED
	public static String createGetAccount(String id, String adapterId) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
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
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////TIME///////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Method create XML of SetTimeZone message
	 * 
	 * @param sid
	 *            sessionID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @param diffToGMT
	 *            difference to GMT (UTC+0)
	 * @return SetTimeZone message
	 * @since 2.2
	 */
	public static String createSetTimeZone(String sid, String aid, int diffToGMT) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);

			serializer.attribute(ns, SID, sid);
			serializer.attribute(ns, STATE, SETTIMEZONE);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, AID, aid);
			serializer.attribute(ns, UTC, Integer.toString(diffToGMT));

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method create XML of GetTimeZone message
	 * 
	 * @param sid
	 *            sessionID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @return GetTimeZone message
	 * @since 2.2
	 */
	public static String createGetTimeZone(String sid, String aid) {
		return createFromSidAid(GETTIMEZONE, sid, aid);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////OTHERS/////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Method create XML of SetLocale message
	 * 
	 * @param sid
	 *            sessionID of user
	 * @param locale
	 *            of phone
	 * @return message SetLocale
	 * @since 2.2
	 */
	public static String createSetLocale(String sid, String locale) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);

			serializer.attribute(ns, SID, sid);
			serializer.attribute(ns, STATE, SETLOCALE);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, LOCALE, locale);

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////CONDITIONS,ACTIONS/////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static String createSetCondition(String id, String name, ConditionType type,
			ArrayList<ConditionFunction> condFuncs) {
		return sendCondition(id, name, type, condFuncs, "0", true);
	}

	public static String createUpdateCondition(String id, String name, ConditionType type, String conditionId,
			ArrayList<ConditionFunction> condFuncs) {
		return sendCondition(id, name, type, condFuncs, conditionId, false);
	}
	
	public static String createDelCondition(String id, String conditionId) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, DELCONDITION);
			serializer.attribute(ns, VERSION, GVER);

			serializer.startTag(ns, CONDITION);
			serializer.attribute(ns, ID, conditionId);
			serializer.endTag(ns, CONDITION);

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String createConditionPlusAction(String id, String conditionId, String actionId) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, CONDITIONPLUSACTION);
			serializer.attribute(ns, VERSION, GVER);

			serializer.startTag(ns, CONDITION);
			serializer.attribute(ns, ID, conditionId);
			serializer.endTag(ns, CONDITION);

			serializer.startTag(ns, ACTION);
			serializer.attribute(ns, ID, actionId);
			serializer.endTag(ns, ACTION);

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String createGetCondition(String id, String conditionId) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETCONDITION);
			serializer.attribute(ns, VERSION, GVER);

			serializer.startTag(ns, CONDITION);
			serializer.attribute(ns, ID, conditionId);
			serializer.endTag(ns, CONDITION);

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String createGetConditions(String id) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETCONDITIONS);
			serializer.attribute(ns, VERSION, GVER);

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String createSetAction(String id, String name, List<Action> actions) {
		return sendAction(id, name, "0", actions, true);
	}

	public static String createUpdateAction(String id, String name, String actionId, List<Action> actions) {
		return sendAction(id, name, actionId, actions, false);
	}

	public static String createGetActions(String id) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETACTIONS);
			serializer.attribute(ns, VERSION, GVER);

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String createGetAction(String id, String actionId) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETACTION);
			serializer.attribute(ns, VERSION, GVER);

			serializer.startTag(ns, COMPLEXACTION);
			serializer.attribute(ns, ID, actionId);
			serializer.endTag(ns, COMPLEXACTION);

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String createDelAction(String id, String actionId) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, DELACTION);
			serializer.attribute(ns, VERSION, GVER);

			serializer.startTag(ns, COMPLEXACTION);
			serializer.attribute(ns, ID, actionId);
			serializer.endTag(ns, COMPLEXACTION);

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////NOTIFICATIONS//////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Method create XML of DelGCMID message (delete google cloud message id)
	 * 
	 * @param id
	 *            of user logged in now
	 * @param email
	 *            of last logged user
	 * @param gcmid
	 *            id of google messaging
	 * @return message DelGCMID
	 */
	public static String createDeLGCMID(String id, String email, String gcmid) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
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
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method create XML of GetNotifications message
	 * 
	 * @param id
	 *            of user
	 * @return message GetNotifications
	 */
	public static String createGetNotifications(String id) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, GETNOTIFICATIONS);
			serializer.attribute(ns, VERSION, GVER);
			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method create XML of NotificationRead message
	 * 
	 * @param id
	 *            of user
	 * @param msgid
	 *            id of read notification
	 * @return message NotificationRead
	 */
	public static String createNotificaionRead(String id, String msgid) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
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
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}



	
	@Deprecated
	private static String formatType(int type) {
		String hex = Integer.toHexString(type);
		if (hex.length() == 1)
			hex = "0" + hex;

		return "0x" + hex;
	}

	/**********************PRIVATE METHODS***************************************************/

	private static String createFromSidAid(String state, String sid, String aid) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);

			serializer.attribute(ns, SID, sid);
			serializer.attribute(ns, STATE, state);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, AID, aid);

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Private method create add or change - conAccount message
	 * 
	 * @param id
	 *            of user (superuser)
	 * @param adapterId
	 * @param users
	 *            map with pairs e-mail of common user (key) and its role (value)
	 * @param ADD
	 *            if is TRUE, addConAccount is chosen, changeConAccount otherwise
	 * @return Add/ChangeConAccount message
	 */
	// TODO: IS NOT REFORMED
	private static String createAddOrChangeConAccount(String id, String adapterId, HashMap<String, String> users,
			boolean ADD) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, (ADD) ? ADDACCOUNT : UPDATEACCOUNT);
			serializer.attribute(ns, VERSION, GVER);
			serializer.attribute(ns, ADAPTER, adapterId);

			for (Entry<String, String> user : users.entrySet()) {
				serializer.startTag(ns, USER);
				serializer.attribute(ns, EMAIL, user.getKey());
				serializer.attribute(ns, ROLE, user.getValue());
				serializer.endTag(ns, USER);
			}

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String sendCondition(String id, String name, ConditionType type,
			ArrayList<ConditionFunction> condFuncs, String conditionId, boolean isNew) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, (isNew) ? SETCONDITION : UPDATECONDITION);
			serializer.attribute(ns, VERSION, GVER);

			serializer.startTag(ns, CONDITION);
			serializer.attribute(ns, NAME, name);
			serializer.attribute(ns, TYPE, type.getValue());
			if (isNew)
				serializer.attribute(ns, ID, conditionId);

			for (ConditionFunction func : condFuncs) {
				serializer.startTag(ns, FUNC);
				serializer.attribute(ns, TYPE, func.getFuncType().getValue());

				switch (func.getFuncType()) {
				case EQ:
					serializer.startTag(ns, DEVICE);
					serializer.attribute(ns, ID, ((EqualFunc) func).getDevice().getId());
					serializer.attribute(ns, TYPE, ((EqualFunc) func).getDevice().getType() + "");
					serializer.endTag(ns, DEVICE);

					serializer.startTag(ns, VALUE);
					serializer.text(((EqualFunc) func).getValue());
					serializer.endTag(ns, VALUE);
					break;
				case GT:
					serializer.startTag(ns, DEVICE);
					serializer.attribute(ns, ID, ((GreaterThanFunc) func).getDevice().getId());
					serializer.attribute(ns, TYPE, ((GreaterThanFunc) func).getDevice().getType() + "");
					serializer.endTag(ns, DEVICE);

					serializer.startTag(ns, VALUE);
					serializer.text(((GreaterThanFunc) func).getValue());
					serializer.endTag(ns, VALUE);
					break;
				case GE:
					serializer.startTag(ns, DEVICE);
					serializer.attribute(ns, ID, ((GreaterEqualFunc) func).getDevice().getId());
					serializer.attribute(ns, TYPE, ((GreaterEqualFunc) func).getDevice().getType() + "");
					serializer.endTag(ns, DEVICE);

					serializer.startTag(ns, VALUE);
					serializer.text(((GreaterEqualFunc) func).getValue());
					serializer.endTag(ns, VALUE);
					break;
				case LT:
					serializer.startTag(ns, DEVICE);
					serializer.attribute(ns, ID, ((LesserThanFunc) func).getDevice().getId());
					serializer.attribute(ns, TYPE, ((LesserThanFunc) func).getDevice().getType() + "");
					serializer.endTag(ns, DEVICE);

					serializer.startTag(ns, VALUE);
					serializer.text(((LesserThanFunc) func).getValue());
					serializer.endTag(ns, VALUE);
					break;
				case LE:
					serializer.startTag(ns, DEVICE);
					serializer.attribute(ns, ID, ((LesserEqualFunc) func).getDevice().getId());
					serializer.attribute(ns, TYPE, ((LesserEqualFunc) func).getDevice().getType() + "");
					serializer.endTag(ns, DEVICE);

					serializer.startTag(ns, VALUE);
					serializer.text(((LesserEqualFunc) func).getValue());
					serializer.endTag(ns, VALUE);
					break;
				case BTW:
					serializer.startTag(ns, DEVICE);
					serializer.attribute(ns, ID, ((BetweenFunc) func).getDevice().getId());
					serializer.attribute(ns, TYPE, ((BetweenFunc) func).getDevice().getType() + "");
					serializer.endTag(ns, DEVICE);

					serializer.startTag(ns, VALUE);
					serializer.text(((BetweenFunc) func).getMinValue());
					serializer.endTag(ns, VALUE);

					serializer.startTag(ns, VALUE);
					serializer.text(((BetweenFunc) func).getMaxValue());
					serializer.endTag(ns, VALUE);
					break;
				case DP:
					serializer.startTag(ns, DEVICE);
					serializer.attribute(ns, ID, ((DewPointFunc) func).getTempDevice().getId());
					serializer.attribute(ns, TYPE, ((DewPointFunc) func).getTempDevice().getType() + "");
					serializer.endTag(ns, DEVICE);

					serializer.startTag(ns, DEVICE);
					serializer.attribute(ns, ID, ((DewPointFunc) func).getHumiDevice().getId());
					serializer.attribute(ns, TYPE, ((DewPointFunc) func).getHumiDevice().getType() + "");
					serializer.endTag(ns, DEVICE);
					break;
				case TIME:
					serializer.startTag(ns, VALUE);
					serializer.text(((TimeFunc) func).getTime());
					serializer.endTag(ns, VALUE);
					break;
				case GEO:
					serializer.startTag(ns, VALUE);
					// FIXME
					serializer.text("TODO");
					serializer.endTag(ns, VALUE);
					break;
				default:
					break;
				}

				serializer.endTag(ns, FUNC);
			}

			serializer.endTag(ns, CONDITION);

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static String sendAction(String id, String name, String actionId, List<Action> actions, boolean isNew) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", null);

			serializer.startTag(ns, COM_ROOT);
			serializer.attribute(ns, ID, id);
			serializer.attribute(ns, STATE, (isNew) ? SETACTION : UPDATEACTION);
			serializer.attribute(ns, VERSION, GVER);

			serializer.startTag(ns, COMPLEXACTION);
			serializer.attribute(ns, NAME, name);
			serializer.attribute(ns, ID, actionId);

			for (Action action : actions) {
				serializer.startTag(ns, ACTION);
				serializer.attribute(ns, TYPE, action.getType().getValue());

				if (action.getType() == Action.ActionType.ACTOR) {
					serializer.startTag(ns, DEVICE);
					serializer.attribute(ns, ID, action.getDevice().getId());
					serializer.attribute(ns, TYPE, action.getDevice().getType() + "");

					serializer.startTag(ns, VALUE);
					serializer.text(action.getValue());
					serializer.endTag(ns, VALUE);

					serializer.endTag(ns, DEVICE);
				}

				serializer.endTag(ns, ACTION);
			}

			serializer.endTag(ns, COMPLEXACTION);

			serializer.endTag(ns, COM_ROOT);
			serializer.endDocument();

			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
