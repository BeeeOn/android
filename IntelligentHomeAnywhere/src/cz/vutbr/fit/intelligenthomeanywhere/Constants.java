package cz.vutbr.fit.intelligenthomeanywhere;

import android.content.Context;
import android.os.Environment;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;

/**
 * Class for all constants
 * @author ThinkDeep
 *
 */
public final class Constants {
	private static Adapter mAdapter;
	private static Context mContext;
	
	public static final String LOCATION_CLICKED = "LOCATION_CLICKED";
	public static final String DEVICE_CLICKED = "DEVICE_CLICKED";
	public static final String LOCATION_LONG_PRESS = "LOCATION_LONG_PRESS";
	public static final String DEVICE_LONG_PRESS = "DEVICE_LONG_PRESS"; 
	public static final String ADAPTER_SERIAL_NUMBER = "ADAPTER_SERIAL_NUMBER";
	public static final String ADAPTER_ID = "ADAPTER_ID";
	public static final String ADAPTER_VERSION = "ADAPTER_VERSION";
	public static final String SPLASH = "SPLASH";
	public static final String LOGIN = "LOGIN";
	public static final String LOGIN_DEMO = "LOGIN_DEMO";
	public static final String LOGIN_COMM = "LOGIN_COMM";
	public static final String DEMO_COMMUNICATION = Environment.getExternalStorageDirectory().toString() + "/IHA/komunikace.xml";
	public static final String DEMO_LOGFILE = Environment.getExternalStorageDirectory().toString() + "/IHA/sensor0.log";	
	
	public static final int ADAPTER_OFFLINE = 0;
	public static final int ADAPTER_NOT_REGISTERED = 1;
	public static final int ADAPTER_READY = 2;
	
	public static final int IDLE = 99;
	public static final int BUTTON_ID = 42;
	public static final int NUMBERPICKER_ID = 666;
	public static final int NUMBERPICKER_IDII = 999;
	public static final int NAMELABEL_ID = 777;
	
	// Device types
	public static final int TYPE_UNKNOWN = -1;
	public static final int TYPE_TEMPERATURE = 0;
	public static final int TYPE_HUMIDITY = 1;
	public static final int TYPE_PRESSURE = 2;
	public static final int TYPE_STATE = 3;
	public static final int TYPE_SWITCH = 4;
	public static final int TYPE_ILLUMINATION = 5;
	public static final int TYPE_NOISE = 6;
	public static final int TYPE_EMMISION = 7;
		
	/**
	 * Set up main adapter object
	 * @param adapter object
	 */
	public static void setAdapter(Adapter adapter){
		mAdapter = adapter;
	}
	public static Adapter getAdapter(){
		// FIXME: get Adapter if doesn't exists
		// e.g. because our application process get killed by Android
		// and then is started again (but without initializing this) 
		/*if (mAdapter == NULL)
			mAdapter = ... */
		
		return mAdapter;
	}
	
	/**
	 * Set up context of aplication
	 * @param context of aplication
	 */
	public static void setContext(Context context){
		mContext = context;
	}	
	public static Context getContext() {
		// FIXME: get Context if doesn't exists
		// e.g. because our application process get killed by Android
		// and then is started again (but without initializing this) 
		/*if (mContext == NULL)
			mContext = ... */
			
		return mContext;
	}
	
}
