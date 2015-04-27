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
public class SensorAddedNotification extends VisibleNotification {


	private int mAdapterId;
	private String mSensorId;

	private SensorAddedNotification(String userId, int msgid, long time, NotificationType type, boolean read, int adapterId, String sensorId) {
		super(userId, msgid, time, type, read);
		mAdapterId = adapterId;
		mSensorId = sensorId;
	}

	public static SensorAddedNotification getInstance(NotificationName name, Integer msgId, String userId, Long time, NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException{
		SensorAddedNotification instance = null;

		try {
			Integer adapterId = Integer.valueOf(bundle.getString(Xconstants.AID));
			String deviceId = bundle.getString(Xconstants.DID);

			if (adapterId == null || deviceId == null ) {
				Log.d(TAG, "SensorAdded: some compulsory value is missing.");
				return null;
			}

			instance = new SensorAddedNotification(userId, msgId, time, type, false, adapterId, deviceId);
		} catch (IllegalArgumentException | NullPointerException e) {
			return instance;
		}

		return instance;
	}

	@Override
	protected void onHandle(Context context, Controller controller) {
		// TODO notifikovat controller aby si stahl nove data, zobrzit notiifkaci a po kliknuti odkazazt na datail senzort
//		NotificationCompat.Builder builder = getBaseNotificationBuilder(context);
//
//		// define notification action
//		Intent resultIntent = new Intent(context, LoginActivity.class);
//
//		builder.setContentText(getMessage());
//
//		// Because clicking the notification opens a new ("special") activity, there's
//		// no need to create an artificial back stack.
//		PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent,
//				PendingIntent.FLAG_UPDATE_CURRENT);
//
//		// Set the Notification's Click Behavior
//		builder.setContentIntent(resultPendingIntent);
//
//		showNotification(context, builder);
	}

	@Override
	protected String getMessage() {
		// TODO pridat lokalizovany string
		return "";
	}
}
