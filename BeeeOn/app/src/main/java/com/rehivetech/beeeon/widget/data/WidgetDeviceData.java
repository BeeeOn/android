package com.rehivetech.beeeon.widget.data;

import android.content.Context;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.widget.persistence.WidgetDevicePersistence;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for sensor app widget (1x1, 2x1, 3x1)
 */
public class WidgetDeviceData extends WidgetData {
    private static final String TAG = WidgetDeviceData.class.getSimpleName();

    public WidgetDevicePersistence widgetDevice;
    protected List<Facility> mFacilities;

    public WidgetDeviceData(int widgetId, Context context, UnitsHelper unitsHelper, TimeHelper timeHelper){
        super(widgetId, context, unitsHelper, timeHelper);
        widgetDevice = new WidgetDevicePersistence(mContext, mWidgetId, 0, R.id.value_container, unitsHelper, timeHelper, settings);

        widgetDevices = new ArrayList<>();
        widgetDevices.add(widgetDevice);

        mFacilities = new ArrayList<>();
    }

    @Override
    public void init() {
        if(widgetDevice.getId().isEmpty()){
            Log.i(TAG, "Could not retrieve device from widget " + String.valueOf(mWidgetId));
            return;
        }

        String[] ids = widgetDevice.getId().split(Device.ID_SEPARATOR, 2);
        Facility facility = new Facility();
        facility.setAdapterId(adapterId);
        facility.setAddress(ids[0]);
        facility.setLastUpdate(new DateTime(widgetDevice.lastUpdateTime, DateTimeZone.UTC));
        facility.setRefresh(RefreshInterval.fromInterval(widgetDevice.refresh));

        Device dev = Device.createFromDeviceTypeId(ids[1]);
        facility.addDevice(dev);

        mFacilities.clear();
        mFacilities.add(facility);
    }

    @Override
    public void load() {
        super.load();
        widgetDevice.load();
    }

    @Override
    protected void save() {
        super.save();
        widgetDevice.save();
    }

    @Override
    public void delete(Context context) {
        super.delete(context);
        widgetDevice.delete();
    }

    @Override
    public List<Facility> getReferredObj() {
        return mFacilities;
    }

    @Override
    public void initLayout() {
        super.initLayout();
        // configuration
        mRemoteViews.setOnClickPendingIntent(R.id.options, mConfigurationPendingIntent);

        // sets onclick "listeners"
        mRemoteViews.setOnClickPendingIntent(R.id.value, mRefreshPendingIntent);
        mRemoteViews.setOnClickPendingIntent(R.id.last_update, mRefreshPendingIntent);
        mRemoteViews.setOnClickPendingIntent(R.id.refresh, mRefreshPendingIntent);

        if(!adapterId.isEmpty() && !widgetDevice.getId().isEmpty()){
            // detail activity
            mRemoteViews.setOnClickPendingIntent(R.id.icon, startDetailActivityPendingIntent(mContext, mWidgetId, adapterId, widgetDevice.getId()));
            mRemoteViews.setOnClickPendingIntent(R.id.name, startDetailActivityPendingIntent(mContext, mWidgetId, adapterId, widgetDevice.getId()));
        }

        widgetDevice.initValueView(mRemoteViews);
    }

    @Override
    protected boolean updateData() {
        Device device = mController.getFacilitiesModel().getDevice(adapterId, widgetDevice.getId());
        if(device == null) {
            Log.v(TAG, String.format("Updating widget (%d) with cached data", getWidgetId()));
            return false;
        }

        Adapter adapter = mController.getAdaptersModel().getAdapter(adapterId);
        widgetDevice.change(device, adapter);

        widgetLastUpdate = getTimeNow();
        adapterId = adapter.getId();

        this.save();
        Log.v(TAG, String.format("Updating widget (%d) with fresh data", getWidgetId()));
        return true;
    }

    @Override
    protected void updateLayout() {
        mRemoteViews.setImageViewResource(R.id.icon, widgetDevice.icon == 0 ? R.drawable.dev_unknown : widgetDevice.icon);
        mRemoteViews.setTextViewText(R.id.name, widgetDevice.getName());
        mRemoteViews.setTextViewText(R.id.last_update, widgetDevice.lastUpdateText);

        widgetDevice.updateValueView(false);

        switch(widgetLayout){
            case R.layout.widget_device_3x2:
            case R.layout.widget_device_3x1:
            case R.layout.widget_device_2x1:
                widgetDevice.setValueUnitSize(16);
                break;

            case R.layout.widget_sensor_1x1:
                widgetDevice.setValueUnitSize(12);
                break;
        }
    }

    @Override
    public void handleUserLogout() {
        super.handleUserLogout();
        widgetDevice.updateValueView(true);
        mRemoteViews.setImageViewResource(R.id.icon, widgetDevice.icon == 0 ? R.drawable.dev_unknown : widgetDevice.icon);
        mRemoteViews.setTextViewText(R.id.name, widgetDevice.getName());
        mRemoteViews.setTextViewText(R.id.last_update, String.format("%s (cached)", widgetDevice.lastUpdateText));
        updateAppWidget();
    }

    @Override
    public String getClassName() {
        return WidgetDeviceData.class.getName();
    }
}
