package com.rehivetech.beeeon.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.rehivetech.beeeon.gcm.notification.BaseNotification;
import com.rehivetech.beeeon.gcm.notification.IGcmNotification;

public class GcmMessageHandler extends IntentService {

	public static final String TAG = GcmMessageHandler.class.getSimpleName();

	private Handler mHandler;

	public GcmMessageHandler() {
		super("GcmMessageHandler");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mHandler = new Handler();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();

		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (extras == null || extras.isEmpty() || messageType == null || messageType.isEmpty()) {
			GcmBroadcastReceiver.completeWakefulIntent(intent);
			Log.w(TAG, GcmHelper.TAG_GCM + "Null notification");
			return;
		}

		if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
			Log.w(TAG, GcmHelper.TAG_GCM + "Send error: " + extras.toString());
		} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
			Log.w(TAG, GcmHelper.TAG_GCM + "Deleted messages on server: " + extras.toString());
			// If it's a regular GCM message, do some work.
		} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
			handleNotification(intent);
		}

		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void handleNotification(Intent intent) {
		final IGcmNotification notification = BaseNotification.parseBundle(this, intent.getExtras());

		// control if message was valid
		if (notification == null) {
			Log.e(TAG, GcmHelper.TAG_GCM + "Invalid message.");
			GcmBroadcastReceiver.completeWakefulIntent(intent);
			return;
		}

		notification.onGcmRecieve(this);
	}

}