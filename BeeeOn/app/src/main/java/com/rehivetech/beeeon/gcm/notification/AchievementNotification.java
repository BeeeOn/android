package com.rehivetech.beeeon.gcm.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.rehivetech.beeeon.activity.ProfileDetailActivity;
import com.rehivetech.beeeon.network.xml.Xconstants;
import com.rehivetech.beeeon.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Martin on 22. 4. 2015.
 */
public class AchievementNotification extends VisibleNotification {

	public static final String TAG = AchievementNotification.class.getSimpleName();

	// FIXME Honza dodelat ENUM nebo CLASS achievementu, dat jako atribut
	private AchievementNotification(int msgid, long time, NotificationType type, boolean read) {
		super(msgid, time, type, read);
	}

	protected static AchievementNotification getInstance(Integer msgId, Long time, NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException {
		AchievementNotification instance = null;

		try {
			Integer achievementId = Integer.valueOf(bundle.getString(Xconstants.ID));

			if (achievementId == null) {
				Log.d(TAG, "Achievement: some compulsory value is missing.");
				return null;
			}

			instance = new AchievementNotification(msgId, time, type, false);
		} catch (IllegalArgumentException | NullPointerException e) {
			return instance;
		}

		return instance;
	}

	protected static VisibleNotification getInstance(Integer msgId, Long time, NotificationType type, boolean isRead, XmlPullParser parser) throws IOException, XmlPullParserException, NumberFormatException {
		Integer achievementId = null;

		String text = null;
		int eventType = parser.getEventType();
		while ((eventType != XmlPullParser.END_TAG && !parser.getName().equals(Xconstants.NOTIFICATION)) || eventType != XmlPullParser.END_DOCUMENT) {
			String tagname = parser.getName();
			switch (eventType) {
				case XmlPullParser.START_TAG:
					// ignore it
					break;

				case XmlPullParser.TEXT:
					text = parser.getText();
					break;

				case XmlPullParser.END_TAG:
					if (tagname.equalsIgnoreCase(Xconstants.ID)) {
						achievementId = Integer.valueOf(text);
					}
					break;
				default:
					break;
			}
			eventType = parser.next();
		}

		if (achievementId == null) {
			Log.d(TAG, "Xml: Some compulsory value is missing.");
			return null;
		}

		return new AchievementNotification(msgId, time, type, isRead);
	}

	@Override
	protected void onGcmHandle(Context context) {
		NotificationCompat.Builder builder = getBaseNotificationBuilder(context);

		// TODO
//		// define notification action
//		Intent resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUri));
//
//
//		// Because clicking the notification opens a new ("special") activity, there's
//		// no need to create an artificial back stack.
//		PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent,
//				PendingIntent.FLAG_UPDATE_CURRENT);
//
//		// Set the Notification's Click Behavior
//		builder.setContentIntent(resultPendingIntent);

		showNotification(context, builder);
	}

	@Override
	protected void onClickHandle(Context context) {
		Intent intent = new Intent(context, ProfileDetailActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected String getMessage(Context context) {
		// FIXME az bude rnum, tak zisakt text z toho
		return "";
	}

	@Override
	protected String getName(Context context) {
		// FIXME az bude rnum, tak zisakt nazev z toho
		return "New achievemnt";
	}
}
