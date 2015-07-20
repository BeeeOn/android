package com.rehivetech.beeeon.network.xml;

import android.util.Xml;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ClientError;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.gcm.notification.VisibleNotification;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.gate.GateInfo;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.household.watchdog.Watchdog;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * @author ThinkDeep
 */
public class XmlParsers {

	private XmlPullParser mParser;

	private static final String COM_VER = Constants.COM_VER;
	private static final String TAG = XmlParsers.class.getSimpleName();
	private static final String ns = null;

	/**
	 * Represents states of communication (from server to app)
	 *
	 * @author ThinkDeep
	 */
	public enum State implements IIdentifier {
		GATES("adapters"),
		GATEINFO("gateinfo"),
		ALLDEVICES("alldevs"),
		DEVICES("devs"),
		LOGDATA("logdata"),
		ACCOUNTS("accounts"),
		TRUE("true"),
		FALSE("false"),
		VIEWS("views"),
		ROOMS("rooms"),
		ROOMCREATED("roomid"),
		NOTIFICATIONS("notifs"),
		BT("bt"),
		ALGCREATED("algcreated"),
		USERINFO("userinfo"),
		ALGORITHMS("algs");

		private final String mValue;

		State(String value) {
			mValue = value;
		}

		public String getId() {
			return mValue;
		}
	}

	public XmlParsers() {
	}

	/**
	 * Method parse message (XML) in communication version
	 *
	 * @param xmlInput  raw string xml input
	 * @param namespace always false
	 * @return object with parsed xml data inside
	 * @throws XmlPullParserException
	 * @throws IOException            means Communication version mismatch exception
	 * @throws ParseException
	 */
	public ParsedMessage parseCommunication(String xmlInput, boolean namespace) throws XmlPullParserException, IOException,
			ParseException {
		mParser = Xml.newPullParser();
		mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, namespace);

		mParser.setInput(new ByteArrayInputStream(xmlInput.getBytes("UTF-8")), null);
		mParser.nextTag();

		mParser.require(XmlPullParser.START_TAG, ns, Xconstants.COM_ROOT);

		State state = Utils.getEnumFromId(State.class, getSecureAttrValue(Xconstants.STATE));
		String version = getSecureAttrValue(Xconstants.VERSION);

		if (!version.equals(COM_VER)) {
			String srv[] = version.split("\\.");
			String app[] = COM_VER.split("\\.");

			if (srv.length != 0 && app.length != 0) {
				int srv_major = Integer.parseInt(srv[0]);
				int srv_minor = Integer.parseInt(srv[1]);
				int app_major = Integer.parseInt(app[0]);
				int app_minor = Integer.parseInt(app[1]);
				if (srv_major != app_major || srv_minor < app_minor) {
					// Server must have same major version as app and same or greater minor version than app
					throw new AppException(NetworkError.COM_VER_MISMATCH)
							.set(NetworkError.PARAM_COM_VER_LOCAL, COM_VER)
							.set(NetworkError.PARAM_COM_VER_SERVER, version);
				}
			} else {
				// Server must have same major version as app and same or greater minor version than app
				throw new AppException(NetworkError.COM_VER_MISMATCH)
						.set(NetworkError.PARAM_COM_VER_LOCAL, COM_VER)
						.set(NetworkError.PARAM_COM_VER_SERVER, version);
			}


		}

		ParsedMessage result = new ParsedMessage(state);

