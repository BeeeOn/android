package com.rehivetech.beeeon.gcm.notification;

import android.content.Context;
import android.os.Bundle;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.network.xml.Xconstants;
import com.rehivetech.beeeon.util.Log;

/**
 * Created by Martin on 22. 4. 2015.
 */
public class AdapterAddedNotification extends VisibleNotification {


	private int mAdapterId;

	private AdapterAddedNotification(String userId, int msgid, long time, NotificationType type, boolean read, int adapterId) {
		super(userId, msgid, time, type, read);
		mAdapterId = adapterId;
	}

	public static AdapterAddedNotification getInstance(NotificationName name, Integer msgId, String userId, Long time, NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException{
		AdapterAddedNotification instance = null;

		try {
			Integer adapterId = Integer.valueOf(bundle.getString(Xconstants.AID));

			if ( adapterId == null  ) {
				Log.d(TAG, "Adapter added: some compulsory value is missing.");
				return null;
			}

			instance = new AdapterAddedNotification(userId, msgId, time, type, false, adapterId);
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
