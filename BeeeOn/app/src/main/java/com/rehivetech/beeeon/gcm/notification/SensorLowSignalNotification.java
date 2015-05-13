package com.rehivetech.beeeon.gcm.notification;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.rehivetech.beeeon.network.xml.Xconstants;
import com.rehivetech.beeeon.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Martin on 22. 4. 2015.
 */
public class SensorLowSignalNotification extends VisibleNotification {
	public static final String TAG = SensorLowSignalNotification.class.getSimpleName();

	private int mAdapterId;
	private String mSensorId;
	private int mSignalLevel;

	private SensorLowSignalNotification(int msgid, long time, NotificationType type, boolean read, int adapterId, String sensorId, int signalLevel) {
		super(msgid, time, type, read);
		mAdapterId = adapterId;
		mSensorId = sensorId;
		mSignalLevel = signalLevel;
	}

	protected static SensorLowSignalNotification getInstance(Integer msgId, Long time, NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException {
		SensorLowSignalNotification instance = null;

		try {
			Integer adapterId = Integer.valueOf(bundle.getString(Xconstants.AID));
			String deviceId = bundle.getString(Xconstants.DID);
			Integer batterylevel = Integer.valueOf(bundle.getString(Xconstants.BATTERY));

			if (adapterId == null || deviceId == null || batterylevel == null) {
				Log.d(TAG, "SensorAdded: some compulsory value is missing.");
				return null;
			}

			instance = new SensorLowSignalNotification(msgId, time, type, false, adapterId, deviceId, batterylevel);
		} catch (IllegalArgumentException | NullPointerException e) {
			return instance;
		}

		return instance;
	}

	protected static VisibleNotification getInstance(Integer msgId, Long time, NotificationType type, boolean isRead, XmlPullParser parser) throws IOException, XmlPullParserException, NumberFormatException {
		Integer adapterId = null;
		String deviceId = null;
		Integer signalLevel = null;

		String text = null;
		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.END_TAG &&
					parser.getName().equals(Xconstants.NOTIFICATION)) {
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
					if (tagname.equalsIgnoreCase(Xconstants.AID)) {
						adapterId = Integer.valueOf(text);
					} else if (tagname.equalsIgnoreCase(Xconstants.DID)) {
						deviceId = text;
					} else if (tagname.equalsIgnoreCase(Xconstants.BATTERY)) {
						signalLevel = Integer.valueOf(text);
					}
					break;
				default:
					break;
			}
			eventType = parser.next();
		}

		if (adapterId == null || deviceId == null || signalLevel == null) {
			Log.d(TAG, "Xml: Some compulsory value is missing.");
			return null;
		}

		return new SensorLowSignalNotification(msgId, time, type, isRead, adapterId, deviceId, signalLevel);

	}

	@Override
	protected void onGcmHandle(Context context) {
		// TODO notifikovat controller aby si stahl nove data, zobrzit notiifkaci a po kliknuti odkazazt na datail senzort
//		NotificationCompat.Builder builder = getBaseNotificationBuilder(context);
//
//		showNotification(context, builder);
	}

	@Override
	protected void onClickHandle(Context context) {
		// TODO
		Toast.makeText(context, "on click", Toast.LENGTH_LONG).show();
	}

	@Override
	protected String getMessage(Context context) {
		// TODO pridat lokalizovany string
		return "ahoj";
	}

	@Override
	protected String getName(Context context) {
		return context.getString(com.rehivetech.beeeon.R.string.notification_name_new_sensor);
	}
}
