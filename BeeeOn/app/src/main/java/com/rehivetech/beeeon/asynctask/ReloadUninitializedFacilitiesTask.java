package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import com.rehivetech.beeeon.controller.Controller;

public class ReloadUninitializedFacilitiesTask extends CallbackTask<String> {

	private final boolean mForceReload;

	public ReloadUninitializedFacilitiesTask(Context context, boolean forceReload) {
		super(context);

		mForceReload = forceReload;
	}

	@Override
	protected Boolean doInBackground(String adapterId) {
		Controller controller = Controller.getInstance(mContext);

		return controller.reloadUninitializedFacilitiesByAdapter(adapterId, mForceReload);
	}

}
