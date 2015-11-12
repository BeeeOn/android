package com.rehivetech.beeeon.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.model.GcmModel;

public class GcmReRegistrationHandler extends IntentService {
	public static final String TAG = GcmReRegistrationHandler.class.getSimpleName();

	public GcmReRegistrationHandler() {
		super("GcmReRegistrationHandler");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Controller controller = Controller.getInstance(this);
		GcmModel gcmModel = controller.getGcmModel();

		// Invalidate GCM ID
		gcmModel.setGCMIdLocal("");

		// If user is logged in, register new GCM ID right now
		if (controller.isLoggedIn()) {
			Log.i(TAG, Constants.GCM_TAG + "Re-registartion GCM");
			gcmModel.registerGCM();
		}
	}

}
