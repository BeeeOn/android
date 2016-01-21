package com.rehivetech.beeeon.gcm;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.gcm.notification.BaseNotification;
import com.rehivetech.beeeon.gcm.notification.IGcmNotification;

/**
 * Created by martin on 21.01.16.
 */


	public class MyGcmListenerService extends GcmListenerService {
		public static final String TAG = MyGcmListenerService.class.getSimpleName();

		public MyGcmListenerService() {
		}

		@Override
		public void onMessageReceived(String from, final Bundle data) {
//        String message = data.getString("data");
			Log.d(TAG, "From: " + from);
			Log.d(TAG, "Message: " + data);

			if (data == null) {
				Log.e(TAG, "onMessageReceived: data is NULL");
				return;
			}

			final IGcmNotification notification = BaseNotification.parseBundle(this, data);

			// control if message was valid
			if (notification == null) {
				Log.e(TAG, Constants.GCM_TAG + "Invalid message.");
				return;
			}

			notification.onGcmRecieve(this);
		}
}
