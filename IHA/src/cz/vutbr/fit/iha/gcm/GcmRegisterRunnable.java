package cz.vutbr.fit.iha.gcm;

import android.content.Context;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.util.Log;

public class GcmRegisterRunnable implements Runnable {
	/**
	 * Minimum delay in milliseconds after register GCM fail and then exponentially more.
	 */
	private static final int MIN_SLEEP_TIME_GCM = 5;

	private String mNewGcmId = null;
	private final Context mContext;
	private final Integer mMaxAttempts;
	private final Controller mController;
	private final String mOldGcmId;

	/**
	 * @param context
	 * @param maxAttempts
	 *            Maximum attempts to get GCM ID, null for infinity
	 */
	public GcmRegisterRunnable(Context context, Integer maxAttempts) {
		this.mContext = context;
		this.mMaxAttempts = maxAttempts;
		this.mController = Controller.getInstance(context);
		this.mOldGcmId = mController.getGCMRegistrationId();
	}

	@Override
	public void run() {
		// if there is no limit, set lower priority of this thread
		if (mMaxAttempts == null) {
			// Moves the current Thread into the background
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		}

		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(mContext);
		int timeToSleep = MIN_SLEEP_TIME_GCM;
		int attempt = 0;
		while (mNewGcmId == null || mNewGcmId == "") {
			if (mMaxAttempts != null && attempt > mMaxAttempts) {
				break;
			}
			attempt++;

			try {
				mNewGcmId = gcm.register(Constants.PROJECT_NUMBER);
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
				+ mNewGcmId);

		// if new GCM ID is different then the old one, delete old on server side and apply new one
		if (!mOldGcmId.equals(mNewGcmId)) {

			if (!mOldGcmId.isEmpty()) {
				final String email = mController.getLastEmail();
				if (!email.isEmpty()) {
					Thread t = new Thread() {
						public void run() {
							Thread t = new Thread() {
								public void run() {
									try {
										mController.deleteGCM(email, mOldGcmId);
									} catch (Exception e) {
										// do nothing
										Log.w(GcmHelper.TAG_GCM,
												"Logout: Delete GCM ID failed: " + e.getLocalizedMessage());
									}
								}
							};
							t.start();
						}
					};
				}
			}

			// Persist the regID - no need to register again.
			mController.setGCMIdLocal(mNewGcmId);
			mController.setGCMIdServer(mNewGcmId);

		}
	}

}
