package com.rehivetech.beeeon.widget.location;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.device.RefreshInterval;
import com.rehivetech.beeeon.adapter.location.Location;
import com.rehivetech.beeeon.arrayadapter.LocationArrayAdapter;
import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.asynctask.ReloadLocationsTask;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.WidgetConfiguration;
import com.rehivetech.beeeon.widget.WidgetData;
import com.rehivetech.beeeon.widget.WidgetService;

import java.util.ArrayList;
import java.util.List;

public class WidgetLocationConfiguration extends WidgetConfiguration {
    private static final String TAG = WidgetLocationConfiguration.class.getSimpleName();

    private List<Location> mLocations = new ArrayList<Location>();
    private Spinner mLocationSpinner;
    private ReloadLocationsTask mReloadLocationsTask;

    public WidgetLocationConfiguration(WidgetData data, Activity activity) {
        super(data, activity);
    }

    @Override
    public int getConfigLayout() { return R.layout.activity_widget_configuration_location; }

    @Override
    public void inflationConstructor() {
        super.inflationConstructor();

        mLocationSpinner = (Spinner) mActivity.findViewById(R.id.widgetConfLocation);
    }

    @Override
    public void initLayout() {
        // location spinner selection
        mLocationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Location location = (Location) parent.getSelectedItem();

                Log.d(TAG, String.format("Selected location %s", location.getName()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "Selected no device");
            }
        });

        // loads data after initializing layout
        this.loadSettings();
    }

    @Override
    public void loadSettings() {
        String adapterId = mWidgetData.adapterId;
        String locationId = ((WidgetLocationData) mWidgetData).locationId;

        if (!adapterId.isEmpty()) {
            for (int i = 0; i < mAdapters.size(); i++) {
                if (mAdapters.get(i).getId().equals(adapterId)) {
                    mAdapterSpinner.setSelection(i);

                    List<Location> locations = mController.getLocations(adapterId);
                    mLocations = locations;

                    // Set locations to spinner
                    LocationArrayAdapter dataAdapter = new LocationArrayAdapter(mActivity, R.layout.custom_spinner_item, mLocations);
                    dataAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
                    mLocationSpinner.setAdapter(dataAdapter);

                    // set selection
                    int foundIndex = Utils.getObjectIndexFromList(locationId, mLocations);
                    if(foundIndex != -1) mLocationSpinner.setSelection(foundIndex);

                    break;
                }
            }

           doChangeAdapter(adapterId, locationId);
        }

    }

    @Override
    public boolean saveSettings() {
        Adapter adapter = (Adapter) mAdapterSpinner.getSelectedItem();
        if (adapter == null) {
            Toast.makeText(mActivity, R.string.widget_configuration_select_adapter, Toast.LENGTH_LONG).show();
            return false;
        }

        Location location = (Location) mLocationSpinner.getSelectedItem();
        if (location == null) {
            Toast.makeText(mActivity, R.string.widget_configuration_select_location, Toast.LENGTH_LONG).show();
            return false;
        }

        RefreshInterval refresh = RefreshInterval.values()[mWidgetUpdateSeekBar.getProgress()];
        mWidgetData.interval = Math.max(refresh.getInterval(), WidgetService.UPDATE_INTERVAL_MIN);
        mWidgetData.adapterId = adapter.getId();
        mWidgetData.lastUpdate = 0;         // nastavi, ze jeste nebylo updatovano
        mWidgetData.initialized = true;
        ((WidgetLocationData) mWidgetData).locationId = location.getId();
        // TODO toto asi pryc
        ((WidgetLocationData) mWidgetData).locationName = location.getName();
        ((WidgetLocationData) mWidgetData).locationIcon = location.getIconResource();

        mWidgetData.saveData(mActivity);

        return true;
    }

    @Override
    protected void doChangeAdapter(final String adapterId, final String activeLocationId) {
        mReloadLocationsTask = new ReloadLocationsTask(mActivity, false);
        mReloadLocationsTask.setListener(new CallbackTask.CallbackTaskListener() {
            @Override
            public void onExecute(boolean success){
                List<Location> locations = mController.getLocations(adapterId);
                mLocations.clear();
                mLocations.addAll(locations);

                // Set locations to spinner
                LocationArrayAdapter dataAdapter = new LocationArrayAdapter(mActivity, R.layout.custom_spinner_item, mLocations);
                dataAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
                mLocationSpinner.setEnabled(true);
                mLocationSpinner.setAdapter(dataAdapter);

                if (!activeLocationId.isEmpty()) {
                    int index = Utils.getObjectIndexFromList(activeLocationId, mLocations);
                    if (index != -1) mLocationSpinner.setSelection(index);
                }
            }
        });

        mLocationSpinner.setEnabled(false);
        mReloadLocationsTask.execute(adapterId);
    }

    @Override
    protected void onNoAdapterSelected(){
        mLocations.clear();
    }

}
