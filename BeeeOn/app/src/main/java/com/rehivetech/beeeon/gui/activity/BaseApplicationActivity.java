package com.rehivetech.beeeon.gui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gcm.INotificationReceiver;
import com.rehivetech.beeeon.gcm.notification.IGcmNotification;
import com.rehivetech.beeeon.gui.dialog.BetterProgressDialog;
import com.rehivetech.beeeon.threading.CallbackTaskManager;

import icepick.Icepick;

/**
 * Abstract parent for application activities that requires logged in user and better using of tasks.
 * <p>
 * When user is not logged in, it will switch to LoginActivity automatically.
 * Provides useful methods for using CallbackTasks.
 */
public abstract class BaseApplicationActivity extends AppCompatActivity implements INotificationReceiver {

	private static String TAG = BaseApplicationActivity.class.getSimpleName();

	@IntDef({INDICATOR_NONE, INDICATOR_BACK, INDICATOR_DISCARD, INDICATOR_ACCEPT, INDICATOR_MENU})
	@interface IndicatorType {
	}

	public static final int INDICATOR_NONE = 0;
	public static final int INDICATOR_BACK = 1;
	public static final int INDICATOR_MENU = R.drawable.ic_menu_white_24dp;
	public static final int INDICATOR_DISCARD = R.drawable.ic_clear_white_24dp;
	public static final int INDICATOR_ACCEPT = R.drawable.ic_done_white_24dp;

	private boolean triedLoginAlready = false;

//	public static String activeLocale = null;

	@Nullable
	private ProgressBar mProgressBar;
	@Nullable
	private View mRefreshIcon;
	@Nullable
	private BetterProgressDialog mProgressDialog;

	@Nullable
	protected ActionBar mActionBar;
	private Toolbar mToolbar;
	public CallbackTaskManager callbackTaskManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		setLocale();
		super.onCreate(savedInstanceState);
		Icepick.restoreInstanceState(this, savedInstanceState);
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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Icepick.saveInstanceState(this, outState);
	}

	/**
	 * Handling "home" action
	 *
	 * @param item clicked menu item
	 * @return if was consumed
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Redirects to login activity
	 *
	 * @param context
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
	 * Called from {@link CallbackTaskManager} when task is started/canceled.
	 *
	 * @param visible whether progressbar will be shown/hidden && refresh icon vice versa
	 */
	public synchronized void setBeeeOnProgressBarVisibility(boolean visible) {
		if (mToolbar == null) {
			return;
		}

		// if refresh icon was setup we either show progress or refresh icon
		if (mRefreshIcon != null) {
			mRefreshIcon.setVisibility(visible ? View.INVISIBLE : View.VISIBLE);
		}

		if (mProgressBar != null) {
			mProgressBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
		}
	}

	public synchronized void showProgressDialog() {
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
	 *
	 * @param title         string of title
	 * @param indicatorType if set to true adds "<-" arrow to title
	 * @return toolbar or null, if no R.id.beeeon_toolbar was found in layout
	 */
	@Nullable
	public Toolbar setupToolbar(String title, @IndicatorType int indicatorType) {
		mToolbar = (Toolbar) findViewById(R.id.beeeon_toolbar);
		if (mToolbar != null) {
			setSupportActionBar(mToolbar);
			mToolbar.setTitle(title);
		}
		mActionBar = getSupportActionBar();
		if (mActionBar != null) {
			mActionBar.setTitle(title);

			if (indicatorType != INDICATOR_NONE) {
				mActionBar.setHomeButtonEnabled(true);
				mActionBar.setDisplayHomeAsUpEnabled(true);
				if (indicatorType > INDICATOR_BACK) {
					mActionBar.setHomeAsUpIndicator(indicatorType);
				}
			}
		}

		mProgressBar = (ProgressBar) mToolbar.findViewById(R.id.beeeon_toolbar_progress);
		mRefreshIcon = mToolbar.findViewById(R.id.beeeon_toolbar_refresh);

		return mToolbar;
	}

	public Toolbar setupToolbar(String title) {
		return setupToolbar(title, INDICATOR_NONE);
	}

	public Toolbar setupToolbar(@StringRes int titleResId, @IndicatorType int indicatorType) {
		String title = getString(titleResId);
		return setupToolbar(title, indicatorType);
	}

	public Toolbar setupToolbar(@StringRes int titleResId) {
		return setupToolbar(titleResId, INDICATOR_NONE);
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
			mRefreshIcon = mToolbar.findViewById(R.id.beeeon_toolbar_refresh);

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
	 *
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
