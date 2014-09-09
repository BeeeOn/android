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
	public static final String ASSET_ADAPTERS_FILENAME = "adapters.xml";
	public static final String ASSET_ADAPTER_DATA_FILENAME = "adapter_%s.xml";
	public static final String ASSET_LOCATIONS_FILENAME = "locations_%s.xml";
	
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
	public static final String PERSISTENCE_PREF_IGNORE_NO_ADAPTER = "ignore_no_adapter";
	public static final String PERSISTENCE_PREF_LAST_LOCATION = "location_%s";
	public static final String PERSISTANCE_PREF_SW2_ADAPTER = "pref_sw2_adapter";
	public static final String PERSISTANCE_PREF_SW2_LOCATION = "pref_sw2_location";
	public static final String PERSISTANCE_PREF_TEMPERATURE = "pref_temperature";
	public static final String PERSISTANCE_PREF_TIMEZONE = "pref_timezone";
	
	public static final String KEY_UNITS = "key_units";
	
	/**
	 * Keys for bundle
	 */
	public static final String ADDSENSOR_COUNT_SENSOR = "countofuninitsensor";
	public static final String CANCEL = "cancel";
	public static final String NOADAPTER = "noadapter";
	
	/**
	 *  Project number from the API Console
	 */
	public static final String PROJECT_NUMBER = "863203863728";
	
	/**
	 * GCM preference name
	 */
	public static final String SHARED_PREF_GCM_NAME = "shared_pref_gcm";
	
	/**
	 * GCM preference keys
	 */
	public static final String PREF_GCM_REG_ID = "gcm_registration_id";
    public static final String PREF_GCM_APP_VERSION = "gcm_app_version";
}

