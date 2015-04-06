package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.pair.DelFacilityPair;
import com.rehivetech.beeeon.pair.DelWatchDogPair;

/**
 * Reloads facilities by adapter
 */
public class RemoveWatchDogTask extends CallbackTask<DelWatchDogPair> {

	private final boolean mForceReload;

	public RemoveWatchDogTask(Context context, boolean forceReload) {
		super(context);

		mForceReload = forceReload;
	}

	@Override
	protected Boolean doInBackground(DelWatchDogPair pair) {
		Controller controller = Controller.getInstance(mContext);

		Adapter adapter = controller.getActiveAdapter();
		if (adapter == null) {
			return false;
		}

		return controller.getWatchDogsModel().deleteWatchDog(controller.getWatchDogsModel().getWatchDog(pair.adapterID, pair.watchdogID));
	}
}
