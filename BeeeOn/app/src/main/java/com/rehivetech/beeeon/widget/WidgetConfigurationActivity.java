package com.rehivetech.beeeon.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.widget.clock.WidgetClockConfiguration;
import com.rehivetech.beeeon.widget.clock.WidgetClockData;
import com.rehivetech.beeeon.widget.location.WidgetLocationConfiguration;
import com.rehivetech.beeeon.widget.location.WidgetLocationData;
import com.rehivetech.beeeon.widget.sensor.WidgetSensorConfiguration;
import com.rehivetech.beeeon.widget.sensor.WidgetSensorData;

import java.util.ArrayList;
import java.util.List;

//TODO asi pridat loading dialog protoze v actionbaru jsou buttony

/**
 * If adding new widget, needs to be added to the switch in onCreate() and initialize() methods
 */
public class WidgetConfigurationActivity extends ActionBarActivity {
    private static final String TAG = WidgetConfigurationActivity.class.getSimpleName();

    // helper variables
    private Context mContext;
    private Toolbar mToolbar;
    private Controller mController;
    private AppWidgetManager mAppWidgetManager;

    // widget specific variables
    private String mWidgetShortClassName;
    private WidgetData mWidgetData;
    private WidgetConfiguration mWidgetConfiguration;

    private List<Adapter> mAdapters = new ArrayList<>();

    // user logged in system variables
    private boolean isInitialized = false;
    private boolean triedLoginAlready = false;

    /**
     * Creates activity, created class for widgetData and inflate widget-specific configuration layout
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
            finishMinimize();
            return;
        }

        // get informations about widget
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        String widgetClassName = mAppWidgetManager.getAppWidgetInfo(appWidgetId).provider.getClassName();

        // need to check this ways cause debug version has whole namespace in className
        int lastDot = widgetClassName.lastIndexOf('.');
        mWidgetShortClassName = widgetClassName.substring(lastDot);

        // ------------ add here awailable widgets
        switch(mWidgetShortClassName){
            case ".WidgetClockProvider":
                mWidgetData = new WidgetClockData(appWidgetId);
                mWidgetConfiguration = new WidgetClockConfiguration(mWidgetData, this);
                break;

            case ".WidgetLocationListProvider":
                mWidgetData = new WidgetLocationData(appWidgetId);
                mWidgetConfiguration = new WidgetLocationConfiguration(mWidgetData, this);
                break;

            case ".WidgetSensorProvider":
            case ".WidgetSensorProviderMedium":
            case ".WidgetSensorProviderLarge":
                mWidgetData = new WidgetSensorData(appWidgetId);
                mWidgetConfiguration = new WidgetSensorConfiguration(mWidgetData, this);
                break;

            default:
                Log.d(TAG, "No widget with class: " + mWidgetShortClassName);
                finishMinimize();
                break;
        }

        // no valid ID, so bail out
        if (mWidgetData == null || mWidgetData.getWidgetId() == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finishMinimize();
            return;
        }

        // if the user press BACK, do not add any widget
        returnIntent(false);
        // creates widget-specific layout
        setContentView(mWidgetConfiguration.getConfigLayout());

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.title_activity_widget_configuration);
            setSupportActionBar(mToolbar);
        }

        // every widget has different layout, so inflate it here
        mWidgetConfiguration.inflationConstructor();
    }

    private void finishMinimize(){
        moveTaskToBack(true);
        finish();
    }

    /**
     * Fetch adapters, if none, tries to log in to app.
     * If success, add widgetData to service and calls widget-specific layout initialization
     */
    public void onResume(){
        super.onResume();

        mController = Controller.getInstance(mContext);
        // controls that we have any adapter, if not tries to login or finish()
        mAdapters = mController.getAdapters();
        if (mAdapters.isEmpty()) {
            if (!mController.isLoggedIn() && !triedLoginAlready) {
                // If user is not logged in we redirect to LoginActivity
                triedLoginAlready = true;
                Toast.makeText(this, R.string.widget_configuration_login_first, Toast.LENGTH_LONG).show();
                BaseApplicationActivity.redirectToLogin(this);
            } else if (mController.isLoggedIn()) {
                // Otherwise he is logged in but has no sensors, we quit completely
                Toast.makeText(this, R.string.widget_configuration_no_adapters, Toast.LENGTH_LONG).show();
                finishMinimize();
            }

            return;
        } else {
            triedLoginAlready = false;
        }

        if (!isInitialized) {
            isInitialized = true;

            // add widgetData to service
            WidgetService.awailableWidgets.put(mWidgetData.getWidgetId(), mWidgetData);
            initGeneralLayout();
            mWidgetConfiguration.controllerConstructor();
            mWidgetConfiguration.initLayout();
        }
    }

    /**
     * Finishes the configuration of widget, calls widget-specific startWidgetOk / startWidgetCancel
     * @param success if true activity finishes with widget creation
     */
    private void returnIntent(boolean success){
        // return the original widget ID, found in onCreate()
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetData.getWidgetId());

        if(success){
            mWidgetConfiguration.startWidgetOk();
            setResult(RESULT_OK, resultValue);
        }
        else{
            mWidgetConfiguration.startWidgetCancel();
            setResult(RESULT_CANCELED, resultValue);
        }
    }

    /**
     * Set whole layout of activity
     */
    private void initGeneralLayout(){
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

    /**
     * Sets actionbar as two buttons layout -> Done, Cancel
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
                        finishMinimize();
                    }
                });
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // "Cancel"
                        Log.d(TAG, "CANCEL clicked");
                        returnIntent(false);
                        finishMinimize();
                    }
                });

        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }
}
