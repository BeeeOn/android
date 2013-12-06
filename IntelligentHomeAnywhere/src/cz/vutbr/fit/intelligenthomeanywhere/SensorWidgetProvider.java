package cz.vutbr.fit.intelligenthomeanywhere;

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

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(TAG, "onUpdate()");

		WidgetUpdateService.startUpdating(context);
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

    	WidgetUpdateService.stopUpdating(context);
    }

    @Override
    public void onEnabled(Context context) {
    	// first widget is created
    	Log.d(TAG, "onEnabled()");
        super.onEnabled(context);
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
		Intent intent = new Intent(context, WidgetUpdateService.class);
		intent.putExtra(WidgetUpdateService.EXTRA_SINGLE_UPDATE, true);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetId});
		PendingIntent pendingIntent = PendingIntent.getService(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		/*
		// TODO: this will crash as application is not ready for this
		Intent intent = new Intent(context, SensorDetailActivity.class);
		intent.putExtra(Constants.DEVICE_CLICKED, device.getName());
		PendingIntent pendingIntent = PendingIntent.getActivity(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		*/
		
		remoteViews.setOnClickPendingIntent(R.id.layout, pendingIntent);

		appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }
    
}