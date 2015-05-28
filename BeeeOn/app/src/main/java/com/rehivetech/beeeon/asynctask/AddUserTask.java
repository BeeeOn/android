package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.SaveUserPair;

public class AddUserTask extends CallbackTask<SaveUserPair> {

	public AddUserTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(SaveUserPair pair) {
		Controller controller = Controller.getInstance(mContext);

		return controller.getUsersModel().addUser(pair.gateId, pair.user);
	}

}