		switch (state) {
			case USERINFO:
				// User
				result.data = parserUserInfo();
				break;
			case BT:
				// String (BeeeonToken)
				result.data = getSecureAttrValue(Xconstants.BT);
				break;
			case TRUE:
				// nothing
				break;
			case FALSE:
				// FalseAnswer
				result.data = parseFalse();
				break;
			case GATES:
				// List<Gate>
				result.data = parseGatesReady();
				break;
			case GATEINFO:
				// GateInfo
				result.data = parseGateInfo();
				break;
			case LOGDATA:
				// ModuleLog
				result.data = parseLogData();
				break;
			case ROOMS:
				// List<Location>
				result.data = parseRooms();
				break;
			case ROOMCREATED:
				// String (locationID)
				result.data = getSecureAttrValue(Xconstants.LID);
				break;
			case ALGCREATED:
				// String (AlgorithmID)
				result.data = getSecureAttrValue(Xconstants.ALGID);
				break;
			case ALGORITHMS:
				getSecureAttrValue(Xconstants.ATYPE); // not used yet
				// ArrayList<Watchdog>
				result.data = parseWatchdog();
				break;
			case ACCOUNTS:
				// List<User>
				result.data = parseConAccountList();
				break;
			case DEVICES:
				String aid = getSecureAttrValue(Xconstants.AID);
				if (aid.length() > 0) {
					// List<Device>
					result.data = parseNewDevices(aid);
				} else
					// List<Device>
					result.data = parseDevices();
				break;
			case ALLDEVICES:
				// List<Device>
				result.data = parseAllDevices();
				break;
			case NOTIFICATIONS:
				// List<Notification>
				result.data = parseNotifications();
				break;
			default:
				break;
		}
		mParser = null;
		return result;
	}

	// /////////////////////////////////FALSE, GATES, USERINFO////////////////////////////////////

	/**
	 * Method parse inner part of GatesReady message
	 *
	 * @return List of gates (contains only Id, name, and user role)
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @since 2.2
	 */
	private List<Gate> parseGatesReady() throws XmlPullParserException, IOException {
		mParser.nextTag();
		List<Gate> result = new ArrayList<>();

		if (!mParser.getName().equals(Xconstants.GATE))
			return result;

		do {
			Gate gate = new Gate(getSecureAttrValue(Xconstants.ID), getSecureAttrValue(Xconstants.NAME));
			gate.setRole(Utils.getEnumFromId(User.Role.class, getSecureAttrValue(Xconstants.ROLE), User.Role.Guest));
			gate.setUtcOffset(getSecureInt(getSecureAttrValue(Xconstants.UTC)));
			result.add(gate);

			mParser.nextTag();

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

		return result;
	}

	/**
	 * Method parse GateInfo message
	 *
	 * @return GateInfo
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @since 2.5
	 */
	private GateInfo parseGateInfo() throws XmlPullParserException, IOException {
		String id = getSecureAttrValue(Xconstants.AID);
		User.Role role = Utils.getEnumFromId(User.Role.class, getSecureAttrValue(Xconstants.ROLE), User.Role.Guest);
		String name = getSecureAttrValue(Xconstants.ANAME);
		int devicesCount = getSecureInt(getSecureAttrValue(Xconstants.NFACS));
		int usersCount = getSecureInt(getSecureAttrValue(Xconstants.NUSERS));
		String ip = getSecureAttrValue(Xconstants.IP);
		String version = getSecureAttrValue(Xconstants.AVERSION);
		int utcOffsetInMinutes = getSecureInt(getSecureAttrValue(Xconstants.UTC));

		return new GateInfo(id, name, role, utcOffsetInMinutes, devicesCount, usersCount, version, ip);
	}

	/**
	 * Method parse inner part of UserInfo message
	 * @return User object
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private User parserUserInfo() throws XmlPullParserException, IOException {
		User user = new User();
		user.setId(getSecureAttrValue(Xconstants.UID));
		user.setName(getSecureAttrValue(Xconstants.NAME));
		user.setSurname(getSecureAttrValue(Xconstants.SURNAME));
		user.setEmail(getSecureAttrValue(Xconstants.EMAIL));
		user.setGender(Utils.getEnumFromId(User.Gender.class, getSecureAttrValue(Xconstants.GENDER), User.Gender.UNKNOWN));
		user.setPictureUrl(getSecureAttrValue(Xconstants.IMGURL));

		mParser.nextTag(); // accounts

		if (!mParser.getName().equals(Xconstants.ACCOUNTS))
			return user;

		HashMap<String, String> providers = user.getJoinedProviders();

		while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.ACCOUNTS)) { //srv
			providers.put(getSecureAttrValue(Xconstants.NAME), getSecureAttrValue(Xconstants.ID));
			mParser.nextTag(); // end srv
		}

		return user;
	}

	/**
	 * Method parse inner part of False message
	 *
	 * @return FalseAnswer
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @since 2.2
	 */
	private FalseAnswer parseFalse() throws XmlPullParserException, IOException {
		int err = getSecureInt(getSecureAttrValue(Xconstants.ERRCODE));
		return new FalseAnswer((mParser.getEventType() == XmlPullParser.END_TAG) ? "" : readText(Xconstants.COM_ROOT), err);
	}

	// /////////////////////////////////DEVICES, LOGS/////////////////////////////////////////////////////////

	/**
	 * Method parse inner part of AllDevice message (old:XML message (using parsePartial()))
	 *
	 * @return list of devices
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws ParseException
	 */
	private List<Device> parseAllDevices() throws XmlPullParserException, IOException, ParseException {

		String aid = getSecureAttrValue(Xconstants.AID);
		mParser.nextTag(); // dev start tag

		List<Device> result = new ArrayList<>();

		if (!mParser.getName().equals(Xconstants.MODULE))
			return result;

		parseInnerDevs(result, aid, true);

		return result;
	}

	// special case of parseDevice
	private List<Device> parseNewDevices(String aid) throws XmlPullParserException, IOException, ParseException {
		mParser.nextTag(); // dev start tag

		List<Device> result = new ArrayList<>();

		if (!mParser.getName().equals(Xconstants.MODULE))
			return result;

		parseInnerDevs(result, aid, false);

		return result;
	}

	/**
	 * Method parse inner part of Module message (old:Partial message (set of module's tag))
	 *
	 * @return List of devices
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws ParseException
	 */
	private List<Device> parseDevices() throws XmlPullParserException, IOException, ParseException {
		mParser.nextTag(); // gate tag

		List<Device> result = new ArrayList<>();

		if (!mParser.getName().equals(Xconstants.GATE))
			return result;

		do { // go through gates
			String aid = getSecureAttrValue(Xconstants.ID);
			mParser.nextTag(); // dev tag

			parseInnerDevs(result, aid, true);

			mParser.nextTag(); // gate endtag
		} while (!mParser.getName().equals(Xconstants.COM_ROOT) && mParser.nextTag() != XmlPullParser.END_TAG);

		return result;
	}

	private void parseInnerDevs(List<Device> result, String aid, boolean init) throws XmlPullParserException, IOException {
		do { // go through devs (devices)
			//int type = getSecureInt(Xconstants.TYPE);
			String type = getSecureAttrValue(Xconstants.TYPE);
			String address = getSecureAttrValue(Xconstants.DID);

			Device device = Device.createDeviceByType(type, aid, address);

			// device.setInitialized(getSecureAttrValue(Xconstants.INITIALIZED).equals(Xconstants.ZERO) ? false : true);
			device.setInitialized(init);
			device.setLocationId(getSecureAttrValue(Xconstants.LID));
			device.setLastUpdate(new DateTime((long) getSecureInt(getSecureAttrValue(Xconstants.TIME)) * 1000, DateTimeZone.UTC));
			device.setInvolveTime(new DateTime((long) getSecureInt(getSecureAttrValue(Xconstants.INVOLVED)) * 1000, DateTimeZone.UTC));
			device.setNetworkQuality(getSecureInt(getSecureAttrValue(Xconstants.RSSI)));

			// FIXME: support for refresh/battery and other features
			// device.setRefresh(RefreshInterval.fromInterval(getSecureInt(getSecureAttrValue(Xconstants.REFRESH))));
			// device.setBattery(getSecureInt(getSecureAttrValue(Xconstants.BATTERY)));

			mParser.nextTag(); // part tag

			if (!mParser.getName().equals(Xconstants.PART)) { // if there is no module in device -> error in DB on server
				Log.e(TAG, "Missing module in device: " + device.getId());
				continue;
			}

			// Load modules values
			do { // go through modules
				String moduleId = getSecureAttrValue(Xconstants.ID);
				String moduleValue = getSecureAttrValue(Xconstants.VALUE);
				device.setModuleValue(moduleId, moduleValue);

				mParser.nextTag(); // module endtag
			} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.MODULE));

			result.add(device);
		} while (mParser.nextTag() != XmlPullParser.END_TAG
				&& (!mParser.getName().equals(Xconstants.GATE) || !mParser.getName().equals(Xconstants.COM_ROOT)));
	}

	/**
	 * Method parse inner part of LogData (old:Content.log) message
	 *
	 * @return List with ContentRow objects
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private ModuleLog parseLogData() throws XmlPullParserException, IOException {
		mParser.nextTag();
		// mParser.require(XmlPullParser.START_TAG, ns, Xconstants.ROW); // strict solution

		ModuleLog log = new ModuleLog();

		if (!mParser.getName().equals(Xconstants.ROW))
			return log;

		do {
			try {

				String repeat = getSecureAttrValue(Xconstants.REPEAT);
				String interval = getSecureAttrValue(Xconstants.INTERVAL);
				String row = readText(Xconstants.ROW);

				// Split row data
				String[] parts = row.split(Xconstants.ROW_DATA_SEPARATOR);
				if (parts.length != 2) {
					Log.e(TAG, String.format("Wrong number of parts (%d) of data: %s", parts.length, row));
					throw new AppException(ClientError.XML).set("parts", parts);
				}

				// Parse values
				Long dateMillis = Long.parseLong(parts[0]) * 1000;
				Float value = Float.parseFloat(parts[1]);

				if (!repeat.isEmpty() && !interval.isEmpty()) {
					log.addValueInterval(dateMillis, value, Integer.parseInt(repeat), Integer.parseInt(interval));
				} else {
					log.addValue(dateMillis, value);
				}
			} catch (NumberFormatException e) {
				throw AppException.wrap(e, ClientError.XML);
			}
		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

		return log;
	}

	// /////////////////////////////////ROOMS//////////////////////////////////////////////////////////////////

	/**
	 * Method parse inner part of Rooms message
	 *
	 * @return list of locations
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private List<Location> parseRooms() throws XmlPullParserException, IOException {
		String aid = getSecureAttrValue(Xconstants.AID);
		mParser.nextTag();

		List<Location> result = new ArrayList<>();

		if (!mParser.getName().equals(Xconstants.LOCATION))
			return result;

		do {
			String type = getSecureAttrValue(Xconstants.TYPE);
			Location location = new Location(getSecureAttrValue(Xconstants.ID), getSecureAttrValue(Xconstants.NAME), aid, type.isEmpty() ? "0" : type);
			result.add(location);

			mParser.nextTag(); // loc end tag

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

		return result;
	}

	// /////////////////////////////////ACCOUNTS///////////////////////////////////////////////////////////////

	/**
	 * Method parse inner part of ConAccountList message
	 *
	 * @return list of users
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private List<User> parseConAccountList() throws XmlPullParserException, IOException {
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, Xconstants.USER);

		List<User> result = new ArrayList<>();
		do {
			User user = new User();
			user.setId(getSecureAttrValue(Xconstants.ID));
			user.setEmail(getSecureAttrValue(Xconstants.EMAIL));
			user.setName(getSecureAttrValue(Xconstants.NAME));
			user.setSurname(getSecureAttrValue(Xconstants.SURNAME));
			user.setRole(Utils.getEnumFromId(User.Role.class, getSecureAttrValue(Xconstants.ROLE), User.Role.Guest));
			user.setGender(Utils.getEnumFromId(User.Gender.class, getSecureAttrValue(Xconstants.GENDER), User.Gender.UNKNOWN));
			user.setPictureUrl(getSecureAttrValue(Xconstants.IMGURL));

			result.add(user);
			mParser.nextTag();

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

		return result;
	}

	// /////////////////////////////////NOTIFICATIONS//////////////////////////////////////////////////////////

	private List<VisibleNotification> parseNotifications() throws XmlPullParserException, IOException {
		mParser.nextTag();
		// mParser.require(XmlPullParser.START_TAG, ns, NOTIFICATION); // strict solution

		List<VisibleNotification> result = new ArrayList<>();

		if (!mParser.getName().equals(Xconstants.NOTIFICATION))
			return result;

		do {
			VisibleNotification ntfc = null;

			String name = getSecureAttrValue(Xconstants.NAME); // name
			String id = getSecureAttrValue(Xconstants.MID); // message id
			String time = getSecureAttrValue(Xconstants.TIME); // time
			String type = getSecureAttrValue(Xconstants.TYPE); // type
			boolean read = (getSecureInt(getSecureAttrValue(Xconstants.READ)) == 0) ? false : true; // read

			//TODO
			// call here some method from gcm/notification part
			// method should parse inner elements of notif tag and return notification
			// the mParser should be at the ends element </notif>, because after call nextTag I need to get <notif> or </com>
			// something like
			// ntfc = method(mParser, name, id, time, type, read);

			ntfc = VisibleNotification.parseXml(name, id, time, type, read, mParser);

			if (ntfc != null) {
				result.add(ntfc);
			}

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

		return result;
	}

	// /////////////////////////////////WATCHDOG/////////////////////////////////////////////////////////////

	/**
	 * Method parse inner part of watchdog
	 *
	 * @return list of watchdog objects
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private ArrayList<Watchdog> parseWatchdog() throws XmlPullParserException, IOException {
		String aid = getSecureAttrValue(Xconstants.AID);
		mParser.nextTag();

		ArrayList<Watchdog> result = new ArrayList<>();

		if (!mParser.getName().equals(Xconstants.ALGORITHM))
			return result;

		do {
			Watchdog watchdog = new Watchdog(getSecureInt(getSecureAttrValue(Xconstants.ATYPE)));
			watchdog.setId(getSecureAttrValue(Xconstants.ID));
			watchdog.setGateId(aid);
			watchdog.setEnabled(getSecureInt(getSecureAttrValue(Xconstants.ENABLE)) > 0);
			watchdog.setName(getSecureAttrValue(Xconstants.NAME));

			TreeMap<String, String> tDevices = new TreeMap<>();
			TreeMap<String, String> tParams = new TreeMap<>();

			mParser.nextTag();

			if (!mParser.getName().equals(Xconstants.MODULE) && !mParser.getName().equals(Xconstants.PARAM) && !mParser.getName().equals(Xconstants.GEOFENCE))
				Log.e(TAG, "someone send bad xml");//TODO do something

			do {
				String position = getSecureAttrValue(Xconstants.POSITION);

				if (mParser.getName().equals(Xconstants.MODULE)) {
					String module = getSecureAttrValue(Xconstants.ID) + Module.ID_SEPARATOR + getSecureAttrValue(Xconstants.TYPE);
					tDevices.put(position, module);

					mParser.nextTag();
				} else if (mParser.getName().equals(Xconstants.GEOFENCE)) {
					watchdog.setGeoRegionId(getSecureAttrValue(Xconstants.RID));
					mParser.nextTag();
				} else {
					String param = readText(Xconstants.PARAM);
					tParams.put(position, param);
					// FIXME: this is workaround cause server not returning <geo> tag .. when it's added, this will not be necessary
					if (position.equals("1") && watchdog.getType() == Watchdog.TYPE_GEOFENCE && watchdog.getGeoRegionId() == null) {
						watchdog.setGeoRegionId(param);
					}
				}

			} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.ALGORITHM));

			watchdog.setModules(new ArrayList<>(tDevices.values()));
			watchdog.setParams(new ArrayList<>(tParams.values()));

			result.add(watchdog);

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

		return result;
	}

	// ///////////////////////////////// OTHER

	// FIXME: check on first use
	List<Device> getFalseMessage10() throws XmlPullParserException, IOException {

		mParser.nextTag();

		List<Device> result = new ArrayList<>();

		mParser.nextTag();
		if (!mParser.getName().equals(Xconstants.MODULE))
			return result;

		do {
			Device device = null;
			boolean deviceExists = false;

			// FIXME: rework this
			/*Module module = Module.createFromModuleTypeId(getSecureAttrValue(Xconstants.TYPE));

			String id = getSecureAttrValue(Xconstants.ID);
			for (Device fac : result) {
				if (fac.getAddress().equals(id)) {
					// We already have this mDevice, just add new devices to it
					deviceExists = true;
					device = fac;
					break;
				}
			}

			if (device == null) {
				// This mDevice is new, first create a object for it
				device = new Device();
				device.setAddress(id);
			}

			device.addModule(module);

			if (!deviceExists)
				result.add(device);*/

			mParser.nextTag();

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

		return result;
	}

	// FIXME: check on first use
	List<User> getFalseMessage17() throws XmlPullParserException, IOException {

		//mParser.nextTag();

		List<User> result = new ArrayList<>();

		mParser.nextTag();
		if (!mParser.getName().equals(Xconstants.USER))
			return result;

		do {
			User user = new User();
			user.setId(getSecureAttrValue(Xconstants.ID));
			user.setEmail(getSecureAttrValue(Xconstants.EMAIL));
			user.setName(getSecureAttrValue(Xconstants.NAME));
			user.setSurname(getSecureAttrValue(Xconstants.SURNAME));
			user.setRole(Utils.getEnumFromId(User.Role.class, getSecureAttrValue(Xconstants.ROLE), User.Role.Guest));
			user.setGender(Utils.getEnumFromId(User.Gender.class, getSecureAttrValue(Xconstants.GENDER), User.Gender.UNKNOWN));
			user.setPictureUrl(getSecureAttrValue(Xconstants.IMGURL));

			result.add(user);
			mParser.nextTag();

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

		return result;
	}

	// //////////////////////////////// XML

	/**
	 * Skips whole element and sub-elements.
	 *
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private void skip() throws XmlPullParserException, IOException {
		Log.d(TAG, "Skipping unknown child '" + mParser.getName() + "'");
		if (mParser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (mParser.next()) {
				case XmlPullParser.END_TAG:
					depth--;
					break;
				case XmlPullParser.START_TAG:
					depth++;
					break;
			}
		}
	}

	/**
	 * Read text value of some element.
	 *
	 * @param tag name of element to proccess
	 * @return value of element
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String readText(String tag) throws IOException, XmlPullParserException {
		mParser.require(XmlPullParser.START_TAG, ns, tag);

		String result = "";
		if (mParser.next() == XmlPullParser.TEXT) {
			result = mParser.getText();
			mParser.nextTag();
		}

		mParser.require(XmlPullParser.END_TAG, ns, tag);
		return result;
	}

	/**
	 * Method change null result value to empty string (mParser and namespace is included)
	 *
	 * @param name of the attribute
	 * @return parsed attribute or empty string
	 */
	private String getSecureAttrValue(String name) {
		String result = mParser.getAttributeValue(ns, name);
		return (result == null) ? "" : result;
	}

	/**
	 * Method return integer value of string, or zero if length is 0
	 *
	 * @param value that should be proccess
	 * @return integer value of zero if length is 0
	 */
	private int getSecureInt(String value) {
		return (value.length() < 1) ? 0 : Integer.parseInt(value);
	}
}