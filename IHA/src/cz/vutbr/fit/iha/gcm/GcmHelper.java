package cz.vutbr.fit.iha.gcm;

import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.util.Log;

public class GcmHelper {
	public static final String TAG_GCM = "IHA_GCM";

	/**
	 * Maximum milliseconds which device will wait in thread for reparation GCM ID. If it isn't registered after this time, new thread will be created.
	 */
	private static final int MAX_MILLSEC_GCM = 1000;

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's shared preferences.
	 */
	public static void registerGCMInBackground(Context context) {
		new GcmRegisterAsyncTask().execute(context, null, null);
	}

	public static void registerGCMInForeground(Context context) {
		try {
			new GcmRegisterAsyncTask().execute(context, null, null).get(MAX_MILLSEC_GCM, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			Log.e(TAG_GCM, "Couldnt get GCM ID in given time.");
			e.printStackTrace();
		}
	}

}
