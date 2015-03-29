package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.UserPair;

public class EditUserTask extends CallbackTask<UserPair> {

	public EditUserTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(UserPair pair) {
		Controller controller = Controller.getInstance(mContext);

		return controller.saveUser(pair.adapterID, pair.user);
	}

}
