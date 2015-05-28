package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.threading.CallbackTask;

public class SwitchGateTask extends CallbackTask<String> {

	private boolean mForceReload;

	public SwitchGateTask(Context context, boolean forceReload) {
		super(context);

		mForceReload = forceReload;
	}

	@Override
	protected Boolean doInBackground(String gateId) {
		Controller controller = Controller.getInstance(mContext);

		return controller.setActiveGate(gateId, mForceReload);
	}

}
