/**
 * 
 */
package cz.vutbr.fit.iha.adapter.parser;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.User;
import cz.vutbr.fit.iha.User.Role;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.device.EmissionDevice;
import cz.vutbr.fit.iha.adapter.device.HumidityDevice;
import cz.vutbr.fit.iha.adapter.device.IlluminationDevice;
import cz.vutbr.fit.iha.adapter.device.NoiseDevice;
import cz.vutbr.fit.iha.adapter.device.PressureDevice;
import cz.vutbr.fit.iha.adapter.device.StateDevice;
import cz.vutbr.fit.iha.adapter.device.SwitchDevice;
import cz.vutbr.fit.iha.adapter.device.TemperatureDevice;
import cz.vutbr.fit.iha.adapter.device.UnknownDevice;
import cz.vutbr.fit.iha.exception.ComVerMisException;
import cz.vutbr.fit.iha.exception.XmlVerMisException;
import cz.vutbr.fit.iha.listing.Location;

/**
 * @author ThinkDeep
 *
 */
public class XmlParsers {
	
	private static XmlPullParser mParser;
	
	/**
	 * Thats mean Android OS
	 */
	public static final String COM_VER = "1.8";
	public static final String XML_VER = "1.0.2";
	
	/**
	 * NameSpace
	 */
	public static final String ns = null;
	public static final String COM_ROOT = "communication";
	public static final String ID = "id";
	public static final String INIT_ID = "0";
	public static final String STATE = "state";
	public static final String TAG = "XmlParser";
	public static final String VERSION = "version";
	public static final String CAPABILITIES = "capabilities";
	
	// states
	public static final String READY = "ready";
	public static final String NOTREGA = "notreg-a";
	public static final String NOTREGB = "notreg-b";
	public static final String XML = "xml";
	public static final String PARTIAL = "partial";
	public static final String CONTENT = "content";
	public static final String CONACCOUNTLIST = "conaccountlist";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	//extra states
	public static final String ADDCONACCOUNT = "addconaccount";
	public static final String DELCONACCOUNT = "delconaccount";
	public static final String CHANGECONACCOUNT = "changeconaccount";
	public static final String VIEWSLIST = "viewslist";
	public static final String TIMEZONE = "timezone";
	public static final String ROOMS = "rooms";
	
	// end of states
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
	
	public static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
	
	// exception
	private static final String mComVerMisExcMessage = "Communication version mismatch.";
	private static final String mXmlVerMisExcMessage = "Xml version mismatch.";

	
	/**
	 * Method parse message (XML) in communication version
	 * @param xmlInput
	 * @param namespace
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws ComVerMisException means Communication version mismatch exception
	 * @throws XmlVerMisException means XML version mismatch exception
	 * @throws ParseException 
	 */
	public static ParsedMessage parseCommunication(String xmlInput, boolean namespace) throws XmlPullParserException, IOException, ComVerMisException, XmlVerMisException, ParseException{
		mParser = Xml.newPullParser();
		mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, namespace);
		
		String state = null;
		int id = 0;
		String version = null;
		
		mParser.setInput(new ByteArrayInputStream(xmlInput.getBytes("UTF-8")), null);
		mParser.nextTag();
		
		mParser.require(XmlPullParser.START_TAG, ns, COM_ROOT);
		
		state = getSecureAttrValue(ns, STATE);
		id = Integer.parseInt(getSecureAttrValue(ns, ID));
		version = getSecureAttrValue(ns, VERSION);
		
		if(!version.equals(COM_VER))
			throw new ComVerMisException(mComVerMisExcMessage + "Expected: " + COM_VER + " but got: " + version);
		
		ParsedMessage result = new ParsedMessage(state, id, null);
		
		switch(getStateEnum(state)){
		case eCONACCOUNTLIST:
			// HashMap<String, User>
			result.data = parseConAccountList();
			break;
		case eCONTENT:
			// ArrayList<ContentRow>
			result.data = parseContent();
			break;
		case eFALSE:
			// FalseAnswer
			result.data = parseFalse();
			break;
		case eNOTREGA:
		case eNOTREGB:
		case eRESIGN:
		case eUNKNOWN: // never gonna happen :D
			// null
			break;
		case ePARTIAL:
			// ArrayList<BaseDevice>
			result.data = parsePartial();
			break;
		case eREADY:
			// ArrayList<Adapter>
			result.data = parseReady();
			break;
		case eTRUE:
			// String
			result.data = getSecureAttrValue(ns, ADDITIONALINFO);
			break;
		case eXML:
			// Adapter
			result.data = parseXml(getSecureAttrValue(ns, ROLE));
			break;
		case eVIEWSLIST:
			// ArrayList<CustomViewPair>
			result.data = parseViewsList();
			break;
		case eTIMEZONE:
			// integer
			result.data = parseTimeZone();
			break;
		case eROOMS:
			// ArrayList<Location>
			result.data = parseRooms();
			break;
		default:
			break;
		}
		
