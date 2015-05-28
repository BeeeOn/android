package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.LogDataPair;

import java.util.List;

public class GetModulesLogsTask extends CallbackTask<List<LogDataPair>> {

	public GetModulesLogsTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(List<LogDataPair> pairs) {
		Controller controller = Controller.getInstance(mContext);

		// Load log data for all devices if needed
		for (LogDataPair pair : pairs) {
			controller.getModuleLogsModel().reloadModuleLog(pair);
		}

		return true;
	}

}
