package com.rehivetech.beeeon.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.SparseArray;

import com.rehivetech.beeeon.adapter.device.Facility;
import com.rehivetech.beeeon.adapter.location.Location;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.widget.clock.WidgetClockProvider;
import com.rehivetech.beeeon.widget.location.WidgetLocationListProvider;
import com.rehivetech.beeeon.widget.sensor.WidgetSensorProvider;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mlyko
 */
public class WidgetService extends Service {
    private final static String TAG = WidgetService.class.getSimpleName();

    private static final String EXTRA_FORCE_UPDATE = "com.rehivetech.beeeon.forceUpdate";
    private static final String EXTRA_STANDBY = "com.rehivetech.beeeon.standby";

    public static final int UPDATE_INTERVAL_DEFAULT = 10; // in seconds
    public static final int UPDATE_INTERVAL_MIN = 5; // in seconds

    private Context mContext;
    private Controller mController;
    private AppWidgetManager mAppWidgetManager;
    private BroadcastReceiver mBroadcastBridge;

    // list of available widgets
    public static SparseArray<WidgetData> awailableWidgets = new SparseArray<WidgetData>();
    // list of actually used facilities (some widgets use these facilities)
    public static List<Facility> usedFacilities = new ArrayList<Facility>();
    public static List<Location> usedLocations = new ArrayList<Location>();

    // variables for checking if user go logged out / in
    private boolean isLoggedInLast = false;
    private boolean isLoginStateChanged = true;

    /**
     * Place to start the service from widget provider
     * @param context
     * @param appWidgetIds
     */
    public static void startUpdating(Context context, int[] appWidgetIds){
        Log.d(TAG, "startUpdating()");

        final Intent intent =  getUpdateIntent(context);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.startService(intent);
    }

    /**
     * When no widgets are available, stops the service
     * @param context
     */
    public static void stopUpdating(Context context) {
        Log.d(TAG, "stopUpdating()");

        stopAlarm(context);

        // removes all widgets in list
        WidgetService.awailableWidgets.clear();
        // removes all used facilities
        WidgetService.usedFacilities.clear();
        // removes all used locations
        WidgetService.usedLocations.clear();

        // stop this service
        final Intent intent =  getUpdateIntent(context);
        context.stopService(intent);
    }

    /**
     * Set repeating to parameter
     * @param triggerAtMillis
     */
    private void setAlarm(long triggerAtMillis){
        // Set new alarm time
        AlarmManager m = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        m.set(AlarmManager.ELAPSED_REALTIME, triggerAtMillis, getUpdatePendingIntent(mContext));
    }

    /**
     * Stops repeating
     * @param context
     */
    private static void stopAlarm(Context context){
        // cancel frequently refreshing
        AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        m.cancel(getUpdatePendingIntent(context));
    }

    /**
     * Get intent of this class
     * @param context
     * @return
     */
    private static Intent getUpdateIntent(Context context) {
        return new Intent(context, WidgetService.class);
    }

    /**
     * Pending intent of this class
     * @param context
     * @return
     */
    private static PendingIntent getUpdatePendingIntent(Context context){
        final Intent intent = getUpdateIntent(context);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    /**
     * Force intent
     * @param context
     * @param widgetId
     * @return
     */
    public static Intent getForceUpdateIntent(Context context, int widgetId) {
        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(WidgetService.EXTRA_FORCE_UPDATE, true);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{
                widgetId
        });

        return intent;
    }

    /**
     * Force pending intent
     * @param context
     * @param widgetId
     * @return
     */
    public static PendingIntent getForceUpdatePendingIntent(Context context, int widgetId) {
        final Intent intent = getForceUpdateIntent(context, widgetId);

        return PendingIntent.getService(context, widgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    /**
     * Intent for putting service asleep
     * @param context
     * @param standby
     * @return
     */
    public static Intent getStandByIntent(Context context, boolean standby){
        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(WidgetService.EXTRA_STANDBY, standby);

        return intent;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        mContext = getApplicationContext();
        mAppWidgetManager = AppWidgetManager.getInstance(mContext);
        mController = Controller.getInstance(mContext);

        boolean isStandBy = intent.getBooleanExtra(EXTRA_STANDBY, false);
        if(isStandBy){
            Log.d(TAG, "is standby...");
            stopAlarm(mContext);
            return START_STICKY;
        }

        boolean forceUpdate = intent.getBooleanExtra(EXTRA_FORCE_UPDATE, false);

        Log.v(TAG, String.format("onStartCommand(), startId = %d, forceUpdate = %b", startId, forceUpdate));

        if (!forceUpdate) {
            // set alarm for next update
            long nextUpdate = calcNextUpdate();

            if (nextUpdate > 0) {
                Log.d(TAG, String.format("Next update in %d seconds", (int) (nextUpdate - SystemClock.elapsedRealtime()) / 1000));
                setAlarm(nextUpdate);
            } else {
                Log.d(TAG, "No planned next update");
                stopSelf();
                return START_STICKY;
            }
        }

        // initializes receiver for screen on/off + time ticking (only once)
        initializeBroadcastBridge();

        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWidgets(intent);
            }
        }).start();

