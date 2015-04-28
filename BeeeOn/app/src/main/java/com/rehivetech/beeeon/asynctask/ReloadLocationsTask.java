package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;

/**
 * Reloads locations by adapter
 */
public class ReloadLocationsTask extends CallbackTask<String> {

	private final boolean mForceReload;

	public ReloadLocationsTask(Context context, boolean forceReload) {
		super(context);

		mForceReload = forceReload;
	}

	@Override
	protected Boolean doInBackground(String adapterId) {
		Controller controller = Controller.getInstance(mContext);

		return controller.getLocationsModel().reloadLocationsByAdapter(adapterId, mForceReload);
	}

}
