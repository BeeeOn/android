package cz.vutbr.fit.iha.gcm;

import cz.vutbr.fit.iha.util.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UpdateBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(GcmHelper.TAG_GCM, "App updated, starting service for re-registering GCM ID.");
		context.startService(new Intent(context, GcmReRegistrationHandler.class));
	}
}
