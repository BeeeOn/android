package com.rehivetech.beeeon.network.xml;

import android.util.Xml;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ClientError;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.Module.SaveModule;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;

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
 */
public class XmlCreator {

	private static final String ns = null;

	private static final String COM_VER = Constants.COM_VER;

	// states

	public static final String SIGNIN = "signin";
	public static final String SIGNUP = "signup";
	public static final String GETUSERINFO = "getuserinfo";
	public static final String JOINACCOUNT = "joinaccount";
	public static final String CUTACCOUNT = "cutaccount";

	public static final String ADDADAPTER = "addadapter";
	public static final String REINITADAPTER = "reinitadapter";
	public static final String GETADAPTERS = "getadapters";
	public static final String SCANMODE = "scanmode";

	public static final String ADDACCOUNTS = "addaccs";
	public static final String DELACCOUNTS = "delaccs";
	public static final String GETACCOUNTS = "getaccs";
	public static final String SETCCOUNTS = "setaccs";

	public static final String SETDEVS = "setdevs";
	public static final String GETDEVICES = "getdevs";
	public static final String GETALLDEVICES = "getalldevs";
	public static final String DELDEVICE = "deldev";
	public static final String SWITCH = "switch";
	public static final String GETLOG = "getlog";
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

	public static final String SETLOCALE = "setlocale";

	public static final String ADDALG = "addalg";
	public static final String GETALLALGS = "getallalgs";
	public static final String GETALGS = "getlags";
	public static final String SETALG = "setalg";
	public static final String DELALG = "delalg";

	public static final String PASSBORDER = "passborder";
	public static final String GETALLACHIEVEMENTS = "getallachievements";
	public static final String SETPROGRESSLVL = "setprogresslvl";

	// end of states

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

	// /////////////////////////////////////SIGNIN,SIGNUP,ADAPTERS/////////////////////////////////////

	/**
	 * Method create message for registration of new user
	 *
	 * @param authProvider provider of authentication with parameters to send
	 * @return xml with signUp message
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
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create message for login
	 *
	 * @param locale       localization of phone
	 * @param pid          unique id of phone
	 * @param authProvider provider of authentication with parameters to send
	 * @return xml with signIn message
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
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create message for joining new provider to actual user
	 *
	 * @param authProvider provider of authentication with parameters to send
	 * @return xml with joinAccount message
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
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create message for removing part of account (or whole account)
	 *
	 * @param bt           beeeon Token (session Id)
	 * @param providerName name of service (beeeon, google, facebook, ...)
	 * @return xml with cutAccount message
	 */
	public static String createCutAccount(String bt, String providerName) {
		return createComAttribsVariant(Xconstants.STATE, CUTACCOUNT, Xconstants.BT, bt, Xconstants.SERVICE, providerName);
	}

	/**
	 * Method create message for obtain information about user
	 *
	 * @param bt beeeon Token (session Id)
	 * @return xml with getUserInfo message
	 */
	public static String createGetUserInfo(String bt) {
		return createComAttribsVariant(Xconstants.STATE, GETUSERINFO, Xconstants.BT, bt);
	}

	/**
	 * Method create XML for AddAdapter message
	 *
	 * @param bt          userID of user
	 * @param aid         adapterID of actual gate
	 * @param adapterName name of gate
	 * @return AddAdapter message
	 * @since 2.2
	 */
	public static String createAddAdapter(String bt, String aid, String adapterName) {
		return createComAttribsVariant(Xconstants.STATE, ADDADAPTER, Xconstants.BT, bt, Xconstants.AID, aid, Xconstants.ANAME, adapterName);
	}

	/**
	 * Method create XML of GetAdapters message
	 *
	 * @param bt userID of user
	 * @return GetAdapters message
	 * @since 2.2
	 */
	public static String createGetAdapters(String bt) {
		return createComAttribsVariant(Xconstants.STATE, GETADAPTERS, Xconstants.BT, bt);
	}

	/**
	 * Method create XML for ReInit message
	 *
	 * @param bt           userID of user
	 * @param adapterIdOld old id of gate
	 * @param adapterIdNew new id of gate
	 * @return ReInit message
	 * @since 2.2
	 */
	public static String createReInitAdapter(String bt, String adapterIdOld, String adapterIdNew) {
		return createComAttribsVariant(Xconstants.STATE, REINITADAPTER, Xconstants.BT, bt, Xconstants.OLDID, adapterIdOld, Xconstants.NEWID, adapterIdNew);
	}

