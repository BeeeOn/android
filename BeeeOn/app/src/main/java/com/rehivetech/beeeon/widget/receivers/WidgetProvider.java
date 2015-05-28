package com.rehivetech.beeeon.widget.receivers;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.rehivetech.beeeon.util.Compatibility;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.widget.service.WidgetService;

abstract public class WidgetProvider extends AppWidgetProvider {
	private static String TAG = WidgetProvider.class.getSimpleName();


	@Override
	public void onEnabled(Context context) {
		WidgetService.startUpdating(context, new int[]{});
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		WidgetService.startUpdating(context, appWidgetIds);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		Log.d(TAG, "onDeleted()");

		// delete widget from service
		context.startService(WidgetService.getIntentWidgetDelete(context, appWidgetIds));
	}

	@Override
	public void onDisabled(Context context) {
		Log.d(TAG, "onDisabled()");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		// handle TouchWiz resizing
		Compatibility.handleTouchWizResizing(this, context, intent);
	}

	@Override
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

		int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
		int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

		// service changes layout
		context.startService(WidgetService.getIntentWidgetChangeLayout(context, appWidgetId, minWidth, minHeight));
	}

	/**
	 * For getting all widget by provider
	 *
	 * @param context
	 * @return
	 */
	public static int[] getWidgetIdsByClass(Context context, Class<?> cls) {
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
		ComponentName thisWidget = new ComponentName(context, cls);
		return widgetManager.getAppWidgetIds(thisWidget);
	}
}
