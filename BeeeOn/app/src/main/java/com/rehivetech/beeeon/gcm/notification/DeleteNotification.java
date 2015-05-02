package com.rehivetech.beeeon.gcm.notification;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import com.rehivetech.beeeon.util.Log;

/**
 * Created by Martin on 22. 4. 2015.
 */
public class DeleteNotification extends BaseNotification {

	public static final String TAG = DeleteNotification.class.getSimpleName();

	public static final String JSON_TAG_DEL_NOTIFICATION_ID = "delete_not";

	private int mDeleteNotificationId;

	/**
	 * Constructor
	 *
	 * @param userId
	 * @param msgid
	 * @param time
	 * @param type
	 */
	private DeleteNotification(String userId, int msgid, long time, NotificationType type, int deleteNotificaitonId) {
		super(msgid, time, type, true);
		mDeleteNotificationId = deleteNotificaitonId;
	}

	public static DeleteNotification getInstance(NotificationName name, Integer msgId, String userId, Long time, NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException{
		DeleteNotification instance = null;

		try {
			Integer delNotificationId = Integer.valueOf(bundle.getString(JSON_TAG_DEL_NOTIFICATION_ID));

			if (delNotificationId == null ) {
				Log.d(TAG, "DeleteNotification: some compulsory value is missing.");
				return null;
			}

			instance = new DeleteNotification(userId, msgId, time, type, delNotificationId);
		} catch (IllegalArgumentException | NullPointerException e) {
			return instance;
		}

		return instance;
	}

	@Override
	protected void onGcmHandle(Context context) {
		NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotifyMgr.cancel(mDeleteNotificationId);
	}
}
