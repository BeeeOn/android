package cz.vutbr.fit.iha.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cz.vutbr.fit.iha.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// FIXME: GCM
		/* Log.i(GcmHelper.TAG_GCM, "Booting finished, starting service for re-registering GCM ID.");
		context.startService(new Intent(context, GcmReRegistrationHandler.class)); */
	}
}
