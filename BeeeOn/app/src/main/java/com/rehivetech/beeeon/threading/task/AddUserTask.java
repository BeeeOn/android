package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.threading.CallbackTask;

public class AddUserTask extends CallbackTask<User.DataPair> {

	public AddUserTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(User.DataPair pair) {
		Controller controller = Controller.getInstance(mContext);

		return controller.getUsersModel().addUser(pair.gateId, pair.user);
	}

}
