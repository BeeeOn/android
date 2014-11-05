package cz.vutbr.fit.iha.gcm;

import android.app.IntentService;
import android.content.Intent;

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
		// FIXME: GCM

		/*
		 * Log.i(GcmHelper.TAG_GCM, "Re-registartion GCM"); GcmHelper.registerGCMInBackground(getApplicationContext());
		 */
	}

}
