package com.rehivetech.beeeon.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rehivetech.beeeon.Constants;

import timber.log.Timber;

public class UpdateBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		onUpdate(context);
	}

	public static void onUpdate(Context context) {
		Timber.i("%s App updated, starting service for re-registering GCM ID.", Constants.GCM_TAG);
		context.startService(new Intent(context, GcmRegistrationIntentService.class));
	}

	public static class LegacyUpdateBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && intent.getData() != null && context.getPackageName().equals(intent.getData().getSchemeSpecificPart())) {
				onUpdate(context);
			}
		}
	}
}
