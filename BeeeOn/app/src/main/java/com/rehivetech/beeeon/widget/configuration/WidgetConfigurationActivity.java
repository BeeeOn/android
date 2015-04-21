package com.rehivetech.beeeon.widget.configuration;

import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.asynctask.FullReloadTask;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ErrorCode;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.widget.data.WidgetClockData;
import com.rehivetech.beeeon.widget.data.WidgetData;
import com.rehivetech.beeeon.widget.data.WidgetDeviceData;
import com.rehivetech.beeeon.widget.data.WidgetGraphData;
import com.rehivetech.beeeon.widget.data.WidgetLocationData;

import java.util.ArrayList;
import java.util.List;

//TODO asi pridat loading dialog protoze v actionbaru jsou buttony

/**
 * If adding new widget, needs to be added to the switch in onCreate() and initialize() methods
 */
public class WidgetConfigurationActivity extends ActionBarActivity {
    private static final String TAG = WidgetConfigurationActivity.class.getSimpleName();

    public static final String EXTRA_WIDGET_EDITING = "com.rehivetech.beeeon.widget.EXTRA_WIDGET_EDITING";

    // helper variables
    private Context mContext;
    private Toolbar mToolbar;
    private Controller mController;
    private AppWidgetManager mAppWidgetManager;

    // widget specific variables
    private String mWidgetProviderShortClassName;
    private WidgetData mWidgetData;
    private WidgetConfiguration mWidgetConfiguration;

    private Fragment mCfgFragment;

    private FullReloadTask mFullReloadTask;

    private ProgressDialog mDialog;

    private List<Adapter> mAdapters = new ArrayList<>();

    private boolean mReturnResult = false;

    /**
     * Creates activity, created class for widgetData and inflate widget-specific configuration widgetLayout
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();
        mAppWidgetManager = AppWidgetManager.getInstance(mContext);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        // if no extras, there's no widget id -> exit
        if(extras == null) {
            Log.d(TAG, "No widget Id => finish()");
            finishActivity();
            return;
        }

        // Prepare progress dialog
        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getString(R.string.progress_loading_adapters));
        mDialog.setCancelable(false);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // get informations about widget
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        String widgetProviderClassName = mAppWidgetManager.getAppWidgetInfo(appWidgetId).provider.getClassName();

        // do we edit or create widget
        boolean isAppWidgetEditing = extras.getBoolean(EXTRA_WIDGET_EDITING, false);

        // need to check this ways cause debug version has whole namespace in className
        int lastDot = widgetProviderClassName.lastIndexOf('.');
        mWidgetProviderShortClassName = widgetProviderClassName.substring(lastDot);

        // ------------ add here awailable widgets
        switch(mWidgetProviderShortClassName){
            case ".WidgetClockProvider":
                mWidgetData = new WidgetClockData(appWidgetId, mContext, null, null);


                /*
                mCfgFragment = new WidgetClockFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.widget_config_fragment, mCfgFragment);
                ft.commit();
                //*/

                break;


            case ".WidgetLocationListProvider":
                mWidgetData = new WidgetLocationData(appWidgetId, mContext, null, null);
                break;

            case ".WidgetDeviceProvider":
            case ".WidgetDeviceProviderMedium":
            case ".WidgetDeviceProviderLarge":
                mWidgetData = new WidgetDeviceData(appWidgetId, mContext, null, null);
                break;

            case ".WidgetGraphProvider":
                mWidgetData = new WidgetGraphData(appWidgetId, mContext, null, null);
                break;

