package com.rehivetech.beeeon.gcm.notification;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gcm.Action;
import com.rehivetech.beeeon.network.xml.Xconstants;
import com.rehivetech.beeeon.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Martin on 22. 4. 2015.
 */
public class WatchdogNotification extends VisibleNotification {

	public static final String TAG = WatchdogNotification.class.getSimpleName();

	private int mAdapterId;
	private String mSensorId;
	private int mSensorType;
	private String mMsg;
	private int mAlgId;

	/**
	 * Constructor
	 *
	 * @param msgid
	 * @param time
	 * @param type
	 * @param read
	 */
	private WatchdogNotification(int msgid, long time, NotificationType type, boolean read, String message, int adapterId, String sensorId, int sensorType, int algId) {
		super(msgid, time, type, read);
		mAdapterId = adapterId;
		mSensorId = sensorId;
		mSensorType = sensorType;
		mMsg = message;
		mAlgId = algId;

	}

	protected static WatchdogNotification getInstance(Integer msgId, Long time, NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException {
		WatchdogNotification instance = null;

		try {
			String message = bundle.getString(Xconstants.MESSAGE);
			Integer adapterId = Integer.valueOf(bundle.getString(Xconstants.AID));
			String deviceId = bundle.getString(Xconstants.DID);
			Integer deviceType = Integer.valueOf(bundle.getString(Xconstants.DTYPE));
			Integer algId = Integer.valueOf(bundle.getString(Xconstants.ALGID));

			if (message == null || adapterId == null || deviceId == null || deviceType == null || algId == null) {
				Log.d(TAG, "Json: Some compulsory value is missing.");
				return null;
			}

			instance = new WatchdogNotification(msgId, time, type, false, message, adapterId, deviceId, deviceType, algId);
		} catch (IllegalArgumentException | NullPointerException e) {
			return instance;
		}

		return instance;
	}

	protected static VisibleNotification getInstance(Integer msgId, Long time, NotificationType type, boolean isRead, XmlPullParser parser) throws IOException, XmlPullParserException, NumberFormatException {
		String message = null;
		Integer adapterId = null;
		String deviceId = null;
		Integer deviceType = null;
		Integer algId = null;

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
					if (tagname.equalsIgnoreCase(Xconstants.MESSAGE)) {
						message = text;
					} else if (tagname.equalsIgnoreCase(Xconstants.AID)) {
						adapterId = Integer.valueOf(text);
					} else if (tagname.equalsIgnoreCase(Xconstants.DID)) {
						deviceId = text;
					} else if (tagname.equalsIgnoreCase(Xconstants.DTYPE)) {
						deviceType = Integer.valueOf(text);
					} else if (tagname.equalsIgnoreCase(Xconstants.ALGID)) {
						deviceType = Integer.valueOf(text);
					}
					break;
				default:
					break;
			}
			eventType = parser.next();
		}

		if (message == null || adapterId == null || deviceId == null || deviceType == null) {
			Log.d(TAG, "Xml: Some compulsory value is missing.");
			return null;
		}

		return new WatchdogNotification(msgId, time, type, isRead, message, adapterId, deviceId, deviceType, algId);
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
		Action.getSensorDetailIntent(context, mAdapterId, mSensorId, mSensorType);
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