	// /////////////////////////////////////DEVICES,LOGS///////////////////////////////////////////////

	/**
	 * Method create XML for AdapterListen message
	 *
	 * @param bt  userID of user
	 * @param aid adapterID of actual gate
	 * @return XML of AdapterListen message
	 * @since 2.2
	 */
	public static String createAdapterScanMode(String bt, String aid) {
		return createComAttribsVariant(Xconstants.STATE, SCANMODE, Xconstants.BT, bt, Xconstants.AID, aid);
	}

	/**
	 * Method create XML for GetAllDevices message
	 *
	 * @param bt  userID of user
	 * @param aid adapterID of actual gate
	 * @return XML of GetAllDevices message
	 * @since 2.2
	 */
	public static String createGetAllDevices(String bt, String aid) {
		return createComAttribsVariant(Xconstants.STATE, GETALLDEVICES, Xconstants.BT, bt, Xconstants.AID, aid);
	}

	/**
	 * Method create XML for getting uninitialized devices
	 *
	 * @param bt  userID of user
	 * @param aid adapterID of actual gate
	 * @return XML of GetNewDevices message
	 * @since 2.2
	 */
	public static String createGetNewDevices(String bt, String aid) {
		return createComAttribsVariant(Xconstants.STATE, GETNEWDEVICES, Xconstants.BT, bt, Xconstants.AID, aid);
	}

	/**
	 * Method create XML of GetDevices message
	 *
	 * @param bt      userID of user
	 * @param devices devices with devices to update
	 * @return update message
	 * @since 2.2
	 */
	public static String createGetDevices(String bt, List<Device> devices) {
		if (devices.size() < 1)
			throw new IllegalArgumentException("Expected more than zero devices");
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.BT, bt);
			serializer.attribute(ns, Xconstants.STATE, GETDEVICES);

			// sort by gate address
			Collections.sort(devices, new Comparator<Device>() {

				@Override
				public int compare(Device left, Device right) {
					return Integer.valueOf(left.getAdapterId()).compareTo(Integer.valueOf(right.getAdapterId()));
				}
			});

			String aid = "";
			for (Device device : devices) {

				boolean isSameAdapter = aid.equals(device.getAdapterId());
				if (!isSameAdapter) { // new gate
					if (aid.length() > 0)
						serializer.endTag(ns, Xconstants.ADAPTER);
					aid = device.getAdapterId();
					serializer.startTag(ns, Xconstants.ADAPTER);
					serializer.attribute(ns, Xconstants.ID, aid);
				}
				serializer.startTag(ns, Xconstants.MODULE);
				serializer.attribute(ns, Xconstants.ID, device.getAddress());

				for (Module module : device.getModules()) {
					serializer.startTag(ns, Xconstants.PART);
					serializer.attribute(ns, Xconstants.TYPE, module.getRawTypeId());
					serializer.endTag(ns, Xconstants.PART);
				}
				serializer.endTag(ns, Xconstants.MODULE);
			}
			serializer.endTag(ns, Xconstants.ADAPTER);

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML for GetLog message
	 *
	 * @param bt         userID of user
	 * @param aid        adapterID of actual gate
	 * @param did        deviceID of wanted module
	 * @param moduleType is type of sensor
	 * @param from       date in unix timestamp
	 * @param to         date in unix timestamp
	 * @param funcType   is aggregation function type {avg, median, ...}
	 * @param interval   is time value in seconds that represents nicely e.g. month, week, day, 10 hours, 1 hour, ...
	 * @return GetLog message
	 * @since 2.2
	 */
	public static String createGetLog(String bt, String aid, String did, String moduleType, String from, String to, String funcType, int interval) {
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
			serializer.attribute(ns, Xconstants.DTYPE, moduleType);

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML of SetDevs message. Almost all fields are optional
	 *
	 * @param bt      userID of user
	 * @param aid     adapterID of actual gate
	 * @param devices with changed fields
	 * @return Partial message
	 * @since 2.2
	 */
	public static String createSetDevs(String bt, String aid, List<Device> devices, EnumSet<SaveModule> toSave) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.BT, bt);
			serializer.attribute(ns, Xconstants.STATE, SETDEVS);
			serializer.attribute(ns, Xconstants.AID, aid);

