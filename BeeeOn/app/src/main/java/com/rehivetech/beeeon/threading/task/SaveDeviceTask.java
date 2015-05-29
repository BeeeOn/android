package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask;

public class SaveDeviceTask extends CallbackTask<Device.DataPair> {

	public SaveDeviceTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(Device.DataPair pair) {
		Controller controller = Controller.getInstance(mContext);

		if (pair.location != null && pair.location.getId().equals(Location.NEW_LOCATION_ID)) {
			// We need to save new location to server first
			Location newLocation = controller.getLocationsModel().createLocation(pair.location);
			if (newLocation == null)
				return false;

			pair.mDevice.setLocationId(newLocation.getId());
		}

		return controller.getDevicesModel().saveDevice(pair.mDevice, pair.what);
	}

}
