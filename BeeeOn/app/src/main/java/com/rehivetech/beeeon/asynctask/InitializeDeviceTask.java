package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Module.SaveModule;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.pair.InitializeDevicePair;

import java.util.EnumSet;

public class InitializeDeviceTask extends CallbackTask<InitializeDevicePair> {

	public InitializeDeviceTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(InitializeDevicePair pair) {
		Controller controller = Controller.getInstance(mContext);

		if (pair.location.getId().equals(Location.NEW_LOCATION_ID)) {
			// We need to save new location to server first
			Location newLocation = controller.getLocationsModel().createLocation(pair.location);
			if (newLocation == null)
				return false;

			pair.mDevice.setLocationId(newLocation.getId());
		}

		EnumSet<SaveModule> what = EnumSet.of(SaveModule.SAVE_LOCATION, SaveModule.SAVE_NAME, SaveModule.SAVE_INITIALIZED);

		return controller.getDevicesModel().saveDevice(pair.mDevice, what);
	}

}
