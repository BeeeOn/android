/**
 * 
 */
package cz.vutbr.fit.iha.network.xml;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;
import android.util.Xml;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.device.DeviceLog;
import cz.vutbr.fit.iha.adapter.device.DeviceType;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.adapter.device.RefreshInterval;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.gcm.Notification;
import cz.vutbr.fit.iha.gcm.Notification.ActionType;
import cz.vutbr.fit.iha.household.User;
import cz.vutbr.fit.iha.network.xml.action.Action;
import cz.vutbr.fit.iha.network.xml.action.ComplexAction;
import cz.vutbr.fit.iha.network.xml.condition.BetweenFunc;
import cz.vutbr.fit.iha.network.xml.condition.ChangeFunc;
import cz.vutbr.fit.iha.network.xml.condition.Condition;
import cz.vutbr.fit.iha.network.xml.condition.ConditionFunction;
import cz.vutbr.fit.iha.network.xml.condition.DewPointFunc;
import cz.vutbr.fit.iha.network.xml.condition.EqualFunc;
import cz.vutbr.fit.iha.network.xml.condition.GreaterEqualFunc;
import cz.vutbr.fit.iha.network.xml.condition.GreaterThanFunc;
import cz.vutbr.fit.iha.network.xml.condition.LesserEqualFunc;
import cz.vutbr.fit.iha.network.xml.condition.LesserThanFunc;
import cz.vutbr.fit.iha.network.xml.exception.ComVerMisException;
import cz.vutbr.fit.iha.network.xml.exception.XmlVerMisException;

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
		ROOMCREATED("roomcreated"),
		NOTIFICATIONS("notifs"),
		CONDITIONCREATED("condcreated"),
		CONDITION("cond"),
		CONDITIONS("conds"),
		ACTIONCREATED("actcreated"),
		ACTIONS("acts"),
		ACTION("act");

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

	// exception
	private static final String mComVerMisExcMessage = "Communication version mismatch.";

	public XmlParsers() {}

	/**
	 * Method parse message (XML) in communication version
	 * 
	 * @param xmlInput
	 * @param namespace
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws ComVerMisException
	 *             means Communication version mismatch exception
	 * @throws XmlVerMisException
	 *             means XML version mismatch exception
	 * @throws ParseException
	 */
	public ParsedMessage parseCommunication(String xmlInput, boolean namespace) throws XmlPullParserException,
			IOException, ComVerMisException, XmlVerMisException, ParseException {
		mParser = Xml.newPullParser();
		mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, namespace);

		mParser.setInput(new ByteArrayInputStream(xmlInput.getBytes("UTF-8")), null);
		mParser.nextTag();

		mParser.require(XmlPullParser.START_TAG, ns, Xconstants.COM_ROOT);

		State state = State.fromValue(getSecureAttrValue(Xconstants.STATE));
		String version = getSecureAttrValue(Xconstants.VERSION);

		if (!version.equals(COM_VER))
			throw new ComVerMisException(mComVerMisExcMessage + "Expected: " + COM_VER + " but got: " + version);

		ParsedMessage result = new ParsedMessage(state);

		switch (state) {
		case TRUE:
			// String (sessionID)
			result.data = getSecureAttrValue(Xconstants.SID);
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
	// /////////////////////////////////SIGNIN,SIGNUP,REGISTRATION,Xconstants.ADAPTERS////////////////////////////////////
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
			trouble = getFalseMessage6();
		}
		if (err == 17) { // TODO: check this with pavel
			trouble = getFalseMessage13();
		}
		return new FalseAnswer(readText(Xconstants.COM_ROOT), err, trouble);
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////Xconstants.DEVICES, LOGS/////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Method parse inner part of AllDevice message (old:XML message (using parsePartial()))
	 * 
	 * @return list of facilities
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws XmlVerMisException
	 *             means XML version mismatch exception
	 * @throws ParseException
	 */
	private List<Facility> parseAllFacilities() throws XmlPullParserException, IOException, XmlVerMisException,
			ParseException {

		String aid = getSecureAttrValue(Xconstants.AID);
		mParser.nextTag(); // dev start tag

		List<Facility> result = new ArrayList<Facility>();

		if (!mParser.getName().equals(Xconstants.DEVICE))
			return result;

		parseInnerDevs(result, aid);

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

			parseInnerDevs(result, aid);

			mParser.nextTag(); // adapter endtag

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));

		return result;
	}
	
	private void parseInnerDevs(List<Facility> result, String aid) throws XmlPullParserException, IOException {
		do { // go through devs (facilities)
			Facility facility = new Facility();
			facility.setAdapterId(aid);
			facility.setInitialized(getSecureAttrValue(Xconstants.INITIALIZED).equals(Xconstants.ZERO) ? false : true);
			facility.setAddress(getSecureAttrValue(Xconstants.DID));
			facility.setLocationId(getSecureAttrValue(Xconstants.LID));
			facility.setRefresh(RefreshInterval.fromInterval(getSecureInt(getSecureAttrValue(Xconstants.REFRESH))));
			facility.setBattery(getSecureInt(getSecureAttrValue(Xconstants.BATTERY)));
			facility.setLastUpdate(new DateTime((long) getSecureInt(getSecureAttrValue(Xconstants.TIME)) * 1000)); // TODO: check this
			// facility.setInvolveTime(new DateTime((long)getSecureInt(getSecureAttrValue(Xconstants.INVOLVED))*1000)); //FIXME: in property
			facility.setNetworkQuality(getSecureInt(getSecureAttrValue(Xconstants.RSSI)));

			mParser.nextTag(); // part tag

			do { // go through parts (devices)
				BaseDevice device = createDeviceByType(getSecureAttrValue(Xconstants.TYPE));
				device.setVisibility(getSecureAttrValue(Xconstants.VISIBILITY).equals(Xconstants.ZERO) ? false : true);
				device.setName(getSecureAttrValue(Xconstants.NAME));
				device.setValue(getSecureAttrValue(Xconstants.VALUE));
				facility.addDevice(device);
				mParser.nextTag(); // part endtag
			} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.DEVICE));

			result.add(facility);
			mParser.nextTag(); // dev endtag

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
		mParser.require(XmlPullParser.START_TAG, ns, Xconstants.ROW);

		DeviceLog log = new DeviceLog();

		try {
			do { // TODO: check this stuffs
				String repeat = getSecureAttrValue(Xconstants.REPEAT);
				if (!repeat.isEmpty()) {
					String interval = getSecureAttrValue(Xconstants.INTERVAL);
					log.addValues(log.expandDataRow(readText(Xconstants.ROW), Integer.parseInt(repeat), Integer.parseInt(interval)));
				} else
					log.addValue(log.new DataRow(readText(Xconstants.ROW)));
			} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(Xconstants.COM_ROOT));
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		}

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
			String email = getSecureAttrValue(Xconstants.EMAIL);
			String name = String.format("%s %s", getSecureAttrValue(Xconstants.NAME), getSecureAttrValue(Xconstants.SURNAME));

			User.Role role = User.Role.fromString(getSecureAttrValue(Xconstants.ROLE));
			User.Gender gender = getSecureAttrValue(Xconstants.GENDER).equals(Xconstants.ZERO) ? User.Gender.Female : User.Gender.Male;

			result.add(new User(name, email, role, gender));
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
						if (stngs.equals(Xconstants.ADAPTER) || stngs.equals(Xconstants.LOCATION)) { // open adapter or location settings
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

		Condition condition = new Condition("0", "none", getSecureAttrValue(Xconstants.TYPE), funcs); // hope this add filled list
																							// to object
		mParser.nextTag(); // func tag

		do {
			ConditionFunction func = null;
			ConditionFunction.FunctionType type = ConditionFunction.FunctionType.fromValue(getSecureAttrValue(Xconstants.TYPE));
			switch (type) {
			case BTW:
				mParser.nextTag(); // device or value tag
				BaseDevice deviceBTW = null;
				String tempValue = null;
				String minValue = "0";
				String maxValue = "1";
				do {
					if (mParser.getName().equals(Xconstants.DEVICE)) {
						String stypeBTW = getSecureAttrValue(Xconstants.TYPE); // device type
						deviceBTW = createDeviceByType(stypeBTW);
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
				BaseDevice deviceCHG = createDeviceByType(stypeCHG);
				Facility facilityCHG = new Facility();
				facilityCHG.setAddress(getSecureAttrValue(Xconstants.ID));
				deviceCHG.setFacility(facilityCHG);
				mParser.nextTag(); // device endtag
				mParser.nextTag(); // func endtag

				func = new ChangeFunc(deviceCHG);
				break;
			case DP:
				mParser.nextTag(); // device tag
				BaseDevice tempoDevice = null;
				BaseDevice deviceDP_t = null;
				BaseDevice deviceDP_h = null;
				do {
					if (mParser.getName().equals(Xconstants.DEVICE)) {
						String stypeDP = getSecureAttrValue(Xconstants.TYPE);
						tempoDevice = createDeviceByType(stypeDP);
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
				BaseDevice device = null;
				String value = null;
				do {
					if (mParser.getName().equals(Xconstants.DEVICE)) {
						String stype = getSecureAttrValue(Xconstants.TYPE);
						device = createDeviceByType(stype);
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
	// /////////////////////////////////Xconstants.ACTIONS,Xconstants.CONDITIONS/////////////////////////////////////////////////////
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
					BaseDevice device = createDeviceByType(getSecureAttrValue(Xconstants.TYPE));
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
	
	// ///////////////////////////////// OTHER

	/**
	 * Method create empty object of device by type
	 * 
	 * @param sType
	 *            string type of device (e.g. 0x03)
	 * @return empty object
	 */
	private BaseDevice createDeviceByType(String sType) {
		int iType;
		if (sType.length() < 3) {
			iType = -1; // Unknown type
		} else {
			iType = Integer.parseInt(sType.replaceAll("0x", ""), 16);
		}
		
		return DeviceType.createDeviceFromType(iType);
	}

	// FIXME: check on first use
	List<Facility> getFalseMessage6() throws XmlPullParserException, IOException {

		mParser.nextTag();

		List<Facility> result = new ArrayList<Facility>();

		mParser.nextTag();
		if (!mParser.getName().equals(Xconstants.DEVICE))
			return result;

		do {
			Facility facility = null;
			boolean facilityExists = false;

			BaseDevice device = createDeviceByType(getSecureAttrValue(Xconstants.TYPE));

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
	List<User> getFalseMessage13() throws XmlPullParserException, IOException {

		mParser.nextTag();

		List<User> result = new ArrayList<User>();

		mParser.nextTag();
		if (!mParser.getName().equals(Xconstants.USER))
			return result;

		do {
			User user = new User("", getSecureAttrValue(Xconstants.EMAIL), User.Role.fromString(getSecureAttrValue(Xconstants.ROLE)),
					getSecureAttrValue(Xconstants.GENDER).equals(Xconstants.ZERO) ? User.Gender.Female : User.Gender.Male);

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
	public List<Facility> getDemoFacilitiesFromAsset(Context context, String filename) {
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
			if (!version.equals(COM_VER))
				throw new ComVerMisException(mComVerMisExcMessage + "Expected: " + COM_VER + " but got: " + version);

			result = parseAllFacilities();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
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
	public List<Location> getDemoLocationsFromAsset(Context context, String filename) {
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
			if (!version.equals(COM_VER))
				throw new ComVerMisException(mComVerMisExcMessage + "Expected: " + COM_VER + " but got: " + version);

			locations = parseRooms();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
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
	public List<Adapter> getDemoAdaptersFromAsset(Context context, String filename) {
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
			if (!version.equals(COM_VER))
				throw new ComVerMisException(mComVerMisExcMessage + "Expected: " + COM_VER + " but got: " + version);

			adapters = parseAdaptersReady();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
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
}