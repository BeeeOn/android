/**
 * Package for whole application resources
 */
package cz.vutbr.fit.iha;

/**
 * Class for all constants
 * 
 * @author ThinkDeep
 * @author Robyer
 */
public final class Constants {

	/**
	 * Network layer version
	 */
	public static final String COM_VER = "2.3";

	/**
	 * Assets with prepared devices and locations for demo mode
	 */
	public static final String ASSET_ADAPTERS_FILENAME = "adapters.xml";
	public static final String ASSET_ADAPTER_DATA_FILENAME = "adapter_%s.xml";
	public static final String ASSET_LOCATIONS_FILENAME = "locations_%s.xml";

	/**
	 * Persistence's shared preferences
	 */
	public static final String PERSISTENCE_PREF_FILENAME = "persistence_%s";
	public static final String PERSISTENCE_PREF_LAST_USER = "last_user";
	public static final String PERSISTENCE_PREF_ACTIVE_ADAPTER = "active_adapter";
	public static final String PERSISTENCE_PREF_IGNORE_NO_ADAPTER = "ignore_no_adapter";
	public static final String PERSISTENCE_PREF_LAST_LOCATION = "location_%s";
	public static final String PERSISTENCE_PREF_SW2_ADAPTER = "pref_sw2_adapter";
	public static final String PERSISTENCE_PREF_SW2_LOCATION = "pref_sw2_location";
	public static final String PERSISTENCE_PREF_TIMEZONE = "pref_timezone";

	public static final String PERSISTENCE_PREF_TEMPERATURE = "pref_temperature";
	public static final String PERSISTENCE_PREF_NOISE = "pref_noise";
	public static final String PERSISTENCE_PREF_EMISSION = "pref_emission";
	public static final String PERSISTENCE_PREF_HUMIDITY = "pref_humidity";
	public static final String PERSISTENCE_PREF_ILLUMINATION = "pref_illumination";
	public static final String PERSISTENCE_PREF_PRESSURE = "pref_pressure";

	public static final String KEY_UNITS = "key_units";
	public static final String KEY_GEOFENCE = "key_geofence";

	/**
	 * GCM preference keys
	 */
	public static final String PREF_GCM_REG_ID = "gcm_registration_id";
	public static final String PREF_GCM_APP_VERSION = "gcm_app_version";

	/**
	 * Project number from the API Console
	 */
	public static final String PROJECT_NUMBER = "863203863728";
	
	/**
	 * GUI constants
	 */
	
	public static final int ADD_ADAPTER_REQUEST_CODE = 1000;
	public static final int ADD_ADAPTER_CANCELED = 1001;
	public static final int ADD_ADAPTER_SUCCESS = 1002;

	public static final int ADD_SENSOR_CANCELED = 1003;

	public static final int ADD_SENSOR_REQUEST_CODE = 1004;

	public static final int SETUP_SENSOR_REQUEST_CODE = 1005;

	public static final int SETUP_SENSOR_CANCELED = 1006;

	public static final int SETUP_SENSOR_SUCCESS = 1007;
	
	public static final String SETUP_SENSOR_ACT_LOC = "SETUP_SENSOR_ACT_LOC";

	public static final int ADD_SENSOR_SUCCESS = 1008;
	
	public static final boolean GUI_DEBUG = true;
	 

}
