package cz.vutbr.fit.iha.widget;

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
import android.util.Log;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.controller.Controller;

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
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetId});
		
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

		for (int widgetId : widgetIds) {
			SharedPreferences settings = SensorWidgetProvider.getSettings(this, widgetId);
			int interval = settings.getInt(Constants.WIDGET_PREF_INTERVAL, UPDATE_INTERVAL_DEFAULT);
			long lastUpdate = settings.getLong(Constants.WIDGET_PREF_LAST_UPDATE, 0);
			
			// ignore uninitialized widgets
			if (!settings.getBoolean(Constants.WIDGET_PREF_INITIALIZED, false)) {
				Log.v(TAG, String.format("Ignoring widget %d (not initialized)", widgetId));
				continue;
			}
			
			// don't update widgets until their interval elapsed (or will be <1000ms from now) or we have forced update			
			if (!intent.getBooleanExtra(EXTRA_FORCE_UPDATE, false)
					&& ((lastUpdate + interval * 1000) - now > 1000)) {
				Log.v(TAG, String.format("Ignoring widget %d (not elapsed)", widgetId));
				continue;
			}
			
			Log.v(TAG, String.format("Updating widget %d", widgetId));
			
			String deviceId = settings.getString(Constants.WIDGET_PREF_DEVICE, "");
			BaseDevice device = Controller.getInstance(this).getDevice(deviceId);
	    	
			// save last update time
			settings
				.edit()
				.putLong(Constants.WIDGET_PREF_LAST_UPDATE, now)
				.commit();

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
			SharedPreferences settings = SensorWidgetProvider.getSettings(this, widgetId);
			if (!settings.getBoolean(Constants.WIDGET_PREF_INITIALIZED, false)) {
				// widget is not added yet (probably only configuration activity is showed)
				continue;
			}
			
			int widgetInterval = settings.getInt(Constants.WIDGET_PREF_INTERVAL, UPDATE_INTERVAL_DEFAULT);
			long widgetLastUpdate = settings.getLong(Constants.WIDGET_PREF_LAST_UPDATE, 0);
			long widgetNextUpdate = widgetLastUpdate > 0 ? widgetLastUpdate + widgetInterval * 1000 : now; 
			
			if (first) {
				minInterval = widgetInterval;
				nextUpdate = widgetNextUpdate;
				first = false;
			} else {
				minInterval = Math.min(minInterval, widgetInterval);
				nextUpdate = Math.min(nextUpdate, widgetNextUpdate);
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
