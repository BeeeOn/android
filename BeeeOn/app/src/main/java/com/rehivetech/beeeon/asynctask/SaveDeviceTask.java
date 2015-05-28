package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.SaveDevicePair;

public class SaveDeviceTask extends CallbackTask<SaveDevicePair> {

	public SaveDeviceTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(SaveDevicePair pair) {
		Controller controller = Controller.getInstance(mContext);

		return controller.getFacilitiesModel().saveDevice(pair.mModule, pair.what);
	}

}