			for (Device device : devices) {
				serializer.startTag(ns, Xconstants.MODULE);

				if (toSave.contains(SaveModule.SAVE_INITIALIZED))
					serializer.attribute(ns, Xconstants.INITIALIZED, (device.isInitialized()) ? Xconstants.ONE : Xconstants.ZERO);
				serializer.attribute(ns, Xconstants.DID, device.getAddress());
				if (toSave.contains(SaveModule.SAVE_LOCATION))
					serializer.attribute(ns, Xconstants.LID, device.getLocationId());
				if (toSave.contains(SaveModule.SAVE_REFRESH))
					serializer.attribute(ns, Xconstants.REFRESH, Integer.toString(device.getRefresh().getInterval()));

				for (Module module : device.getModules()) {
					serializer.startTag(ns, Xconstants.PART);

					serializer.attribute(ns, Xconstants.TYPE, module.getRawTypeId());
					if (toSave.contains(SaveModule.SAVE_VISIBILITY))
						serializer.attribute(ns, Xconstants.VISIBILITY, (module.isVisible()) ? Xconstants.ONE : Xconstants.ZERO);
					if (toSave.contains(SaveModule.SAVE_NAME))
						serializer.attribute(ns, Xconstants.NAME, module.getName());
					// if (toSave.contains(SaveModule.SAVE_VALUE))
					// serializer.attribute(ns, Xconstants.VALUE, String.valueOf(module.getId().getDoubleValue()));

					serializer.endTag(ns, Xconstants.PART);
				}
				serializer.endTag(ns, Xconstants.MODULE);
			}

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * New method create XML of SetDevs message with only one module in it. toSave parameter must by set properly.
	 *
	 * @param bt     userID of user
	 * @param aid    adapterID of actual gate
	 * @param module to save
	 * @param toSave ECO mode to save only wanted fields
	 * @return SetDevs message
	 * @since 2.2
	 */
	public static String createSetDev(String bt, String aid, Module module, EnumSet<SaveModule> toSave) {
		StringWriter writer = new StringWriter();
		try {
			Device device = module.getDevice();

			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.BT, bt);
			serializer.attribute(ns, Xconstants.STATE, SETDEVS);
			serializer.attribute(ns, Xconstants.AID, aid);

			serializer.startTag(ns, Xconstants.MODULE);

			if (toSave.contains(SaveModule.SAVE_INITIALIZED))
				serializer.attribute(ns, Xconstants.INITIALIZED, (device.isInitialized()) ? Xconstants.ONE : Xconstants.ZERO);
			// send always
			serializer.attribute(ns, Xconstants.DID, device.getAddress());
			if (toSave.contains(SaveModule.SAVE_LOCATION))
				serializer.attribute(ns, Xconstants.LID, device.getLocationId());
			if (toSave.contains(Module.SaveModule.SAVE_REFRESH))
				serializer.attribute(ns, Xconstants.REFRESH, Integer.toString(device.getRefresh().getInterval()));

			if (toSave.contains(SaveModule.SAVE_NAME) || toSave.contains(Module.SaveModule.SAVE_VALUE)) {
				serializer.startTag(ns, Xconstants.PART);
				// send always if sensor changed
				serializer.attribute(ns, Xconstants.TYPE, module.getRawTypeId());
				if (toSave.contains(SaveModule.SAVE_NAME))
					serializer.attribute(ns, Xconstants.NAME, module.getName());
				if (toSave.contains(SaveModule.SAVE_VALUE))
					serializer.attribute(ns, Xconstants.VALUE, String.valueOf(module.getValue().getDoubleValue()));

				serializer.endTag(ns, Xconstants.PART);
			}

			serializer.endTag(ns, Xconstants.MODULE);

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML for Switch message
	 *
	 * @param bt     userID of user
	 * @param aid    adapterID of actual gate
	 * @param module to switch value
	 * @return XML of Switch message
	 * @since 2.2
	 */
	public static String createSwitch(String bt, String aid, Module module) {
		return createComAttribsVariant(Xconstants.STATE, SWITCH, Xconstants.BT, bt, Xconstants.AID, aid, Xconstants.DID, module.getDevice().getAddress(), Xconstants.DTYPE,
				module.getRawTypeId(), Xconstants.VALUE, String.valueOf(module.getValue().getDoubleValue()));
	}

	/**
	 * Method create XML of DelDevice message
	 *
	 * @param bt     userID of user
	 * @param device to be removed
	 * @return XML of DelDevice message
	 * @since 2.2
	 */
	public static String createDeleteDevice(String bt, Device device) {
		return createComAttribsVariant(Xconstants.STATE, DELDEVICE, Xconstants.BT, bt, Xconstants.AID, device.getAdapterId(), Xconstants.DID, device.getAddress());
	}

	// /////////////////////////////////////ROOMS//////////////////////////////////////////////////////

	/**
	 * Method create XML of AddRoom message
	 *
	 * @param bt       userID of user
	 * @param location to create
	 * @return created message
	 * @since 2.2
	 */
	public static String createAddRoom(String bt, Location location) {
		return createComAttribsVariant(Xconstants.STATE, ADDROOM, Xconstants.BT, bt, Xconstants.AID, location.getAdapterId(), Xconstants.LTYPE, location.getType(), Xconstants.LNAME,
				location.getName());
	}

	/**
	 * Method create XML of SetRooms message
	 *
	 * @param bt        userID of user
	 * @param aid       adapterID of actual gate
	 * @param locations list of location object to update
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
				serializer.attribute(ns, Xconstants.TYPE, location.getType());
				serializer.attribute(ns, Xconstants.NAME, location.getName());

				serializer.endTag(ns, Xconstants.LOCATION);
			}

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML of DelRoom message
	 *
	 * @param bt  userID of user
	 * @param lid locationID of location to delete
	 * @return DelRoom message
	 * @since 2.2
	 */
	public static String createDeleteRoom(String bt, Location location) {
		return createComAttribsVariant(Xconstants.STATE, DELROOM, Xconstants.BT, bt, Xconstants.AID, location.getAdapterId(), Xconstants.LID, location.getId());
	}

	/**
	 * Method create XML of GetRooms message
	 *
	 * @param bt  userID of user
	 * @param aid adapterID of actual gate
	 * @return message GetRooms
	 * @since 2.2
	 */
	public static String createGetRooms(String bt, String aid) {
		return createComAttribsVariant(Xconstants.STATE, GETROOMS, Xconstants.BT, bt, Xconstants.AID, aid);
	}

	// /////////////////////////////////////ACCOUNTS///////////////////////////////////////////////////

	/**
	 * Method create XML for AddAcount message
	 *
	 * @param bt    userID of user
	 * @param aid   adapterID of actual gate
	 * @param users map with User object and User.Role
	 * @return AddAcc message
	 * @since 2.2
	 */
	public static String createAddAccounts(String bt, String aid, ArrayList<User> users) {
		return createAddSeTAcc(ADDACCOUNTS, bt, aid, users);
	}

	/**
	 * Method create XML for SetAcount message
	 *
	 * @param bt    userID of user
	 * @param aid   adapterID of actual gate
	 * @param users map with User object and User.Role
	 * @return SetAcc message
	 * @since 2.2
	 */
	public static String createSetAccounts(String bt, String aid, ArrayList<User> users) {
		return createAddSeTAcc(SETCCOUNTS, bt, aid, users);
	}

	/**
	 * Method create XML for DelAcc message
	 *
	 * @param bt    userID of user
	 * @param aid   adapterID of actual gate
	 * @param users list with Users
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
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML for GetAccs message
	 *
	 * @param bt  userID of user
	 * @param aid adapterID of actual gate
	 * @return GetAcc message
	 * @since 2.2
	 */
	public static String createGetAccounts(String bt, String aid) {
		return createComAttribsVariant(Xconstants.STATE, GETACCOUNTS, Xconstants.BT, bt, Xconstants.AID, aid);
	}

	// /////////////////////////////////////TIME///////////////////////////////////////////////////////

	/**
	 * Method create XML of SetTimeZone message
	 *
	 * @param bt              userID of user
	 * @param aid             adapterID of actual gate
	 * @param offsetInMinutes difference to GMT (Xconstants.UTC+0)
	 * @return SetTimeZone message
	 * @since 2.2
	 */
	public static String createSetTimeZone(String bt, String aid, int offsetInMinutes) {
		return createComAttribsVariant(Xconstants.STATE, SETTIMEZONE, Xconstants.BT, bt, Xconstants.AID, aid, Xconstants.UTC, Integer.toString(offsetInMinutes));
	}

	/**
	 * Method create XML of GetTimeZone message
	 *
	 * @param bt  userID of user
	 * @param aid adapterID of actual gate
	 * @return GetTimeZone message
	 * @since 2.2
	 */
	public static String createGetTimeZone(String bt, String aid) {
		return createComAttribsVariant(Xconstants.STATE, GETTIMEZONE, Xconstants.BT, bt, Xconstants.AID, aid);
	}

	// /////////////////////////////////////LOCALE/////////////////////////////////////////////////////

	/**
	 * Method create XML of SetLocale message
	 *
	 * @param bt     userID of user
	 * @param locale of phone
	 * @return message SetLocale
	 * @since 2.2
	 */
	public static String createSetLocale(String bt, String locale) {
		return createComAttribsVariant(Xconstants.STATE, SETLOCALE, Xconstants.BT, bt, Xconstants.LOCALE, locale);
	}

	// /////////////////////////////////////NOTIFICATIONS//////////////////////////////////////////////

	/**
	 * Method create XML of DelXconstants.GCMID message (delete google cloud message id)
	 *
	 * @param userId of last logged user
	 * @param gcmid  id of google messaging
	 * @return message GCMID
	 * @since 2.2
	 */
	public static String createDeLGCMID(String userId, String gcmid) {
		return createComAttribsVariant(Xconstants.STATE, DELGCMID, Xconstants.UID, userId, Xconstants.GCMID, gcmid);
	}

	/**
	 * Method create XML of SetXconstants.GCMID message
	 *
	 * @param bt    userID of user logged in now
	 * @param gcmid id of google messaging
	 * @return message SetXconstants.GCMID
	 * @since 2.2
	 */
	public static String createSetGCMID(String bt, String gcmid) {
		return createComAttribsVariant(Xconstants.STATE, SETGCMID, Xconstants.BT, bt, Xconstants.GCMID, gcmid);
	}

	/**
	 * Method create XML of GetNotifs message
	 *
	 * @param bt SessionID of user
	 * @return message GetNotifs
	 * @since 2.2
	 */
	public static String createGetNotifications(String bt) {
		return createComAttribsVariant(Xconstants.STATE, GETNOTIFICATIONS, Xconstants.BT, bt);
	}

	/**
	 * Method create XML of NotifRead message
	 *
	 * @param bt   userID of user
	 * @param mids list of gcmID of read notification
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
				serializer.startTag(ns, Xconstants.NOTIFICATION);
				serializer.attribute(ns, Xconstants.MSGID, mid);
				serializer.endTag(ns, Xconstants.NOTIFICATION);
			}

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	// /////////////////////////////////////ALGORITHMS//////////////////////////////////////////////

	/**
	 * Method create message for creating new rule or editing the old one
	 *
	 * @param bt      beeeon Token (session Id)
	 * @param name    of algorithm
	 * @param aid     gate id
	 * @param type    of algorithm
	 * @param modules list of modules in right position for algorithm
	 * @param params  list of strings with additional params for new rule
	 * @return xml with add or set algorithm
	 */
	public static String createAddSetAlgor(String bt, String name, String algId, String aid, int type, List<String> modules, List<String> params, String regionId, Boolean state) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.BT, bt);
			if (state == null)
				serializer.attribute(ns, Xconstants.STATE, ADDALG);
			else {
				serializer.attribute(ns, Xconstants.STATE, SETALG);
				serializer.attribute(ns, Xconstants.ENABLE, state ? "1" : "0");
				serializer.attribute(ns, Xconstants.ALGID, algId);
			}

