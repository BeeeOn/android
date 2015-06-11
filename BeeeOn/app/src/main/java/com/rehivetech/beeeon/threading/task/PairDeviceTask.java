package com.rehivetech.beeeon.threading.task;

import android.content.Context;
import android.os.SystemClock;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.model.UninitializedDevicesModel;
import com.rehivetech.beeeon.threading.CallbackTask;


public class PairDeviceTask extends CallbackTask<String> {
	private final String mGateId;
	private long mStartTimeMSec;

	public PairDeviceTask(Context context, String GateId, long startTimeMSec) {
		super(context);
		this.mGateId = GateId;
		this.mStartTimeMSec = startTimeMSec; // Value is zero when first start, or the real time of the first start
	}

	@Override
	protected Boolean doInBackground(String gateId) {
		Controller controller = Controller.getInstance(mContext);
		UninitializedDevicesModel uninitializedDevicesModel = controller.getUninitializedDevicesModel();

		// First check if there are any uninit devices that belong to this gate
		uninitializedDevicesModel.reloadUninitializedDevicesByGate(mGateId,true);
		if (uninitializedDevicesModel.getUninitializedDevicesByGate(mGateId).size() > 0) {
			return true;
		}
		// Zero means this is the first time it is called
		if (mStartTimeMSec == 0) {
			// Make request
			controller.getGatesModel().sendPairRequest(mGateId);
			// Use current time as the beginningTime
			mStartTimeMSec = System.nanoTime() / 1000000;
		}
		long endTimeNanoSec = (mStartTimeMSec + (Constants.PAIR_TIME_SEC * 1000)) * 1000000;
		while (System.nanoTime() < endTimeNanoSec) {
			if (isCancelled()) {
				break;
			}

			uninitializedDevicesModel.reloadUninitializedDevicesByGate(gateId, true);
			if (uninitializedDevicesModel.getUninitializedDevicesByGate(mGateId).size() > 0) {
				return true;
			}
			SystemClock.sleep(1000);
		}
		// when the loop is over, the time is out -> it was unsuccessful
		return false;
	}


}
