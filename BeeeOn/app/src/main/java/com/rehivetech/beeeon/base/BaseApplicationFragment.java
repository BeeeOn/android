package com.rehivetech.beeeon.base;

import com.rehivetech.beeeon.asynctask.CallbackTaskManager;

public abstract class BaseApplicationFragment extends TrackFragment {

	public final CallbackTaskManager callbackTaskManager = new CallbackTaskManager();

	@Override
	public void onStop() {
		super.onStop();

		// Cancel and remove all remembered tasks
		callbackTaskManager.cancelAndRemoveAll();
	}

}
