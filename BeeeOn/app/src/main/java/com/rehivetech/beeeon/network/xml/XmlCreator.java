/**
 * @brief Package for manipulation with XML and parsers
 */

package com.rehivetech.beeeon.network.xml;

import android.util.Xml;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Device.SaveDevice;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.network.INetwork.NetworkAction;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.network.xml.action.Action;
import com.rehivetech.beeeon.network.xml.condition.BetweenFunc;
import com.rehivetech.beeeon.network.xml.condition.ConditionFunction;
import com.rehivetech.beeeon.network.xml.condition.DewPointFunc;
import com.rehivetech.beeeon.network.xml.condition.EqualFunc;
import com.rehivetech.beeeon.network.xml.condition.GreaterEqualFunc;
import com.rehivetech.beeeon.network.xml.condition.GreaterThanFunc;
import com.rehivetech.beeeon.network.xml.condition.LesserEqualFunc;
import com.rehivetech.beeeon.network.xml.condition.LesserThanFunc;
import com.rehivetech.beeeon.network.xml.condition.TimeFunc;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Class for creating XML messages
 *
 * @author ThinkDeep
 *
 */
public class XmlCreator {

	private static final String ns = null;

	private static final String COM_VER = Constants.COM_VER;

	// states

	public static final String PASSBORDER = "passborder";
	public static final String SIGNIN = "signin";
	public static final String SIGNUP = "signup";
	public static final String GETUSERINFO ="getuserinfo";
	public static final String JOINACCOUNT = "joinaccount";
	public static final String CUTACCOUNT = "cutaccount";

	public static final String ADDADAPTER = "addadapter";
	public static final String REINITADAPTER = "reinitadapter";
	public static final String GETADAPTERS = "getadapters";

	public static final String ADDACCOUNTS = "addaccs";
	public static final String DELACCOUNTS = "delaccs";
	public static final String GETACCOUNTS = "getaccs";
	public static final String SETCCOUNTS = "setaccs";
	public static final String SCANMODE = "scanmode";

	public static final String SETDEVS = "setdevs";
	public static final String GETDEVICES = "getdevs";
	public static final String GETALLDEVICES = "getalldevs";
	public static final String DELDEVICE = "deldev";
	public static final String SWITCH = "switch";
	public static final String GETLOG = "getlog";

	public static final String ADDVIEW = "addview";
	public static final String DELVIEW = "delview";
	public static final String SETVIEW = "setview";
	public static final String GETVIEWS = "getviews";
	public static final String GETNEWDEVICES = "getnewdevs";

	public static final String SETTIMEZONE = "settimezone";
	public static final String GETTIMEZONE = "gettimezone";

	public static final String GETROOMS = "getrooms";
	public static final String SETROOMS = "setrooms";
	public static final String ADDROOM = "addroom";
	public static final String DELROOM = "delroom";

	public static final String DELGCMID = "delgcmid";
	public static final String SETGCMID = "setgcmid";
	public static final String GETNOTIFICATIONS = "getnotifs";
	public static final String NOTIFICATIONREAD = "notifread";

	public static final String SETCONDITION = "setcond";
	public static final String SETLOCALE = "setlocale";

	public static final String CONDITIONPLUSACTION = "condacition";
	public static final String GETCONDITION = "getcond";
	public static final String GETCONDITIONS = "getconds";
	public static final String ADDCONDITION = "addcond";
	public static final String DELCONDITION = "delcond";

	public static final String SETACTION = "setact";
	public static final String GETACTIONS = "getacts";
	public static final String GETACTION = "getact";
	public static final String ADDACTION = "addact";
	public static final String DELACTION = "delact";

	public static final String ADDALG = "addalg";
	public static final String GETALLALGS = "getallalgs";
	public static final String GETALGS =  "getlags";
	public static final String SETALG = "setalg";
	public static final String DELALG = "delalg";

	public static final String GETALLACHIEVEMENTS = "getallachievements";
	public static final String SETPROGRESSLVL = "setprogresslvl";

	// end of states

	/**
	 * Type of condition
	 *
	 * @author ThinkDeep
	 *
	 */
	public enum ConditionType {
		AND("and"), OR("or");

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

	private static XmlSerializer beginXml(StringWriter writer) throws IOException {
		XmlSerializer serializer = Xml.newSerializer();

		serializer.setOutput(writer);
		serializer.startDocument("UTF-8", null);

		serializer.startTag(ns, Xconstants.COM_ROOT);
		serializer.attribute(ns, Xconstants.VERSION, COM_VER); // every time use version

		return serializer;
	}

	private static void endXml(XmlSerializer serializer) throws IOException {
		serializer.text("");
		serializer.endTag(ns, Xconstants.COM_ROOT);
		serializer.endDocument();
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////SIGNIN,SIGNUP,ADAPTERS/////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method create message for registration of new user
	 * @param authProvider provider of authentication with parameters to send
	 * @return
	 */
	public static String createSignUp(IAuthProvider authProvider) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.STATE, SIGNUP);

			// NOTE: Ok, i did it like that, but i do not like it
			serializer.attribute(ns, Xconstants.SERVICE, authProvider.getProviderName());

