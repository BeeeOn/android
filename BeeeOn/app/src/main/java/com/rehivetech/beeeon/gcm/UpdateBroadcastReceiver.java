package com.rehivetech.beeeon.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.rehivetech.beeeon.util.Log;

public class UpdateBroadcastReceiver extends BroadcastReceiver {

	public static final String TAG = UpdateBroadcastReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, GcmHelper.TAG_GCM + "App updated, starting service for re-registering GCM ID.");
		context.startService(new Intent(context, GcmReRegistrationHandler.class));
	}
}
