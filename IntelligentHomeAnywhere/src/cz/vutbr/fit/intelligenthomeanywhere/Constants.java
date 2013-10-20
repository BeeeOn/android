package cz.vutbr.fit.intelligenthomeanywhere;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;

/**
 * Class for all constants
 * @author ThinkDeep
 *
 */
public final class Constants {
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
	private static Adapter mAdapter;
	private static Context mContext;
	
	public static final int ADAPTER_OFFLINE = 0;
	public static final int ADAPTER_NOT_REGISTERED = 1;
	public static final int ADAPTER_READY = 2;
	
	public static final int IDLE = 99;
	public static final int BUTTON_ID = 42;
	public static final int NUMBERPICKER_ID = 666;
	public static final int NUMBERPICKER_IDII = 999;
	public static final int NAMELABEL_ID = 777;
		
	/**
	 * Return state of sensors
	 * @param value string param with ON or OFF
	 * @return String Open or Close
	 */
	public static final String isOpen(String value){
		if(value.equals("ON"))
			return mContext.getResources().getString(R.string.sensor_open);
		else return mContext.getResources().getString(R.string.sensor_close);
	}
	
	/**
	 * Return color by state
	 * @param value string param with ON or OFF
	 * @return int with color GREEN or RED
	 */
	public static final int isOn(String value){
		if(value.equals("ON"))
			return Color.GREEN;
		else return Color.RED;
	}
	
	/**
	 * Set up main data object
	 * @param adapter
	 */
	public static void setAdapter(Adapter adapter){
		mAdapter = adapter;
	}
	public static Adapter getAdapter(){
		return mAdapter;
	}
	
	/**
	 * Set up context of aplication
	 * @param context of aplication
	 */
	public static void setContext(Context context){
		mContext = context;
	}
	
}
