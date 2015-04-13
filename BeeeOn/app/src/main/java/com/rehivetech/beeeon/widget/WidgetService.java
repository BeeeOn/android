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
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.SparseArray;

import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.asynctask.ActorActionTask;
import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.clock.WidgetClockProvider;
import com.rehivetech.beeeon.widget.location.WidgetLocationListProvider;
import com.rehivetech.beeeon.widget.device.WidgetDeviceData;
import com.rehivetech.beeeon.widget.device.WidgetDeviceProvider;
import com.rehivetech.beeeon.widget.device.WidgetDeviceProviderLarge;
import com.rehivetech.beeeon.widget.device.WidgetDeviceProviderMedium;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mlyko
 */
public class WidgetService extends Service {
    private final static String TAG = WidgetService.class.getSimpleName();

    public static final int UPDATE_INTERVAL_DEFAULT = 10; // in seconds
    public static final int UPDATE_INTERVAL_MIN = 5; // in seconds

    private static final String EXTRA_FORCE_UPDATE = "com.rehivetech.beeeon.force_update";
    private static final String EXTRA_STANDBY = "com.rehivetech.beeeon.standby";
    private static final String EXTRA_ACTOR_CHANGE = "com.rehivetech.beeeon.actor_change";
    private static final String EXTRA_DELETE_WIDGET = "com.rehivetech.beeeon.delete_widget";
    private static final String EXTRA_CHANGE_LAYOUT = "com.rehivetech.beeeon.change_layout";
    private static final String EXTRA_CHANGE_LAYOUT_RESOURCE = "com.rehivetech.beeeon.change_layout_resource";

    // list of available widgets
    private static SparseArray<WidgetData> availableWidgets = new SparseArray<WidgetData>();

    private Context mContext;
    private Controller mController;
    private AppWidgetManager mAppWidgetManager;
    private BroadcastReceiver mBroadcastBridge;
    // helpers
    private UnitsHelper mUnitsHelper;
    private TimeHelper mTimeHelper;

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
     * Insert widgetData to list of used widgets
     * @param widgetData
     */
    public static void addWidgetData(WidgetData widgetData) {
        // add widgetData to service
        availableWidgets.put(widgetData.getWidgetId(), widgetData);
    }

    /**
     * Deletes widgets data by Ids
     * @param widgetIds
     */
    private void widgetsDelete(int[] widgetIds) {
        for(int widgetId : widgetIds){
            if(widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) continue;

            WidgetData data = getWidgetData(widgetId);
            widgetDelete(mContext, data);
        }
    }

    /**
     * For deleting widgetData outside of service
     * @param context
     * @param data
     */
    public static void widgetDelete(Context context, WidgetData data){
        if(data == null) return;
        int widgetId = data.getWidgetId();
        Log.v(TAG, String.format("delete widgetData(%d)", widgetId));

        data.deleteData(context);
        availableWidgets.delete(widgetId);
    }

    /**
     * Gets widget data either from list of widgets or instantiating and loading from settings (when OS restarted)
     * @param widgetId id of widget to load
     * @return
     */
    public WidgetData getWidgetData(int widgetId){
        WidgetData widgetData = availableWidgets.get(widgetId);
        if (widgetData == null) {
            String widgetClassName = WidgetData.getSettingClassName(mContext, widgetId);
            // TODO mozna nejaka chyba??
            if (widgetClassName.isEmpty()) return null;

            // TODO osetrit
            try {
                // instantiate class from string
                widgetData = (WidgetData) Class.forName(widgetClassName).getConstructor(int.class, Context.class).newInstance(widgetId, mContext);
                availableWidgets.put(widgetId, widgetData);
                Log.v(TAG, String.format("finished creation of WidgetData(%d)", widgetId));
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
            filter.addAction(Intent.ACTION_LOCALE_CHANGED);
            mBroadcastBridge = new WidgetBridgeBroadcastReceiver();
            registerReceiver(mBroadcastBridge, filter);
        }
    }

    /**
     * Initializes things only first time service is started
     */
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        super.onCreate();

        mContext = getApplicationContext();
        mAppWidgetManager = AppWidgetManager.getInstance(mContext);
        mController = Controller.getInstance(mContext);

        // TODO inicializovat helpery kontrolovat ale, jestli se nekdo neodhlasil
        SharedPreferences userSettings = mController.getUserSettings();
        mUnitsHelper = (userSettings == null) ? null : new UnitsHelper(userSettings, mContext);
        mTimeHelper = (userSettings == null) ? null : new TimeHelper(userSettings);
    }

