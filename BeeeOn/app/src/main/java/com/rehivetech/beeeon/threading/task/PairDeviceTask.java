package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.model.UninitializedDevicesModel;
import com.rehivetech.beeeon.threading.CallbackTask;


public class PairDeviceTask extends CallbackTask<String> {
	private final String mGateId;
	private final boolean mSendPairRequest;

	public PairDeviceTask(Context context, String GateId, boolean sendPairRequest) {
		super(context);
		mGateId = GateId;
		mSendPairRequest = sendPairRequest;

	}

	@Override
	protected Boolean doInBackground(String gateId) {
		Controller controller = Controller.getInstance(mContext);
		UninitializedDevicesModel uninitializedDevicesModel = controller.getUninitializedDevicesModel();

		if (mSendPairRequest) {
			// Make pair request
			controller.getGatesModel().sendPairRequest(mGateId);
		}

		uninitializedDevicesModel.reloadUninitializedDevicesByGate(mGateId, true);
		return uninitializedDevicesModel.getUninitializedDevicesByGate(mGateId).size() > 0;
	}

}
