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
	public static final String COM_VER = "2.1";

	/**
	 * Assets with prepared devices and locations for demo mode
	 */
	public static final String ASSET_ADAPTERS_FILENAME = "adapters.xml";
	public static final String ASSET_ADAPTER_DATA_FILENAME = "adapter_%s.xml";
	public static final String ASSET_LOCATIONS_FILENAME = "locations_%s.xml";

	/**
	 * Device's types
	 */
	public static final int TYPE_UNKNOWN = -1; // unknown device
	public static final int TYPE_TEMPERATURE = 0; // temperature meter
	public static final int TYPE_HUMIDITY = 1; // humidity meter
	public static final int TYPE_PRESSURE = 2; // pressure meter
	public static final int TYPE_STATE = 3; // state sensor
	public static final int TYPE_SWITCH = 4; // switch sensor
	public static final int TYPE_ILLUMINATION = 5; // illumination meter
	public static final int TYPE_NOISE = 6; // noise meter
	public static final int TYPE_EMMISION = 7; // emmision meter

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
	public static final String PERSISTENCE_PREF_TEMPERATURE = "pref_temperature";
	public static final String PERSISTENCE_PREF_TIMEZONE = "pref_timezone";

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

}