		return result;
	}
	
	/////////////////////////////////// PARSE
	
	/**
	 * Method parse inner part of XML message (using parsePartial())
	 * @param role authority of current user for this adapter
	 * @return Adapter object
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws XmlVerMisException means XML version mismatch exception
	 * @throws ParseException 
	 */
	private static Adapter parseXml(String role) throws XmlPullParserException, IOException, XmlVerMisException, ParseException{
		Adapter result = new Adapter();
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, ADAPTER);
		
		result.setId(getSecureAttrValue(ns, ID));
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, VERSION);
		
			result.setVersion(readText(VERSION));
			if(!result.getVersion().equals(XML_VER))
				throw new XmlVerMisException(mXmlVerMisExcMessage + "Expected: " + XML_VER + " but got: " + result.getVersion());
			
			result.setRole(Role.fromString(role));
			mParser.nextTag();
			mParser.require(XmlPullParser.START_TAG, ns, CAPABILITIES);
			result.setDevices(parsePartial());
		
		return result;
	}
	
	/**
	 * Method parse inner part of Partial message (set of device's tag)
	 * @return List of devices
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws ParseException 
	 */
	private static ArrayList<BaseDevice> parsePartial() throws XmlPullParserException, IOException, ParseException{
		mParser.nextTag();
		//mParser.require(XmlPullParser.START_TAG, ns, DEVICE); // strict solution
		
		ArrayList<BaseDevice> result = new ArrayList<BaseDevice>();
		
		if(!mParser.getName().equals(DEVICE))
			return result;
		
		do{
			BaseDevice device = getDeviceByType(getSecureAttrValue(ns, TYPE));
			device.setAddress(getSecureAttrValue(ns, ID));
			device.setInitialized((getSecureAttrValue(ns, INITIALIZED).equals(INIT_1))?true:false);
			if(!device.isInitialized()){
				device.setInvolveTime(getSecureAttrValue(ns, INVOLVED));
			}
			device.setVisibility(getSecureAttrValue(ns, VISIBILITY).toLowerCase(Locale.US).charAt(0));
			
			String nameTag = null;
			

			while(mParser.nextTag() != XmlPullParser.END_TAG && !(nameTag = mParser.getName()).equals(DEVICE)){
				if(nameTag.equals(LOCATION)){
					Location location = new Location();
					location.setId(getSecureAttrValue(ns, ID));
					device.setLocation(location);
					mParser.next();
				}
				else if(nameTag.equals(NAME))
					device.setName(readText(NAME));
				else if(nameTag.equals(REFRESH))
					device.setRefresh(Integer.parseInt(readText(REFRESH)));
				else if(nameTag.equals(BATTERY))
					device.setBattery(Integer.parseInt(readText(BATTERY)));
				else if(nameTag.equals(QUALITY))
					device.setQuality(Integer.parseInt(readText(QUALITY)));
				else if(nameTag.equals(VALUE)){
					String hwupdated = getSecureAttrValue(ns, HWUPDATED);
					if(hwupdated.length() < 1){
						device.lastUpdate.setToNow();
					}else{
						device.lastUpdate.set((new SimpleDateFormat(DATEFORMAT, Locale.getDefault()).parse(hwupdated)).getTime());
					}
					device.setValue(readText(VALUE));
				}
				else if(nameTag.equals(LOGGING))
					device.setLogging((getSecureAttrValue(ns, ENABLED).equals(INIT_1))?true:false);
			}
			
			result.add(device);
			mParser.nextTag();
			
		}while(mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));
		
		return result;
	}
	
	/**
	 * Method parse inner part of Ready[x1,x2,...] message
	 * @return ArrayList of adapters (contains only Id, name, and user role)
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private static ArrayList<Adapter> parseReady() throws XmlPullParserException, IOException{
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, ADAPTER);
		
		ArrayList<Adapter> result = new ArrayList<Adapter>();
		
		do{
			Adapter adapter = new Adapter();
			adapter.setId(getSecureAttrValue(ns, ID));
			adapter.setName(getSecureAttrValue(ns, NAME));
			adapter.setRole(User.Role.fromString(getSecureAttrValue(ns, ROLE)));
			result.add(adapter);
			
			mParser.nextTag();
			
		}while(mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));
		
		return result;
	}
	
	/**
	 * Method parse inner part of Content.log message
	 * @return List with ContentRow objects
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private static ArrayList<ContentRow> parseContent() throws XmlPullParserException, IOException {
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, ROW);
		
		ArrayList<ContentRow> result = new ArrayList<ContentRow>();
		do{
			
			result.add(new ContentRow(readText(ROW)));

		}while(mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));
		
		return result;
	}
	
	/**
	 * Method parse inner part of ConAccountList message
	 * @return HashMap with email as key and User object as value
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private static HashMap<String, User> parseConAccountList() throws XmlPullParserException, IOException{
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, USER);
		
		HashMap<String, User> result = new HashMap<String, User>();
		do {
			String email = getSecureAttrValue(ns, EMAIL);
			String name = String.format("%s %s",
					getSecureAttrValue(ns, NAME),
					getSecureAttrValue(ns, SURNAME));

			User.Role role = User.Role.fromString(getSecureAttrValue(ns, ROLE));
			User.Gender gender = getSecureAttrValue(ns, GENDER).equals(POSITIVEONE)
					? User.Gender.Male
					: User.Gender.Female;

			result.put(email, new User(name, email, role, gender));
			mParser.nextTag();
			
		} while(mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));
		
		return result;
	}
	
	/**
	 * Method parse inner part of False message
	 * @return FalseAnswer
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private static FalseAnswer parseFalse() throws XmlPullParserException, IOException{
		return new FalseAnswer(getSecureAttrValue(ns, ADDITIONALINFO), getSecureInt(getSecureAttrValue(ns, ERRCODE)), readText(COM_ROOT));
	}
	
	/**
	 * Method parse inner part of ViewList message
	 * @return list of CustomViewPairs
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private static ArrayList<CustomViewPair> parseViewsList() throws XmlPullParserException, IOException{
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, VIEW);
		
		ArrayList<CustomViewPair> result = new ArrayList<CustomViewPair>();
		do {
			result.add(new CustomViewPair(Integer.parseInt(getSecureAttrValue(ns, ICON)), getSecureAttrValue(ns, NAME)));
			
			mParser.nextTag();
			
		} while(mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));
		
		return result;
	}
	
	/**
	 * Method parse inner part of TimeZone message
	 * @return integer in range <-12,12>
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private static Integer parseTimeZone() throws XmlPullParserException, IOException{
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, TIME);
		
		return Integer.valueOf(getSecureInt(getSecureAttrValue(ns, UTC)));
	}
	
	/**
	 * Method parse inner part of Rooms message
	 * @return list of locations
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private static ArrayList<Location> parseRooms() throws XmlPullParserException, IOException{
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, LOCATION);
		
		ArrayList<Location> result = new ArrayList<Location>();
		
		do{
			int type = getSecureInt(getSecureAttrValue(ns, TYPE));
			result.add(new Location(getSecureAttrValue(ns, ID), readText(LOCATION), type));
			
		}while(mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));
		
		return result;
	}
	
	/////////////////////////////////// OTHER
	
	/**
	 * Method convert state as string to its ENUM representation
	 * @param state of communication
	 * @return one of ENUM STATES
	 */
	private static STATES getStateEnum(String state){
		if(state.equals(READY))
			return STATES.eREADY;
		else if(state.equals(NOTREGA))
			return STATES.eNOTREGA;
		else if(state.equals(NOTREGB))
			return STATES.eNOTREGB;
		else if(state.equals(XML))
			return STATES.eXML;
		else if(state.equals(PARTIAL))
			return STATES.ePARTIAL;
		else if(state.equals(CONTENT))
			return STATES.eCONTENT;
		else if(state.equals(CONACCOUNTLIST))
			return STATES.eCONACCOUNTLIST;
		else if(state.equals(TRUE))
			return STATES.eTRUE;
		else if(state.equals(FALSE))
			return STATES.eFALSE;
		else if(state.equals(VIEWSLIST))
			return STATES.eVIEWSLIST;
		else if(state.equals(TIMEZONE))
			return STATES.eTIMEZONE;
		else if(state.equals(ROOMS))
			return STATES.eROOMS;
		else
			return STATES.eUNKNOWN;
	}
	
	/**
	 * Method create empty object of device by type
	 * @param sType string type of device (e.g. 0x03)
	 * @return empty object
	 */
	public static BaseDevice getDeviceByType(String sType){
		
		int iType = Integer.parseInt(sType.replaceAll("0x", ""), 16);
		
		switch(iType){
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
	
	/**
	 * Represents states of communication (from server to app)
	 * @author ThinkDeep
	 *
	 */
	private enum STATES{
		eREADY, eNOTREGA, eNOTREGB,
		eXML, ePARTIAL, eCONTENT,
		eCONACCOUNTLIST, eTRUE, eFALSE,
		eRESIGN, eUNKNOWN, eVIEWSLIST,
		eTIMEZONE, eROOMS
	}
	
	////////////////////////////////// XML
	
	/**
	 * Skips whole element and sub-elements.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private static void skip() throws XmlPullParserException, IOException {
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
	 * @param tag
	 * @return value of element
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private static String readText(String tag) throws IOException, XmlPullParserException {
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
	 * @param nameSpace 
	 * @param name of the attribute
	 * @return parsed attribute or empty string
	 */
	private static String getSecureAttrValue(String nameSpace, String name){
		String result = mParser.getAttributeValue(nameSpace, name);
		if(result == null)
			result = "";
		return result;
	}
	
	/**
	 * Method return integer value of string, of zero if length is 0
	 * @param value
	 * @return integer value of zero if length is 0
	 */
	private static int getSecureInt(String value){
		return (value.length() < 1) ? 0 : Integer.parseInt(value);
	}

	///////////////////////////////// DEMO
	
	/**
     * Factory for parsing adapter from file.
     * @param filename - path to file
     * @return Adapter object
	 * @throws XmlPullParserException 
     */
	public static Adapter getDemoAdapterFromFile(String filename) {
		Log.i(TAG, String.format("Parsing data from file '%s'", filename));
//		boolean ee = false;
		
		Adapter adapter = null;
		
		File file = new File(filename);
		if (!file.exists() || !file.canRead()) {
			Log.w(TAG, String.format("File '%s' doesn't exists or is not readable", filename));
			return adapter;
		}

		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			mParser = Xml.newPullParser();
			mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			mParser.setInput(in, null);
			adapter = parseXml("superuser");
		} catch(XmlVerMisException e){
//			ee = true;
		} catch(Exception e){
			Log.e(TAG, e.getMessage(), e);
		} finally {
	        try {
	        	if (in != null)
	        		in.close();
//	        	if(ee){
//	        		file.delete();
//	        	}
	        } catch (IOException ioe) {
	        	Log.e(TAG, ioe.getMessage(), ioe);
	        }
		}

		return adapter;
	}
	
	public static ArrayList<Location> getDemoLocationsFromFile(String filename) {
		Log.i(TAG, String.format("Parsing data from file '%s'", filename));
//		boolean ee = false;
		
		ArrayList<Location> result = new ArrayList<Location>();
		
		File file = new File(filename);
		if (!file.exists() || !file.canRead()) {
			Log.w(TAG, String.format("File '%s' doesn't exists or is not readable", filename));
			return result;
		}

		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			mParser = Xml.newPullParser();
			mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			mParser.setInput(in, null);
			mParser.nextTag();
			
			String version = getSecureAttrValue(ns, VERSION);
			
			if(!version.equals(COM_VER))
				throw new ComVerMisException(mComVerMisExcMessage + "Expected: " + COM_VER + " but got: " + version);
			
			result = parseRooms();
		} catch(ComVerMisException e){
//			ee = true;
		} catch(Exception e){
			Log.e(TAG, e.getMessage(), e);
		} finally {
	        try {
	        	if (in != null)
	        		in.close();
//	        	if(ee){
//	        		file.delete();
//	        	}
	        } catch (IOException ioe) {
	        	Log.e(TAG, ioe.getMessage(), ioe);
	        }
		}

		return result;
	}
}