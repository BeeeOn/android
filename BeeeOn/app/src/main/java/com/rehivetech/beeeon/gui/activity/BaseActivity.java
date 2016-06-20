package com.rehivetech.beeeon.gui.activity;

import android.app.ProgressDialog;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.dialog.BetterProgressDialog;
import com.rehivetech.beeeon.gui.dialog.InfoDialogFragment;

import icepick.Icepick;

/**
 * @author mlyko
 * @since 30.05.2016
 */
public abstract class BaseActivity extends AppCompatActivity {

	private static final String TAG = BaseActivity.class.getSimpleName();
	protected static final String TAG_ABOUT_DIALOG = "about_dialog";

	@IntDef({INDICATOR_NONE, INDICATOR_BACK, INDICATOR_DISCARD, INDICATOR_ACCEPT, INDICATOR_MENU})
	@interface IndicatorType {
	}

	public static final int INDICATOR_NONE = 0;
	public static final int INDICATOR_BACK = 1;
	public static final int INDICATOR_MENU = R.drawable.ic_menu_white_24dp;
	public static final int INDICATOR_DISCARD = R.drawable.ic_clear_white_24dp;
	public static final int INDICATOR_ACCEPT = R.drawable.ic_done_white_24dp;

	//	public static String activeLocale = null;

	@Nullable
	private BetterProgressDialog mProgressDialog;
	@Nullable
	protected Toolbar mToolbar;
	@Nullable
	protected View mRefreshIcon;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		//		setLocale();
		super.onCreate(savedInstanceState);
		Icepick.restoreInstanceState(this, savedInstanceState);
	}

	/**
	 * Uses Icepick for saving instance variables -> just use @State
	 *
	 * @param outState persistent state
	 */
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

//		private void setLocale() {
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

	/**
	 * Shows about app dialog from any activity
	 */
	public void showAboutDialog() {
		InfoDialogFragment dialog = new InfoDialogFragment();
		dialog.show(getSupportFragmentManager(), TAG_ABOUT_DIALOG);
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

	// ------------------------------------------------------- //
	// -------------------- TOOLBAR SETUP -------------------- //
	// ------------------------------------------------------- //

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
		if (mToolbar == null) {
			Log.e(TAG, "Trying to setup toolbar without element in layout!");
			return null;
		}

		setSupportActionBar(mToolbar);
		mToolbar.setTitle(title);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setTitle(title);

			if (indicatorType != INDICATOR_NONE) {
				actionBar.setHomeButtonEnabled(true);
				actionBar.setDisplayHomeAsUpEnabled(true);
				if (indicatorType > INDICATOR_BACK) {
					actionBar.setHomeAsUpIndicator(indicatorType);
				}
			}
		}

		mRefreshIcon = mToolbar.findViewById(R.id.beeeon_toolbar_refresh);

		return mToolbar;
	}

	public Toolbar setupToolbar(@StringRes int titleResId, @IndicatorType int indicatorType) {
		return setupToolbar(getString(titleResId), indicatorType);
	}

	/**
	 * Setups toolbar with no indicator
	 *
	 * @param titleResId title res
	 * @return toolbar
	 */
	public Toolbar setupToolbar(@StringRes int titleResId) {
		return setupToolbar(titleResId, INDICATOR_NONE);
	}

	/**
	 * Setups toolbar with no indicator
	 *
	 * @param title string
	 * @return toolbar
	 */
	public Toolbar setupToolbar(String title) {
		return setupToolbar(title, INDICATOR_NONE);
	}

	public void setToolbarTitle(@StringRes int titleRes) {
		setToolbarTitle(getString(titleRes));
	}

	public void setToolbarTitle(String title) {
		ActionBar actionBar = getSupportActionBar();

		if (actionBar != null) {
			actionBar.setTitle(title);
		}
	}

	// ------------------------------------------------------- //
	// ----------------------- OTHERS ------------------------ //
	// ------------------------------------------------------- //

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
