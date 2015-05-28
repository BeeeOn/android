package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.SaveDevicePair;

public class SaveFacilityTask extends CallbackTask<SaveDevicePair> {

	public SaveFacilityTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(SaveDevicePair pair) {
		Controller controller = Controller.getInstance(mContext);

		return controller.getDevicesModel().saveFacility(pair.mDevice, pair.what);
	}

}
