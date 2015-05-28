package com.rehivetech.beeeon.widget.configuration;

import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.Log;

/**
 * @author mlyko
 */
public class WidgetConfigurationActivity extends ActionBarActivity{
	private static final String TAG = WidgetConfigurationActivity.class.getSimpleName();

	public static final String EXTRA_WIDGET_EDITING = "com.rehivetech.beeeon.widget.EXTRA_WIDGET_EDITING";

	private Toolbar mToolbar;
	private AppWidgetManager mAppWidgetManager;
	private ProgressDialog mDialog;
	private Fragment mConfigFragment;


	private boolean mAppWidgetEditing = false;
	private int mWidgetId;
	private boolean mReturnResult = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_widget_configuration);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		// if no extras, there's no widget id -> exit
		if(extras == null) {
			Log.e(TAG, "No widget Id => finish()");
			finishActivity();
			return;
		}

		// Prepare progress dialog
		mDialog = new ProgressDialog(this);
		mDialog.setMessage(getString(R.string.progress_loading_adapters));
		mDialog.setCancelable(false);
		mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		// prepare toolbar with button instead of "HOME" arrow
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if (mToolbar != null) {
			mToolbar.setTitle(R.string.title_activity_widget_configuration);
			setSupportActionBar(mToolbar);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_accept);
		}

		// do we edit or create widget
		mAppWidgetEditing = extras.getBoolean(EXTRA_WIDGET_EDITING, false);
		mAppWidgetManager = AppWidgetManager.getInstance(this);

		// get informations about widget
		mWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		// if the user press BACK, do not add any widget
		returnIntent(false);

		// get widgetprovider class
		String widgetProviderClassName = mAppWidgetManager.getAppWidgetInfo(mWidgetId).provider.getClassName();
		// need to check this ways cause debug version has whole namespace in className
		int lastDot = widgetProviderClassName.lastIndexOf('.');
		String widgetProviderShortClassName = widgetProviderClassName.substring(lastDot);

		// ------------ add here awailable widgets
		switch(widgetProviderShortClassName){
			case ".WidgetClockProvider":
				mConfigFragment = new WidgetClockFragment();
				break;

			case ".WidgetModuleProvider":
			case ".WidgetModuleProviderMedium":
			case ".WidgetModuleProviderLarge":
				mConfigFragment = new WidgetModuleFragment();
				break;

			case ".WidgetGraphProvider":
				mConfigFragment = new WidgetGraphFragment();
				break;

			case ".WidgetLocationListProvider":
                mConfigFragment = new WidgetLocationFragment();
                break;

			default:
				Log.e(TAG, "No widget with class: " + widgetProviderShortClassName);
				finishActivity();
				break;
		}

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.widget_config_fragment, mConfigFragment);
		ft.commit();
	}

	/**
	 * Finishes the configuration of widget, calls widget-specific startWidgetOk / startWidgetCancel
	 * @param success if true activity finishes with widget creation
	 */
	public void returnIntent(boolean success){
		// return the original widget ID, found in onCreate()
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, getWidgetId());
		// prepare result of configuration widget
		setResult(success ? RESULT_OK : RESULT_CANCELED, resultValue);
		mReturnResult = success;
	}

	/**
	 * Finishes activity with last set result - returnIntent()
	 */
	public void finishActivity(){
		// TODO somehow manage to alway going to homescreen
		finish();
	}

	public ProgressDialog getDialog() {
		return mDialog;
	}

	public ProgressDialog getDialog(String title){
		if(mDialog != null) mDialog.setMessage(title);
		return mDialog;
	}

	public int getWidgetId() {
		return mWidgetId;
	}

	public Toolbar getToolbar() {
		return mToolbar;
	}

	public boolean isReturnResult() {
		return mReturnResult;
	}

	public boolean isAppWidgetEditing() {
		return mAppWidgetEditing;
	}
}
