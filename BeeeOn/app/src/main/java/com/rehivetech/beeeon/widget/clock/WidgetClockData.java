package com.rehivetech.beeeon.widget.clock;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.widget.RemoteViews;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.WidgetData;
import com.rehivetech.beeeon.widget.WidgetService;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;

/**
 * Class for clock widget (2x2)
 */
public class WidgetClockData extends WidgetData {
    private static final String TAG = WidgetClockData.class.getSimpleName();

    private static final String PREF_DEVICE_ID = "device";
    private static final String PREF_DEVICE_NAME = "device_name";
    private static final String PREF_DEVICE_ICON = "device_icon";
    private static final String PREF_DEVICE_VALUE = "device_value";
    private static final String PREF_DEVICE_UNIT = "device_unit";
    private static final String PREF_DEVICE_LAST_UPDATE_TEXT = "device_last_update_text";
    private static final String PREF_DEVICE_LAST_UPDATE_TIME = "device_last_update_time";
    private static final String PREF_DEVICE_REFRESH = "device_refresh";

    public static String weekDays[] = reloadWeekDays();

    // publicly accessible properties of widget
    public String deviceId;
    public String deviceName;
    public int deviceIcon;
    public String deviceValue;
    public String deviceUnit;
    public long deviceLastUpdateTime;
    public String deviceLastUpdateText;
    public int deviceRefresh;

    public WidgetClockData(int widgetId, Context context) {
        super(widgetId, context);
        widgetProvider = new WidgetClockProvider();
        mClassName = WidgetClockData.class.getName();

        // updates clock
        onUpdateClock(mContext, mRemoteViews, new int[]{mWidgetId});

        if(adapterId.length() > 0 && deviceId.length() > 0){
            mRemoteViews.setOnClickPendingIntent(R.id.widget_clock_temperature_in, startDetailActivityPendingIntent(mContext, mWidgetId, adapterId, deviceId));
        }
    }

    public static String[] reloadWeekDays(){
        return new DateFormatSymbols().getShortWeekdays();
    }

    /**
     * Updates widget's time asynchroningly to sensor updates
     * Updates always on time broadcasts
     * @param context
     */
    public static void onUpdateClock(Context context, RemoteViews rv, int[] appWidgetIds) {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        Calendar cal = Calendar.getInstance(context.getResources().getConfiguration().locale);
        cal.setTime(new Date());

        if(appWidgetIds == null) return;

        for(int widgetId : appWidgetIds) {
            Log.d(TAG, String.format("onUpdateClock(%d)", widgetId));

            // tries to get initialized first
            if(rv == null){
                rv = new RemoteViews(context.getPackageName(), R.layout.widget_clock);
            }

            rv.setTextViewText(R.id.widget_clock_hours, String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)));
            rv.setTextViewText(R.id.widget_clock_minutes, String.format("%02d", cal.get(Calendar.MINUTE)));

            rv.setTextViewText(R.id.widget_clock_date, DateTimeFormat.shortDate().print(cal.getTimeInMillis()));
            rv.setTextViewText(R.id.widget_clock_day_of_week, weekDays[cal.get(Calendar.DAY_OF_WEEK)]);

