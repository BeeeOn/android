package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.SaveUserPair;

public class RemoveUserTask extends CallbackTask<SaveUserPair> {

	public RemoveUserTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(SaveUserPair pair) {
		Controller controller = Controller.getInstance(mContext);

		return controller.getUsersModel().deleteUser(pair.gateId, pair.user);
	}

}