            default:
                Log.d(TAG, "No widget with class: " + mWidgetProviderShortClassName);
                finishActivity();
                break;
        }

        // no valid ID, so bail out
        if (mWidgetData == null || mWidgetData.getWidgetId() == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finishActivity();
            return;
        }

        mWidgetConfiguration = mWidgetData.createConfiguration(this, isAppWidgetEditing);

        // if the user press BACK, do not add any widget
        returnIntent(false);
        // creates widget-specific widgetLayout
        setContentView(mWidgetConfiguration.getConfigLayout());

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.title_activity_widget_configuration);
            setSupportActionBar(mToolbar);
        }

        // every widget has different widgetLayout, so inflate it here
        mWidgetConfiguration.inflationConstructor();
    }


    /**
     * Fetch adapters, if none, tries to log in to app.
     * If success, add widgetData to service and calls widget-specific widgetLayout initialization
     */
    public void onResume(){
        super.onResume();
        Log.d(TAG, "onResume()");

        mFullReloadTask = new FullReloadTask(this, false);
        mFullReloadTask.setNotifyErrors(false);
        mFullReloadTask.setListener(new CallbackTask.CallbackTaskListener() {
            @Override
            public void onExecute(boolean success) {
                if (!success) {
                    AppException e = mFullReloadTask.getException();
                    ErrorCode errCode = e != null ? e.getErrorCode() : null;
                    if (errCode != null) {
                        if (errCode instanceof NetworkError && errCode == NetworkError.SRV_BAD_BT) {
                            BaseApplicationActivity.redirectToLogin(mContext);
                            return;
                        }
                        Toast.makeText(WidgetConfigurationActivity.this, e.getTranslatedErrorMessage(WidgetConfigurationActivity.this), Toast.LENGTH_LONG).show();
                    }
                }

                // Redraw Activity
                Log.d(TAG, "After reload task - go to redraw mainActivity");
                if (mDialog != null) mDialog.dismiss();

                redrawActivity();
            }
        });

        if(mDialog != null) mDialog.show();
        Log.d(TAG, "Execute fullReloadTask");
        mFullReloadTask.execute();
    }

    /**
     * Redraw activity (after login) .. there needs to be Controller initialization otherwise no data found
     */
    private void redrawActivity(){
        mController = Controller.getInstance(mContext);
        initGeneralLayout();
        mWidgetConfiguration.controllerConstructor();
        mWidgetConfiguration.initLayout();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        if(mDialog != null) mDialog.dismiss();

        if(mFullReloadTask != null) mFullReloadTask.cancel(true);
    }

    /**
     * Finishes the configuration of widget, calls widget-specific startWidgetOk / startWidgetCancel
     * @param success if true activity finishes with widget creation
     */
    private void returnIntent(boolean success){
        // return the original widget ID, found in onCreate()
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetData.getWidgetId());
        // prepare result of configuration widget
        setResult(success ? RESULT_OK : RESULT_CANCELED, resultValue);
        mReturnResult = success;
    }

    /**
     * Specified to start or delete widget
     */
    private void finishActivity(){
        // pred ukoncenim aktivity zavolame konec konfigurace widgetu
        if(mWidgetConfiguration != null) {
            if (mReturnResult) {
                mWidgetConfiguration.startWidgetOk();
            } else {
                mWidgetConfiguration.startWidgetCancel();
            }
        }

        finish();
    }

    /**
     * Set whole widgetLayout of activity
     */
    private void initGeneralLayout(){
        mAdapters = mController.getAdaptersModel().getAdapters();
        // hide adapter choice if there's only 1 adapter
        if(mAdapters.size() == 1){
            LinearLayout adapterLayout = (LinearLayout) findViewById(R.id.widgetConfAdapterLayout);
            //adapterLayout.setVisibility(View.GONE);
        }

        initActionBarLayout();

        // adapter spinner
        Spinner adapterSpinner = (Spinner) findViewById(R.id.widgetConfAdapter);
        ArrayAdapter<?> arrayAdapter = new ArrayAdapter<Adapter>(this, android.R.layout.simple_spinner_dropdown_item, mAdapters);
        adapterSpinner.setAdapter(arrayAdapter);
    }

    public ProgressDialog getDialog(){
        return mDialog;
    }

    /**
     * Sets actionbar as two buttons widgetLayout -> Done, Cancel
     */
    private void initActionBarLayout(){
        // set actionMode with done and cancel button
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_done_cancel, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // "Done"
                        Log.d(TAG, "DONE clicked");

                        if(!mWidgetConfiguration.saveSettings()) return;

                        returnIntent(true);
                        finishActivity();
                    }
                });
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // "Cancel"
                        Log.d(TAG, "CANCEL clicked");
                        returnIntent(false);
                        finishActivity();
                    }
                });

        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }
}
