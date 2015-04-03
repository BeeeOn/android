package com.rehivetech.beeeon.widget;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.device.RefreshInterval;
import com.rehivetech.beeeon.asynctask.ReloadFacilitiesTask;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.widget.old.WidgetUpdateService;

import java.util.ArrayList;
import java.util.List;

abstract public class WidgetConfiguration {
    private static final String TAG = WidgetConfiguration.class.getSimpleName();

    protected Activity mActivity;
    protected WidgetData mWidgetData;
    protected Controller mController;
    protected List<Adapter> mAdapters = new ArrayList<>();
    protected ReloadFacilitiesTask mReloadFacilitiesTask;
    protected Spinner mAdapterSpinner;
    protected SeekBar mWidgetUpdateSeekBar;
    protected Adapter mActiveAdapter;

    /**
     * Constructor initializes helper variables
     *
     * !!! CAN'T INITIALIZE ANY VIEW CAUSE HERE LAYOUT IS NOT INFLATED YET !!!
     *
     * @param data widgetData
     * @param activity context
     */
    public WidgetConfiguration(WidgetData data, Activity activity){
        mWidgetData = data;
        mActivity = activity;
    }

    /**
     * Here you can initialize helper view variables
     */
    public void inflationConstructor(){
        mAdapterSpinner = (Spinner) mActivity.findViewById(R.id.widgetConfAdapter);
        mWidgetUpdateSeekBar = (SeekBar) mActivity.findViewById(R.id.widgetConfIntervalWidget);

        // loads data if editing widget otherwise default data
        mWidgetData.loadData(mActivity);
        initWidgetUpdateIntervalLayout();
    }

    /**
     * Initializes working with controller
     */
    public void controllerConstructor() {
        mController = Controller.getInstance(mActivity);
        mAdapters = mController.getAdapters();
        mActiveAdapter = mController.getActiveAdapter();

        // sets adapter onclicklistener
        mAdapterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Adapter adapter = mAdapters.get(position);
                Log.d(TAG, String.format("Selected adapter %s", adapter.getName()));
                doChangeAdapter(adapter.getId(), "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "Selected no adapter");
                onNoAdapterSelected();
            }
        });
    }

    /**
     * When adapter was selected, need to reload data
     * @param adapterId
     * @param activeItemId
     */
    protected abstract void doChangeAdapter(String adapterId, String activeItemId);

    /**
     * When no adapter was selected
     */
    protected abstract void onNoAdapterSelected();

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

        int interval = Math.max(mWidgetData.interval, WidgetService.UPDATE_INTERVAL_MIN);
        int intervalIndex = RefreshInterval.fromInterval(interval).getIntervalIndex();
        mWidgetUpdateSeekBar.setProgress(intervalIndex);
        // set text of seekbar
        setIntervalWidgetText(intervalIndex);
    }

    /**
     * Returns layout xml which will be used in configuration activity
     * @return xml layout
     */
    public abstract int getConfigLayout();

    /**
     * Initializes layout and behaving
     */
    public abstract void initLayout();

    /**
     * Loads data and fill layout with them
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
        WidgetService.startUpdating(mActivity, new int[] { mWidgetData.getWidgetId() });
    }

    /**
     * Runs when clicked "cancel" to cancel creation of widget
     */
    public void startWidgetCancel(){}

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
