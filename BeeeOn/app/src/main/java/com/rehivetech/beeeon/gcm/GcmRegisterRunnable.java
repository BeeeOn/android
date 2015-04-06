package com.rehivetech.beeeon.gcm;

import android.content.Context;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;

public class GcmRegisterRunnable implements Runnable {
	public static final String TAG = GcmRegisterRunnable.class.getSimpleName();

	/**
	 * Minimum delay in milliseconds after register GCM fail and then exponentially more.
	 */
	private static final int MIN_SLEEP_TIME_GCM = 5;
	private final Context mContext;
	private final Integer mMaxAttempts;
	private final Controller mController;
	private final String mOldGcmId;
	private String mNewGcmId = null;

	/**
	 * @param context
	 * @param maxAttempts Maximum attempts to get GCM ID, null for infinity
	 */
	public GcmRegisterRunnable(Context context, Integer maxAttempts) {
		this.mContext = context;
		this.mMaxAttempts = maxAttempts;
		this.mController = Controller.getInstance(context);
		this.mOldGcmId = mController.getGcmModel().getGCMRegistrationId();
	}

	@Override
	public void run() {
		// if there is no limit, set lower priority of this thread
		if (mMaxAttempts == null) {
			// Moves the current Thread into the background
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		}

		// if there is not Internet connection, locally invalidate and next event will try again to get new GCM ID 
		if (!Utils.isInternetAvailable(mContext)) {
			Log.w(TAG, GcmHelper.TAG_GCM + "No Internet, locally invalidate GCM ID");
			GcmHelper.invalidateLocalGcmId(mController);
			return;
		}

		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(mContext);
		int timeToSleep = MIN_SLEEP_TIME_GCM;
		int attempt = 0;
		while (mNewGcmId == null || mNewGcmId.isEmpty()) {
			if (mMaxAttempts != null && attempt > mMaxAttempts) {
				break;
			}
			attempt++;

			try {
				mNewGcmId = gcm.register(Constants.PROJECT_NUMBER);
			} catch (Exception e) {
				Log.e(TAG, GcmHelper.TAG_GCM + "Error: attempt n." + String.valueOf(attempt) + " :" + e.getMessage());
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

		Log.i(TAG, GcmHelper.TAG_GCM + "Device registered, attempt number " + String.valueOf(attempt) + " , registration ID="
				+ mNewGcmId);

		// if new GCM ID is different then the old one, delete old on server side and apply new one
		if (!mOldGcmId.equals(mNewGcmId)) {
			mController.getGcmModel().deleteGCM(mController.getActualUser().getId(), mOldGcmId);

			// Persist the regID - no need to register again.
			mController.getGcmModel().setGCMIdLocal(mNewGcmId);
			mController.getGcmModel().setGCMIdServer(mNewGcmId);

		} else {
			Log.i(TAG, GcmHelper.TAG_GCM + "New GCM ID is the same, no need to change");
		}
	}

}
