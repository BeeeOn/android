package com.rehivetech.beeeon.gui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gcm.INotificationReceiver;
import com.rehivetech.beeeon.gcm.notification.IGcmNotification;
import com.rehivetech.beeeon.gui.dialog.BetterProgressDialog;
import com.rehivetech.beeeon.threading.CallbackTaskManager;

/**
 * Abstract parent for application activities that requires logged in user and better using of tasks.
 * <p/>
 * When user is not logged in, it will switch to LoginActivity automatically.
 * Provides useful methods for using CallbackTasks.
 */
public abstract class BaseApplicationActivity extends AppCompatActivity implements INotificationReceiver {

	private static String TAG = BaseApplicationActivity.class.getSimpleName();

	private boolean triedLoginAlready = false;

//	public static String activeLocale = null;

	@Nullable
	private View mProgressBar;
	@Nullable
	private View mRefreshIcon;
	@Nullable
	private BetterProgressDialog mProgressDialog;

	@Nullable
	protected ActionBar mActionBar;

	protected boolean isPaused = false;

	public CallbackTaskManager callbackTaskManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		setLocale();

		super.onCreate(savedInstanceState);

		callbackTaskManager = new CallbackTaskManager(this);
	}

//	private void setLocale() {
//		// We need to set locale only once per application lifetime
//		if (activeLocale != null) {
//			return;
//		}
//
//		SharedPreferences prefs = Controller.getInstance(this).getUserSettings();
//		Language.Item lang = (Language.Item) new Language().fromSettings(prefs);
//		activeLocale = lang.getCode();
//
//		Resources res = getResources();
//		DisplayMetrics dm = res.getDisplayMetrics();
//		Configuration conf = res.getConfiguration();
//		conf.locale = new Locale(activeLocale);
//		res.updateConfiguration(conf, dm);
//	}


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
		intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

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
	 * Helper for initializing toolbar and actionbar
	 * @param title string of title
	 * @param backButton if set to true adds "<-" arrow to title
	 * @return toolbar or null, if no R.id.beeeon_toolbar was found in layout
	 */
	@Nullable
	public Toolbar setupToolbar(String title, boolean backButton) {
		Toolbar toolbar = (Toolbar) findViewById(R.id.beeeon_toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
			toolbar.setTitle(title);
		}
		mActionBar = getSupportActionBar();
		if (mActionBar != null) {
			mActionBar.setTitle(title);

			if (backButton) {
				mActionBar.setHomeButtonEnabled(true);
				mActionBar.setDisplayHomeAsUpEnabled(true);
			}
		}

		return toolbar;
	}

	public Toolbar setupToolbar(String title){
		return setupToolbar(title, false);
	}

	public Toolbar setupToolbar(@StringRes int titleResId, boolean backButton){
		String title = getString(titleResId);
		return setupToolbar(title, backButton);
	}

	public Toolbar setupToolbar(@StringRes int titleResId) {
		return setupToolbar(titleResId, false);
	}

	public void setToolbarTitle(String title) {
		ActionBar actionBar = getSupportActionBar();

		if (actionBar != null) {
			actionBar.setTitle(title);
		}
	}

	/**
	 * When set, refresh icon will be shown in Toolbar and when async task, icon will be hidden/visible
	 * {@link #setBeeeOnProgressBarVisibility(boolean)} changes visibility of icon
	 *
	 * @param onClickListener Callback for refresh icon
	 */
	public void setupRefreshIcon(View.OnClickListener onClickListener) {
		if (onClickListener == null) {
			if (mRefreshIcon != null) {
				// Reset callback
				mRefreshIcon.setOnClickListener(null);
				mRefreshIcon.setOnLongClickListener(null);

				// Hide refresh icon
				mRefreshIcon.setVisibility(View.INVISIBLE);
				mRefreshIcon = null;
			}
			return;
		}

		if (mRefreshIcon == null) {
			mRefreshIcon = findViewById(R.id.beeeon_toolbar_refresh);

			if (mRefreshIcon == null) {
				// This activity probably doesn't have refreshIcon (or toolbar) in layout
				Log.w(TAG, String.format("Can't set visibility of refreshIcon in '%s', it wasn't found in layout.", getClass().getSimpleName()));
				return;
			}
		}

		boolean show = (mProgressBar == null || mProgressBar.getVisibility() == View.INVISIBLE);
		mRefreshIcon.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
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

	/**
	 * Set's status bar color programmatically
	 *
	 * @param statusBarColor
	 */
	public void setStatusBarColor(@ColorInt int statusBarColor) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setStatusBarColor(statusBarColor);
		}
	}

	@Override
	public void onSupportActionModeStarted(ActionMode mode) {
		super.onSupportActionModeStarted(mode);
		setStatusBarColor(ContextCompat.getColor(this, R.color.gray_status_bar));
	}

	@Override
	public void onSupportActionModeFinished(ActionMode mode) {
		super.onSupportActionModeFinished(mode);
		setStatusBarColor(ContextCompat.getColor(this, R.color.beeeon_primary_dark));
	}

	/**
	 * Replace fragment in activity and add current to backStack
	 * @param currentFragmentTag
	 * @param fragment
	 */
	public void replaceFragment(String currentFragmentTag, Fragment fragment) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.addToBackStack(currentFragmentTag);
		transaction.replace(R.id.activity_add_dashboard_container, fragment);
		transaction.commit();
	}
}
