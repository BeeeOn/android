package com.rehivetech.beeeon.gcm.notification;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gcm.Action;
import com.rehivetech.beeeon.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Martin on 22. 4. 2015.
 */
public class WatchdogNotification extends VisibleNotification {

	public static final String TAG = WatchdogNotification.class.getSimpleName();

	private String mGateId;
	private String mDeviceId;
	private String mModuleId;
	private String mMsg;
	private String mAlgId;

	/**
	 * Constructor
	 *
	 * @param msgid
	 * @param time
	 * @param type
	 * @param read
	 */
	private WatchdogNotification(int msgid, long time, NotificationType type, boolean read, String message, String gateId, String deviceId, String moduleId, String algId) {
		super(msgid, time, type, read);
		mGateId = gateId;
		mDeviceId = deviceId;
		mModuleId = moduleId;
		mMsg = message;
		mAlgId = algId;
	}

	protected static WatchdogNotification getInstance(Integer msgId, Long time, NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException {
		WatchdogNotification instance;

		try {
			String message = bundle.getString("msg");
			String gateId = bundle.getString("aid");
			String deviceId = bundle.getString("did");
			String moduleId = bundle.getString("dtype");
			String algId = bundle.getString("algid");

			if (message == null || gateId == null || deviceId == null || moduleId == null || algId == null) {
				Log.d(TAG, "Json: Some compulsory value is missing.");
				return null;
			}

			instance = new WatchdogNotification(msgId, time, type, false, message, gateId, deviceId, moduleId, algId);
		} catch (IllegalArgumentException | NullPointerException e) {
			return null;
		}

		return instance;
	}

	protected static VisibleNotification getInstance(Integer msgId, Long time, NotificationType type, boolean isRead, XmlPullParser parser) throws IOException, XmlPullParserException, NumberFormatException {
		String message = null;
		String gateId = null;
		String deviceId = null;
		String moduleId = null;
		String algId = null;

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
					} else if (tagname.equalsIgnoreCase("aid")) {
						gateId = text;
					} else if (tagname.equalsIgnoreCase("did")) {
						deviceId = text;
					} else if (tagname.equalsIgnoreCase("dtype")) {
						moduleId = text;
					} else if (tagname.equalsIgnoreCase("algid")) {
						algId = text;
					}
					break;
				default:
					break;
			}
			eventType = parser.next();
		}

		if (message == null || gateId == null || deviceId == null || moduleId == null || algId == null) {
			Log.d(TAG, "Xml: Some compulsory value is missing.");
			return null;
		}

		return new WatchdogNotification(msgId, time, type, isRead, message, gateId, deviceId, moduleId, algId);
	}

	@Override
	protected void onGcmHandle(Context context) {
		NotificationCompat.Builder builder = getBaseNotificationBuilder(context);

		/*
		 * Sets the big view "big text" style and supplies the
         * text (the user's reminder message) that will be displayed
         * in the detail area of the expanded notification.
         * These calls are ignored by the support library for
         * pre-4.1 devices.
         */
//		Intent dismissIntent = new Intent(this, PingService.class);
//		dismissIntent.setAction(CommonConstants.ACTION_DISMISS);
//		PendingIntent piDismiss = PendingIntent.getService(this, 0, dismissIntent, 0);
//
//		Intent snoozeIntent = new Intent(this, PingService.class);
//		snoozeIntent.setAction(CommonConstants.ACTION_SNOOZE);
//		PendingIntent piSnooze = PendingIntent.getService(this, 0, snoozeIntent, 0);
//
//		builder
//				.setStyle(new NotificationCompat.BigTextStyle()
//						.bigText(getMessage(context)))
//				.addAction (R.drawable.ic_stat_dismiss,
//						context.getString(R.string.action_watchdog), piDismiss)
//				.addAction (R.drawable.ic_stat_snooze,
//						, piSnooze);

		showNotification(context, builder);
	}

	@Override
	protected void onClickHandle(Context context) {
		Action.getModuleDetailIntent(context, mGateId, mDeviceId, mModuleId);
	}

	@Override
	protected String getMessage(Context context) {
		return mMsg;
	}

	@Override
	protected String getName(Context context) {
		return context.getString(R.string.nav_drawer_menu_watchdog_notif_menu_watchdog);
	}
}
