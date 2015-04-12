package com.rehivetech.beeeon.widget.sensor;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.widget.WidgetProvider;
import com.rehivetech.beeeon.widget.WidgetService;

/**
 * @author mlyko
 */
public class WidgetSensorProvider extends WidgetProvider{
    private static final String TAG = WidgetSensorProvider.class.getSimpleName();

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Log.d(TAG, "onAppWidgetOptionsChanged()");
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        int min_width = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int max_width = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int min_height = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        int max_height = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);

        int layout;
        String name;

        if(min_width < WIDGET_MIN_CELLS_2){
            layout = R.layout.widget_sensor_1x1;
            name = "widget_sensor_1x1.xml";
        }
        else if(min_width >= WIDGET_MIN_CELLS_2 && min_width < WIDGET_MIN_CELLS_3){
            layout = R.layout.widget_sensor_2x1;
            name = "widget_sensor_2x1.xml";
        }
        else{
            layout = R.layout.widget_sensor_3x1;
            name = "widget_sensor_3x1.xml";
        }

        Log.d(TAG, String.format("[%d-%d] x [%d-%d] -> %s", min_width, max_width, min_height, max_height, name));

        // service changes layout
        context.startService(WidgetService.getWidgetChangeLayoutIntent(context, appWidgetId, layout));
    }

}
