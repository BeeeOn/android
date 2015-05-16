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

	@Override
	public <T> void executeTask(CallbackTask<T> task, T param) {
		mCallbackTaskManager.addTask(task);
		task.execute(param);
	}

	@Override
	public void executeTask(CallbackTask task) {
		mCallbackTaskManager.addTask(task);
		task.execute();
	}

}
