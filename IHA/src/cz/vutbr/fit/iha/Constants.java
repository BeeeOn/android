/*! \mainpage 
 * \section todo doxygen main page :-)
 * Some awesome first page here
 * \section other important stuffs
 * Put Here
 */

/**
 * @brief Package for whole application resources
 */
package cz.vutbr.fit.iha;


/**
 * @brief Class for all constants
 * @author ThinkDeep
 */
public final class Constants {
	
	/**
	 * Footing for pass from LocationScreenActivity to DataOfLocationScreenActivity
	 */
	public static final String LOCATION_CLICKED = "LOCATION_CLICKED";
	
	/**
	 * Footing for pass from DataOfLocationScreenActivity to SensorDetailActivity
	 */
	public static final String DEVICE_CLICKED = "DEVICE_CLICKED";
	
	/**
	 * Footing for pass from AddAdapterActivity to RegistrationActivity
	 */
	public static final String ADAPTER_SERIAL_NUMBER = "ADAPTER_SERIAL_NUMBER";
	
	/**
	 * Not used
	 */
	public static final String ADAPTER_ID = "ADAPTER_ID";
	
	/**
	 * Not used
	 */
	public static final String ADAPTER_VERSION = "ADAPTER_VERSION";
	
	/**
	 * Filename of file with prepared household sensors
	 */
	public static final String DEMO_FILENAME = "komunikace.xml";
	public static final String DEMO_ASSETNAME = "komunikace.xml";
	
	public static final String DEMO_LOCATION_FILENAME = "lokace.xml";
	public static final String DEMO_LOCATION_ASSETNAME = "lokace.xml";
	
	/**
	 * Filename of demo file with prepared household log info from one sensor
	 */
	public static final String DEMO_LOG_FILENAME = "sensor0.log";
	public static final String DEMO_LOG_ASSETNAME = "sensor0.log";
	
	/**
	 * Device types
	 */
	public static final int TYPE_UNKNOWN = -1; 		// unknown device
	public static final int TYPE_TEMPERATURE = 0; 	// temperature meter
	public static final int TYPE_HUMIDITY = 1; 		// humidity meter
	public static final int TYPE_PRESSURE = 2; 		// pressure meter
	public static final int TYPE_STATE = 3; 		// state sensor
	public static final int TYPE_SWITCH = 4; 		// switch sensor
	public static final int TYPE_ILLUMINATION = 5; 	// illumination meter
	public static final int TYPE_NOISE = 6; 		// noise meter
	public static final int TYPE_EMMISION = 7; 		// emmision meter
	
	/**
	 * Widget's shared preferences
	 */	
	public static final String WIDGET_PREF_FILENAME = "widget_%d";	
	public static final String WIDGET_PREF_LAYOUT = "layout";
	public static final String WIDGET_PREF_INTERVAL = "interval";
	public static final String WIDGET_PREF_LAST_UPDATE = "lastUpdate";
	public static final String WIDGET_PREF_INITIALIZED = "initialized";
	public static final String WIDGET_PREF_DEVICE = "device";
	
	/**
	 * Persistence's shared preferences
	 */
	public static final String PERSISTENCE_PREF_FILENAME = "persistence";
	public static final String PERSISTENCE_PREF_LAST_USER = "last_user";
	
	/**
	 * Sony Smartwatch2 preference activity
	 */
	public static final String SW2_PREF_DEF_ADAPTER = "default_adapter";
	public static final String SW2_PREF_DEF_LOCATION = "default_location";
}
