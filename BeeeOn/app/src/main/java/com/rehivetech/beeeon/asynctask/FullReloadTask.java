package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.controller.Controller;

/**
 * Reloads list of adapters and then locations and facilities for active adapter.
 */
public class FullReloadTask extends CallbackTask<Void> {

	private final boolean mForceReload;

	public FullReloadTask(Context context, boolean forceReload) {
		super(context);

		mForceReload = forceReload;
	}

	@Override
	protected Boolean doInBackground(Void nothing) {
		Controller controller = Controller.getInstance(mContext);

		controller.reloadAdapters(mForceReload);
		Adapter active = controller.getActiveAdapter();
		if (active != null) {
			// Load data for active adapter
			controller.reloadLocations(active.getId(), mForceReload);
			controller.getFacilitiesModel().reloadFacilitiesByAdapter(active.getId(), mForceReload);
		}

		return true;
	}

}
