package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;

/**
 * Reloads watchdogs by adapter
 */
public class ReloadWatchDogsTask extends CallbackTask<String> {

	private final boolean mForceReload;

	public ReloadWatchDogsTask(Context context, boolean forceReload) {
		super(context);

		mForceReload = forceReload;
	}

	@Override
	protected Boolean doInBackground(String adapterId) {
		Controller controller = Controller.getInstance(mContext);

		return controller.reloadWatchDogs(adapterId, mForceReload);
	}

}
