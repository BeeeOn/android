package com.rehivetech.beeeon.gcm.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.rehivetech.beeeon.activity.LoginActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.network.xml.Xconstants;
import com.rehivetech.beeeon.util.Log;

/**
 * Created by Martin on 22. 4. 2015.
 */
public class UriNotification extends VisibleNotification {

	private String mUri;
	private String mMsg;

	private UriNotification(String userId, int msgid, long time, NotificationType type, boolean read, String message, String uri) {
		super(userId, msgid, time, type, read);
		mMsg = message;
		mUri = uri;
	}

	public static UriNotification getInstance(NotificationName name, Integer msgId, String userId, Long time, NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException{
		UriNotification instance = null;

		try {
			String message = bundle.getString(Xconstants.MESSAGE);
			String uri = bundle.getString(Xconstants.URL);

			if (message == null || uri == null ) {
				Log.d(TAG, "Watdog: some compulsory value is missing.");
				return null;
			}

			instance = new UriNotification(userId, msgId, time, type, false, message, uri);
		} catch (IllegalArgumentException | NullPointerException e) {
			return instance;
		}

		return instance;
	}

	@Override
	protected void onGcmHandle(Context context, Controller controller) {
		NotificationCompat.Builder builder = getBaseNotificationBuilder(context);

		// define notification action
		Intent resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUri));

		builder.setContentText(getMessage());

		// Because clicking the notification opens a new ("special") activity, there's
		// no need to create an artificial back stack.
		PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// Set the Notification's Click Behavior
		builder.setContentIntent(resultPendingIntent);

		showNotification(context, builder);
	}

	@Override
	protected void onClickHandle(Context context, Controller controller) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUri));
		context.startActivity(intent);
	}

	@Override
	protected String getMessage() {
		return mMsg;
	}
}
