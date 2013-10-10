package cz.vutbr.fit.intelligenthomeanywhere.adapter.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Capabilities;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.Device;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.Emission;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.Humidity;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.Illumination;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.Noise;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.Pressure;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.Switch_c;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.Switch_s;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.Temperature;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.UnknownDeviceType;

/**
 * Class for parsing xml file for device
 * @author Robyer
 */
public class XmlDeviceParser {
	private static final String TAG = XmlDeviceParser.class.getSimpleName();
    private static final String ns = null; // We don't use namespaces
    
    // Adapter values
	public static final String ADAPTER_ROOT = "adapter";
	public static final String ADAPTER_ID = "id";
	public static final String ADAPTER_VERSION = "version";
	public static final String ADAPTER_CAPABILITIES = "capabilities";
	
	// Device values
	public static final String DEVICE_ROOT = "device";
	public static final String DEVICE_INITIALIZED = "initialized";
	public static final String DEVICE_TYPE = "type";
	public static final String DEVICE_INVOLVED = "involved";
	
	public static final String DEVICE_LOCATION = "location";
	public static final String DEVICE_NAME = "name";
	public static final String DEVICE_REFRESH = "refresh";
	public static final String DEVICE_BATTERY = "battery";
	public static final String DEVICE_VALUE = "value";
	
	public static final String DEVICE_NETWORK = "network";	
	public static final String DEVICE_NETWORK_ADDRESS = "address";
	public static final String DEVICE_NETWORK_QUALITY = "quality";
	
	public static final String DEVICE_LOGGING = "logging";
	public static final String DEVICE_LOGGING_ENABLED = "enabled";
	
	// Device types constants
	public static final int TYPE_TEMPERATURE = 0;
	public static final int TYPE_HUMIDITY = 1;
	public static final int TYPE_PRESSURE = 2;
	public static final int TYPE_SENSOR = 3;
	public static final int TYPE_SWITCH = 4;
	public static final int TYPE_ILLUMINATION = 5;
	public static final int TYPE_NOISE = 6;
	public static final int TYPE_EMMISION = 7;

    private XmlPullParser parser;
    
    /**
     * Factory for parsing adapter from file.
     * @param filename - path to file
     * @return Capabilities object
     */
	public static Capabilities fromFile(String filename) {
		Log.i(TAG, String.format("Parsing data from file '%s'", filename));
		Capabilities adapter = null;
		
		File file = new File(filename);
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			XmlDeviceParser parser = new XmlDeviceParser(false);
			adapter = parser.parse(in);			
		} catch(Exception e){
			Log.e(TAG, e.getMessage(), e);
		} finally {
	        try {
	        	if (in != null)
	        		in.close();
	        } catch (IOException ioe) {
	        	Log.e(TAG, ioe.getMessage(), ioe);
	        }
		}

		if (adapter != null)
			Log.i(TAG, String.format("Got adapter with %d devices", adapter.devices.size()));

