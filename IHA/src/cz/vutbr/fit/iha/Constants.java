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
	 * Error codes for communication layer
	 */
	public static final int ERR_COM_VER_MISMATCH = 1;
	public static final int ERR_NOT_VALID_USER = 2;
	public static final int ERR_ADAPTER_NOT_EXISTS = 5;
	public static final int ERR_ADAPTER_NOT_FREE = 6;
	public static final int ERR_ADAPTER_HAVE_YET = 7;
	public static final int ERR_BAD_AGREG_FUNC = 8;
	public static final int ERR_BAD_INTERVAL = 9;
	public static final int ERR_NOT_CONSISTENT_SENSOR_ADDR = 10;
	public static final int ERR_BAD_LOCATION_TYPE = 11;
	public static final int ERR_DAMAGED_XML = 12;
	public static final int ERR_NO_SUCH_ENTITY = 13;
	public static final int ERR_BAD_ICON = 14;
	public static final int ERR_BAD_ACTION = 15;
	public static final int ERR_LOW_RIGHTS = 16;
	public static final int ERR_BAD_EMAIL_OR_ROLE = 17;
	public static final int ERR_BAD_UTC = 18;
	public static final int ERR_BAD_ACTOR_VALUE = 19;
	public static final int ERR_BAD_UID = 20;
	public static final int ERR_ADA_SERVER_PROBLEM = 100;
	
	
	
}
