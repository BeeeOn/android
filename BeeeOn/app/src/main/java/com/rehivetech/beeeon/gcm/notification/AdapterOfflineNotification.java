package com.rehivetech.beeeon.gcm.notification;

import android.content.Context;
import android.os.Bundle;
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
public class AdapterOfflineNotification extends VisibleNotification {

	public static final String TAG = AdapterOfflineNotification.class.getSimpleName();

	private int mAdapterId;

	private AdapterOfflineNotification(int msgid, long time, NotificationType type, boolean read, int adapterId) {
		super(msgid, time, type, read);
		mAdapterId = adapterId;
	}

	protected static AdapterOfflineNotification getInstance(Integer msgId, Long time, NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException {
		AdapterOfflineNotification instance = null;

		try {
			Integer adapterId = Integer.valueOf(bundle.getString(Xconstants.AID));

			if (adapterId == null) {
				Log.d(TAG, "Adapter added: some compulsory value is missing.");
				return null;
			}

			instance = new AdapterOfflineNotification(msgId, time, type, false, adapterId);
		} catch (IllegalArgumentException | NullPointerException e) {
			return instance;
		}

		return instance;
	}

	protected static VisibleNotification getInstance(Integer msgId, Long time, NotificationType type, boolean isRead, XmlPullParser parser) throws IOException, XmlPullParserException, NumberFormatException {
		Integer adapterId = null;

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
					if (tagname.equalsIgnoreCase(Xconstants.AID)) {
						adapterId = Integer.valueOf(text);
					}
					break;
				default:
					break;
			}
			eventType = parser.next();
		}

		if (adapterId == null) {
			Log.d(TAG, "Xml: Some compulsory value is missing.");
			return null;
		}

		return new AdapterOfflineNotification(msgId, time, type, false, adapterId);

	}

	@Override
	protected void onGcmHandle(Context context) {
		// TODO
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
	protected String getName(Context context){
		// TODO
		return "ahoj";
	}
}