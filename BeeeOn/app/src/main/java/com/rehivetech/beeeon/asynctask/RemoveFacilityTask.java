package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.DelFacilityPair;

/**
 * Reloads facilities by adapter
 */
public class RemoveFacilityTask extends CallbackTask<DelFacilityPair> {

	private final boolean mForceReload;

	public RemoveFacilityTask(Context context, boolean forceReload) {
		super(context);

		mForceReload = forceReload;
	}

	@Override
	protected Boolean doInBackground(DelFacilityPair pair) {
		Controller controller = Controller.getInstance(mContext);

		return controller.delFacility(controller.getFacility(pair.adapterID, pair.facilityID));
	}

}
