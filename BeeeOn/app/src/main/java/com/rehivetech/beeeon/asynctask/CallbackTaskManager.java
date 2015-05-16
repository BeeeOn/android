package com.rehivetech.beeeon.asynctask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CallbackTaskManager {

	/**
	 * Holder for running tasks that we need to stop when activity is being stopped.
	 */
	private List<CallbackTask> mTasks = new ArrayList<>();

	public void addTask(CallbackTask task) {
		mTasks.add(task);
	}

	public void cancelAndRemoveAll() {
		// Cancel and remove all tasks
		Iterator<CallbackTask> iterator = mTasks.iterator();
		while (iterator.hasNext()) {
			iterator.next().cancel(true);
			iterator.remove();
		}
	}

	/**
	 * Add this task to internal list of tasks which will be automatically stopped and removed at activity's onStop() method.
	 *
	 * @param task
	 * @param param
	 */
	public <T> void executeTask(CallbackTask<T> task, T param) {
		addTask(task);
		task.execute(param);
	}

	/**
	 * Add this task to internal list of tasks which will be automatically stopped and removed at activity's onStop() method.
	 *
	 * @param task
	 */
	public void executeTask(CallbackTask task) {
		addTask(task);
		task.execute();
	}

	/**
	 * Plan to execute this task periodically. It will be automatically stopped at
	 *
	 * @param task
	 * @param param
	 * @param id
	 * @param everySecs
	 * @param <T>
	 */
	<T> void executeTaskEvery(CallbackTask<T> task, T param, String id, int everySecs) {
		// FIXME: implement this
	}

}
