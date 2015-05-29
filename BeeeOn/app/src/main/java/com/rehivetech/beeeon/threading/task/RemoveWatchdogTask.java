package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.watchdog.Watchdog;
import com.rehivetech.beeeon.threading.CallbackTask;

public class RemoveWatchdogTask extends CallbackTask<Watchdog> {

	public RemoveWatchdogTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(Watchdog watchdog) {
		Controller controller = Controller.getInstance(mContext);

		return controller.getWatchdogsModel().deleteWatchdog(watchdog);
	}
}
