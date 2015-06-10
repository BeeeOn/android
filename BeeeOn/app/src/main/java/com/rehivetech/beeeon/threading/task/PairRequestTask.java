package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.util.Log;


public class PairRequestTask extends CallbackTask<String> {
	private final String mGateId;
	private final long mTimeLimit;
	private final Context mContext;
	public boolean success = false;

	public PairRequestTask(Context context, String GateId, long timeLimit) {
		super(context);
		this.mGateId = GateId;
		this.mTimeLimit = timeLimit;
		this.mContext = context;
	}

	@Override
	protected Boolean doInBackground(String gateId) {
		Controller controller = Controller.getInstance(mContext);

		controller.getGatesModel().sendPairRequest(mGateId);

		long startTime = System.currentTimeMillis();
		Log.d("PAIR REQUEST", "INSIDE FUNCTION doInBackground");
		while (startTime + mTimeLimit * 1000 > System.currentTimeMillis()) {
			if (isCancelled()) {
				break;
			}

			if (Controller.getInstance(mContext).getUninitializedDevicesModel().getUninitializedDevicesByGate(mGateId).size() > 0){
				// If there are any uninitialized devices that belong to the gate, the request was successful
				return true;
			}

			if (success) {
				return true;
			}
		}
		Log.d("PAIR REQUEST", "INSIDE after the loop in FUNCTION doInBackground");
		// when the loop is over, the time is out -> it was unsuccessful
		return false;
	}


}
