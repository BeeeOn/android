package com.rehivetech.beeeon.threading;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ClientError;
import com.rehivetech.beeeon.exception.IErrorCode;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class CallbackTaskManager {

	/**
	 * Progress indicator types when task is executing
	 */
	@IntDef({PROGRESS_NONE, PROGRESS_ICON, PROGRESS_DIALOG})
	public @interface ProgressIndicator {
	}

	public static final int PROGRESS_NONE = 0;
	public static final int PROGRESS_ICON = 1;
	public static final int PROGRESS_DIALOG = 2;


	/**
	 * Holder for running tasks that we need to stop when activity is being stopped.
	 */
	private final List<CallbackTask> mTasks = new ArrayList<>();

	/**
	 * Holder for TimerTask objects which are doing automatic running of task at scheduled time.
	 */
	private final Map<String, TimerTask> mTimerTasks = new HashMap<>();

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
		Iterator<CallbackTask> tasksIterator = mTasks.iterator();
		while (tasksIterator.hasNext()) {
			tasksIterator.next().cancelTask();
			tasksIterator.remove();
		}

		// Cancel timerTasks
		Iterator<Map.Entry<String, TimerTask>> timersIterator = mTimerTasks.entrySet().iterator();
		while (timersIterator.hasNext()) {
			timersIterator.next().getValue().cancel();
			timersIterator.remove();
		}

		// Cancel timer and thus its scheduled tasks
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
		}

		// Hide progressbar and dialog when cancelling tasks
		mActivity.setBeeeOnProgressBarVisibility(false);
		mActivity.setProgressDialogVisibility(false);
	}

	/**
	 * Add this task to internal list of tasks which will be automatically stopped and removed at activity's onStop() method.
	 *
	 * @param task              task to be executed
	 * @param progressIndicator what kind of progress indicator this task should show in Activity
	 * @param params            param for the task
	 */
	public <T> void executeTask(@Nullable CallbackTask<T> task, @ProgressIndicator int progressIndicator, @Nullable T... params) {
		// Check if we've got task object
		if (task == null)
			return;

		// TODO: check if it makes sense to start the task (data are expired, etc.) - need implementation in each particular task
		/*if (!task.needRun()) {
			return;
		}*/

		// Don't wait for task's preExecuteCallback for showing progressbar and show it immediately
		// because when switching activities there could be still running previous task which would postpone executing of this one
		setProgressIndicator(progressIndicator, true);

		// Prepare listeners for showing/hiding progress indicator
		prepareTaskListeners(task, progressIndicator);

		// Remember task
		addTask(task);

		// Execute task
		if (params != null) {
			task.execute(params);
		} else {
			task.execute();
		}
	}

	/**
	 * Add this task to internal list of tasks which will be automatically stopped and removed at activity's onStop() method.
	 * Shows progress indicator in Activity automatically during its running.
	 *
	 * @param task   task to be executed
	 * @param params param for the task
	 */
	@SafeVarargs
	public final <T> void executeTask(@Nullable CallbackTask<T> task, @Nullable T... params) {
		executeTask(task, PROGRESS_ICON, params);
	}

	/**
	 * Add this task to internal list of tasks which will be automatically stopped and removed at activity's onStop() method.
	 * Shows progress indicator in Activity automatically during its running.
	 *
	 * @param task task to be executed
	 */
	public <T> void executeTask(@Nullable CallbackTask task) {
		executeTask(task, (T) null);
	}

	/**
	 * Plan to execute this task periodically. It will be automatically stopped at
	 *
	 * @param taskFactory
	 * @param id
	 * @param everySecs
	 */
	public <T> void executeTaskEvery(final ICallbackTaskFactory<T> taskFactory, final String id, final int everySecs) {
		if (mTimer == null) {
			mTimer = new Timer();
		}

		TimerTask timerTask = new TimerTask() {
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
		};

		// Remember this task so we can cancel/reschedule it when this method is called with same id
		TimerTask oldTask = mTimerTasks.put(id, timerTask);
		if (oldTask != null) {
			oldTask.cancel();
		}

		mTimer.scheduleAtFixedRate(timerTask, everySecs * DateUtils.SECOND_IN_MILLIS, everySecs * DateUtils.SECOND_IN_MILLIS);
	}

	/**
	 * Prepare own listeners (and preserve original ones) for showing/hiding progress indicator of activity.
	 *
	 * @param task
	 * @param progressIndicator what kind of progress indicator this task should show in Activity
	 */
	private void prepareTaskListeners(final CallbackTask task, @ProgressIndicator final int progressIndicator) {
		final CallbackTask.ICallbackTaskPreExecuteListener origPreListener = task.getPreExecuteListener();
		task.setPreExecuteListener(new CallbackTask.ICallbackTaskPreExecuteListener() {
			@Override
			public void onPreExecute() {
				// Show progress indicator in activity
				setProgressIndicator(progressIndicator, true);

				// Call original listener, if exists
				if (origPreListener != null) {
					origPreListener.onPreExecute();
				}
			}
		});

		final CallbackTask.ICallbackTaskListener origListener = task.getListener();
		task.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				// Remove task from remembered tasks
				Iterator<CallbackTask> iterator = mTasks.iterator();
				while (iterator.hasNext()) {
					CallbackTask otherTask = iterator.next();
					if (otherTask == task) {
						iterator.remove();
					}
				}

				// Hide progress indicator in activity if all tasks were executed
				if (mTasks.size() == 0) {
					setProgressIndicator(progressIndicator, false);
				}

				// Handle eventual exceptions
				AppException exception = task.getException();
				if (exception != null && !task.isCancelled()) {

					// Handle specific error codes
					IErrorCode errCode = exception.getErrorCode();
					if (errCode != null) {
						if (errCode instanceof NetworkError) {
							switch ((NetworkError) errCode) {
								case BAD_BT: {
									BaseApplicationActivity.redirectToLogin(mActivity, false);
									break;
									// Intentionally no return here to let show error toast below
								}
								case COM_VER_MISMATCH: {
									BaseApplicationActivity.redirectToLogin(mActivity, true);
									break;
								}
							}
						} else if (errCode instanceof ClientError) {
							switch ((ClientError) errCode) {
								case SOCKET:
								case INTERNET_CONNECTION: {
									// Stop scheduled tasks on client errors? -> Separate server and client errors into separate *Error classes? Probably
									break;
								}
							}
						}
					}

					// TODO: For some errors show dialog instead of toast?
					// Notify error to user
					Toast.makeText(mActivity, exception.getTranslatedErrorMessage(mActivity), Toast.LENGTH_LONG).show();
				}

				// Call original listener, if exists
				// NOTE: We must call it here at the end, because sometimes activity starts new task inside the listener
				// TODO: Do we need/want to call it always? Don't we want to behave differently when some error code happens? E.g. when we're redirecting to LoginActivity?
				if (origListener != null) {
					origListener.onExecute(success);
				}
			}
		});
	}

	/**
	 * Sets the state of progress indicator
	 *
	 * @param type         type of indicator
	 * @param isInProgress if in progress, animates icon, shows dialog
	 */
	private void setProgressIndicator(@ProgressIndicator int type, boolean isInProgress) {
		switch (type) {
			case PROGRESS_NONE:
				break;
			case PROGRESS_ICON:
				mActivity.setBeeeOnProgressBarVisibility(isInProgress);
				break;
			case PROGRESS_DIALOG:
				mActivity.setProgressDialogVisibility(isInProgress);
				break;
		}
	}
}
