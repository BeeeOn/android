package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import com.rehivetech.beeeon.controller.Controller;

public class PairRequestTask extends CallbackTask<String> {

	private Context mContext;

	public PairRequestTask(Context context) {
		super();
		mContext = context;
	}

	@Override
	protected Boolean doInBackground(String adapterId) {
		Controller controller = Controller.getInstance(mContext);

		return controller.sendPairRequest(adapterId);
	}

}
