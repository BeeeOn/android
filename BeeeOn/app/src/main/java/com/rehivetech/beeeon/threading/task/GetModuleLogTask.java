package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.LogDataPair;
import com.rehivetech.beeeon.threading.CallbackTask;

public class GetModuleLogTask extends CallbackTask<LogDataPair> {

	public GetModuleLogTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(LogDataPair pair) {
		Controller controller = Controller.getInstance(mContext);

		// Load log data if needed
		return controller.getModuleLogsModel().reloadModuleLog(pair);
	}

}
