package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import com.rehivetech.beeeon.controller.Controller;

/**
 * Reloads locations and facilities by adapter.
 */
public class ReloadFacilitiesTask extends CallbackTask<String> {

	private final boolean mForceReload;

	public ReloadFacilitiesTask(Context context, boolean forceReload) {
		super(context);

		mForceReload = forceReload;
	}

	@Override
	protected Boolean doInBackground(String adapterId) {
		Controller controller = Controller.getInstance(mContext);

		controller.reloadLocations(adapterId, mForceReload);
		controller.getFacilitiesModel().reloadFacilitiesByAdapter(adapterId, mForceReload);

		return true;
	}

}
