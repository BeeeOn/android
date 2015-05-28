package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.gate.Gate;

public class UnregisterGateTask extends CallbackTask<String> {

	public UnregisterGateTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(String adapterId) {
		Controller controller = Controller.getInstance(mContext);

		Gate activeGate = controller.getActiveGate();

		// Unegister gate and reset activeGate
		if (controller.getGatesModel().unregisterGate(adapterId, controller.getActualUser())) {
			if (activeGate != null && activeGate.getId().equals(adapterId)) {
				controller.setActiveGate("", false);
			}
			return true;
		}

		return false;
	}

}
