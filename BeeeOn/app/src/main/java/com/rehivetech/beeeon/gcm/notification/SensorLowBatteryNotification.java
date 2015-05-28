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
public class SensorLowBatteryNotification extends VisibleNotification {
	public static final String TAG = SensorLowBatteryNotification.class.getSimpleName();

	private int mGateId;
	private String mSensorId;
	private int mBatteryLevel;

	private SensorLowBatteryNotification(int msgid, long time, NotificationType type, boolean read, int gateId, String sensorId, int batteryLevel) {
		super(msgid, time, type, read);
		mGateId = gateId;
		mSensorId = sensorId;
		mBatteryLevel = batteryLevel;
	}

	protected static SensorLowBatteryNotification getInstance(Integer msgId, Long time, NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException {
		SensorLowBatteryNotification instance = null;

		try {
			Integer gateId = Integer.valueOf(bundle.getString(Xconstants.AID));
			String moduleId = bundle.getString(Xconstants.DID);
			Integer batterylevel = Integer.valueOf(bundle.getString(Xconstants.BATTERY));

			if (gateId == null || moduleId == null || batterylevel == null) {
				Log.d(TAG, "SensorAdded: some compulsory value is missing.");
				return null;
			}

			instance = new SensorLowBatteryNotification(msgId, time, type, false, gateId, moduleId, batterylevel);
		} catch (IllegalArgumentException | NullPointerException e) {
			return instance;
		}

		return instance;
	}

	protected static VisibleNotification getInstance(Integer msgId, Long time, NotificationType type, boolean isRead, XmlPullParser parser) throws IOException, XmlPullParserException, NumberFormatException {
		Integer gateId = null;
		String moduleId = null;
		Integer batteryLevel = null;

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
						gateId = Integer.valueOf(text);
					} else if (tagname.equalsIgnoreCase(Xconstants.DID)) {
						moduleId = text;
					} else if (tagname.equalsIgnoreCase(Xconstants.BATTERY)) {
						batteryLevel = Integer.valueOf(text);
					}
					break;
				default:
					break;
			}
			eventType = parser.next();
		}

		if (gateId == null || moduleId == null || batteryLevel == null) {
			Log.d(TAG, "Xml: Some compulsory value is missing.");
			return null;
		}

		return new SensorLowBatteryNotification(msgId, time, type, isRead, gateId, moduleId, batteryLevel);

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
