package com.rehivetech.beeeon.widget.data;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.widget.RemoteViews;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.widget.configuration.WidgetClockConfiguration;
import com.rehivetech.beeeon.widget.configuration.WidgetConfiguration;
import com.rehivetech.beeeon.widget.configuration.WidgetConfigurationActivity;
import com.rehivetech.beeeon.widget.persistence.WidgetDevicePersistence;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Class for clock widget (2x2)
 */
public class WidgetClockData extends WidgetData {
    private static final String TAG = WidgetClockData.class.getSimpleName();

    public static String weekDays[] = reloadWeekDays();

    public List<WidgetDevicePersistence> widgetDevices;
    private List<Facility> mFacilities;

    public WidgetClockData(int widgetId, Context context, UnitsHelper unitsHelper, TimeHelper timeHelper){
        super(widgetId, context, unitsHelper, timeHelper);

        // inside devices
        widgetDevices = new ArrayList<>();
        widgetDevices.add(new WidgetDevicePersistence(mContext, mWidgetId, 0, R.id.value_container_inside_temp, unitsHelper, timeHelper));
        widgetDevices.add(new WidgetDevicePersistence(mContext, mWidgetId, 1, R.id.value_container_inside_humid, unitsHelper, timeHelper));

        mFacilities = new ArrayList<>();
        load();
    }

    @Override
    public void init() {
        mFacilities.clear();
        for(WidgetDevicePersistence dev : widgetDevices){
            String[] ids = dev.getId().split(Device.ID_SEPARATOR, 2);
            // TODO  zde nekdy je deviceId prazdne ci tak neco a nevytvori se objekt
            Facility facility = new Facility();
            facility.setAdapterId(adapterId);
            facility.setAddress(ids[0]);
            facility.setLastUpdate(new DateTime(dev.lastUpdateTime, DateTimeZone.UTC));
            facility.setRefresh(RefreshInterval.fromInterval(dev.refresh));
            facility.addDevice(Device.createFromDeviceTypeId(ids[1]));

            mFacilities.add(facility);
        }

        onUpdateClock(mContext, null, new int[]{mWidgetId});
    }

    @Override
    protected void load() {
        super.load();
        WidgetDevicePersistence.loadAll(widgetDevices);
    }

    @Override
    public void save() {
        super.save();
        WidgetDevicePersistence.saveAll(widgetDevices);
    }

    @Override
    public void delete(Context context) {
        super.delete(context);
        WidgetDevicePersistence.deleteAll(widgetDevices);
    }

    @Override
    public List<Facility> getReferredObj() {
        return mFacilities;
    }

    @Override
    public void initLayout() {
        super.initLayout();

        // configuration
        mRemoteViews.setOnClickPendingIntent(R.id.widget_clock_time_layout, mConfigurationPendingIntent);

        for(WidgetDevicePersistence dev : widgetDevices){
            if(!adapterId.isEmpty()){
                // detail activity
                mRemoteViews.setOnClickPendingIntent(dev.getBoundView(), startDetailActivityPendingIntent(mContext, mWidgetId + dev.getOffset(), adapterId, dev.getId()));
            }

            dev.initValueView(mRemoteViews);
        }
    }

    @Override
    protected boolean updateData() {
        int updated = 0;
        Adapter adapter = mController.getAdaptersModel().getAdapter(adapterId);
        for(WidgetDevicePersistence dev : widgetDevices) {
            Device device = mController.getFacilitiesModel().getDevice(adapterId, dev.getId());
            if (device != null) {
                dev.change(device, adapter);
                updated++;
            }
        }

        if(updated > 0) {
            // update last update to "now"
            widgetLastUpdate = getTimeNow();
            adapterId = adapter.getId();

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
    public void updateLayout() {
        // updates all inside devices
        for(WidgetDevicePersistence dev : widgetDevices){
            dev.updateValueView();
        }

        // TODO temporary solution
        onUpdateClock(mContext, mRemoteViews, new int[]{ mWidgetId });
    }


    @Override
    public void handleUserLogout() {
        super.handleUserLogout();
        // updates all inside devices
        for(WidgetDevicePersistence dev : widgetDevices){
            dev.updateValueView("%s (cached)");
        }
        updateAppWidget();
    }

    /**
     * Updates widget's time asynchroningly to sensor updates
     * Updates always on time broadcasts
     * @param context
     */
    public static void onUpdateClock(Context context, RemoteViews rv, int[] appWidgetIds) {
        // TODO have comparing to old time and skip if the same
        //lastKnownTime = cal.getTime();
        if(appWidgetIds == null || appWidgetIds.length == 0) return;

        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        Calendar cal = Calendar.getInstance(context.getResources().getConfiguration().locale);
        cal.setTime(new Date());

        for(int widgetId : appWidgetIds) {
            Log.d(TAG, String.format("onUpdateClock(%d)", widgetId));

            // tries to get widgetInitialized first
            if(rv == null){
                rv = new RemoteViews(context.getPackageName(), R.layout.widget_clock);
            }

            rv.setTextViewText(R.id.widget_clock_hours, String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)));
            rv.setTextViewText(R.id.widget_clock_minutes, String.format("%02d", cal.get(Calendar.MINUTE)));

            rv.setTextViewText(R.id.widget_clock_day_of_week, weekDays[cal.get(Calendar.DAY_OF_WEEK)]);
            rv.setTextViewText(R.id.widget_clock_date, DateTimeFormat.shortDate().print(cal.getTimeInMillis()));

            // TODO format without year http://stackoverflow.com/questions/3790918/format-date-without-year
            /*
            Log.d(TAG, "Locale: " + Locale.getDefault().toString());
            Log.d(TAG, "JODA: " + DateTimeFormat.shortDate().print(cal.getTimeInMillis()));
            SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
            Log.d(TAG, "XXX: " + sdf.toPattern());

            */

            // request widget redraw
            widgetManager.updateAppWidget(widgetId, rv);
        }
    }

    /**
     * When changed locale, change statically week day names
     * @return array of weekday names
     */
    public static String[] reloadWeekDays(){
        weekDays = new DateFormatSymbols().getShortWeekdays();
        return weekDays;
    }

    public WidgetConfiguration createConfiguration(WidgetConfigurationActivity activity, boolean isWidgetEditing){
        return new WidgetClockConfiguration(this, activity, isWidgetEditing);
    }

    @Override
    public String getClassName() {
        return WidgetClockData.class.getName();
    }
}
