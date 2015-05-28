package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.pair.SaveDevicePair;

public class SaveDeviceTask extends CallbackTask<SaveDevicePair> {

	public SaveDeviceTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(SaveDevicePair pair) {
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
