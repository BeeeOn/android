package com.rehivetech.beeeon.threading.task;

import android.content.Context;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.model.UninitializedDevicesModel;
import com.rehivetech.beeeon.threading.CallbackTask;


public class PairDeviceTask extends CallbackTask<String> {
	private final String mGateId;
	private final boolean mSendPairRequest;
	@Nullable private final String mDeviceIpAddress;

	/**
	 * Constructor for automatic pairing
	 *
	 * @param context
	 * @param gateId
	 * @param sendPairRequest
	 * @param deviceIpAddress
	 */
	public PairDeviceTask(Context context, String gateId, boolean sendPairRequest, @Nullable String deviceIpAddress) {
		super(context);
		mGateId = gateId;
		mSendPairRequest = sendPairRequest;
		mDeviceIpAddress = deviceIpAddress;
	}


	@Override
	protected Boolean doInBackground(String gateId) {
		Controller controller = Controller.getInstance(mContext);
		UninitializedDevicesModel uninitializedDevicesModel = controller.getUninitializedDevicesModel();

		if (mSendPairRequest) {
			//clear uninitialized devices cache before first pair request
			if (mDeviceIpAddress == null || mDeviceIpAddress.trim().isEmpty()) {
				uninitializedDevicesModel.clearUninitializedDevicesCacheByGate(mGateId);
			}

			// Make pair request
			controller.getGatesModel().sendPairRequest(mGateId, mDeviceIpAddress);
		}

		uninitializedDevicesModel.reloadUninitializedDevicesByGate(mGateId, true);
		return uninitializedDevicesModel.getUninitializedDevicesByGate(mGateId).size() > 0;
	}

}
