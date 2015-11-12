package com.rehivetech.beeeon.model;

import android.content.Context;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.gcm.INotificationReceiver;
import com.rehivetech.beeeon.gcm.notification.IGcmNotification;
import com.rehivetech.beeeon.gcm.notification.VisibleNotification;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.network.server.Network;
import com.rehivetech.beeeon.persistence.Persistence;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

public class GcmModel extends BaseModel {

	private static final String TAG = GcmModel.class.getSimpleName();

	private final Context mContext;
	private final Persistence mPersistence;
	private final User mUser;

	/**
	 * Weak map for holding registered notification receivers
	 */
	private final WeakHashMap<INotificationReceiver, Boolean> mNotificationReceivers = new WeakHashMap<>();

	public GcmModel(INetwork network, Context context, Persistence persistence, User user) {
		super(network);
		mContext = context;
		mPersistence = persistence;
		mUser = user;
	}

	/**
	 * This CAN'T be called on UI thread!
	 */
	public void registerGCM() {
		// If we don't have Play services, delete GCM ID from server (there might have been from previous login with play services)
		if (!Utils.isGooglePlayServicesAvailable(mContext)) {
			setGCMIdLocal("");
			setGCMIdServer("");
			return;
		}

		final String gcmId = getGCMRegistrationId();
		if (!gcmId.isEmpty()) {
			// Just send existing GCM ID to server
			setGCMIdServer(gcmId);
			return;
		}

		// Get new GCM ID via new thread
		Log.w(TAG, Constants.GCM_TAG + "GCM ID is not accessible in persistence, creating new thread");

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				// Moves the current Thread into the background
				android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

				Controller controller = Controller.getInstance(mContext);
				GcmModel gcmModel = controller.getGcmModel();

				// if there is not Internet connection, locally invalidate and next event will try again to get new GCM ID
				if (!Utils.isInternetAvailable(mContext)) {
					Log.w(TAG, Constants.GCM_TAG + "No Internet, locally invalidate GCM ID");
					gcmModel.setGCMIdLocal("");
					return;
				}

				GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(mContext);
				int timeToSleep = 500; // Minimum delay in milliseconds after register GCM fail and then exponentially more.
				int attempt = 0;
				String gcmIdNew = null;
				while (gcmIdNew == null || gcmIdNew.isEmpty()) {
					attempt++;

					try {
						gcmIdNew = gcm.register(Constants.PROJECT_NUMBER);
					} catch (Exception e) {
						Log.e(TAG, Constants.GCM_TAG + "Error: attempt n." + String.valueOf(attempt) + " :" + e.getMessage());
						// No matter how many times you call register, it will always fail and throw an exception on some devices. On these devices we need to get GCM ID this way:
						/* final String registrationId = context.getIntent().getStringExtra("registration_id");
						if (registrationId != null && registrationId != "") {
							mRegId = registrationId;
							break;
						} */
						SystemClock.sleep(timeToSleep);
						timeToSleep = timeToSleep * 2;
					}
				}

				Log.i(TAG, Constants.GCM_TAG + "Device registered, attempt number " + String.valueOf(attempt) + ", registration ID=" + gcmIdNew);

				// if new GCM ID is different then the old one, delete old on server side and apply new one
				String gcmIdOld = gcmModel.getGCMRegistrationId();
				if (!gcmIdOld.equals(gcmIdNew)) {
					gcmModel.deleteGCM(controller.getActualUser().getId(), gcmIdOld);

					// Persist the GCM ID - no need to register again.
					gcmModel.setGCMIdLocal(gcmIdNew);
					gcmModel.setGCMIdServer(gcmIdNew);
				} else {
					// Save it just locally to update app version
					gcmModel.setGCMIdLocal(gcmIdNew);
					Log.i(TAG, Constants.GCM_TAG + "New GCM ID is the same, no need to change");
				}
			}
		});
		thread.start();
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * If result is empty, the app needs to register.
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @return registration ID, or empty string if there is no existing registration ID.
	 */
	public String getGCMRegistrationId() {
		String registrationId = mPersistence.loadGCMRegistrationId();
		if (registrationId.isEmpty()) {
			Log.i(TAG, Constants.GCM_TAG + "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID since the existing regID is not guaranteed to work with the new app version.
		int registeredVersion = mPersistence.loadLastApplicationVersion();
		int currentVersion = Utils.getAppVersion(mContext);
		if (registeredVersion != currentVersion) {
			// delete actual GCM ID from server
			deleteGCM(mUser.getId(), registrationId);
			setGCMIdLocal("");
			Log.i(TAG, Constants.GCM_TAG + "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Delete GCM ID from user
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param userId
	 * @param gcmId  - if null, then it will be loaded automatically by calling getGCMRegistrationId()
	 */
	public void deleteGCM(final String userId, final String gcmId) {
		if (mNetwork instanceof Network) {
			// delete GCM ID from server
			Thread t = new Thread() {
				public void run() {
					String id = (gcmId != null) ? gcmId : getGCMRegistrationId();

					setGCMIdLocal("");

					if (userId.isEmpty() || id.isEmpty())
						return;

					try {
						((Network) mNetwork).deleteGCMID(userId, id);
					} catch (AppException e) {
						// do nothing
						Log.w(TAG, Constants.GCM_TAG + "Delete GCM ID failed: " + e.getTranslatedErrorMessage(mContext));
					}
				}
			};
			t.start();
		}
	}

	/**
	 * Stores the registration ID and app versionCode in the application's {@code SharedPreferences}.
	 *
	 * @param gcmId registration ID
	 */
	public void setGCMIdLocal(String gcmId) {
		int appVersion = Utils.getAppVersion(mContext);
		Log.i(TAG, Constants.GCM_TAG + "Saving GCM ID on app version " + appVersion);

		mPersistence.saveGCMRegistrationId(gcmId);
		mPersistence.saveLastApplicationVersion(appVersion);
	}

	/**
	 * Method set gcmID to server (applied only if there is some user)
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param gcmID to be set
	 */
	public void setGCMIdServer(String gcmID) {
		if (!(mNetwork instanceof Network) || mUser.getId().isEmpty())
			return;

		try {
			Log.i(TAG, Constants.GCM_TAG + "Set GCM ID to server: " + gcmID);
			((Network) mNetwork).setGCMID(gcmID);
		} catch (AppException e) {
			// nothing to do
			Log.e(TAG, Constants.GCM_TAG + "Set GCM ID to server failed: " + e.getTranslatedErrorMessage(mContext));
		}
	}


	/** Notification methods ************************************************/

	/**
	 * Register receiver for receiving new notifications.
	 *
	 * @param receiver
	 */
	public void registerNotificationReceiver(INotificationReceiver receiver) {
		mNotificationReceivers.put(receiver, true);
	}

	/**
	 * Unregister listener from receiving new notifications.
	 *
	 * @param receiver
	 */
	public void unregisterNotificationReceiver(INotificationReceiver receiver) {
		mNotificationReceivers.remove(receiver);
	}

	/**
	 * Sends Notification to all registered receivers.
	 * <p/>
	 * <br>
	 * NOTE: This should be called by some GcmHandler only. Or maybe this should be inside of that class directly and
	 * Controller should "redirect" (un)registering for calling it there too.
	 *
	 * @param notification
	 * @return True if notification was handled by any of listener.
	 */
	public boolean receiveNotification(IGcmNotification notification) {
		boolean isHandled = false;
		for (INotificationReceiver receiver : mNotificationReceivers.keySet()) {
			if (receiver.receiveNotification(notification)) {
				isHandled = true;
			}
		}

		return isHandled;
	}

	/**
	 * Set notification as read on server side
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param msgId Notifiaction ID
	 */
	public void setNotificationRead(String msgId) {
		ArrayList<String> list = new ArrayList<>();
		list.add(msgId);
		setNotificationRead(list);
	}

	/**
	 * Set notifications as read on server side
	 * <p/>
	 * This CAN'T be called on UI thread!
	 *
	 * @param msgIds Array of message IDs which will be marked as read
	 */
	public void setNotificationRead(ArrayList<String> msgIds) {
		mNetwork.notifications_read(msgIds);
	}


	/**
	 * Get notification history
	 * <p/>
	 * This CAN'T be called on UI thread!
	 */
	public List<VisibleNotification> getNotificationHistory() {
		List<VisibleNotification> list = mNetwork.notifications_getLatest();
		Collections.sort(list);
		return list;
	}
}
