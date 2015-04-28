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
    protected List<Object> mFacilities;

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
    public List<Object> getReferredObj() {
        return mFacilities;
    }

    @Override
    public void initLayout() {
        super.initLayout();
        // configuration
        mBuilder.setOnClickListener(R.id.options, mConfigurationPendingIntent);

        // sets onclick "listeners"
        // mBuilder.setOnClickListener(R.id.value, mRefreshPendingIntent);
        mBuilder.setOnClickListener(R.id.widget_last_update, mRefreshPendingIntent);
        mBuilder.setOnClickListener(R.id.refresh, mRefreshPendingIntent);

        if(!adapterId.isEmpty() && !widgetDevice.getId().isEmpty()){
            // detail activity
            mBuilder.setOnClickListener(R.id.icon, startDetailActivityPendingIntent(mContext, mWidgetId, adapterId, widgetDevice.getId()));
            mBuilder.setOnClickListener(R.id.name, startDetailActivityPendingIntent(mContext, mWidgetId, adapterId, widgetDevice.getId()));
        }

        widgetDevice.initView();
    }

    @Override
    protected boolean updateData() {
        Device device = mController.getFacilitiesModel().getDevice(adapterId, widgetDevice.getId());
        if(device == null) {
            Log.v(TAG, String.format("Updating widget (%d) with cached data", getWidgetId()));
            return false;
        }

        Adapter adapter = mController.getAdaptersModel().getAdapter(adapterId);
        widgetDevice.configure(device, adapter);

        widgetLastUpdate = getTimeNow();
        adapterId = adapter.getId();

        this.save();
        Log.v(TAG, String.format("Updating widget (%d) with fresh data", getWidgetId()));
        return true;
    }

    @Override
    protected void renderLayout() {
        mBuilder.setImage(R.id.icon, widgetDevice.icon == 0 ? R.drawable.dev_unknown : widgetDevice.icon);
        mBuilder.setTextViewText(R.id.name, widgetDevice.getName());

        if(mIsCached){
            widgetDevice.renderView(mBuilder, true, "");
            mBuilder.setTextViewText(R.id.widget_last_update, String.format("%s " + mContext.getString(R.string.widget_cached), widgetDevice.lastUpdateText));
        }
        else {
            widgetDevice.renderView(mBuilder);
        }

        switch(widgetLayout){
            case R.layout.widget_device_3x1:
            case R.layout.widget_device_2x1:
                mBuilder.setTextViewText(R.id.widget_last_update, widgetDevice.lastUpdateText);
            case R.layout.widget_device_3x2:
                widgetDevice.setValueUnitSize(R.dimen.textsize_subhead);
                break;

            case R.layout.widget_device_1x1:
                widgetDevice.setValueUnitSize(R.dimen.textsize_caption);
                break;
        }
    }

    @Override
    public void handleUserLogout() {
        super.handleUserLogout();

        renderWidget();
    }

    @Override
    public void handleResize(int minWidth, int minHeight) {
        super.handleResize(minWidth, minHeight);

        int layout;
        // 1 cell
        if(minWidth < WIDGET_MIN_CELLS_2){
            layout = R.layout.widget_device_1x1;
        }
        // 2 cells
        else if(minWidth >= WIDGET_MIN_CELLS_2 && minWidth < WIDGET_MIN_CELLS_3){
            layout = R.layout.widget_device_2x1;
        }
        // 3 cells
        else{
            layout = R.layout.widget_device_3x1;
        }

       changeLayout(layout);
    }

    @Override
    public String getClassName() {
        return WidgetDeviceData.class.getName();
    }
}