    /**
     * Cleaning up cause service is stopped
     */
    @Override
    public void onDestroy(){
        super.onDestroy();

        // removes all widgets in list
        availableWidgets.clear();

        if(mBroadcastBridge != null){
            Log.v(TAG, "Unregistering broadcast bridge");
            unregisterReceiver(mBroadcastBridge);
            mBroadcastBridge = null;
        }
    }

    /**
     * Entry point of service .. widgets can sent Intent with specific data and pass them to service
     * @param intent Intent which can specify what action should service do now
     * @param flags      
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        boolean isActorChange = false, isStandBy = false, isForceUpdate = false, isChangeLayout = false;
        int[] appWidgetIds = new int[]{};

        if(intent != null) {
            // if standby, stop alarm (leaves service running though)
            // better if first, so that less code is running
            isStandBy = intent.getBooleanExtra(EXTRA_STANDBY, false);
            if (isStandBy) {
                Log.d(TAG, "is standby...");
                stopAlarm(mContext);
                return START_STICKY;
            }

            // get widget Ids
            appWidgetIds  = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

            // async task for changing widget actor
            isActorChange = intent.getBooleanExtra(EXTRA_ACTOR_CHANGE, false);
            if (isActorChange) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        changeWidgetActor(intent);
                    }
                }).start();
            }

            // widget deletion
            boolean isDeleteWidget = intent.getBooleanExtra(EXTRA_DELETE_WIDGET, false);
            if(isDeleteWidget){
                widgetsDelete(appWidgetIds);
            }

            // force update
            isForceUpdate = intent.getBooleanExtra(EXTRA_FORCE_UPDATE, false);

            // onAppWidgetOptionsChanged (changing layout)
            isChangeLayout = intent.getBooleanExtra(EXTRA_CHANGE_LAYOUT, false);
            if(isChangeLayout){
                // TODO doresit jestli by nebylo lepsi primo v updateWidgets()
                int newWidgetLayout = intent.getIntExtra(EXTRA_CHANGE_LAYOUT_RESOURCE, 0);
                widgetsChangeLayout(appWidgetIds, newWidgetLayout);
                isForceUpdate = true;
            }
        }

        Log.v(TAG, String.format("onStartCommand(intent = %b), startId = %d, standby = %b, actorchange = %b, forceUpdate = %b, changeLayout = %b", (intent == null), startId, isStandBy, isActorChange, isForceUpdate, isChangeLayout));

        if (!isForceUpdate) {
            // set alarm for next update
            long nextUpdate = calcNextUpdate(appWidgetIds);

            if (nextUpdate > 0) {
                Log.d(TAG, String.format("Next update in %d seconds", (int) (nextUpdate - SystemClock.elapsedRealtime()) / 1000));
                setAlarm(nextUpdate);
            } else {
                Log.d(TAG, "No planned next update");
                WidgetService.stopUpdating(mContext);
                return START_STICKY;
            }
        }

        // initializes receiver for screen on/off + time ticking (only once)
        initializeBroadcastBridge();

        // TODO takto retardovane?
        final int[] xxx = appWidgetIds;
        final boolean forceXxx = isForceUpdate;

        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWidgets(forceXxx, xxx);
            }
        }).start();

        return START_STICKY;
    }

    /**
     * Method calls all widgets which needs to be updated
     * @param isForceUpdate
     * @param allWidgetIds
     */
    private void updateWidgets(boolean isForceUpdate, int[] allWidgetIds) {
        Log.d(TAG, "updateWidgets()");

        // check login state and for one cycle set flag
        if((mController.isLoggedIn() && !isLoggedInLast) || (!mController.isLoggedIn() && isLoggedInLast)){
            Log.d(TAG, "changed login state!");
            isLoginStateChanged = true;
            isLoggedInLast = mController.isLoggedIn();
        }

        if(allWidgetIds == null || allWidgetIds.length == 0){
            allWidgetIds = getAllIds();
        }

        try {
            // Reload adapters to have data about Timezone offset
            mController.getAdaptersModel().reloadAdapters(false);
        } catch(AppException e) {
            // TODO !!!!
            Log.e(TAG, e.getSimpleErrorMessage());
        }

        Log.d(TAG, "Widgets length = " + String.valueOf(allWidgetIds.length));

        long timeNow = SystemClock.elapsedRealtime();
        SparseArray<WidgetData> widgetsToUpdate = new SparseArray<>();
        List<Object> widgetReferredObjects = new ArrayList<>();

        Map<String, List<Object>> usedObjects = new HashMap<>();

        // update all widgets
        // TODO must not have any network request !!
        for(int widgetId : allWidgetIds) {
            // ziska bud z pole pouzitych dat nebo instanciuje z disku
            WidgetData widgetData = getWidgetData(widgetId);
            if(widgetData == null){
                Log.w(TAG, String.format("WidgetData(%d) doesn't exist & couldn't be created", widgetId));
                continue;
            }

            // Ignore uninitialized widgets
            if (!widgetData.initialized) {
                Log.v(TAG, String.format("Ignoring widget %d (not initialized)", widgetId));
                continue;
            }

            // Don't update widgets until their interval elapsed or we have force update
            if (!isForceUpdate && !widgetData.isExpired(timeNow)) {
                Log.v(TAG, String.format("Ignoring widget %d (not expired nor forced)", widgetId));
                continue;
            }

            // if user state changes, calls so that there is visible change when neseccery (no operation by default)
            if(isLoginStateChanged){
                if(isLoggedInLast)
                    widgetData.whenUserLogin();
                else
                    widgetData.whenUserLogout();
            }

            // if preparation of widget is successfull, remember this widget
            if(widgetData.prepare()){
                widgetsToUpdate.put(widgetId, widgetData);

                // TODO udelat jako addAll (aby lokace mohla pridat vsechny facilities)
                widgetReferredObjects.add(widgetData.getReferredObj());

                /*
                Object refObj = widgetData.getReferredObj();
                String simpleClassName = refObj.getClass().getSimpleName();
                List<? extends IIdentifier> xxx;
                if(usedObjects.get(simpleClassName) == null){
                    xxx = new ArrayList<>();
                    usedObjects.put(simpleClassName, xxx);
                }
                else{
                    xxx = usedObjects.get(simpleClassName);
                }

                if(xxx == null || Utils.getFromList(refObj.getId(), xxx) != null) continue;
                xxx.add(refObj);
                //*/
            }
        }

        try{
            mController.beginPersistentConnection();

            List<Facility> usedFacilities = new ArrayList<>();

            if(!widgetReferredObjects.isEmpty()){
                for(Object refObj : widgetReferredObjects){
                    if(refObj instanceof Facility){
                        Facility fac = (Facility) refObj;
                        // already there, skip
                        // TODO predava se do Utils NULL
                        if(usedFacilities == null || Utils.getFromList(fac.getId(), usedFacilities) != null) continue;
                        usedFacilities.add((Facility) refObj);
                    }

                    // TODO pridat napr lokaci
                }
            }

            if(!usedFacilities.isEmpty()){
                Log.v(TAG, "refreshing facilities...");
                mController.getFacilitiesModel().refreshFacilities(usedFacilities, isForceUpdate);
            }

            for (int i = 0; i < widgetsToUpdate.size(); i++) {
                WidgetData widgetData = widgetsToUpdate.valueAt(i);

                // change actual widget's data
                widgetData.changeData();

                // Update widget
                widgetData.setLayoutValues();
            }

            mController.endPersistentConnection();
        } catch(AppException e){
            Log.e(TAG, e.getSimpleErrorMessage());
            // TODO doresit vyjimky
        }

        // put back information about changed login
        isLoginStateChanged = false;
    }

