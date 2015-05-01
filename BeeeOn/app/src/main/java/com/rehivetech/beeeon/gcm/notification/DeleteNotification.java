package com.rehivetech.beeeon.gcm.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.rehivetech.beeeon.activity.LoginActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.network.xml.Xconstants;
import com.rehivetech.beeeon.util.Log;

/**
 * Created by Martin on 22. 4. 2015.
 */
public class DeleteNotification extends BaseNotification {

	public static final String TAG_DEL_NOTIFICATION_ID = "delete_not";

	private int mDeleteNotificationId;

	/**
	 * Constructor
	 *
	 * @param userId
	 * @param msgid
	 * @param time
	 * @param type
	 * @param read
	 */
	private DeleteNotification(String userId, int msgid, long time, NotificationType type, boolean read, int deleteNotificaitonId) {
		super(userId, msgid, time, type, read);
		mDeleteNotificationId = deleteNotificaitonId;
	}

	public static DeleteNotification getInstance(NotificationName name, Integer msgId, String userId, Long time, NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException{
		DeleteNotification instance = null;

		try {
			Integer delNotificationId = Integer.valueOf(bundle.getString(TAG_DEL_NOTIFICATION_ID));

			if (delNotificationId == null ) {
				Log.d(TAG, "DeleteNotification: some compulsory value is missing.");
				return null;
			}

			instance = new DeleteNotification(userId, msgId, time, type, false, delNotificationId);
		} catch (IllegalArgumentException | NullPointerException e) {
			return instance;
		}

		return instance;
	}

	@Override
	protected void onGcmHandle(Context context, Controller controller) {
		NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotifyMgr.cancel(mDeleteNotificationId);
	}

	@Override
	protected void onClickHandle(Context context, Controller controller) {
		// TODO
		Toast.makeText(context, "on click", Toast.LENGTH_LONG).show();
	}
}
