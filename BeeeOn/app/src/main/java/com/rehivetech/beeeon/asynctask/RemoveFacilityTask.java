package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.pair.DelFacilityPair;

public class RemoveFacilityTask extends CallbackTask<DelFacilityPair> {

	public RemoveFacilityTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(DelFacilityPair pair) {
		Controller controller = Controller.getInstance(mContext);

		Facility facility = controller.getFacilitiesModel().getFacility(pair.adapterID, pair.facilityID);
		if (facility == null)
			return false;

		return controller.getFacilitiesModel().deleteFacility(facility);
	}

}