		return adapter;
	}
	
	/**
	 * Class constructor.
	 * @param namespaces - use namespaces
	 * @throws XmlPullParserException
	 */
    public XmlDeviceParser(boolean namespaces) throws XmlPullParserException {
    	this.parser = Xml.newPullParser();
    	this.parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, namespaces);
    }
	
    /**
     * Parses inputstream into Capabilities object.
     * @param input - input stream
     * @return Capabilities
     * @throws XmlPullParserException
     * @throws IOException
     */
	public Capabilities parse(InputStream input) throws XmlPullParserException, IOException {
	    Capabilities adapter = new Capabilities();
		
		parser.setInput(input, null);
	    parser.nextTag();
	    parser.require(XmlPullParser.START_TAG, ns, ADAPTER_ROOT);
	    
	    String id = parser.getAttributeValue(null, ADAPTER_ID);
	    adapter.SetId(id);
	    
	    while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG)
	            continue;

	        String el = parser.getName();
	        if (el.equalsIgnoreCase(ADAPTER_CAPABILITIES)) {
	        	adapter.devices = readCapabilities();
	        } else if (el.equalsIgnoreCase(ADAPTER_VERSION)) {
	    		String version = readText(ADAPTER_VERSION);
	    		adapter.SetVersion(version);
	    	} else
	            skip();
	    }
	    
	    return adapter;
	}
	
	/**
	 * Read capabilities.
	 * @return ArrayList<Device>
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private ArrayList<Device> readCapabilities() throws XmlPullParserException, IOException {				
		ArrayList<Device> devices = new ArrayList<Device>();

		parser.require(XmlPullParser.START_TAG, ns, ADAPTER_CAPABILITIES);
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG)
	            continue;

	        if (parser.getName().equalsIgnoreCase(DEVICE_ROOT)) {
	        	Device device = readDevice();
	        	if (device != null)
	        		devices.add(device);
	        } else {
	        	skip();
	        }
	    }
		parser.require(XmlPullParser.END_TAG, ns, ADAPTER_CAPABILITIES);	

		return devices;
	}

	/**
	 * Read triggers.
	 * @return Device
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private Device readDevice() throws XmlPullParserException, IOException {		
		Device device = new Device();

		parser.require(XmlPullParser.START_TAG, ns, DEVICE_ROOT);
		
		// Initialized
    	String initialized_ = parser.getAttributeValue(null, DEVICE_INITIALIZED);    	
    	if (initialized_ != null)
    		device.SetInit(Integer.parseInt(initialized_) != 0);

    	// If not initialized yet, check involved attribute
    	if (!device.GetInit()) {
    		String involved_ = parser.getAttributeValue(null, DEVICE_INVOLVED);
    		if (involved_ != null)
    			device.SetInvolveTime(involved_);
    	}
    	
    	// Type
    	String type_ = parser.getAttributeValue(null, DEVICE_TYPE);
    	
    	int type = -1;
    	if (type_ != null)
    		type = Integer.decode(type_);
    	
    	device.SetType(type);
    	
    	switch (type) {
    	case TYPE_EMMISION:
    		device.deviceDestiny = new Emission();
    		break;
    	case TYPE_HUMIDITY:
    		device.deviceDestiny = new Humidity();
    		break;
    	case TYPE_ILLUMINATION:
    		device.deviceDestiny = new Illumination();
    		break;
    	case TYPE_NOISE:
    		device.deviceDestiny = new Noise();
    		break;
    	case TYPE_PRESSURE:
    		device.deviceDestiny = new Pressure();
    		break;
    	case TYPE_SENSOR:
    		device.deviceDestiny = new Switch_s();
    		break;
    	case TYPE_SWITCH:
    		device.deviceDestiny = new Switch_c();
    		break;
    	case TYPE_TEMPERATURE:
    		device.deviceDestiny = new Temperature();
    		break;
    	default:
    		device.deviceDestiny = new UnknownDeviceType();
    		break;
    	}
    	
    	Log.d(TAG, String.format("Got %s %s device", device.GetInit() ? "initialized" : "uninitialized", device.deviceDestiny.getClass().getSimpleName()));

    	String location_ = null;
        String name_ = null;
        String refresh_ = null;
        String battery_ = null;
        String value_ = null;
        String address_ = null;
        String quality_ = null;
        String logging_ = null;
        String logfile_ = null;
        
		while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG)
	            continue;	        
	        
	        String el = parser.getName();
	        if (el.equals(DEVICE_LOCATION)) {
	            location_ = readText(DEVICE_LOCATION);
	        } else if (el.equals(DEVICE_NAME)) {
	        	name_ = readText(DEVICE_NAME);
	        } else if (el.equals(DEVICE_REFRESH)) {
	        	refresh_ = readText(DEVICE_REFRESH);
	        } else if (el.equals(DEVICE_BATTERY)) {
	        	battery_ = readText(DEVICE_BATTERY);
	        } else if (el.equals(DEVICE_VALUE)) {
	        	value_ = readText(DEVICE_VALUE);
	        } else if (el.equals(DEVICE_NETWORK)) {	        	
	        	parser.require(XmlPullParser.START_TAG, ns, DEVICE_NETWORK);	        	
	        	while (parser.next() != XmlPullParser.END_TAG) {
        			if (parser.getEventType() != XmlPullParser.START_TAG)
        	            continue;
        			
        			String el2 = parser.getName();        			
        			if (el2.equalsIgnoreCase(DEVICE_NETWORK_ADDRESS)) {
	        			address_ = readText(DEVICE_NETWORK_ADDRESS);
        			} else if (el2.equalsIgnoreCase(DEVICE_NETWORK_QUALITY)) {
        				quality_ = readText(DEVICE_NETWORK_QUALITY);
        			} else {
        				skip();
        			}
        		}	        	
	        	parser.require(XmlPullParser.END_TAG, ns, DEVICE_NETWORK);
	        } else if (el.equals(DEVICE_LOGGING)) {
	        	logging_ = parser.getAttributeValue(null, DEVICE_LOGGING_ENABLED);
	        	logfile_ = readText(DEVICE_LOGGING);
	        } else {
	            skip();
	        }
	    }
		
		if (location_ != null)
			device.SetLocation(location_);		
		if (name_ != null)
			device.SetName(name_);
		if (refresh_ != null)
			device.SetRefresh(Integer.parseInt(refresh_));
		if (battery_ != null)
			device.SetBattery(Integer.parseInt(battery_));
		if (value_ != null)
			device.deviceDestiny.SetValue(value_);
		if (address_ != null)
			device.SetAddress(address_);
		if (quality_ != null)
			device.SetQuality(Integer.parseInt(quality_));
		if (logging_ != null)
			device.deviceDestiny.SetLog(Integer.parseInt(logging_) != 0);
		if (logfile_ != null)
			device.SetLog(logfile_);
		
		parser.require(XmlPullParser.END_TAG, ns, DEVICE_ROOT);
    	
		return device;
	}
	
	/**
	 * Skips whole element and subelements.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void skip() throws XmlPullParserException, IOException {
	    Log.d(TAG, "Skipping unknown child '" + parser.getName() + "'");
		if (parser.getEventType() != XmlPullParser.START_TAG) {
	        throw new IllegalStateException();
	    }
	    int depth = 1;
	    while (depth != 0) {
	        switch (parser.next()) {
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
	private String readText(String tag) throws IOException, XmlPullParserException {
	    parser.require(XmlPullParser.START_TAG, ns, tag);
		
		String result = "";
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = parser.getText();
	        parser.nextTag();
	    }
	    
	    parser.require(XmlPullParser.END_TAG, ns, tag);
	    return result;
	}

}
