package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.watchdog.Watchdog;

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
