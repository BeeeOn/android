package cz.vutbr.fit.iha.gcm;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import cz.vutbr.fit.iha.Constants;

public class GcmRegisterAsyncTask extends AsyncTask<Context, Void, Void> {
	/**
	 * Minimum delay in milliseconds after register GCM fail and then exponentially more.
	 */
	private static final int MIN_SLEEP_TIME_GCM = 5;

	private String mRegId = null;
	private GoogleCloudMessaging mGcm;

	@Override
	protected Void doInBackground(Context... params) {
		Context context = params[0];
		if (mGcm == null) {
			mGcm = GoogleCloudMessaging.getInstance(context);
		}
		int timeToSleep = MIN_SLEEP_TIME_GCM;
		int attempt = 0;
		while (mRegId == null || mRegId == "") {
			attempt++;
			try {
				mRegId = mGcm.register(Constants.PROJECT_NUMBER);
			} catch (Exception e) {
				Log.e(GcmHelper.TAG_GCM, "Error: attempt n." + String.valueOf(attempt) + " :" + e.getMessage());
				/*
				 * No matter how many times you call register, it will always fail and throw an exception on some devices. On these devices we need to get GCM ID this way.
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
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				timeToSleep = timeToSleep * timeToSleep;
				continue;
			}
		}

		Log.i(GcmHelper.TAG_GCM, "Device registered, attempt number " + String.valueOf(attempt) + " , registration ID=" + mRegId);

		// For this demo: we don't need to send it because the device
		// will send upstream messages to a server that echo back the
		// message using the 'from' address in the message.

		// Persist the regID - no need to register again.
		GcmHelper.storeGCMRegistrationId(context, mRegId);

		return null;
	}

}
