package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import android.os.SystemClock;

import com.rehivetech.beeeon.adapter.WatchDog;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.SaveFacilityPair;

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
