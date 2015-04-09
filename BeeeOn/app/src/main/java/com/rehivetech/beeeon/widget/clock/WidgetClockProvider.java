package com.rehivetech.beeeon.widget.clock;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.WidgetBridgeBroadcastReceiver;
import com.rehivetech.beeeon.widget.WidgetData;
import com.rehivetech.beeeon.widget.WidgetProvider;
import com.rehivetech.beeeon.widget.WidgetService;
import com.rehivetech.beeeon.widget.sensor.WidgetSensorData;
import com.rehivetech.beeeon.widget.sensor.WidgetSensorProvider;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 */
public class WidgetClockProvider extends WidgetProvider {
	private static final String TAG = WidgetClockProvider.class.getSimpleName();

	private static String weekDays[] = reloadWeekDays();

	protected WidgetClockData mWidgetData;

	private static String[] reloadWeekDays(){
		return new DateFormatSymbols().getShortWeekdays();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.d(TAG, action);

		if (action.equals(WidgetBridgeBroadcastReceiver.ACTION_TIME_CHANGED) || action.equals(WidgetBridgeBroadcastReceiver.ACTION_SCREEN_ON)) {
			onUpdateClock(context, mRemoteViews);
		}
		else if(action.equals(WidgetBridgeBroadcastReceiver.ACTION_LOCALE_CHANGED)){
			weekDays = reloadWeekDays();
		}

		super.onReceive(context, intent);
	}

	/**
	 * Updates widget's time asynchroningly to sensor updates
	 * Updates always on time broadcasts
	 * @param context
	 */
	public void onUpdateClock(Context context, RemoteViews rv) {
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
		Calendar cal = Calendar.getInstance(context.getResources().getConfiguration().locale);
		cal.setTime(new Date());

		for(int widgetId : getAllIds(context)) {
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
	public void initialize(Context context, WidgetData data) {
		super.initialize(context, data);
		mWidgetData = (WidgetClockData) data;

		// updates clock
		onUpdateClock(mContext, mRemoteViews);

		if(mWidgetData.adapterId.length() > 0 && mWidgetData.deviceId.length() > 0){
			mRemoteViews.setOnClickPendingIntent(R.id.widget_clock_temperature_in, startDetailActivityPendingIntent(mContext, mWidgetId, mWidgetData.adapterId, mWidgetData.deviceId));
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

	@Override
	public void changeData() {
		long timeNow = SystemClock.elapsedRealtime();
		Controller controller = Controller.getInstance(mContext);
		SharedPreferences userSettings = controller.getUserSettings();
		// UserSettings can be null when user is not logged in!
		UnitsHelper unitsHelper = (userSettings == null) ? null : new UnitsHelper(userSettings, mContext);
		TimeHelper timeHelper = (userSettings == null) ? null : new TimeHelper(userSettings);

		Adapter adapter = controller.getAdaptersModel().getAdapter(mWidgetData.adapterId);

		Device device = controller.getFacilitiesModel().getDevice(mWidgetData.adapterId, mWidgetData.deviceId);

		if (device != null) {
			// Get fresh data from device
			mWidgetData.deviceIcon = device.getIconResource();
			mWidgetData.deviceName = device.getName();
			mWidgetData.adapterId = device.getFacility().getAdapterId();
			mWidgetData.deviceId = device.getId();
			mWidgetData.lastUpdate = timeNow;
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
		mRemoteViews.setTextViewText(R.id.widget_clock_temperature_in, String.format("%s %s", mWidgetData.deviceValue, mWidgetData.deviceUnit));

		// TODO temporary solution
		onUpdateClock(mContext, mRemoteViews);

		// request widget redraw
		mWidgetManager.updateAppWidget(mWidgetId, mRemoteViews);
	}
}

