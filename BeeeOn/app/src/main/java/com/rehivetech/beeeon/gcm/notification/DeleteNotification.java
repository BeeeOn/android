package com.rehivetech.beeeon.gcm.notification;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by Martin on 22. 4. 2015.
 */
public class DeleteNotification extends BaseNotification {

	public static final String JSON_TAG_DEL_NOTIFICATION_ID = "mid_del";

	private int mDeleteNotificationId;

	/**
	 * Constructor
	 *
	 * @param msgid
	 * @param time
	 * @param type
	 */
	private DeleteNotification(int msgid, long time, NotificationType type, int deleteNotificaitonId) {
		super(msgid, time, type, true);
		mDeleteNotificationId = deleteNotificaitonId;
	}

	protected static DeleteNotification getInstance(Integer msgId, Long time, NotificationType type, JSONObject data) throws NullPointerException, IllegalArgumentException {
		try {
			Integer delNotificationId = Integer.valueOf(data.getString(JSON_TAG_DEL_NOTIFICATION_ID));

			if (delNotificationId == null) {
				Timber.d("DeleteNotification: some compulsory value is missing.");
				return null;
			}

			return new DeleteNotification(msgId, time, type, delNotificationId);
		} catch (IllegalArgumentException | NullPointerException | JSONException e) {
			return null;
		}
	}

	@Override
	protected void onGcmHandle(Context context) {
		NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotifyMgr.cancel(mDeleteNotificationId);
	}
}
