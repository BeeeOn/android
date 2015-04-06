package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import com.rehivetech.beeeon.controller.Controller;

public class ReloadUsersTask extends CallbackTask<String> {

	private final boolean mForceReload;

	public ReloadUsersTask(Context context, boolean forceReload) {
		super(context);

		mForceReload = forceReload;
	}

	@Override
	protected Boolean doInBackground(String adapterId) {
		Controller controller = Controller.getInstance(mContext);

		return controller.getUsersModel().reloadUsersByAdapter(adapterId, mForceReload);
	}

}
