package com.rehivetech.beeeon.gcm.notification;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import timber.log.Timber;

/**
 * Created by Martin on 22. 4. 2015.
 */
public class DeviceLowSignalNotification extends VisibleNotification {

	private String mGateId;
	private String mDeviceId;
	private String mSignalLevel;

	private DeviceLowSignalNotification(int msgid, long time, NotificationType type, boolean read, String gateId, String deviceId, String signalLevel) {
		super(msgid, time, type, read);
		mGateId = gateId;
		mDeviceId = deviceId;
		mSignalLevel = signalLevel;
	}

	protected static DeviceLowSignalNotification getInstance(Integer msgId, Long time, NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException {
		try {
			String gateId = bundle.getString("gateid");
			String deviceId = bundle.getString("did");
			String batterylevel = bundle.getString("batt");

			if (gateId == null || deviceId == null || batterylevel == null) {
				Timber.d("DeviceAdded: some compulsory value is missing.");
				return null;
			}

			return new DeviceLowSignalNotification(msgId, time, type, false, gateId, deviceId, batterylevel);
		} catch (IllegalArgumentException | NullPointerException e) {
			return null;
		}
	}

	protected static VisibleNotification getInstance(Integer msgId, Long time, NotificationType type, boolean isRead, XmlPullParser parser) throws IOException, XmlPullParserException, NumberFormatException {
		String gateId = null;
		String deviceId = null;
		String signalLevel = null;

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
					if (tagname.equalsIgnoreCase("gateid")) {
						gateId = text;
					} else if (tagname.equalsIgnoreCase("did")) {
						deviceId = text;
					} else if (tagname.equalsIgnoreCase("batt")) {
						signalLevel = text;
					}
					break;
				default:
					break;
			}
			eventType = parser.next();
		}

		if (gateId == null || deviceId == null || signalLevel == null) {
			Timber.d("Xml: Some compulsory value is missing.");
			return null;
		}

		return new DeviceLowSignalNotification(msgId, time, type, isRead, gateId, deviceId, signalLevel);

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
		return context.getString(com.rehivetech.beeeon.R.string.notification_device_low_battery_low_signal_name_new_module);
	}
}
