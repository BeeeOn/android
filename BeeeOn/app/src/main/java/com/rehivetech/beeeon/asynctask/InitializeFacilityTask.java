package com.rehivetech.beeeon.asynctask;

import java.util.EnumSet;

import android.content.Context;
import com.rehivetech.beeeon.household.device.Module.SaveDevice;
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

			pair.facility.setLocationId(newLocation.getId());
		}

		EnumSet<SaveDevice> what = EnumSet.of(SaveDevice.SAVE_LOCATION, SaveDevice.SAVE_NAME, SaveDevice.SAVE_INITIALIZED);

		return controller.getFacilitiesModel().saveFacility(pair.facility, what);
	}

}
