package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.model.UninitializedDevicesModel;
import com.rehivetech.beeeon.threading.CallbackTask;


public class PairDeviceTask extends CallbackTask<String> {
	private final String mGateId;

	public PairDeviceTask(Context context, String GateId) {
		super(context);
		this.mGateId = GateId;
	}

	@Override
	protected Boolean doInBackground(String gateId) {
		Controller controller = Controller.getInstance(mContext);
		UninitializedDevicesModel uninitializedDevicesModel = controller.getUninitializedDevicesModel();

		uninitializedDevicesModel.reloadUninitializedDevicesByGate(mGateId, true);
		return uninitializedDevicesModel.getUninitializedDevicesByGate(mGateId).size() > 0;
	}

}
