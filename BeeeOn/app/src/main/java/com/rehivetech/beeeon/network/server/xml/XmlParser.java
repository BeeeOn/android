package com.rehivetech.beeeon.network.server.xml;

import android.support.annotation.NonNull;
import android.util.Xml;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ClientError;
import com.rehivetech.beeeon.gcm.notification.VisibleNotification;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceType;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.gate.GateInfo;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.GpsData;
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
import java.util.Map;

import timber.log.Timber;

/**
 * @author ThinkDeep
 * @author Robyer
 */
public class XmlParser {

	public static final String DEFAULT_CHARSET = "UTF-8";
	private static final String ns = null;

	private XmlPullParser mParser;

	private String mNamespace;
	private String mType;
	private String mVersion;
	private Result mResult;
	private int mErrorCode = -1;

	public enum Result implements IIdentifier {
		OK,
		DATA,
		ERROR;

		public String getId() {
			return this.name().toLowerCase();
		}
	}

	private XmlParser(@NonNull String xmlInput) throws XmlPullParserException, UnsupportedEncodingException {
		mParser = Xml.newPullParser();
		mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		mParser.setInput(new ByteArrayInputStream(xmlInput.getBytes(DEFAULT_CHARSET)), null);
	}

	public static XmlParser parse(@NonNull String xmlInput) throws AppException {
		XmlParser parser;
		try {
			parser = new XmlParser(xmlInput);
			parser.parseRoot();
		} catch (XmlPullParserException | IOException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
		return parser;
	}

	private void parseRoot() throws IOException, XmlPullParserException, AppException {
		mParser.nextTag();

		mParser.require(XmlPullParser.START_TAG, ns, "response");

		mNamespace = getSecureAttributeString("ns");
		mType = getSecureAttributeString("type");
		mVersion = getSecureAttributeString("version");
		mResult = Utils.getEnumFromId(Result.class, getSecureAttributeString("result"), Result.ERROR);
		mErrorCode = (mResult == Result.ERROR) ? getSecureAttributeInt("errcode", -1) : -1;
	}

	public String getNamespace() {
		return mNamespace;
	}

	public String getType() {
		return mType;
	}

	public Result getResult() {
		return mResult;
	}

	public String getVersion() {
		return mVersion;
	}

	public int getErrorCode() {
		return mErrorCode;
	}

	// /////////////////////////////////SIMPLE RESULTS///////////////////////////////////////////

	public String parseSessionId() {
		try {
			mParser.nextTag();
			if (!mParser.getName().equals("session"))
				throw new AppException(ClientError.XML);

			return getSecureAttributeString("id");
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
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
				gate.setRole(Utils.getEnumFromId(User.Role.class, getSecureAttributeString("permission"), User.Role.Guest));
				gate.setUtcOffset(getSecureAttributeInt("timezone", 0));
				result.add(gate);

				mParser.nextTag();

			} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals("response"));

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
			User.Role role = Utils.getEnumFromId(User.Role.class, getSecureAttributeString("permission"), User.Role.Guest);
			String name = getSecureAttributeString("name");
			String owner = getSecureAttributeString("owner");
			int devicesCount = getSecureAttributeInt("devices", -1);
			int usersCount = getSecureAttributeInt("users", -1);
			String ip = getSecureAttributeString("ip");
			String version = getSecureAttributeString("version");
			int utcOffsetInMinutes = getSecureAttributeInt("timezone", 0);

			GpsData gpsData = new GpsData();
			gpsData.setAltitude(getSecureAttributeInt("altitude", -1));
			gpsData.setLongitude(getSecureAttributeDouble("longitude", -1));
			gpsData.setLatitude(getSecureAttributeDouble("latitude", -1));

			return new GateInfo(id, name, owner, role, utcOffsetInMinutes, devicesCount, usersCount, version, ip, gpsData);
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method parse inner part of UserInfo message
	 * @return User object
	 */
	public User parseGetMyProfile() {
		try {
			mParser.nextTag(); // user
			if (!mParser.getName().equals("user"))
				throw new AppException(ClientError.XML);

			User user = new User();
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
	 * Method parse inner part of Module message (old:Partial message (set of module's tag))
	 *
	 * @return List of devices
	 */
	public List<Device> parseDevices() {
		Timber.d("received device list...");

		try {
			mParser.nextTag(); // device start tag
			List<Device> result = new ArrayList<>();

			if (!mParser.getName().equals("device"))
				return result;

			do { // go through devices
				String type = getSecureAttributeString("type");
				String address = getSecureAttributeString("euid");
				String gateId = getSecureAttributeString("gateid");

				String customName = getSecureAttributeString("name");
				String locationId = getSecureAttributeString("locationid");
				long time = getSecureAttributeInt("time", 0) * 1000L;
				long involved = getSecureAttributeInt("involved", 0) * 1000L;
				String status = getSecureAttributeString("status");

				DeviceType deviceType = DeviceType.getById(type);
				boolean typeFinished = true;

				if (deviceType == DeviceType.getUnknown()) {
					final String name = getSecureAttributeString("type_name");

					Timber.d("learning device type: " + name);

					deviceType = new DeviceType(
						type,
						name,
						getSecureAttributeString("name"),
						getSecureAttributeString("vendor"),
						new ArrayList<Module.Factory>());

					typeFinished = false;
				}

				List<Module.Factory> modules = new ArrayList<Module.Factory>();
				Map<String, String> moduleValueMap = new HashMap<>();
				Map<String, String> moduleStatusMap = new HashMap<>();

				// Load modules values
				while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals("device")) {
					// go through modules
					String moduleId = getSecureAttributeString("id");
					String moduleValue = getSecureAttributeString("value");
					String moduleStatus = getSecureAttributeString("status");
					moduleValueMap.put(moduleId, moduleValue);
					moduleStatusMap.put(moduleId, moduleStatus);

					if (!typeFinished) {
						Timber.d("learning module type: " + moduleId);

						if (!getSecureAttributeString("type-enum-values").equals("")) {
							ArrayList<EnumValue.Item> items = new ArrayList<>();
							final String raw = getSecureAttributeString("type-enum-values");
							int i = 0;

							Timber.d("parsing enum values: " + raw);

							for (final String pair : raw.split(";")) {
								if (pair.length() == 0) {
									i += 1;
									continue;
								}

								final String[] parts = pair.split(":");
								if (parts.length != 2) {
									Timber.e("invalid enum pair: " + pair
										+ " (" + Integer.toString(parts.length) + ")");
									i += 1;
									continue;
								}

								items.add(new EnumValue.Item(i++, parts[0], parts[1]));
							}

							Timber.d("learned module enum type: "
									+ raw + " (" + Integer.toString(items.size()) + ")");

							modules.add(new Module.Factory(
								moduleId,
								getSecureAttributeInt("type", -1),
								null,
								null,
								getSecureAttributeString("name"),
								getSecureAttributeString("actuator").equals("yes"),
								null,
								items,
								null
							));
						}
						else if (!getSecureAttributeString("type-range-max").equals("")) {
							final double max = getSecureAttributeDouble("type-range-max", Double.NaN);
							final double min = getSecureAttributeDouble("type-range-min", Double.NaN);
							final double step = getSecureAttributeDouble("type-range-step", 1);

							Timber.d("learned module constrained type: "
									+ Double.toString(min) + ".." + Double.toString(max)
									+ " (" + Double.toString(step) + ")");

							modules.add(new Module.Factory(
								moduleId,
								getSecureAttributeInt("type", -1),
								null,
								null,
								getSecureAttributeString("name"),
								getSecureAttributeString("actuator").equals("yes"),
								null,
								new BaseValue.Constraints(min, max, step),
								null
							));
						}
						else {
							Timber.d("skipped unspecified module type " + moduleId);
						}
					}

					mParser.nextTag(); // module endtag
				}

				if (!typeFinished) {
					deviceType.setModuleFactory(modules);
					DeviceType.registerType(deviceType, true);
					Timber.d("learned new device type " + deviceType.getId());
				}

				Device device = Device.createDevice(deviceType, gateId, address);

				device.setCustomName(customName);
				device.setLocationId(locationId);
				device.setLastUpdate(new DateTime(time, DateTimeZone.UTC));
				// PairedTime is not used always...
				device.setPairedTime(new DateTime(involved, DateTimeZone.UTC));
				//noinspection ResourceType
				device.setStatus(status);

				for (Map.Entry<String, String> entry : moduleValueMap.entrySet()) {
					String moduleStatus = moduleStatusMap.get(entry.getKey());
					device.setModuleValue(entry.getKey(), entry.getValue(), moduleStatus);
				}

				result.add(device);
			} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals("response"));

			Timber.d("parsed " + Integer.toString(result.size()) + " devices");
			return result;
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
						Timber.e("Wrong number of parts (%d) of data: %s", parts.length, row);
						throw new AppException(ClientError.XML).set("parts", parts);
					}

					// Parse values
					Long dateMillis = Long.parseLong(parts[0]) * 1000;
					Float value;

					if (parts[1].equalsIgnoreCase("nan"))
						value = Float.NaN;
					else
						value = Float.parseFloat(parts[1]);

					if (!repeat.isEmpty() && !interval.isEmpty()) {
						log.addValueInterval(dateMillis, value, Integer.parseInt(repeat), Integer.parseInt(interval));
					} else {
						log.addValue(dateMillis, value);
					}
				} catch (NumberFormatException e) {
					throw AppException.wrap(e, ClientError.XML);
				}
			} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals("response"));

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

			} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals("response"));

			return result;
		} catch (IOException | XmlPullParserException e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	public String parseNewLocationId() {
		try {
			mParser.nextTag();
			// TODO: Use whole returned location element as it contains all info about (new) location
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
				user.setRole(Utils.getEnumFromId(User.Role.class, getSecureAttributeString("permission"), User.Role.Guest)); // FIXME: probably this should have different name
				user.setName(getSecureAttributeString("name"));
				user.setSurname(getSecureAttributeString("surname"));
				user.setGender(Utils.getEnumFromId(User.Gender.class, getSecureAttributeString("gender"), User.Gender.UNKNOWN));
				user.setPictureUrl(getSecureAttributeString("imgurl"));

				result.add(user);
				mParser.nextTag();

			} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals("response"));

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
				boolean read = (getSecureAttributeInt("read", 0) != 0);

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

			} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals("response"));

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
			Timber.d("Skipping unknown child '%s'", mParser.getName());
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
	 * @param tag name of element to process
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
	 * Method return int value of string, or defaultValue if number can't be parsed.
	 *
	 * @param name Name of the attribute
	 * @param defaultValue Return this value when number can't be parsed
	 * @return int value of attribute or defaultValue
	 */
	private int getSecureAttributeInt(String name, int defaultValue) {
		String value = mParser.getAttributeValue(ns, name);
		return Utils.parseIntSafely(value, defaultValue);
	}

	/**
	 * Method return double value of string, or defaultValue if number can't be parsed.
	 *
	 * @param name Name of the attribute
	 * @param defaultValue Return this value when number can't be parsed
	 * @return double value of attribute or defaultValue
	 */
	private double getSecureAttributeDouble(String name, double defaultValue) {
		String value = mParser.getAttributeValue(ns, name);
		return Utils.parseDoubleSafely(value, defaultValue);
	}
}
