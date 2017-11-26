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
import com.rehivetech.beeeon.util.GpsData;

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
		private final StringWriter mWriter;
		private final XmlSerializer mSerializer;
		public final String namespace;
		public final String type;
		public final XmlParser.Result expectedResult;

		private String mMessage;

		private Request(XmlSerializer serializer, String namespace, String type, XmlParser.Result expectedResult) {
			this.mWriter = new StringWriter();
			this.mSerializer = serializer;
			this.namespace = namespace;
			this.type = type;
			this.expectedResult = expectedResult;
		}

		/**
		 * @return Created request message (string containing xml request) or null when request is not ready yet (was not called {@link #endXml()} method on it)
		 */
		@Nullable
		public String getMessage() {
			return mMessage;
		}

		protected static Request beginXml(String namespace, String type, @Nullable String sessionId, XmlParser.Result expectedResult) throws IOException {
			Request req = new Request(Xml.newSerializer(), namespace, type, expectedResult);

			req.mSerializer.setOutput(req.mWriter);
			req.mSerializer.startDocument("UTF-8", null);

			req.mSerializer.startTag(ns, "request");
			req.mSerializer.attribute(ns, "version", Network.PROTOCOL_VERSION); // every time use version
			req.mSerializer.attribute(ns, "ns", namespace);
			req.mSerializer.attribute(ns, "type", type);
			if (sessionId != null) {
				req.mSerializer.attribute(ns, "sessionid", sessionId);
			}

			return req;
		}

		protected Request endXml() throws IOException {
			mSerializer.text("");
			mSerializer.endTag(ns, "request");
			mSerializer.endDocument();

			// Create the data
			mMessage = mWriter.toString();
			return this;
		}

		protected void startTag(String tag) throws IOException {
			mSerializer.startTag(ns, tag);
		}

		protected void endTag(String tag) throws IOException {
			mSerializer.endTag(ns, tag);
		}

		protected void addTag(String tag, String... attributes) throws IOException {
			startTag(tag);

			if (attributes.length % 2 != 0) { // odd
				throw new IllegalArgumentException("Invalid attributes count");
			}

			for (int i = 0; i < attributes.length; i += 2) { // take pair of args
				String name = attributes[i];
				String value = attributes[i + 1];

				if (value != null)
					mSerializer.attribute(ns, name, value);
			}

			endTag(tag);
		}

		protected void addAttribute(String name, String value) throws IOException {
			mSerializer.attribute(ns, name, value);
		}
	}

	public static class Accounts {

		private static Request beginXml(String type, @Nullable String sessionId, XmlParser.Result expectedResult) throws IOException {
			return Request.beginXml("accounts", type, sessionId, expectedResult);
		}

		/**
		 * Method create message for registration of new user
		 *
		 * @param authProvider provider of authentication with parameters to send
		 * @return xml with signUp message
		 */
		public static Request register(IAuthProvider authProvider) {
			try {
				Request req = beginXml("register", null, XmlParser.Result.OK);

				req.startTag("provider");
				req.addAttribute("name", authProvider.getProviderName());

				for (Map.Entry<String, String> entry : authProvider.getParameters().entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();

					if (key != null && value != null)
						req.addAttribute(key, value);
				}
				req.endTag("provider");

				return req.endXml();
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
		public static Request login(String phone, IAuthProvider authProvider) {
			try {
				Request req = beginXml("login", null, XmlParser.Result.DATA);

				req.startTag("provider");
				req.addAttribute("name", authProvider.getProviderName());

				for (Map.Entry<String, String> entry : authProvider.getParameters().entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();

					if (key != null && value != null)
						req.addAttribute(key, value);
				}
				req.endTag("provider");

				req.addTag("phone", "name", phone);

				return req.endXml();
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
		public static Request getMyProfile(String bt) {
			try {
				Request req = beginXml("getmyprofile", bt, XmlParser.Result.DATA);
				return req.endXml();
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
		public static Request logout(String bt) {
			try {
				Request req = beginXml("logout", bt, XmlParser.Result.OK);
				return req.endXml();
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
		public static Request connectAuthProvider(String bt, IAuthProvider authProvider) {
			try {
				Request req = beginXml("connectauthprovider", bt, XmlParser.Result.OK);

				req.startTag("provider");
				req.addAttribute("name", authProvider.getProviderName());

				for (Map.Entry<String, String> entry : authProvider.getParameters().entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();

					if (key != null && value != null)
						req.addAttribute(key, value);
				}
				req.endTag("provider");

				return req.endXml();
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
		public static Request disconnectAuthProvider(String bt, String providerName) {
			try {
				Request req = beginXml("disconnectAuthProvider", bt, XmlParser.Result.OK);

				req.addTag("provider",
						"name", providerName);

				return req.endXml();
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}
	}

	public static class Devices {

		private static Request beginXml(String type, @Nullable String sessionId, XmlParser.Result expectedResult) throws IOException {
			return Request.beginXml("devices", type, sessionId, expectedResult);
		}

		/**
		 * Method create XML for GetAllDevices message
		 *
		 * @param bt     userID of user
		 * @param gateId gateId of actual gate
		 * @return XML of GetAllDevices message
		 * @since 2.2
		 */
		public static Request getAll(String bt, String gateId) {
			try {
				Request req = beginXml("getall", bt, XmlParser.Result.DATA);
				req.addAttribute("gateid", gateId);

				return req.endXml();
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
		public static Request getNew(String bt, String gateId) {
			try {
				Request req = beginXml("getnew", bt, XmlParser.Result.DATA);
				req.addAttribute("gateid", gateId);

				return req.endXml();
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
		public static Request get(String bt, List<Device> devices) {
			if (devices.size() < 1)
				throw new IllegalArgumentException("Expected more than zero devices");

			try {
				Request req = beginXml("get", bt, XmlParser.Result.DATA);

				for (Device device : devices) {
					req.addTag("device",
							"gateid", device.getGateId(),
							"euid", device.getAddress());
				}

				return req.endXml();
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
		public static Request getLog(String bt, String gateId, String deviceId, String moduleId, String from, String to, String funcType, int interval) {
			try {
				Request req = beginXml("getlog", bt, XmlParser.Result.DATA);
				req.addAttribute("gateid", gateId);

				req.addTag("logs",
						"from", from,
						"to", to,
						"ftype", funcType,
						"interval", String.valueOf(interval),
						"deviceeuid", deviceId,
						"moduleid", moduleId);

				return req.endXml();
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
		public static Request update(String bt, String gateId, List<Device> devices) {
			try {
				Request req = beginXml("update", bt, XmlParser.Result.OK);
				req.addAttribute("gateid", gateId);

				for (Device device : devices) {
					RefreshInterval refresh = device.getRefresh();

					req.addTag("device",
							"euid", device.getAddress(),
							"locationid", device.getLocationId(),
							"refresh", refresh != null ? Integer.toString(refresh.getInterval()) : null, // FIXME: Remove this, it must be saved as actor switching
							"name", device.getCustomName());
				}

				return req.endXml();
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
		public static Request setState(String bt, String gateId, Module module) {
			try {
				Request req = beginXml("setstate", bt, XmlParser.Result.OK);
				req.addAttribute("gateid", gateId);

				req.addTag("device",
						"euid", module.getDevice().getId(),
						"moduleid", module.getId(),
						"value", String.valueOf(module.getValue().getDoubleValue()));

				return req.endXml();
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
		public static Request unregister(String bt, Device device) {
			try {
				Request req = beginXml("unregister", bt, XmlParser.Result.OK);
				req.addAttribute("gateid", device.getGateId());

				req.addTag("device",
						"euid", device.getAddress());

				return req.endXml();
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

		/**
		 * Sends creating device parameter
		 *
		 * @param sessionId userId
		 * @param device    specified device
		 * @param key       parameter key
		 * @param value     parameter value
		 * @return
		 */
		public static Request createParameter(String sessionId, Device device, String key, String value) {
			try {
				Request req = beginXml("createparameter", sessionId, XmlParser.Result.OK);
				req.addAttribute("gateid", device.getGateId());

				req.addTag("device",
						"euid", device.getAddress(),
						"parameterkey", key,
						"parametervalue", value
				);

				return req.endXml();
			} catch (IOException e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

		/**
		 * Sends getting device parameter
		 *
		 * @param sessionId userId
		 * @param device    specified device
		 * @param key       parameter key
		 * @return
		 */
		public static Request getParameter(String sessionId, Device device, String key) {
			try {
				Request req = beginXml("createParameter", sessionId, XmlParser.Result.DATA);
				req.addAttribute("gateid", device.getGateId());

				req.addTag("device",
						"euid", device.getAddress(),
						"parameterkey", key
				);

				return req.endXml();
			} catch (IOException e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

	}

	public static class Gates {

		private static Request beginXml(String type, @Nullable String sessionId, XmlParser.Result expectedResult) throws IOException {
			return Request.beginXml("gates", type, sessionId, expectedResult);
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
		public static Request register(String bt, String aid, String gateName, int offsetInMinutes) {
			try {
				Request req = beginXml("register", bt, XmlParser.Result.OK);

				req.addTag("gate",
						"id", aid,
						"name", gateName,
						"timezone", String.valueOf(offsetInMinutes));

				return req.endXml();
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
		public static Request unregister(String bt, String aid) {
			try {
				Request req = beginXml("unregister", bt, XmlParser.Result.OK);

				req.addTag("gate",
						"id", aid);

				return req.endXml();
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
		public static Request getAll(String bt) {
			try {
				Request req = beginXml("getall", bt, XmlParser.Result.DATA);
				return req.endXml();
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
		public static Request get(String bt, String gateId) {
			try {
				Request req = beginXml("get", bt, XmlParser.Result.DATA);

				req.addTag("gate",
						"id", gateId);

				return req.endXml();
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

		/**
		 * New method create XML of SetDevs message with only one module in it. toSave parameter must by set properly.
		 *
		 * @param bt      BeeeOn token (active session)
		 * @param gate    to save
		 * @param gpsData
		 * @return SetGate message
		 * @since 2.5
		 */
		public static Request update(String bt, Gate gate, GpsData gpsData) {
			try {
				Request req = beginXml("update", bt, XmlParser.Result.OK);

				req.addTag("gate",
						"id", gate.getId(),
						"name", gate.getName(),
						"timezone", String.valueOf(gate.getUtcOffset()),
						"altitude", String.valueOf(gpsData.getAltitude()),
						"longitude", String.valueOf(gpsData.getLongitude()),
						"latitude", String.valueOf(gpsData.getLatitude()));

				return req.endXml();
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
		public static Request startListen(String bt, String gateId) {
			try {
				Request req = beginXml("startlisten", bt, XmlParser.Result.OK);

				req.addTag("gate",
						"id", gateId);

				return req.endXml();
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

		public static Request search(String bt, String gateId, @Nullable String deviceIpAddress) {
			try {
				Request req = beginXml("search", bt, XmlParser.Result.OK);
				req.addTag("gate",
						"id", gateId,
						"deviceip", deviceIpAddress
				);

				return req.endXml();
			} catch (IOException e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}
	}

	public static class GateUsers {

		private static Request beginXml(String type, @Nullable String sessionId, XmlParser.Result expectedResult) throws IOException {
			return Request.beginXml("gateusers", type, sessionId, expectedResult);
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
		public static Request invite(String bt, String gateId, ArrayList<User> users) {
			try {
				Request req = beginXml("invite", bt, XmlParser.Result.OK);
				req.addAttribute("gateid", gateId);

				for (User user : users) {
					req.addTag("user",
							"email", user.getEmail(),
							"permission", user.getRole().getId());
				}

				return req.endXml();
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
		public static Request updateAccess(String bt, String gateId, ArrayList<User> users) {
			try {
				Request req = beginXml("updateaccess", bt, XmlParser.Result.OK);
				req.addAttribute("gateid", gateId);

				for (User user : users) {
					req.addTag("user",
							"id", user.getId(),
							"permission", user.getRole().getId());
				}

				return req.endXml();
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
		public static Request remove(String bt, String gateId, List<User> users) {
			try {
				Request req = beginXml("remove", bt, XmlParser.Result.OK);
				req.addAttribute("gateid", gateId);

				for (User user : users) {
					req.addTag("user",
							"id", user.getId());
				}

				return req.endXml();
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
		public static Request getAll(String bt, String gateId) {
			try {
				Request req = beginXml("getall", bt, XmlParser.Result.DATA);
				req.addAttribute("gateid", gateId);

				return req.endXml();
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}
	}

	public static class Locations {

		private static Request beginXml(String type, @Nullable String sessionId, XmlParser.Result expectedResult) throws IOException {
			return Request.beginXml("locations", type, sessionId, expectedResult);
		}

		/**
		 * Method create XML of AddRoom message
		 *
		 * @param bt       userID of user
		 * @param location to create
		 * @return created message
		 * @since 2.2
		 */
		public static Request create(String bt, Location location) {
			try {
				Request req = beginXml("create", bt, XmlParser.Result.DATA);
				req.addAttribute("gateid", location.getGateId());

				req.addTag("location",
						"type", location.getType(),
						"name", location.getName());

				return req.endXml();
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
		public static Request update(String bt, Location location) {
			try {
				Request req = beginXml("update", bt, XmlParser.Result.OK);
				req.addAttribute("gateid", location.getGateId());

				req.addTag("location",
						"id", location.getId(),
						"type", location.getType(),
						"name", location.getName());

				return req.endXml();
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
		public static Request delete(String bt, Location location) {
			try {
				Request req = beginXml("delete", bt, XmlParser.Result.OK);
				req.addAttribute("gateid", location.getGateId());

				req.addTag("location",
						"id", location.getId());

				return req.endXml();
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
		public static Request getAll(String bt, String gateId) {
			try {
				Request req = beginXml("getall", bt, XmlParser.Result.DATA);
				req.addAttribute("gateid", gateId);

				return req.endXml();
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

	}

	public static class Notifications {

		private static Request beginXml(String type, @Nullable String sessionId, XmlParser.Result expectedResult) throws IOException {
			return Request.beginXml("notifications", type, sessionId, expectedResult);
		}

		/**
		 * Method create XML of DelXconstants.GCMID message (delete google cloud message id)
		 *
		 * @param userId of last logged user
		 * @param gcmid  id of google messaging
		 * @return message GCMID
		 * @since 2.2
		 */
		public static Request deleteGCMID(String bt, String userId, String gcmid) {
			try {
				Request req = beginXml("unregister", bt, XmlParser.Result.OK);

				req.addTag("service",
						"name", "fcm",
						"id", gcmid,
						"userid", userId);

				return req.endXml();
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
		public static Request setGCMID(String bt, String gcmid) {
			try {
				Request req = beginXml("register", bt, XmlParser.Result.OK);

				req.addTag("service",
						"name", "fcm",
						"id", gcmid);

				return req.endXml();
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
		public static Request getLatest(String bt) {
			try {
				Request req = beginXml("getlatest", bt, XmlParser.Result.DATA);
				return req.endXml();
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
		public static Request read(String bt, List<String> ids) {
			try {
				Request req = beginXml("read", bt, XmlParser.Result.OK);

				for (String id : ids) {
					req.addTag("notification",
							"id", id);
				}

				return req.endXml();
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

		public static Request delete(String bt, List<String> ids) {
			try {
				Request req = beginXml("delete", bt, XmlParser.Result.OK);

				for (String id : ids) {
					req.addTag("notification",
							"id", id);
				}

				return req.endXml();
			} catch (Exception e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		}

	}

}
