package com.rehivetech.beeeon.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.SystemClock;
import android.widget.RemoteViews;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Compatibility;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.old.OldWidgetData;

import java.util.ArrayList;
import java.util.List;


abstract public class WidgetProvider extends AppWidgetProvider {
    private static String TAG = WidgetProvider.class.getSimpleName();

    // TODO wtf ty hodnoty
    public static int WIDGET_1_CELLS = 40;
    public static int WIDGET_2_CELLS = 110;
    public static int WIDGET_3_CELLS = 180;

    public boolean initialized = false;
    public boolean loggedIn = false;

    // variables available only in prepare(), changeData() and setValues() methods
    protected Context mContext;
    protected Controller mController;
    protected AppWidgetManager mWidgetManager;
    protected AppWidgetProviderInfo mWidgetProviderInfo;
    protected int mWidgetId;
    protected RemoteViews mRemoteViews;

    protected PendingIntent mConfigurationPendingIntent;
    protected PendingIntent mRefreshPendingIntent;

    @Override
    public void onEnabled(Context context){
        Log.d(TAG, "onEnabled()");

        // TODO doresit to, protoze to muze byt efektivnejsi zpusob nez service porad pollovat
        // register receiver so that we can stop service when screen is off
        /*if(!WidgetService.ScreenFilterRegistered) {
            Context c = context.getApplicationContext();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            c.registerReceiver(this, filter);
            WidgetService.ScreenFilterRegistered = true;
        }
        //*/
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        Log.d(TAG, "onUpdate()");
        WidgetService.startUpdating(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context){
        Log.d(TAG, "onDisabled()");

        /*
        if(WidgetService.ScreenFilterRegistered) {
            Context c = context.getApplicationContext();
            c.unregisterReceiver(this);
            WidgetService.ScreenFilterRegistered = false;
        }
        //*/

        //WidgetService.stopUpdating(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // some widget is deleted
        Log.d(TAG, "onDeleted()");
        super.onDeleted(context, appWidgetIds);

        // delete removed widgets settings
        for (int widgetId : appWidgetIds) {
            WidgetData widgetData = WidgetService.getWidgetData(widgetId, context);
            if(widgetData == null) continue;

            WidgetService.deleteWidgetData(widgetData);
            widgetData.deleteData(context);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive()");
        super.onReceive(context, intent);

        if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
            Log.d(TAG, "obrazovka byla vypnuta !!!");
        }
        else{
            Log.d(TAG, "obrazovka byla ZAPNUTA !!!");
        }

        // handle TouchWiz resizing
        Compatibility.handleTouchWizResizing(this, context, intent);
    }

    /*
    TODO
    // Taken from CellLayout.java
    public int[] getLauncherCellDimensions(int width, int height) {
        // Always assume we're working with the smallest span to make sure we
        // reserve enough space in both orientations.
        Resources resources = getResources();
        int cellWidth = resources.getDimensionPixelSize(R.dimen.workspace_cell_width);
        int cellHeight = resources.getDimensionPixelSize(R.dimen.workspace_cell_height);
        int widthGap = resources.getDimensionPixelSize(R.dimen.workspace_width_gap);
        int heightGap = resources.getDimensionPixelSize(R.dimen.workspace_height_gap);
        int previewCellSize = resources.getDimensionPixelSize(R.dimen.preview_cell_size);

        // This logic imitates Launcher's CellLayout.rectToCell.
        // Always round up to next largest cell
        int smallerSize = Math.min(cellWidth, cellHeight);
        int spanX = (width + smallerSize) / smallerSize;
        int spanY = (height + smallerSize) / smallerSize;

        // We use a fixed preview cell size so that you get the same preview image for
        // the same cell-sized widgets across all devices
        width = spanX * previewCellSize + ((spanX - 1) * widthGap);
        height = spanY * previewCellSize + ((spanY - 1) * heightGap);
        return new int[] { width, height };
    }
    //*/

    /**
     * Runs only ones to initialize properties + set general pendingIntents
     * @param context
     * @param data
     */
    public void initialize(Context context, WidgetData data){
        Log.d(TAG, String.format("initialize(%d)", data.getWidgetId()));

        mWidgetId = data.getWidgetId();
        mContext = context;
        mWidgetManager = AppWidgetManager.getInstance(context);
        mWidgetProviderInfo = mWidgetManager.getAppWidgetInfo(data.getWidgetId());
        mController = Controller.getInstance(context);
        mRemoteViews = new RemoteViews(context.getPackageName(), data.layout);

        // refresh onclick
        mRefreshPendingIntent = WidgetService.getForceUpdatePendingIntent(mContext, mWidgetId);

        // configuration onclick
        Intent intent = new Intent(context, WidgetConfigurationActivity.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
        mConfigurationPendingIntent = PendingIntent.getActivity(mContext, mWidgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        initialized = true;
    }

    public void whenUserLogin() {
    }

    public abstract boolean prepare();

    public abstract void changeData();

    public abstract void setValues();

    public void whenUserLogout() {
    }

    /**
     * Depending on version of system calls setRemoteAdapter
     * @param remoteViewsFactoryIntent
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void setCompatRemoteAdapter(Intent remoteViewsFactoryIntent){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mRemoteViews.setRemoteAdapter(R.id.widget_sensor_list_view, remoteViewsFactoryIntent);
        }
        else{
            mRemoteViews.setRemoteAdapter(mWidgetId, R.id.widget_sensor_list_view, remoteViewsFactoryIntent);
        }
    }

}
