package com.rehivetech.beeeon.gui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gcm.INotificationReceiver;
import com.rehivetech.beeeon.gcm.notification.IGcmNotification;
import com.rehivetech.beeeon.threading.CallbackTaskManager;

/**
 * Abstract parent for application activities that requires logged in user and better using of tasks.
 * <p/>
 * When user is not logged in, it will switch to LoginActivity automatically.
 * Provides useful methods for using CallbackTasks.
 */
public abstract class BaseApplicationActivity extends BaseActivity implements INotificationReceiver {
	private static String TAG = BaseApplicationActivity.class.getSimpleName();

	private boolean triedLoginAlready = false;

	@Nullable private View.OnClickListener mOnRefreshClickListener;
	public CallbackTaskManager callbackTaskManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		callbackTaskManager = new CallbackTaskManager(this);
	}

	@Override
	public void onResume() {
		super.onResume();

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

	@Override
	public void onPause() {
		super.onPause();

		Controller controller = Controller.getInstance(this);
		controller.getGcmModel().unregisterNotificationReceiver(this);
	}

	@Override
	public void onStop() {
		super.onStop();

		// Cancel and remove all remembered tasks
		callbackTaskManager.cancelAndRemoveAll();

		// Hide progress dialog if it is showing
		hideProgressDialog();
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
	 * Method that receives Notifications.
	 */
	public boolean receiveNotification(final IGcmNotification notification) {
		// FIXME: Leo (or someone else?) should implement correct handling of notifications (showing somewhere in activity or something like that?)
		return false;
	}

	// ------------------------------------------------------- //
	// -------------------- REFRESH SETUP -------------------- //
	// ------------------------------------------------------- //

	/**
	 * When set, refresh icon will be shown in Toolbar and when async task running, icon will be hidden/visible
	 * {@link #setBeeeOnProgressBarVisibility(boolean)} changes visibility of icon
	 *
	 * @param onClickListener Callback for refresh icon
	 */
	public void setupRefreshIcon(@Nullable View.OnClickListener onClickListener) {
		if (mToolbar == null || mRefreshIcon == null || mProgressBar == null) {
			Log.e(TAG, "Trying to setup refresh icon without element(s) in layout!");
			return;
		}

		mOnRefreshClickListener = onClickListener;

		// always show only icon (if set listener)
		mRefreshIcon.setVisibility(mOnRefreshClickListener != null ? View.VISIBLE : View.INVISIBLE);
		mProgressBar.setVisibility(View.INVISIBLE);
		mRefreshIcon.setOnClickListener(mOnRefreshClickListener);
		mRefreshIcon.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// show toast right below the view
				Toast toast = Toast.makeText(v.getContext(), R.string.toolbar_refresh_title, Toast.LENGTH_SHORT);
				// toast will be under the view and half from right
				toast.setGravity(Gravity.TOP | Gravity.END, v.getWidth() - (v.getWidth() / 2), v.getBottom());
				toast.show();
				return true;
			}
		});
	}

	/**
	 * Called from {@link CallbackTaskManager} when task is started/canceled.
	 * Must be {@link #setupRefreshIcon(View.OnClickListener)} called first!
	 *
	 * @param visible whether progressbar will be shown/hidden && refresh icon vice versa
	 */
	public synchronized void setBeeeOnProgressBarVisibility(boolean visible) {
		// check if listener was set, otherwise do nothing
		if (mOnRefreshClickListener == null) return;

		if (mToolbar == null || mRefreshIcon == null || mProgressBar == null) {
			Log.e(TAG, "Trying to setup refresh icon without element(s) in layout!");
			return;
		}

		// if refresh icon was setup we either show progress or refresh icon
		mRefreshIcon.setVisibility(visible ? View.INVISIBLE : View.VISIBLE);
		mProgressBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
	}

}
