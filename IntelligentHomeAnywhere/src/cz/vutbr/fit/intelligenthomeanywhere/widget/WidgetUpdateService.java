package cz.vutbr.fit.intelligenthomeanywhere.widget;

import java.util.Random;

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
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.HumidityDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.PressureDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.TemperatureDevice;

public class WidgetUpdateService extends Service {

	private static final String TAG = WidgetUpdateService.class.getSimpleName();

	private static final String EXTRA_FORCE_UPDATE = "cz.vutbr.fit.intelligenthomeanywhere.forceUpdate";
	
	private static final int UPDATE_INTERVAL_DEFAULT = 5; // in seconds
	private static final int UPDATE_INTERVAL_MIN = 1; // in seconds
	

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
    	
    	Log.d(TAG, "onStartCommand(), startId = " + startId);
    	
    	if (!intent.getBooleanExtra(EXTRA_FORCE_UPDATE, false)) {
    		// set alarm for next update
	    	setAlarm(this, calcNextUpdate());
    	}
    	
    	// don't update when screen is off
	    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	    if (!pm.isScreenOn()) {
	    	Log.d(TAG, "Screen is off, exiting...");
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
		
		Log.d(TAG, "Updating " + widgetIds.length + " widgets");
		
		SensorWidgetProvider widgetProvider = new SensorWidgetProvider();
		long now = SystemClock.elapsedRealtime();

		for (int widgetId : widgetIds) {
			// don't update widgets until their interval elapsed (or will be <1000ms from now) or we have forced update			
			if (!intent.getBooleanExtra(EXTRA_FORCE_UPDATE, false)
					&& (getNextWidgetUpdate(widgetId) - now) > 1000)
				continue;
			
			// TODO: get correct widget data, not random
			BaseDevice device = null;
			switch (new Random().nextInt(3)) {
			case 0:				
				device = new TemperatureDevice();
				device.setName("Teplota v kuchyni");
				device.setValue(new Random().nextInt(35 + 15) - 15);
				break;
			case 1:				
				device = new HumidityDevice();
				device.setName("Vlhkost v koupelně");
				device.setValue(new Random().nextInt(50) + 50);
				break;
			case 2:
				device = new PressureDevice();
				device.setName("Tlak venku");
				device.setValue(new Random().nextInt(300) + 699);
				break;
			}
	    	
			// save last update time
			SensorWidgetProvider.getSettings(this, widgetId)
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

		int[] allWidgetIds = getAllWidgetIds();
		for (int i = 0; i < allWidgetIds.length; i++) {
			int widgetId = allWidgetIds[i];
			int widgetInterval = SensorWidgetProvider.getSettings(this, widgetId).getInt(Constants.WIDGET_PREF_INTERVAL, UPDATE_INTERVAL_DEFAULT);
			long widgetNextUpdate = getNextWidgetUpdate(widgetId);
			
			if (i == 0) {
				minInterval = widgetInterval;
				nextUpdate = widgetNextUpdate;
			} else {
				minInterval = Math.min(minInterval, widgetInterval);
				nextUpdate = Math.min(nextUpdate, widgetNextUpdate);
			}			
		}
		
		minInterval = Math.max(minInterval, UPDATE_INTERVAL_MIN);
		return Math.max(nextUpdate, SystemClock.elapsedRealtime() + minInterval * 1000);
	}
	
	private int[] getAllWidgetIds() {
		ComponentName thisWidget = new ComponentName(this, SensorWidgetProvider.class);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

		return appWidgetManager.getAppWidgetIds(thisWidget);	
	}
	
	private long getNextWidgetUpdate(int widgetId) {
		SharedPreferences settings = SensorWidgetProvider.getSettings(this, widgetId);
		int interval = settings.getInt(Constants.WIDGET_PREF_INTERVAL, UPDATE_INTERVAL_DEFAULT);
		long lastUpdate = settings.getLong(Constants.WIDGET_PREF_LAST_UPDATE, 0);

		return lastUpdate + interval * 1000;
	}
	
}
