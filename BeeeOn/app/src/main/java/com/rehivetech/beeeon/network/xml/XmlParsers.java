package com.rehivetech.beeeon.network.xml;

import android.support.annotation.NonNull;
import android.util.Xml;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ClientError;
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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * @author ThinkDeep
 * @author Robyer
 */
public class XmlParsers {

	private XmlPullParser mParser;

	private State mState;
	private int mErrorCode = -1;
	private String mVersion;

	private static final String TAG = XmlParsers.class.getSimpleName();
	private static final String ns = null;

	/**
	 * Represents states of communication (from server to app)
	 *
	 * @author ThinkDeep
	 */
	public enum State implements IIdentifier {
		SESSION_ID("sessionid"),
		USERINFO("userinfo"),
		GATES("gates"),
		GATEINFO("gateinfo"),
		DEVICES("devices"),

		ALLDEVICES("alldevs"),
		LOGDATA("logdata"),
		LOCATIONS("locations"),
		LOCATIONID("locationid"),
		ACCOUNTS("gateusers"),

		TRUE("true"),
		FALSE("false"),

		ALGCREATED("algcreated"),
		ALGORITHMS("algs"),

		VIEWS("views"),
		NOTIFICATIONS("notifs");

		private final String mValue;

		State(String value) {
			mValue = value;
		}

		public String getId() {
			return mValue;
		}
	}

	private XmlParsers(@NonNull String xmlInput) throws XmlPullParserException, UnsupportedEncodingException {
		mParser = Xml.newPullParser();
		mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		mParser.setInput(new ByteArrayInputStream(xmlInput.getBytes("UTF-8")), null);
	}

