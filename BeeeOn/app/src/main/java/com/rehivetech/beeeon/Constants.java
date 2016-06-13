/**
 * Package for whole application resources
 */
package com.rehivetech.beeeon;

/**
 * Class for all constants
 *
 * @author ThinkDeep
 * @author Robyer
 */
public final class Constants {
	public static final int NO_ID = -1;
	public static final int NO_INDEX = -1;

	/**
	 * Persistence's shared preferences
	 */
	public static final String PERSISTENCE_PREF_FILENAME = "persistence_%s";
	public static final String PERSISTENCE_PREF_ACTIVE_GATE = "active_adapter";
	public static final String PERSISTENCE_PREF_IGNORE_NO_GATE = "ignore_no_adapter";
	public static final String PERSISTENCE_PREF_TIMEZONE = "pref_timezone";
	public static final String PERSISTENCE_PREF_UNAVAILABLE_MODULES = "pref_hide_unavailable_modules";

	public static final String PERSISTENCE_PREF_TEMPERATURE = "pref_temperature";
	public static final String PERSISTENCE_PREF_NOISE = "pref_noise";
	public static final String PERSISTENCE_PREF_EMISSION = "pref_emission";
	public static final String PERSISTENCE_PREF_HUMIDITY = "pref_humidity";
	public static final String PERSISTENCE_PREF_ILLUMINATION = "pref_illumination";
	public static final String PERSISTENCE_PREF_PRESSURE = "pref_pressure";
	public static final String PERSISTENCE_PREF_BATTERY = "pref_battery";
	public static final String PERSISTENCE_PREF_RSSI = "pref_rssi";
	public static final String PERSISTENCE_PREF_REFRESH = "pref_refresh";

	public static final String PERSISTENCE_PREF_LAST_USER_ID = "last_user_id";
	public static final String PERSISTENCE_PREF_LAST_AUTH_PROVIDER = "last_auth_provider";
	public static final String PERSISTENCE_PREF_LAST_AUTH_PARAMETER = "last_auth_parameter";
	public static final String PERSISTENCE_PREF_LAST_DEMO_MODE = "last_demo_mode";
	public static final String PERSISTENCE_PREF_LAST_MENU_ID = "last_content_tag";


	public static final String PERSISTENCE_PREF_USER_BT = "user_bt";
	public static final String PERSISTENCE_PREF_USER_ID = "user_id";
	public static final String PERSISTENCE_PREF_USER_EMAIL = "user_email";
	public static final String PERSISTENCE_PREF_USER_NAME = "user_name";
	public static final String PERSISTENCE_PREF_USER_SURNAME = "user_surname";
	public static final String PERSISTENCE_PREF_USER_GENDER = "user_gender";
	public static final String PERSISTENCE_PREF_USER_PICTURE = "user_picture";

	public static final String PERSISTENCE_PREF_LOGIN_CHOOSE_SERVER_MANUALLY = "network_select_server_manually";
	public static final String PERSISTENCE_PREF_LOGIN_SERVER = "network_server";

	public static final String PERSISTENCE_PREF_DASHBOARD_ITEMS = "dashboard_items";
	public static final String PERSISTENCE_KEY_DASHBOARD_MIGRATE = "dashboard_migrate";

	public static final String KEY_UNITS = "key_units";

	/**
	 * GCM preference keys
	 */
	public static final String PREF_GCM_REG_ID = "gcm_registration_id";
	public static final String PREF_GCM_APP_VERSION = "gcm_app_version";

	public static final String GCM_TAG = "BEEEON_GCM: ";

	/**
	 * GUI constants
	 */
	public static final int ADD_GATE_REQUEST_CODE = 1000;
	public static final int ADD_DEVICE_REQUEST_CODE = 1001;
	public static final int SETUP_DEVICE_REQUEST_CODE = 1002;

	public static final String SETUP_DEVICE_ACT_LOC = "SETUP_DEVICE_ACT_LOC";

	public static final String TUTORIAL_ADD_GATE_SHOWED = "TUTORIAL_ADD_GATE_SHOWED";

	public static final String TUTORIAL_ADD_DEVICE_SHOWED = "TUTORIAL_ADD_DEVICE_SHOWED";

	public static final String GUI_INTRO_PLAY = "GUI_INTRO_PLAY";

	public static final String GUI_MENU_DEVICES = "GUI_MENU_DEVICES";

	/**
	 * Broadcasts
	 */
	public static final String BROADCAST_PREFERENCE_CHANGED = "com.rehivetech.beeeon.BROADCAST_PREFERENCE_CHANGED";
	public static final String BROADCAST_USER_LOGIN = "com.rehivetech.beeeon.BROADCAST_USER_LOGIN";
	public static final String BROADCAST_USER_LOGOUT = "com.rehivetech.beeeon.BROADCAST_USER_LOGOUT";

	/**
	 * Actor change broadcasts
	 */
	public static final String BROADCAST_ACTOR_CHANGED = "com.rehivetech.beeeon.BROADCAST_ACTOR_CHANGED";
	public static final String BROADCAST_EXTRA_ACTOR_CHANGED_ID = "com.rehivetech.beeeon.EXTRA_ACTION_CHANGED_ID";
	public static final String BROADCAST_EXTRA_ACTOR_CHANGED_GATE_ID = "com.rehivetech.beeeon.BROADCAST_EXTRA_ACTOR_CHANGED_GATE_ID";

	/**
	 * Permissions codes for requesting
	 */
	public static final int PERMISSION_CODE_GET_ACCOUNTS = 1;
	public static final int PERMISSION_CODE_CAMERA = 2;
	public static final int PERMISSION_CODE_LOCATION = 3;
	public static final int PERMISSION_CODE_STORAGE = 4;
}
