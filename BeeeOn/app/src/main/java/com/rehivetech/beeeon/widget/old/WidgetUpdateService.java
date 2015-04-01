package com.rehivetech.beeeon.widget.old;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.SparseArray;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;

public class WidgetUpdateService extends Service {

	private static final String TAG = WidgetUpdateService.class.getSimpleName();

	private static final String EXTRA_FORCE_UPDATE = "com.rehivetech.beeeon.forceUpdate";

	public static final int UPDATE_INTERVAL_DEFAULT = 5; // in seconds
	public static final int UPDATE_INTERVAL_MIN = 1; // in seconds

	/** Helpers for managing service updating **/

	public static void startUpdating(Context context, int[] appWidgetIds) {
		final Intent intent = getUpdateIntent(context);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

		// Start update service
		context.startService(intent);
	}

	public static void stopUpdating(Context context) {
		// Cancel already planned alarm
		AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		m.cancel(getUpdatePendingIntent(context));

		// Stop update service
		final Intent intent = getUpdateIntent(context);
		context.stopService(intent);
	}

	private void setAlarm(long triggerAtMillis) {
		// Set new alarm time
		Context context = getApplicationContext();
		AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		m.set(AlarmManager.ELAPSED_REALTIME, triggerAtMillis, getUpdatePendingIntent(context));
	}

	/** Intent factories **/

	private static Intent getUpdateIntent(Context context) {
		return new Intent(context, WidgetUpdateService.class);
	}

	private static PendingIntent getUpdatePendingIntent(Context context) {
		final Intent intent = getUpdateIntent(context);

		return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}

	public static Intent getForceUpdateIntent(Context context, int widgetId) {
		Intent intent = new Intent(context, WidgetUpdateService.class);
		intent.putExtra(WidgetUpdateService.EXTRA_FORCE_UPDATE, true);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {
			widgetId
		});