	public static XmlParsers parse(@NonNull String xmlInput) throws AppException {
		XmlParsers parser;
		try {
			parser = new XmlParsers(xmlInput);
			parser.parseRoot();
		} catch (XmlPullParserException | IOException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
		return parser;
	}

	private void parseRoot() throws IOException, XmlPullParserException, AppException {
		mParser.nextTag();

		mParser.require(XmlPullParser.START_TAG, ns, "com");

		String state = getSecureAttrValue("state");
		String version = getSecureAttrValue("version");
		String errorCode = getSecureAttrValue("errcode");

		mState = Utils.getEnumFromId(State.class, state);
		mErrorCode = !errorCode.isEmpty() ? Integer.parseInt(errorCode) : -1;
		mVersion = version;
	}

	public State getState() {
		return mState;
	}

	public String getVersion() {
		return mVersion;
	}

	public int getErrorCode() {
		return mErrorCode;
	}

	// /////////////////////////////////SIMPLE RESULTS///////////////////////////////////////////

	public String parseSessionId() {
		return getSecureAttrValue("sessionid");
	}

	public String parseNewWatchdogId() {
		return getSecureAttrValue("algid");
	}

	// /////////////////////////////////GATES, USERINFO///////////////////////////////////////////

	/**
	 * Method parse inner part of GatesReady message
	 *
	 * @return List of gates (contains only Id, name, and user role)
	 * @since 2.2
	 */
	public List<Gate> parseGates() {
		try {
			mParser.nextTag();
			List<Gate> result = new ArrayList<>();

			if (!mParser.getName().equals("gate"))
				return result;

			do {
				Gate gate = new Gate(getSecureAttrValue("id"), getSecureAttrValue("name"));
				gate.setRole(Utils.getEnumFromId(User.Role.class, getSecureAttrValue("role"), User.Role.Guest));
				gate.setUtcOffset(getSecureInt(getSecureAttrValue("utc")));
				result.add(gate);

				mParser.nextTag();

			} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals("com"));

			return result;
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method parse GateInfo message
	 *
	 * @return GateInfo
	 * @since 2.5
	 */
	public GateInfo parseGateInfo() {
		try {
			mParser.nextTag();

			if (!mParser.getName().equals("gate"))
				throw new AppException(ClientError.XML);

			String id = getSecureAttrValue("id");
			User.Role role = Utils.getEnumFromId(User.Role.class, getSecureAttrValue("accessrole"), User.Role.Guest);
			String name = getSecureAttrValue("name");
			int devicesCount = getSecureInt(getSecureAttrValue("devices"));
			int usersCount = getSecureInt(getSecureAttrValue("users"));
			String ip = getSecureAttrValue("ip");
			String version = getSecureAttrValue("version");
			int utcOffsetInMinutes = getSecureInt("utc");

			return new GateInfo(id, name, role, utcOffsetInMinutes, devicesCount, usersCount, version, ip);
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method parse inner part of UserInfo message
	 * @return User object
	 */
	public User parseUserInfo() {
		try {
			User user = new User();

			mParser.nextTag(); // user
			if (!mParser.getName().equals("user"))
				throw new AppException(ClientError.XML);

			user.setId(getSecureAttrValue("uid"));
			user.setName(getSecureAttrValue("name"));
			user.setSurname(getSecureAttrValue("surname"));
			user.setGender(Utils.getEnumFromId(User.Gender.class, getSecureAttrValue("gender"), User.Gender.UNKNOWN));
			user.setEmail(getSecureAttrValue("email"));
			user.setPictureUrl(getSecureAttrValue("imgurl"));

			mParser.nextTag(); // providers
			if (!mParser.getName().equals("providers"))
				return user;

			HashMap<String, String> providers = user.getJoinedProviders();

			while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals("provider")) { // provider
				providers.put(getSecureAttrValue("name"), getSecureAttrValue("id"));
				mParser.nextTag(); // end provider
			}

			mParser.nextTag(); // end providers

			return user;
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	// /////////////////////////////////DEVICES, LOGS/////////////////////////////////////////////////////////

	/**
	 * Method parse inner part of AllDevice message (old:XML message (using parsePartial()))
	 *
	 * @return list of devices
	 */
	public List<Device> parseAllDevices() {
		try {
			String aid = getSecureAttrValue("gateid");
			mParser.nextTag(); // dev start tag

			List<Device> result = new ArrayList<>();

			if (!mParser.getName().equals("dev"))
				return result;

			parseInnerDevs(result, aid, true);
			return result;
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	// special case of parseDevice
	public List<Device> parseNewDevices(String aid) {
		try {
			mParser.nextTag(); // dev start tag

			List<Device> result = new ArrayList<>();

			if (!mParser.getName().equals("dev"))
				return result;

			parseInnerDevs(result, aid, false);
			return result;
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method parse inner part of Module message (old:Partial message (set of module's tag))
	 *
	 * @return List of devices
	 */
	public List<Device> parseDevices() {
		try {
			mParser.nextTag(); // gate tag

			List<Device> result = new ArrayList<>();

			if (!mParser.getName().equals("gate"))
				return result;

			do { // go through gates
				String aid = getSecureAttrValue("id");
				mParser.nextTag(); // dev tag

				parseInnerDevs(result, aid, true);

				mParser.nextTag(); // gate endtag
			} while (!mParser.getName().equals("com") && mParser.nextTag() != XmlPullParser.END_TAG);

			return result;
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	private void parseInnerDevs(List<Device> result, String aid, boolean init) {
		try {
			do { // go through devs (devices)
				String type = getSecureAttrValue("type");
				String address = getSecureAttrValue("id");

				Device device = Device.createDeviceByType(type, aid, address);
				device.setInitialized(init);
				// Alternatively get it from XML
				// device.setInitialized(getSecureAttrValue("init").equals("1"));

				device.setLocationId(getSecureAttrValue("locationid"));
				device.setLastUpdate(new DateTime((long) getSecureInt(getSecureAttrValue("time")) * 1000, DateTimeZone.UTC));
				// PairedTime is not used always...
				device.setPairedTime(new DateTime((long) getSecureInt(getSecureAttrValue("involved")) * 1000, DateTimeZone.UTC));

				// Load modules values
				while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals("module")) {
					// go through modules
					String moduleId = getSecureAttrValue("id");
					String moduleValue = getSecureAttrValue("value");
					device.setModuleValue(moduleId, moduleValue);

					mParser.nextTag(); // module endtag
				}

				result.add(device);
			} while (mParser.nextTag() != XmlPullParser.END_TAG
					&& (!mParser.getName().equals("gate") || !mParser.getName().equals("com")));
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method parse inner part of LogData (old:Content.log) message
	 *
	 * @return List with ContentRow objects
	 */
	public ModuleLog parseLogData() {
		try {
			mParser.nextTag();
			// mParser.require(XmlPullParser.START_TAG, ns, "row); // strict solution

			ModuleLog log = new ModuleLog();

			if (!mParser.getName().equals("row"))
				return log;

			do {
				try {

					String repeat = getSecureAttrValue("repeat");
					String interval = getSecureAttrValue("interval");
					String row = readText("row");

					// Split row data
					String[] parts = row.split("\\s+");
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
			} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals("com"));

			return log;
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	// /////////////////////////////////LOCATIONS//////////////////////////////////////////////////////////////////

	/**
	 * Method parse inner part of Rooms message
	 *
	 * @return list of locations
	 */
	public List<Location> parseLocations() {
		try {
			String gateId = getSecureAttrValue("gateid");
			mParser.nextTag();

			List<Location> result = new ArrayList<>();

			if (!mParser.getName().equals("location"))
				return result;

			do {
				String id = getSecureAttrValue("id");
				String type = getSecureAttrValue("type");
				String name = getSecureAttrValue("name");
				Location location = new Location(id, name, gateId, type.isEmpty() ? "0" : type);
				result.add(location);

				mParser.nextTag(); // loc end tag

			} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals("com"));

			return result;
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	public String parseNewLocationId() {
		try {
			mParser.nextTag();

			if (!mParser.getName().equals("location"))
				throw new AppException(ClientError.XML);

			return getSecureAttrValue("id");
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	// /////////////////////////////////PROVIDERS///////////////////////////////////////////////////////////////

	/**
	 * Method parse inner part of ConAccountList message
	 *
	 * @return list of users
	 */
	public List<User> parseGateUsers() {
		try {
			mParser.nextTag();
			mParser.require(XmlPullParser.START_TAG, ns, "user");

			List<User> result = new ArrayList<>();
			do {
				User user = new User();
				user.setId(getSecureAttrValue("id"));
				user.setEmail(getSecureAttrValue("email"));
				user.setRole(Utils.getEnumFromId(User.Role.class, getSecureAttrValue("role"), User.Role.Guest)); // FIXME: probably this should have different name
				user.setName(getSecureAttrValue("name"));
				user.setSurname(getSecureAttrValue("surname"));
				user.setGender(Utils.getEnumFromId(User.Gender.class, getSecureAttrValue("gender"), User.Gender.UNKNOWN));
				user.setPictureUrl(getSecureAttrValue("imgurl")); // FIXME: this isn't in specification but it should be here...

				result.add(user);
				mParser.nextTag();

			} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals("com"));

			return result;
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	// /////////////////////////////////NOTIFICATIONS//////////////////////////////////////////////////////////

	public List<VisibleNotification> parseNotifications() {
		try {
			mParser.nextTag();
			// mParser.require(XmlPullParser.START_TAG, ns, NOTIFICATION); // strict solution

			List<VisibleNotification> result = new ArrayList<>();

			if (!mParser.getName().equals("notif"))
				return result;

			do {
				VisibleNotification ntfc;

				String name = getSecureAttrValue("name");
				String id = getSecureAttrValue("mid");
				String time = getSecureAttrValue("time");
				String type = getSecureAttrValue("type");
				boolean read = (getSecureInt(getSecureAttrValue("read")) != 0);

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

			} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals("com"));

			return result;
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	// /////////////////////////////////WATCHDOG/////////////////////////////////////////////////////////////

	/**
	 * Method parse inner part of watchdog
	 *
	 * @return list of watchdog objects
	 */
	public ArrayList<Watchdog> parseWatchdog() {
		try {
			getSecureAttrValue("atype"); // not used yet

			String aid = getSecureAttrValue("gateid");
			mParser.nextTag();

			ArrayList<Watchdog> result = new ArrayList<>();

			if (!mParser.getName().equals("alg"))
				return result;

			do {
				Watchdog watchdog = new Watchdog(getSecureInt(getSecureAttrValue("atype")));
				watchdog.setId(getSecureAttrValue("id"));
				watchdog.setGateId(aid);
				watchdog.setEnabled(getSecureInt(getSecureAttrValue("enable")) > 0);
				watchdog.setName(getSecureAttrValue("name"));

				TreeMap<String, String> tDevices = new TreeMap<>();
				TreeMap<String, String> tParams = new TreeMap<>();

				mParser.nextTag();

				if (!mParser.getName().equals("dev") && !mParser.getName().equals("par") && !mParser.getName().equals("geo"))
					Log.e(TAG, "someone send bad xml");//TODO do something

				do {
					String position = getSecureAttrValue("pos");

					if (mParser.getName().equals("dev")) {
						String module = getSecureAttrValue("id") + Module.ID_SEPARATOR + getSecureAttrValue("type");
						tDevices.put(position, module);

						mParser.nextTag();
					} else if (mParser.getName().equals("geo")) {
						watchdog.setGeoRegionId(getSecureAttrValue("rid"));
						mParser.nextTag();
					} else {
						String param = readText("par");
						tParams.put(position, param);
						// FIXME: this is workaround cause server not returning <geo> tag .. when it's added, this will not be necessary
						if (position.equals("1") && watchdog.getType() == Watchdog.TYPE_GEOFENCE && watchdog.getGeoRegionId() == null) {
							watchdog.setGeoRegionId(param);
						}
					}

				} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals("alg"));

				watchdog.setModules(new ArrayList<>(tDevices.values()));
				watchdog.setParams(new ArrayList<>(tParams.values()));

				result.add(watchdog);

			} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals("com"));

			return result;
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	// ////////////////////////////////HELPERS///////////////////////////////////////////////////////////////

	/**
	 * Skips whole element and sub-elements.
	 */
	@SuppressWarnings("unused")
	private void skip() {
		try {
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
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
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