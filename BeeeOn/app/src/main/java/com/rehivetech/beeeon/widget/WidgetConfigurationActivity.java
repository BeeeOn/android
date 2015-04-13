package com.rehivetech.beeeon.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.rehivetech.beeeon.asynctask.FullReloadTask;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.widget.clock.WidgetClockConfiguration;
import com.rehivetech.beeeon.widget.clock.WidgetClockData;
import com.rehivetech.beeeon.widget.clock.WidgetClockFragment;
import com.rehivetech.beeeon.widget.location.WidgetLocationConfiguration;
import com.rehivetech.beeeon.widget.location.WidgetLocationData;
import com.rehivetech.beeeon.widget.device.WidgetDeviceConfiguration;
import com.rehivetech.beeeon.widget.device.WidgetDeviceData;

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
    private String mWidgetProviderShortClassName;
    private WidgetData mWidgetData;
    private WidgetConfiguration mWidgetConfiguration;

    private Fragment mCfgFragment;

    private FullReloadTask mFullReloadTask;

    private List<Adapter> mAdapters = new ArrayList<>();

    // user logged in system variables
    private boolean isInitialized = false;
    private boolean triedLoginAlready = false;
    private boolean mReturnResult = false;

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
            finishActivity();
            return;
        }

        // get informations about widget
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        String widgetProviderClassName = mAppWidgetManager.getAppWidgetInfo(appWidgetId).provider.getClassName();

        // need to check this ways cause debug version has whole namespace in className
        int lastDot = widgetProviderClassName.lastIndexOf('.');
        mWidgetProviderShortClassName = widgetProviderClassName.substring(lastDot);

        // ------------ add here awailable widgets
        switch(mWidgetProviderShortClassName){
            case ".WidgetClockProvider":
                mWidgetData = new WidgetClockData(appWidgetId, mContext);
                mWidgetConfiguration = new WidgetClockConfiguration(mWidgetData, this);

                /*
                mCfgFragment = new WidgetClockFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.widget_config_fragment, mCfgFragment);
                ft.commit();
                //*/

                break;

            case ".WidgetLocationListProvider":
                mWidgetData = new WidgetLocationData(appWidgetId, mContext);
                mWidgetConfiguration = new WidgetLocationConfiguration(mWidgetData, this);
                break;

            case ".WidgetDeviceProvider":
            case ".WidgetDeviceProviderMedium":
            case ".WidgetDeviceProviderLarge":
                mWidgetData = new WidgetDeviceData(appWidgetId, mContext);
                mWidgetConfiguration = new WidgetDeviceConfiguration(mWidgetData, this);
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
     * Fetch adapters, if none, tries to log in to app.
     * If success, add widgetData to service and calls widget-specific layout initialization
     */
    public void onResume(){
        super.onResume();

        mController = Controller.getInstance(mContext);

        // controls that we have any adapter, if not tries to login or finish()
        mAdapters = mController.getAdaptersModel().getAdapters();
        if (mAdapters.isEmpty()) {
            if (!mController.isLoggedIn() && !triedLoginAlready) {
                // If user is not logged in we redirect to LoginActivity
                triedLoginAlready = true;
                Toast.makeText(this, R.string.widget_configuration_login_first, Toast.LENGTH_LONG).show();
                BaseApplicationActivity.redirectToLogin(this);
            } else if (mController.isLoggedIn()) {
                // Otherwise he is logged in but has no sensors, we quit completely
                Toast.makeText(this, R.string.widget_configuration_no_adapters, Toast.LENGTH_LONG).show();
                finishActivity();
            }

            return;
        } else {
            triedLoginAlready = false;
        }

        if (!isInitialized) {
            isInitialized = true;
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
        // prepare result of configuration widget
        setResult(success ? RESULT_OK : RESULT_CANCELED, resultValue);
        mReturnResult = success;
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
