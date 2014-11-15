package cz.vutbr.fit.iha.gcm;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.util.Log;

public class GcmRegisterRunnable implements Runnable {
	/**
	 * Minimum delay in milliseconds after register GCM fail and then exponentially more.
	 */
	private static final int MIN_SLEEP_TIME_GCM = 5;

	private String mRegId = null;
	private Context context;
	private Integer mMaxAttempts;

	/**
	 * @param context
	 * @param maxAttempts Maximum attempts to get GCM ID, null for infinity
	 */
	public GcmRegisterRunnable(Context context, Integer maxAttempts) {
		this.context = context;
		this.mMaxAttempts = maxAttempts;
	}

	@Override
	public void run() {
		// if there is no limit, set lower priority of this thread
		if (mMaxAttempts == null) {
			// Moves the current Thread into the background
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		}
		
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
		int timeToSleep = MIN_SLEEP_TIME_GCM;
		int attempt = 0;
		while (mRegId == null || mRegId == "") {
			if (mMaxAttempts != null && attempt > mMaxAttempts) {
				break;
			}
			attempt++;
			
			try {
				mRegId = gcm.register(Constants.PROJECT_NUMBER);
			} catch (Exception e) {
				Log.e(GcmHelper.TAG_GCM, "Error: attempt n." + String.valueOf(attempt) + " :" + e.getMessage());
				/*
				 * No matter how many times you call register, it will always fail and throw an exception on some
				 * devices. On these devices we need to get GCM ID this way.
				 */
				// final String registrationId =
				// context.getIntent().getStringExtra(
				// "registration_id");
				// if (registrationId != null && registrationId != "") {
				// mRegId = registrationId;
				// break;
				// }
				try {
					Thread.sleep(timeToSleep);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				timeToSleep = timeToSleep * 2;
				continue;
			}
		}

		Log.i(GcmHelper.TAG_GCM, "Device registered, attempt number " + String.valueOf(attempt) + " , registration ID="
				+ mRegId);

		// Persist the regID - no need to register again.
		Controller.getInstance(context.getApplicationContext()).setGCMRegistrationId(mRegId);

		// TODO odstranit stare ID (pokud bylo)
		// TODO odeslat zpravu o novem ID

	}

}
