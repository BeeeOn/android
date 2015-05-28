package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.SaveUserPair;
import com.rehivetech.beeeon.threading.CallbackTask;

public class EditUserTask extends CallbackTask<SaveUserPair> {

	public EditUserTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(SaveUserPair pair) {
		Controller controller = Controller.getInstance(mContext);

		return controller.getUsersModel().updateUser(pair.gateId, pair.user);
	}

}
