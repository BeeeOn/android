package com.rehivetech.beeeon.network.server.xml;

import android.support.annotation.Nullable;
import android.util.Xml;

import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ClientError;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.network.server.Network;

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

	public static class Request {
		public final StringWriter writer;
		public final XmlSerializer serializer;
		public final String namespace;
		public final String type;

		public Request(XmlSerializer serializer, String namespace, String type) {
			this.writer = new StringWriter();
			this.serializer = serializer;
			this.namespace = namespace;
			this.type = type;
		}

		protected static String endXml(Request req) throws IOException {
			req.serializer.text("");
			req.serializer.endTag(ns, "request");
			req.serializer.endDocument();

			return req.writer.toString();
		}

		protected static void startTag(Request req, String tag) throws IOException {
			req.serializer.startTag(ns, tag);
		}

		protected static void endTag(Request req, String tag) throws IOException {
			req.serializer.endTag(ns, tag);
		}

		protected static void addTag(Request req, String tag, String... attributes) throws IOException {
			startTag(req, tag);

			if (attributes.length % 2 != 0) { // odd
				throw new IllegalArgumentException("Invalid attributes count");
			}

			for (int i = 0; i < attributes.length; i += 2) { // take pair of args
				String name = attributes[i];
				String value = attributes[i + 1];

				if (value != null)
					req.serializer.attribute(ns, name, value);
			}

			endTag(req, tag);
		}

		protected static void addAttribute(Request req, String name, String value) throws IOException {
			req.serializer.attribute(ns, name, value);
		}
	}

	public static abstract class Base {

		protected static Request beginXml(String namespace, String type, @Nullable String sessionId) throws IOException {
			Request req = new Request(Xml.newSerializer(), namespace, type);

			req.serializer.setOutput(req.writer);
			req.serializer.startDocument("UTF-8", null);

			req.serializer.startTag(ns, "request");
			req.serializer.attribute(ns, "version", Network.PROTOCOL_VERSION); // every time use version
			req.serializer.attribute(ns, "ns", namespace);
			req.serializer.attribute(ns, "type", type);
			if (sessionId != null) {
				req.serializer.attribute(ns, "sessionid", sessionId);
			}

			return req;
		}

		protected static String endXml(Request req) throws IOException {
			req.serializer.text("");
			req.serializer.endTag(ns, "request");
			req.serializer.endDocument();

			return req.writer.toString();
		}

		protected static void startTag(Request req, String tag) throws IOException {
			req.serializer.startTag(ns, tag);
		}

		protected static void endTag(Request req, String tag) throws IOException {
			req.serializer.endTag(ns, tag);
		}

		protected static void addTag(Request req, String tag, String... attributes) throws IOException {
			startTag(req, tag);

			if (attributes.length % 2 != 0) { // odd
				throw new IllegalArgumentException("Invalid attributes count");
			}

			for (int i = 0; i < attributes.length; i += 2) { // take pair of args
				String name = attributes[i];
				String value = attributes[i + 1];

				if (value != null)
					req.serializer.attribute(ns, name, value);
			}

			endTag(req, tag);
		}

		protected static void addAttribute(Request req, String name, String value) throws IOException {
			req.serializer.attribute(ns, name, value);
		}
	}

	public static class Accounts extends Base {

		private static Request beginXml(String type, @Nullable String sessionId) throws IOException {
			return Base.beginXml("accounts", type, sessionId);
		}

		/**
		 * Method create message for registration of new user
		 *
		 * @param authProvider provider of authentication with parameters to send
		 * @return xml with signUp message
		 */
		public static String register(IAuthProvider authProvider) {
			try {
				Request req = beginXml("register", null);

				startTag(req, "provider");
				addAttribute(req, "name", authProvider.getProviderName());

				for (Map.Entry<String, String> entry : authProvider.getParameters().entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();

					if (key != null && value != null)
						addAttribute(req, key, value);
				}
				endTag(req, "provider");

				return endXml(req);
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
		public static String login(String phone, IAuthProvider authProvider) {
			try {
				Request req = beginXml("login", null);

				startTag(req, "provider");
				addAttribute(req, "name", authProvider.getProviderName());

				for (Map.Entry<String, String> entry : authProvider.getParameters().entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();

					if (key != null && value != null)
						addAttribute(req, key, value);
				}
				endTag(req, "provider");

				addTag(req, "phone", "name", phone);

				return endXml(req);
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
		public static String getMyProfile(String bt) {
			try {
				Request req = beginXml("getmyprofile", bt);
				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

		/**
		 * Method create message for loging out user
		 *
		 * @param bt beeeon Token (session Id)
		 * @return xml with logout message
		 * @since 2.5
		 */
		public static String logout(String bt) {
			try {
				Request req = beginXml("logout", bt);
				return endXml(req);
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
		public static String connectAuthProvider(String bt, IAuthProvider authProvider) {
			try {
				Request req = beginXml("connectauthprovider", bt);

				startTag(req, "provider");
				addAttribute(req, "name", authProvider.getProviderName());

				for (Map.Entry<String, String> entry : authProvider.getParameters().entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();

					if (key != null && value != null)
						addAttribute(req, key, value);
				}
				endTag(req, "provider");

				return endXml(req);
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
		public static String disconnectAuthProvider(String bt, String providerName) {
			try {
				Request req = beginXml("disconnectAuthProvider", bt);

				addTag(req, "provider",
						"name", providerName);

				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}
	}

	public static class Devices extends Base {

		private static Request beginXml(String type, @Nullable String sessionId) throws IOException {
			return Base.beginXml("devices", type, sessionId);
		}

		/**
		 * Method create XML for GetAllDevices message
		 *
		 * @param bt     userID of user
		 * @param gateId gateId of actual gate
		 * @return XML of GetAllDevices message
		 * @since 2.2
		 */
		public static String getAll(String bt, String gateId) {
			try {
				Request req = beginXml("getall", bt);
				addAttribute(req, "gateid", gateId);

				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

		/**
		 * Method create XML for getting uninitialized devices
		 *
		 * @param bt     userID of user
		 * @param gateId gateId of actual gate
		 * @return XML of GetNewDevices message
		 * @since 2.2
		 */
		public static String getNew(String bt, String gateId) {
			try {
				Request req = beginXml("getnew", bt);
				addAttribute(req, "gateid", gateId);

				return endXml(req);
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
		public static String get(String bt, List<Device> devices) {
			if (devices.size() < 1)
				throw new IllegalArgumentException("Expected more than zero devices");

			try {
				Request req = beginXml("get", bt);

				for (Device device : devices) {
					addTag(req, "device",
							"gateid", device.getGateId(),
							"euid", device.getAddress());
				}

				return endXml(req);
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
		public static String getLog(String bt, String gateId, String deviceId, String moduleId, String from, String to, String funcType, int interval) {
			try {
				Request req = beginXml("getlog", bt);
				addAttribute(req, "gateid", gateId);

				addTag(req, "logs",
						"from", from,
						"to", to,
						"ftype", funcType,
						"interval", String.valueOf(interval),
						"deviceeuid", deviceId,
						"moduleid", moduleId);

				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

		/**
		 * Method create XML of SetDevs message. Almost all fields are optional
		 *
		 * @param bt      userID of user
		 * @param gateId  gateId of actual gate
		 * @param devices with changed fields
		 * @return UpdateDevice message
		 * @since 2.2
		 */
		public static String update(String bt, String gateId, List<Device> devices) {
			try {
				Request req = beginXml("update", bt);
				addAttribute(req, "gateid", gateId);

				for (Device device : devices) {
					RefreshInterval refresh = device.getRefresh();

					addTag(req, "device",
							"euid", device.getAddress(),
							"locationid", device.getLocationId(),
							"refresh", refresh != null ? Integer.toString(refresh.getInterval()) : null, // FIXME: Remove this, it must be saved as actor switching
							"name", device.getCustomName());
				}

				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

		/**
		 * Method create XML for Switch message
		 *
		 * @param bt     userID of user
		 * @param gateId gateId of actual gate
		 * @param module to switch value
		 * @return XML of Switch message
		 * @since 2.2
		 */
		// FIXME: Use ModuleId instead
		public static String setState(String bt, String gateId, Module module) {
			try {
				Request req = beginXml("setstate", bt);
				addAttribute(req, "gateid", gateId);

				addTag(req, "device",
						"euid", module.getDevice().getId(),
						"moduleid", module.getId(),
						"value", String.valueOf(module.getValue().getDoubleValue()));

				return endXml(req);
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
		public static String unregister(String bt, Device device) {
			try {
				Request req = beginXml("unregister", bt);
				addAttribute(req, "gateid", device.getGateId());

				addTag(req, "device",
						"euid", device.getAddress());

				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}
	}

	public static class Gates extends Base {

		private static Request beginXml(String type, @Nullable String sessionId) throws IOException {
			return Base.beginXml("gates", type, sessionId);
		}

		/**
		 * Method create XML for AddGate message
		 *
		 * @param bt              userID of user
		 * @param aid             gateId of actual gate
		 * @param gateName        name of gate
		 * @param offsetInMinutes timezone of gate as offset in minutes
		 * @return AddGate message
		 * @since 2.2
		 */
		public static String register(String bt, String aid, String gateName, int offsetInMinutes) {
			try {
				Request req = beginXml("register", bt);

				addTag(req, "gate",
						"id", aid,
						"name", gateName,
						"timezone", String.valueOf(offsetInMinutes));

				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

		/**
		 * Method create message for removing actual user from adapter
		 *
		 * @param bt  beeeon Token (session Id)
		 * @param aid gate Id
		 * @return xml with delAdapter message
		 * @since 2.4
		 */
		public static String unregister(String bt, String aid) {
			try {
				Request req = beginXml("unregister", bt);

				addTag(req, "gate",
						"id", aid);

				return endXml(req);
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
		public static String getAll(String bt) {
			try {
				Request req = beginXml("getall", bt);
				return endXml(req);
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
		public static String get(String bt, String gateId) {
			try {
				Request req = beginXml("get", bt);

				addTag(req, "gate",
						"id", gateId);

				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

		/**
		 * New method create XML of SetDevs message with only one module in it. toSave parameter must by set properly.
		 *
		 * @param bt   BeeeOn token (active session)
		 * @param gate to save
		 * @return SetGate message
		 * @since 2.5
		 */
		public static String update(String bt, Gate gate) {
			try {
				Request req = beginXml("update", bt);

				addTag(req, "gate",
						"id", gate.getId(),
						"name", gate.getName(),
						"timezone", String.valueOf(gate.getUtcOffset()));

				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

		/**
		 * Method create XML for GateListen message
		 *
		 * @param bt     userID of user
		 * @param gateId id of actual gate
		 * @return XML of GateListen message
		 * @since 2.2
		 */
		public static String startListen(String bt, String gateId) {
			try {
				Request req = beginXml("startlisten", bt);

				addTag(req, "gate",
						"id", gateId);

				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}
	}

	public static class GateUsers extends Base {

		private static Request beginXml(String type, @Nullable String sessionId) throws IOException {
			return Base.beginXml("gateusers", type, sessionId);
		}

		/**
		 * Method create XML for AddAcount message
		 *
		 * @param bt     userID of user
		 * @param gateId gateId of actual gate
		 * @param users  map with User object and User.Role
		 * @return AddAcc message
		 * @since 2.2
		 */
		public static String invite(String bt, String gateId, ArrayList<User> users) {
			try {
				Request req = beginXml("invite", bt);
				addAttribute(req, "gateid", gateId);

				for (User user : users) {
					addTag(req, "user",
							"email", user.getEmail(),
							"permission", user.getRole().getId());
				}

				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

		/**
		 * Method create XML for SetAcount message
		 *
		 * @param bt     userID of user
		 * @param gateId gateId of actual gate
		 * @param users  map with User object and User.Role
		 * @return SetAcc message
		 * @since 2.2
		 */
		public static String updateAccess(String bt, String gateId, ArrayList<User> users) {
			try {
				Request req = beginXml("updateaccess", bt);
				addAttribute(req, "gateid", gateId);

				for (User user : users) {
					addTag(req, "user",
							"id", user.getId(),
							"permission", user.getRole().getId());
				}

				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

		/**
		 * Method create XML for DelAcc message
		 *
		 * @param bt     userID of user
		 * @param gateId gateId of actual gate
		 * @param users  list with Users
		 * @return dellAcc message
		 * @since 2.2
		 */
		public static String remove(String bt, String gateId, List<User> users) {
			try {
				Request req = beginXml("remove", bt);
				addAttribute(req, "gateid", gateId);

				for (User user : users) {
					addTag(req, "user",
							"id", user.getId());
				}

				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

		/**
		 * Method create XML for GetAccs message
		 *
		 * @param bt     userID of user
		 * @param gateId gateId of actual gate
		 * @return GetAcc message
		 * @since 2.2
		 */
		public static String getAll(String bt, String gateId) {
			try {
				Request req = beginXml("getall", bt);
				addAttribute(req, "gateid", gateId);

				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}
	}

	public static class Locations extends Base {

		private static Request beginXml(String type, @Nullable String sessionId) throws IOException {
			return Base.beginXml("locations", type, sessionId);
		}

		/**
		 * Method create XML of AddRoom message
		 *
		 * @param bt       userID of user
		 * @param location to create
		 * @return created message
		 * @since 2.2
		 */
		public static String create(String bt, Location location) {
			try {
				Request req = beginXml("create", bt);
				addAttribute(req, "gateid", location.getGateId());

				addTag(req, "location",
						"type", location.getType(),
						"name", location.getName());

				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

		/**
		 * Method create XML of SetRooms message
		 *
		 * @param bt       userID of user
		 * @param location location to update
		 * @return message SetRooms
		 * @since 2.2
		 */
		public static String update(String bt, Location location) {
			try {
				Request req = beginXml("update", bt);
				addAttribute(req, "gateid", location.getGateId());

				addTag(req, "location",
						"id", location.getId(),
						"type", location.getType(),
						"name", location.getName());

				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

		/**
		 * Method create XML of DelRoom message
		 *
		 * @param bt       userID of user
		 * @param location location to delete
		 * @return DelRoom message
		 * @since 2.2
		 */
		public static String delete(String bt, Location location) {
			try {
				Request req = beginXml("delete", bt);
				addAttribute(req, "gateid", location.getGateId());

				addTag(req, "location",
						"id", location.getId());

				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

		/**
		 * Method create XML of GetRooms message
		 *
		 * @param bt     userID of user
		 * @param gateId gateId of actual gate
		 * @return message GetRooms
		 * @since 2.2
		 */
		public static String getAll(String bt, String gateId) {
			try {
				Request req = beginXml("getall", bt);
				addAttribute(req, "gateid", gateId);

				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

	}

	public static class Notifications extends Base {

		private static Request beginXml(String type, @Nullable String sessionId) throws IOException {
			return Base.beginXml("notifications", type, sessionId);
		}

		/**
		 * Method create XML of DelXconstants.GCMID message (delete google cloud message id)
		 *
		 * @param userId of last logged user
		 * @param gcmid  id of google messaging
		 * @return message GCMID
		 * @since 2.2
		 */
		public static String deleteGCMID(String userId, String gcmid) {
			try {
				Request req = beginXml("unregisterservice", null);

				addTag(req, "service",
						"name", "gcm",
						"id", gcmid,
						"userid", userId);

				return endXml(req);
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
		public static String setGCMID(String bt, String gcmid) {
			try {
				Request req = beginXml("registerservice", bt);

				addTag(req, "service",
						"name", "gcm",
						"id", gcmid);

				return endXml(req);
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
		public static String getLatest(String bt) {
			try {
				Request req = beginXml("getlatest", bt);
				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

		/**
		 * Method create XML of NotifRead message
		 *
		 * @param bt  userID of user
		 * @param ids list of gcmID of read notification
		 * @return message NotifRead
		 * @since 2.2
		 */
		public static String read(String bt, List<String> ids) {
			try {
				Request req = beginXml("read", bt);

				for (String id : ids) {
					addTag(req, "notification",
							"id", id);
				}

				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

		public static String delete(String bt, List<String> ids) {
			try {
				Request req = beginXml("delete", bt);

				for (String id : ids) {
					addTag(req, "notification",
							"id", id);
				}

				return endXml(req);
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

	}

}
