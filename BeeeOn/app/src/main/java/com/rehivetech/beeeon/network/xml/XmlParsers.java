package com.rehivetech.beeeon.network.xml;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Xml;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ClientError;
import com.rehivetech.beeeon.gcm.notification.VisibleNotification;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.gate.GateInfo;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
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

		ALLDEVICES("alldevices"),
		LOGDATA("logs"),
		LOCATIONS("locations"),
		LOCATIONID("locationid"),
		ACCOUNTS("accounts"),

		TRUE("true"),
		FALSE("false"),

		VIEWS("views"),
		NOTIFICATIONS("notifications");

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

		String state = getSecureAttributeString("state");
		String version = getSecureAttributeString("version");
		String errorCode = getSecureAttributeString("errcode");

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
		return getSecureAttributeString("sessionid");
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
				Gate gate = new Gate(getSecureAttributeString("id"), getSecureAttributeString("name"));
				gate.setRole(Utils.getEnumFromId(User.Role.class, getSecureAttributeString("role"), User.Role.Guest));
				gate.setUtcOffset(getSecureAttributeInt("utc"));
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

			String id = getSecureAttributeString("id");
			User.Role role = Utils.getEnumFromId(User.Role.class, getSecureAttributeString("accessrole"), User.Role.Guest);
			String name = getSecureAttributeString("name");
			int devicesCount = getSecureAttributeInt("devices");
			int usersCount = getSecureAttributeInt("users");
			String ip = getSecureAttributeString("ip");
			String version = getSecureAttributeString("version");
			int utcOffsetInMinutes = getSecureAttributeInt("utc");

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

			user.setId(getSecureAttributeString("id"));
			user.setName(getSecureAttributeString("name"));
			user.setSurname(getSecureAttributeString("surname"));
			user.setGender(Utils.getEnumFromId(User.Gender.class, getSecureAttributeString("gender"), User.Gender.UNKNOWN));
			user.setEmail(getSecureAttributeString("email"));
			user.setPictureUrl(getSecureAttributeString("imgurl"));

			mParser.nextTag(); // providers
			if (!mParser.getName().equals("providers"))
				return user;

			HashMap<String, String> providers = user.getJoinedProviders();

			while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals("provider")) { // provider
				providers.put(getSecureAttributeString("name"), getSecureAttributeString("id"));
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
			mParser.nextTag(); // device start tag

			if (!mParser.getName().equals("device"))
				return new ArrayList<>();

			return parseInnerDevices();
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	// special case of parseDevice
	public List<Device> parseNewDevices() {
		try {
			mParser.nextTag(); // device start tag

			if (!mParser.getName().equals("device"))
				return new ArrayList<>();

			return parseInnerDevices();
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
			mParser.nextTag(); // device start tag

			if (!mParser.getName().equals("device"))
				return new ArrayList<>();

			return parseInnerDevices();
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	private List<Device> parseInnerDevices() {
		List<Device> result = new ArrayList<>();

		try {
			do { // go through devices
				String type = getSecureAttributeString("type");
				String address = getSecureAttributeString("id");
				String gateId = getSecureAttributeString("gateid");

				Device device = Device.createDeviceByType(type, gateId, address);

				device.setCustomName(getSecureAttributeString("name"));
				device.setLocationId(getSecureAttributeString("locationid"));
				device.setLastUpdate(new DateTime((long) getSecureAttributeInt("time") * 1000, DateTimeZone.UTC));
				// PairedTime is not used always...
				device.setPairedTime(new DateTime((long) getSecureAttributeInt("involved") * 1000, DateTimeZone.UTC));

				// FIXME: Temporary workaround
				int refresh = getSecureAttributeInt("refresh");
				if (refresh > 0) {
					device.setRefresh(RefreshInterval.fromInterval(refresh));
				}

				// Load modules values
				while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals("device")) {
					// go through modules
					String moduleId = getSecureAttributeString("id");
					String moduleValue = getSecureAttributeString("value");
					device.setModuleValue(moduleId, moduleValue);

					mParser.nextTag(); // module endtag
				}

				result.add(device);
			} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals("com"));
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
		}

		return result;
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
					String repeat = getSecureAttributeString("repeat");
					String interval = getSecureAttributeString("interval");
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
			String gateId = getSecureAttributeString("gateid");
			mParser.nextTag();

			List<Location> result = new ArrayList<>();

			if (!mParser.getName().equals("location"))
				return result;

			do {
				String id = getSecureAttributeString("id");
				String type = getSecureAttributeString("type");
				String name = getSecureAttributeString("name");
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

			return getSecureAttributeString("id");
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
				user.setId(getSecureAttributeString("id"));
				user.setEmail(getSecureAttributeString("email"));
				user.setRole(Utils.getEnumFromId(User.Role.class, getSecureAttributeString("role"), User.Role.Guest)); // FIXME: probably this should have different name
				user.setName(getSecureAttributeString("name"));
				user.setSurname(getSecureAttributeString("surname"));
				user.setGender(Utils.getEnumFromId(User.Gender.class, getSecureAttributeString("gender"), User.Gender.UNKNOWN));
				user.setPictureUrl(getSecureAttributeString("imgurl")); // FIXME: this isn't in specification but it should be here...

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

				String name = getSecureAttributeString("name");
				String id = getSecureAttributeString("mid");
				String time = getSecureAttributeString("time");
				String type = getSecureAttributeString("type");
				boolean read = (getSecureAttributeInt("read") != 0);

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
	private String getSecureAttributeString(String name) {
		String result = mParser.getAttributeValue(ns, name);
		return (result == null) ? "" : result;
	}

	/**
	 * Method return integer value of string, or zero if length is 0
	 *
	 * @param name of the attribute
	 * @return integer value of attribute or zero if empty
	 */
	private int getSecureAttributeInt(String name) {
		String value = getSecureAttributeString(name);
		return (value.length() < 1) ? 0 : Integer.parseInt(value);
	}
}