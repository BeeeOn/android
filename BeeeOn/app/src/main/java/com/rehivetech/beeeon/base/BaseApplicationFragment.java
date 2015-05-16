package com.rehivetech.beeeon.base;

import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.asynctask.CallbackTaskManager;

public abstract class BaseApplicationFragment extends TrackFragment implements CallbackTaskManager.ICallbackTaskManager {

	private CallbackTaskManager mCallbackTaskManager = new CallbackTaskManager();

	@Override
	public void onStop() {
		super.onStop();

		// Cancel and remove all remembered tasks
		mCallbackTaskManager.cancelAndRemoveAll();
	}

	/**
	 * Add this task to internal list of tasks which will be automatically stopped and removed at activity's onStop() method.
	 *
	 * @param task
	 */
	@Override
	public void rememberTask(CallbackTask task) {
		mCallbackTaskManager.addTask(task);
	}

}
