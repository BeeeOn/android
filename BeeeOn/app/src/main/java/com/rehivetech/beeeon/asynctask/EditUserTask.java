package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.UserPair;

/**
 * Reloads facilities by adapter
 */
public class EditUserTask extends CallbackTask<UserPair> {

	private final Context mContext;

	private final boolean mForceReload;

	public EditUserTask(Context context, boolean forceReload) {
		super();

		mContext = context;
		mForceReload = forceReload;
	}

	@Override
	protected Boolean doInBackground(UserPair pair) {
		Controller controller = Controller.getInstance(mContext);

		return controller.saveUser(pair.adapterID, pair.user);
	}

}
