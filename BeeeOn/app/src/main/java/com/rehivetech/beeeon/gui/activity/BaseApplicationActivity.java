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
	@Nullable private View.OnClickListener mOnRefreshClickListener;
	public CallbackTaskManager callbackTaskManager;
	private Animation mRotation;
	private long mRefreshAnimStart;
	private Handler mHandler = new Handler();

	/**
	 * Runnable handling stopping refresh animation
	 */
	private Runnable mStopAnimationRunnable = new Runnable() {
		@Override
		public void run() {
			if (mRefreshIcon != null) {
				mRefreshAnimStart = 0;
				mRefreshIcon.clearAnimation();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mRotation = AnimationUtils.loadAnimation(this, R.anim.rotate);
		mRotation.setRepeatCount(Animation.INFINITE);
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
		callbackTaskManager.cancelAndRemoveAll();

		// Hide progress dialog if it is showing
		setProgressDialogVisibility(false);
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
		if (mToolbar == null || mRefreshIcon == null) {
			Log.e(TAG, "Trying to setup refresh icon without element(s) in layout!");
			return;
		}

		mOnRefreshClickListener = onClickListener;

		// always show only icon (if set listener)
		mRefreshIcon.setVisibility(mOnRefreshClickListener != null ? View.VISIBLE : View.INVISIBLE);
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
	 * @param isRefreshing whether progressbar will be shown/hidden && refresh icon vice versa
	 */
	public synchronized void setBeeeOnProgressBarVisibility(boolean isRefreshing) {
		// check if listener was set, otherwise do nothing
		if (mOnRefreshClickListener == null) return;

		if (mToolbar == null || mRefreshIcon == null) {
			Log.e(TAG, "Trying to setup refresh icon without element(s) in layout!");
			return;
		}

		if (isRefreshing) {
			mRefreshAnimStart = new Date().getTime();
			mRefreshIcon.startAnimation(mRotation);
		} else {
			Animation animation = mRefreshIcon.getAnimation();
			if (animation != null) {
				// calculates time in when animation should be properly stopped
				long postTime = mRefreshAnimStart + animation.getDuration() - new Date().getTime();
				mHandler.postDelayed(mStopAnimationRunnable, postTime);
			}
		}
	}

	/**
	 * Safely replaces fragment (stops any tasks, disables refresh icon)
	 *
	 * @param fragment to be shown
	 * @param tag      for the fragment
	 */
	public void fragmentReplace(Fragment fragment, String tag) {
		callbackTaskManager.cancelAndRemoveAll();
		setupRefreshIcon(null);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.main_content_frame, fragment, tag)
				.commit();
	}
}
