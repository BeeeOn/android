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
	
	/**
	 * Filename of demo file with prepared household log info from one sensor
	 */
	public static final String DEMO_LOG_FILENAME = "sensor0.log";
	public static final String DEMO_LOG_ASSETNAME = "sensor0.log";
	
	//TODO: maybe will not used or/and make comments
	public static final int ADAPTER_OFFLINE = 0;
	public static final int ADAPTER_NOT_REGISTERED = 1;
	public static final int ADAPTER_READY = 2;
	
	//TODO: maybe need to re-numbered these constants and/or make comments
	public static final int IDLE = 99;
	public static final int BUTTON_ID = 42;
	public static final int NUMBERPICKER_ID = 666;
	public static final int NUMBERPICKER_IDII = 999;
	public static final int NAMELABEL_ID = 777;
	
	/**
	 * Type of device: unknown type
	 */
	public static final int TYPE_UNKNOWN = -1;
	
	/**
	 * Type of device: temperature meter type
	 */
	public static final int TYPE_TEMPERATURE = 0;
	
	/**
	 * Type of device: humidity meter type
	 */
	public static final int TYPE_HUMIDITY = 1;
	
	/**
	 * Type of device: pressure meter type
	 */
	public static final int TYPE_PRESSURE = 2;
	
	/**
	 * Type of device: state sensor type
	 */
	public static final int TYPE_STATE = 3;
	
	/**
	 * Type of device: switch sensor type
	 */
	public static final int TYPE_SWITCH = 4;
	
	/**
	 * Type of device: illumination meter type
	 */
	public static final int TYPE_ILLUMINATION = 5;
	
	/**
	 * Type of device: noise meter type
	 */
	public static final int TYPE_NOISE = 6;
	
	/**
	 * Type of device: emmision meter type
	 */
	public static final int TYPE_EMMISION = 7;
	
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
