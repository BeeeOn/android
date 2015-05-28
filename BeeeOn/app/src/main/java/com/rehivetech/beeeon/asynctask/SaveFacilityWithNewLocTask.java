package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.pair.SaveDeviceWithNewLocPair;

public class SaveFacilityWithNewLocTask extends CallbackTask<SaveDeviceWithNewLocPair> {

	public SaveFacilityWithNewLocTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(SaveDeviceWithNewLocPair pair) {
		Controller controller = Controller.getInstance(mContext);

		if (pair.location.getId().equals(Location.NEW_LOCATION_ID)) {
			// We need to save new location to server first
			Location newLocation = controller.getLocationsModel().createLocation(pair.location);
			if (newLocation == null)
				return false;

			pair.mDevice.setLocationId(newLocation.getId());
		}

		return controller.getDevicesModel().saveFacility(pair.mDevice, pair.what);
	}

}
