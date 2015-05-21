package com.rehivetech.beeeon.asynctask;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.base.BaseApplicationActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CallbackTaskManager {

	private static final String TAG = CallbackTaskManager.class.getSimpleName();

	/**
	 * Holder for running tasks that we need to stop when activity is being stopped.
	 */
	private final List<CallbackTask> mTasks = new ArrayList<>();

	private final BaseApplicationActivity mActivity;

	private Timer mTimer;

	public CallbackTaskManager(@NonNull BaseApplicationActivity activity) {
		mActivity = activity;
	}

	public void addTask(CallbackTask task) {
		mTasks.add(task);
	}

	public void cancelAndRemoveAll() {
		// Cancel and remove all tasks
		Iterator<CallbackTask> iterator = mTasks.iterator();
		while (iterator.hasNext()) {
			iterator.next().cancelTask();
			iterator.remove();
		}

		// Cancel timer and thus its scheduled tasks
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
		}

		// Hide progressbar when cancelling tasks
		mActivity.setBeeeOnProgressBarVisibility(false);
	}

	/**
	 * Add this task to internal list of tasks which will be automatically stopped and removed at activity's onStop() method.
	 *
	 * @param task
	 * @param param
	 */
	public <T> void executeTask(@NonNull CallbackTask<T> task, @Nullable T param) {
		// Don't wait for task's preExecuteCallback for showing progressbar and show it immediatelly
		// because when switching activities there could be still running previous task which would postpone executing of this one
		mActivity.setBeeeOnProgressBarVisibility(true);

		// Prepare listeners for showing/hiding progress bar
		prepareTaskListeners(task);

		// Remember task
		addTask(task);

		// Execute task
		if (param != null) {
			task.execute(param);
		} else {
			task.execute();
		}
	}

	/**
	 * Add this task to internal list of tasks which will be automatically stopped and removed at activity's onStop() method.
	 *
	 * @param task
	 */
	public void executeTask(@NonNull CallbackTask task) {
		executeTask(task, null);
	}

	/**
	 * Plan to execute this task periodically. It will be automatically stopped at
	 *
	 * @param taskFactory
	 * @param id
	 * @param everySecs
	 */
	public <T> void executeTaskEvery(final CallbackTaskFactory taskFactory, final String id, final int everySecs) {
		if (mTimer == null) {
			mTimer = new Timer();
		}

		mTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				mActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						CallbackTask task = taskFactory.createTask();
						Object param = taskFactory.createParam();

						if (param == null) {
							executeTask(task);
						} else {
							executeTask(task, param);
						}
					}
				});
			}
		}, 0, everySecs * 1000);
	}

	/**
	 * Prepare own listeners (and preserve original ones) for showing/hiding progress bar of activity.
	 * @param task
	 */
	private void prepareTaskListeners(final CallbackTask task) {
		final CallbackTask.CallbackTaskPreExecuteListener origPreListener = task.getPreExecuteListener();
		task.setPreExecuteListener(new CallbackTask.CallbackTaskPreExecuteListener() {
			@Override
			public void onPreExecute() {
				// Show progress bar in activity
				mActivity.setBeeeOnProgressBarVisibility(true);

				// Call original listener, if exists
				if (origPreListener != null) {
					origPreListener.onPreExecute();
				}
			}
		});

		final CallbackTask.CallbackTaskListener origListener = task.getListener();
		task.setListener(new CallbackTask.CallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				// Call original listener, if exists
				if (origListener != null) {
					origListener.onExecute(success);
				}

				// Remove task from remembered tasks
				Iterator<CallbackTask> iterator = mTasks.iterator();
				while (iterator.hasNext()) {
					CallbackTask otherTask = iterator.next();
					if (otherTask == task) {
						iterator.remove();
					}
				}

				// Hide progress bar in activity (only if it's last task)
				if (mTasks.size() == 0) {
					mActivity.setBeeeOnProgressBarVisibility(false);
				}
			}
		});
	}

}
