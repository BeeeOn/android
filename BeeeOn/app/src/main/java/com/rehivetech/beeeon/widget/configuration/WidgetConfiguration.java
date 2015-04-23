package com.rehivetech.beeeon.widget.configuration;

import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.asynctask.ReloadAdapterDataTask;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.widget.data.WidgetData;
import com.rehivetech.beeeon.widget.service.WidgetService;

import java.util.ArrayList;
import java.util.List;

abstract public class WidgetConfiguration {
    private static final String TAG = WidgetConfiguration.class.getSimpleName();

    protected WidgetConfigurationActivity mActivity;
    protected WidgetData mWidgetData;
    protected Controller mController;
    protected List<Adapter> mAdapters = new ArrayList<>();
    protected ReloadAdapterDataTask mReloadFacilitiesTask;
    protected Spinner mAdapterSpinner;
    protected SeekBar mWidgetUpdateSeekBar;
    protected Adapter mActiveAdapter;

    protected boolean isWidgetEditing = false;

    /**
     * Constructor initializes helper variables
     *
     * !!! CAN'T INITIALIZE ANY VIEW CAUSE HERE LAYOUT IS NOT INFLATED YET !!!
     *
     * @param data widgetData
     * @param activity context
     */
    public WidgetConfiguration(WidgetData data, WidgetConfigurationActivity activity, boolean widgetEditing){
        mWidgetData = data;
        mActivity = activity;
        isWidgetEditing = widgetEditing;
    }

    /**
     * Here you can initialize helper view variables
     */
    public void inflationConstructor(){
        mAdapterSpinner = (Spinner) mActivity.findViewById(R.id.widgetConfAdapter);
        mWidgetUpdateSeekBar = (SeekBar) mActivity.findViewById(R.id.widgetConfIntervalWidget);
        initWidgetUpdateIntervalLayout();
    }

    /**
     * Initializes working with controller
     */
    public void controllerConstructor() {
        mController = Controller.getInstance(mActivity);
        mAdapters = mController.getAdaptersModel().getAdapters();
        mActiveAdapter = mController.getActiveAdapter();
    }

    /**
     * Initializes widget update interval seekbar and text
     */
    protected void initWidgetUpdateIntervalLayout() {
        // Set Max value by length of array with values
        mWidgetUpdateSeekBar.setMax(RefreshInterval.values().length - 1);
        // set interval
        mWidgetUpdateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setIntervalWidgetText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Nothing to do here
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Nothing to do here
            }
        });

        int interval = Math.max(mWidgetData.widgetInterval, WidgetService.UPDATE_INTERVAL_MIN);
        int intervalIndex = RefreshInterval.fromInterval(interval).getIntervalIndex();
        mWidgetUpdateSeekBar.setProgress(intervalIndex);
        // set text of seekbar
        setIntervalWidgetText(intervalIndex);
    }

    /**
     * Returns widgetLayout xml which will be used in configuration activity
     * @return xml widgetLayout
     */
    public abstract int getConfigLayout();

    /**
     * Initializes widgetLayout and behaving
     */
    public abstract void initLayout();

    /**
     * Loads data and fill widgetLayout with them
     */
    public abstract void loadSettings();

    /**
     * Saves data to Widgetdata and sharedPreferences
     * @return
     */
    public abstract boolean saveSettings();

    /**
     * Runs when clicked "ok" to done creation of widget
     * !!! Starts the service !!!
     */
    public void startWidgetOk(){
        WidgetService.startUpdating(mActivity, new int[] { mWidgetData.getWidgetId() }, isWidgetEditing);
    }

    /**
     * Runs when clicked "cancel" to cancel creation of widget
     */
    public void startWidgetCancel(){
    }

    /**
     * Sets widget interval text
     * @param intervalIndex index in seekbar
     */
    protected void setIntervalWidgetText(int intervalIndex) {
        TextView intervalText = (TextView) mActivity.findViewById(R.id.widgetConfIntervalWidgetText);
        String interval = RefreshInterval.values()[intervalIndex].getStringInterval(mActivity);
        intervalText.setText(interval);
    }
}
