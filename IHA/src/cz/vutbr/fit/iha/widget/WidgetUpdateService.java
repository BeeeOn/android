package cz.vutbr.fit.iha.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.util.Timezone;

public class WidgetUpdateService extends Service {

	private static final String TAG = WidgetUpdateService.class.getSimpleName();

	private static final String EXTRA_FORCE_UPDATE = "cz.vutbr.fit.iha.forceUpdate";

	public static final int UPDATE_INTERVAL_DEFAULT = 5; // in seconds
	public static final int UPDATE_INTERVAL_MIN = 1; // in seconds

	/** Helpers for managing service updating **/

	public static void startUpdating(Context context, int[] appWidgetIds) {
		final Intent service = getUpdateIntent(context);
		service.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		context.startService(service);
	}

	public static void stopUpdating(Context context) {
		final PendingIntent service = getUpdatePendingIntent(context);
		final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		m.cancel(service);
		context.stopService(getUpdateIntent(context));
	}

	public static void setAlarm(Context context, long triggerAtMillis) {
		final PendingIntent service = getUpdatePendingIntent(context);
		final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		m.set(AlarmManager.ELAPSED_REALTIME, triggerAtMillis, service);
	}

	/** Intent factories **/

	public static Intent getUpdateIntent(Context context) {
		return new Intent(context, WidgetUpdateService.class);
	}

	public static PendingIntent getUpdatePendingIntent(Context context) {
		final Intent intent = getUpdateIntent(context);

		return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT); // or FLAG_UPDATE_CURRENT?
	}

	public static Intent getForceUpdateIntent(Context context, int widgetId) {
		Intent intent = new Intent(context, WidgetUpdateService.class);
		intent.putExtra(WidgetUpdateService.EXTRA_FORCE_UPDATE, true);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { widgetId });

		return intent;
	}

	public static PendingIntent getForceUpdatePendingIntent(Context context, int widgetId) {
		final Intent intent = getForceUpdateIntent(context, widgetId);

		return PendingIntent.getService(context, widgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT); // or FLAG_UPDATE_CURRENT?
	}

	/** Service override methods **/

	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		Log.v(TAG, String.format("onStartCommand(), startId = %d", startId));

		if (!intent.getBooleanExtra(EXTRA_FORCE_UPDATE, false)) {
			// set alarm for next update
			long nextUpdate = calcNextUpdate();
			if (nextUpdate > 0)
				setAlarm(this, nextUpdate);
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

		SensorWidgetProvider widgetProvider = new SensorWidgetProvider();
		long now = SystemClock.elapsedRealtime();

		Controller controller = Controller.getInstance(getApplicationContext());
		// NOTE: "workaround" for not logged in user
		Timezone timezone = controller.isLoggedIn() ? Timezone.fromPreferences(controller.getUserSettings()) : Timezone.getDefault();
		
		// Reload adapters to have data about Timezone offset
		controller.reloadAdapters(false);
		
		boolean forceUpdate = intent.getBooleanExtra(EXTRA_FORCE_UPDATE, false); 
		
		// TODO: reload all widgets with id IN widgetIds

		for (int widgetId : widgetIds) {
			WidgetData widgetData = new WidgetData(widgetId);
			widgetData.loadData(this);

			// ignore uninitialized widgets
			if (!widgetData.initialized) {
				Log.v(TAG, String.format("Ignoring widget %d (not initialized)", widgetId));
				continue;
			}

			// don't update widgets until their interval elapsed or we have force update
			if (!forceUpdate && !widgetData.isExpired(now)) {
				Log.v(TAG, String.format("Ignoring widget %d (not expired or forced)", widgetId));
				continue;
			}

			Log.v(TAG, String.format("Updating widget %d", widgetId));

			Adapter adapter = controller.getAdapter(widgetData.deviceAdapterId);
			BaseDevice device = controller.getDevice(widgetData.deviceAdapterId, widgetData.deviceId);

			if (device != null) {
				// NOTE: This should use absolute time, because widgets aren't updated so often
				String deviceLastUpdate = timezone.formatLastUpdate(device.getFacility().getLastUpdate(), adapter);
				
				widgetData.saveLastUpdate(this, now, deviceLastUpdate);
			}

			// update widget
			widgetProvider.updateWidget(this, widgetId, device);
		}
	}

	private long calcNextUpdate() {
		int minInterval = 0;
		long nextUpdate = 0;
		long now = SystemClock.elapsedRealtime();
		boolean first = true;

		for (int widgetId : getAllWidgetIds()) {
			WidgetData widgetData = new WidgetData(widgetId);
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

	private int[] getAllWidgetIds() {
		ComponentName thisWidget = new ComponentName(this, SensorWidgetProvider.class);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

		return appWidgetManager.getAppWidgetIds(thisWidget);
	}

}
