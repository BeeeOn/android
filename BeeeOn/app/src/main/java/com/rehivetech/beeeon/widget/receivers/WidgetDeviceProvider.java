package com.rehivetech.beeeon.widget.receivers;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.widget.service.WidgetService;

/**
 * @author mlyko
 */
public class WidgetDeviceProvider extends WidgetProvider{
    private static final String TAG = WidgetDeviceProvider.class.getSimpleName();

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

        int cellsWidth = howManyCells(min_width);
        int cellsHeight = howManyCells(min_height);

        /*
        int count_width;
        int count_height;

        String x;

        pole.put(0, 11);
        pole.put(1, 12);
        pole.put(2, 13);

        pole.put(5, 21);

        if(cellsWidth == 1 && cellsHeight == 1)
            x = "1_1";
        else if(cellsWidth == 2 && cellsHeight == 1)
            x = "2_1";


        int w = 1, h = 1;
        for(; w <= 4; w++){
            for(; h <= 4; h++){
                if(cellsHeight == h){
                    if(existLayout(w, h) > 0){
                        break;
                    }
                }
            }

            if(cellsWidth == w) break;
        }

//*/


        // 1 cell
        if(min_width < WIDGET_MIN_CELLS_2){
            layout = R.layout.widget_device_1x1;
            name = "widget_device_1x1.xml";
        }
        // 2 cells
        else if(min_width >= WIDGET_MIN_CELLS_2 && min_width < WIDGET_MIN_CELLS_3){
            layout = R.layout.widget_device_2x1;
            name = "widget_sensor_2x1.xml";
        }
        // 3 cells
        else{
            layout = R.layout.widget_device_3x1;
            name = "widget_sensor_3x1.xml";
        }

        Log.d(TAG, String.format("[%d-%d] x [%d-%d] -> %s", min_width, max_width, min_height, max_height, name));

        // service changes layout
        context.startService(WidgetService.getIntentWidgetChangeLayout(context, appWidgetId, layout));
    }

}
