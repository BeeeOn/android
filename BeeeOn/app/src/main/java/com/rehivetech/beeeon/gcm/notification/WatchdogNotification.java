package com.rehivetech.beeeon.gcm.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.LoginActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.network.xml.Xconstants;
import com.rehivetech.beeeon.util.Log;

/**
 * Created by Martin on 22. 4. 2015.
 */
public class WatchdogNotification extends VisibleNotification {


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
			String message = bundle.getString(Xconstants.MESSAGE);
			Log.w(TAG, message);
			Integer adapterId = Integer.valueOf(bundle.getString(Xconstants.AID));
			Log.w(TAG, String.valueOf(adapterId));
			String deviceId = bundle.getString(Xconstants.DID);
			Log.w(TAG, deviceId);
			Integer deviceType = Integer.valueOf(bundle.getString(Xconstants.DTYPE));
			Log.w(TAG, String.valueOf(deviceType));

			if (message == null || adapterId == null || deviceId == null || deviceType == null) {
				Log.d(TAG, "Watcdog: some compulsory value is missing.");
				return null;
			}

			instance = new WatchdogNotification(userId, msgId, time, type, false, message, adapterId, deviceId, deviceType);
		} catch (IllegalArgumentException | NullPointerException e) {
			return instance;
		}

		return instance;
	}

	@Override
	protected void onGcmHandle(Context context) {
		NotificationCompat.Builder builder = getBaseNotificationBuilder(context);


		showNotification(context, builder);
	}

	@Override
	protected void onClickHandle(Context context) {
		// TODO
		Toast.makeText(context, "on click", Toast.LENGTH_LONG).show();
	}

	@Override
	protected String getMessage(Context context) {
		return mMsg;
	}

	@Override
	protected String getName(Context context) {
		return context.getString(R.string.menu_watchdog);
	}
}
