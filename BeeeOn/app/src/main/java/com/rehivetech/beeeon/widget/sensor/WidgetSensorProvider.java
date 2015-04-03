package com.rehivetech.beeeon.widget.sensor;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.SensorDetailActivity;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.DeviceType;
import com.rehivetech.beeeon.adapter.device.Facility;
import com.rehivetech.beeeon.adapter.device.RefreshInterval;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.WidgetData;
import com.rehivetech.beeeon.widget.WidgetProvider;
import com.rehivetech.beeeon.widget.WidgetService;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * @author mlyko
 */
public class WidgetSensorProvider extends WidgetProvider{
    private static final String TAG = WidgetSensorProvider.class.getSimpleName();

    protected WidgetSensorData mWidgetData;
    protected Device mDevice;

    @Override
    public void initialize(Context context, WidgetData data){
        super.initialize(context, data);
        mWidgetData = (WidgetSensorData) data;

        // sets onclick "listeners"
        mRemoteViews.setOnClickPendingIntent(R.id.value, mRefreshPendingIntent);
        mRemoteViews.setOnClickPendingIntent(R.id.last_update, mRefreshPendingIntent);
        mRemoteViews.setOnClickPendingIntent(R.id.refresh, mRefreshPendingIntent);

        // configuration
        mRemoteViews.setOnClickPendingIntent(R.id.options, mConfigurationPendingIntent);

        // open detail activity on click to icon
        if (mWidgetData.adapterId.length() > 0 && mWidgetData.deviceId.length() > 0) {
            Intent intent = new Intent(mContext, SensorDetailActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(SensorDetailActivity.EXTRA_DEVICE_ID, mWidgetData.deviceId);
            intent.putExtra(SensorDetailActivity.EXTRA_ADAPTER_ID, mWidgetData.adapterId);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, mWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mRemoteViews.setOnClickPendingIntent(R.id.icon, pendingIntent);
        }
    }

    @Override
    public boolean prepare(){
        Log.d(TAG, String.format("prepare(%d)", mWidgetId));
        // Prepare list of facilities for network request
        if (mWidgetData.deviceId.isEmpty() || mWidgetData.adapterId.isEmpty()) return false;

        String[] ids = mWidgetData.deviceId.split(Device.ID_SEPARATOR, 2);

        // TODO toto urcite bude treba prekopat
        Facility facility = Utils.getFromList(ids[0], WidgetService.usedFacilities);
        if(facility == null){
            Log.d(TAG, String.format("Need to create fac from widgetData(%d)", mWidgetData.getWidgetId()));

            facility = new Facility();
            facility.setAdapterId(mWidgetData.adapterId);
            facility.setAddress(ids[0]);
            facility.setLastUpdate(new DateTime(mWidgetData.deviceLastUpdateTime, DateTimeZone.UTC));
            facility.setRefresh(RefreshInterval.fromInterval(mWidgetData.deviceRefresh));

            facility.addDevice(Device.createFromDeviceTypeId(ids[1]));

            //WidgetService.usedFacilities.put(facility.getId(), facility);
            WidgetService.usedFacilities.add(facility);
        }
        return true;
    }

    public void changeData() {
        long now = SystemClock.elapsedRealtime();
        Controller controller = Controller.getInstance(mContext);
        SharedPreferences userSettings = controller.getUserSettings();
        // UserSettings can be null when user is not logged in!
        UnitsHelper unitsHelper = (userSettings == null) ? null : new UnitsHelper(userSettings, mContext);
        TimeHelper timeHelper = (userSettings == null) ? null : new TimeHelper(userSettings);

        Adapter adapter = controller.getAdapter(mWidgetData.adapterId);

        Device device = controller.getDevice(mWidgetData.adapterId, mWidgetData.deviceId);

        if (device != null) {
            // Get fresh data from device
            mWidgetData.deviceIcon = device.getIconResource();
            mWidgetData.deviceName = device.getName();
            mWidgetData.adapterId = device.getFacility().getAdapterId();
            mWidgetData.deviceId = device.getId();
            mWidgetData.lastUpdate = now;
            mWidgetData.deviceLastUpdateTime = device.getFacility().getLastUpdate().getMillis();
            mWidgetData.deviceRefresh = device.getFacility().getRefresh().getInterval();

            // Check if we can format device's value (unitsHelper is null when user is not logged in)
            if (unitsHelper != null) {
                mWidgetData.deviceValue = unitsHelper.getStringValue(device.getValue());
                mWidgetData.deviceUnit = unitsHelper.getStringUnit(device.getValue());
            }

            // Check if we can format device's last update (timeHelper is null when user is not logged in)
            if (timeHelper != null) {
                // NOTE: This should use always absolute time, because widgets aren't updated so often
                mWidgetData.deviceLastUpdateText = timeHelper.formatLastUpdate(device.getFacility().getLastUpdate(), adapter);
            }

            // Save fresh data
            mWidgetData.saveData(mContext);

            Log.v(TAG, String.format("Updating widget (%d) with fresh data", mWidgetData.getWidgetId()));
        }
        else {
            // TODO bug -> pridava se (cached) porad
            // NOTE: just temporary solution until it will be showed better on widget
            mWidgetData.deviceLastUpdateText = String.format("%s %s", mWidgetData.deviceLastUpdateText, mContext.getString(R.string.widget_cached));

            Log.v(TAG, String.format("Updating widget (%d) with cached data", mWidgetData.getWidgetId()));
        }
    }

    @Override
    public void setValues() {
        Log.d(TAG, String.format("setValues(%d)", mWidgetId));

        mRemoteViews.setImageViewResource(R.id.icon, mWidgetData.deviceIcon == 0 ? R.drawable.dev_unknown : mWidgetData.deviceIcon);
        mRemoteViews.setTextViewText(R.id.name, mWidgetData.deviceName);
        mRemoteViews.setTextViewText(R.id.value, mWidgetData.deviceValue);
        mRemoteViews.setTextViewText(R.id.unit, mWidgetData.deviceUnit);

        switch(mWidgetData.layout){
            case R.layout.widget_sensor_3x1:

                //appWidgetProviderInfo.resizeMode = AppWidgetProviderInfo.RESIZE_BOTH;
                //AppWidgetHostView host = new AppWidgetHostView(context);
                //host.setAppWidget(widgetId, appWidgetProviderInfo);


            case R.layout.widget_sensor_2x1:
                // For classic (= not-small) layout of widget, set also lastUpdate
                mRemoteViews.setTextViewText(R.id.last_update, mWidgetData.deviceLastUpdateText);
                break;

            default:
                break;
        }

        // request widget redraw
        mWidgetManager.updateAppWidget(mWidgetId, mRemoteViews);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Log.d(TAG, "onAppWidgetOptionsChanged()");
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        int min_width = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int max_width = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int min_height = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        int max_height = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);

        int layout = R.layout.widget_sensor_1x1;
        String name = "widget_sensor_1x1.xml";

        if(min_width >= WIDGET_2_CELLS){
            layout = R.layout.widget_sensor_2x1;
            name = "widget_sensor_2x1.xml";
        }

        if(min_width >= WIDGET_3_CELLS){
            layout = R.layout.widget_sensor_3x1;
            name = "widget_sensor_3x1.xml";
        }


        Log.d(TAG, String.format("[%d-%d] x [%d-%d] -> %s", min_width, max_width, min_height, max_height, name));

        WidgetSensorData mWidgetData = (WidgetSensorData) WidgetService.getWidgetData(appWidgetId, context);
        if(mWidgetData == null) return;

        mWidgetData.saveLayout(context, layout);
        mWidgetData.saveData(context);

        // force update widget
        context.startService(WidgetService.getForceUpdateIntent(context, appWidgetId));
    }

}
