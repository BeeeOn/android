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
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.PressureDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.TemperatureDevice;

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

			remoteViews.setImageViewResource(R.id.icon, device.getTypeIconResource());
			remoteViews.setTextViewText(R.id.name, device.getName());
			remoteViews.setTextViewText(R.id.value, device.getStringValueUnit(context));
			
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
