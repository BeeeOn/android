package cz.vutbr.fit.intelligenthomeanywhere.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import cz.vutbr.fit.intelligenthomeanywhere.Compatibility;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.activity.SensorDetailActivity;
import cz.vutbr.fit.intelligenthomeanywhere.activity.WidgetConfigurationActivity;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;

public class SensorWidgetProvider extends AppWidgetProvider {
	
	private static final String TAG = SensorWidgetProvider.class.getSimpleName();

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// new widget has been instantiated or the system booting
		Log.d(TAG, "onUpdate()");

		// start own service for periodic widget updating
		WidgetUpdateService.startUpdating(context, appWidgetIds);
	}

	@Override
    public void onDeleted(Context context, int[] appWidgetIds) {
		// some widget is deleted
		Log.d(TAG, "onDeleted()");
		super.onDeleted(context, appWidgetIds);
		
		// delete removed widgets settings
		for (int widgetId : appWidgetIds) {
			getSettings(context, widgetId).edit().clear().commit();
		}
    }

    @Override
    public void onDisabled(Context context) {
    	// last widget is deleted
    	Log.d(TAG, "onDisabled()");
    	super.onDisabled(context);

    	// stop updating service as there are no widgets anymore
    	WidgetUpdateService.stopUpdating(context);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onAppWidgetOptionsChanged(Context context,
    		AppWidgetManager appWidgetManager, int appWidgetId,
    		Bundle newOptions) {
    	// widget has changed size
    	Log.d(TAG, "onAppWidgetOptionsChanged()");
    	super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

    	int min_width = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
    	int max_width = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
    	int min_height = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
    	int max_height = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);    	

    	int layout;
    	String name;
    	if (min_width >= 110) {
    		// width of 2 or more cells
    		layout = R.layout.widget_sensor;
    		name = "widget_sensor.xml";
    	} else {
    		// width of 1 cell
    		layout = R.layout.widget_sensor_small;
    		name = "widget_sensor_small.xml";
    	}
    	
    	Log.d(TAG, "[" + min_width + "-" + max_width + "] x [" + min_height + "-" + max_height + "] -> " + name);
    	
    	// save layout resource to widget settings 
        SharedPreferences.Editor editor = getSettings(context, appWidgetId).edit();
        editor.putInt(Constants.WIDGET_PREF_LAYOUT, layout);
        editor.commit();
        
    	// force update widget
    	context.startService(WidgetUpdateService.getForceUpdateIntent(context, appWidgetId));
    }
    
	@Override
	public void onReceive(Context context, Intent intent) {
	    // handle TouchWiz resizing
		Compatibility.handleTouchWizResizing(this, context, intent);

		super.onReceive(context, intent);
	}
	
	public static SharedPreferences getSettings(Context context, int widgetId) {
		return context.getSharedPreferences(String.format(Constants.WIDGET_PREF_FILENAME, widgetId), 0);
	}
    
    public void updateWidget(Context context, int widgetId, BaseDevice device) {
    	//Log.d(TAG, "updateWidget()");
    	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        
    	// set layout resource from settings
    	int layout = getSettings(context, widgetId).getInt(Constants.WIDGET_PREF_LAYOUT, R.layout.widget_sensor);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layout);
        
        if (device != null) {
	        int icon = device.getTypeIconResource();
	        if (icon == 0) {
	        	icon = R.drawable.ic_launcher;
	        }
	        
	        remoteViews.setImageViewResource(R.id.icon, icon);
			remoteViews.setTextViewText(R.id.name, device.getName());
			remoteViews.setTextViewText(R.id.value, device.getStringValueUnit(context));
        } else {
        	// device doesn't exists - was removed or something
			remoteViews.setTextViewText(R.id.name, "NOT EXISTS"); // FIXME: use string from resources
			remoteViews.setTextViewText(R.id.value, "");
        }
		
		// register an onClickListener
		PendingIntent pendingIntent;
		Intent intent;

		// force update on click to icon
		pendingIntent = WidgetUpdateService.getForceUpdatePendingIntent(context, widgetId);
		remoteViews.setOnClickPendingIntent(R.id.icon, pendingIntent);
		
		// open configuration on click elsewhere
		intent = new Intent(context, WidgetConfigurationActivity.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		pendingIntent = PendingIntent.getActivity(context, widgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.layout, pendingIntent);
		
		// open detail activity on click
		if (device != null) {
			intent = new Intent(context, SensorDetailActivity.class);
			intent.putExtra(Constants.DEVICE_CLICKED, device.getId());
			pendingIntent = PendingIntent.getActivity(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.name, pendingIntent);
		}

		// request widget redraw
		appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }
    
}