			serializer.attribute(ns, Xconstants.ALGNAME, name);
			serializer.attribute(ns, Xconstants.AID, aid);
			serializer.attribute(ns, Xconstants.ATYPE, Integer.toString(type));

			int i = 1;
			if (regionId != null && !regionId.isEmpty()) {
				serializer.startTag(ns, Xconstants.GEOFENCE);
				serializer.attribute(ns, Xconstants.ID, regionId);
				serializer.attribute(ns, Xconstants.POSITION, Integer.toString(i++));
				serializer.endTag(ns, Xconstants.GEOFENCE);
			}

			i = 1;

			for (String module : modules) {
				serializer.startTag(ns, Xconstants.MODULE);
				String[] id_type = module.split(Module.ID_SEPARATOR);
				serializer.attribute(ns, Xconstants.ID, id_type[0]);
				serializer.attribute(ns, Xconstants.TYPE, id_type[1]);
				serializer.attribute(ns, Xconstants.POSITION, Integer.toString(i++));
				serializer.endTag(ns, Xconstants.MODULE);
			}
			i = 1;

			for (String param : params) {
				serializer.startTag(ns, Xconstants.PARAM);
				serializer.attribute(ns, Xconstants.POSITION, Integer.toString(i++));
				serializer.text(param);
				serializer.endTag(ns, Xconstants.PARAM);
			}