		return intent;
	}

	public static PendingIntent getForceUpdatePendingIntent(Context context, int widgetId) {
		final Intent intent = getForceUpdateIntent(context, widgetId);

		return PendingIntent.getService(context, widgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}

	/** Service override methods **/

	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		boolean forceUpdate = intent.getBooleanExtra(EXTRA_FORCE_UPDATE, false);

		Log.v(TAG, String.format("onStartCommand(), startId = %d, forceUpdate = %b", startId, forceUpdate));

		if (!forceUpdate) {
			// set alarm for next update
			long nextUpdate = calcNextUpdate();

			if (nextUpdate > 0) {
				Log.d(TAG, String.format("Next update in %d seconds", (int) (nextUpdate - SystemClock.elapsedRealtime()) / 1000));
				setAlarm(nextUpdate);
			} else {
				Log.d(TAG, "No planned next update");
			}
		}

		// don't update when screen is off
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if (!pm.isScreenOn()) {
			Log.v(TAG, "Screen is off, exiting...");
			stopSelf();
			return START_NOT_STICKY;
		}

		// start new thread for processing
		new Thread(new Runnable() {

			@Override
			public void run() {
				updateWidgets(intent);
				stopSelf();
			}

		}).start();

		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null; // we don't use binding
	}

	private void updateWidgets(Intent intent) {
		// get ids from intent
		int[] widgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
		if (widgetIds == null || widgetIds.length == 0) {
			// if there are no ids, get ids of all widgets
			widgetIds = getAllWidgetIds();
		}

		OldSensorWidgetProvider widgetProvider = new OldSensorWidgetProvider();
		long now = SystemClock.elapsedRealtime();

		Controller controller = Controller.getInstance(this);
		SharedPreferences userSettings = controller.getUserSettings();

		// UserSettings can be null when user is not logged in!
		UnitsHelper unitsHelper = (userSettings == null) ? null : new UnitsHelper(userSettings, getApplicationContext());
		TimeHelper timeHelper = (userSettings == null) ? null : new TimeHelper(userSettings);

		// Reload adapters to have data about Timezone offset
		controller.getAdaptersModel().reloadAdapters(false);

		boolean forceUpdate = intent.getBooleanExtra(EXTRA_FORCE_UPDATE, false);

		SparseArray<OldWidgetData> widgetsToUpdate = new SparseArray<OldWidgetData>();

		// Reload facilities data
		List<Facility> facilities = new ArrayList<Facility>();
		for (int widgetId : widgetIds) {
			OldWidgetData widgetData = new OldWidgetData(widgetId);
			widgetData.loadData(this);

			// Ignore uninitialized widgets
			if (!widgetData.initialized) {
				Log.v(TAG, String.format("Ignoring widget %d (not initialized)", widgetId));
				continue;
			}

			// Don't update widgets until their interval elapsed or we have force update
			if (!forceUpdate && !widgetData.isExpired(now)) {
				Log.v(TAG, String.format("Ignoring widget %d (not expired nor forced)", widgetId));
				continue;
			}

			// Remember we're updating this widget
			widgetsToUpdate.put(widgetId, widgetData);

			// Prepare list of facilities for network request
			if (!widgetData.deviceId.isEmpty() && !widgetData.deviceAdapterId.isEmpty()) {
				String[] ids = widgetData.deviceId.split(Device.ID_SEPARATOR, 2);

				Facility facility = new Facility();
				facility.setAdapterId(widgetData.deviceAdapterId);
				facility.setAddress(ids[0]);
				facility.setLastUpdate(new DateTime(widgetData.deviceLastUpdateTime, DateTimeZone.UTC));
				facility.setRefresh(RefreshInterval.fromInterval(widgetData.deviceRefresh));

				facility.addDevice(Device.createFromDeviceTypeId(ids[1]));

				facilities.add(facility);
			}
		}

		if (!facilities.isEmpty()) {
			try {
				controller.getFacilitiesModel().refreshFacilities(facilities, forceUpdate);
			} catch (AppException e) {
				e.printStackTrace(); // Nothing to do here
			}
		}

		for (int i = 0; i < widgetsToUpdate.size(); i++) {
			OldWidgetData widgetData = widgetsToUpdate.valueAt(i);
			int widgetId = widgetData.getWidgetId();

			Adapter adapter = controller.getAdaptersModel().getAdapter(widgetData.deviceAdapterId);
			Device device = controller.getFacilitiesModel().getDevice(widgetData.deviceAdapterId, widgetData.deviceId);

			if (device != null) {
				// Get fresh data from device
				widgetData.deviceIcon = device.getIconResource();
				widgetData.deviceName = device.getName();
				widgetData.deviceAdapterId = device.getFacility().getAdapterId();
				widgetData.deviceId = device.getId();
				widgetData.lastUpdate = now;
				widgetData.deviceLastUpdateTime = device.getFacility().getLastUpdate().getMillis();
				widgetData.deviceRefresh = device.getFacility().getRefresh().getInterval();

				// Check if we can format device's value (unitsHelper is null when user is not logged in)
				if (unitsHelper != null) {
					widgetData.deviceValue = unitsHelper.getStringValue(device.getValue());
					widgetData.deviceUnit = unitsHelper.getStringUnit(device.getValue());
				}

				// Check if we can format device's last update (timeHelper is null when user is not logged in)
				if (timeHelper != null) {
					// NOTE: This should use always absolute time, because widgets aren't updated so often
					widgetData.deviceLastUpdateText = timeHelper.formatLastUpdate(device.getFacility().getLastUpdate(), adapter);
				}

				// Save fresh data
				widgetData.saveData(getApplicationContext());

				Log.v(TAG, String.format("Updating widget (%d) with fresh data", widgetId));
			} else {
				// NOTE: just temporary solution until it will be showed better on widget
				widgetData.deviceLastUpdateText = String.format("%s %s", widgetData.deviceLastUpdateText, getString(R.string.widget_cached));

				Log.v(TAG, String.format("Updating widget (%d) with cached data", widgetId));
			}

			// Update widget
			widgetProvider.updateWidget(this, widgetData);
		}
	}

	private long calcNextUpdate() {
		int minInterval = 0;
		long nextUpdate = 0;
		long now = SystemClock.elapsedRealtime();
		boolean first = true;

		for (int widgetId : getAllWidgetIds()) {
			OldWidgetData widgetData = new OldWidgetData(widgetId);
			widgetData.loadData(this);

			if (!widgetData.initialized) {
				// widget is not added yet (probably only configuration activity is showed)
				continue;
			}

			if (first) {
				minInterval = widgetData.interval;
				nextUpdate = widgetData.getNextUpdate(now);
				first = false;
			} else {
				minInterval = Math.min(minInterval, widgetData.interval);
				nextUpdate = Math.min(nextUpdate, widgetData.getNextUpdate(now));
			}
		}

		minInterval = Math.max(minInterval, UPDATE_INTERVAL_MIN);
		return first ? 0 : Math.max(nextUpdate, SystemClock.elapsedRealtime() + minInterval * 1000);
	}

	private List<Integer> getWidgetIds(Class<?> cls) {
		ComponentName thisWidget = new ComponentName(this, cls);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

		List<Integer> arr = new ArrayList<Integer>();
		for (int i : appWidgetManager.getAppWidgetIds(thisWidget)) {
			arr.add(i);
		}
		return arr;
	}

	private int[] getAllWidgetIds() {
		List<Integer> ids = new ArrayList<Integer>();
		ids.addAll(getWidgetIds(OldSensorWidgetProviderSmall.class));
		ids.addAll(getWidgetIds(OldSensorWidgetProviderMedium.class));
		ids.addAll(getWidgetIds(OldSensorWidgetProviderLarge.class));

		int[] arr = new int[ids.size()];
		for (int i = 0; i < ids.size(); i++) {
			arr[i] = ids.get(i);
		}

		return arr;
	}

}
