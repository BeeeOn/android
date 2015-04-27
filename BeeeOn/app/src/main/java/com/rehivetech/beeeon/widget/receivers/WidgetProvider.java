package com.rehivetech.beeeon.widget.receivers;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;

import com.rehivetech.beeeon.util.Compatibility;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.widget.service.WidgetService;

abstract public class WidgetProvider extends AppWidgetProvider {
    private static String TAG = WidgetProvider.class.getSimpleName();

    // according to android this should be widget cell boundaries (this makes 70dp for cell)
    public static final int WIDGET_MIN_CELLS_1 = 40;
    public static final int WIDGET_MIN_CELLS_2 = 110;
    public static final int WIDGET_MIN_CELLS_3 = 180;
    public static final int WIDGET_MIN_CELLS_4 = 250;

    public static final int cell_1_1 = 0;
    public static final int cell_1_2 = 1;
    public static final int cell_1_3 = 2;
    public static final int cell_1_4 = 3;

    SparseArray<Integer> pole = new SparseArray<>(16);


    @Override
    public void onEnabled(Context context){
        Log.d(TAG, "onEnabled()");
        WidgetService.startUpdating(context, new int[] {});
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        Log.d(TAG, "onUpdate()");
        WidgetService.startUpdating(context, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // some widget is deleted
        Log.d(TAG, "onDeleted()");
        super.onDeleted(context, appWidgetIds);

        // delete widget from service
        context.startService(WidgetService.getIntentWidgetDelete(context, appWidgetIds));
    }

    @Override
    public void onDisabled(Context context){
        Log.d(TAG, "onDisabled()");
        //WidgetService.stopUpdating(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, String.format("onReceive(%s)", action));
        super.onReceive(context, intent);

        // handle TouchWiz resizing
        Compatibility.handleTouchWizResizing(this, context, intent);
    }

    public int existLayout(int w, int h){
        return 1;
    }

    public int howManyCells(int size) {
        if (size < WIDGET_MIN_CELLS_2) {
            return 1;
        } else if (size >= WIDGET_MIN_CELLS_2 && size < WIDGET_MIN_CELLS_3) {
            return 2;
        }
        else if(size >= WIDGET_MIN_CELLS_3 && size < WIDGET_MIN_CELLS_4){
            return 3;
        }
        else {
            return 4;
        }
    }

    /**
     * For getting all widget by provider
     * @param context
     * @return
     */
    public static int[] getAllIdsByClass(Context context, Class<?> cls){
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, cls);
        return widgetManager.getAppWidgetIds(thisWidget);
    }

    /**
     * Short version to call from subclass of widgetProvider
     * @param context
     * @return
     */
    protected int[] getAllIds(Context context) {
        return getAllIdsByClass(context, this.getClass());
    }
}
