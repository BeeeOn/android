package com.rehivetech.beeeon.widget.configuration;


import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.arrayadapter.DeviceArrayAdapter;
import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.asynctask.ReloadFacilitiesTask;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.data.WidgetClockData;
import com.rehivetech.beeeon.widget.data.WidgetData;
import com.rehivetech.beeeon.widget.persistence.WidgetDevice;
import com.rehivetech.beeeon.widget.service.WidgetService;

import java.util.ArrayList;
import java.util.List;

public class WidgetClockConfiguration extends WidgetConfiguration{
    private static final String TAG = WidgetClockConfiguration.class.getSimpleName();

    private List<Device> mDevices = new ArrayList<Device>();
    private List<Location> mLocations = new ArrayList<Location>();
    private Spinner mSensorSpinner;
    private Spinner mSensorSpinner2;

    private WidgetDevice mWidgetDevice;
    private WidgetDevice mWidgetDevice2;

    public WidgetClockConfiguration(WidgetData data, WidgetConfigurationActivity activity, boolean widgetEditing) {
        super(data, activity, widgetEditing);

        mWidgetDevice = ((WidgetClockData) mWidgetData).widgetDevices.get(0);
        mWidgetDevice2 = ((WidgetClockData) mWidgetData).widgetDevices.get(1);
    }

    @Override
    public int getConfigLayout(){ return R.layout.activity_widget_configuration_clock; }

    @Override
    public void inflationConstructor() {
        super.inflationConstructor();

        mSensorSpinner = (Spinner) mActivity.findViewById(R.id.widgetConfSensor);
        mSensorSpinner2 = (Spinner) mActivity.findViewById(R.id.widgetConfSensor2);
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
                doChangeAdapter(adapter.getId(), "", "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "Selected no adapter");
                mDevices.clear();
                mLocations.clear();
            }
        });
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

        // sensor spinner selection
        mSensorSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Device device = (Device) parent.getSelectedItem();
                TextView intervalText = (TextView) mActivity.findViewById(R.id.widgetConfIntervalSensor2);
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

        // loads data after initializing widgetLayout
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

                    mDevices.clear();
                    // get all devices by locations (avoiding facility without location)
                    for(Location loc : mLocations){
                        List<Facility> tempFac = mController.getFacilitiesModel().getFacilitiesByLocation(adapterId, loc.getId());
                        for (Facility facility : tempFac) {
                            mDevices.addAll(facility.getDevices());
                        }
                    }

                    // fill sensor spinner
                    DeviceArrayAdapter dataAdapter = new DeviceArrayAdapter(mActivity, R.layout.custom_spinner2_item, mDevices, mLocations);
                    dataAdapter.setLayoutInflater(mActivity.getLayoutInflater());
                    dataAdapter.setDropDownViewResource(R.layout.custom_spinner2_dropdown_item);
                    // select spinner
                    int foundIndex;

                    mSensorSpinner.setAdapter(dataAdapter);
                    foundIndex = Utils.getObjectIndexFromList(mWidgetDevice.id, mDevices);
                    if(foundIndex != -1) mSensorSpinner.setSelection(foundIndex);

                    mSensorSpinner2.setAdapter(dataAdapter);
                    foundIndex = Utils.getObjectIndexFromList(mWidgetDevice2.id, mDevices);
                    if(foundIndex != -1) mSensorSpinner2.setSelection(foundIndex);

                    break;
                }
            }

            doChangeAdapter(adapterId, mWidgetDevice.id, mWidgetDevice2.id);
        }
    }

    @Override
    public boolean saveSettings() {
        Adapter adapter = (Adapter) mAdapterSpinner.getSelectedItem();
        if (adapter == null) {
            Toast.makeText(mActivity, R.string.widget_configuration_select_adapter, Toast.LENGTH_LONG).show();
            return false;
        }

        WidgetClockData data = (WidgetClockData) mWidgetData;
        Spinner tempSpinner = mSensorSpinner;
        for(WidgetDevice dev : data.widgetDevices){
            Device device = (Device) tempSpinner.getSelectedItem();
            if (device == null) {
                Toast.makeText(mActivity, R.string.widget_configuration_select_device, Toast.LENGTH_LONG).show();
                return false;
            }

            dev.configure(device, adapter);

            // TODO udelat jinak nez takto retardovane
            tempSpinner = mSensorSpinner2;
        }

        RefreshInterval refresh = RefreshInterval.values()[mWidgetUpdateSeekBar.getProgress()];
        //sets widgetdata
        mWidgetData.configure(isWidgetEditing, Math.max(refresh.getInterval(), WidgetService.UPDATE_INTERVAL_MIN), adapter.getId());

        return true;
    }

    protected void doChangeAdapter(final String adapterId, final String activeDeviceId, final String activeDevice2Id) {
        mReloadFacilitiesTask = new ReloadFacilitiesTask(mActivity, false);
        mReloadFacilitiesTask.setListener(new CallbackTask.CallbackTaskListener() {

            @Override
            public void onExecute(boolean success) {
                // check if new locations are awailable
                mController.getLocationsModel().reloadLocationsByAdapter(adapterId, false);
                // get all locations
                mLocations.clear();
                List<Location> locations = mController.getLocationsModel().getLocationsByAdapter(adapterId);
                mLocations.addAll(locations);

                mDevices.clear();
                // get all devices by locations (avoiding facility without location)
                for (Location loc : mLocations) {
                    List<Facility> tempFac = mController.getFacilitiesModel().getFacilitiesByLocation(adapterId, loc.getId());
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

                mSensorSpinner2.setEnabled(true);
                mSensorSpinner2.setAdapter(dataAdapter);

                String devId = (activeDeviceId.isEmpty() && mWidgetDevice.adapterId.equals(adapterId)) ? mWidgetDevice.id : activeDeviceId;
                String dev2Id = (activeDevice2Id.isEmpty() && mWidgetDevice2.adapterId.equals(adapterId)) ? mWidgetDevice2.id : activeDevice2Id;

                if (!devId.isEmpty()) {
                    int index = Utils.getObjectIndexFromList(devId, mDevices);
                    if (index != -1) mSensorSpinner.setSelection(index);
                }

                if (!dev2Id.isEmpty()) {
                    int index = Utils.getObjectIndexFromList(dev2Id, mDevices);
                    if (index != -1) mSensorSpinner2.setSelection(index);
                }

                mActivity.getDialog().dismiss();
            }
        });

        mSensorSpinner.setEnabled(false);
        mSensorSpinner2.setEnabled(false);

        mActivity.getDialog().setMessage(mActivity.getString(R.string.progress_loading_facilities));
        mActivity.getDialog().show();
        mReloadFacilitiesTask.execute(adapterId);
    }
}
