package com.rehivetech.beeeon.gcm.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.rehivetech.beeeon.activity.LoginActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.network.xml.Xconstants;
import com.rehivetech.beeeon.util.Log;

/**
 * Created by Martin on 22. 4. 2015.
 */
public class WatchdogNotification extends BaseNotification {


	private int mAdapterId;
	private String mSensorId;
	private int mSensorType;
	private String mMsg;
	/**
	 * Constructor
	 *
	 * @param userId
	 * @param msgid
	 * @param time
	 * @param type
	 * @param read
	 */
	private WatchdogNotification(String userId, int msgid, long time, NotificationType type, boolean read, String message, int adapterId, String sensorId, int sensorType) {
		super(userId, msgid, time, type, read);
		mAdapterId = adapterId;
		mSensorId = sensorId;
		mSensorType = sensorType;
		mMsg = message;
	}

	public static WatchdogNotification getInstance(NotificationName name, Integer msgId, String userId, Long time, NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException{
		WatchdogNotification instance = null;

		try {
			Log.w(TAG, "ahoj");
			String message = bundle.getString(Xconstants.MESSAGE);
			Log.w(TAG, message);
			Integer adapterId = Integer.valueOf(bundle.getString(Xconstants.AID));
			Log.w(TAG, String.valueOf(adapterId));
			String deviceId = bundle.getString(Xconstants.DID);
			Log.w(TAG, deviceId);
			Integer deviceType = Integer.valueOf(bundle.getString(Xconstants.DTYPE));
			Log.w(TAG, String.valueOf(deviceType));

			if (message == null || adapterId == null || deviceId == null || deviceType == null) {
				Log.d(TAG, "Watdog: some copulsory value is missing.");
				return null;
			}

			instance = new WatchdogNotification(userId, msgId, time, type, false, message, adapterId, deviceId, deviceType);
		} catch (IllegalArgumentException | NullPointerException e) {
			return instance;
		}

		return instance;
	}

	@Override
	protected void onHandle(Context context, Controller controller) {
		NotificationCompat.Builder builder = getBaseNotificationBuilder(context);

		// define notification action
		Intent resultIntent = new Intent(context, LoginActivity.class);

		// Because clicking the notification opens a new ("special") activity, there's
		// no need to create an artificial back stack.
		PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// Set the Notification's Click Behavior
		builder.setContentIntent(resultPendingIntent);

		showNotification(context, builder);
	}
}
