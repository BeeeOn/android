/*! \mainpage 
 * \section todo doxygen main page :-)
 * Some awesome first page here
 * \section other important stuffs
 * Put Here
 */

/**
 * @brief Package for whole application resources
 */
package cz.vutbr.fit.intelligenthomeanywhere;

import android.content.Context;
import android.os.Environment;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;

/**
 * @brief Class for all constants
 * @author ThinkDeep
 */
public final class Constants {
	private static Adapter mAdapter;
	private static Context mContext;
	
	/**
	 * Footing for pass from LocationScreenActivity to DataOfLocationScreenActivity
	 */
	public static final String LOCATION_CLICKED = "LOCATION_CLICKED";
	
	/**
	 * Footing for pass from DataOfLocationScreenActivity to SensorDetailActivity
	 */
	public static final String DEVICE_CLICKED = "DEVICE_CLICKED";
	
	/**
	 * Footing for pass from LocationScreenActivity to ChangeLocationNameActivity
	 */
	public static final String LOCATION_LONG_PRESS = "LOCATION_LONG_PRESS";
	
	/**
	 * Footing for pass from DataOfLocationScreenActivity to ChangeDeviceNameActivity
	 */
	public static final String DEVICE_LONG_PRESS = "DEVICE_LONG_PRESS"; 
	
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
	 * Footing for initial screen
	 */
	public static final String SPLASH = "SPLASH";
	
	/**
	 * Footing for pass from some activities to LocationScreenActivity
	 */
	public static final String LOGIN = "LOGIN";
	
	/**
	 * Footing for pass from SplashActivity to LocationScreenActivity
	 */
	public static final String LOGIN_DEMO = "LOGIN_DEMO";
	
	/**
	 * Footing for pass from LoginActivity to LocationScreenActivity
	 */
	public static final String LOGIN_COMM = "LOGIN_COMM";
	
	/**
	 * Path to demo file with prepared household sensors
	 */
	public static final String DEMO_COMMUNICATION = Environment.getExternalStorageDirectory().toString() + "/IHA/komunikace.xml";
	
	/**
	 * Path to demo file with prepared household log info from one sensor
	 */
	public static final String DEMO_LOGFILE = Environment.getExternalStorageDirectory().toString() + "/IHA/sensor0.log";	
	
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
	 * Set up main adapter object
	 * @param adapter object
	 */
	public static void setAdapter(Adapter adapter){
		mAdapter = adapter;
	}
	/**
	 * Returning actual adapter
	 * @return Adapter object
	 */
	public static Adapter getAdapter(){
		// FIXME: get Adapter if doesn't exists
		// e.g. because our application process get killed by Android
		// and then is started again (but without initializing this) 
		/*if (mAdapter == NULL)
			mAdapter = ... */
		
		return mAdapter;
	}
	
	/**
	 * Set up context of application
	 * @param context of application
	 */
	public static void setContext(Context context){
		mContext = context;
	}
	
	/**
	 * Returning actual context
	 * @return Context object
	 */
	public static Context getContext() {
		// FIXME: get Context if doesn't exists
		// e.g. because our application process get killed by Android
		// and then is started again (but without initializing this) 
		/*if (mContext == NULL)
			mContext = ... */
			
		return mContext;
	}
	
}