    private void widgetsChangeLayout(int[] widgetIds, int layoutResource){
        for(int widgetId : widgetIds){
            if(widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) continue;

            WidgetData data = getWidgetData(widgetId);
            if(data == null) continue;

            Log.v(TAG, String.format("change widgetData layout(%d)", widgetId));
            data.saveLayout(mContext, layoutResource);
            data.saveData(mContext);
            data.updateLayout();
        }
    }

    /**
     * Actor task
     * @param intent  id of widgets
     */
    private void changeWidgetActor(Intent intent) {
        Log.d(TAG, "changeWidgetActor()");
        int[] allWidgetIds;

        if(intent != null){
            allWidgetIds = intent.getIntArrayExtra(mAppWidgetManager.EXTRA_APPWIDGET_IDS);
        }
        else{
            allWidgetIds = new int[]{};
        }

        if(allWidgetIds == null || allWidgetIds.length == 0){
            Log.d(TAG, "No Widget Ids !!");
            return;
        }

        for(int widgetId : allWidgetIds){
            // TODO toto je spatne
            final WidgetDeviceData widgetData = (WidgetDeviceData) getWidgetData(widgetId);
            final Device dev = mController.getFacilitiesModel().getDevice(widgetData.adapterId, widgetData.deviceId);

            ActorActionTask mActorActionTask = new ActorActionTask(mContext.getApplicationContext());
            mActorActionTask.setListener(new CallbackTask.CallbackTaskListener() {
                @Override
                public void onExecute(boolean success) {
                    widgetData.asyncTask(dev);
                }
            });

            mActorActionTask.execute(dev);
        }

    }

