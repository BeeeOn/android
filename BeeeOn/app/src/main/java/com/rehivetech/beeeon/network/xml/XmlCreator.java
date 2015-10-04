package com.rehivetech.beeeon.network.xml;

import android.support.annotation.Nullable;
import android.util.Xml;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ClientError;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class for creating XML messages
 *
 * @author ThinkDeep
 * @author Robyer
 */
public class XmlCreator {

	protected static final String ns = null;

	protected static XmlSerializer beginXml(StringWriter writer, String state, @Nullable String sessionId) throws IOException {
		XmlSerializer serializer = Xml.newSerializer();

		serializer.setOutput(writer);
		serializer.startDocument("UTF-8", null);

		serializer.startTag(ns, "com");
		serializer.attribute(ns, "version", Constants.PROTOCOL_VERSION); // every time use version
		serializer.attribute(ns, "state", state);
		if (sessionId != null) {
			serializer.attribute(ns, "sessionid", sessionId);
		}

		return serializer;
	}

	protected static String endXml(StringWriter writer, XmlSerializer serializer) throws IOException {
		serializer.text("");
		serializer.endTag(ns, "com");
		serializer.endDocument();

		return writer.toString();
	}

	protected static void startTag(XmlSerializer serializer, String tag) throws IOException {
		serializer.startTag(ns, tag);
	}

	protected static void endTag(XmlSerializer serializer, String tag) throws IOException {
		serializer.endTag(ns, tag);
	}

	protected static void addTag(XmlSerializer serializer, String tag, String... attributes) throws IOException {
		startTag(serializer, tag);

		if (attributes.length % 2 != 0) { // odd
			throw new IllegalArgumentException("Invalid attributes count");
		}

		for (int i = 0; i < attributes.length; i += 2) { // take pair of args
			String name = attributes[i];
			String value = attributes[i + 1];

			if (value != null)
				serializer.attribute(ns, name, value);
		}

		endTag(serializer, tag);
	}

	protected static void addAttribute(XmlSerializer serializer, String name, String value) throws IOException {
		serializer.attribute(ns, name, value);
	}

	// /////////////////////////////////////SIGNIN,SIGNUP,GATES/////////////////////////////////////

