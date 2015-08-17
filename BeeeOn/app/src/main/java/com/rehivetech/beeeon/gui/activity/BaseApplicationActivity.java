package com.rehivetech.beeeon.gui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gcm.INotificationReceiver;
import com.rehivetech.beeeon.gcm.notification.IGcmNotification;
import com.rehivetech.beeeon.gui.dialog.BetterProgressDialog;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.util.Log;

/**
 * Abstract parent for application activities that requires logged in user and better using of tasks.
 * <p/>
 * When user is not logged in, it will switch to LoginActivity automatically.
 * Provides useful methods for using CallbackTasks.
 */
public abstract class BaseApplicationActivity extends BaseActivity implements INotificationReceiver {

	private static String TAG = BaseApplicationActivity.class.getSimpleName();

	private boolean triedLoginAlready = false;

	@Nullable
	private View mProgressBar;
	@Nullable
	private View mRefreshIcon;
	@Nullable
	private BetterProgressDialog mProgressDialog;

	protected boolean isPaused = false;

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
				redirectToLogin(this);
			} else {
				finish();
			}
			return;
		} else {
			triedLoginAlready = false;
		}

		controller.getGcmModel().registerNotificationReceiver(this);

		isPaused = false;
		onAppResume();
	}

	@Override
	public void onPause() {
		super.onPause();

		Controller controller = Controller.getInstance(this);
		controller.getGcmModel().unregisterNotificationReceiver(this);

		isPaused = true;
		onAppPause();
	}

	@Override
	public void onStop() {
		super.onStop();

		// Cancel and remove all remembered tasks
		callbackTaskManager.cancelAndRemoveAll();

		// Hide progress dialog if it is showing
		hideProgressDialog();
	}

	public static void redirectToLogin(Context context, boolean logout) {
		if (logout) {
			Controller.getInstance(context).logout(false);
		}

		Log.d(TAG, "Redirecting to login");
		Intent intent = new Intent(context, LoginActivity.class);
		intent.putExtra(LoginActivity.BUNDLE_REDIRECT, true);
		intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);

		context.startActivity(intent);
	}

	public static void redirectToLogin(Context context) {
		redirectToLogin(context, false);
	}

	/**
	 * This is called after onResume(), but only when user is correctly logged in
	 */
	protected void onAppResume() {
		// Empty default method
	}

	/**
	 * This is called after onPause()
	 */
	protected void onAppPause() {
		// Empty default method
	}

	/**
	 * Called from {@link CallbackTaskManager} when task is started/canceled.
	 *
	 * @param visible whether progressbar will be shown/hidden && refresh icon vice versa
	 */
	public synchronized void setBeeeOnProgressBarVisibility(boolean visible) {
		if (mProgressBar == null) {
			mProgressBar = findViewById(R.id.beeeon_toolbar_progress);

			if (mProgressBar == null) {
				// This activity probably doesn't have progressbar in layout
				Log.w(TAG, String.format("Can't set visibility of progressbar in '%s', it wasn't found in layout.", getClass().getSimpleName()));
				return;
			}
		}

		// if refresh icon was setup we either show progress or refresh icon
		if (mRefreshIcon != null) {
			mRefreshIcon.setVisibility(visible ? View.INVISIBLE : View.VISIBLE);
		}

		mProgressBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
	}

	public synchronized void showProgressDialog(/*@StringRes int progressMessageRes*/) {
		if (mProgressDialog == null) {
			// Prepare progress dialog
			mProgressDialog = new BetterProgressDialog(this);
			mProgressDialog.setMessageResource(R.string.base_application_progress_saving_data);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		}

		mProgressDialog.show();
	}

	public synchronized void hideProgressDialog() {
		if (mProgressDialog == null || !mProgressDialog.isShowing()) {
			return;
		}

		mProgressDialog.dismiss();
	}

	/**
	 * Method that receives Notifications.
	 */
	public boolean receiveNotification(final IGcmNotification notification) {
		// FIXME: Leo (or someone else?) should implement correct handling of notifications (showing somewhere in activity or something like that?)

		return false;
	}

	public void onFragmentAttached(Fragment fragment) {

	}

	/**
	 * When set, refresh icon will be shown in Toolbar and when async task, icon will be hidden/visible
	 * {@link #setBeeeOnProgressBarVisibility(boolean)} changes visibility of icon
	 *
	 * @param onClickListener Callback for refresh icon
	 */
	public void setupRefreshIcon(View.OnClickListener onClickListener) {
		if (mRefreshIcon == null) {
			mRefreshIcon = findViewById(R.id.beeeon_toolbar_refresh);

			if (mRefreshIcon == null) {
				// This activity probably doesn't have refreshIcon (or toolbar) in layout
				Log.w(TAG, String.format("Can't set visibility of refreshIcon in '%s', it wasn't found in layout.", getClass().getSimpleName()));
				return;
			}
		}

		mRefreshIcon.setVisibility(View.VISIBLE);
		mRefreshIcon.setOnClickListener(onClickListener);
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
}
