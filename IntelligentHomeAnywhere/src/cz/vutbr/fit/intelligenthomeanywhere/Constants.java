package cz.vutbr.fit.intelligenthomeanywhere;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;

/**
 * Class for all constants
 * @author ThinkDeep
 *
 */
public final class Constants {
	public static final String LOCATION_CLICKED = "LOCATION_CLICKED";
	public static final String DEVICE_CLICKED = "DEVICE_CLICKED";
	public static final String ADAPTER_ID = "ADAPTER_ID";
	public static final String ADAPTER_VERSION = "ADAPTER_VERSION";
	public static final String LOGIN = "LOGIN";
	public static final String LOGIN_DEMO = "LOGIN_DEMO";
	public static final String LOGIN_COMM = "LOGIN_COMM";
	public static final String DEMO_COMMUNICATION = Environment.getExternalStorageDirectory().toString() + "/IHA/komunikace.xml";
	public static final String DEMO_LOGFILE = Environment.getExternalStorageDirectory().toString() + "/IHA/sensor0.log";
	private static Capabilities _capabilities;
	private static Context _context;
	
	public static final int IDLE = 99;
	public static final int BUTTON_ID = 42;
	public static final int NUMBERPICKER_ID = 666;
	public static final int NUMBERPICKER_IDII = 999;
	public static final int NAMELABEL_ID = 777;
	
	public static final int DEVICE_TYPE_TEMP = 1;
	
	/**
	 * Return state of sensors
	 * @param _switch string param with ON or OFF
	 * @return String Open or Close
	 */
	public static final String isOpen(String _switch){
		if(_switch.equals("ON"))
			return _context.getResources().getString(R.string.sensor_open);
		else return _context.getResources().getString(R.string.sensor_close);
	}
	
	/**
	 * Return color by state
	 * @param _switch string param with ON or OFF
	 * @return int with color GREEN or RED
	 */
	public static final int isOn(String _switch){
		if(_switch.equals("ON"))
			return Color.GREEN;
		else return Color.RED;
	}
	
	/**
	 * Set up main data object
	 * @param capabilities
	 */
	public static void SetCapabilities(Capabilities capabilities){
		_capabilities = capabilities;
	}
	public static Capabilities GetCapabilities(){
		return _capabilities;
	}
	
	/**
	 * Set up context of aplication
	 * @param context of aplication
	 */
	public static void setContext(Context context){
		_context = context;
	}
}
