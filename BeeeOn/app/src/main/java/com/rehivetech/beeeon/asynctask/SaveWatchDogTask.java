package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.adapter.watchdog.WatchDog;
import com.rehivetech.beeeon.controller.Controller;

public class SaveWatchDogTask extends CallbackTask<WatchDog> {

	public SaveWatchDogTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(WatchDog data) {
		Controller controller = Controller.getInstance(mContext);
		return controller.saveWatchDog(data);
	}

}
