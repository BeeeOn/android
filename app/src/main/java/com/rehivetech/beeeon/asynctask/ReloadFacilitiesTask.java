package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import com.rehivetech.beeeon.controller.Controller;

/**
 * Reloads facilities by adapter
 */
public class ReloadFacilitiesTask extends CallbackTask<String> {

	private final Context mContext;

	private final boolean mForceReload;

	public ReloadFacilitiesTask(Context context, boolean forceReload) {
		super();

		mContext = context;
		mForceReload = forceReload;
	}

	@Override
	protected Boolean doInBackground(String adapterId) {
		Controller controller = Controller.getInstance(mContext);

		return controller.reloadFacilitiesByAdapter(adapterId, mForceReload);
	}

}
