package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.threading.CallbackTask;

/**
 * Reloads gateInfo of specified gate from server.
 */
public class ReloadGateInfoTask extends CallbackTask<String> {

	private final boolean mForceReload;

	public ReloadGateInfoTask(Context context, boolean forceReload) {
		super(context);

		mForceReload = forceReload;
	}

	@Override
	protected Boolean doInBackground(String gateId) {
		return Controller.getInstance(mContext).getGatesModel().reloadGateInfo(gateId, mForceReload);
	}

}
