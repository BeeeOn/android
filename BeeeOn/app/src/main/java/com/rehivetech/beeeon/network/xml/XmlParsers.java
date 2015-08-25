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
		TOKEN("token"),
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

		mParser.require(XmlPullParser.START_TAG, ns, Xconstants.COM_ROOT);

		String state = getSecureAttrValue(Xconstants.COM_STATE);
		String version = getSecureAttrValue(Xconstants.COM_VERSION);
		String errorCode = getSecureAttrValue(Xconstants.COM_ERRCODE);

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

	public String parseSessionId() {
		return getSecureAttrValue(Xconstants.COM_SESSION_ID);
	}

	public String parseNewLocationId() {
		return getSecureAttrValue(Xconstants.LID);
	}

	public String parseNewWatchdogId() {
		return getSecureAttrValue(Xconstants.ALGID);
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
	 */
	public User parseUserInfo() {
		try {
			User user = new User();

			mParser.nextTag(); // user
			if (!mParser.getName().equals(Xconstants.USER_ROOT))
				throw new AppException(ClientError.XML);

			user.setId(getSecureAttrValue(Xconstants.USER_UID));
			user.setName(getSecureAttrValue(Xconstants.USER_NAME));
			user.setSurname(getSecureAttrValue(Xconstants.USER_SURNAME));
			user.setEmail(getSecureAttrValue(Xconstants.USER_EMAIL));
			user.setGender(Utils.getEnumFromId(User.Gender.class, getSecureAttrValue(Xconstants.USER_GENDER), User.Gender.UNKNOWN));
			user.setPictureUrl(getSecureAttrValue(Xconstants.USER_IMGURL));

			mParser.nextTag(); // providers
			if (!mParser.getName().equals(Xconstants.USER_PROVIDERS_ROOT))
				return user;

			HashMap<String, String> providers = user.getJoinedProviders();

			while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.USER_PROVIDERS_PROVIDER)) { // provider
				providers.put(getSecureAttrValue(Xconstants.NAME), getSecureAttrValue(Xconstants.ID));
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
			String aid = getSecureAttrValue(Xconstants.AID);
			mParser.nextTag(); // dev start tag

			List<Device> result = new ArrayList<>();

			if (!mParser.getName().equals(Xconstants.MODULE))
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

			if (!mParser.getName().equals(Xconstants.MODULE))
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

			if (!mParser.getName().equals(Xconstants.GATE))
				return result;

			do { // go through gates
				String aid = getSecureAttrValue(Xconstants.ID);
				mParser.nextTag(); // dev tag

				parseInnerDevs(result, aid, true);

				mParser.nextTag(); // gate endtag
			} while (!mParser.getName().equals(Xconstants.COM_ROOT) && mParser.nextTag() != XmlPullParser.END_TAG);

			return result;
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	private void parseInnerDevs(List<Device> result, String aid, boolean init) {
		try {
			do { // go through devs (devices)
				String type = getSecureAttrValue(Xconstants.TYPE);
				String address = getSecureAttrValue(Xconstants.DID);

				Device device = Device.createDeviceByType(type, aid, address);
				device.setInitialized(init);
				device.setLocationId(getSecureAttrValue(Xconstants.LID));
				device.setLastUpdate(new DateTime((long) getSecureInt(getSecureAttrValue(Xconstants.TIME)) * 1000, DateTimeZone.UTC));
				device.setPairedTime(new DateTime((long) getSecureInt(getSecureAttrValue(Xconstants.INVOLVED)) * 1000, DateTimeZone.UTC));

				// Load modules values
				while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.MODULE)) {
					// go through modules
					String moduleId = getSecureAttrValue(Xconstants.ID);
					String moduleValue = getSecureAttrValue(Xconstants.VALUE);
					device.setModuleValue(moduleId, moduleValue);

					mParser.nextTag(); // module endtag
				}

				result.add(device);
			} while (mParser.nextTag() != XmlPullParser.END_TAG
					&& (!mParser.getName().equals(Xconstants.GATE) || !mParser.getName().equals(Xconstants.COM_ROOT)));
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
	public List<User> parseConAccountList() {
		try {
			mParser.nextTag();
			mParser.require(XmlPullParser.START_TAG, ns, Xconstants.USER_ROOT);

			List<User> result = new ArrayList<>();
			do {
				User user = new User();
				user.setId(getSecureAttrValue(Xconstants.ID));
				user.setEmail(getSecureAttrValue(Xconstants.USER_EMAIL));
				user.setName(getSecureAttrValue(Xconstants.NAME));
				user.setSurname(getSecureAttrValue(Xconstants.USER_SURNAME));
				user.setRole(Utils.getEnumFromId(User.Role.class, getSecureAttrValue(Xconstants.ROLE), User.Role.Guest));
				user.setGender(Utils.getEnumFromId(User.Gender.class, getSecureAttrValue(Xconstants.USER_GENDER), User.Gender.UNKNOWN));
				user.setPictureUrl(getSecureAttrValue(Xconstants.USER_IMGURL));

				result.add(user);
				mParser.nextTag();

			} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

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

			if (!mParser.getName().equals(Xconstants.NOTIFICATION))
				return result;

			do {
				VisibleNotification ntfc;

				String name = getSecureAttrValue(Xconstants.NAME); // name
				String id = getSecureAttrValue(Xconstants.MID); // message id
				String time = getSecureAttrValue(Xconstants.TIME); // time
				String type = getSecureAttrValue(Xconstants.TYPE); // type
				boolean read = (getSecureInt(getSecureAttrValue(Xconstants.READ)) != 0); // read

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
			getSecureAttrValue(Xconstants.ATYPE); // not used yet

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