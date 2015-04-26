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
import com.rehivetech.beeeon.widget.ViewsBuilder;
import com.rehivetech.beeeon.widget.persistence.WidgetDevicePersistence;
import com.rehivetech.beeeon.widget.persistence.WidgetWeatherPersistence;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Class for clock widget
 */
public class WidgetClockData extends WidgetData {
    private static final String TAG = WidgetClockData.class.getSimpleName();

    public static String weekDays[] = reloadWeekDays();

    private List<Facility> mFacilities;

    public WidgetWeatherPersistence weather;
    private Calendar mCalendar;

    boolean weatherCheckedThisHour = false; // TODO

    public WidgetClockData(int widgetId, Context context, UnitsHelper unitsHelper, TimeHelper timeHelper){
        super(widgetId, context, unitsHelper, timeHelper);

        // inside devices
        widgetDevices = new ArrayList<>();
        widgetDevices.add(new WidgetDevicePersistence(mContext, mWidgetId, 0, R.id.value_container_inside_temp, unitsHelper, timeHelper, settings));
        widgetDevices.add(new WidgetDevicePersistence(mContext, mWidgetId, 1, R.id.value_container_inside_humid, unitsHelper, timeHelper, settings));

        mFacilities = new ArrayList<>();

        weather = new WidgetWeatherPersistence(mContext, mWidgetId, mUnitsHelper, mTimeHelper, settings);
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
            facility.setAdapterId(adapterId);
            facility.setAddress(ids[0]);
            facility.setLastUpdate(new DateTime(dev.lastUpdateTime, DateTimeZone.UTC));
            facility.setRefresh(RefreshInterval.fromInterval(dev.refresh));
            facility.addDevice(Device.createFromDeviceTypeId(ids[1]));

            mFacilities.add(facility);
        }
    }

    @Override
    public void load() {
        super.load();
        weather.load();
        WidgetDevicePersistence.loadAll(widgetDevices);
    }

    @Override
    public void save() {
        super.save();
        weather.save();
        WidgetDevicePersistence.saveAll(widgetDevices);
    }

    @Override
    public List<Facility> getReferredObj() {
        return mFacilities;
    }

    @Override
    public void initLayout() {
        super.initLayout();

        // configuration
        mBuilder.setOnClickListener(R.id.clock_container, mConfigurationPendingIntent);

        mBuilder.setTextViewColor(R.id.widget_clock_household_label, settings.colorPrimary);
        mBuilder.setTextViewColor(R.id.widget_clock_day_of_week, settings.colorSecondary);
        mBuilder.setTextViewColor(R.id.widget_clock_date, settings.colorSecondary);

        for(WidgetDevicePersistence dev : widgetDevices){
            if(!adapterId.isEmpty()){
                // detail activity
                mBuilder.setOnClickListener(dev.getBoundView(), startDetailActivityPendingIntent(mContext, mWidgetId + dev.getOffset(), adapterId, dev.getId()));
            }

            dev.initView();
        }
    }

    @Override
    protected boolean updateData() {
        int updated = 0;
        Adapter adapter = mController.getAdaptersModel().getAdapter(adapterId);
        for(WidgetDevicePersistence dev : widgetDevices) {
            Device device = mController.getFacilitiesModel().getDevice(adapterId, dev.getId());
            if (device != null) {
                dev.configure(device, adapter);
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
    public void renderLayout() {
        // updates all inside devices
        for(WidgetDevicePersistence dev : widgetDevices){
            dev.renderView(mBuilder);
        }

        // TODO temporary solution
        onUpdateClock(mCalendar);
    }

    @Override
    public void handleUserLogout() {
        super.handleUserLogout();
        // updates all inside devices
        for(WidgetDevicePersistence dev : widgetDevices){
            dev.renderView(mBuilder, true, "%s " + mContext.getString(R.string.widget_cached));
        }
        renderAppWidget();
    }

    // -------------------------------------------------------------------- //
    // ---------------------------- Clock methods ------------------------- //
    // -------------------------------------------------------------------- //

    /**
     * Updates widget's time asynchroningly to sensor updates
     * Updates always on time broadcasts
     * @param cal Calendar holding fresh time
     */
    public void onUpdateClock(Calendar cal){
        Log.d(TAG, String.format("onUpdateClock(%d)", mWidgetId));

        // NOTE: we need to be sure, that time will always update, so we add here in case that reference is lost
        if(cal == null){
            cal = Calendar.getInstance(mContext.getResources().getConfiguration().locale);
            cal.setTime(new Date());
        }

        ViewsBuilder clockBuilder = new ViewsBuilder(mContext, R.layout.widget_include_clock);
        // clear old sub views
        mBuilder.removeAllViews(R.id.clock_container);
        mBuilder.addView(R.id.clock_container, clockBuilder.getRoot());

        int hours_format = is24HourMode(mContext) ? Calendar.HOUR_OF_DAY : Calendar.HOUR;
        clockBuilder.setTextViewText(R.id.widget_clock_hours, String.format("%02d", cal.get(hours_format)));
        clockBuilder.setTextViewColor(R.id.widget_clock_hours, settings.colorPrimary);

        // shows minutes
        clockBuilder.setTextViewText(R.id.widget_clock_minutes, String.format("%02d", cal.get(Calendar.MINUTE)));
        clockBuilder.setTextViewColor(R.id.widget_clock_minutes, settings.colorPrimary);

        mBuilder.setTextViewText(R.id.widget_clock_day_of_week, weekDays[cal.get(Calendar.DAY_OF_WEEK)]);
        clockBuilder.setTextViewColor(R.id.widget_clock_day_of_week, settings.colorSecondary);

        mBuilder.setTextViewText(R.id.widget_clock_date, DateTimeFormat.shortDate().print(cal.getTimeInMillis()));
        clockBuilder.setTextViewColor(R.id.widget_clock_date, settings.colorSecondary);

        clockBuilder.setTextViewColor(R.id.widget_clock_doubledots, settings.colorPrimary);

        renderAppWidget();

        // TODO format without year http://stackoverflow.com/questions/3790918/format-date-without-year
    }

    /**
     * Checs if user uses 24 hour format or not
     * @param context
     * @return
     */
    private static boolean is24HourMode(final Context context) {
        return android.text.format.DateFormat.is24HourFormat(context);
    }

    /**
     * When changed locale, change statically week day names
     * @return array of weekday names
     */
    public static String[] reloadWeekDays(){
        weekDays = new DateFormatSymbols().getShortWeekdays();
        return weekDays;
    }

    // -------------------------------------------------------------------- //
    // ---------------------------- Weather methods ----------------------- //
    // -------------------------------------------------------------------- //

    /**
     * Updates and stores date from the server
     * @param json
     */
    public void updateWeather(JSONObject json){
        weather.configure(json, null);
    }

    /**
     * Renders new weather data based on data saved in preferences
     */
    public void renderWeather() {
        Log.v(TAG, "rendering weather widget !!!!!");

        /*
        detailsField.setText(
                details.getString("description").toUpperCase(Locale.US) +
                        "\n" + "Humidity: " + main.getString("humidity") + "%" +
                        "\n" + "Pressure: " + main.getString("pressure") + " hPa");
       //*/
        //updatedField.setText("Last update: " + updatedOn);


        weather.renderView(mBuilder);

        renderAppWidget();
    }

    @Override
    public String getClassName() {
        return WidgetClockData.class.getName();
    }

    public void injectObject(Calendar calendar) {
        mCalendar = calendar;
    }
}