        // TODO should be start_sticky or start_not_sticky (was)
        return START_STICKY;
    }

    /**
     * Creates broadcast receiver which bridges broadcasts to appwidgets for handling time and screen
     * No operation if already initialized
     */
    private void initializeBroadcastBridge() {
        if(mBroadcastBridge == null){
            Log.d(TAG, "registeringBroadcastReceiver()");
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            mBroadcastBridge = new WidgetBridgeBroadcastReceiver();
            registerReceiver(mBroadcastBridge, filter);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(mBroadcastBridge != null){
            Log.d(TAG, "UNregistering");
            unregisterReceiver(mBroadcastBridge);
            mBroadcastBridge = null;
        }
    }

    /**
     * Method calls all widgets which needs to be updated
     * @param intent
     */
    private void updateWidgets(Intent intent) {
        Log.d(TAG, "updateWidgets()");

        // check login state and for one cycle set flag
        if((mController.isLoggedIn() && !isLoggedInLast) || (!mController.isLoggedIn() && isLoggedInLast)){
            Log.d(TAG, "change state!");
            isLoginStateChanged = true;
            isLoggedInLast = mController.isLoggedIn();
        }

        int[] allWidgetIds = intent.getIntArrayExtra(mAppWidgetManager.EXTRA_APPWIDGET_IDS);
        if(allWidgetIds == null || allWidgetIds.length == 0){
            allWidgetIds = getAllIds();
        }

        if(allWidgetIds.length == 0) Log.d(TAG, "No Widget Ids !!");

        boolean forceUpdate = intent.getBooleanExtra(EXTRA_FORCE_UPDATE, false);
        long timeNow = SystemClock.elapsedRealtime();
        SparseArray<WidgetData> widgetsToUpdate = new SparseArray<>();

        // Reload adapters to have data about Timezone offset
        mController.reloadAdapters(false);
        //mController.reloadLocations()

        // update all widgets
        for(int widgetId : allWidgetIds) {
            if(WidgetData.getSettings(mContext, widgetId) == null){
                Log.d(TAG, String.format("Widget %d without data (probably configurating)", widgetId));
                continue;
            }

            WidgetData widgetData = getWidgetData(widgetId);
            if(widgetData == null){
                Log.d(TAG, "NotExistingWidgetData");
                continue;
            }

            // Ignore uninitialized widgets
            if (!widgetData.initialized) {
                Log.v(TAG, String.format("Ignoring widget %d (not initialized)", widgetId));
                continue;
            }

            // Don't update widgets until their interval elapsed or we have force update
            if (!forceUpdate && !widgetData.isExpired(timeNow)) {
                Log.v(TAG, String.format("Ignoring widget %d (not expired nor forced)", widgetId));
                continue;
            }

            // initializes variables, sets remoteViews & helper variables for operations
            if(forceUpdate || !widgetData.widgetProvider.initialized) {
                widgetData.widgetProvider.initialize(mContext, widgetData);
            }

            // if user state changes, calls so that there is visible change when neseccery (no operation by default)
            if(isLoginStateChanged){
                if(isLoggedInLast)
                    widgetData.widgetProvider.whenUserLogin();
                else
                    widgetData.widgetProvider.whenUserLogout();
            }

            // if preparation of widget is successfull, remember this widget
            if(widgetData.widgetProvider.prepare()){
                widgetsToUpdate.put(widgetId, widgetData);
            }
        }

		if(!WidgetService.usedLocations.isEmpty()){
			// ziskat vsechny facilities z mistnosti
			// nejak je priradit podle toho seznamu facilities
			// tak aby tam byly vzdy fresh data, aby se nemusely ostatni widgety dotazovat na fresh data

//            mController.reloadLocations("64206", forceUpdate);

            /*
            for(Location loc : WidgetService.usedLocations){
                loc = mController.getLocation(widgetd)
            }
            //*/
		}

        if (!WidgetService.usedFacilities.isEmpty()) {
            mController.updateFacilities(WidgetService.usedFacilities, forceUpdate);
        }

        for (int i = 0; i < widgetsToUpdate.size(); i++) {
            WidgetData widgetData = widgetsToUpdate.valueAt(i);

			// change actual widget's data
            widgetData.widgetProvider.changeData();

            // Update widget
            widgetData.widgetProvider.setValues();
        }

        // put back information about changed login
        isLoginStateChanged = false;
    }

    /**
     * Gets widget data either from list of widgets or instantiating and loading from settings (when OS restarted)
     * @param widgetId id of widget to load
     * @return
     */
    public WidgetData getWidgetData(int widgetId) {
        return getWidgetData(widgetId, mContext);
    }

    /**
     * TODO doresit vyjimky
     * Returns widget data by id (usage outside service)
     * @param widgetId system widget id
     * @param context context
     * @return widget data class
     */
    public static WidgetData getWidgetData(int widgetId, Context context){
        WidgetData widgetData = awailableWidgets.get(widgetId);
        if (widgetData == null) {
            String widgetClassName = WidgetData.getSettingClassName(context, widgetId);
            // TODO mozna nejaka chyba??
            if (widgetClassName.isEmpty()) return null;

            // TODO osetrit
            try {
                // instantiate class from string
                widgetData = (WidgetData) Class.forName(widgetClassName).getConstructor(int.class).newInstance(widgetId);
                widgetData.loadData(context);
                awailableWidgets.put(widgetId, widgetData);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return null;
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                return null;
            } catch (InstantiationException e) {
                e.printStackTrace();
                return null;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }

        return widgetData;
    }

    /**
     * Deletes widget data from list of widget datas
     * @param widgetData object of WidgetData
     */
    public static void deleteWidgetData(WidgetData widgetData){
        WidgetService.awailableWidgets.delete(widgetData.getWidgetId());
    }


    /**
     * Get next time to update widgets
     * @return
     */
    private long calcNextUpdate() {
        int minInterval = 0;
        long nextUpdate = 0;
        long timeNow = SystemClock.elapsedRealtime();
        boolean first = true;

        for (int widgetId : getAllIds()) {
            WidgetData widgetData = getWidgetData(widgetId);

            if (widgetData == null || !widgetData.initialized) {
                // widget is not added yet (probably only configuration activity is showed)
                continue;
            }

            if (first) {
                minInterval = widgetData.interval;
                nextUpdate = widgetData.getNextUpdate(timeNow);
                first = false;
            } else {
                minInterval = Math.min(minInterval, widgetData.interval);
                nextUpdate = Math.min(nextUpdate, widgetData.getNextUpdate(timeNow));
            }
        }

        minInterval = Math.max(minInterval, UPDATE_INTERVAL_MIN);
        return first ? 0 : Math.max(nextUpdate, SystemClock.elapsedRealtime() + minInterval * 1000);
    }

    /**
     * Gets ids of one widget provider
     * @param cls class of widget provider
     * @return widget ids list
     */
    private List<Integer> getWidgetIds(Class<?> cls) {
        return getWidgetIds(cls, this, mAppWidgetManager);
    }

    public static List<Integer> getWidgetIds(Class<?> cls, Context context, AppWidgetManager widgetManager){
        ComponentName thisWidget = new ComponentName(context, cls);

        List<Integer> arr = new ArrayList<Integer>();
        for (int i : widgetManager.getAppWidgetIds(thisWidget)) {
            arr.add(i);
        }
        return arr;
    }


    /**
     * !!! If we have any new widget, we need to add it here so that it keeps updating. !!!
     * @return array of widget ids
     */
    public int[] getAllIds() {
        List<Integer> ids = new ArrayList<Integer>();

        // here need to add all widget providers
        ids.addAll(getWidgetIds(WidgetClockProvider.class));
        ids.addAll(getWidgetIds(WidgetLocationListProvider.class));
        ids.addAll(getWidgetIds(WidgetSensorProvider.class));

        int[] arr = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            arr[i] = ids.get(i);
        }

        return arr;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
