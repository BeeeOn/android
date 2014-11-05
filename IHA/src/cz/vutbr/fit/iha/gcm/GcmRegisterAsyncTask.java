package cz.vutbr.fit.iha.gcm;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmRegisterAsyncTask extends AsyncTask<Context, Void, Void> {
	/**
	 * Minimum delay in milliseconds after register GCM fail and then exponentially more.
	 */
	private static final int MIN_SLEEP_TIME_GCM = 5;

	private String mRegId = null;
	private GoogleCloudMessaging mGcm;

	@Override
	protected Void doInBackground(Context... params) {
		// FIXME: GCM

		/*
		 * Context context = params[0]; if (mGcm == null) { mGcm = GoogleCloudMessaging.getInstance(context); } int timeToSleep = MIN_SLEEP_TIME_GCM; int attempt = 0; while (mRegId == null || mRegId
		 * == "") { attempt++; try { mRegId = mGcm.register(Constants.PROJECT_NUMBER); } catch (Exception e) { Log.e(GcmHelper.TAG_GCM, "Error: attempt n." + String.valueOf(attempt) + " :" +
		 * e.getMessage()); // No matter how many times you call register, it will always fail and throw an exception on some devices. On these devices we need to get GCM ID this way.
		 * 
		 * // final String registrationId = // context.getIntent().getStringExtra( // "registration_id"); // if (registrationId != null && registrationId != "") { // mRegId = registrationId; // break;
		 * // } try { Thread.sleep(timeToSleep); } catch (InterruptedException e1) { e1.printStackTrace(); } timeToSleep = timeToSleep * 2; continue; } }
		 * 
		 * Log.i(GcmHelper.TAG_GCM, "Device registered, attempt number " + String.valueOf(attempt) + " , registration ID=" + mRegId);
		 * 
		 * // Persist the regID - no need to register again. Controller.getInstance(context.getApplicationContext()).setGCMRegistrationId(mRegId);
		 */

		return null;
	}

}
