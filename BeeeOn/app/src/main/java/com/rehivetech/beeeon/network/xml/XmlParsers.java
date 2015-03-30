/**
 * 
 */
package com.rehivetech.beeeon.network.xml;

import android.content.Context;
import android.util.Xml;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.WatchDog;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.DeviceLog;
import com.rehivetech.beeeon.adapter.device.DeviceType;
import com.rehivetech.beeeon.adapter.device.Facility;
import com.rehivetech.beeeon.adapter.device.RefreshInterval;
import com.rehivetech.beeeon.adapter.location.Location;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.gcm.Notification;
import com.rehivetech.beeeon.gcm.Notification.ActionType;
import com.rehivetech.beeeon.household.User;
import com.rehivetech.beeeon.network.xml.action.Action;
import com.rehivetech.beeeon.network.xml.action.ComplexAction;
import com.rehivetech.beeeon.network.xml.condition.BetweenFunc;
import com.rehivetech.beeeon.network.xml.condition.ChangeFunc;
import com.rehivetech.beeeon.network.xml.condition.Condition;
import com.rehivetech.beeeon.network.xml.condition.ConditionFunction;
import com.rehivetech.beeeon.network.xml.condition.DewPointFunc;
import com.rehivetech.beeeon.network.xml.condition.EqualFunc;
import com.rehivetech.beeeon.network.xml.condition.GreaterEqualFunc;
import com.rehivetech.beeeon.network.xml.condition.GreaterThanFunc;
import com.rehivetech.beeeon.network.xml.condition.LesserEqualFunc;
import com.rehivetech.beeeon.network.xml.condition.LesserThanFunc;
import com.rehivetech.beeeon.util.Log;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * @author ThinkDeep
 * 
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
	public enum State {
		ADAPTERS("adapters"),
		ALLDEVICES("alldevs"),
		DEVICES("devs"),
		LOGDATA("logdata"),
		ACCOUNTS("accounts"),
		TRUE("true"),
		FALSE("false"),
		VIEWS("views"),
		TIMEZONE("timezone"),
		ROOMS("rooms"),
		ROOMCREATED("roomid"),
		NOTIFICATIONS("notifs"),
		CONDITIONCREATED("condcreated"),
		CONDITION("cond"),
		CONDITIONS("conds"),
		ACTIONCREATED("actcreated"),
		ACTIONS("acts"),
		ACTION("act"),
        BT("bt"),
        ALGCREATED("algcreated"),
        USERINFO("userinfo"),
        ALGORITHMS("algs");

		private final String mValue;

		private State(String value) {
			mValue = value;
		}

		public String getValue() {
			return mValue;
		}

		public static State fromValue(String value) {
			for (State item : values()) {
				if (value.equalsIgnoreCase(item.getValue()))
					return item;
			}
			throw new IllegalArgumentException("Invalid State value");
		}
	}

	public XmlParsers() {
	}

	/**
	 * Method parse message (XML) in communication version
	 * 
	 * @param xmlInput
	 * @param namespace
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException
	 *             means Communication version mismatch exception
	 * @throws ParseException
	 */
	public ParsedMessage parseCommunication(String xmlInput, boolean namespace) throws XmlPullParserException, IOException,
			ParseException {
		mParser = Xml.newPullParser();
		mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, namespace);

		mParser.setInput(new ByteArrayInputStream(xmlInput.getBytes("UTF-8")), null);
		mParser.nextTag();

		mParser.require(XmlPullParser.START_TAG, ns, Xconstants.COM_ROOT);

		State state = State.fromValue(getSecureAttrValue(Xconstants.STATE));
		String version = getSecureAttrValue(Xconstants.VERSION);

		if (!version.equals(COM_VER)) {
			throw new AppException(NetworkError.COM_VER_MISMATCH)
				.set("Expected", COM_VER)
				.set("Real", version);
		}

		ParsedMessage result = new ParsedMessage(state);

		switch (state) {
		case USERINFO:
			// String (userID)
            User user = new User();
			user.setId(getSecureAttrValue(Xconstants.UID));
			user.setName(getSecureAttrValue(Xconstants.NAME));
            user.setSurname(getSecureAttrValue(Xconstants.SURNAME));
            user.setEmail(getSecureAttrValue(Xconstants.EMAIL));
            user.setGender(User.Gender.fromString(getSecureAttrValue(Xconstants.GENDER)));
            user.setPictureUrl(getSecureAttrValue(Xconstants.IMGURL));

			result.data = user;
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
			result.data = parseFalse(); // FIXME: in
			break;
		case ADAPTERS:
			// List<Adapter>
			result.data = parseAdaptersReady();
			break;
		case LOGDATA:
			// DeviceLog
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
            // ArrayList<WatchDog>
            result.data = parseWatchDog();
            break;
		case VIEWS:
			// List<CustomViewPair>
			result.data = parseViewsList(); // TODO: PENDING (need ROB)
			break;
		case ACCOUNTS:
			// List<User>
			result.data = parseConAccountList();
			break;
		case TIMEZONE:
			// integer
			result.data = getSecureInt(getSecureAttrValue(Xconstants.UTC));
			break;
		case CONDITIONCREATED:
			// String
			result.data = getSecureAttrValue(Xconstants.CID);
			break;
		case CONDITION:
			// Condition
			result.data = parseCondition();
			break;
		case DEVICES:
			// TODO: this is workaround in v2.2 before demo, will be do better in v2.2+ if causing problems
			String aid = getSecureAttrValue(Xconstants.AID);
			if (aid.length() > 0) {
				// List<Facility>
				result.data = parseNewFacilities(aid);
			} else
				// List<Facility>
				result.data = parseFacilities();
			break;
		case ALLDEVICES:
			// List<Facility>
			result.data = parseAllFacilities();
			break;
		case CONDITIONS:
			// List<Condition>
			result.data = parseConditions();
			break;
		case ACTIONCREATED:
			// String
			result.data = getSecureAttrValue(Xconstants.ACID);
			break;
		case ACTIONS:
			// List<ComplexAction>
			result.data = parseActions();
			break;
		case ACTION:
			// ComplexAction
			result.data = parseAction();
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

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////SIGNIN,SIGNUP,REGISTRATION,ADAPTERS////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method parse inner part of AdaptersReady message
	 * 
	 * @return List of adapters (contains only Id, name, and user role)
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @since 2.2
	 */
	private List<Adapter> parseAdaptersReady() throws XmlPullParserException, IOException {
		mParser.nextTag();
		List<Adapter> result = new ArrayList<Adapter>();

		if (!mParser.getName().equals(Xconstants.ADAPTER))
			return result;

		do {
			Adapter adapter = new Adapter();
			adapter.setId(getSecureAttrValue(Xconstants.ID));
			adapter.setName(getSecureAttrValue(Xconstants.NAME));
			adapter.setRole(User.Role.fromString(getSecureAttrValue(Xconstants.ROLE)));
			adapter.setUtcOffset(getSecureInt(getSecureAttrValue(Xconstants.UTC)));
			result.add(adapter);

			mParser.nextTag();

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

		return result;
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
		Object trouble = null;
		int err = getSecureInt(getSecureAttrValue(Xconstants.ERRCODE));
		if (err == 10) { // TODO: check this with pavel
			trouble = getFalseMessage10();
		}
		if (err == 17) { // TODO: check this with pavel
			trouble = getFalseMessage17();
		}
		if (err == 3){
			trouble = getFalseMessage17();
		}
		return new FalseAnswer((mParser.getEventType() == XmlPullParser.END_TAG)? "" : readText(Xconstants.COM_ROOT), err, trouble);
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////DEVICES, LOGS/////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method parse inner part of AllDevice message (old:XML message (using parsePartial()))
	 * 
	 * @return list of facilities
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws ParseException
	 */
	private List<Facility> parseAllFacilities() throws XmlPullParserException, IOException, ParseException {

		String aid = getSecureAttrValue(Xconstants.AID);
		mParser.nextTag(); // dev start tag

		List<Facility> result = new ArrayList<Facility>();

		if (!mParser.getName().equals(Xconstants.DEVICE))
			return result;

		parseInnerDevs(result, aid, true);

		return result;
	}

	// special case of parseFacility
	private List<Facility> parseNewFacilities(String aid) throws XmlPullParserException, IOException, ParseException {
		mParser.nextTag(); // dev start tag

		List<Facility> result = new ArrayList<Facility>();

		if (!mParser.getName().equals(Xconstants.DEVICE))
			return result;

		parseInnerDevs(result, aid, false);

		return result;
	}

	/**
	 * Method parse inner part of Device message (old:Partial message (set of device's tag))
	 * 
	 * @return List of facilities
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws ParseException
	 */
	private List<Facility> parseFacilities() throws XmlPullParserException, IOException, ParseException {
		mParser.nextTag(); // adapter tag

		List<Facility> result = new ArrayList<Facility>();

		if (!mParser.getName().equals(Xconstants.ADAPTER))
			return result;

		do { // go through adapters
			String aid = getSecureAttrValue(Xconstants.ID);
			mParser.nextTag(); // dev tag

			parseInnerDevs(result, aid, true);

			mParser.nextTag(); // adapter endtag
			// FIXME: check if it works for request from multiple adapters!!!
		} while (!mParser.getName().equals(Xconstants.COM_ROOT) && mParser.nextTag() != XmlPullParser.END_TAG);

		return result;
	}

	private void parseInnerDevs(List<Facility> result, String aid, boolean init) throws XmlPullParserException, IOException {
		do { // go through devs (facilities)
			Facility facility = new Facility();
			facility.setAdapterId(aid);
			// facility.setInitialized(getSecureAttrValue(Xconstants.INITIALIZED).equals(Xconstants.ZERO) ? false :
			// true);
			facility.setInitialized(init);
			facility.setAddress(getSecureAttrValue(Xconstants.DID));
			facility.setLocationId(getSecureAttrValue(Xconstants.LID));
			facility.setRefresh(RefreshInterval.fromInterval(getSecureInt(getSecureAttrValue(Xconstants.REFRESH))));
			facility.setBattery(getSecureInt(getSecureAttrValue(Xconstants.BATTERY)));
			facility.setLastUpdate(new DateTime((long) getSecureInt(getSecureAttrValue(Xconstants.TIME)) * 1000, DateTimeZone.UTC));
			facility.setInvolveTime(new DateTime((long) getSecureInt(getSecureAttrValue(Xconstants.INVOLVED)) * 1000, DateTimeZone.UTC));
			facility.setNetworkQuality(getSecureInt(getSecureAttrValue(Xconstants.RSSI)));

			mParser.nextTag(); // part tag

			do { // go through parts (devices)
				Device device = DeviceType.createDeviceFromType(getSecureAttrValue(Xconstants.TYPE));
				device.setVisibility(getSecureAttrValue(Xconstants.VISIBILITY).equals(Xconstants.ZERO) ? false : true);
				device.setName(getSecureAttrValue(Xconstants.NAME));
				device.setValue(getSecureAttrValue(Xconstants.VALUE));
				facility.addDevice(device);
				mParser.nextTag(); // part endtag
			} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.DEVICE));

			result.add(facility);

		} while (mParser.nextTag() != XmlPullParser.END_TAG
				&& (!mParser.getName().equals(Xconstants.ADAPTER) || !mParser.getName().equals(Xconstants.COM_ROOT)));
	}

	/**
	 * Method parse inner part of LogData (old:Content.log) message
	 * 
	 * @return List with ContentRow objects
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private DeviceLog parseLogData() throws XmlPullParserException, IOException {
		mParser.nextTag();
		// mParser.require(XmlPullParser.START_TAG, ns, Xconstants.ROW); // strict solution

		DeviceLog log = new DeviceLog();

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
					throw new AppException(NetworkError.XML).set("parts", parts);
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
				throw AppException.wrap(e, NetworkError.XML);
			}
		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

		return log;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////ROOMS//////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

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

		List<Location> result = new ArrayList<Location>();

		if (!mParser.getName().equals(Xconstants.LOCATION))
			return result;

		do {
			Location location = new Location(getSecureAttrValue(Xconstants.ID), getSecureAttrValue(Xconstants.NAME),
					getSecureInt(getSecureAttrValue(Xconstants.TYPE)));
			location.setAdapterId(aid);
			result.add(location);

			mParser.nextTag(); // loc end tag

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

		return result;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////VIEWS//////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method parse inner part of ViewList message
	 * 
	 * @return list of CustomViewPairs
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private List<CustomViewPair> parseViewsList() throws XmlPullParserException, IOException {
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, Xconstants.VIEW);

		List<CustomViewPair> result = new ArrayList<CustomViewPair>();
		do {
			result.add(new CustomViewPair(Integer.parseInt(getSecureAttrValue(Xconstants.ICON)), getSecureAttrValue(Xconstants.NAME)));

			mParser.nextTag();

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

		return result;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////ACCOUNTS///////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

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

		List<User> result = new ArrayList<User>();
		do {
			User user = new User();
			user.setId(getSecureAttrValue(Xconstants.ID));
			user.setEmail(getSecureAttrValue(Xconstants.EMAIL));
			user.setName(getSecureAttrValue(Xconstants.NAME));
			user.setSurname(getSecureAttrValue(Xconstants.SURNAME));
			user.setRole(User.Role.fromString(getSecureAttrValue(Xconstants.ROLE)));
			user.setGender(getSecureAttrValue(Xconstants.GENDER).equals(Xconstants.ZERO) ? User.Gender.Female : User.Gender.Male);
			user.setPictureUrl(getSecureAttrValue(Xconstants.IMGURL));

			result.add(user);
			mParser.nextTag();

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

		return result;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////NOTIFICATIONS//////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	// TODO: need to check
	private List<Notification> parseNotifications() throws XmlPullParserException, IOException {
		mParser.nextTag();
		// mParser.require(XmlPullParser.START_TAG, ns, NOTIFICATION); // strict solution

		List<Notification> result = new ArrayList<Notification>();

		if (!mParser.getName().equals(Xconstants.NOTIFICATION))
			return result;

		do {
			Notification ntfc = new Notification(getSecureAttrValue(Xconstants.MSGID), getSecureAttrValue(Xconstants.TIME),
					getSecureAttrValue(Xconstants.TYPE), (getSecureAttrValue(Xconstants.READ).equals(Xconstants.ZERO)) ? false : true);

			mParser.nextTag();

			if (mParser.getName().equals(Xconstants.MESSAGE)) { // get text from notification if is first tag
				ntfc.setMessage(readText(Xconstants.MESSAGE));
				mParser.nextTag();
			}

			if (mParser.getName().equals(Xconstants.ACTION)) { // get action from notification
				Notification.Action action = ntfc.new Action(getSecureAttrValue(Xconstants.TYPE));

				if (action.getMasterType() == ActionType.WEB) { // get url and should open web or play
					action.setURL(getSecureAttrValue(Xconstants.URL));
					ntfc.setAction(action);
				}

				if (action.getMasterType() == ActionType.APP) { // open some aktivity in app
					mParser.nextTag();

					ActionType tagName = ActionType.fromValue(mParser.getName());
					action.setSlaveType(tagName);
					switch (tagName) {
					case SETTINGS: // open settings {main, account, adapter, location}
						ActionType settings = ActionType.fromValue(Xconstants.SETTINGS + getSecureAttrValue(Xconstants.TYPE));
						action.setSlaveType(settings);
						String stngs = settings.getValue();
						if (stngs.equals(Xconstants.ADAPTER) || stngs.equals(Xconstants.LOCATION)) { // open adapter or
																										// location
																										// settings
							action.setAdapterId(getSecureAttrValue(Xconstants.AID));
							if (stngs.equals(Xconstants.LOCATION)) // open location settings
								action.setLocationId(getSecureAttrValue(Xconstants.LID));
						}
						break;
					case OPENADAPTER: // switch adapter
						action.setAdapterId(getSecureAttrValue(Xconstants.AID));
						break;
					case OPENLOCATION: // open specific location in specific adapter
						action.setAdapterId(getSecureAttrValue(Xconstants.AID));
						action.setLocationId(getSecureAttrValue(Xconstants.LID));
						break;
					case OPENDEVICE: // open detail of specific device in specific adapter
						action.setAdapterId(getSecureAttrValue(Xconstants.AID));
						action.setDeviceId(getSecureAttrValue(Xconstants.DID));
						break;
					default:
						break;
					}
					ntfc.setAction(action);
					mParser.nextTag(); // end of settings or adapter or location or device
				}
				mParser.nextTag(); // action end tag
			}

			if (ntfc.getMessage() == null) { // get text from notification if is second tag
				mParser.nextTag(); // notification end tag or message start tag
				if (mParser.getName().equals(Xconstants.MESSAGE)) {
					ntfc.setMessage(readText(Xconstants.MESSAGE));
					mParser.nextTag(); // notification end tag
				}
			}

			result.add(ntfc);

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

		return result;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////NOTIFICATIONS//////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	// TODO: need to be checked
	private Condition parseCondition() throws XmlPullParserException, IOException {
		// mParser.nextTag();
		// mParser.require(XmlPullParser.START_TAG, ns, Xconstants.CONDITION);
		ArrayList<ConditionFunction> funcs = new ArrayList<ConditionFunction>();

		Condition condition = new Condition("0", "none", getSecureAttrValue(Xconstants.TYPE), funcs); // hope this add
																										// filled list
		// to object
		mParser.nextTag(); // func tag

		do {
			ConditionFunction func = null;
			ConditionFunction.FunctionType type = ConditionFunction.FunctionType.fromValue(getSecureAttrValue(Xconstants.TYPE));
			switch (type) {
			case BTW:
				mParser.nextTag(); // device or value tag
				Device deviceBTW = null;
				String tempValue = null;
				String minValue = "0";
				String maxValue = "1";
				do {
					if (mParser.getName().equals(Xconstants.DEVICE)) {
						String stypeBTW = getSecureAttrValue(Xconstants.TYPE); // device type
						deviceBTW = DeviceType.createDeviceFromType(stypeBTW);
						Facility facilityBTW = new Facility();
						facilityBTW.setAddress(getSecureAttrValue(Xconstants.ID));
						deviceBTW.setFacility(facilityBTW);
						mParser.nextTag();
					}
					if (mParser.getName().equals(Xconstants.VALUE)) {
						String value = readText(Xconstants.VALUE);
						if (tempValue == null) {
							tempValue = value;
						} else {
							if (Integer.parseInt(tempValue) > Integer.parseInt(value)) {
								minValue = value;
								maxValue = tempValue;
							} else {
								minValue = tempValue;
								maxValue = value;
							}
						}
					}
				} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.FUNC));

				func = new BetweenFunc(deviceBTW, minValue, maxValue);
				break;
			case CHG:
				mParser.nextTag(); // device tag

				String stypeCHG = getSecureAttrValue(Xconstants.TYPE);
				Device deviceCHG = DeviceType.createDeviceFromType(stypeCHG);
				Facility facilityCHG = new Facility();
				facilityCHG.setAddress(getSecureAttrValue(Xconstants.ID));
				deviceCHG.setFacility(facilityCHG);
				mParser.nextTag(); // device endtag
				mParser.nextTag(); // func endtag

				func = new ChangeFunc(deviceCHG);
				break;
			case DP:
				mParser.nextTag(); // device tag
				Device tempoDevice = null;
				Device deviceDP_t = null;
				Device deviceDP_h = null;
				do {
					if (mParser.getName().equals(Xconstants.DEVICE)) {
						String stypeDP = getSecureAttrValue(Xconstants.TYPE);
						tempoDevice = DeviceType.createDeviceFromType(stypeDP);
						Facility facilityDP = new Facility();
						facilityDP.setAddress(getSecureAttrValue(Xconstants.ID));
						tempoDevice.setFacility(facilityDP);
						if (tempoDevice.getType() == DeviceType.TYPE_TEMPERATURE) {
							deviceDP_t = tempoDevice;
						} else {
							deviceDP_h = tempoDevice;
						}

						mParser.nextTag();
					}
				} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.FUNC));

				func = new DewPointFunc(deviceDP_t, deviceDP_h);
				break;
			case EQ:
			case GE:
			case GT:
			case LE:
			case LT:
				mParser.nextTag(); // device or value tag
				Device device = null;
				String value = null;
				do {
					if (mParser.getName().equals(Xconstants.DEVICE)) {
						String stype = getSecureAttrValue(Xconstants.TYPE);
						device = DeviceType.createDeviceFromType(stype);
						Facility facility = new Facility();
						facility.setAddress(getSecureAttrValue(Xconstants.ID));
						device.setFacility(facility);
						mParser.nextTag();
					}
					if (mParser.getName().equals(Xconstants.VALUE)) {
						value = readText(Xconstants.VALUE);
					}
				} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.FUNC));
				switch (type) {
				case EQ:
					func = new EqualFunc(device, value);
					break;
				case GE:
					func = new GreaterEqualFunc(device, value);
					break;
				case GT:
					func = new GreaterThanFunc(device, value);
					break;
				case LE:
					func = new LesserEqualFunc(device, value);
					break;
				case LT:
					func = new LesserThanFunc(device, value);
					break;
				default:
					break;
				}
				break;
			case GEO:
				// TODO: this
				break;
			case TIME:
				mParser.nextTag(); // value tag
				value = readText(Xconstants.VALUE); // time
				mParser.nextTag(); // func endtag
				break;
			case UNKNOWN:
				break;
			default:
				break;
			}
			funcs.add(func);
		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.CONDITION));

		return condition;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////ACTIONS,CONDITIONS/////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	// TODO: need to be checked
	private List<ComplexAction> parseActions() throws XmlPullParserException, IOException {
		mParser.nextTag();

		List<ComplexAction> result = new ArrayList<ComplexAction>();

		if (!mParser.getName().equals(Xconstants.ACTION))
			return result;

		do {
			result.add(new ComplexAction(getSecureAttrValue(Xconstants.ID), getSecureAttrValue(Xconstants.NAME)));
			mParser.nextTag();
		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

		return result;
	}

	// TODO: need to be checked
	private ComplexAction parseAction() throws XmlPullParserException, IOException {
		mParser.nextTag(); // action tag

		ComplexAction result = new ComplexAction();
		List<Action> actions = new ArrayList<Action>();

		if (!mParser.getName().equals(Xconstants.ACTION))
			return result;

		do {
			Action.ActionType type = Action.ActionType.fromValue(getSecureAttrValue(Xconstants.TYPE));

			if (type == Action.ActionType.ACTOR) {
				mParser.nextTag(); // dev tag
				do {
					Action action = new Action(type);
					Device device = DeviceType.createDeviceFromType(getSecureAttrValue(Xconstants.TYPE));
					Facility facility = new Facility();
					facility.setAddress(getSecureAttrValue(Xconstants.ID));
					device.setFacility(facility);
					action.setDevice(device);
					action.setValue(getSecureAttrValue(Xconstants.VALUE));
					actions.add(action);
					mParser.nextTag(); // device endtag
				} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.ACTION));
			} else {
				actions.add(new Action(type));
				mParser.nextTag(); // action endtag
			}
		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

		result.setActions(actions);

		return result;
	}

	// TODO: need to be checked
	private List<Condition> parseConditions() throws XmlPullParserException, IOException {
		mParser.nextTag();

		List<Condition> result = new ArrayList<Condition>();

		if (!mParser.getName().equals(Xconstants.CONDITION))
			return result;

		do {
			result.add(new Condition(getSecureAttrValue(Xconstants.ID), getSecureAttrValue(Xconstants.NAME)));
			mParser.nextTag();
		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

		return result;
	}

    private ArrayList<WatchDog> parseWatchDog() throws XmlPullParserException, IOException{
        String aid = getSecureAttrValue(Xconstants.AID);
        mParser.nextTag();

        ArrayList<WatchDog> result = new ArrayList<>();

        if(!mParser.getName().equals(Xconstants.ALGORITHM))
            return result;

        do{
            WatchDog watchDog = new WatchDog(getSecureInt(getSecureAttrValue(Xconstants.ATYPE)));
			watchDog.setId(getSecureAttrValue(Xconstants.ID));
            watchDog.setAdapterId(aid);
            watchDog.setEnabled((getSecureInt(getSecureAttrValue(Xconstants.ENABLE)) > 0)?true:false);
            watchDog.setName(getSecureAttrValue(Xconstants.NAME));

            TreeMap<String, String> tDevices = new TreeMap<>();
            TreeMap<String, String> tParams = new TreeMap<>();

            mParser.nextTag();

            if(!mParser.getName().equals(Xconstants.DEVICE) && !mParser.getName().equals(Xconstants.PARAM) && !mParser.getName().equals(Xconstants.GEOFENCE))
                Log.e(TAG, "someone send bad xml");//TODO do something

            do{
                String position = getSecureAttrValue(Xconstants.POSITION);

                if(mParser.getName().equals(Xconstants.DEVICE)){
					String device = getSecureAttrValue(Xconstants.ID) + Device.ID_SEPARATOR + getSecureAttrValue(Xconstants.TYPE);
                    tDevices.put(position, device);

                    mParser.nextTag();
                }else if(mParser.getName().equals(Xconstants.PARAM)){
                    tParams.put(position, readText(Xconstants.PARAM));
                }else{
					watchDog.setGeoRegionId(getSecureAttrValue(Xconstants.RID));
					watchDog.setGeoDirectionType(getSecureAttrValue(Xconstants.TYPE));
				}

            }while(mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.ALGORITHM));

            watchDog.setDevices(new ArrayList<>(tDevices.values()));
            watchDog.setParams(new ArrayList<>(tParams.values()));

            result.add(watchDog);

        }while(mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

        return result;
    }

	// ///////////////////////////////// OTHER

	// FIXME: check on first use
	List<Facility> getFalseMessage10() throws XmlPullParserException, IOException {

		mParser.nextTag();

		List<Facility> result = new ArrayList<Facility>();

		mParser.nextTag();
		if (!mParser.getName().equals(Xconstants.DEVICE))
			return result;

		do {
			Facility facility = null;
			boolean facilityExists = false;

			Device device = DeviceType.createDeviceFromType(getSecureAttrValue(Xconstants.TYPE));

			String id = getSecureAttrValue(Xconstants.ID);
			for (Facility fac : result) {
				if (fac.getAddress().equals(id)) {
					// We already have this facility, just add new devices to it
					facilityExists = true;
					facility = fac;
					break;
				}
			}

			if (facility == null) {
				// This facility is new, first create a object for it
				facility = new Facility();
				facility.setAddress(id);
			}

			facility.addDevice(device);

			if (!facilityExists)
				result.add(facility);

			mParser.nextTag();

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

		return result;
	}

	// FIXME: check on first use
	List<User> getFalseMessage17() throws XmlPullParserException, IOException {

		//mParser.nextTag();

		List<User> result = new ArrayList<User>();

		mParser.nextTag();
		if (!mParser.getName().equals(Xconstants.USER))
			return result;

		do {
			User user = new User();
			user.setId(getSecureAttrValue(Xconstants.ID));
			user.setEmail(getSecureAttrValue(Xconstants.EMAIL));
			user.setName(getSecureAttrValue(Xconstants.NAME));
			user.setSurname(getSecureAttrValue(Xconstants.SURNAME));
			user.setRole(User.Role.fromString(getSecureAttrValue(Xconstants.ROLE)));
			user.setGender(getSecureAttrValue(Xconstants.GENDER).equals(Xconstants.ZERO) ? User.Gender.Female : User.Gender.Male);
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
	 * @param tag
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
	 * @param name
	 *            of the attribute
	 * @return parsed attribute or empty string
	 */
	private String getSecureAttrValue(String name) {
		String result = mParser.getAttributeValue(ns, name);
		return (result == null) ? "" : result;
	}

	/**
	 * Method return integer value of string, or zero if length is 0
	 * 
	 * @param value
	 * @return integer value of zero if length is 0
	 */
	private int getSecureInt(String value) {
		return (value.length() < 1) ? 0 : Integer.parseInt(value);
	}

	// /////////////////////////////// DEMO

	/**
	 * Factory for parsing adapter from asset.
	 * 
	 * @param context
	 * @param filename
	 * @return Adapter or null
	 */
	public List<Facility> getDemoFacilitiesFromAsset(Context context, String filename) throws AppException {
		Log.i(TAG, String.format("Loading adapter from asset '%s'", filename));
		List<Facility> result = null;
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(context.getAssets().open(filename));
			mParser = Xml.newPullParser();
			mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			mParser.setInput(stream, null);
			mParser.nextTag();

			String version = getSecureAttrValue(Xconstants.VERSION);
			if (!version.equals(COM_VER)) {
				throw new AppException(NetworkError.COM_VER_MISMATCH)
					.set("Expected", COM_VER)
					.set("Real", version);
			}

			result = parseAllFacilities();
		} catch (IOException | XmlPullParserException | ParseException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException ioe) {
				Log.e(TAG, ioe.getMessage(), ioe);
			}
		}
		return result;
	}

	/**
	 * Factory for parsing locations from asset.
	 * 
	 * @param context
	 * @param filename
	 * @return list of locations or empty list
	 */
	public List<Location> getDemoLocationsFromAsset(Context context, String filename) throws AppException {
		Log.i(TAG, String.format("Loading locations from asset '%s'", filename));
		List<Location> locations = new ArrayList<Location>();
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(context.getAssets().open(filename));
			mParser = Xml.newPullParser();
			mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			mParser.setInput(stream, null);
			mParser.nextTag();

			String version = getSecureAttrValue(Xconstants.VERSION);
			if (!version.equals(COM_VER)) {
				throw new AppException(NetworkError.COM_VER_MISMATCH)
					.set("Expected", COM_VER)
					.set("Real", version);
			}

			locations = parseRooms();
		} catch (IOException | XmlPullParserException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException ioe) {
				Log.e(TAG, ioe.getMessage(), ioe);
			}
		}
		return locations;
	}

	/**
	 * Factory for parsing list of adapters from asset
	 * 
	 * @param context
	 * @param filename
	 * @return list of adapters or empty list
	 */
	public List<Adapter> getDemoAdaptersFromAsset(Context context, String filename) throws AppException {
		Log.i(TAG, String.format("Loading adapters from asset '%s'", filename));
		List<Adapter> adapters = new ArrayList<Adapter>();
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(context.getAssets().open(filename));
			mParser = Xml.newPullParser();
			mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			mParser.setInput(stream, null);
			mParser.nextTag();

			String version = getSecureAttrValue(Xconstants.VERSION);
			if (!version.equals(COM_VER)) {
				throw new AppException(NetworkError.COM_VER_MISMATCH)
					.set("Expected", COM_VER)
					.set("Real", version);
			}

			adapters = parseAdaptersReady();
		} catch (IOException | XmlPullParserException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException ioe) {
				Log.e(TAG, ioe.getMessage(), ioe);
			}
		}
		return adapters;
	}

	public List<WatchDog> getDemoWatchDogsFromAsset(Context context, String filename) throws AppException {
		Log.i(TAG, String.format("Loading watchdog from asset '%s'", filename));
		List<WatchDog> watchdogs = new ArrayList<WatchDog>();
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(context.getAssets().open(filename));
			mParser = Xml.newPullParser();
			mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			mParser.setInput(stream, null);
			mParser.nextTag();

			String version = getSecureAttrValue(Xconstants.VERSION);
			if (!version.equals(COM_VER)) {
				throw new AppException(NetworkError.COM_VER_MISMATCH)
						.set("Expected", COM_VER)
						.set("Real", version);
			}

			watchdogs = parseWatchDog();
		} catch (IOException | XmlPullParserException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException ioe) {
				Log.e(TAG, ioe.getMessage(), ioe);
			}
		}
		return watchdogs;
	}
}