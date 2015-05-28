package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.pair.DelFacilityPair;

public class RemoveFacilityTask extends CallbackTask<DelFacilityPair> {

	public RemoveFacilityTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(DelFacilityPair pair) {
		Controller controller = Controller.getInstance(mContext);

		Device device = controller.getDevicesModel().getFacility(pair.gateId, pair.facilityId);
		if (device == null)
			return false;

		return controller.getDevicesModel().deleteFacility(device);
	}

}
