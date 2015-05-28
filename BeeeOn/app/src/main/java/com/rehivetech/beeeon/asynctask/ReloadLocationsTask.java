package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;

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
