package com.rehivetech.beeeon.gcm;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.gcm.notification.BaseNotification;
import com.rehivetech.beeeon.gcm.notification.IGcmNotification;

import timber.log.Timber;

/**
 * Created by martin on 21.01.16.
 */


	public class MyGcmListenerService extends GcmListenerService {

		public MyGcmListenerService() {
		}

		@Override
		public void onMessageReceived(String from, final Bundle data) {
//        String message = data.getString("data");
			Timber.d("From: %s",from);
			Timber.d("Message: %s", data);

			if (data == null) {
				Timber.e("onMessageReceived: data is NULL");
				return;
			}

			final IGcmNotification notification = BaseNotification.parseBundle(this, data);

			// control if message was valid
			if (notification == null) {
				Timber.e(" %s Invalid message.", Constants.GCM_TAG);
				return;
			}

			notification.onGcmRecieve(this);
		}
}