	/**
	 * Method create message for registration of new user
	 *
	 * @param authProvider provider of authentication with parameters to send
	 * @return xml with signUp message
	 */
	public static String createRegister(IAuthProvider authProvider) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "register", null);

			startTag(serializer, "provider");
				addAttribute(serializer, "name", authProvider.getProviderName());

				for (Map.Entry<String, String> entry : authProvider.getParameters().entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();

					if (key != null && value != null)
						addAttribute(serializer, key, value);
				}
			endTag(serializer, "provider");

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create message for login
	 *
	 * @param phone        name of phone model
	 * @param authProvider provider of authentication with parameters to send
	 * @return xml with signIn message
	 */
	public static String createLogin(String phone, IAuthProvider authProvider) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "login", null);

			startTag(serializer, "provider");
				addAttribute(serializer, "name", authProvider.getProviderName());

				for (Map.Entry<String, String> entry : authProvider.getParameters().entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();

					if (key != null && value != null)
						addAttribute(serializer, key, value);
				}
			endTag(serializer, "provider");

			addTag(serializer, "phone", "name", phone);

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	public static String createUpdateUser(String bt, User user) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "updateuser", bt);

			addTag(serializer, "user",
					"name", user.getName(),
					"surname", user.getSurname(),
					"gender", user.getGender().getId(),
					"email", user.getEmail(),
					"imgurl", user.getPictureUrl());

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create message for obtain information about user
	 *
	 * @param bt beeeon Token (session Id)
	 * @return xml with getUserInfo message
	 */
	public static String createGetUserInfo(String bt) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "getuserinfo", bt);
			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create message for loging out user
	 * @param bt beeeon Token (session Id)
	 * @return xml with logout message
	 * @since 2.5
	 */
	public static String createLogout(String bt){
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "logout", bt);
			return endXml(writer, serializer);
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
			XmlSerializer serializer = beginXml(writer, "connectauthprovider", bt);

			startTag(serializer, "provider");
				addAttribute(serializer, "name", authProvider.getProviderName());

				for (Map.Entry<String, String> entry : authProvider.getParameters().entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();

					if (key != null && value != null)
						addAttribute(serializer, key, value);
				}
			endTag(serializer, "provider");

			return endXml(writer, serializer);
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
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "disconnectAuthProvider", bt);

			addTag(serializer, "provider",
					"name", providerName);

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML for AddGate message
	 *
	 * @param bt       userID of user
	 * @param aid      gateId of actual gate
	 * @param gateName name of gate
	 * @return AddGate message
	 * @since 2.2
	 */
	public static String createAddGate(String bt, String aid, String gateName) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "addgate", bt);

			addTag(serializer, "gate",
					"id", aid,
					"name", gateName);

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create message for removing actual user from adapter
	 * @param bt beeeon Token (session Id)
	 * @param aid gate Id
	 * @return xml with delAdapter message
	 * @since 2.4
	 */
	public static String createDelGate(String bt, String aid){
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "deletegate", bt);

			addTag(serializer, "gate",
					"id", aid);

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML of GetGates message
	 *
	 * @param bt userID of user
	 * @return GetGates message
	 * @since 2.2
	 */
	public static String createGetGates(String bt) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "getgates", bt);
			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML of GetGateInfo message
	 *
	 * @param bt BeeeOn token (active session)
	 * @return GetGateInfo message
	 * @since 2.5
	 */
	public static String createGetGateInfo(String bt, String gateId) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "getgateinfo", bt);

			addAttribute(serializer, "gateid", gateId);

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * New method create XML of SetDevs message with only one module in it. toSave parameter must by set properly.
	 *
	 * @param bt BeeeOn token (active session)
	 * @param gate to save
	 * @return SetGate message
	 * @since 2.5
	 */
	public static String createUpdateGate(String bt, Gate gate) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "updategate", bt);

			addAttribute(serializer, "gateid", gate.getId());

			addTag(serializer, "gate",
					"name", gate.getName(),
					"utc", String.valueOf(gate.getUtcOffset()));

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	// /////////////////////////////////////DEVICES,LOGS///////////////////////////////////////////////

	/**
	 * Method create XML for GateListen message
	 *
	 * @param bt  userID of user
	 * @param gateId id of actual gate
	 * @return XML of GateListen message
	 * @since 2.2
	 */
	public static String createGateScanMode(String bt, String gateId) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "scanmode", bt);

			addAttribute(serializer, "gateid", gateId);

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML for GetAllDevices message
	 *
	 * @param bt  userID of user
	 * @param gateId gateId of actual gate
	 * @return XML of GetAllDevices message
	 * @since 2.2
	 */
	public static String createGetAllDevices(String bt, String gateId) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "getalldevices", bt);

			addAttribute(serializer, "gateid", gateId);

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML for getting uninitialized devices
	 *
	 * @param bt  userID of user
	 * @param gateId gateId of actual gate
	 * @return XML of GetNewDevices message
	 * @since 2.2
	 */
	public static String createGetNewDevices(String bt, String gateId) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "getnewdevices", bt);

			addAttribute(serializer, "gateid", gateId);

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
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
			XmlSerializer serializer = beginXml(writer, "getdevices", bt);

			for (Device device : devices) {
				addTag(serializer, "device",
						"gateid", device.getGateId(),
						"id", device.getAddress());
			}

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML for GetLog message
	 *
	 * @param bt       userID of user
	 * @param gateId   gateId of actual gate
	 * @param deviceId deviceID of wanted module
	 * @param moduleId id of module
	 * @param from     date in unix timestamp
	 * @param to       date in unix timestamp
	 * @param funcType is aggregation function type {avg, median, ...}
	 * @param interval is time value in seconds that represents nicely e.g. month, week, day, 10 hours, 1 hour, ...
	 * @return GetLog message
	 * @since 2.2
	 */
	public static String createGetLog(String bt, String gateId, String deviceId, String moduleId, String from, String to, String funcType, int interval) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "getlogs", bt);

			addAttribute(serializer, "gateid", gateId);

			addTag(serializer, "logs",
					"from", from,
					"to", to,
					"ftype", funcType,
					"interval", String.valueOf(interval),
					"deviceid", deviceId,
					"moduleid", moduleId);

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML of SetDevs message. Almost all fields are optional
	 *
	 * @param bt      userID of user
	 * @param gateId     gateId of actual gate
	 * @param devices with changed fields
	 * @return UpdateDevice message
	 * @since 2.2
	 */
	public static String createUpdateDevice(String bt, String gateId, List<Device> devices) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "updatedevice", bt);

			addAttribute(serializer, "gateid", gateId);

			for (Device device : devices) {
				RefreshInterval refresh = device.getRefresh();

				addTag(serializer, "device",
						"init", device.isInitialized() ? "1" : "0",
						"id", device.getAddress(),
						"locationid", device.getLocationId(),
						"refresh", refresh != null ? Integer.toString(refresh.getInterval()) : null,
						"name", device.getCustomName());
			}

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML for Switch message
	 *
	 * @param bt     userID of user
	 * @param gateId    gateId of actual gate
	 * @param module to switch value
	 * @return XML of Switch message
	 * @since 2.2
	 */
	// FIXME: Use ModuleId instead
	public static String createSwitchState(String bt, String gateId, Module module) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "switchstate", bt);

			addAttribute(serializer, "gateid", gateId);

			addTag(serializer, "device",
					"id", module.getDevice().getId(),
					"moduleId", module.getId(),
					"value", String.valueOf(module.getValue().getDoubleValue()));

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
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
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "deletedevice", bt);

			addAttribute(serializer, "gateid", device.getGateId());

			addTag(serializer, "device",
					"id", device.getAddress());

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	// /////////////////////////////////////LOCATIONS//////////////////////////////////////////////////////

	/**
	 * Method create XML of AddRoom message
	 *
	 * @param bt       userID of user
	 * @param location to create
	 * @return created message
	 * @since 2.2
	 */
	public static String createAddLocation(String bt, Location location) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "addlocation", bt);
			addAttribute(serializer, "gateid", location.getGateId());

			addTag(serializer, "location",
					"type", location.getType(),
					"name", location.getName());

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML of SetRooms message
	 *
	 * @param bt        userID of user
	 * @param location  location to update
	 * @return message SetRooms
	 * @since 2.2
	 */
	public static String createUpdateLocation(String bt, Location location) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "updatelocation", bt);
			addAttribute(serializer, "gateid", location.getGateId());

			addTag(serializer, "location",
					"id", location.getId(),
					"type", location.getType(),
					"name", location.getName());

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML of DelRoom message
	 *
	 * @param bt  userID of user
	 * @param location location to delete
	 * @return DelRoom message
	 * @since 2.2
	 */
	public static String createDeleteLocation(String bt, Location location) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "deletelocation", bt);
			addAttribute(serializer, "gateid", location.getGateId());

			addTag(serializer, "location",
					"id", location.getId());

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML of GetRooms message
	 *
	 * @param bt  userID of user
	 * @param gateId gateId of actual gate
	 * @return message GetRooms
	 * @since 2.2
	 */
	public static String createGetLocations(String bt, String gateId) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "getlocations", bt);
			addAttribute(serializer, "gateid", gateId);

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	// /////////////////////////////////////PROVIDERS///////////////////////////////////////////////////

	/**
	 * Method create XML for AddAcount message
	 *
	 * @param bt    userID of user
	 * @param gateId   gateId of actual gate
	 * @param users map with User object and User.Role
	 * @return AddAcc message
	 * @since 2.2
	 */
	public static String createInviteGateUser(String bt, String gateId, ArrayList<User> users) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "invitegateuser", bt);

			addAttribute(serializer, "gateid", gateId);

			for (User user : users) {
				addTag(serializer, "user",
						"email", user.getEmail(),
						"role", user.getRole().getId());
			}

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML for SetAcount message
	 *
	 * @param bt    userID of user
	 * @param gateId   gateId of actual gate
	 * @param users map with User object and User.Role
	 * @return SetAcc message
	 * @since 2.2
	 */
	public static String createUpdateGateUser(String bt, String gateId, ArrayList<User> users) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "updategateuser", bt);

			addAttribute(serializer, "gateid", gateId);

			for (User user : users) {
				addTag(serializer, "user",
						"id", user.getId(),
						"role", user.getRole().getId());
			}

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML for DelAcc message
	 *
	 * @param bt    userID of user
	 * @param gateId   gateId of actual gate
	 * @param users list with Users
	 * @return dellAcc message
	 * @since 2.2
	 */
	public static String createDeleteGateUser(String bt, String gateId, List<User> users) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "deletegateuser", bt);
			serializer.attribute(ns, "gateid", gateId);

			for (User user : users) {
				addTag(serializer, "user",
						"id", user.getId());
			}

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML for GetAccs message
	 *
	 * @param bt  userID of user
	 * @param gateId gateId of actual gate
	 * @return GetAcc message
	 * @since 2.2
	 */
	public static String createGetGateUsers(String bt, String gateId) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "getgateusers", bt);
			serializer.attribute(ns, "gateid", gateId);
			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
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
	public static String createDelGcmid(String userId, String gcmid) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "deletegcmid", null);
			serializer.attribute(ns, "gcmid", gcmid);

			addTag(serializer, "user",
					"id", userId);

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
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
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "setgcmid", bt);
			serializer.attribute(ns, "gcmid", gcmid);
			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML of GetNotifs message
	 *
	 * @param bt SessionID of user
	 * @return message GetNotifs
	 * @since 2.2
	 */
	public static String createGetNotifications(String bt) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "getnotifications", bt);
			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
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
			XmlSerializer serializer = beginXml(writer, "notificationreaded", bt);

			for (String id : mids) {
				addTag(serializer, "notification",
						"id", id);
			}

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
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
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, "passborder", bt);
			serializer.attribute(ns, "rid", rid);
			serializer.attribute(ns, "type", type);
			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

}
