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

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Tomáš on 29. 4. 2015.
 */
public class WidgetClockData extends WidgetData {
	private static final String TAG = WidgetClockData.class.getSimpleName();

	public static String weekDays[] = reloadWeekDays();
	public WidgetWeatherPersistence weather;
	private List<Facility> mFacilities = new ArrayList<>();
	private Calendar mCalendar;

	private int mClockFont = R.dimen.widget_textsize_clock;
	private int mWeatherFont = R.dimen.textsize_headline;

	private int mWeatherIconDimension = R.dimen.widget_weather_icon;
	private boolean mForceReloadWeatherIcon = true;

	/**
	 * Constructing object holding information about widget (instantiating in config activity and then in service)
	 *
	 * @param widgetId
	 * @param context
	 * @param unitsHelper
	 * @param timeHelper
	 */
	public WidgetClockData(int widgetId, Context context, UnitsHelper unitsHelper, TimeHelper timeHelper) {
		super(widgetId, context, unitsHelper, timeHelper);

		// inside devices persistence data
		widgetDevices = new ArrayList<>();
		widgetDevices.add(new WidgetDevicePersistence(mContext, mWidgetId, 0, R.id.value_container_inside_temp, unitsHelper, timeHelper, settings));
		widgetDevices.add(new WidgetDevicePersistence(mContext, mWidgetId, 1, R.id.value_container_inside_humid, unitsHelper, timeHelper, settings));

		// weather persistence data
		weather = new WidgetWeatherPersistence(mContext, mWidgetId, mUnitsHelper, mTimeHelper, settings);
	}

	// ----------------------------------------------------------- //
	// ---------------- MANIPULATING PERSISTENCE ----------------- //
	// ----------------------------------------------------------- //

	@Override
	public void load() {
		super.load();
		weather.load();
		WidgetDevicePersistence.loadAll(widgetDevices);
	}

	@Override
	public void initAdvanced(Object obj) {
		mCalendar = (Calendar) obj;
		init();
	}

	@Override
	public void init() {

		// NOTE: if in any case someone calls this instead of initAdvanced()
		if(mCalendar == null){
			mCalendar = Calendar.getInstance(mContext.getResources().getConfiguration().locale);
			mCalendar.setTime(new Date());
		}

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

		// sets icon of weather (even default)
		weather.getBitmapIcon(mForceReloadWeatherIcon, (int) mContext.getResources().getDimension(mWeatherIconDimension));
	}

	@Override
	public void save() {
		super.save();
		weather.save();
		WidgetDevicePersistence.saveAll(widgetDevices);
	}

	// ----------------------------------------------------------- //
	// ------------------------ RENDERING ------------------------ //
	// ----------------------------------------------------------- //

