package com.rehivetech.beeeon.persistence;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.gcm.GcmHelper;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.network.Network;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;

public class GcmModel {

	private static final String TAG = GcmModel.class.getSimpleName();

	private final Context mContext;
	private final INetwork mNetwork;
	private final Persistence mPersistence;
	private final User mUser;

	public GcmModel(Context context, INetwork network, Persistence persistence, User user) {
		mContext = context;
		mNetwork = network;
		mPersistence = persistence;
		mUser = user;
	}

	public void registerGCM() {
		// Send GCM ID to server
		final String gcmId = getGCMRegistrationId();
		if (gcmId.isEmpty()) {
			GcmHelper.registerGCMInBackground(mContext);
			Log.w(TAG, GcmHelper.TAG_GCM + "GCM ID is not accessible in persistence, creating new thread");
		} else {
			// send GCM ID to server
			Thread t = new Thread() {
				public void run() {
					try {
						setGCMIdServer(gcmId);
					} catch (Exception e) {
						// do nothing
						Log.w(TAG, GcmHelper.TAG_GCM + "Login: Sending GCM ID to server failed: " + e.getLocalizedMessage());
					}
				}
			};
			t.start();
			setGCMIdLocal(gcmId);
		}
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @return registration ID, or empty string if there is no existing registration ID.
	 */
	public String getGCMRegistrationId() {
		String registrationId = mPersistence.loadGCMRegistrationId();
		if (registrationId.isEmpty()) {
			Log.i(TAG, GcmHelper.TAG_GCM + "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = mPersistence.loadLastApplicationVersion();
		int currentVersion = Utils.getAppVersion(mContext);
		if (registeredVersion != currentVersion) {
			// delete actual GCM ID from server
			deleteGCM(mUser.getId(), registrationId);
			mPersistence.saveGCMRegistrationId("");
			Log.i(TAG, GcmHelper.TAG_GCM + "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Delete GCM ID from user
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param userId
	 * @param gcmId - if null, then it will be loaded automatically by calling getGCMRegistrationId()
	 */
	public void deleteGCM(final String userId, final String gcmId) {
		if (mNetwork instanceof Network) {
			// delete GCM ID from server
			Thread t = new Thread() {
				public void run() {
					String id = (gcmId != null) ? gcmId : getGCMRegistrationId();

					if (userId.isEmpty() || id.isEmpty())
						return;

					try {
						((Network) mNetwork).deleteGCMID(userId, gcmId);
					} catch (AppException e) {
						// do nothing
						Log.w(TAG, GcmHelper.TAG_GCM + "Delete GCM ID failed: " + e.getLocalizedMessage());
					}
				}
			};
			t.start();
		}
	}

	/**
	 * Stores the registration ID and app versionCode in the application's {@code SharedPreferences}.
	 *
	 * @param gcmId
	 *            registration ID
	 */
	public void setGCMIdLocal(String gcmId) {
		int appVersion = Utils.getAppVersion(mContext);
		Log.i(TAG, GcmHelper.TAG_GCM + "Saving GCM ID on app version " + appVersion);

		mPersistence.saveGCMRegistrationId(gcmId);
		mPersistence.saveLastApplicationVersion(appVersion);
	}

	/**
	 * Method set gcmID to server (applied only if there is some user)
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param gcmID
	 *            to be set
	 */
	public void setGCMIdServer(String gcmID) {
		Log.i(TAG, GcmHelper.TAG_GCM + "setGcmIdServer");
		if (Controller.isDemoMode()) {
			Log.i(TAG, GcmHelper.TAG_GCM + "DemoMode -> return");
			return;
		}

		if (mUser.getId().isEmpty()) {
			// no user, it will be sent in user login
			return;
		}

		try {
			Log.i(TAG, GcmHelper.TAG_GCM + "Set GCM ID to server: " + gcmID);
			if (mNetwork instanceof Network) {
				((Network) mNetwork).setGCMID(gcmID);
			}
		} catch (AppException e) {
			// nothing to do
			Log.e(TAG, GcmHelper.TAG_GCM + "Set GCM ID to server failed.");
		}
	}

}
