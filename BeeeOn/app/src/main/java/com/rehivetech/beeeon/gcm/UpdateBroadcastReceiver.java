package com.rehivetech.beeeon.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.rehivetech.beeeon.Constants;

public class UpdateBroadcastReceiver extends BroadcastReceiver {

	public static final String TAG = UpdateBroadcastReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		onUpdate(context);
	}

	public static void onUpdate(Context context) {
		Log.i(TAG, Constants.GCM_TAG + "App updated, starting service for re-registering GCM ID.");
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
