package cz.vutbr.fit.iha.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.SensorDetailActivity;
import cz.vutbr.fit.iha.activity.WidgetConfigurationActivity;
import cz.vutbr.fit.iha.util.Compatibility;
import cz.vutbr.fit.iha.util.Log;

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
			WidgetData widgetData = new WidgetData(widgetId);
			widgetData.deleteData(context);
		}
	}

	@Override
	public void onDisabled(Context context) {
		// last widget is deleted
		Log.d(TAG, "onDisabled()");
		super.onDisabled(context);

		// stop updating service as there are no widgets anymore
		// WidgetUpdateService.stopUpdating(context);
		// NOTE: this works weird... sometimes it call's it when there are still widgets, sometimes it doesn't call it at all
		// so we just comment this out and let stopping service to calcNextUpdate method
	}

	@Override
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
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

		Log.d(TAG, String.format("[%d-%d] x [%d-%d] -> %s", min_width, max_width, min_height, max_height, name));

		// save layout resource to widget settings
		WidgetData widgetData = new WidgetData(appWidgetId);
		widgetData.saveLayout(context, layout);

		// force update widget
		context.startService(WidgetUpdateService.getForceUpdateIntent(context, appWidgetId));
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// handle TouchWiz resizing
		Compatibility.handleTouchWizResizing(this, context, intent);

		super.onReceive(context, intent);
	}

	public void updateWidget(Context context, WidgetData widgetData) {
		// Log.d(TAG, "updateWidget()");
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), widgetData.layout);
		remoteViews.setImageViewResource(R.id.icon, widgetData.deviceIcon == 0 ? R.drawable.ic_launcher : widgetData.deviceIcon);
		remoteViews.setTextViewText(R.id.name, widgetData.deviceName);
		remoteViews.setTextViewText(R.id.value, widgetData.deviceValue);

		if (widgetData.layout == R.layout.widget_sensor) {
			// For classic (= not-small) layout of widget, set also lastUpdate
			remoteViews.setTextViewText(R.id.last_update, widgetData.deviceLastUpdateText);
		}

		int widgetId = widgetData.getWidgetId();

		// register an onClickListener
		PendingIntent pendingIntent;
		Intent intent;

		// force update on click to lastUpdate
		pendingIntent = WidgetUpdateService.getForceUpdatePendingIntent(context, widgetId);
		remoteViews.setOnClickPendingIntent(R.id.value, pendingIntent);
		remoteViews.setOnClickPendingIntent(R.id.last_update, pendingIntent);

		// open configuration on click elsewhere
		intent = new Intent(context, WidgetConfigurationActivity.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		pendingIntent = PendingIntent.getActivity(context, widgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.layout, pendingIntent);

		// open detail activity on click to icon
		if (widgetData.deviceAdapterId.length() > 0 && widgetData.deviceId.length() > 0) {
			intent = new Intent(context, SensorDetailActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(SensorDetailActivity.EXTRA_DEVICE_ID, widgetData.deviceId);
			intent.putExtra(SensorDetailActivity.EXTRA_ADAPTER_ID, widgetData.deviceAdapterId);
			pendingIntent = PendingIntent.getActivity(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.icon, pendingIntent);
		}

		// request widget redraw
		appWidgetManager.updateAppWidget(widgetId, remoteViews);
	}

}