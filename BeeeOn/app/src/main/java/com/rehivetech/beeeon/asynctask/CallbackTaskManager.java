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
	 * Interface to use in Activity or Fragments who work with CallbackTask objects.
	 */
	public interface ICallbackTaskManager {

		/**
		 * Add this task to internal list of tasks which will be automatically stopped and removed at activity's onStop() method.
		 *
		 * @param task
		 * @param param
		 */
		<T> void executeTask(CallbackTask<T> task, T param);

		/**
		 * Add this task to internal list of tasks which will be automatically stopped and removed at activity's onStop() method.
		 *
		 * @param task
		 */
		void executeTask(CallbackTask task);

	}

}
