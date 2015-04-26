package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.DeviceLog;
import com.rehivetech.beeeon.pair.LogDataPair;

public class GetDeviceLogTask extends CallbackTask<LogDataPair> {

	public GetDeviceLogTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(LogDataPair pair) {
		Controller controller = Controller.getInstance(mContext);

		// Load log data if needed
		return controller.getDeviceLogsModel().reloadDeviceLog(pair);
	}

}
