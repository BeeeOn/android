package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.watchdog.Watchdog;

public class SaveWatchdogTask extends CallbackTask<Watchdog> {

	public SaveWatchdogTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(Watchdog data) {
		Controller controller = Controller.getInstance(mContext);

		Gate gate = controller.getActiveGate();
		if (gate == null) {
			return false;
		}

		if (data.getId() != null)
			return controller.getWatchdogsModel().updateWatchdog(data);
		else
			return controller.getWatchdogsModel().addWatchdog(data);
	}

}
