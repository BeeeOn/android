package com.rehivetech.beeeon.gcm;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.model.GcmModel;

import java.io.IOException;

import timber.log.Timber;

/**
 * Created by martin on 21.01.16.
 */
public class GcmRegistrationIntentService extends IntentService {

	public GcmRegistrationIntentService() {
		super("GcmRegistrationIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			String token = InstanceID.getInstance(this).getToken(getString(R.string.api_keys_google_console_project_id), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

			Controller controller = Controller.getInstance(this);
			GcmModel gcmModel = controller.getGcmModel();
			gcmModel.saveGcm(token);
		} catch (IOException | NullPointerException e) {
			Timber.e("Error while getting GCM token");
			e.printStackTrace();
		}
	}
}