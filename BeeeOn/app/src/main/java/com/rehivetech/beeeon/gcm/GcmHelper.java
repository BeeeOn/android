package com.rehivetech.beeeon.gcm;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Utils;

public class GcmHelper {

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p/>
	 * Stores the registration ID and app versionCode in the application's shared preferences.
	 */
	public static void registerGCMInBackground(Context context) {
//		Handler handler = new Handler();
//		handler.post(new GcmRegisterRunnable(context, null));
		if (!Utils.isGooglePlayServicesAvailable(context)) {
			return;
		}
		Thread thread = new Thread(new GcmRegisterRunnable(context, null));
		thread.start();
	}

	/**
	 * Registers the application with GCM servers synchronously in the same thread.
	 * Cannot be GUI thread because of network communication.
	 * <p/>
	 * Stores the registration ID and app versionCode in the application's shared preferences.
	 */
	public static void registerGCMInForeground(Context context) {
		registerGCMInForeground(context, MAX_GCM_ATTEMPTS);
	}

	public static void registerGCMInForeground(Context context, int maxAttempts) {
		if (!Utils.isGooglePlayServicesAvailable(context)) {
			return;
		}
		GcmRegisterRunnable gcmReg = new GcmRegisterRunnable(context, maxAttempts);
		gcmReg.run();
	}

}
