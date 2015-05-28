package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.pair.DelDevicePair;

public class RemoveFacilityTask extends CallbackTask<DelDevicePair> {

	public RemoveFacilityTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(DelDevicePair pair) {
		Controller controller = Controller.getInstance(mContext);

		Device device = controller.getDevicesModel().getFacility(pair.gateId, pair.deviceId);
		if (device == null)
			return false;

		return controller.getDevicesModel().deleteFacility(device);
	}

}
