package cz.vutbr.fit.intelligenthomeanywhere;

import java.util.Random;

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
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.HumidityDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.PressureDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.TemperatureDevice;

public class WidgetUpdateService extends Service {

	private static final String TAG = WidgetUpdateService.class.getSimpleName();
	
	public static final String EXTRA_SINGLE_UPDATE = "ManualUpdate";	

	public static int UPDATE_FREQUENCY_SEC = 5;
	
	public static void startUpdating(Context context) { 
		final Intent service = getUpdateIntent(context);
		
		context.startService(service);

		// variant 1 - automatically repeat alarm
		// setRepeatingAlarm(context, UPDATE_FREQUENCY_SEC);
	}
	
	public static void stopUpdating(Context context) {
    	final PendingIntent service = getUpdatePendingIntent(context);
    	final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	
    	m.cancel(service);
	}
	
	public static void setAlarm(Context context, long secs, boolean repeating) {
		final PendingIntent service = getUpdatePendingIntent(context);
		final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		if (repeating) {
			m.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), secs * 1000, service);
		} else {
			m.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + secs * 1000, service);	
		}
	}

	public static Intent getUpdateIntent(Context context) {
		return new Intent(context, WidgetUpdateService.class);
	}
	
	public static PendingIntent getUpdatePendingIntent(Context context) {
		final Intent intent = getUpdateIntent(context);
		return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}
	
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
    	super.onStartCommand(intent, flags, startId);
    	
    	Log.d(TAG, "onStartCommand(), startId = " + startId);
    	
    	if (!intent.getBooleanExtra(EXTRA_SINGLE_UPDATE, false)) {
    		// variant 2 - manually repeat alarm
	    	setAlarm(this, UPDATE_FREQUENCY_SEC, false);
    	}
    	
    	// Don't update when screen is off
	    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	    if (!pm.isScreenOn()) {
	    	Log.d(TAG, "Screen is off, exiting...");
	    	stopSelf();
	        return START_NOT_STICKY;
	    }
    	
    	// Start new thread for processing
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
	
	protected void updateWidgets(Intent intent) {		
		// Get ids from intent
		int[] widgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
		if (widgetIds == null || widgetIds.length == 0) {
			// If there are no ids, get ids of all widgets
			ComponentName thisWidget = new ComponentName(this, SensorWidgetProvider.class);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
			widgetIds = appWidgetManager.getAppWidgetIds(thisWidget);	
		}
		
		Log.d(TAG, "Updating " + widgetIds.length + " widgets");
		
		for (int widgetId : widgetIds) {

			// TODO: update only widgets that need updating now
			/*long lastUpdate = ... // get from widget somehow
			if (lastUpdate + UPDATE_FREQUENCY_SEC * 1000 < SystemClock.elapsedRealtime()) {
				
			}*/
			
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
	    	
	        SensorWidgetProvider widget = new SensorWidgetProvider();
	        widget.updateWidget(this, widgetId, device);
		}
	}
	
}
