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
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;
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
import cz.vutbr.fit.iha.adapter.device.EmissionDevice;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.adapter.device.HumidityDevice;
import cz.vutbr.fit.iha.adapter.device.IlluminationDevice;
import cz.vutbr.fit.iha.adapter.device.NoiseDevice;
import cz.vutbr.fit.iha.adapter.device.PressureDevice;
import cz.vutbr.fit.iha.adapter.device.RefreshInterval;
import cz.vutbr.fit.iha.adapter.device.StateDevice;
import cz.vutbr.fit.iha.adapter.device.SwitchDevice;
import cz.vutbr.fit.iha.adapter.device.TemperatureDevice;
import cz.vutbr.fit.iha.adapter.device.UnknownDevice;
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

	/**
	 * Thats mean Android OS
	 */
	public static final String COM_VER = Constants.COM_VER;
	public static final String XML_VER = "1.0.2";

	/**
	 * NameSpace
	 */
	public static final String ns = null;
	public static final String COM_ROOT = "communication";
	public static final String ID = "id";
	public static final String INIT_ID = "0";
	public static final String STATE = "state";
	public static final String TAG = XmlParsers.class.getSimpleName();
	public static final String VERSION = "version";
	public static final String CAPABILITIES = "capabilities";

	/**
	 * Represents states of communication (from server to app)
	 * 
	 * @author ThinkDeep
	 * @author Robyer
	 * 
	 */
	public enum State {
		ADAPTERSREADY("adaptersready"),
		NOTREGA("notreg-a"),
		NOTREGB("notreg-b"),
		ALLDEVICES("alldevices"),
		DEVICES("devices"),
		LOGDATA("logdata"),
		ACCOUNTSLIST("accountslist"),
		TRUE("true"),
		FALSE("false"),
		RESIGN("resign"),
		VIEWSLIST("viewslist"),
		TIMEZONE("timezone"),
		ROOMS("rooms"),
		ROOMCREATED("roomcreated"),
		NOTIFICATIONS("notifications"),
		CONDITIONCREATED("conditioncreated"),
		CONDITION("condition"),
		CONDITIONS("conditions"),
		ACTIONCREATED("actioncreated"),
		ACTIONS("actions"),
		ACTION("action"),
		UNKNOWN("");

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

	public static final String ADAPTER = "adapter";
	public static final String ROW = "row";
	public static final String USER = "user";
	public static final String EMAIL = "email";
	public static final String ROLE = "role";
	public static final String ADDITIONALINFO = "additionalinfo";
	//
	public static final String DEVICE = "device";
	public static final String INITIALIZED = "initialized";
	public static final String INVOLVED = "involved";
	public static final String VISIBILITY = "visibility";
	public static final String TYPE = "type";
	public static final String LOCATION = "location";
	public static final String NAME = "name";
	public static final String SURNAME = "surname";
	public static final String GENDER = "gender";
	public static final String REFRESH = "refresh";
	public static final String BATTERY = "battery";
	public static final String QUALITY = "quality";
	public static final String VALUE = "value";
	public static final String LOGGING = "logging";
	public static final String ENABLED = "enabled";
	public static final String INIT_1 = "1";
	public static final String INIT_0 = "0";
	public static final String POSITIVEONE = "1";
	public static final String ICON = "icon";
	public static final String VIEW = "view";
	public static final String HWUPDATED = "hwupdated";
	public static final String TIME = "time";
	public static final String UTC = "utc";
	public static final String ERRCODE = "errcode";
	public static final String NOTIFICATION = "notification";
	public static final String MSGID = "msgid";
	public static final String READ = "read";
	public static final String ACTION = "action";
	public static final String URL = "url";
	public static final String ADAPTERID = "adapterid";
	public static final String LOCATIONID = "locationid";
	public static final String DEVICEID = "deviceid";
	public static final String MESSAGE = "message";
	public static final String SETTINGS = "settings";
	public static final String CONDITION = "condition";
	public static final String FUNC = "func";
	public static final String CONDITIONS = "conditions";
	public static final String COMPLEXACTION = "complexaction";

	public static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";

	// exception
	private static final String mComVerMisExcMessage = "Communication version mismatch.";
	private static final String mXmlVerMisExcMessage = "Xml version mismatch.";

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

		// Log.i(TAG, xmlInput.length() + "");
		mParser.setInput(new ByteArrayInputStream(xmlInput.getBytes("UTF-8")), null);
		mParser.nextTag();

		mParser.require(XmlPullParser.START_TAG, ns, COM_ROOT);

		State state = State.fromValue(getSecureAttrValue(ns, STATE));
		String id = getSecureAttrValue(ns, ID);
		String version = getSecureAttrValue(ns, VERSION);

		if (!version.equals(COM_VER))
			throw new ComVerMisException(mComVerMisExcMessage + "Expected: " + COM_VER + " but got: " + version);

		ParsedMessage result = new ParsedMessage(state, id, null);

		switch (state) {
		case ACCOUNTSLIST:
			// HashMap<String, User>
			result.data = parseConAccountList();
			break;
		case LOGDATA:
			// DeviceLog
			result.data = parseLogData();
			break;
		case FALSE:
			// FalseAnswer
			result.data = parseFalse();
			break;
		case NOTREGA:
		case NOTREGB:
		case RESIGN:
		case UNKNOWN: // never gonna happen :D
			// null
			break;
		case DEVICES:
			// List<Facility>
			String adapterId = getSecureAttrValue(ns, ADAPTER);
			result.data = parseFacilities(adapterId);
			break;
		case ADAPTERSREADY:
			// List<Adapter>
			result.data = parseAdaptersReady();
			break;
		case TRUE:
			// String
			result.data = getSecureAttrValue(ns, ADDITIONALINFO);
			break;
		case ALLDEVICES:
			// List<Facility>
			result.data = parseAllFacilities();
			break;
		case VIEWSLIST:
			// List<CustomViewPair>
			result.data = parseViewsList();
			break;
		case TIMEZONE:
			// integer
			result.data = parseTimeZone();
			break;
		case ROOMS:
			// List<Location>
			result.data = parseRooms();
			break;
		case ROOMCREATED:
			// String
			result.data = parseRoomCreated();
			break;
		case NOTIFICATIONS:
			// List<Notification>
			result.data = parseNotifications();
		case CONDITIONCREATED:
			// String
			result.data = parseConditionCreated();
			break;
		case CONDITION:
			// Condition
			result.data = parseCondition();
			break;
		case CONDITIONS:
			// List<Condition>
			result.data = parseConditions();
			break;
		case ACTIONCREATED:
			// String
			result.data = parseActionCreated();
			break;
		case ACTIONS:
			// List<ComplexAction>
			result.data = parseActions();
			break;
		case ACTION:
			// ComplexAction
			result.data = parseAction();
			break;
		default:
			break;
		}

		return result;
	}

	// ///////////////////////////////// PARSE

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
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, ADAPTER);

		String adapterId = getSecureAttrValue(ns, ID);

		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, VERSION);

		String version = readText(VERSION);
		if (!version.equals(XML_VER))
			throw new XmlVerMisException(mXmlVerMisExcMessage + "Expected: " + XML_VER + " but got: " + version);

		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, CAPABILITIES);
		return parseFacilities(adapterId);
	}

	/**
	 * Method parse inner part of Device message (old:Partial message (set of device's tag))
	 * 
	 * @return List of facilities
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws ParseException
	 */
	private List<Facility> parseFacilities(String adapterId) throws XmlPullParserException, IOException, ParseException {
		mParser.nextTag();
		// mParser.require(XmlPullParser.START_TAG, ns, DEVICE); // strict solution

		List<Facility> result = new ArrayList<Facility>();

		if (!mParser.getName().equals(DEVICE))
			return result;

		do {
			Facility facility = null;
			boolean facilityExists = false;

			BaseDevice device = createDeviceByType(getSecureAttrValue(ns, TYPE));

			String id = getSecureAttrValue(ns, ID);
			for (Facility fac : result) {
				if (fac.getAddress().equals(id)) {
					// We already have this facility, just add new devices to it
					facilityExists = true;
					facility = fac;
					break;
				}
			}

			boolean initialized = (getSecureAttrValue(ns, INITIALIZED).equals(INIT_1)) ? true : false;
			String involved = (!initialized) ? getSecureAttrValue(ns, INVOLVED) : "";
			boolean visibility = (getSecureAttrValue(ns, VISIBILITY).equals(INIT_1)) ? true : false;

			if (facility == null) {
				// This facility is new, first create a object for it
				facility = new Facility();
				facility.setAdapterId(adapterId);
				facility.setAddress(id);
				facility.setInitialized(initialized);
				if (!facility.isInitialized()) {
					facility.setInvolveTime(involved);
				}
				facility.setVisibility(visibility);
			}

			String nameTag = null;
			while (mParser.nextTag() != XmlPullParser.END_TAG && !(nameTag = mParser.getName()).equals(DEVICE)) {
				if (nameTag.equals(LOCATION)) {
					facility.setLocationId(getSecureAttrValue(ns, ID));
					mParser.next();
				} else if (nameTag.equals(NAME))
					device.setName(readText(NAME));
				else if (nameTag.equals(REFRESH))
					facility.setRefresh(RefreshInterval.fromInterval(Integer.parseInt(readText(REFRESH))));
				else if (nameTag.equals(BATTERY))
					facility.setBattery(Integer.parseInt(readText(BATTERY)));
				else if (nameTag.equals(QUALITY))
					facility.setNetworkQuality(Integer.parseInt(readText(QUALITY)));
				else if (nameTag.equals(VALUE)) {
					String hwupdated = getSecureAttrValue(ns, HWUPDATED);
					DateTime lastUpdate = hwupdated.isEmpty() ? DateTime.now() : DateTimeFormat.forPattern(DATEFORMAT).parseDateTime(hwupdated);
					facility.setLastUpdate(lastUpdate);

					device.setValue(readText(VALUE));
				} else if (nameTag.equals(LOGGING))
					facility.setLogging((getSecureAttrValue(ns, ENABLED).equals(INIT_1)) ? true : false);
			}

			facility.addDevice(device);

			if (!facilityExists) {
				Log.d(TAG, String.format("Adding facility (%s) with %d devices.", facility.getId(), facility
						.getDevices().size()));
				result.add(facility);
			}

			mParser.nextTag();

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));

		return result;
	}

	/**
	 * Method parse inner part of AdaptersReady message
	 * 
	 * @return List of adapters (contains only Id, name, and user role)
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private List<Adapter> parseAdaptersReady() throws XmlPullParserException, IOException {
		mParser.nextTag();
		// mParser.require(XmlPullParser.START_TAG, ns, ADAPTER); // strict solution

		List<Adapter> result = new ArrayList<Adapter>();

		if (!mParser.getName().equals(ADAPTER))
			return result;

		do {
			Adapter adapter = new Adapter();
			adapter.setId(getSecureAttrValue(ns, ID));
			adapter.setName(getSecureAttrValue(ns, NAME));
			adapter.setRole(User.Role.fromString(getSecureAttrValue(ns, ROLE)));
			adapter.setUtcOffset(getSecureInt(getSecureAttrValue(ns, UTC)));
			result.add(adapter);

			mParser.nextTag();

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));

		return result;
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
		mParser.require(XmlPullParser.START_TAG, ns, ROW);

		DeviceLog log = new DeviceLog();

		try {
			do {
				log.addValue(log.new DataRow(readText(ROW)));
			} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			// FIXME:!!!
		}

		return log;
	}

	/**
	 * Method parse inner part of ConAccountList message
	 * 
	 * @return HashMap with email as key and User object as value
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private HashMap<String, User> parseConAccountList() throws XmlPullParserException, IOException {
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, USER);

		HashMap<String, User> result = new HashMap<String, User>();
		do {
			String email = getSecureAttrValue(ns, EMAIL);
			String name = String.format("%s %s", getSecureAttrValue(ns, NAME), getSecureAttrValue(ns, SURNAME));

			User.Role role = User.Role.fromString(getSecureAttrValue(ns, ROLE));
			User.Gender gender = getSecureAttrValue(ns, GENDER).equals(POSITIVEONE) ? User.Gender.Male
					: User.Gender.Female;

			result.put(email, new User(name, email, role, gender));
			mParser.nextTag();

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));

		return result;
	}

	/**
	 * Method parse inner part of False message
	 * 
	 * @return FalseAnswer
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private FalseAnswer parseFalse() throws XmlPullParserException, IOException {
		Object trouble = null;
		int err = getSecureInt(getSecureAttrValue(ns, ERRCODE));
		if (err == 6) {
			trouble = getFalseMessage6();
		}
		if (err == 13) {
			trouble = getFalseMessage13();
		}
		return new FalseAnswer(getSecureAttrValue(ns, ADDITIONALINFO), err, trouble);
	}

	/**
	 * Method parse inner part of ViewList message
	 * 
	 * @return list of CustomViewPairs
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private List<CustomViewPair> parseViewsList() throws XmlPullParserException, IOException {
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, VIEW);

		List<CustomViewPair> result = new ArrayList<CustomViewPair>();
		do {
			result.add(new CustomViewPair(Integer.parseInt(getSecureAttrValue(ns, ICON)), getSecureAttrValue(ns, NAME)));

			mParser.nextTag();

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));

		return result;
	}

	/**
	 * Method parse inner part of TimeZone message
	 * 
	 * @return integer in range <-12,12>
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private Integer parseTimeZone() throws XmlPullParserException, IOException {
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, TIME);

		return Integer.valueOf(getSecureInt(getSecureAttrValue(ns, UTC)));
	}

	/**
	 * Method parse inner part of Rooms message
	 * 
	 * @return list of locations
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private List<Location> parseRooms() throws XmlPullParserException, IOException {
		mParser.nextTag();
		// mParser.require(XmlPullParser.START_TAG, ns, LOCATION); // strict solution

		List<Location> result = new ArrayList<Location>();

		if (!mParser.getName().equals(LOCATION))
			return result;

		do {
			int type = getSecureInt(getSecureAttrValue(ns, TYPE));
			Location location = new Location(getSecureAttrValue(ns, ID), readText(LOCATION), type);
			// FIXME: Fix this when we will have support in protocol
			// location.setAdapterId(adapterId);
			result.add(location);

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));

		return result;
	}

	private String parseRoomCreated() throws XmlPullParserException, IOException {
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, LOCATION);

		return getSecureAttrValue(ns, ID);
	}

	private List<Notification> parseNotifications() throws XmlPullParserException, IOException {
		mParser.nextTag();
		// mParser.require(XmlPullParser.START_TAG, ns, NOTIFICATION); // strict solution

		List<Notification> result = new ArrayList<Notification>();

		if (!mParser.getName().equals(NOTIFICATION))
			return result;

		do {
			Notification ntfc = new Notification(getSecureAttrValue(ns, MSGID), getSecureAttrValue(ns, TIME),
					getSecureAttrValue(ns, TYPE), (getSecureAttrValue(ns, READ).equals(INIT_1)) ? true : false);

			mParser.nextTag();

			if (mParser.getName().equals(MESSAGE)) { // get text from notification if is first tag
				ntfc.setMessage(readText(MESSAGE));
				mParser.nextTag();
			}

			if (mParser.getName().equals(ACTION)) { // get action from notification
				Notification.Action action = ntfc.new Action(getSecureAttrValue(ns, TYPE));

				if (action.getMasterType() == ActionType.WEB) { // get url and should open web or play
					action.setURL(getSecureAttrValue(ns, URL));
					ntfc.setAction(action);
				}

				if (action.getMasterType() == ActionType.APP) { // open some aktivity in app
					mParser.nextTag();

					ActionType tagName = ActionType.fromValue(mParser.getName());
					action.setSlaveType(tagName);
					switch (tagName) {
					case SETTINGS: // open settings {main, account, adapter, location}
						ActionType settings = ActionType.fromValue(SETTINGS + getSecureAttrValue(ns, TYPE));
						action.setSlaveType(settings);
						String stngs = settings.getValue();
						if (stngs.equals(ADAPTER) || stngs.equals(LOCATION)) { // open adapter or location settings
							action.setAdapterId(getSecureAttrValue(ns, ADAPTERID));
							if (stngs.equals(LOCATION)) // open location settings
								action.setLocationId(getSecureAttrValue(ns, LOCATIONID));
						}
						break;
					case OPENADAPTER: // switch adapter
						action.setAdapterId(getSecureAttrValue(ns, ADAPTERID));
						break;
					case OPENLOCATION: // open specific location in specific adapter
						action.setAdapterId(getSecureAttrValue(ns, ADAPTERID));
						action.setLocationId(getSecureAttrValue(ns, LOCATIONID));
						break;
					case OPENDEVICE: // open detail of specific device in specific adapter
						action.setAdapterId(getSecureAttrValue(ns, ADAPTERID));
						action.setDeviceId(getSecureAttrValue(ns, DEVICEID));
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
				if (mParser.getName().equals(MESSAGE)) {
					ntfc.setMessage(readText(MESSAGE));
					mParser.nextTag(); // notification end tag
				}
			}

			result.add(ntfc);

			// boolean f1 = mParser.nextTag() != XmlPullParser.END_TAG;
			// boolean f2 = !mParser.getName().equals(COM_ROOT);
			// String name = mParser.getName();

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));

		return result;
	}

	private String parseConditionCreated() throws XmlPullParserException, IOException {
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, CONDITION);

		return getSecureAttrValue(ns, ID);
	}
	
	//TODO: need to be checked
	private Condition parseCondition() throws XmlPullParserException, IOException{
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, CONDITION);
		ArrayList<ConditionFunction> funcs = new ArrayList<ConditionFunction>();
		
		Condition condition = new Condition("0", "none", getSecureAttrValue(ns, TYPE), funcs); // hope this add filled list to object
		
		mParser.nextTag(); // func tag
		
		do{
			ConditionFunction func = null;
			ConditionFunction.FunctionType type = ConditionFunction.FunctionType.fromValue(getSecureAttrValue(ns, TYPE));
			switch(type){
				case BTW:
					mParser.nextTag(); // device or value tag
					BaseDevice deviceBTW = null;
					String tempValue = null;
					String minValue = "0";
					String maxValue = "1";
					do{
						if(mParser.getName().equals(DEVICE)){
							String stypeBTW = getSecureAttrValue(ns, TYPE);
							deviceBTW = createDeviceByType(stypeBTW);
							Facility facilityBTW = new Facility();
							facilityBTW.setAddress(getSecureAttrValue(ns, ID));
							deviceBTW.setFacility(facilityBTW);
							mParser.nextTag();
						}
						if(mParser.getName().equals(VALUE)){
							String value = readText(VALUE);
							if(tempValue == null){
								tempValue = value;
							}else{
								if(Integer.parseInt(tempValue) > Integer.parseInt(value)){
									minValue = value;
									maxValue = tempValue;
								}else{
									minValue = tempValue;
									maxValue = value;
								}
							}
						}
					}while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(FUNC));
					
					func = new BetweenFunc(deviceBTW, minValue, maxValue);
					break;
				case CHG:
					mParser.nextTag(); // device tag

					String stypeCHG = getSecureAttrValue(ns, TYPE);
					BaseDevice deviceCHG = createDeviceByType(stypeCHG);
					Facility facilityCHG = new Facility();
					facilityCHG.setAddress(getSecureAttrValue(ns, ID));
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
					do{
						if(mParser.getName().equals(DEVICE)){
							String stypeDP = getSecureAttrValue(ns, TYPE);
							tempoDevice = createDeviceByType(stypeDP);
							Facility facilityDP = new Facility();
							facilityDP.setAddress(getSecureAttrValue(ns, ID));
							tempoDevice.setFacility(facilityDP);
							if(tempoDevice instanceof TemperatureDevice){
								deviceDP_t = tempoDevice;
							}else{
								deviceDP_h = tempoDevice;
							}
							
							mParser.nextTag();
						}
					}while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(FUNC));
					
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
					do{
						if(mParser.getName().equals(DEVICE)){
							String stype = getSecureAttrValue(ns, TYPE);
							device = createDeviceByType(stype);
							Facility facility = new Facility();
							facility.setAddress(getSecureAttrValue(ns, ID));
							device.setFacility(facility);
							mParser.nextTag();
						}
						if(mParser.getName().equals(VALUE)){
							value = readText(VALUE);
						}
					}while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(FUNC));
					switch(type){
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
					//TODO: this
					break;
				case TIME:
					mParser.nextTag(); // value tag
					value = readText(VALUE); // time
					mParser.nextTag(); // func endtag
					break;
				case UNKNOWN:
					break;
				default:
					break;
			}
			funcs.add(func);
		} while(mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(CONDITION));
		
		return condition;
	}

	//TODO: need to be checked
	private List<Condition> parseConditions() throws XmlPullParserException, IOException{
		mParser.nextTag();

		List<Condition> result = new ArrayList<Condition>();

		if (!mParser.getName().equals(CONDITION))
			return result;

		do {
			result.add(new Condition(getSecureAttrValue(ns, ID), getSecureAttrValue(ns, NAME)));
			mParser.nextTag();
		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));

		return result;
	}
	
	private String parseActionCreated() throws XmlPullParserException, IOException {
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, COMPLEXACTION);

		return getSecureAttrValue(ns, ID);
	}
	
	//TODO: need to be checked
	private List<ComplexAction> parseActions() throws XmlPullParserException, IOException{
		mParser.nextTag();

		List<ComplexAction> result = new ArrayList<ComplexAction>();

		if (!mParser.getName().equals(COMPLEXACTION))
			return result;

		do {
			result.add(new ComplexAction(getSecureAttrValue(ns, ID), getSecureAttrValue(ns, NAME)));
			mParser.nextTag();
		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));

		return result;
	}
	
	//TODO: need to be checked
	private ComplexAction parseAction() throws XmlPullParserException, IOException{
		mParser.nextTag();

		ComplexAction result = new ComplexAction();
		List<Action> actions = new ArrayList<Action>();

		if (!mParser.getName().equals(COMPLEXACTION))
			return result;

		do {
			mParser.nextTag(); // action tag
			Action action = new Action(Action.ActionType.fromValue(getSecureAttrValue(ns, TYPE)));
			if(action.getType() == Action.ActionType.ACTOR){
				mParser.nextTag(); //device tag
				BaseDevice device = createDeviceByType(getSecureAttrValue(ns, TYPE));
				Facility facility = new Facility();
				facility.setAddress(getSecureAttrValue(ns, ID));
				device.setFacility(facility);
				action.setDevice(device);
				
				mParser.nextTag(); //value tag
				action.setValue(readText(VALUE)); // value endtag
				mParser.nextTag(); //device endtag
			}
			actions.add(action);
			mParser.nextTag(); // action endtag
		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COMPLEXACTION));
		
		result.setActions(actions);

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

		if (sType.length() < 3)
			return new UnknownDevice();

		int iType = Integer.parseInt(sType.replaceAll("0x", ""), 16);

		switch (iType) {
		case Constants.TYPE_EMMISION:
			return new EmissionDevice();
		case Constants.TYPE_HUMIDITY:
			return new HumidityDevice();
		case Constants.TYPE_ILLUMINATION:
			return new IlluminationDevice();
		case Constants.TYPE_NOISE:
			return new NoiseDevice();
		case Constants.TYPE_PRESSURE:
			return new PressureDevice();
		case Constants.TYPE_STATE:
			return new StateDevice();
		case Constants.TYPE_SWITCH:
			return new SwitchDevice();
		case Constants.TYPE_TEMPERATURE:
			return new TemperatureDevice();
		default:
			return new UnknownDevice();
		}
	}

	// FIXME: check on first use
	List<Facility> getFalseMessage6() throws XmlPullParserException, IOException {

		mParser.nextTag();

		List<Facility> result = new ArrayList<Facility>();

		mParser.nextTag();
		if (!mParser.getName().equals(DEVICE))
			return result;

		do {
			Facility facility = null;
			boolean facilityExists = false;

			BaseDevice device = createDeviceByType(getSecureAttrValue(ns, TYPE));

			String id = getSecureAttrValue(ns, ID);
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

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));

		return result;
	}

	// FIXME: check on first use
	List<User> getFalseMessage13() throws XmlPullParserException, IOException {

		mParser.nextTag();

		List<User> result = new ArrayList<User>();

		mParser.nextTag();
		if (!mParser.getName().equals(USER))
			return result;

		do {
			User user = new User("", getSecureAttrValue(ns, EMAIL), User.Role.fromString(getSecureAttrValue(ns, ROLE)),
					getSecureAttrValue(ns, GENDER).equals(POSITIVEONE) ? User.Gender.Male : User.Gender.Female);

			result.add(user);
			mParser.nextTag();

		} while (mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));

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
	 * Method change null result value to empty string (mParser is included)
	 * 
	 * @param nameSpace
	 * @param name
	 *            of the attribute
	 * @return parsed attribute or empty string
	 */
	private String getSecureAttrValue(String nameSpace, String name) {
		String result = mParser.getAttributeValue(nameSpace, name);
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

			String version = getSecureAttrValue(ns, VERSION);
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

			String version = getSecureAttrValue(ns, VERSION);
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