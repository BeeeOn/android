package com.rehivetech.beeeon.widget.receivers;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.rehivetech.beeeon.widget.data.WidgetData;
import com.rehivetech.beeeon.widget.data.WidgetLocationData;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetLocationListProvider extends WidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        // open detail activity of chosen device from list
        if (intent.getAction().equals(WidgetLocationData.OPEN_DETAIL_ACTION)) {

            int widgetId = intent.getIntExtra(WidgetLocationData.EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            String deviceId = intent.getStringExtra(WidgetLocationData.EXTRA_ITEM_DEV_ID);
            String adapterId = intent.getStringExtra(WidgetLocationData.EXTRA_ITEM_ADAPTER_ID);

            if(widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                WidgetData.startDetailActivityPendingIntent(context, widgetId, adapterId, deviceId);
            }
        }

        super.onReceive(context, intent);
    }
}