package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Device;

public class RemoveDeviceTask extends CallbackTask<Device> {

	public RemoveDeviceTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(Device device) {
		Controller controller = Controller.getInstance(mContext);

		return controller.getDevicesModel().deleteDevice(device);
	}

}
