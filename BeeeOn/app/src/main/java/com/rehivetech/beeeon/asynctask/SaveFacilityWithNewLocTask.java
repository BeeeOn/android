package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.SaveFacilityWithNewLocPair;

public class SaveFacilityWithNewLocTask extends CallbackTask<SaveFacilityWithNewLocPair> {

	public SaveFacilityWithNewLocTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(SaveFacilityWithNewLocPair pair) {
		Controller controller = Controller.getInstance(mContext);

		if (pair.location.getId().equals(Location.NEW_LOCATION_ID)) {
			// We need to save new location to server first
			Location newLocation = controller.addLocation(pair.location);
			if (newLocation == null)
				return false;

			pair.facility.setLocationId(newLocation.getId());
		}

		return controller.saveFacility(pair.facility, pair.what);
	}

}
