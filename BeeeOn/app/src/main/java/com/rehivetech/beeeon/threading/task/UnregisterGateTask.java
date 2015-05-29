package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.threading.CallbackTask;

public class UnregisterGateTask extends CallbackTask<String> {

	public UnregisterGateTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(String gateId) {
		Controller controller = Controller.getInstance(mContext);

		Gate activeGate = controller.getActiveGate();

		// Unegister gate and reset activeGate
		if (controller.getGatesModel().unregisterGate(gateId, controller.getActualUser())) {
			if (activeGate != null && activeGate.getId().equals(gateId)) {
				controller.setActiveGate("", false);
			}
			return true;
		}

		return false;
	}

}
