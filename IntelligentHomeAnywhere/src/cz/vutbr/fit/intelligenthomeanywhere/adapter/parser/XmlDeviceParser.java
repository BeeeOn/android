package cz.vutbr.fit.intelligenthomeanywhere.adapter.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;
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
	public static final int TYPE_UNKNOWN = -1;
	public static final int TYPE_TEMPERATURE = 0;
	public static final int TYPE_HUMIDITY = 1;
	public static final int TYPE_PRESSURE = 2;
	public static final int TYPE_STATE = 3;
	public static final int TYPE_SWITCH = 4;
	public static final int TYPE_ILLUMINATION = 5;
	public static final int TYPE_NOISE = 6;
	public static final int TYPE_EMMISION = 7;

    private XmlPullParser mParser;
    
    /**
     * Factory for parsing adapter from file.
     * @param filename - path to file
     * @return Adapter object
     */
	public static Adapter fromFile(String filename) {
		Log.i(TAG, String.format("Parsing data from file '%s'", filename));
		Adapter adapter = null;
		
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
    	this.mParser = Xml.newPullParser();
    	this.mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, namespaces);
    }
	
    /**
     * Parses inputstream into Adapter object.
     * @param input - input stream
     * @return Adapter
     * @throws XmlPullParserException
     * @throws IOException
     */
	public Adapter parse(InputStream input) throws XmlPullParserException, IOException {
	    Adapter adapter = new Adapter();
		
		mParser.setInput(input, null);
	    mParser.nextTag();
	    mParser.require(XmlPullParser.START_TAG, ns, ADAPTER_ROOT);
	    
	    String id = mParser.getAttributeValue(null, ADAPTER_ID);
	    adapter.setId(id);
	    
	    while (mParser.next() != XmlPullParser.END_TAG) {
	        if (mParser.getEventType() != XmlPullParser.START_TAG)
	            continue;

	        String el = mParser.getName();
	        if (el.equalsIgnoreCase(ADAPTER_CAPABILITIES)) {
	        	adapter.devices = readCapabilities();
	        } else if (el.equalsIgnoreCase(ADAPTER_VERSION)) {
	    		String version = readText(ADAPTER_VERSION);
	    		adapter.setVersion(version);
	    	} else
	            skip();
	    }
	    
	    return adapter;
	}
	
	/**
	 * Read capabilities.
	 * @return ArrayList<BaseDevice>
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private List<BaseDevice> readCapabilities() throws XmlPullParserException, IOException {				
		List<BaseDevice> devices = new ArrayList<BaseDevice>();

		mParser.require(XmlPullParser.START_TAG, ns, ADAPTER_CAPABILITIES);
		while (mParser.next() != XmlPullParser.END_TAG) {
			if (mParser.getEventType() != XmlPullParser.START_TAG)
	            continue;

	        if (mParser.getName().equalsIgnoreCase(DEVICE_ROOT)) {
	        	BaseDevice device = readDevice();
	        	if (device != null)
	        		devices.add(device);
	        } else {
	        	skip();
	        }
	    }
		mParser.require(XmlPullParser.END_TAG, ns, ADAPTER_CAPABILITIES);	

		return devices;
	}

	/**
	 * Read triggers.
	 * @return BaseDevice
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private BaseDevice readDevice() throws XmlPullParserException, IOException {		
		BaseDevice device;

		mParser.require(XmlPullParser.START_TAG, ns, DEVICE_ROOT);
		
    	// Type
    	String type_ = mParser.getAttributeValue(null, DEVICE_TYPE);
    	
    	int type = -1;
    	if (type_ != null)
    		type = Integer.decode(type_);    	
    	
    	switch (type) {
    	case TYPE_EMMISION:
    		device = new EmissionDevice();
    		break;
    	case TYPE_HUMIDITY:
    		device = new HumidityDevice();
    		break;
    	case TYPE_ILLUMINATION:
    		device = new IlluminationDevice();
    		break;
    	case TYPE_NOISE:
    		device = new NoiseDevice();
    		break;
    	case TYPE_PRESSURE:
    		device = new PressureDevice();
    		break;
    	case TYPE_STATE:
    		device = new StateDevice();
    		break;
    	case TYPE_SWITCH:
    		device = new SwitchDevice();
    		break;
    	case TYPE_TEMPERATURE:
    		device = new TemperatureDevice();
    		break;
    	default:
    		device = new UnknownDevice();
    		break;
    	}
    	
		// Initialized
    	String initialized_ = mParser.getAttributeValue(null, DEVICE_INITIALIZED);    	
    	if (initialized_ != null)
    		device.setInitialized(Integer.parseInt(initialized_) != 0);

    	// If not initialized yet, check involved attribute
    	if (!device.isInitialized()) {
    		String involved_ = mParser.getAttributeValue(null, DEVICE_INVOLVED);
    		if (involved_ != null)
    			device.setInvolveTime(involved_);
    	}
    	
    	Log.d(TAG, String.format("Got %s %s device", device.isInitialized() ? "initialized" : "uninitialized", device.getClass().getSimpleName()));

    	String location_ = null;
        String name_ = null;
        String refresh_ = null;
        String battery_ = null;
        String value_ = null;
        String address_ = null;
        String quality_ = null;
        String logging_ = null;
        String logfile_ = null;
        
		while (mParser.next() != XmlPullParser.END_TAG) {
	        if (mParser.getEventType() != XmlPullParser.START_TAG)
	            continue;	        
	        
	        String el = mParser.getName();
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
	        	mParser.require(XmlPullParser.START_TAG, ns, DEVICE_NETWORK);	        	
	        	while (mParser.next() != XmlPullParser.END_TAG) {
        			if (mParser.getEventType() != XmlPullParser.START_TAG)
        	            continue;
        			
        			String el2 = mParser.getName();        			
        			if (el2.equalsIgnoreCase(DEVICE_NETWORK_ADDRESS)) {
	        			address_ = readText(DEVICE_NETWORK_ADDRESS);
        			} else if (el2.equalsIgnoreCase(DEVICE_NETWORK_QUALITY)) {
        				quality_ = readText(DEVICE_NETWORK_QUALITY);
        			} else {
        				skip();
        			}
        		}	        	
	        	mParser.require(XmlPullParser.END_TAG, ns, DEVICE_NETWORK);
	        } else if (el.equals(DEVICE_LOGGING)) {
	        	logging_ = mParser.getAttributeValue(null, DEVICE_LOGGING_ENABLED);
	        	logfile_ = readText(DEVICE_LOGGING);
	        } else {
	            skip();
	        }
	    }
		
		if (location_ != null)
			device.setLocation(location_);		
		if (name_ != null)
			device.setName(name_);
		if (refresh_ != null)
			device.setRefresh(Integer.parseInt(refresh_));
		if (battery_ != null)
			device.setBattery(Integer.parseInt(battery_));
		if (value_ != null)
			device.setValue(value_);
		if (address_ != null)
			device.setAddress(address_);
		if (quality_ != null)
			device.setQuality(Integer.parseInt(quality_));
		if (logging_ != null)
			device.setLogging(Integer.parseInt(logging_) != 0);
		if (logfile_ != null)
			device.setLog(logfile_);
		
		mParser.require(XmlPullParser.END_TAG, ns, DEVICE_ROOT);
    	
		return device;
	}
	
	/**
	 * Skips whole element and subelements.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
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

}
