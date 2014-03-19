/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.adapter.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.User;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.EmissionDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.HumidityDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.IlluminationDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.NoiseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.PressureDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.StateDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.SwitchDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.TemperatureDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.UnknownDevice;

/**
 * @author ThinkDeep
 *
 */
public class XmlParsers {
	
	private static XmlPullParser mParser;
	
	/**
	 * NameSpace
	 */
	public static final String ns = null;
	public static final String COM_ROOT = "communication";
	public static final String ID = "id";
	public static final String INIT_ID = "0";
	public static final String STATE = "state";
	public static final String TAG = "XmlParser";
	
	// states
	public static final String READY = "ready";
	public static final String NOTREGA = "notreg-a";
	public static final String NOTREGB = "notreg-b";
	public static final String XML = "xml";
	public static final String PARTIAL = "partial";
	public static final String CONTENT = "content";
	public static final String CONACCOUNTLIST = "conacountlist";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	//extra states
	public static final String ADDCONACCOUNT = "addconaccount";
	public static final String DELCONACCOUNT = "delconaccount";
	public static final String CHANGECONACCOUNT = "changeconaccount";
	
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
	public static final String VISIBILITY = "visibility";
	public static final String TYPE = "type";
	public static final String LOCATION = "location";
	public static final String NAME = "name";
	public static final String REFRESH = "refresh";
	public static final String BATTERY = "battery";
	public static final String QUALITY = "quality";
	public static final String VALUE = "value";
	public static final String LOGGING = "logging";
	public static final String ENABLED = "endabled";
	public static final String INIT_1 = "1";
	public static final String INIT_0 = "0";

	
	
	public static HashMap<String, HashMap<Integer, Object>> parseCommunication(String xmlInput, boolean namespace) throws XmlPullParserException, IOException{
		mParser = Xml.newPullParser();
		mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, namespace);
		
		HashMap<String, HashMap<Integer, Object>> outerResult = new HashMap<String, HashMap<Integer, Object>>();
		HashMap innerResult = null;
		String state = null;
		int id = 0;
		
		mParser.setInput(new ByteArrayInputStream(xmlInput.getBytes("UTF-8")), null);
		mParser.nextTag();
		
		mParser.require(XmlPullParser.START_TAG, ns, COM_ROOT);
		
		state = mParser.getAttributeValue(ns, STATE);
		id = Integer.parseInt(mParser.getAttributeValue(ns, ID));
		
		
		switch(getStateEnum(state)){
		case eCONACCOUNTLIST:
			innerResult = new HashMap<Integer, HashMap<String, String>>();
			innerResult.put(id, parseConAccountList());
			break;
		case eCONTENT:
			innerResult = new HashMap<Integer, ArrayList<String>>();
			innerResult.put(id, parseContent());
			break;
		case eFALSE:
			innerResult = new HashMap<Integer, HashMap<String, String>>();
			innerResult.put(id, parseFalse(mParser.getAttributeValue(ns, ADDITIONALINFO)));
			break;
		case eNOTREGA:
		case eNOTREGB:
			innerResult = new HashMap<Integer, Object>();
			innerResult.put(id, null);
			break;
		case ePARTIAL:
			innerResult = new HashMap<Integer, ArrayList<BaseDevice>>();
			innerResult.put(id, parsePartial());
			break;
		case eREADY:
			innerResult = new HashMap<Integer, HashMap<String, String>>();
			innerResult.put(id, parseReady());
			break;
		case eTRUE:
			innerResult = new HashMap<Integer, String>();
			innerResult.put(id, mParser.getAttributeValue(ns, ADDITIONALINFO));
			break;
		case eXML:
			//TODO
			break;
		case eUNKNOWN: // never gonna happen :D
			break;
		}
		
		outerResult.put(state, innerResult);
		