			serializer.startTag(ns, Xconstants.PARAM);
			for (Map.Entry<String, String> entry : authProvider.getParameters().entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();

				if (key != null && value != null)
					serializer.attribute(ns, key, value);
			}
			serializer.endTag(ns, Xconstants.PARAM);

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, NetworkError.CL_XML);
		}
	}

	/**
	 * Method create message for login
	 * @param locale localization of phone
	 * @param pid unique id of phone
	 * @param authProvider provider of authentication with parameters to send
	 * @return
	 */
	public static String createSignIn(String locale, String pid, IAuthProvider authProvider) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.STATE, SIGNIN);
			serializer.attribute(ns, Xconstants.LOCALE, locale);
			serializer.attribute(ns, Xconstants.PID, pid);

			// NOTE: Ok, i did it like that, but i do not like it
			serializer.attribute(ns, Xconstants.SERVICE, authProvider.getProviderName());

			serializer.startTag(ns, Xconstants.PARAM);
			for (Map.Entry<String, String> entry : authProvider.getParameters().entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();

				if (key != null && value != null)
					serializer.attribute(ns, key, value);
			}
			serializer.endTag(ns, Xconstants.PARAM);

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, NetworkError.CL_XML);
		}
	}

	/**
	 * Method create message for joining new provider to actual user
	 * @param authProvider provider of authentication with parameters to send
	 *
	 * @return
	 */
	public static String createJoinAccount(String bt, IAuthProvider authProvider) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.STATE, JOINACCOUNT);
			serializer.attribute(ns, Xconstants.BT, bt);
			serializer.attribute(ns, Xconstants.SERVICE, authProvider.getProviderName());

			serializer.startTag(ns, Xconstants.PARAM);
			for (Map.Entry<String, String> entry : authProvider.getParameters().entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();

				if (key != null && value != null)
					serializer.attribute(ns, key, value);
			}
			serializer.endTag(ns, Xconstants.PARAM);

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, NetworkError.CL_XML);
		}
	}

	/**
	 * Method create message for removing part of account (or whole account)
	 * @param bt
	 * @param providerName name of service (beeeon, google, facebook, ...)
	 * @return
	 */
	public static String createCutAccount(String bt, String providerName){
		return createComAttribsVariant(Xconstants.STATE, CUTACCOUNT, Xconstants.BT, bt, Xconstants.SERVICE, providerName);
	}

	public static String createGetUserInfo(String bt){
		return createComAttribsVariant(Xconstants.STATE, GETUSERINFO, Xconstants.BT, bt);
	}

	/**
	 * Method create XML for AddAdapter message
	 *
	 * @param bt
	 *            userID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @param adapterName
	 *            name of adapter
	 * @return AddAdapter message
	 * @since 2.2
	 */
	public static String createAddAdapter(String bt, String aid, String adapterName) {
		return createComAttribsVariant(Xconstants.STATE, ADDADAPTER, Xconstants.BT, bt, Xconstants.AID, aid, Xconstants.ANAME, adapterName);
	}

	/**
	 * Method create XML of GetAdapters message
	 *
	 * @param bt
	 *            userID of user
	 * @return GetAdapters message
	 * @since 2.2
	 */
	public static String createGetAdapters(String bt) {
		return createComAttribsVariant(Xconstants.STATE, GETADAPTERS, Xconstants.BT, bt);
	}

	/**
	 * Method create XML for ReInit message
	 *
	 * @param bt
	 *            userID of user
	 * @param adapterIdOld
	 *            old id of adapter
	 * @param adapterIdNew
	 *            new id of adapter
	 * @return ReInit message
	 * @since 2.2
	 */
	public static String createReInitAdapter(String bt, String adapterIdOld, String adapterIdNew) {
		return createComAttribsVariant(Xconstants.STATE, REINITADAPTER, Xconstants.BT, bt, Xconstants.OLDID, adapterIdOld, Xconstants.NEWID, adapterIdNew);
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////DEVICES,LOGS///////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method create XML for AdapterListen message
	 *
	 * @param bt
	 *            userID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @return XML of AdapterListen message
	 * @since 2.2
	 */
	public static String createAdapterScanMode(String bt, String aid) {
		return createComAttribsVariant(Xconstants.STATE, SCANMODE, Xconstants.BT, bt, Xconstants.AID, aid);
	}

	/**
	 * Method create XML for GetAllDevices message
	 *
	 * @param bt
	 *            userID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @return XML of GetAllDevices message
	 * @since 2.2
	 */
	public static String createGetAllDevices(String bt, String aid) {
		return createComAttribsVariant(Xconstants.STATE, GETALLDEVICES, Xconstants.BT, bt, Xconstants.AID, aid);
	}

	/**
	 * Method create XML for getting uninitialized devices
	 *
	 * @param bt
	 *            userID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @return XML of GetNewDevices message
	 * @since 2.2
	 */
	public static String createGetNewDevices(String bt, String aid) {
		return createComAttribsVariant(Xconstants.STATE, GETNEWDEVICES, Xconstants.BT, bt, Xconstants.AID, aid);
	}

	/**
	 * Method create XML of GetDevices message
	 *
	 * @param bt
	 *            userID of user
	 * @param facilities
	 *            facilities with devices to update
	 * @return update message
	 * @since 2.2
	 */
	public static String createGetDevices(String bt, List<Facility> facilities) {
		if(facilities.size() < 1)
			throw new IllegalArgumentException("Expected more than zero facilities");
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.BT, bt);
			serializer.attribute(ns, Xconstants.STATE, GETDEVICES);

			// sort by adapter address
			Collections.sort(facilities, new Comparator<Facility>() {

				@Override
				public int compare(Facility left, Facility right) {
					return Integer.valueOf(left.getAdapterId()).compareTo(Integer.valueOf(right.getAdapterId()));
				}

			});

			String aid = "";
			for (Facility facility : facilities) {

				boolean isSameAdapter = aid.equals(facility.getAdapterId());
				if (!isSameAdapter) { // new adapter
					if (aid.length() > 0)
						serializer.endTag(ns, Xconstants.ADAPTER);
					aid = facility.getAdapterId();
					serializer.startTag(ns, Xconstants.ADAPTER);
					serializer.attribute(ns, Xconstants.ID, aid);
				}
				serializer.startTag(ns, Xconstants.DEVICE);
				serializer.attribute(ns, Xconstants.ID, facility.getAddress());

				for (Device device : facility.getDevices()) {
					serializer.startTag(ns, Xconstants.PART);
					serializer.attribute(ns, Xconstants.TYPE, device.getRawTypeId());
					serializer.endTag(ns, Xconstants.PART);
				}
				serializer.endTag(ns, Xconstants.DEVICE);
			}
			serializer.endTag(ns, Xconstants.ADAPTER);

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, NetworkError.CL_XML);
		}
	}

	/**
	 * Method create XML for GetLog message
	 *
	 * @param bt
	 *            userID of user
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
	public static String createGetLog(String bt, String aid, String did, String deviceType, String from, String to, String funcType, int interval) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.BT, bt);
			serializer.attribute(ns, Xconstants.STATE, GETLOG);
			serializer.attribute(ns, Xconstants.FROM, from);
			serializer.attribute(ns, Xconstants.TO, to);
			serializer.attribute(ns, Xconstants.FTYPE, funcType);
			serializer.attribute(ns, Xconstants.INTERVAL, String.valueOf(interval));
			serializer.attribute(ns, Xconstants.AID, aid);
			serializer.attribute(ns, Xconstants.DID, did);
			serializer.attribute(ns, Xconstants.DTYPE, deviceType);

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, NetworkError.CL_XML);
		}
	}

	/**
	 * Method create XML of SetDevs message. Almost all fields are optional
	 *
	 * @param bt
	 *            userID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @param facilities
	 *            with changed fields
	 * @return Partial message
	 * @since 2.2
	 */
	public static String createSetDevs(String bt, String aid, List<Facility> facilities, EnumSet<SaveDevice> toSave) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.BT, bt);
			serializer.attribute(ns, Xconstants.STATE, SETDEVS);
			serializer.attribute(ns, Xconstants.AID, aid);

			for (Facility facility : facilities) {
				serializer.startTag(ns, Xconstants.DEVICE);

				if (toSave.contains(SaveDevice.SAVE_INITIALIZED))
					serializer.attribute(ns, Xconstants.INITIALIZED, (facility.isInitialized()) ? Xconstants.ONE : Xconstants.ZERO);
				serializer.attribute(ns, Xconstants.DID, facility.getAddress());
				if (toSave.contains(SaveDevice.SAVE_LOCATION))
					serializer.attribute(ns, Xconstants.LID, facility.getLocationId());
				if (toSave.contains(SaveDevice.SAVE_REFRESH))
					serializer.attribute(ns, Xconstants.REFRESH, Integer.toString(facility.getRefresh().getInterval()));

				for (Device device : facility.getDevices()) {
					serializer.startTag(ns, Xconstants.PART);

					serializer.attribute(ns, Xconstants.TYPE, device.getRawTypeId());
					if (toSave.contains(SaveDevice.SAVE_VISIBILITY))
						serializer.attribute(ns, Xconstants.VISIBILITY, (device.isVisible()) ? Xconstants.ONE : Xconstants.ZERO);
					if (toSave.contains(SaveDevice.SAVE_NAME))
						serializer.attribute(ns, Xconstants.NAME, device.getName());
					// if (toSave.contains(SaveDevice.SAVE_VALUE))
					// serializer.attribute(ns, Xconstants.VALUE, String.valueOf(device.getValue().getDoubleValue()));

					serializer.endTag(ns, Xconstants.PART);
				}
				serializer.endTag(ns, Xconstants.DEVICE);
			}

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, NetworkError.CL_XML);
		}
	}

	/**
	 * New method create XML of SetDevs message with only one device in it. toSave parameter must by set properly.
	 *
	 * @param bt
	 *            userID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @param device
	 *            to save
	 * @param toSave
	 *            ECO mode to save only wanted fields
	 * @return SetDevs message
	 * @since 2.2
	 */
	public static String createSetDev(String bt, String aid, Device device, EnumSet<SaveDevice> toSave) {
		StringWriter writer = new StringWriter();
		try {
			Facility facility = device.getFacility();

			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.BT, bt);
			serializer.attribute(ns, Xconstants.STATE, SETDEVS);
			serializer.attribute(ns, Xconstants.AID, aid);

			serializer.startTag(ns, Xconstants.DEVICE);

			if (toSave.contains(SaveDevice.SAVE_INITIALIZED))
				serializer.attribute(ns, Xconstants.INITIALIZED, (facility.isInitialized()) ? Xconstants.ONE : Xconstants.ZERO);
			// send always
			serializer.attribute(ns, Xconstants.DID, facility.getAddress());
			if (toSave.contains(SaveDevice.SAVE_LOCATION))
				serializer.attribute(ns, Xconstants.LID, facility.getLocationId());
			if (toSave.contains(SaveDevice.SAVE_REFRESH))
				serializer.attribute(ns, Xconstants.REFRESH, Integer.toString(facility.getRefresh().getInterval()));

			if (toSave.contains(SaveDevice.SAVE_NAME) || toSave.contains(SaveDevice.SAVE_VALUE)) {
				serializer.startTag(ns, Xconstants.PART);
				// send always if sensor changed
				serializer.attribute(ns, Xconstants.TYPE, device.getRawTypeId());
				// if (toSave.contains(SaveDevice.SAVE_VISIBILITY))
				// serializer.attribute(ns, Xconstants.VISIBILITY, (device.getVisibility()) ? Xconstants.ONE : Xconstants.ZERO);
				if (toSave.contains(SaveDevice.SAVE_NAME))
					serializer.attribute(ns, Xconstants.NAME, device.getName());
				if (toSave.contains(SaveDevice.SAVE_VALUE))
					serializer.attribute(ns, Xconstants.VALUE, String.valueOf(device.getValue().getDoubleValue()));

				serializer.endTag(ns, Xconstants.PART);
			}

			serializer.endTag(ns, Xconstants.DEVICE);

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, NetworkError.CL_XML);
		}
	}

	/**
	 * Method create XML for Switch message
	 *
	 * @param bt
	 *            userID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @param device
	 *            to switch value
	 * @return XML of Switch message
	 * @since 2.2
	 */
	public static String createSwitch(String bt, String aid, Device device) {
		return createComAttribsVariant(Xconstants.STATE, SWITCH, Xconstants.BT, bt, Xconstants.AID, aid, Xconstants.DID, device.getFacility().getAddress(), Xconstants.DTYPE,
				device.getRawTypeId(), Xconstants.VALUE, String.valueOf(device.getValue().getDoubleValue()));
	}

	/**
	 * Method create XML of DelDevice message
	 *
	 * @param bt
	 *            userID of user
	 * @param facility
	 *            to be removed
	 * @return XML of DelDevice message
	 * @since 2.2
	 */
	public static String createDeleteDevice(String bt, Facility facility) {
		return createComAttribsVariant(Xconstants.STATE, DELDEVICE, Xconstants.BT, bt, Xconstants.AID, facility.getAdapterId(), Xconstants.DID, facility.getAddress());
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////ROOMS//////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method create XML of AddRoom message
	 *
	 * @param bt
	 *            userID of user
	 * @param location
	 *            to create
	 * @return created message
	 * @since 2.2
	 */
	public static String createAddRoom(String bt, Location location) {
		return createComAttribsVariant(Xconstants.STATE, ADDROOM, Xconstants.BT, bt, Xconstants.AID, location.getAdapterId(), Xconstants.LTYPE, Integer.toString(location.getType()), Xconstants.LNAME,
				location.getName());
	}

	/**
	 * Method create XML of SetRooms message
	 *
	 * @param bt
	 *            userID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @param locations
	 *            list of location object to update
	 * @return message SetRooms
	 * @since 2.2
	 */
	public static String createSetRooms(String bt, String aid, List<Location> locations) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.BT, bt);
			serializer.attribute(ns, Xconstants.STATE, SETROOMS);
			serializer.attribute(ns, Xconstants.AID, aid);

			for (Location location : locations) {
				serializer.startTag(ns, Xconstants.LOCATION);

				serializer.attribute(ns, Xconstants.ID, location.getId());
				serializer.attribute(ns, Xconstants.TYPE, Integer.toString(location.getType()));
				serializer.attribute(ns, Xconstants.NAME, location.getName());

				serializer.endTag(ns, Xconstants.LOCATION);
			}

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, NetworkError.CL_XML);
		}
	}

	/**
	 * Method create XML of DelRoom message
	 *
	 * @param bt
	 *            userID of user
	 * @param lid
	 *            locationID of location to delete
	 * @return DelRoom message
	 * @since 2.2
	 */
	public static String createDeleteRoom(String bt, Location location) {
		return createComAttribsVariant(Xconstants.STATE, DELROOM, Xconstants.BT, bt, Xconstants.AID, location.getAdapterId(), Xconstants.LID, location.getId());
	}

	/**
	 * Method create XML of GetRooms message
	 *
	 * @param bt
	 *            userID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @return message GetRooms
	 * @since 2.2
	 */
	public static String createGetRooms(String bt, String aid) {
		return createComAttribsVariant(Xconstants.STATE, GETROOMS, Xconstants.BT, bt, Xconstants.AID, aid);
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////VIEWS//////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method create XML of AddView message
	 *
	 * @param bt
	 *            userID of user
	 * @param viewName
	 *            name of custom view (also used as ID)
	 * @param iconNum
	 *            type of icon
	 * @param devices
	 *            list of devices in view
	 * @return addView message
	 * @since 2.2
	 */
	public static String createAddView(String bt, String viewName, int iconNum, List<Device> devices) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.BT, bt);
			serializer.attribute(ns, Xconstants.STATE, ADDVIEW);
			serializer.attribute(ns, Xconstants.NAME, viewName);
			serializer.attribute(ns, Xconstants.ICON, Integer.toString(iconNum));

			for (Device device : devices) {
				serializer.startTag(ns, Xconstants.DEVICE);

				serializer.attribute(ns, Xconstants.AID, device.getFacility().getAdapterId());
				serializer.attribute(ns, Xconstants.DID, device.getId());
				serializer.attribute(ns, Xconstants.TYPE, device.getRawTypeId());
				serializer.endTag(ns, Xconstants.DEVICE);
			}

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, NetworkError.CL_XML);
		}
	}

	/**
	 * Method create one view to update message
	 *
	 * @param bt
	 *            userID of user
	 * @param viewName
	 *            name of view and also ID
	 * @param iconNum
	 *            type of icon
	 * @param device
	 *            device to be updated
	 * @param action
	 *            type of manipulation, add or remove
	 * @return updateView message
	 * @since 2.2
	 */
	public static String createSetView(String bt, String viewName, int iconNum, Device device, NetworkAction action) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.BT, bt);
			serializer.attribute(ns, Xconstants.STATE, SETVIEW);
			serializer.attribute(ns, Xconstants.NAME, viewName);
			serializer.attribute(ns, Xconstants.ICON, Integer.toString(iconNum));

			serializer.startTag(ns, Xconstants.DEVICE);
			serializer.attribute(ns, Xconstants.AID, device.getFacility().getAdapterId());
			serializer.attribute(ns, Xconstants.DID, device.getId());
			serializer.attribute(ns, Xconstants.ACTION, action.getValue());
			serializer.endTag(ns, Xconstants.DEVICE);

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, NetworkError.CL_XML);
		}
	}

	/**
	 * Method create XML of DelVIew message
	 *
	 * @param bt
	 *            userID of user
	 * @param viewName
	 *            name of view and also ID
	 * @return DelVIew message
	 * @since 2.2
	 */
	public static String createDelView(String bt, String viewName) {
		return createComAttribsVariant(Xconstants.STATE, DELVIEW, Xconstants.BT, bt, Xconstants.NAME, viewName);
	}

	/**
	 * Method create XML of GetViews message (method added in 1.6 version)
	 *
	 * @param bt
	 *            userID of user
	 * @return getViews message
	 * @since 2.2
	 */
	public static String createGetViews(String bt) {
		return createComAttribsVariant(Xconstants.STATE, GETVIEWS, Xconstants.BT, bt);
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////ACCOUNTS///////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method create XML for AddAcount message
	 *
	 * @param bt
	 *            userID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @param users
	 *            map with User object and User.Role
	 * @return AddAcc message
	 * @since 2.2
	 */
	public static String createAddAccounts(String bt, String aid, ArrayList<User> users) {
		return createAddSeTAcc(ADDACCOUNTS, bt, aid, users);
	}

	/**
	 * Method create XML for SetAcount message
	 *
	 * @param bt
	 *            userID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @param users
	 *            map with User object and User.Role
	 * @return SetAcc message
	 * @since 2.2
	 */
	public static String createSetAccounts(String bt, String aid, ArrayList<User> users) {
		return createAddSeTAcc(SETCCOUNTS, bt, aid, users);
	}

	/**
	 * Method create XML for DelAcc message
	 *
	 * @param bt
	 *            userID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @param users
	 *            list with Users
	 * @return dellAcc message
	 * @since 2.2
	 */
	public static String createDelAccounts(String bt, String aid, List<User> users) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.BT, bt);
			serializer.attribute(ns, Xconstants.STATE, DELACCOUNTS);
			serializer.attribute(ns, Xconstants.AID, aid);

			for (User user : users) {
				serializer.startTag(ns, Xconstants.USER);
				serializer.attribute(ns, Xconstants.EMAIL, user.getEmail());
				serializer.endTag(ns, Xconstants.USER);
			}

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, NetworkError.CL_XML);
		}
	}

	/**
	 * Method create XML for GetAccs message
	 *
	 * @param bt
	 *            userID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @return GetAcc message
	 * @since 2.2
	 */
	public static String createGetAccounts(String bt, String aid) {
		return createComAttribsVariant(Xconstants.STATE, GETACCOUNTS, Xconstants.BT, bt, Xconstants.AID, aid);
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////TIME///////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method create XML of SetTimeZone message
	 *
	 * @param bt
	 *            userID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @param diffToGMT
	 *            difference to GMT (Xconstants.UTC+0)
	 * @return SetTimeZone message
	 * @since 2.2
	 */
	public static String createSetTimeZone(String bt, String aid, int diffToGMT) {
		return createComAttribsVariant(Xconstants.STATE, SETTIMEZONE, Xconstants.BT, bt, Xconstants.AID, aid, Xconstants.UTC, Integer.toString(diffToGMT));
	}

	/**
	 * Method create XML of GetTimeZone message
	 *
	 * @param bt
	 *            userID of user
	 * @param aid
	 *            adapterID of actual adapter
	 * @return GetTimeZone message
	 * @since 2.2
	 */
	public static String createGetTimeZone(String bt, String aid) {
		return createComAttribsVariant(Xconstants.STATE, GETTIMEZONE, Xconstants.BT, bt, Xconstants.AID, aid);
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////OTHERS/////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method create XML of SetLocale message
	 *
	 * @param bt
	 *            userID of user
	 * @param locale
	 *            of phone
	 * @return message SetLocale
	 * @since 2.2
	 */
	public static String createSetLocale(String bt, String locale) {
		return createComAttribsVariant(Xconstants.STATE, SETLOCALE, Xconstants.BT, bt, Xconstants.LOCALE, locale);
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////CONDITIONS,ACTIONS/////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method create XML of AddCond message
	 *
	 * @param bt
	 *            userID of actual user
	 * @param name
	 *            name of condition
	 * @param type
	 *            type of condition
	 * @param condFuncs
	 *            list of conditions
	 * @return AddCond message
	 * @since 2.2
	 */
	public static String createAddCondition(String bt, String name, ConditionType type, ArrayList<ConditionFunction> condFuncs) {
		return createAddSetCond(ADDCONDITION, bt, name, type, condFuncs, "");
	}

	/**
	 * Method crate XMl of SetCond message
	 *
	 * @param bt
	 *            userID of actual user
	 * @param name
	 *            name of condition
	 * @param type
	 *            type of condition
	 * @param cid
	 *            conditionID to be update
	 * @param condFuncs
	 *            list of conditions
	 * @return SetCond message
	 * @since 2.2
	 */
	public static String createSetCondition(String bt, String name, ConditionType type, String cid, ArrayList<ConditionFunction> condFuncs) {
		return createAddSetCond(SETCONDITION, bt, name, type, condFuncs, cid);
	}

	/**
	 * Method create XML of DelCond message
	 *
	 * @param bt
	 *            userID of actual user
	 * @param cid
	 *            conditionID to be delete
	 * @return DelCond message
	 * @since 2.2
	 */
	public static String createDelCondition(String bt, String cid) {
		return createComAttribsVariant(Xconstants.STATE, DELCONDITION, Xconstants.BT, bt, Xconstants.CID, cid);
	}

	/**
	 * Method create XML of GetCond message
	 *
	 * @param bt
	 *            userID of actual user
	 * @param cid
	 *            conditionID
	 * @return GetCond message
	 * @since 2.2
	 */
	public static String createGetCondition(String bt, String cid) {
		return createComAttribsVariant(Xconstants.STATE, GETCONDITION, Xconstants.BT, bt, Xconstants.CID, cid);
	}

	/**
	 * Method create XML of GetConds message
	 *
	 * @param bt
	 *            userID of actual user
	 * @return GetConds message
	 * @since 2.2
	 */
	public static String createGetConditions(String bt) {
		return createComAttribsVariant(Xconstants.STATE, GETCONDITIONS, Xconstants.BT, bt);
	}

	/**
	 * Method create XML of AddAct message
	 *
	 * @param bt
	 *            userID of actual user
	 * @param name
	 *            name of new action
	 * @param actions
	 *            list of action to add
	 * @return AddAct message
	 * @since 2.2
	 */
	public static String createAddAction(String bt, String name, List<Action> actions) {
		return createAddSetAct(ADDACTION, bt, name, "", actions);
	}

	/**
	 * Method create XML of SetAct message
	 *
	 * @param bt
	 *            userID of actual user
	 * @param name
	 *            name of new action
	 * @param acid
	 *            actionID to be updated
	 * @param actions
	 *            list of action to update
	 * @return SetAct message
	 * @since 2.2
	 */
	public static String createSetAction(String bt, String name, String acid, List<Action> actions) {
		return createAddSetAct(SETACTION, bt, name, acid, actions);
	}

	/**
	 * Method create XML of DelAct
	 *
	 * @param bt
	 *            userID of actual user
	 * @param acid
	 *            actionID to be removed
	 * @return DelAct message
	 * @since 2.2
	 */
	public static String createDelAction(String bt, String acid) {
		return createComAttribsVariant(Xconstants.STATE, DELACTION, Xconstants.BT, bt, Xconstants.ACID, acid);
	}

	/**
	 * Method create XML of GetActs message
	 *
	 * @param bt
	 *            userID of actual user
	 * @return GetActs message
	 * @since 2.2
	 */
	public static String createGetActions(String bt) {
		return createComAttribsVariant(Xconstants.STATE, GETACTIONS, Xconstants.BT, bt);
	}

	/**
	 * Method create XML of GetAct message
	 *
	 * @param bt
	 *            userID of actual user
	 * @param acid
	 *            actionID
	 * @return GetAct message
	 * @since 2.2
	 */
	public static String createGetAction(String bt, String acid) {
		return createComAttribsVariant(Xconstants.STATE, GETACTION, Xconstants.BT, bt, Xconstants.ACID, acid);
	}

	/**
	 * Method create XML of CondAction message
	 *
	 * @param bt
	 *            userID of user
	 * @param cid
	 *            conditionID
	 * @param acid
	 *            actionID
	 * @return CondAction message
	 * @since 2.2
	 */
	public static String createConditionPlusAction(String bt, String cid, String acid) {
		return createComAttribsVariant(Xconstants.STATE, CONDITIONPLUSACTION, Xconstants.BT, bt, Xconstants.CID, cid, Xconstants.ACID, acid);
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////NOTIFICATIONS//////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method create XML of DelXconstants.GCMID message (delete google cloud message id)
	 *
	 * @param userId
	 *            of last logged user
	 * @param gcmid
	 *            id of google messaging
	 * @return message DelXconstants.GCMID
	 * @since 2.2
	 */
	public static String createDeLGCMID(String userId, String gcmid) {
		return createComAttribsVariant(Xconstants.STATE, DELGCMID, Xconstants.UID, userId, Xconstants.GCMID, gcmid);
	}

	/**
	 * Method create XML of SetXconstants.GCMID message
	 *
	 * @param bt
	 *            userID of user logged in now
	 * @param gcmid
	 *            id of google messaging
	 * @return message SetXconstants.GCMID
	 * @since 2.2
	 */
	public static String createSetGCMID(String bt, String gcmid) {
		return createComAttribsVariant(Xconstants.STATE, SETGCMID, Xconstants.BT, bt, Xconstants.GCMID, gcmid);
	}

	/**
	 * Method create XML of GetNotifs message
	 *
	 * @param bt
	 *            SessionID of user
	 * @return message GetNotifs
	 * @since 2.2
	 */
	public static String createGetNotifications(String bt) {
		return createComAttribsVariant(Xconstants.STATE, GETNOTIFICATIONS, Xconstants.BT, bt);
	}

	/**
	 * Method create XML of NotifRead message
	 *
	 * @param bt
	 *            userID of user
	 * @param mids
	 *            list of gcmID of read notification
	 * @return message NotifRead
	 * @since 2.2
	 */
	public static String createNotificaionRead(String bt, List<String> mids) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.BT, bt);
			serializer.attribute(ns, Xconstants.STATE, NOTIFICATIONREAD);

			for (String mid : mids) {
				serializer.startTag(ns, Xconstants.NOTIFICAION);
				serializer.attribute(ns, Xconstants.MSGID, mid);
				serializer.endTag(ns, Xconstants.NOTIFICAION);
			}

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, NetworkError.CL_XML);
		}
	}

	/**
	 * Method create message for creating new rule or editing the old one
	 * @param bt
	 * @param name
	 * @param aid
	 * @param type
	 * @param devices
	 *          list of devices in right position for algorithm
	 * @param params
	 *          list of strings with additional params for new rule
	 * @return
	 */
	public static String createAddSetAlgor(String bt, String name, String algId, String aid, int type, List<String> devices, List<String> params, String regionId, String geoType, Boolean state){
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.BT, bt);
			if(state == null)
				serializer.attribute(ns, Xconstants.STATE, ADDALG);
			else {
				serializer.attribute(ns, Xconstants.STATE, SETALG);
				serializer.attribute(ns, Xconstants.ENABLE, (state.booleanValue()) ? "1" : "0");
				serializer.attribute(ns, Xconstants.ALGID, algId);
			}

			serializer.attribute(ns, Xconstants.ALGNAME, name);
			serializer.attribute(ns, Xconstants.AID, aid);
			serializer.attribute(ns, Xconstants.ATYPE, Integer.toString(type));

			int i = 1;
			for(String device : devices){
				serializer.startTag(ns, Xconstants.DEVICE);
				String[] id_type = device.split(Device.ID_SEPARATOR);
				serializer.attribute(ns, Xconstants.ID, id_type[0]);
				serializer.attribute(ns, Xconstants.TYPE, id_type[1]);
				serializer.attribute(ns, Xconstants.POSITION, Integer.toString(i++));
				serializer.endTag(ns, Xconstants.DEVICE);
			}
			i=1;

			for(String param : params) {
				serializer.startTag(ns, Xconstants.PARAM);
				serializer.attribute(ns, Xconstants.POSITION, Integer.toString(i++));
				serializer.text(param);
				serializer.endTag(ns, Xconstants.PARAM);
			}

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, NetworkError.CL_XML);
		}
	}


	public static String createAddAlgor(String bt, String name, String aid, int type, List<String> devices, List<String> params, String regionId, String geoType){
		return createAddSetAlgor(bt, name, null, aid, type, devices, params, regionId, geoType, null);
	}

	public static String createSetAlgor(String bt, String name, String algId, String aid, int type, boolean enable, List<String> devices, List<String>params, String regionId, String geoType){
		return createAddSetAlgor(bt, name, algId, aid, type, devices, params, regionId, geoType, enable);
	}

	/**
	 * Method return message with demands for specific rules
	 * @param bt
	 * @param aid
	 * @param algids
	 * @return
	 */
	public static String createGetAlgs(String bt, String aid, ArrayList<String> algids){
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.BT, bt);
			serializer.attribute(ns, Xconstants.STATE, GETALGS);
			serializer.attribute(ns, Xconstants.AID, aid);

			for(String algId : algids){
				serializer.startTag(ns, Xconstants.ALGORITHM);
				serializer.attribute(ns, Xconstants.ID, algId);
				serializer.endTag(ns, Xconstants.ALGORITHM);
			}

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, NetworkError.CL_XML);
		}
	}

	/**
	 * Method returns message with demands for all rules of user on specific adapter
	 * @param bt
	 * @return
	 */
	public static String createGetAllAlgs(String bt, String aid){
		return createComAttribsVariant(Xconstants.STATE, GETALLALGS, Xconstants.BT, bt, Xconstants.AID, aid);
	}

	/**
	 * Method returns message with demands for delete specific rule
	 * @param bt
	 * @param algid
	 * @return
	 */
	public static String createDelAlg(String bt, String algid){
		return createComAttribsVariant(Xconstants.STATE, DELALG, Xconstants.BT, bt, Xconstants.ALGID, algid);
	}

	/**
	 * Method create message for PassBorder event
	 * @param bt
	 * @param rid
	 * @param type
	 * @return
	 */
	public static String createPassBorder(String bt, String rid, String type){
		return  createComAttribsVariant(Xconstants.STATE, PASSBORDER, Xconstants.BT, bt, Xconstants.RID, rid, Xconstants.TYPE, type);
	}

	/**
	 * Method create message for GetAllAchievements request
	 * @param bt
	 * @param aid
	 * @return
	 */
	public static String createGetAllAchievements(String bt, String aid){
		return createComAttribsVariant(Xconstants.STATE, GETALLACHIEVEMENTS, Xconstants.BT, bt, Xconstants.AID, aid);
	}

	/**
	 * Method crete message for incrementing progress level of achievement
	 * @param bt
	 * @param aid
	 * @param achId
	 * @return
	 */
	public static String createSetProgressLvl(String bt, String aid, String achId){
		return createComAttribsVariant(Xconstants.STATE, SETPROGRESSLVL, Xconstants.BT, bt, Xconstants.AID, aid, Xconstants.ID, achId);
	}

	/************************************* PRIVATE METHODS *********************************************/

	private static String createComAttribsVariant(String... args) {
		if (0 != (args.length % 2)) { // odd
			throw new RuntimeException("Bad params count");
		}

		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			for (int i = 0; i < args.length; i += 2) { // take pair of args
				serializer.attribute(ns, args[i], args[i + 1]);
			}

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, NetworkError.CL_XML);
		}
	}

	private static String createAddSeTAcc(String state, String bt, String aid, ArrayList<User> users) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.BT, bt);
			serializer.attribute(ns, Xconstants.STATE, state);
			serializer.attribute(ns, Xconstants.AID, aid);

			for (User user : users) {
				serializer.startTag(ns, Xconstants.USER);

				serializer.attribute(ns, Xconstants.EMAIL, user.getEmail());
				serializer.attribute(ns, Xconstants.ROLE, user.getRole().getValue());
				serializer.endTag(ns, Xconstants.USER);
			}

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, NetworkError.CL_XML);
		}
	}

	private static String createAddSetCond(String state, String bt, String name, ConditionType type, ArrayList<ConditionFunction> condFuncs, String cid) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.BT, bt);
			serializer.attribute(ns, Xconstants.STATE, state);
			serializer.attribute(ns, Xconstants.CNAME, name);
			serializer.attribute(ns, Xconstants.CTYPE, type.getValue());
			if (state.equals(SETCONDITION))
				serializer.attribute(ns, Xconstants.ID, cid);

			for (ConditionFunction func : condFuncs) {
				serializer.startTag(ns, Xconstants.FUNC);
				serializer.attribute(ns, Xconstants.TYPE, func.getFuncType().getValue());

				switch (func.getFuncType()) {
				case EQ:
					serializer.startTag(ns, Xconstants.DEVICE);
					serializer.attribute(ns, Xconstants.ID, ((EqualFunc) func).getDevice().getId());
					serializer.attribute(ns, Xconstants.TYPE, ((EqualFunc) func).getDevice().getRawTypeId());
					serializer.endTag(ns, Xconstants.DEVICE);

					serializer.startTag(ns, Xconstants.VALUE);
					serializer.text(((EqualFunc) func).getValue());
					serializer.endTag(ns, Xconstants.VALUE);
					break;
				case GT:
					serializer.startTag(ns, Xconstants.DEVICE);
					serializer.attribute(ns, Xconstants.ID, ((GreaterThanFunc) func).getDevice().getId());
					serializer.attribute(ns, Xconstants.TYPE, ((GreaterThanFunc) func).getDevice().getRawTypeId());
					serializer.endTag(ns, Xconstants.DEVICE);

					serializer.startTag(ns, Xconstants.VALUE);
					serializer.text(((GreaterThanFunc) func).getValue());
					serializer.endTag(ns, Xconstants.VALUE);
					break;
				case GE:
					serializer.startTag(ns, Xconstants.DEVICE);
					serializer.attribute(ns, Xconstants.ID, ((GreaterEqualFunc) func).getDevice().getId());
					serializer.attribute(ns, Xconstants.TYPE, ((GreaterEqualFunc) func).getDevice().getRawTypeId());
					serializer.endTag(ns, Xconstants.DEVICE);

					serializer.startTag(ns, Xconstants.VALUE);
					serializer.text(((GreaterEqualFunc) func).getValue());
					serializer.endTag(ns, Xconstants.VALUE);
					break;
				case LT:
					serializer.startTag(ns, Xconstants.DEVICE);
					serializer.attribute(ns, Xconstants.ID, ((LesserThanFunc) func).getDevice().getId());
					serializer.attribute(ns, Xconstants.TYPE, ((LesserThanFunc) func).getDevice().getRawTypeId());
					serializer.endTag(ns, Xconstants.DEVICE);

					serializer.startTag(ns, Xconstants.VALUE);
					serializer.text(((LesserThanFunc) func).getValue());
					serializer.endTag(ns, Xconstants.VALUE);
					break;
				case LE:
					serializer.startTag(ns, Xconstants.DEVICE);
					serializer.attribute(ns, Xconstants.ID, ((LesserEqualFunc) func).getDevice().getId());
					serializer.attribute(ns, Xconstants.TYPE, ((LesserEqualFunc) func).getDevice().getRawTypeId());
					serializer.endTag(ns, Xconstants.DEVICE);

					serializer.startTag(ns, Xconstants.VALUE);
					serializer.text(((LesserEqualFunc) func).getValue());
					serializer.endTag(ns, Xconstants.VALUE);
					break;
				case BTW:
					serializer.startTag(ns, Xconstants.DEVICE);
					serializer.attribute(ns, Xconstants.ID, ((BetweenFunc) func).getDevice().getId());
					serializer.attribute(ns, Xconstants.TYPE, ((BetweenFunc) func).getDevice().getRawTypeId());
					serializer.endTag(ns, Xconstants.DEVICE);

					serializer.startTag(ns, Xconstants.VALUE);
					serializer.text(((BetweenFunc) func).getMinValue());
					serializer.endTag(ns, Xconstants.VALUE);

					serializer.startTag(ns, Xconstants.VALUE);
					serializer.text(((BetweenFunc) func).getMaxValue());
					serializer.endTag(ns, Xconstants.VALUE);
					break;
				case DP:
					serializer.startTag(ns, Xconstants.DEVICE);
					serializer.attribute(ns, Xconstants.ID, ((DewPointFunc) func).getTempDevice().getId());
					serializer.attribute(ns, Xconstants.TYPE, ((DewPointFunc) func).getTempDevice().getType() + "");
					serializer.endTag(ns, Xconstants.DEVICE);

					serializer.startTag(ns, Xconstants.DEVICE);
					serializer.attribute(ns, Xconstants.ID, ((DewPointFunc) func).getHumiDevice().getId());
					serializer.attribute(ns, Xconstants.TYPE, ((DewPointFunc) func).getHumiDevice().getRawTypeId());
					serializer.endTag(ns, Xconstants.DEVICE);
					break;
				case TIME:
					serializer.startTag(ns, Xconstants.VALUE);
					serializer.text(((TimeFunc) func).getTime());
					serializer.endTag(ns, Xconstants.VALUE);
					break;
				case GEO:
					serializer.startTag(ns, Xconstants.VALUE);
					// FIXME: wait for martin
					serializer.text("TODO");
					serializer.endTag(ns, Xconstants.VALUE);
					break;
				default:
					break;
				}

				serializer.endTag(ns, Xconstants.FUNC);
			}

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, NetworkError.CL_XML);
		}
	}

	private static String createAddSetAct(String state, String bt, String name, String actid, List<Action> actions) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.BT, bt);
			serializer.attribute(ns, Xconstants.STATE, state);
			serializer.attribute(ns, Xconstants.ACNAME, name);
			if (state.equals(SETACTION))
				serializer.attribute(ns, Xconstants.ACID, actid);

			for (Action action : actions) {
				serializer.startTag(ns, Xconstants.ACTION);
				serializer.attribute(ns, Xconstants.TYPE, action.getType().getValue());

				if (action.getType() == Action.ActionType.ACTOR) {
					serializer.startTag(ns, Xconstants.DEVICE);

					serializer.attribute(ns, Xconstants.ID, action.getDevice().getId());
					serializer.attribute(ns, Xconstants.TYPE, action.getDevice().getRawTypeId());
					serializer.attribute(ns, Xconstants.VALUE, action.getValue());
					serializer.endTag(ns, Xconstants.DEVICE);
				}
				serializer.endTag(ns, Xconstants.ACTION);
			}

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, NetworkError.CL_XML);
		}
	}
}
