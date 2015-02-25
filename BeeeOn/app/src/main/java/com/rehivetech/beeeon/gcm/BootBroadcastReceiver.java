package com.rehivetech.beeeon.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.rehivetech.beeeon.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(GcmHelper.TAG_GCM, "Booting finished, starting service for re-registering GCM ID.");
		context.startService(new Intent(context, GcmReRegistrationHandler.class));
	}
}
