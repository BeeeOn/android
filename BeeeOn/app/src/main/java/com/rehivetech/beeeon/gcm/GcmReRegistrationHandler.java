package com.rehivetech.beeeon.gcm;

import android.app.IntentService;
import android.content.Intent;
import com.rehivetech.beeeon.util.Log;

public class GcmReRegistrationHandler extends IntentService {

	String mes;

	public GcmReRegistrationHandler() {
		super("GcmReRegistrationHandler");
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(GcmHelper.TAG_GCM, "Re-registartion GCM");
		GcmHelper.registerGCMInBackground(getApplicationContext());
	}

}
