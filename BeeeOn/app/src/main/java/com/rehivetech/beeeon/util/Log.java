/**
 * 
 */
package com.rehivetech.beeeon.util;

/**
 * @author ThinkDeep
 * 
 */
public class Log {

	public static final boolean ENABLED = true;

	public static void v(String tag, String msg) {
		if (ENABLED)
			android.util.Log.v(tag, msg);
	}

	public static void d(String tag, String msg) {
		if (ENABLED)
			android.util.Log.d(tag, msg);
	}

	public static void i(String tag, String msg) {
		if (ENABLED)
			android.util.Log.i(tag, msg);
	}

	public static void w(String tag, String msg) {
		if (ENABLED)
			android.util.Log.w(tag, msg);
	}

	public static void w(String tag, String msg, Throwable t) {
		if (ENABLED)
			android.util.Log.w(tag, msg, t);
	}

	public static void e(String tag, String msg) {
		if (ENABLED)
			android.util.Log.e(tag, msg);
	}

	public static void e(String tag, String msg, Throwable t) {
		if (ENABLED)
			android.util.Log.e(tag, msg);
	}

}
