package cz.vutbr.fit.iha.gcm;

import cz.vutbr.fit.iha.controller.Controller;
import android.content.Context;

public class GcmHelper {
	public static final String TAG_GCM = "IHA_GCM";

	/**
	 * Maximum attempts to get GCM ID. After reaching this attempts there will be created new thread to get GCM ID.
	 */
	private static final int MAX_GCM_ATTEMPTS = 3;

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's shared preferences.
	 */
	public static void registerGCMInBackground(Context context) {
//		Handler handler = new Handler();
//		handler.post(new GcmRegisterRunnable(context, null));
		Thread thread = new Thread(new GcmRegisterRunnable(context, null));
		thread.start();
	}
	
	/**
	 * Registers the application with GCM servers synchronously in the same thread. 
	 * Cannot be GUI thread because of network communication.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's shared preferences.
	 */
	public static void registerGCMInForeground(Context context) {
		registerGCMInForeground(context, MAX_GCM_ATTEMPTS);
	}
	
	public static void registerGCMInForeground(Context context, int maxAttempts) {
		GcmRegisterRunnable gcmReg = new GcmRegisterRunnable(context, maxAttempts);
		gcmReg.run();
	}
	
	public static void invalidateLocalGcmId(Controller controller) {
		controller.setGCMIdLocal("");
	}

}
