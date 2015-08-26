package com.rehivetech.beeeon.gcm.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Martin on 22. 4. 2015.
 */
public class UriNotification extends VisibleNotification {

	public static final String TAG = UriNotification.class.getSimpleName();

	private String mUri;
	private String mMsg;

	private UriNotification(int msgid, long time, NotificationType type, boolean read, String message, String uri) {
		super(msgid, time, type, read);
		mMsg = message;
		mUri = uri;
	}

	protected static UriNotification getInstance(Integer msgId, Long time, NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException {
		UriNotification instance = null;

		try {
			String message = bundle.getString("msg");
			String uri = bundle.getString("url");

			if (message == null || uri == null) {
				Log.d(TAG, "Watdog: some compulsory value is missing.");
				return null;
			}

			instance = new UriNotification(msgId, time, type, false, message, uri);
		} catch (IllegalArgumentException | NullPointerException e) {
			return instance;
		}

		return instance;
	}

	protected static VisibleNotification getInstance(Integer msgId, Long time, NotificationType type, boolean isRead, XmlPullParser parser) throws IOException, XmlPullParserException, NumberFormatException {
		String message = null;
		String uri = null;

		String text = null;
		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.END_TAG &&
					parser.getName().equals("notif")) {
				break;
			}
			String tagname = parser.getName();
			switch (eventType) {
				case XmlPullParser.START_TAG:
					// ignore it
					break;

				case XmlPullParser.TEXT:
					text = parser.getText();
					break;

				case XmlPullParser.END_TAG:
					if (tagname.equalsIgnoreCase("msg")) {
						message = text;
					} else if (tagname.equalsIgnoreCase("url")) {
						uri = text;
					}
					break;
				default:
					break;
			}
			eventType = parser.next();
		}

		if (message == null || uri == null) {
			Log.d(TAG, "Xml: Some compulsory value is missing.");
			return null;
		}

		return new UriNotification(msgId, time, type, isRead, message, uri);
	}

	@Override
	protected void onGcmHandle(Context context) {
		NotificationCompat.Builder builder = getBaseNotificationBuilder(context);

		// define notification action
		Intent resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUri));


		// Because clicking the notification opens a new ("special") activity, there's
		// no need to create an artificial back stack.
		PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// Set the Notification's Click Behavior
		builder.setContentIntent(resultPendingIntent);

		showNotification(context, builder);
	}

	@Override
	protected void onClickHandle(Context context) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUri));
		context.startActivity(intent);
	}

	@Override
	protected String getMessage(Context context) {
		return mMsg;
	}

	@Override
	protected String getName(Context context) {
		return context.getString(R.string.notification_uri_name_link);
	}
}
