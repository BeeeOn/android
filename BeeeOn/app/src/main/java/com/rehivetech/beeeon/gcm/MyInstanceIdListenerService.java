package com.rehivetech.beeeon.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by martin on 21.01.16.
 */
public class MyInstanceIdListenerService extends InstanceIDListenerService {

	public void onTokenRefresh() {
		Intent intent = new Intent(this, GcmRegistrationIntentService.class);
		startService(intent);
	}

}
