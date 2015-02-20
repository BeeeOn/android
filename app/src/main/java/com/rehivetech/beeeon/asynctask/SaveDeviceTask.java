package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.SaveDevicePair;

public class SaveDeviceTask extends CallbackTask<SaveDevicePair> {

	private Context mContext;

	public SaveDeviceTask(Context context) {
		super();
		mContext = context;
	}

	@Override
	protected Boolean doInBackground(SaveDevicePair pair) {
		Controller controller = Controller.getInstance(mContext);

		return controller.saveDevice(pair.device, pair.what);
	}

}
