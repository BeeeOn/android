package com.rehivetech.beeeon.gcm;

import android.app.IntentService;
import android.content.Intent;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;

public class GcmReRegistrationHandler extends IntentService {
	public static final String TAG = GcmReRegistrationHandler.class.getSimpleName();

	public GcmReRegistrationHandler() {
		super("GcmReRegistrationHandler");
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Controller controller = Controller.getInstance(this);
		if (controller.isLoggedIn()) {
			Log.i(TAG, GcmHelper.TAG_GCM + "Re-registartion GCM");
			GcmHelper.registerGCMInBackground(getApplicationContext());
		} else {
			GcmHelper.invalidateLocalGcmId(controller);
		}
	}

}
