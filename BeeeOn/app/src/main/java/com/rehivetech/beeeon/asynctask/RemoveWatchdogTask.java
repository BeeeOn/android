package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.pair.DelFacilityPair;
import com.rehivetech.beeeon.pair.DelWatchdogPair;

/**
 * Reloads devices by adapter
 */
public class RemoveWatchdogTask extends CallbackTask<DelWatchdogPair> {

	private final boolean mForceReload;

	public RemoveWatchdogTask(Context context, boolean forceReload) {
		super(context);

		mForceReload = forceReload;
	}

	@Override
	protected Boolean doInBackground(DelWatchdogPair pair) {
		Controller controller = Controller.getInstance(mContext);

		Adapter adapter = controller.getActiveAdapter();
		if (adapter == null) {
			return false;
		}

		return controller.getWatchdogsModel().deleteWatchdog(controller.getWatchdogsModel().getWatchdog(pair.adapterID, pair.watchdogID));
	}
}
