package com.rehivetech.beeeon.widget.sensor;


import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.Facility;
import com.rehivetech.beeeon.adapter.device.RefreshInterval;
import com.rehivetech.beeeon.adapter.location.Location;
import com.rehivetech.beeeon.arrayadapter.DeviceArrayAdapter;
import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.asynctask.ReloadFacilitiesTask;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.WidgetConfiguration;
import com.rehivetech.beeeon.widget.WidgetData;
import com.rehivetech.beeeon.widget.WidgetService;

import java.util.ArrayList;
import java.util.List;

public class WidgetSensorConfiguration extends WidgetConfiguration{
    private static final String TAG = WidgetSensorConfiguration.class.getSimpleName();

    private List<Device> mDevices = new ArrayList<Device>();
    private List<Location> mLocations = new ArrayList<Location>();
    private Spinner mSensorSpinner;

    public WidgetSensorConfiguration(WidgetData data, Activity activity) {
        super(data, activity);
    }

    @Override
    public int getConfigLayout(){ return R.layout.activity_widget_configuration_sensor; }

    @Override
    public void inflationConstructor() {
        super.inflationConstructor();

        mSensorSpinner = (Spinner) mActivity.findViewById(R.id.widgetConfSensor);
    }

    @Override
    public void initLayout(){
        // sensor spinner selection
        mSensorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Device device = (Device) parent.getSelectedItem();
                TextView intervalText = (TextView) mActivity.findViewById(R.id.widgetConfIntervalSensor);
                intervalText.setText(device.getFacility().getRefresh().getStringInterval(mActivity));

                Log.d(TAG, String.format("Selected device %s", device.getName()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                TextView interval = (TextView) mActivity.findViewById(R.id.widgetConfIntervalSensor);
                interval.setText("");

                Log.d(TAG, "Selected no device");
            }
        });

        // loads data after initializing layout
        this.loadSettings();
    }

    @Override
    public void loadSettings() {
        String adapterId = mWidgetData.adapterId;
        String deviceId = ((WidgetSensorData) mWidgetData).deviceId;

        if (!adapterId.isEmpty()) {
            for (int i = 0; i < mAdapters.size(); i++) {
                if (mAdapters.get(i).getId().equals(adapterId)) {
                    mAdapterSpinner.setSelection(i);

                    List<Location> locations = mController.getLocations(adapterId);
                    mLocations = locations;

                    mDevices.clear();
                    // get all devices by locations (avoiding facility without location)
                    for(Location loc : mLocations){
                        List<Facility> tempFac = mController.getFacilitiesByLocation(adapterId, loc.getId());
                        for (Facility facility : tempFac) {
                            mDevices.addAll(facility.getDevices());
                        }
                    }

                    // fill sensor spinner
                    DeviceArrayAdapter dataAdapter = new DeviceArrayAdapter(mActivity, R.layout.custom_spinner2_item, mDevices, mLocations);
                    dataAdapter.setLayoutInflater(mActivity.getLayoutInflater());
                    dataAdapter.setDropDownViewResource(R.layout.custom_spinner2_dropdown_item);
                    mSensorSpinner.setAdapter(dataAdapter);

                    // select spinner
                    int foundIndex = Utils.getObjectIndexFromList(deviceId, mDevices);
                    if(foundIndex != -1) mSensorSpinner.setSelection(foundIndex);

                    break;
                }
            }

            doChangeAdapter(adapterId, deviceId);
        }

        // TODO tady v OldConfig byl jeste seekbar
    }

    @Override
    public boolean saveSettings() {
        Adapter adapter = (Adapter) mAdapterSpinner.getSelectedItem();
        if (adapter == null) {
            Toast.makeText(mActivity, R.string.widget_configuration_select_adapter, Toast.LENGTH_LONG).show();
            return false;
        }

        Device device = (Device) mSensorSpinner.getSelectedItem();
        if (device == null) {
            Toast.makeText(mActivity, R.string.widget_configuration_select_device, Toast.LENGTH_LONG).show();
            return false;
        }

        RefreshInterval refresh = RefreshInterval.values()[mWidgetUpdateSeekBar.getProgress()];
        mWidgetData.interval = Math.max(refresh.getInterval(), WidgetService.UPDATE_INTERVAL_MIN);
        mWidgetData.adapterId = adapter.getId();
        // TODO je potreba, kdyz to inicializuji?
        mWidgetData.lastUpdate = 0;         // nastavi, ze jeste nebylo updatovano
        mWidgetData.initialized = true;
        ((WidgetSensorData) mWidgetData).deviceId = device.getId();
        ((WidgetSensorData) mWidgetData).deviceName = device.getName();
        ((WidgetSensorData) mWidgetData).deviceIcon = device.getIconResource();

        mWidgetData.saveData(mActivity);

        // TODO pridat device do WidgetService
        //WidgetService.usedFacilities

        return true;
    }

    @Override
    protected void doChangeAdapter(final String adapterId, final String activeDeviceId) {
        mReloadFacilitiesTask = new ReloadFacilitiesTask(mActivity, false);
        mReloadFacilitiesTask.setListener(new CallbackTask.CallbackTaskListener() {

            @Override
            public void onExecute(boolean success) {
                // check if new locations are awailable
                mController.reloadLocations(adapterId, false);
                // get all locations
                mLocations.clear();
                List<Location> locations = mController.getLocations(adapterId);
                mLocations.addAll(locations);

                mDevices.clear();
                // get all devices by locations (avoiding facility without location)
                for(Location loc : mLocations){
                    List<Facility> tempFac = mController.getFacilitiesByLocation(adapterId, loc.getId());
                    for (Facility facility : tempFac) {
                        mDevices.addAll(facility.getDevices());
                    }
                }

                // fill sensor spinner
                DeviceArrayAdapter dataAdapter = new DeviceArrayAdapter(mActivity, R.layout.custom_spinner2_item, mDevices, mLocations);
                dataAdapter.setLayoutInflater(mActivity.getLayoutInflater());
                dataAdapter.setDropDownViewResource(R.layout.custom_spinner2_dropdown_item);
                mSensorSpinner.setEnabled(true);
                mSensorSpinner.setAdapter(dataAdapter);

                if (!activeDeviceId.isEmpty()) {
                    int index = Utils.getObjectIndexFromList(activeDeviceId, mDevices);
                    if(index != -1) mSensorSpinner.setSelection(index);
                }

                // TODO asi ten swipetorefresh
                //setProgressBarIndeterminateVisibility(false);
            }
        });

        mSensorSpinner.setEnabled(false);

        //setProgressBarIndeterminateVisibility(true);
        mReloadFacilitiesTask.execute(adapterId);
    }

    @Override
    protected void onNoAdapterSelected(){
        mDevices.clear();
        mLocations.clear();
    }
}
