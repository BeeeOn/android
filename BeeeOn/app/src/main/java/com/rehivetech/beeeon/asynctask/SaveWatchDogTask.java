package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.watchdog.WatchDog;
import com.rehivetech.beeeon.controller.Controller;

public class SaveWatchDogTask extends CallbackTask<WatchDog> {

	public SaveWatchDogTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(WatchDog data) {
		Controller controller = Controller.getInstance(mContext);

		Adapter adapter = controller.getActiveAdapter();
		if (adapter == null) {
			return false;
		}

		if (data.getId() != null)
			return controller.getWatchDogsModel().updateWatchDog(data);
		else
			return controller.getWatchDogsModel().addWatchDog(data);
	}

}
