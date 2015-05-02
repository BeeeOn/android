package com.rehivetech.beeeon.gcm.notification;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
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

	/**
	 * Constructor
	 *
	 * @param userId
	 * @param msgid
	 * @param time
	 * @param type
	 * @param read
	 */
	private WatchdogNotification(int msgid, long time, NotificationType type, boolean read, String message, int adapterId, String sensorId, int sensorType) {
		super(msgid, time, type, read);
		mAdapterId = adapterId;
		mSensorId = sensorId;
		mSensorType = sensorType;
		mMsg = message;
	}

	public static WatchdogNotification getInstance(NotificationName name, Integer msgId, String userId, Long time, NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException {
		WatchdogNotification instance = null;

		try {
			String message = bundle.getString(Xconstants.MESSAGE);
			Integer adapterId = Integer.valueOf(bundle.getString(Xconstants.AID));
			String deviceId = bundle.getString(Xconstants.DID);
			Integer deviceType = Integer.valueOf(bundle.getString(Xconstants.DTYPE));

			if (message == null || adapterId == null || deviceId == null || deviceType == null) {
				Log.d(TAG, "Json: Some compulsory value is missing.");
				return null;
			}

			instance = new WatchdogNotification(msgId, time, type, false, message, adapterId, deviceId, deviceType);
		} catch (IllegalArgumentException | NullPointerException e) {
			return instance;
		}

		return instance;
	}

	protected static VisibleNotification getInstance(NotificationName name, Integer msgId, Long time, NotificationType type, boolean isRead, XmlPullParser parser) throws IOException, XmlPullParserException, NumberFormatException {
		String message = null;
		Integer adapterId = null;
		String deviceId = null;
		Integer deviceType = null;

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

		return new WatchdogNotification(msgId, time, type, isRead, message, adapterId, deviceId, deviceType);


	}

	@Override
	protected void onGcmHandle(Context context) {
		NotificationCompat.Builder builder = getBaseNotificationBuilder(context);


		showNotification(context, builder);
	}

	@Override
	protected void onClickHandle(Context context) {
		// TODO
		Toast.makeText(context, "on click", Toast.LENGTH_LONG).show();
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