		return outerResult;
	}
	
	private static ArrayList<BaseDevice> parsePartial() throws XmlPullParserException, IOException{
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, DEVICE);
		
		ArrayList<BaseDevice> result = new ArrayList<BaseDevice>();
		do{
			BaseDevice device = getDeviceByType(mParser.getAttributeValue(ns, TYPE));
			device.setAddress(mParser.getAttributeValue(ns, ID));
			device.setInitialized((mParser.getAttributeValue(ns, INITIALIZED).equals(INIT_1))?true:false);
			device.setVisibility(mParser.getAttributeValue(ns, VISIBILITY).toLowerCase().charAt(0));
			
			String nameTag = null;
			
			//TODO: check this!!
			while(mParser.nextTag() != XmlPullParser.END_TAG && !(nameTag = mParser.getName()).equals(DEVICE)){
				if(nameTag.equals(LOCATION))
					device.setLocation(mParser.getText());
				else if(nameTag.equals(NAME))
					device.setName(mParser.getText());
				else if(nameTag.equals(REFRESH))
					device.setRefresh(Integer.parseInt(mParser.getText()));
				else if(nameTag.equals(BATTERY))
					device.setBattery(Integer.parseInt(mParser.getText()));
				else if(nameTag.equals(QUALITY))
					device.setQuality(Integer.parseInt(mParser.getText()));
				else if(nameTag.equals(VALUE))
					device.setValue(mParser.getText());
				else if(nameTag.equals(LOGGING)){
					device.setLogging((mParser.getAttributeValue(ns, ENABLED).equals(INIT_1))?true:false);
				}
			}
			
			result.add(device);
			
			//TODO: check this!!
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
			adapter.setId(mParser.getAttributeValue(ns, ID));
			adapter.setName(mParser.getAttributeValue(ns, NAME));
			adapter.setRole(User.Role.fromString(mParser.getAttributeValue(ns, ROLE)));
			result.add(adapter);
			
			mParser.nextTag();
			
			//TODO: check this!!
		}while(mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));
		
		return result;
	}
	
	/**
	 * Method parse inner part of Content.log message
	 * @return ArrayList of rows of log
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private static ArrayList<String> parseContent() throws XmlPullParserException, IOException {
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, ROW);
		
		ArrayList<String> result = new ArrayList<String>();
		do{
			
			result.add(mParser.getText());
			mParser.nextTag();
			
			//TODO: check this!!
		}while(mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));
		
		return result;
	}
	
	/**
	 * Method parse inner part of ConAccountList message
	 * @return HashMap with email as key and role as value
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private static HashMap<String, String> parseConAccountList() throws XmlPullParserException, IOException{
		mParser.nextTag();
		mParser.require(XmlPullParser.START_TAG, ns, USER);
		
		HashMap<String, String> result = new HashMap<String, String>();
		do{
			String keyEmail = mParser.getAttributeValue(ns, EMAIL);
			result.put(keyEmail, mParser.getAttributeValue(ns, ROLE));
			mParser.nextTag();
			
			//TODO: check this!!
		}while(mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));
		
		return result;
	}
	
	/**
	 * Method parse inner part of False message.
	 * In standard way (additionalInfo in {ADDCONACOUNT|DELCONACCOUNT|CHANGECONACCOUNT})
	 * returns email as key and role as value (except DELCONACCOUNT => role=null).
	 * In non-standard way (unknown type of additionalInfo) is in key "nameOfTag#num" where
	 * nameOfTag is name of actual tag and num is number of tag
	 * @param additionalInfo is previous state
	 * @return HashMap with user email as key and role as value (or null)
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private static HashMap<String, String> parseFalse(String additionalInfo) throws XmlPullParserException, IOException{
		mParser.nextTag();
		boolean unknownFlag = false;
		int unknownCounter = 0;
		if(additionalInfo.equals(ADDCONACCOUNT) || additionalInfo.equals(DELCONACCOUNT) || additionalInfo.equals(CHANGECONACCOUNT))
			mParser.require(XmlPullParser.START_TAG, ns, USER);
		else
			unknownFlag = true;
		
		HashMap<String, String> result = new HashMap<String, String>();
		do{
			String keyEmail = null;
			String valueRole = null;
			
			if(additionalInfo.equals(ADDCONACCOUNT) || additionalInfo.equals(CHANGECONACCOUNT))
				valueRole = mParser.getAttributeValue(ns, ROLE);
			if(!unknownFlag){
				keyEmail = mParser.getAttributeValue(ns, EMAIL);
				result.put(keyEmail, valueRole);
				mParser.nextTag();
			}else{
				keyEmail = mParser.getName() + "#" + Integer.toString(unknownCounter);
				result.put(keyEmail, null);
				skip();
			}

			//TODO: check this!!
		}while(mParser.nextTag() != XmlPullParser.END_TAG && !mParser.getName().equals(COM_ROOT));
		
		return result;
	}
	
	/**
	 * Skips whole element and sub-elements.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
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
	 * Method convert state as string to its enum representation
	 * @param state of communication
	 * @return one of enum STATES
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
		else
			return STATES.eUNKNOWN;
	}
	
	/**
	 * Method create empty object of device by type
	 * @param sType string type of device (e.g. 0x03)
	 * @return emtpy object
	 */
	public static BaseDevice getDeviceByType(String sType){
		int iType = Integer.parseInt(sType, 16);
		
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
		eXML, ePARTIAL, eCONTENT, eCONACCOUNTLIST,
		eTRUE, eFALSE, eUNKNOWN
	}
	
}