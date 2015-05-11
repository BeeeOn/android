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

    protected List<Object> mFacilities;

    /**
     * Constructing object holding information about widget (instantiating in config activity and then in service)
     *
     * @param widgetId
     * @param context
     * @param unitsHelper
     * @param timeHelper
     */
    public WidgetDeviceData(int widgetId, Context context, UnitsHelper unitsHelper, TimeHelper timeHelper) {
        super(widgetId, context, unitsHelper, timeHelper);

        widgetDevices = new ArrayList<>();
        widgetDevices.add(new WidgetDevicePersistence(mContext, mWidgetId, 0, R.id.value_container, unitsHelper, timeHelper, settings));

        mFacilities = new ArrayList<>();
    }

    // ----------------------------------------------------------- //
    // ---------------- MANIPULATING PERSISTENCE ----------------- //
    // ----------------------------------------------------------- //

    @Override
    public void load() {
        super.load();
        WidgetDevicePersistence.loadAll(widgetDevices);
    }

    @Override
    public void init() {
        mFacilities.clear();
        for(WidgetDevicePersistence dev : widgetDevices){
            if(dev.getId().isEmpty()){
                Log.i(TAG, "Could not retrieve device from widget " + String.valueOf(mWidgetId));
                continue;
            }

            String[] ids = dev.getId().split(Device.ID_SEPARATOR, 2);
            Facility facility = new Facility();
            facility.setAdapterId(widgetAdapterId);
            facility.setAddress(ids[0]);
            facility.setLastUpdate(new DateTime(dev.lastUpdateTime, DateTimeZone.UTC));
            facility.setRefresh(RefreshInterval.fromInterval(dev.refresh));
            facility.addDevice(Device.createFromDeviceTypeId(ids[1]));

            mFacilities.add(facility);
        }
    }

    @Override
    public void save() {
        super.save();
        WidgetDevicePersistence.saveAll(widgetDevices);
    }

    // ----------------------------------------------------------- //
    // ------------------------ RENDERING ------------------------ //
    // ----------------------------------------------------------- //

    @Override
    protected void renderLayout() {
        // -------------------- initialize layout
        mBuilder.setOnClickListener(R.id.options, mConfigurationPendingIntent);
        mBuilder.setOnClickListener(R.id.widget_last_update, mRefreshPendingIntent);
        mBuilder.setOnClickListener(R.id.refresh, mRefreshPendingIntent);

        if(widgetAdapterId.isEmpty()) return;

        for(WidgetDevicePersistence dev : widgetDevices) {
            // detail activity
            mBuilder.setOnClickListener(R.id.icon, startDetailActivityPendingIntent(mContext, mWidgetId + dev.getOffset(), widgetAdapterId, dev.getId()));
            mBuilder.setOnClickListener(R.id.name, startDetailActivityPendingIntent(mContext, mWidgetId + dev.getOffset(), widgetAdapterId, dev.getId()));
            dev.initView();
        }

        // -------------------- render layout
        // updates all inside devices
        boolean isFirst = true;
        for(WidgetDevicePersistence dev : widgetDevices){
            if(isFirst){
                mBuilder.setImage(R.id.icon, dev.icon == 0 ? R.drawable.dev_unknown : dev.icon);
                mBuilder.setTextViewText(R.id.name, dev.getName());

                isFirst = false;
            }

            // render view based on if is cached information
            dev.renderView(mBuilder, getIsCached(), "");

            switch(widgetLayout){
                case R.layout.widget_device_3x1:
                case R.layout.widget_device_2x1:
                    mBuilder.setTextViewText(R.id.widget_last_update, getIsCached() ? String.format("%s " + mContext.getString(R.string.widget_cached), dev.lastUpdateText) : dev.lastUpdateText);

                case R.layout.widget_device_3x2:
                    dev.setValueUnitSize(R.dimen.textsize_subhead);
                    break;

                case R.layout.widget_device_1x1:
                    dev.setValueUnitSize(R.dimen.textsize_caption);
                    break;
            }
        }
    }

    // ----------------------------------------------------------- //
    // ---------------------- FAKE HANDLERS ---------------------- //
    // ----------------------------------------------------------- //

    @Override
    public boolean handleUpdateData() {
        int updated = 0;
        Adapter adapter = mController.getAdaptersModel().getAdapter(widgetAdapterId);
        if(adapter == null) return false;

        for(WidgetDevicePersistence dev : widgetDevices) {
            Device device = mController.getFacilitiesModel().getDevice(widgetAdapterId, dev.getId());
            if(device != null) {
                dev.configure(device, adapter);
            }
            updated++;
        }

        if(updated > 0) {
            // update last update to "now"
            widgetLastUpdate = getTimeNow();
            widgetAdapterId = adapter.getId();

            // Save fresh data
            this.save();
            Log.v(TAG, String.format("Updating widget (%d) with fresh data", getWidgetId()));
        }
        else {
            // TODO show some kind of icon
            Log.v(TAG, String.format("Updating widget (%d) with cached data", getWidgetId()));
        }

        return updated > 0;
    }

    @Override
    public void handleResize(int minWidth, int minHeight) {
        super.handleResize(minWidth, minHeight);

        int layout;
        // 1 cell
        if(minWidth < 170){
            layout = R.layout.widget_device_1x1;
        }
        // 2 cells
        else if(minWidth < 200){
            layout = R.layout.widget_device_2x1;
        }
        // 3 cells
        else{
            layout = R.layout.widget_device_3x1;
        }

        changeLayout(layout);
    }

    // ----------------------------------------------------------- //
    // ------------------------- GETTERS ------------------------- //
    // ----------------------------------------------------------- //

    @Override
    public List<Object> getObjectsToReload() {
        return mFacilities;
    }

    @Override
    public String getClassName() {
        return WidgetDeviceData.class.getName();
    }

}