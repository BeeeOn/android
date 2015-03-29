package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.AddUserPair;

public class AddAdapterUserTask extends CallbackTask<AddUserPair> {

	public AddAdapterUserTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(AddUserPair pair) {
		Controller controller = Controller.getInstance(mContext);

		return controller.addUser(pair.adapter.getId(), pair.user);
	}

}
