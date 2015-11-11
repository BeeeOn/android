package com.rehivetech.beeeon.threading.task;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.model.GatesModel;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.util.GpsData;

/**
 * Created by david on 18.6.15.
 */
public class EditGateTask extends CallbackTask<Pair<Gate, GpsData>> {

	public EditGateTask(@NonNull Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(Pair<Gate, GpsData> pair) {
		GatesModel gatesModel = Controller.getInstance(mContext).getGatesModel();
		return gatesModel.editGate(pair.first, pair.second);
	}
}
