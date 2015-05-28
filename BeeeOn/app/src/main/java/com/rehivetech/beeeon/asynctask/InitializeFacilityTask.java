package com.rehivetech.beeeon.asynctask;

import java.util.EnumSet;

import android.content.Context;
import com.rehivetech.beeeon.household.device.Module.SaveModule;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.InitializeFacilityPair;

public class InitializeFacilityTask extends CallbackTask<InitializeFacilityPair> {

	public InitializeFacilityTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(InitializeFacilityPair pair) {
		Controller controller = Controller.getInstance(mContext);

		if (pair.location.getId().equals(Location.NEW_LOCATION_ID)) {
			// We need to save new location to server first
			Location newLocation = controller.getLocationsModel().createLocation(pair.location);
			if (newLocation == null)
				return false;

			pair.mDevice.setLocationId(newLocation.getId());
		}

		EnumSet<SaveModule> what = EnumSet.of(SaveModule.SAVE_LOCATION, SaveModule.SAVE_NAME, SaveModule.SAVE_INITIALIZED);

		return controller.getDevicesModel().saveFacility(pair.mDevice, what);
	}

}
