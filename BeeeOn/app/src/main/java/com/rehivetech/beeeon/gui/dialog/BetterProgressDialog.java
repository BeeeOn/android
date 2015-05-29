package com.rehivetech.beeeon.gui.dialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

/**
 * Improved version of ProgressDialog which supports calling show/dismiss on UI thread and disables screen orientation during showing.
 */
public class BetterProgressDialog extends ProgressDialog {
	private final Activity mActivity;

	public BetterProgressDialog(final Activity activity) {
		super(activity);
		mActivity = activity;
	}

	/**
	 * Show progress dialog and disable orientation change of activity.
	 * This method works on UI thread.
	 */
	@Override
	public void show() {
		// Disable orientation change
		int currentOrientation = mActivity.getResources().getConfiguration().orientation;
		mActivity.setRequestedOrientation(currentOrientation == Configuration.ORIENTATION_LANDSCAPE
				? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
				: ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				BetterProgressDialog.super.show();
			}
		});
	}

	/**
	 * Dismiss progress dialog and enable again orientation change of activity.
	 * This method works on UI thread (by default).
	 */
	@Override
	public void dismiss() {
		// For older versions of Android we must call it on UI thread, newer devices probably handles that itself
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				BetterProgressDialog.super.dismiss();
			}
		});

		// Enable orientation change again
		mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}

	/**
	 * Change progress message to string from resources.
	 * This method works on UI thread.
	 *
	 * @param resourceId
	 */
	public void setMessageResource(int resourceId) {
		CharSequence message = resourceId > 0 ? mActivity.getString(resourceId) : "";

		// Call our setMessage that works on UI thread
		setMessage(message);
	}

	/**
	 * Change progress message.
	 * This method works on UI thread.
	 *
	 * @param message
	 */
	@Override
	public void setMessage(final CharSequence message) {
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				BetterProgressDialog.super.setMessage(message);
			}
		});
	}
}
