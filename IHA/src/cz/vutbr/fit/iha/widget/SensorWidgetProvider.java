package cz.vutbr.fit.iha.widget;

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
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.WidgetConfigurationActivity;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.util.Compatibility;

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
		// We don't use getting settings from Controller, because widgets are independent and doesn't depend on logged user
		return context.getSharedPreferences(String.format(Constants.WIDGET_PREF_FILENAME, widgetId), 0);
	}
	
	public void updateWidget(Context context, int widgetId, BaseDevice device) {
		// Log.d(TAG, "updateWidget()");
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

		SharedPreferences settings = getSettings(context, widgetId);

		// set layout resource from settings
		int layout = settings.getInt(Constants.WIDGET_PREF_LAYOUT, R.layout.widget_sensor);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layout);

		int icon = 0;
		String name = "";
		String value = "";
		String adapterId = "";
		//String deviceId = "";

		if (device != null) {
			// Load values from device
			icon = device.getTypeIconResource();
			name = device.getName();
			value = device.getStringValueUnit(context);
			adapterId = device.getFacility().getAdapterId();
			//deviceId = device.getId();

			// Cache these values
			Log.v(TAG, String.format("Saving widget (%d) data to cache", widgetId));

			settings
				.edit()
				.putInt(Constants.WIDGET_PREF_DEVICE_ICON, icon)
				.putString(Constants.WIDGET_PREF_DEVICE_NAME, name)
				.putString(Constants.WIDGET_PREF_DEVICE_VALUE, value)
				.putString(Constants.WIDGET_PREF_DEVICE_ADAPTER_ID, adapterId)
				.commit();
		} else {
			// Device doesn't exists -> try to load values from cache
			Log.v(TAG, String.format("Loading widget (%d) data from cache", widgetId));

			icon = settings.getInt(Constants.WIDGET_PREF_DEVICE_ICON, 0);
			name = settings.getString(Constants.WIDGET_PREF_DEVICE_NAME, context.getString(R.string.placeholder_not_exists));
			value = settings.getString(Constants.WIDGET_PREF_DEVICE_VALUE, "");
			adapterId = settings.getString(Constants.WIDGET_PREF_DEVICE_ADAPTER_ID, "");
			//deviceId = settings.getString(Constants.WIDGET_PREF_DEVICE, "");

			name += " (cached)"; // NOTE: just temporary solution until it will be showed better on widget
		}

		remoteViews.setImageViewResource(R.id.icon, icon == 0 ? R.drawable.ic_launcher : icon);
		remoteViews.setTextViewText(R.id.name, name);
		remoteViews.setTextViewText(R.id.value, value);
		
		if (layout == R.layout.widget_sensor) {
			// For classic (= not-small) layout of widget, set also lastUpdate
			String lastUpdate = settings.getString(Constants.WIDGET_PREF_DEVICE_LAST_UPDATE, "");
			remoteViews.setTextViewText(R.id.last_update, lastUpdate);
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
		// FIXME: this is waiting for Leo to allow opening SensorDetail...
		/*if (adapterId.length() > 0 && deviceId.length() > 0) {
			intent = new Intent(context, SensorDetailActivity.class);
			intent.putExtra(Constants.DEVICE_ID, deviceId);
			intent.putExtra(Constants.ADAPTER_ID, adapterId);
			pendingIntent = PendingIntent.getActivity(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.name, pendingIntent);
		}*/

		// request widget redraw
		appWidgetManager.updateAppWidget(widgetId, remoteViews);
	}

}