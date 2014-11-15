package cz.vutbr.fit.iha.gcm;

import cz.vutbr.fit.iha.util.Log;
import android.content.Context;
import android.os.Handler;

public class GcmHelper {
	public static final String TAG_GCM = "IHA_GCM";

	/**
	 * Maximum milliseconds which device will wait in thread for reparation GCM ID. If it isn't registered after this
	 * time, new thread will be created.
	 */
	private static final int MAX_GCM_ATTEMPTS = 3;

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's shared preferences.
	 */
	public static void registerGCMInBackground(Context context) {
//		new GcmRegisterAsyncTask().execute(context, null, null);
		Handler handler = new Handler();
		handler.post(new GcmRegisterRunnable(context, null));
	}

	public static void registerGCMInForeground(Context context) {
		registerGCMInForeground(context, MAX_GCM_ATTEMPTS);
	}
	
	public static void registerGCMInForeground(Context context, int maxAttempts) {
		GcmRegisterRunnable gcmReg = new GcmRegisterRunnable(context, maxAttempts);
		gcmReg.run();
	}

}
