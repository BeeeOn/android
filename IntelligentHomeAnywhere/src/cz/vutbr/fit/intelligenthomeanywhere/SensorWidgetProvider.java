package cz.vutbr.fit.intelligenthomeanywhere;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;

public class SensorWidgetProvider extends AppWidgetProvider {
	
	private static final String TAG = SensorWidgetProvider.class.getSimpleName();
	
	public static int UPDATE_FREQUENCY_SEC = 5;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(TAG, "onUpdate()");
		
		//long timeToRefresh = SystemClock.elapsedRealtime() + UPDATE_FREQUENCY_SEC * 1000;
		
		PendingIntent service = getWidgetUpdatePendingIntent(context);
		final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	    m.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, UPDATE_FREQUENCY_SEC * 1000, service);
	}

	private PendingIntent getWidgetUpdatePendingIntent(Context context) {
		final Intent i = new Intent(context, WidgetUpdateService.class);
        //	i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		return PendingIntent.getService(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
	}

	@Override
    public void onDeleted(Context context, int[] appWidgetIds) {
		// some widget is deleted	
		Log.d(TAG, "onDeleted()");
		super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
    	// last widget is deleted	
    	Log.d(TAG, "onDisabled()");    
    	super.onDisabled(context);
    	
    	PendingIntent service = getWidgetUpdatePendingIntent(context);
    	final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	m.cancel(service);
    }

    @Override
    public void onEnabled(Context context) {
    	// first widget is created
    	Log.d(TAG, "onEnabled()");
        super.onEnabled(context);
        
        //startWidgetUpdateService(context);
    }
    
    @Override
    public void onAppWidgetOptionsChanged(Context context,
    		AppWidgetManager appWidgetManager, int appWidgetId,
    		Bundle newOptions) {
    	// widget has changed size
    	//super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    	
    	// TODO: use different layout based on widget size
    } 
    
    public void updateWidget(Context context, int widgetId, BaseDevice device) {
    	//Log.d(TAG, "updateWidget()");
    	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_sensor);
        
        remoteViews.setImageViewResource(R.id.icon, device.getTypeIconResource());
		remoteViews.setTextViewText(R.id.name, device.getName());
		remoteViews.setTextViewText(R.id.value, device.getStringValueUnit(context));
		
		// Register an onClickListener
		Intent intent = new Intent(context, SensorWidgetProvider.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetId});

		/*
		// TODO: this will crash as application is not ready for this
		Intent intent = new Intent(context, SensorDetailActivity.class);
		intent.putExtra(Constants.DEVICE_CLICKED, device.getName());
		*/

		PendingIntent pendingIntent = PendingIntent.getActivity(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.layout, pendingIntent);

		appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }
    
}