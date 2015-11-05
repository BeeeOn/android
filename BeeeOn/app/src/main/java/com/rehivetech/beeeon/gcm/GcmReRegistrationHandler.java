package com.rehivetech.beeeon.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.controller.Controller;

public class GcmReRegistrationHandler extends IntentService {
	public static final String TAG = GcmReRegistrationHandler.class.getSimpleName();

	public GcmReRegistrationHandler() {
		super("GcmReRegistrationHandler");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Controller controller = Controller.getInstance(this);
		if (controller.isLoggedIn()) {
			Log.i(TAG, Constants.GCM_TAG + "Re-registartion GCM");
			GcmHelper.registerGCMInBackground(getApplicationContext());
		} else {
			controller.getGcmModel().setGCMIdLocal("");
		}
	}

}
