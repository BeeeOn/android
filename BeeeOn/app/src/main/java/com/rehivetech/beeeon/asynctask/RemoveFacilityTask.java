package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Device;

public class RemoveFacilityTask extends CallbackTask<Device> {

	public RemoveFacilityTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(Device device) {
		Controller controller = Controller.getInstance(mContext);

		return controller.getDevicesModel().deleteFacility(device);
	}

}