	@Override
	protected void renderLayout() {
		// -------------------- initialize layout
		mBuilder.setOnClickListener(R.id.widget_clock_container, mConfigurationPendingIntent);
		mBuilder.setOnClickListener(R.id.widget_clock_household_label, mRefreshPendingIntent);

		if(widgetAdapterId.isEmpty()) return;

		for(WidgetDevicePersistence dev : widgetDevices) {
			// detail activity
			mBuilder.setOnClickListener(dev.getBoundView(), startDetailActivityPendingIntent(mContext, mWidgetId + dev.getOffset(), widgetAdapterId, dev.getId()));
			dev.initView();
		}

		// -------------------- render layout
		switch (this.widgetLayout){
			case R.layout.widget_clock_3x2:
				mClockFont = R.dimen.widget_textsize_clock_large;
				mWeatherFont = R.dimen.textsize_title;
				mWeatherIconDimension = R.dimen.widget_weather_icon;
				mBuilder.setImage(R.id.widget_clock_separator_1, settings.colorSecondary);
				break;

			case R.layout.widget_clock_2x2:
				mWeatherIconDimension = R.dimen.widget_weather_icon_small;
				mBuilder.setImage(R.id.widget_clock_separator_1, settings.colorPrimary);
				mBuilder.setImage(R.id.widget_clock_separator_2, settings.colorPrimary);
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
				if(dev.containerType == dev.VALUE_UNIT) dev.getBuilder().setViewVisibility(R.id.icon, View.VISIBLE);
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
	private void renderClock(){
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
	private void renderDate(){
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

	/**
	 * Renders new weather data based on data saved in preferences
	 */
	private void renderWeather() {
		switch (this.widgetLayout){
			case R.layout.widget_clock_3x2:
				mBuilder.setTextView(
						R.id.widget_clock_weather_humidity,
						weather.getHumidity(),
						settings.colorPrimary,
						R.dimen.textsize_caption
				);

				mBuilder.setTextView(
						R.id.widget_clock_weather_pressure,
						weather.getPressure(),
						settings.colorPrimary,
						R.dimen.textsize_caption
				);

			case R.layout.widget_clock_2x2:
				mBuilder.setTextView(
						R.id.widget_clock_weather_city,
						weather.cityName,
						settings.colorPrimary,
						R.dimen.textsize_body
				);

				mBuilder.setTextView(
						R.id.widget_clock_weather_temperature,
						weather.getTemperature(),
						settings.colorSecondary,
						mWeatherFont
				);

				mBuilder.setTextViewColor(R.id.widget_clock_household_label, settings.colorPrimary);

				mBuilder.setImage(R.id.widget_weather_icon, weather.getBitmapIcon(false, (int) mContext.getResources().getDimension(mWeatherIconDimension)));
				break;
		}
	}

	// ----------------------------------------------------------- //
	// ---------------------- FAKE HANDLERS ---------------------- //
	// ----------------------------------------------------------- //

	/**
	 * Updates widget's time asynchroningly to sensor updates
	 * Updates always on time broadcasts
	 */
	public void handleClockUpdate(){
		Log.d(TAG, String.format("handleClockUpdate(%d)", mWidgetId));
		renderWidget();
	}

	@Override
	public boolean handleUpdateData() {
		int updated = 0;
		Adapter adapter = mController.getAdaptersModel().getAdapter(widgetAdapterId);
		if(adapter == null) return false;

		for(WidgetDevicePersistence dev : widgetDevices) {
			Device device = mController.getFacilitiesModel().getDevice(widgetAdapterId, dev.getId());
			if (device != null) {
				if(!dev.locationId.isEmpty()){
					Location location = mController.getLocationsModel().getLocation(widgetAdapterId, dev.locationId);
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
			widgetAdapterId = adapter.getId();

			// Save fresh data
			this.save();
			Log.v(TAG, String.format("Updating widget (%d) with fresh data", getWidgetId()));
		}

		return updated > 0;
	}

	@Override
	public void handleResize(int minWidth, int minHeight) {
		super.handleResize(minWidth, minHeight);

		int layout;
		String debugName;

		if (minWidth < 220) {
			if (minHeight < 120) {
				layout = R.layout.widget_clock_2x1;
				debugName = "2x1";
			} else {
				layout = R.layout.widget_clock_2x2;
				debugName = "2x2";
			}
		} else {
			if (minHeight < 120) {
				layout = R.layout.widget_clock_2x1;
				debugName = "2x1";
			} else {
				layout = R.layout.widget_clock_3x2;
				debugName = "3x2";
			}
		}

		Log.d(TAG, "changed to layout: " + debugName);
		changeLayout(layout);
	}

	// ----------------------------------------------------------- //
	// ------------------------- GETTERS ------------------------- //
	// ----------------------------------------------------------- //

	@Override
	public List<Object> getObjectsToReload() {
		List<Object> resultObj = new ArrayList<>();

		// first add parent objects (facilities)
		resultObj.addAll(mFacilities);

		// then from this widget
		resultObj.add(weather);

		return resultObj;
	}

	@Override
	public String getClassName() {
		return WidgetClockData.class.getName();
	}

	// -------------------------------------------------------------------- //
	// --------------------- Time & weather methods ----------------------- //
	// -------------------------------------------------------------------- //

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
}
