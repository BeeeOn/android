package com.rehivetech.beeeon.network.xml;

import android.util.Xml;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ClientError;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.Module.SaveModule;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.gate.Gate;
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

	protected static final String ns = null;

	// states

	public static final String STATE_LOGIN = "login";
	public static final String STATE_REGISTER = "register";
	public static final String STATE_LOGOUT = "logout";

	public static final String STATE_USER_UPDATE = "updateuser";
	public static final String STATE_USER_GETINFO = "getuserinfo";
	public static final String STATE_USER_ACCOUNT_CONNECT = "connectauthprovider";
	public static final String STATE_USER_ACCOUNT_DISCONNECT = "disconnectauthprovider";

	public static final String STATE_GATE_ADD = "addgate";
	public static final String STATE_GATE_DELETE = "deletegate";
	public static final String STATE_GATE_GETINFO = "getgateinfo";
	public static final String STATE_GATE_GETALL = "getgates";
	public static final String STATE_GATE_UPDATE = "updategate";

	public static final String STATE_SCANMODE = "scanmode";

	public static final String STATE_DEVICE_GETALL = "getalldevices";
	public static final String STATE_DEVICE_GETSOME = "getdevices";
	public static final String STATE_DEVICE_GETNEW = "getnewdevices";
	public static final String STATE_DEVICE_UPDATE = "updatedevice";
	public static final String STATE_DEVICE_DELETE = "deletedevice";

	public static final String STATE_GETLOGS = "getlogs";

	public static final String STATE_SWITCH = "switchstate";

	public static final String STATE_LOCATION_ADD = "addlocation";
	public static final String STATE_LOCATION_UPDATE = "updatelocation";
	public static final String STATE_LOCATION_DELETE = "deletelocation";
	public static final String STATE_LOCATION_GETALL = "getlocations";

	public static final String STATE_GATE_USER_INVITE = "invitegateuser";
	public static final String STATE_GATE_USER_UPDATE = "updategateuser";
	public static final String STATE_GATE_USER_DELETE = "deletegateuser";
	public static final String STATE_GATE_USER_GETALL = "getgateusers";


	public static final String STATE_GCMID_DELETE = "deletegcmid";
	public static final String STATE_GCMID_UPDATE = "setgcmid";

	public static final String STATE_NOTIFICATION_GETALL = "getnotifications";
	public static final String STATE_NOTIFICATION_READ = "notificationreaded";

	public static final String STATE_ADDALG = "addalg";
	public static final String STATE_GETALLALGS = "getallalgs";
	public static final String STATE_GETALGS = "getlags";
	public static final String STATE_SETALG = "setalg";
	public static final String STATE_DELALG = "delalg";

	public static final String STATE_PASSBORDER = "passborder";

	// end of states

	protected static XmlSerializer beginXml(StringWriter writer, String state) throws IOException {
		XmlSerializer serializer = Xml.newSerializer();

		serializer.setOutput(writer);
		serializer.startDocument("UTF-8", null);

		serializer.startTag(ns, Xconstants.COM_ROOT);
		serializer.attribute(ns, Xconstants.COM_VERSION, Constants.PROTOCOL_VERSION); // every time use version
		serializer.attribute(ns, Xconstants.COM_STATE, state);

		return serializer;
	}

	protected static String endXml(StringWriter writer, XmlSerializer serializer) throws IOException {
		serializer.text("");
		serializer.endTag(ns, Xconstants.COM_ROOT);
		serializer.endDocument();

		return writer.toString();
	}

	// /////////////////////////////////////SIGNIN,SIGNUP,GATES/////////////////////////////////////

	/**
	 * Method create message for registration of new user
	 *
	 * @param authProvider provider of authentication with parameters to send
	 * @return xml with signUp message
	 */
	public static String createSignUp(IAuthProvider authProvider) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, STATE_REGISTER);

			serializer.startTag(ns, Xconstants.PROVIDER_ROOT);
				serializer.attribute(ns, Xconstants.PROVIDER_NAME, authProvider.getProviderName());

				for (Map.Entry<String, String> entry : authProvider.getParameters().entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();

					if (key != null && value != null)
						serializer.attribute(ns, key, value);
				}
			serializer.endTag(ns, Xconstants.PROVIDER_ROOT);

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
	public static String createSignIn(String locale, String phone, IAuthProvider authProvider) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, STATE_LOGIN);

			serializer.startTag(ns, Xconstants.PROVIDER_ROOT);
				serializer.attribute(ns, Xconstants.PROVIDER_NAME, authProvider.getProviderName());

				for (Map.Entry<String, String> entry : authProvider.getParameters().entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();

					if (key != null && value != null)
						serializer.attribute(ns, key, value);
				}
			serializer.endTag(ns, Xconstants.PROVIDER_ROOT);

			serializer.startTag(ns, Xconstants.PHONE_ROOT);
				serializer.attribute(ns, Xconstants.PHONE_NAME, phone);
			serializer.endTag(ns, Xconstants.PHONE_ROOT);

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
			XmlSerializer serializer = beginXml(writer, STATE_USER_ACCOUNT_CONNECT);

			serializer.attribute(ns, Xconstants.COM_SESSION_ID, bt);
			serializer.attribute(ns, Xconstants.PROVIDER_NAME, authProvider.getProviderName());

			serializer.startTag(ns, Xconstants.PARAM);
			for (Map.Entry<String, String> entry : authProvider.getParameters().entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();

				if (key != null && value != null)
					serializer.attribute(ns, key, value);
			}
			serializer.endTag(ns, Xconstants.PARAM);

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
		return createComAttribsVariant(Xconstants.COM_STATE, STATE_USER_ACCOUNT_DISCONNECT, Xconstants.COM_SESSION_ID, bt, Xconstants.PROVIDER_NAME, providerName);
	}

	/**
	 * Method create message for loging out user
	 * @param bt beeeon Token (session Id)
	 * @return xml with logout message
	 * @since 2.5
	 */
	public static String createLogout(String bt){
		return createComAttribsVariant(Xconstants.COM_STATE, STATE_LOGOUT, Xconstants.COM_SESSION_ID, bt);
	}

	/**
	 * Method create message for obtain information about user
	 *
	 * @param bt beeeon Token (session Id)
	 * @return xml with getUserInfo message
	 */
	public static String createGetUserInfo(String bt) {
		return createComAttribsVariant(Xconstants.COM_STATE, STATE_USER_GETINFO, Xconstants.COM_SESSION_ID, bt);
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
		return createComAttribsVariant(Xconstants.COM_STATE, STATE_GATE_ADD, Xconstants.COM_SESSION_ID, bt, Xconstants.AID, aid, Xconstants.ANAME, gateName);
	}

	/**
	 * Method create XML of GetGates message
	 *
	 * @param bt userID of user
	 * @return GetGates message
	 * @since 2.2
	 */
	public static String createGetGates(String bt) {
		return createComAttribsVariant(Xconstants.COM_STATE, STATE_GATE_GETALL, Xconstants.COM_SESSION_ID, bt);
	}

	/**
	 * Method create XML of GetGateInfo message
	 *
	 * @param bt BeeeOn token (active session)
	 * @return GetGateInfo message
	 * @since 2.5
	 */
	public static String createGetGateInfo(String bt, String gateId) {
		return createComAttribsVariant(
				Xconstants.COM_STATE, STATE_GATE_GETINFO,
				Xconstants.COM_SESSION_ID, bt,
				Xconstants.AID, gateId);
	}

	/**
	 * Method create message for removing actual user from adapter
	 * @param bt beeeon Token (session Id)
	 * @param aid gate Id
	 * @return xml with delAdapter message
	 * @since 2.4
	 */
	public static String createDelGate(String bt, String aid){
		return createComAttribsVariant(Xconstants.COM_STATE, STATE_GATE_DELETE, Xconstants.COM_SESSION_ID, bt, Xconstants.AID, aid);
	}

	/**
	 * New method create XML of SetDevs message with only one module in it. toSave parameter must by set properly.
	 *
	 * @param bt BeeeOn token (active session)
	 * @param gate to save
	 * @return SetGate message
	 * @since 2.5
	 */
	public static String createSetGate(String bt, Gate gate) {
		return createComAttribsVariant(
				Xconstants.COM_STATE, STATE_GATE_UPDATE,
				Xconstants.COM_SESSION_ID, bt,
				Xconstants.AID, gate.getId(),
				Xconstants.ANAME, gate.getName(),
				Xconstants.UTC, String.valueOf(gate.getUtcOffset()));
	}

	// /////////////////////////////////////DEVICES,LOGS///////////////////////////////////////////////

	/**
	 * Method create XML for GateListen message
	 *
	 * @param bt  userID of user
	 * @param aid gateId of actual gate
	 * @return XML of GateListen message
	 * @since 2.2
	 */
	public static String createGateScanMode(String bt, String aid) {
		return createComAttribsVariant(Xconstants.COM_STATE, STATE_SCANMODE, Xconstants.COM_SESSION_ID, bt, Xconstants.AID, aid);
	}

	/**
	 * Method create XML for GetAllDevices message
	 *
	 * @param bt  userID of user
	 * @param aid gateId of actual gate
	 * @return XML of GetAllDevices message
	 * @since 2.2
	 */
	public static String createGetAllDevices(String bt, String aid) {
		return createComAttribsVariant(Xconstants.COM_STATE, STATE_DEVICE_GETALL, Xconstants.COM_SESSION_ID, bt, Xconstants.AID, aid);
	}

	/**
	 * Method create XML for getting uninitialized devices
	 *
	 * @param bt  userID of user
	 * @param aid gateId of actual gate
	 * @return XML of GetNewDevices message
	 * @since 2.2
	 */
	public static String createGetNewDevices(String bt, String aid) {
		return createComAttribsVariant(Xconstants.COM_STATE, STATE_DEVICE_GETNEW, Xconstants.COM_SESSION_ID, bt, Xconstants.AID, aid);
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
			XmlSerializer serializer = beginXml(writer, STATE_DEVICE_GETSOME);

			serializer.attribute(ns, Xconstants.COM_SESSION_ID, bt);

			// sort by gate address
			Collections.sort(devices, new Comparator<Device>() {

				@Override
				public int compare(Device left, Device right) {
					return Integer.valueOf(left.getGateId()).compareTo(Integer.valueOf(right.getGateId()));
				}
			});

			String aid = "";
			for (Device device : devices) {

				boolean isSameGate = aid.equals(device.getGateId());
				if (!isSameGate) { // new gate
					if (aid.length() > 0)
						serializer.endTag(ns, Xconstants.GATE);
					aid = device.getGateId();
					serializer.startTag(ns, Xconstants.GATE);
					serializer.attribute(ns, Xconstants.ID, aid);
				}
				serializer.startTag(ns, Xconstants.MODULE);
				serializer.attribute(ns, Xconstants.ID, device.getAddress());

				// FIXME: rework this
				/*for (Module module : device.getAllModules()) {
					serializer.startTag(ns, Xconstants.PART);
					serializer.attribute(ns, Xconstants.TYPE, module.getRawTypeId());
					serializer.endTag(ns, Xconstants.PART);
				}*/
				serializer.endTag(ns, Xconstants.MODULE);
			}
			serializer.endTag(ns, Xconstants.GATE);

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML for GetLog message
	 *
	 * @param bt         userID of user
	 * @param aid        gateId of actual gate
	 * @param did        deviceID of wanted module
	 * @param moduleType is type of module
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
			XmlSerializer serializer = beginXml(writer, STATE_GETLOGS);

			serializer.attribute(ns, Xconstants.COM_SESSION_ID, bt);
			serializer.attribute(ns, Xconstants.FROM, from);
			serializer.attribute(ns, Xconstants.TO, to);
			serializer.attribute(ns, Xconstants.FTYPE, funcType);
			serializer.attribute(ns, Xconstants.INTERVAL, String.valueOf(interval));
			serializer.attribute(ns, Xconstants.AID, aid);
			serializer.attribute(ns, Xconstants.DID, did);
			serializer.attribute(ns, Xconstants.DTYPE, moduleType);

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML of SetDevs message. Almost all fields are optional
	 *
	 * @param bt      userID of user
	 * @param aid     gateId of actual gate
	 * @param devices with changed fields
	 * @return Partial message
	 * @since 2.2
	 */
	public static String createSetDevs(String bt, String aid, List<Device> devices, EnumSet<SaveModule> toSave) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, STATE_DEVICE_UPDATE);

			serializer.attribute(ns, Xconstants.COM_SESSION_ID, bt);
			serializer.attribute(ns, Xconstants.AID, aid);

			for (Device device : devices) {
				serializer.startTag(ns, Xconstants.MODULE);

				if (toSave.contains(SaveModule.SAVE_INITIALIZED))
					serializer.attribute(ns, Xconstants.INITIALIZED, (device.isInitialized()) ? Xconstants.ONE : Xconstants.ZERO);
				serializer.attribute(ns, Xconstants.DID, device.getAddress());
				if (toSave.contains(SaveModule.SAVE_LOCATION))
					serializer.attribute(ns, Xconstants.LID, device.getLocationId());

				RefreshInterval refresh = device.getRefresh();
				if (refresh != null) {
					if (toSave.contains(SaveModule.SAVE_REFRESH))
						serializer.attribute(ns, Xconstants.REFRESH, Integer.toString(refresh.getInterval()));
				}

				// FIXME: rework this
				/*
				for (Module module : device.getAllModules()) {
					serializer.startTag(ns, Xconstants.PART);

					serializer.attribute(ns, Xconstants.TYPE, module.getRawTypeId());
					// if (toSave.contains(SaveModule.SAVE_VALUE))
					// serializer.attribute(ns, Xconstants.VALUE, String.valueOf(module.getId().getDoubleValue()));

					serializer.endTag(ns, Xconstants.PART);
				}*/
				serializer.endTag(ns, Xconstants.MODULE);
			}

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * New method create XML of SetDevs message with only one module in it. toSave parameter must by set properly.
	 *
	 * @param bt     userID of user
	 * @param aid    gateId of actual gate
	 * @param module to save
	 * @param toSave ECO mode to save only wanted fields
	 * @return SetDevs message
	 * @since 2.2
	 */
	public static String createSetDev(String bt, String aid, Module module, EnumSet<SaveModule> toSave) {
		StringWriter writer = new StringWriter();
		try {
			Device device = module.getDevice();

			XmlSerializer serializer = beginXml(writer, STATE_DEVICE_UPDATE);

			serializer.attribute(ns, Xconstants.COM_SESSION_ID, bt);
			serializer.attribute(ns, Xconstants.AID, aid);

			serializer.startTag(ns, Xconstants.MODULE);

			if (toSave.contains(SaveModule.SAVE_INITIALIZED))
				serializer.attribute(ns, Xconstants.INITIALIZED, (device.isInitialized()) ? Xconstants.ONE : Xconstants.ZERO);
			// send always
			serializer.attribute(ns, Xconstants.DID, device.getAddress());
			if (toSave.contains(SaveModule.SAVE_LOCATION))
				serializer.attribute(ns, Xconstants.LID, device.getLocationId());

			RefreshInterval refresh = device.getRefresh();
			if (refresh != null) {
				if (toSave.contains(Module.SaveModule.SAVE_REFRESH))
					serializer.attribute(ns, Xconstants.REFRESH, Integer.toString(refresh.getInterval()));
			}

			// FIXME: rework this
			/*if (toSave.contains(SaveModule.SAVE_NAME) || toSave.contains(Module.SaveModule.SAVE_VALUE)) {
				serializer.startTag(ns, Xconstants.PART);
				// send always if module changed
				serializer.attribute(ns, Xconstants.TYPE, module.getRawTypeId());
				if (toSave.contains(SaveModule.SAVE_NAME))
					serializer.attribute(ns, Xconstants.NAME, module.getName());
				if (toSave.contains(SaveModule.SAVE_VALUE))
					serializer.attribute(ns, Xconstants.VALUE, String.valueOf(module.getValue().getDoubleValue()));

				serializer.endTag(ns, Xconstants.PART);
			}*/

			serializer.endTag(ns, Xconstants.MODULE);

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create XML for Switch message
	 *
	 * @param bt     userID of user
	 * @param aid    gateId of actual gate
	 * @param module to switch value
	 * @return XML of Switch message
	 * @since 2.2
	 */
	public static String createSwitch(String bt, String aid, Module module) {
		// FIXME: rework this
		return "";/*createComAttribsVariant(Xconstants.COM_STATE, STATE_SWITCH, Xconstants.COM_SESSION_ID, bt, Xconstants.AID, aid, Xconstants.DID, module.getDevice().getAddress(), Xconstants.DTYPE,
				module.getRawTypeId(), Xconstants.VALUE, String.valueOf(module.getValue().getDoubleValue()));*/
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
		return createComAttribsVariant(Xconstants.COM_STATE, STATE_DEVICE_DELETE, Xconstants.COM_SESSION_ID, bt, Xconstants.AID, device.getGateId(), Xconstants.DID, device.getAddress());
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
	public static String createAddRoom(String bt, Location location) {
		return createComAttribsVariant(Xconstants.COM_STATE, STATE_LOCATION_ADD, Xconstants.COM_SESSION_ID, bt, Xconstants.AID, location.getGateId(), Xconstants.LTYPE, location.getType(), Xconstants.LNAME,
				location.getName());
	}

	/**
	 * Method create XML of SetRooms message
	 *
	 * @param bt        userID of user
	 * @param aid       gateId of actual gate
	 * @param locations list of location object to update
	 * @return message SetRooms
	 * @since 2.2
	 */
	public static String createSetRooms(String bt, String aid, List<Location> locations) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, STATE_LOCATION_UPDATE);

			serializer.attribute(ns, Xconstants.COM_SESSION_ID, bt);
			serializer.attribute(ns, Xconstants.AID, aid);

			for (Location location : locations) {
				serializer.startTag(ns, Xconstants.LOCATION);

				serializer.attribute(ns, Xconstants.ID, location.getId());
				serializer.attribute(ns, Xconstants.TYPE, location.getType());
				serializer.attribute(ns, Xconstants.NAME, location.getName());

				serializer.endTag(ns, Xconstants.LOCATION);
			}

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
	public static String createDeleteRoom(String bt, Location location) {
		return createComAttribsVariant(Xconstants.COM_STATE, STATE_LOCATION_DELETE, Xconstants.COM_SESSION_ID, bt, Xconstants.AID, location.getGateId(), Xconstants.LID, location.getId());
	}

	/**
	 * Method create XML of GetRooms message
	 *
	 * @param bt  userID of user
	 * @param aid gateId of actual gate
	 * @return message GetRooms
	 * @since 2.2
	 */
	public static String createGetRooms(String bt, String aid) {
		return createComAttribsVariant(Xconstants.COM_STATE, STATE_LOCATION_GETALL, Xconstants.COM_SESSION_ID, bt, Xconstants.AID, aid);
	}

	// /////////////////////////////////////PROVIDERS///////////////////////////////////////////////////

	/**
	 * Method create XML for AddAcount message
	 *
	 * @param bt    userID of user
	 * @param aid   gateId of actual gate
	 * @param users map with User object and User.Role
	 * @return AddAcc message
	 * @since 2.2
	 */
	public static String createAddAccounts(String bt, String aid, ArrayList<User> users) {
		return createAddSetAcc(STATE_GATE_USER_INVITE, bt, aid, users);
	}

	/**
	 * Method create XML for SetAcount message
	 *
	 * @param bt    userID of user
	 * @param aid   gateId of actual gate
	 * @param users map with User object and User.Role
	 * @return SetAcc message
	 * @since 2.2
	 */
	public static String createSetAccounts(String bt, String aid, ArrayList<User> users) {
		return createAddSeTAcc(STATE_GATE_USER_UPDATE, bt, aid, users);
	}

	/**
	 * Method create XML for DelAcc message
	 *
	 * @param bt    userID of user
	 * @param aid   gateId of actual gate
	 * @param users list with Users
	 * @return dellAcc message
	 * @since 2.2
	 */
	public static String createDelAccounts(String bt, String aid, List<User> users) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, STATE_GATE_USER_DELETE);

			serializer.attribute(ns, Xconstants.COM_SESSION_ID, bt);
			serializer.attribute(ns, Xconstants.AID, aid);

			for (User user : users) {
				serializer.startTag(ns, Xconstants.USER_ROOT);
				serializer.attribute(ns, Xconstants.USER_EMAIL, user.getEmail());
				serializer.endTag(ns, Xconstants.USER_ROOT);
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
	 * @param aid gateId of actual gate
	 * @return GetAcc message
	 * @since 2.2
	 */
	public static String createGetAccounts(String bt, String aid) {
		return createComAttribsVariant(Xconstants.COM_STATE, STATE_GATE_USER_GETALL, Xconstants.COM_SESSION_ID, bt, Xconstants.AID, aid);
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
		return createComAttribsVariant(Xconstants.COM_STATE, STATE_GCMID_DELETE, Xconstants.USER_UID, userId, Xconstants.GCMID, gcmid);
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
		return createComAttribsVariant(Xconstants.COM_STATE, STATE_GCMID_UPDATE, Xconstants.COM_SESSION_ID, bt, Xconstants.GCMID, gcmid);
	}

	/**
	 * Method create XML of GetNotifs message
	 *
	 * @param bt SessionID of user
	 * @return message GetNotifs
	 * @since 2.2
	 */
	public static String createGetNotifications(String bt) {
		return createComAttribsVariant(Xconstants.COM_STATE, STATE_NOTIFICATION_GETALL, Xconstants.COM_SESSION_ID, bt);
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
			XmlSerializer serializer = beginXml(writer, STATE_NOTIFICATION_READ);

			serializer.attribute(ns, Xconstants.COM_SESSION_ID, bt);

			for (String mid : mids) {
				serializer.startTag(ns, Xconstants.NOTIFICATION);
				serializer.attribute(ns, Xconstants.MSGID, mid);
				serializer.endTag(ns, Xconstants.NOTIFICATION);
			}

			return endXml(writer, serializer);
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
			XmlSerializer serializer = beginXml(writer, state == null ? STATE_ADDALG : STATE_SETALG);

			serializer.attribute(ns, Xconstants.COM_SESSION_ID, bt);
			if (state != null) {
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

			return endXml(writer, serializer);
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
			XmlSerializer serializer = beginXml(writer, STATE_GETALGS);

			serializer.attribute(ns, Xconstants.COM_SESSION_ID, bt);
			serializer.attribute(ns, Xconstants.AID, aid);

			for (String algId : algids) {
				serializer.startTag(ns, Xconstants.ALGORITHM);
				serializer.attribute(ns, Xconstants.ID, algId);
				serializer.endTag(ns, Xconstants.ALGORITHM);
			}

			return endXml(writer, serializer);
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
		return createComAttribsVariant(Xconstants.COM_STATE, STATE_GETALLALGS, Xconstants.COM_SESSION_ID, bt, Xconstants.AID, aid);
	}

	/**
	 * Method returns message with demands for delete specific rule
	 *
	 * @param bt    beeeon Token (session Id)
	 * @param algid gate id
	 * @return xml with delAlg message
	 */
	public static String createDelAlg(String bt, String algid) {
		return createComAttribsVariant(Xconstants.COM_STATE, STATE_DELALG, Xconstants.COM_SESSION_ID, bt, Xconstants.ALGID, algid);
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
		return createComAttribsVariant(Xconstants.COM_STATE, STATE_PASSBORDER, Xconstants.COM_SESSION_ID, bt, Xconstants.RID, rid, Xconstants.TYPE, type);
	}

	// /////////////////////////////////////PRIVATE METHODS//////////////////////////////////////////////

	protected static String createComAttribsVariant(String state, String... args) {
		if (0 != (args.length % 2)) { // odd
			throw new RuntimeException("Bad params count");
		}

		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, state);

			for (int i = 0; i < args.length; i += 2) { // take pair of args
				serializer.attribute(ns, args[i], args[i + 1]);
			}

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	private static String createAddSeTAcc(String state, String bt, String aid, ArrayList<User> users) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer, state);

			serializer.attribute(ns, Xconstants.COM_SESSION_ID, bt);
			serializer.attribute(ns, Xconstants.AID, aid);

			for (User user : users) {
				serializer.startTag(ns, Xconstants.USER_ROOT);

				serializer.attribute(ns, Xconstants.USER_EMAIL, user.getEmail());
				serializer.attribute(ns, Xconstants.ROLE, user.getRole().getId());
				serializer.endTag(ns, Xconstants.USER_ROOT);
			}

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}
}
