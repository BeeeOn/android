package com.rehivetech.beeeon.widget.configuration;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.arrayadapter.LocationArrayAdapter;
import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.asynctask.ReloadLocationsTask;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.data.WidgetData;
import com.rehivetech.beeeon.widget.data.WidgetLocationData;
import com.rehivetech.beeeon.widget.persistence.WidgetLocationPersistence;
import com.rehivetech.beeeon.widget.service.WidgetService;

import java.util.ArrayList;
import java.util.List;

public class WidgetLocationConfiguration extends WidgetConfiguration {
    private static final String TAG = WidgetLocationConfiguration.class.getSimpleName();

    private List<Location> mLocations = new ArrayList<Location>();
    private Spinner mLocationSpinner;

    private ReloadLocationsTask mReloadLocationsTask;
    private WidgetLocationPersistence mWidgetLocation;

    public WidgetLocationConfiguration(WidgetData data, WidgetConfigurationActivity activity, boolean widgetEditing) {
        super(data, activity, widgetEditing);
        mWidgetLocation = ((WidgetLocationData) mWidgetData).widgetLocation;
    }

    @Override
    public int getConfigLayout() { return R.layout.activity_widget_configuration_location; }

    @Override
    public void inflationConstructor() {
        super.inflationConstructor();

        mLocationSpinner = (Spinner) mActivity.findViewById(R.id.widgetConfLocation);
    }

    @Override
    public void controllerConstructor() {
        super.controllerConstructor();

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
                mLocations.clear();
            }
        });
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

        if (!adapterId.isEmpty()) {
            for (int i = 0; i < mAdapters.size(); i++) {
                if (mAdapters.get(i).getId().equals(adapterId)) {
                    mAdapterSpinner.setSelection(i);

                    List<Location> locations = mController.getLocationsModel().getLocationsByAdapter(adapterId);
                    mLocations = locations;

                    // Set locations to spinner
                    LocationArrayAdapter dataAdapter = new LocationArrayAdapter(mActivity, R.layout.custom_spinner_item, mLocations);
                    dataAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
                    mLocationSpinner.setAdapter(dataAdapter);

                    // set selection
                    int foundIndex = Utils.getObjectIndexFromList(mWidgetLocation.getId(), mLocations);
                    if(foundIndex != -1) mLocationSpinner.setSelection(foundIndex);

                    break;
                }
            }

           doChangeAdapter(adapterId, mWidgetLocation.getId());
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

        // sets widgetpersistence
        mWidgetLocation.configure(location, adapter);
        // sets widgetdata
        RefreshInterval refresh = RefreshInterval.values()[mWidgetUpdateSeekBar.getProgress()];
        mWidgetData.configure(isWidgetEditing, Math.max(refresh.getInterval(), WidgetService.UPDATE_INTERVAL_MIN), adapter.getId());

        return true;
    }

    protected void doChangeAdapter(final String adapterId, final String activeLocationId) {
        mReloadLocationsTask = new ReloadLocationsTask(mActivity, false);
        mReloadLocationsTask.setListener(new CallbackTask.CallbackTaskListener() {
            @Override
            public void onExecute(boolean success){
                List<Location> locations = mController.getLocationsModel().getLocationsByAdapter(adapterId);
                mLocations.clear();
                mLocations.addAll(locations);

                // Set locations to spinner
                LocationArrayAdapter dataAdapter = new LocationArrayAdapter(mActivity, R.layout.custom_spinner_item, mLocations);
                dataAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
                mLocationSpinner.setEnabled(true);
                mLocationSpinner.setAdapter(dataAdapter);

                String locId = (activeLocationId.isEmpty() && mWidgetLocation.getAdapterId().equals(adapterId)) ? mWidgetLocation.getId() : activeLocationId;

                if (!locId.isEmpty()) {
                    int index = Utils.getObjectIndexFromList(locId, mLocations);
                    if (index != -1) mLocationSpinner.setSelection(index);
                }

                mActivity.getDialog().dismiss();
            }
        });

        mLocationSpinner.setEnabled(false);
        mActivity.getDialog().setMessage(mActivity.getString(R.string.progress_loading_locations));
        mActivity.getDialog().show();
        mReloadLocationsTask.execute(adapterId);
    }


}