    /**
     * Get next time to update widgets
     * @return
     */
    private long calcNextUpdate(int[] appWidgetIds) {
        int minInterval = 0;
        long nextUpdate = 0;
        long timeNow = SystemClock.elapsedRealtime();
        boolean first = true;

        // first gets IDs of all widgets
        int[] widgetIds = getAllIds();

        Log.d(TAG, String.format("getAllIds = %d", widgetIds.length));
        // if none, tries the passed ids
        if(widgetIds == null || widgetIds.length == 0){
            widgetIds = appWidgetIds;
        }

        if(widgetIds != null) for (int widgetId : widgetIds) {
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
        ComponentName thisWidget = new ComponentName(mContext, cls);

        List<Integer> arr = new ArrayList<Integer>();
        for (int i : mAppWidgetManager.getAppWidgetIds(thisWidget)) {
            arr.add(i);
        }
        return arr;
    }

    /**
     * !!! If we have any new widget, we need to add it here so that it keeps updating. !!!
     * @return array of widget ids
     */
    private int[] getAllIds() {
        List<Integer> ids = new ArrayList<>();

        // here need to add all widget providers
        ids.addAll(getWidgetIds(WidgetClockProvider.class));
        ids.addAll(getWidgetIds(WidgetLocationListProvider.class));
        // sensor widget
        ids.addAll(getWidgetIds(WidgetDeviceProvider.class));
        ids.addAll(getWidgetIds(WidgetDeviceProviderMedium.class));
        ids.addAll(getWidgetIds(WidgetDeviceProviderLarge.class));

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

    // -------- Intent factories

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
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{ widgetId });

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

    public static Intent getActorChangeIntent(Context context, int widgetId){
        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(WidgetService.EXTRA_ACTOR_CHANGE, true);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{ widgetId });
        return intent;
    }

    public static PendingIntent getActorChangePendingIntent(Context context, int widgetId) {
        final Intent intent = getActorChangeIntent(context, widgetId);
        return PendingIntent.getService(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static Intent getWidgetDeleteIntent(Context context, int[] widgetIds) {
        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(WidgetService.EXTRA_DELETE_WIDGET, true);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        return intent;
    }

    public static Intent getWidgetChangeLayoutIntent(Context context, int widgetId,  int layout){
        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(EXTRA_CHANGE_LAYOUT, true);
        intent.putExtra(EXTRA_CHANGE_LAYOUT_RESOURCE, layout);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{ widgetId });
        return intent;
    }
}
