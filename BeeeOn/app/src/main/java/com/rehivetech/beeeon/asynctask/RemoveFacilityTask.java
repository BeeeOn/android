package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.DelFacilityPair;

public class RemoveFacilityTask extends CallbackTask<DelFacilityPair> {

	public RemoveFacilityTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(DelFacilityPair pair) {
		Controller controller = Controller.getInstance(mContext);

		return controller.getFacilitiesModel().deleteFacility(controller.getFacilitiesModel().getFacility(pair.adapterID, pair.facilityID));
	}

}