            // request widget redraw
            widgetManager.updateAppWidget(widgetId, rv);
        }
    }

    @Override
    public boolean prepare(){
        Log.d(TAG, String.format("prepare(%d)", mWidgetId));
        // Prepare list of facilities for network request
        if (deviceId.isEmpty() || adapterId.isEmpty()) return false;

        String[] ids = deviceId.split(Device.ID_SEPARATOR, 2);

        // TODO toto urcite bude treba prekopat
        Facility facility = Utils.getFromList(ids[0], WidgetService.usedFacilities);
        if(facility == null){
            Log.d(TAG, String.format("Need to create fac from widgetData(%d)", getWidgetId()));

            facility = new Facility();
            facility.setAdapterId(adapterId);
            facility.setAddress(ids[0]);
            facility.setLastUpdate(new DateTime(deviceLastUpdateTime, DateTimeZone.UTC));
            facility.setRefresh(RefreshInterval.fromInterval(deviceRefresh));

            facility.addDevice(Device.createFromDeviceTypeId(ids[1]));

            //WidgetService.usedFacilities.put(facility.getId(), facility);
            WidgetService.usedFacilities.add(facility);
        }

        return true;
    }

    @Override
    public void changeData() {
        long timeNow = SystemClock.elapsedRealtime();

        Adapter adapter = mController.getAdaptersModel().getAdapter(adapterId);

        Device device = mController.getFacilitiesModel().getDevice(adapterId, deviceId);

        if (device != null) {
            // Get fresh data from device
            deviceIcon = device.getIconResource();
            deviceName = device.getName();
            adapterId = device.getFacility().getAdapterId();
            deviceId = device.getId();
            lastUpdate = timeNow;
            deviceLastUpdateTime = device.getFacility().getLastUpdate().getMillis();
            deviceRefresh = device.getFacility().getRefresh().getInterval();

            // Check if we can format device's value (unitsHelper is null when user is not logged in)
            if (mUnitsHelper != null) {
                deviceValue = mUnitsHelper.getStringValue(device.getValue());
                deviceUnit = mUnitsHelper.getStringUnit(device.getValue());
            }

            // Check if we can format device's last update (timeHelper is null when user is not logged in)
            if (mTimeHelper != null) {
                // NOTE: This should use always absolute time, because widgets aren't updated so often
                deviceLastUpdateText = mTimeHelper.formatLastUpdate(device.getFacility().getLastUpdate(), adapter);
            }

            // Save fresh data
            saveData(mContext);

            Log.v(TAG, String.format("Updating widget (%d) with fresh data", getWidgetId()));
        }
        else {
            // TODO bug -> pridava se (cached) porad
            // NOTE: just temporary solution until it will be showed better on widget
            deviceLastUpdateText = String.format("%s %s", deviceLastUpdateText, mContext.getString(R.string.widget_cached));

            Log.v(TAG, String.format("Updating widget (%d) with cached data", getWidgetId()));
        }
    }

    @Override
    public void setLayoutValues() {
        mRemoteViews.setTextViewText(R.id.widget_clock_temperature_in, String.format("%s %s", deviceValue, deviceUnit));

        // TODO temporary solution
        onUpdateClock(mContext, mRemoteViews, new int[]{mWidgetId});

        updateLayout();
    }

    @Override
    public void loadData(Context context) {
        super.loadData(context);

        deviceId = mPrefs.getString(PREF_DEVICE_ID, "");
        deviceName = mPrefs.getString(PREF_DEVICE_NAME, context.getString(R.string.placeholder_not_exists));
        deviceIcon = mPrefs.getInt(PREF_DEVICE_ICON, 0);
        deviceValue = mPrefs.getString(PREF_DEVICE_VALUE, "");
        deviceUnit = mPrefs.getString(PREF_DEVICE_UNIT, "");
        deviceLastUpdateText = mPrefs.getString(PREF_DEVICE_LAST_UPDATE_TEXT, "");
        deviceLastUpdateTime = mPrefs.getLong(PREF_DEVICE_LAST_UPDATE_TIME, 0);
        deviceRefresh = mPrefs.getInt(PREF_DEVICE_REFRESH, 0);
    }

    @Override
    public void saveData(Context context) {
        super.saveData(context);

        getSettings(context).edit()
                .putString(PREF_DEVICE_ID, deviceId)
                .putString(PREF_DEVICE_NAME, deviceName)
                .putInt(PREF_DEVICE_ICON, deviceIcon)
                .putString(PREF_DEVICE_VALUE, deviceValue)
                .putString(PREF_DEVICE_UNIT, deviceUnit)
                .putString(PREF_DEVICE_LAST_UPDATE_TEXT, deviceLastUpdateText)
                .putLong(PREF_DEVICE_LAST_UPDATE_TIME, deviceLastUpdateTime)
                .putInt(PREF_DEVICE_REFRESH, deviceRefresh)
                .commit();
    }
}
