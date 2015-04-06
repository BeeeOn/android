package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.AddUserPair;

public class AddUserTask extends CallbackTask<AddUserPair> {

	public AddUserTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(AddUserPair pair) {
		Controller controller = Controller.getInstance(mContext);

		return controller.getUsersModel().addUser(pair.adapter.getId(), pair.user);
	}

}
