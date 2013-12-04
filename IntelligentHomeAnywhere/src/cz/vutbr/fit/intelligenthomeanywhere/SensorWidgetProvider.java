package cz.vutbr.fit.intelligenthomeanywhere;

import java.util.Random;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.HumidityDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.TemperatureDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.UnknownDevice;

public class SensorWidgetProvider extends AppWidgetProvider {

	private static final String TAG = SensorWidgetProvider.class.getSimpleName();

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		// Get all ids
		ComponentName thisWidget = new ComponentName(context, SensorWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		
		for (int widgetId : allWidgetIds) {
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_sensor);
			
			// TODO: use real data
			BaseDevice device = null;
			device.setName("Random sensor");
			device.setValue(new Random().nextInt(35 + 15) - 15);			

			remoteViews.setImageViewResource(R.id.icon, device.getTypeIconResource());
			remoteViews.setTextViewText(R.id.name, device.getName());
			remoteViews.setTextViewText(R.id.value, device.getStringValue()); // TODO: value with unit

			// Register an onClickListener
			Intent intent = new Intent(context, SensorWidgetProvider.class);

			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.layout, pendingIntent);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
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
    }

    @Override
    public void onEnabled(Context context) {
    	// first widget is created
    	Log.d(TAG, "onEnabled()");
        super.onEnabled(context);
    }

}
