package com.rehivetech.beeeon.gui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gcm.INotificationReceiver;
import com.rehivetech.beeeon.gcm.notification.IGcmNotification;
import com.rehivetech.beeeon.threading.CallbackTaskManager;

import java.util.Date;

/**
 * Abstract parent for application activities that requires logged in user and better using of tasks.
 * <p/>
 * When user is not logged in, it will switch to LoginActivity automatically.
 * Provides useful methods for using CallbackTasks.
 */
public abstract class BaseApplicationActivity extends BaseActivity implements INotificationReceiver {
	private static String TAG = BaseApplicationActivity.class.getSimpleName();

	private boolean triedLoginAlready = false;
	public CallbackTaskManager callbackTaskManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		callbackTaskManager = new CallbackTaskManager(this);
	}

	/**
	 * Checks if user is logged in, otherwise redirects to {@link LoginActivity}
	 * Also registers notification receiver
	 */
	@Override
	protected void onStart() {
		super.onStart();
		Controller controller = Controller.getInstance(this);
		if (!controller.isLoggedIn()) {
			if (!triedLoginAlready) {
				triedLoginAlready = true;
				redirectToLogin(this, false);
			} else {
				finish();
			}
			return;
		} else {
			triedLoginAlready = false;
		}
		controller.getGcmModel().registerNotificationReceiver(this);
	}

	/**
	 * Stops receiving notification in activities, stops all callback tasks, hides progress dialog
	 */
	@Override
	public void onStop() {
		super.onStop();
		Controller.getInstance(this).getGcmModel().unregisterNotificationReceiver(this);

		// Cancel and remove all remembered tasks
		callbackTaskManager.cancelAllTasks();
	}

	/**
	 * Redirects to login activity
	 *
	 * @param context from which will be started login activity
	 * @param logout  if user will be logged out
	 */
	public static void redirectToLogin(Context context, boolean logout) {
		if (logout) {
			Controller.getInstance(context).logout(false);
		}

		Log.d(TAG, "Redirecting to login");
		Intent intent = new Intent(context, LoginActivity.class);
		intent.putExtra(LoginActivity.BUNDLE_REDIRECT, true);
		intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

		context.startActivity(intent);
	}

	/**
	 * Logouts from application and redirects to login
	 */
	public void logout() {
		Controller.getInstance(this).logout(false);
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
		this.finish();
	}

	/**
	 * Method that receives Notifications.
	 */
	public boolean receiveNotification(final IGcmNotification notification) {
		// FIXME: Leo (or someone else?) should implement correct handling of notifications (showing somewhere in activity or something like that?)
		return false;
	}

	/**
	 * Safely replaces fragment (stops any tasks, disables refresh icon)
	 *
	 * @param fragment to be shown
	 * @param tag      for the fragment
	 */
	public void fragmentReplace(Fragment fragment, String tag) {
		callbackTaskManager.cancelAllTasks();
		setupRefreshIcon(null);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.main_content_frame, fragment, tag)
				.commit();
	}
}