			//TODO: GEO TAG!!

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	public static String createAddAlgor(String bt, String name, String aid, int type, List<String> modules, List<String> params, String regionId) {
		return createAddSetAlgor(bt, name, null, aid, type, modules, params, regionId, null);
	}

	public static String createSetAlgor(String bt, String name, String algId, String aid, int type, boolean enable, List<String> modules, List<String> params, String regionId) {
		return createAddSetAlgor(bt, name, algId, aid, type, modules, params, regionId, enable);
	}

	/**
	 * Method return message with demands for specific rules
	 *
	 * @param bt     beeeon Token (session Id)
	 * @param aid    gate id
	 * @param algids algorithm id
	 * @return xml with getAlgs message
	 */
	public static String createGetAlgs(String bt, String aid, ArrayList<String> algids) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.BT, bt);
			serializer.attribute(ns, Xconstants.STATE, GETALGS);
			serializer.attribute(ns, Xconstants.AID, aid);

			for (String algId : algids) {
				serializer.startTag(ns, Xconstants.ALGORITHM);
				serializer.attribute(ns, Xconstants.ID, algId);
				serializer.endTag(ns, Xconstants.ALGORITHM);
			}

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method returns message with demands for all rules of user on specific gate
	 *
	 * @param bt beeeon Token (session Id)
	 * @return xlm with getAllAlgs message
	 */
	public static String createGetAllAlgs(String bt, String aid) {
		return createComAttribsVariant(Xconstants.STATE, GETALLALGS, Xconstants.BT, bt, Xconstants.AID, aid);
	}

	/**
	 * Method returns message with demands for delete specific rule
	 *
	 * @param bt    beeeon Token (session Id)
	 * @param algid gate id
	 * @return xml with delAlg message
	 */
	public static String createDelAlg(String bt, String algid) {
		return createComAttribsVariant(Xconstants.STATE, DELALG, Xconstants.BT, bt, Xconstants.ALGID, algid);
	}

	// /////////////////////////////////////GEOFENCING//////////////////////////////////////////////

	/**
	 * Method create message for PassBorder event
	 *
	 * @param bt   beeeon Token (session Id)
	 * @param rid  region id
	 * @param type type of passing (in/out)
	 * @return xml with passBorder message
	 */
	public static String createPassBorder(String bt, String rid, String type) {
		return createComAttribsVariant(Xconstants.STATE, PASSBORDER, Xconstants.BT, bt, Xconstants.RID, rid, Xconstants.TYPE, type);
	}

	/**
	 * Method create message for GetAllAchievements request
	 *
	 * @param bt
	 * @param aid
	 * @return
	 */
	public static String createGetAllAchievements(String bt, String aid) {
		return createComAttribsVariant(Xconstants.STATE, GETALLACHIEVEMENTS, Xconstants.BT, bt, Xconstants.AID, aid);
	}

	/**
	 * Method crete message for incrementing progress level of achievement
	 *
	 * @param bt
	 * @param aid
	 * @param achId
	 * @return
	 */
	public static String createSetProgressLvl(String bt, String aid, String achId) {
		return createComAttribsVariant(Xconstants.STATE, SETPROGRESSLVL, Xconstants.BT, bt, Xconstants.AID, aid, Xconstants.ID, achId);
	}

	// /////////////////////////////////////PRIVATE METHODS//////////////////////////////////////////////

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
			throw AppException.wrap(e, ClientError.XML);
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
				serializer.attribute(ns, Xconstants.ROLE, user.getRole().getId());
				serializer.endTag(ns, Xconstants.USER);
			}

			endXml(serializer);

			return writer.toString();
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}
}
