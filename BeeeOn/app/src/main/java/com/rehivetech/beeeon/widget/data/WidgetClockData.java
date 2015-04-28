package com.rehivetech.beeeon.widget.data;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.location.Location;
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

    private int mClockFont = R.dimen.widget_textsize_clock;
    private int mWeatherFont = R.dimen.textsize_headline;

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
        mBuilder.setOnClickListener(R.id.widget_clock_container, mConfigurationPendingIntent);

        mBuilder.setOnClickListener(R.id.widget_clock_household_label, mRefreshPendingIntent);

        if(!adapterId.isEmpty()){
            for(WidgetDevicePersistence dev : widgetDevices) {
                // detail activity
                mBuilder.setOnClickListener(dev.getBoundView(), startDetailActivityPendingIntent(mContext, mWidgetId + dev.getOffset(), adapterId, dev.getId()));

                dev.initView();
            }
        }
    }

    @Override
    protected boolean updateData() {
        int updated = 0;
        Adapter adapter = mController.getAdaptersModel().getAdapter(adapterId);
        for(WidgetDevicePersistence dev : widgetDevices) {
            Device device = mController.getFacilitiesModel().getDevice(adapterId, dev.getId());
            if (device != null) {
                if(!dev.locationId.isEmpty()){
                    Location location = mController.getLocationsModel().getLocation(adapterId, dev.locationId);
                    dev.configure(device, adapter, location);
                }
                else {
                    dev.configure(device, adapter);
                }
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
        // NOTE: we need to be sure, that time will always update, so we add here in case that reference is lost
        if(mCalendar == null){
            mCalendar = Calendar.getInstance(mContext.getResources().getConfiguration().locale);
            mCalendar.setTime(new Date());
        }

        // TODO takto?
        if(mBuilder.getRoot() == null){
            mBuilder.loadRootView(this.widgetLayout);
        }

        switch (this.widgetLayout){
            case R.layout.widget_clock_3x2:
                mClockFont = R.dimen.widget_textsize_clock_large;
                mWeatherFont = R.dimen.textsize_headline;
                break;

            case R.layout.widget_clock_2x2:
                mBuilder.setImage(R.id.widget_clock_separator_1, settings.colorPrimary);
                mBuilder.setImage(R.id.widget_clock_separator_2, settings.colorPrimary);
                mBuilder.setImage(R.id.widget_clock_separator_3, settings.colorPrimary);
            default:
                mClockFont = R.dimen.widget_textsize_clock;
                mWeatherFont = R.dimen.textsize_body;
                break;
        }

        // updates all inside devices
        for(WidgetDevicePersistence dev : widgetDevices){
            dev.renderView(mBuilder);
            dev.setValueUnitColor(settings.colorSecondary);

            if(this.widgetLayout == R.layout.widget_clock_3x2) {
                dev.getBuilder().setViewVisibility(R.id.icon, View.VISIBLE); // TODO check if the layout is shown
            }
            else if(this.widgetLayout == R.layout.widget_clock_2x2){
                dev.setValueUnitSize(R.dimen.textsize_caption);
            }
        }

        renderClock();
        renderDate();
        renderWeather();

        // TODO format without year http://stackoverflow.com/questions/3790918/format-date-without-year
    }

    /**
     * Renders digital clock and sets its color and font size
     */
    public void renderClock(){
        ViewsBuilder clockBuilder = new ViewsBuilder(mContext, R.layout.widget_include_clock);

        boolean is24hMode = is24HourMode(mContext);

        // set hours
        clockBuilder.setTextView(
                R.id.widget_clock_hours,
                String.format("%02d", mCalendar.get(is24hMode ? Calendar.HOUR_OF_DAY : Calendar.HOUR)),
                settings.colorPrimary,
                mClockFont
        );

        // set minutes
        clockBuilder.setTextView(
                R.id.widget_clock_minutes,
                String.format("%02d", mCalendar.get(Calendar.MINUTE)),
                settings.colorPrimary,
                mClockFont
        );

        // show pm / am
        if(is24hMode){
            clockBuilder.setViewVisibility(R.id.widget_clock_ampm, View.GONE);
        }
        else {
            clockBuilder.setViewVisibility(R.id.widget_clock_ampm, View.VISIBLE);
            clockBuilder.setTextViewText(R.id.widget_clock_ampm, DateFormat.format("aa", mCalendar.getTime()).toString());
            clockBuilder.setTextViewColor(R.id.widget_clock_ampm, settings.colorPrimary);
        }

        // double dots
        clockBuilder.setTextViewColor(R.id.widget_clock_doubledots, settings.colorPrimary);
        clockBuilder.setTextViewTextSize(R.id.widget_clock_doubledots, mClockFont);

        // clear old sub views
        mBuilder.removeAllViews(R.id.widget_clock_container);
        mBuilder.addView(R.id.widget_clock_container, clockBuilder.getRoot());
    }

    /**
     * Renders date with color
     */
    public void renderDate(){
        ViewsBuilder builder = new ViewsBuilder(mContext, R.layout.widget_include_date);

        // set day of week
        builder.setTextView(
                R.id.day_of_week,
                weekDays[mCalendar.get(Calendar.DAY_OF_WEEK)],
                settings.colorSecondary,
                R.dimen.textsize_body
        );

        // set date
        builder.setTextView(
                R.id.date,
                DateTimeFormat.shortDate().print(mCalendar.getTimeInMillis()),
                settings.colorSecondary,
                R.dimen.textsize_body
        );

        // clear old sub views
        mBuilder.removeAllViews(R.id.widget_date_container);
        mBuilder.addView(R.id.widget_date_container, builder.getRoot());
    }

    @Override
    public void handleUserLogout() {
        super.handleUserLogout();
        // updates all inside devices
        for(WidgetDevicePersistence dev : widgetDevices){
            dev.renderView(mBuilder, true, mContext.getString(R.string.widget_cached));
        }

        renderWidget();
    }

    @Override
    public void handleResize(int minWidth, int minHeight) {
        super.handleResize(minWidth, minHeight);

        int layout;
        String debugName;

        if(minWidth < 220){
            if(minHeight < 140){
                layout = R.layout.widget_clock_2x1;
                debugName = "2x1";
            }
            else{
                layout = R.layout.widget_clock_2x2;
                debugName = "2x2";
            }
        }
        else{
            if(minHeight < 140){
                layout = R.layout.widget_clock_2x1;
                debugName = "2x1";
            }
            else {
                layout = R.layout.widget_clock_3x2;
                debugName = "3x2";
            }
        }

        Log.d(TAG, "changed to layout: " + debugName);
        changeLayout(layout);
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

        mCalendar = cal;
        renderWidget();
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
        Log.v(TAG, "updating weather widget !!!!!");

        weather.configure(json, null);
    }

    /**
     * Renders new weather data based on data saved in preferences
     */
    public void renderWeather() {
        /*
        detailsField.setText(
                details.getString("description").toUpperCase(Locale.US) +
                        "\n" + "Humidity: " + main.getString("humidity") + "%" +
                        "\n" + "Pressure: " + main.getString("pressure") + " hPa");
       //*/
        //updatedField.setText("Last update: " + updatedOn);


        //weather.renderView(mBuilder);


        switch (this.widgetLayout){
            case R.layout.widget_clock_2x2:
            case R.layout.widget_clock_3x2:

                mBuilder.setTextView(
                        R.id.widget_clock_weather_city,
                        weather.cityName,
                        settings.colorPrimary,
                        R.dimen.textsize_body
                );

                mBuilder.setTextView(
                        R.id.widget_clock_weather_temperature,
                        weather.temperature,
                        settings.colorSecondary,
                        mWeatherFont
                );

                mBuilder.setTextViewColor(R.id.widget_clock_household_label, settings.colorPrimary);

                mBuilder.setImage(R.id.widget_weather_icon, weather.getBitmapIcon(false));
                break;
        }
    }

    @Override
    public String getClassName() {
        return WidgetClockData.class.getName();
    }

    public void injectObject(Calendar calendar) {
        mCalendar = calendar;
    }
}
