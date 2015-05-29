package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.threading.CallbackTask;

public class PairRequestTask extends CallbackTask<String> {

	public PairRequestTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(String gateId) {
		Controller controller = Controller.getInstance(mContext);

		return controller.getGatesModel().sendPairRequest(gateId);
	}

}
