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

	/**
	 * Network layer version
	 */
	public static final String COM_VER = "2.5";

	/**
	 * Assets with prepared devices and locations for demo mode
	 */
	public static final String ASSET_GATES_FILENAME = "adapters.xml";
	public static final String ASSET_GATE_DATA_FILENAME = "adapter_%s.xml";
	public static final String ASSET_LOCATIONS_FILENAME = "locations_%s.xml";
	public static final String ASSET_WATCHDOGS_FILENAME = "watchdogs_%s.xml";
	public static final String ASSET_ACHIEVEMENTS_FILENAME = "achievements_%s.xml";

	/**
	 * Persistence's shared preferences
	 */
	public static final String PERSISTENCE_PREF_FILENAME = "persistence_%s";
	public static final String PERSISTENCE_PREF_ACTIVE_GATE = "active_adapter";
	public static final String PERSISTENCE_PREF_IGNORE_NO_GATE = "ignore_no_adapter";
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

	public static final String PERSISTENCE_PREF_LAST_USER_ID = "last_user_id";
	public static final String PERSISTENCE_PREF_LAST_AUTH_PROVIDER = "last_auth_provider";
	public static final String PERSISTENCE_PREF_LAST_AUTH_PARAMETER = "last_auth_parameter";
	public static final String PERSISTENCE_PREF_LAST_DEMO_MODE = "last_demo_mode";

	public static final String PERSISTENCE_PREF_USER_BT = "user_bt";
	public static final String PERSISTENCE_PREF_USER_ID = "user_id";
	public static final String PERSISTENCE_PREF_USER_EMAIL = "user_email";
	public static final String PERSISTENCE_PREF_USER_NAME = "user_name";
	public static final String PERSISTENCE_PREF_USER_SURNAME = "user_surname";
	public static final String PERSISTENCE_PREF_USER_GENDER = "user_gender";
	public static final String PERSISTENCE_PREF_USER_PICTURE = "user_picture";

	public static final String PERSISTENCE_PREF_LOGIN_FACEBOOK = "login_facebook";
	public static final String PERSISTENCE_PREF_LOGIN_TWITTER = "login_twitter";
	public static final String PERSISTENCE_PREF_LOGIN_VKONTAKTE = "login_vkontakte";

	public static final String PERSISTENCE_PREF_LOGIN_CHOOSE_SERVER_MANUALLY = "login_choose_server_manually";

	public static final String PERSISTENCE_APP_IMAGE_DIR = "image_dir";

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
	public static final String PROJECT_NUMBER = "72175563561";

	/**
	 * Google web login constants
	 */
	public static final String WEB_LOGIN_CLIENT_ID = "72175563561-ustmn77c152m6o819sf3htgrhch3rjcq.apps.googleusercontent.com";
	public static final String WEB_LOGIN_SECRET = "NzG3qF8v6I0V0Xfe37GeeZq0";

	/**
	 * GUI constants
	 */

	public static final int ADD_GATE_REQUEST_CODE = 1000;
	public static final int EDIT_SENSOR_REQUEST_CODE = 1001;
	public static final int ADD_SENSOR_REQUEST_CODE = 1002;
	public static final int SETUP_SENSOR_REQUEST_CODE = 1003;

	public static final int SHARE_TWITTER = 1050;
	public static final int SHARE_GOOGLE = 1051;
	public static final int SHARE_VKONTAKTE = 1052;

	public static final String SETUP_SENSOR_ACT_LOC = "SETUP_SENSOR_ACT_LOC";

	public static final boolean GUI_DEBUG = true;

	public static final String TUTORIAL_ADD_GATE_SHOWED = "TUTORIAL_ADD_GATE_SHOWED";

	public static final String TUTORIAL_ADD_SENSOR_SHOWED = "TUTORIAL_ADD_SENSOR_SHOWED";

	public static final String TUTORIAL_LOGIN_SHOWED = "TUTORIAL_LOGIN_SHOWED";

	public static final String GUI_MENU_ALL_SENSOR_ID = "GUI_MENU_ALL_SENSOR_ID";

	public static final String GUI_MENU_CONTROL = "GUI_MENU_CONTROL";

	public static final String GUI_MENU_DASHBOARD = "GUI_MENU_DASHBOARD";

	public static final String GUI_MENU_WATCHDOG = "GUI_MENU_WATCHDOG";

	public static final String GUI_MENU_PROFILE = "GUI_MENU_PROFILE";

	public static final String GUI_EDIT_SENSOR_ID = "GUI_EDIT_SENSOR_ID";

	public static final String GUI_INTRO_PLAY = "GUI_INTRO_PLAY";

	/**
	 * Broadcasts
	 */

	public static final String BROADCAST_PREFERENCE_CHANGED = "com.rehivetech.beeeon.BROADCAST_PREFERENCE_CHANGED";
	public static final String BROADCAST_USER_LOGIN = "com.rehivetech.beeeon.BROADCAST_USER_LOGIN";
	public static final String BROADCAST_USER_LOGOUT = "com.rehivetech.beeeon.BROADCAST_USER_LOGOUT";

	/**
	 * Achievements ID
	 */
	public static final String ACHIEVEMENT_LOGIN = "1";
	public static final String ACHIEVEMENT_ROOM = "4";
	public static final String ACHIEVEMENT_TUTORIAL = "6";
	public static final String ACHIEVEMENT_WIDGET_CLOCK = "7";
	public static final String ACHIEVEMENT_WIDGET_ANY = "8";
	public static final String ACHIEVEMENT_GEOFENCE = "9";
	public static final String ACHIEVEMENT_FACEBOOK_LOGIN = "31";
	public static final String ACHIEVEMENT_FACEBOOK_SHARE = "32";
	public static final String ACHIEVEMENT_TWITTER_LOGIN = "34";
	public static final String ACHIEVEMENT_TWITTER_SHARE = "35";
	public static final String ACHIEVEMENT_VKONTAKTE_LOGIN = "37";
	public static final String ACHIEVEMENT_VKONTAKTE_SHARE = "38";
	public static final String ACHIEVEMENT_GPLUS_SHARE = "40";
	public static final String ACHIEVEMENT_USER_ADMIN = "50";
	public static final String ACHIEVEMENT_USER_NORMAL = "51";
	public static final String ACHIEVEMENT_USER_GUEST = "52";
	public static final String ACHIEVEMENT_ADD_GATEWAY = "61";
	public static final String ACHIEVEMENT_ADD_SENSOR = "64";

	// actor change
	public static final String BROADCAST_ACTOR_CHANGED = "com.rehivetech.beeeon.BROADCAST_ACTOR_CHANGED";
	public static final String BROADCAST_EXTRA_ACTOR_CHANGED_ID = "com.rehivetech.beeeon.EXTRA_ACTION_CHANGED_ID";
	public static final String BROADCAST_EXTRA_ACTOR_CHANGED_GATE_ID = "com.rehivetech.beeeon.BROADCAST_EXTRA_ACTOR_CHANGED_GATE_ID";

	// The limit time for adding device in addDeviceFragment
	public static final int PAIR_TIME_SEC = 30;
}
