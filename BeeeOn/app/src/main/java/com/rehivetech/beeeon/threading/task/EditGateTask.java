package com.rehivetech.beeeon.threading.task;

import android.content.Context;
import android.support.annotation.NonNull;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.model.GatesModel;
import com.rehivetech.beeeon.threading.CallbackTask;

/**
 * Created by david on 18.6.15.
 */
public class EditGateTask extends CallbackTask<Gate> {

	public EditGateTask(@NonNull Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(Gate gate) {
		GatesModel gatesModel = Controller.getInstance(mContext).getGatesModel();
		// FIXME: now it always returns true, but nothing is sent to server
		return true;
		//return gatesModel.editGate(gate);
	}
}
