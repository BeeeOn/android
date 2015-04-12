package com.rehivetech.beeeon.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.RemoteViews;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.SensorDetailActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Compatibility;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;

abstract public class WidgetProvider extends AppWidgetProvider {
    private static String TAG = WidgetProvider.class.getSimpleName();

    // according to android this should be widget cell boundaries
    public static final int WIDGET_MIN_CELLS_1 = 40;
    public static final int WIDGET_MIN_CELLS_2 = 110;
    public static final int WIDGET_MIN_CELLS_3 = 180;
    public static final int WIDGET_MIN_CELLS_4 = 250;

    @Override
    public void onEnabled(Context context){
        Log.d(TAG, "onEnabled()");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        Log.d(TAG, "onUpdate()");
        WidgetService.startUpdating(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context){
        Log.d(TAG, "onDisabled()");
        //WidgetService.stopUpdating(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // some widget is deleted
        Log.d(TAG, "onDeleted()");
        super.onDeleted(context, appWidgetIds);

        // delete widget from service
        context.startService(WidgetService.getWidgetDeleteIntent(context, appWidgetIds));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive()");
        super.onReceive(context, intent);

        // handle TouchWiz resizing
        Compatibility.handleTouchWizResizing(this, context, intent);
    }


    // or use WidgetService.getWidgetIds(WidgetLocationListProvider.class, mContext, mWidgetManager).toArray()
    protected int[] getAllIds(Context context) {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, this.getClass());
        return widgetManager.getAppWidgetIds(thisWidget);
    }
}
