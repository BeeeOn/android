/**
 * Package for whole application resources
 */
package cz.vutbr.fit.iha;


/**
 * Class for all constants
 * @author ThinkDeep
 * @author Robyer
 */
public final class Constants {
	
	/**
	 * Footing for pass between activities 
	 */
	public static final String LOCATION_CLICKED = "LOCATION_CLICKED"; 			// LocationScreenActivity -> DataOfLocationScreenActivity
	public static final String DEVICE_CLICKED = "DEVICE_CLICKED"; 				// DataOfLocationScreenActivity -> SensorDetailActivity
	public static final String ADAPTER_SERIAL_NUMBER = "ADAPTER_SERIAL_NUMBER";	// AddAdapterActivity -> RegistrationActivity
	
	/**
	 * Not used
	 */
	public static final String ADAPTER_ID = "ADAPTER_ID";
	public static final String ADAPTER_VERSION = "ADAPTER_VERSION";
	
	/**
	 * Assets with prepared devices and locations for demo mode
	 */
	public static final String ASSET_ADAPTERS_FILENAME = "komunikace.xml";
	public static final String ASSET_LOCATIONS_FILENAME = "lokace.xml";
	
	/**
	 * Device's types
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
	public static final String PERSISTENCE_PREF_FILENAME = "persistence_%s";
	public static final String PERSISTENCE_PREF_LAST_USER = "last_user";
	public static final String PERSISTENCE_PREF_ACTIVE_ADAPTER = "active_adapter";
	
	
	/**
	 * Settings keys for SharedPreferences
	 */
	public static final String PREF_SW2_ADAPTER = "pref_sw2_adapter";
	public static final String PREF_SW2_LOCATION = "pref_sw2_location";
	public static final String PREF_TEMPERATURE = "pref_temperature";
	public static final String KEY_UNITS = "key_units";
	
	/**
	 * Keys for bundle
	 */
	public static final String ADDSENSOR_COUNT_SENSOR = "countofuninitsensor";
}

