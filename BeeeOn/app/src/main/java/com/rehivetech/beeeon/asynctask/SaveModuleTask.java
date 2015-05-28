package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.SaveModulePair;

public class SaveModuleTask extends CallbackTask<SaveModulePair> {

	public SaveModuleTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(SaveModulePair pair) {
		Controller controller = Controller.getInstance(mContext);

		return controller.getDevicesModel().saveModule(pair.mModule, pair.what);
	}

}
