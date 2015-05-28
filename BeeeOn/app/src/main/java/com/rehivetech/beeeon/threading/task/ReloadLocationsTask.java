package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.threading.CallbackTask;

/**
 * Reloads locations by gate
 */
public class ReloadLocationsTask extends CallbackTask<String> {

	private final boolean mForceReload;

	public ReloadLocationsTask(Context context, boolean forceReload) {
		super(context);

		mForceReload = forceReload;
	}

	@Override
	protected Boolean doInBackground(String gateId) {
		Controller controller = Controller.getInstance(mContext);

		return controller.getLocationsModel().reloadLocationsByGate(gateId, mForceReload);
	}

}
