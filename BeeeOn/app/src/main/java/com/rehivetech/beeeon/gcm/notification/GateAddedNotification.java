package com.rehivetech.beeeon.gcm.notification;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.rehivetech.beeeon.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Martin on 22. 4. 2015.
 */
public class GateAddedNotification extends VisibleNotification {

	public static final String TAG = GateAddedNotification.class.getSimpleName();

	private String mGateId;

	private GateAddedNotification(int msgid, long time, NotificationType type, boolean read, String gateId) {
		super(msgid, time, type, read);
		mGateId = gateId;
	}

	protected static GateAddedNotification getInstance(Integer msgId, Long time, NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException {
		GateAddedNotification instance = null;

		try {
			String gateId = bundle.getString("gateid");

			if (gateId == null) {
				Log.d(TAG, "Gate added: some compulsory value is missing.");
				return null;
			}

			instance = new GateAddedNotification(msgId, time, type, false, gateId);
		} catch (IllegalArgumentException | NullPointerException e) {
			return instance;
		}

		return instance;
	}

	protected static VisibleNotification getInstance(Integer msgId, Long time, NotificationType type, boolean isRead, XmlPullParser parser) throws IOException, XmlPullParserException, NumberFormatException {
		String gateId = null;

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
					}
					break;
				default:
					break;
			}
			eventType = parser.next();
		}

		if (gateId == null) {
			Log.d(TAG, "Xml: Some compulsory value is missing.");
			return null;
		}

		return new GateAddedNotification(msgId, time, type, false, gateId);

	}

	@Override
	protected void onGcmHandle(Context context) {
		// TODO notifikvoat controler aby si stahl nova data
	}

	@Override
	protected void onClickHandle(Context context) {
		// 	TODO
		Toast.makeText(context, "on click", Toast.LENGTH_LONG).show();
	}

	@Override
	protected String getMessage(Context context) {
		// TODO pridat lokalizovany string
		return "ahoj";
	}

	@Override
	protected String getName(Context context) {
		return context.getString(R.string.notification_gate_added_name_new_gate);
	}
}
