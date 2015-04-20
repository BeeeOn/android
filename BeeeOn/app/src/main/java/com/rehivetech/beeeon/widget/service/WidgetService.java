package com.rehivetech.beeeon.widget.service;

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

import com.rehivetech.beeeon.asynctask.ActorActionTask;
import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ErrorCode;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.values.BaseEnumValue;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.data.WidgetDeviceData;
import com.rehivetech.beeeon.widget.persistence.WidgetDevice;
import com.rehivetech.beeeon.widget.receivers.WidgetBridgeBroadcastReceiver;
import com.rehivetech.beeeon.widget.data.WidgetData;
import com.rehivetech.beeeon.widget.receivers.WidgetClockProvider;
import com.rehivetech.beeeon.widget.receivers.WidgetDeviceProvider;
import com.rehivetech.beeeon.widget.receivers.WidgetDeviceProviderLarge;
import com.rehivetech.beeeon.widget.receivers.WidgetDeviceProviderMedium;
import com.rehivetech.beeeon.widget.receivers.WidgetLocationListProvider;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mlyko
 */
public class WidgetService extends Service {
    private final static String TAG = WidgetService.class.getSimpleName();

    public static final int UPDATE_INTERVAL_DEFAULT = 10; // in seconds
    public static final int UPDATE_INTERVAL_MIN = 5; // in seconds

    private static final String EXTRA_START_UPDATING =          "com.rehivetech.beeeon.start_updating";
    private static final String EXTRA_FORCE_UPDATE =            "com.rehivetech.beeeon.force_update";
    private static final String EXTRA_STANDBY =                 "com.rehivetech.beeeon.standby";
    private static final String EXTRA_ACTOR_CHANGE_REQUEST =    "com.rehivetech.beeeon.actor_change_request";
    private static final String EXTRA_ACTOR_CHANGE_RESULT =     "com.rehivetech.beeeon.actor_change_result";
    private static final String EXTRA_ACTOR_ID =                "com.rehivetech.beeeon.actor_ids";
    private static final String EXTRA_DELETE_WIDGET =           "com.rehivetech.beeeon.delete_widget";
    private static final String EXTRA_CHANGE_LAYOUT =           "com.rehivetech.beeeon.change_layout";
    private static final String EXTRA_CHANGE_LAYOUT_RESOURCE =  "com.rehivetech.beeeon.change_layout_resource";
    private static final String EXTRA_WIDGETS_SHOULD_RELOAD =   "com.rehivetech.beeeon.widget_should_reload";
    private static final String EXTRA_ACTOR_ADAPTER_ID =        "com.rehivetech.beeeon.actor_adapter_id";

    // list of available widgets
    private SparseArray<WidgetData> mAvailableWidgets = new SparseArray<>();

    private Context mContext;
    private Controller mController;
    private AppWidgetManager mAppWidgetManager;
    private BroadcastReceiver mBroadcastBridge;
    // helpers
    private UnitsHelper mUnitsHelper;
    private TimeHelper mTimeHelper;

    // for checking if user is logged in (we presume that for the first time he is)
    private boolean isLoggedIn = true;

    // -------------------------------------------------------------------- //
    // --------------- Main methods (entry points) of service ------------- //
    // -------------------------------------------------------------------- //

    /**
     * When startin updating widgets, we can tell that these widgets should be reloaded
     * @param context
     * @param appWidgetIds
     * @param widgetShouldReload
     */
    public static void startUpdating(Context context, int[] appWidgetIds, boolean widgetShouldReload){
        Log.d(TAG, "startUpdating()");
        final Intent intent =  getIntentUpdate(context);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        intent.putExtra(EXTRA_START_UPDATING, true);
        intent.putExtra(EXTRA_WIDGETS_SHOULD_RELOAD, widgetShouldReload);
        context.startService(intent);
    }
    
    /**
     * Place to start the service from widget provider
     * @param context
     * @param appWidgetIds
     */
    public static void startUpdating(Context context, int[] appWidgetIds){
        startUpdating(context, appWidgetIds, false);
    }

    /**
     * When no widgets are available, stops the service
     * @param context
     */
    public static void stopUpdating(Context context) {
        Log.d(TAG, "stopUpdating()");

        stopAlarm(context);

        // stop this service
        final Intent intent =  getIntentUpdate(context);
        context.stopService(intent);
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

        // Creates broadcast receiver which bridges broadcasts to appwidgets for handling time and screen
        Log.v(TAG, "registeringBroadcastReceiver()");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(ActorActionTask.ACTION_ACTOR_CHANGED);

        mBroadcastBridge = new WidgetBridgeBroadcastReceiver();
        registerReceiver(mBroadcastBridge, filter);
    }

    /**
     * Cleaning up cause service is stopped
     */
    @Override
    public void onDestroy(){
        super.onDestroy();

        // removes all widgets in list
        mAvailableWidgets.clear();

        Log.v(TAG, "Unregistering broadcast bridge");
        unregisterReceiver(mBroadcastBridge);
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

        // TODO zoptimalizovat aby se to volalo jenom tehdy, kdy je potreba (tzn kdyz se prihlasi / odhlasi / etc)
        mController = Controller.getInstance(mContext);
        initHelpers(mContext, mController);

        boolean isWidgetReload = false,
                isStartUpdating = false,
                isActorChangeRequest = false,
                isActorChangeResult = false,
                isStandBy = false,
                isForceUpdate = false,
                isChangeLayout = false;
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

            // -------------- async task for changing widget actor
            isActorChangeRequest = intent.getBooleanExtra(EXTRA_ACTOR_CHANGE_REQUEST, false);
            if (isActorChangeRequest) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String adapterId = intent.getStringExtra(EXTRA_ACTOR_ADAPTER_ID);
                        String actorId = intent.getStringExtra(EXTRA_ACTOR_ID);
                        changeWidgetActorRequest(adapterId, actorId);
                    }
                }).start();
                return START_STICKY;
            }

            // -------------- result of async task (called by broadcast)
            isActorChangeResult = intent.getBooleanExtra(EXTRA_ACTOR_CHANGE_RESULT, false);
            if(isActorChangeResult){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String adapterId = intent.getStringExtra(EXTRA_ACTOR_ADAPTER_ID);
                        String actorId = intent.getStringExtra(EXTRA_ACTOR_ID);
                        changeWidgetActorResult(adapterId, actorId);
                    }
                }).start();
                return START_STICKY;
            }

            // widget deletion
            boolean isDeleteWidget = intent.getBooleanExtra(EXTRA_DELETE_WIDGET, false);
            if(isDeleteWidget){
                widgetsDelete(appWidgetIds);
            }

            // force update
            isForceUpdate = intent.getBooleanExtra(EXTRA_FORCE_UPDATE, false);

            // onAppWidgetOptionsChanged (changing widgetLayout)
            isChangeLayout = intent.getBooleanExtra(EXTRA_CHANGE_LAYOUT, false);
            if(isChangeLayout){
                // TODO doresit jestli by nebylo lepsi primo v updateWidgets()
                int newWidgetLayout = intent.getIntExtra(EXTRA_CHANGE_LAYOUT_RESOURCE, 0);
                widgetsChangeLayout(appWidgetIds, newWidgetLayout);
                isForceUpdate = true;
            }

            // start updating
            isStartUpdating = intent.getBooleanExtra(EXTRA_START_UPDATING, false);
            if(isStartUpdating){
                isForceUpdate = true;
            }

            isWidgetReload = intent.getBooleanExtra(EXTRA_WIDGETS_SHOULD_RELOAD, false);
        }

        Log.v(TAG, String.format("onStartCommand(intent = %b), startUpdating = %b, standby = %b, isActorWantsChange = %b, forceUpdate = %b, changeLayout = %b", (intent == null), isStartUpdating, isStandBy, isActorChangeRequest, isForceUpdate, isChangeLayout));

        // calculate it first time always or only when force update
        if (isStartUpdating || !isForceUpdate) {
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

        final boolean widgetsShouldReload = isWidgetReload;
        final boolean foceUpdating = isForceUpdate;
        final int[] updatingWidgets = appWidgetIds;

        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWidgets(widgetsShouldReload, foceUpdating, updatingWidgets);
            }
        }).start();

        return START_STICKY;
    }

    // -------------------------------------------------------------------- //
    // ------------------------ Running in own threads -------------------- //
    // -------------------------------------------------------------------- //

    /**
     * Method calls all widgets which needs to be updated
     * @param isForceUpdate
     * @param allWidgetIds
     */
    private void updateWidgets(boolean widgetsShouldReload, boolean isForceUpdate, int[] allWidgetIds) {
        Log.d(TAG, "updateWidgets()");

        // if there are no widgets passed by intent, try to get all widgets
        if(allWidgetIds == null || allWidgetIds.length == 0) allWidgetIds = getAllIds();

        try {
            // TODO presunout nize ke zbytku pozadavkum?
            // Reload adapters to have data about Timezone offset
            mController.getAdaptersModel().reloadAdapters(false);
        } catch(AppException e) {
            // TODO probably do nothing cause we need to update widgets !!!!
            Log.e(TAG, e.getSimpleErrorMessage());
        }

        long timeNow = SystemClock.elapsedRealtime();
        SparseArray<WidgetData> widgetsToUpdate = new SparseArray<>();
        List<Object> widgetReferredObjects = new ArrayList<>();

        // update all widgets
        // TODO must not have any network request !!
        for(int widgetId : allWidgetIds) {
            // ziska bud z pole pouzitych dat nebo instanciuje z disku
            WidgetData widgetData = getWidgetData(widgetId);
            if(widgetData == null){
                Log.w(TAG, String.format("WidgetData(%d) doesn't exist & couldn't be created", widgetId));
                continue;
            }

            // reload data (after configuration and if objects already exists)
            if(widgetsShouldReload) widgetData.reload();

            // Ignore uninitialized widgets
            if (!widgetData.widgetInitialized) {
                Log.v(TAG, String.format("Ignoring widget %d (not widgetInitialized)", widgetId));
                continue;
            }

            // Don't update widgets until their interval elapsed or we have force update
            if (!isForceUpdate && !widgetData.isExpired(timeNow)) {
                Log.v(TAG, String.format("Ignoring widget %d (not expired nor forced)", widgetId));
                continue;
            }

            // if preparation of widget is successfull, remember this widget
            widgetsToUpdate.put(widgetId, widgetData);
            widgetReferredObjects.addAll(widgetData.getReferredObj());
        }

        try{
            mController.beginPersistentConnection();

            List<Facility> usedFacilities = new ArrayList<>();
            // check if any objects need to refresh
            if(!widgetReferredObjects.isEmpty()){
                for(Object refObj : widgetReferredObjects){
                    if(refObj instanceof Facility){
                        Facility fac = (Facility) refObj;
                        // already there, skip
                        if(usedFacilities == null || Utils.getFromList(fac.getId(), usedFacilities) != null) continue;
                        usedFacilities.add((Facility) refObj);
                    }
                    else if(refObj instanceof Location){
                         // TODO
                    }
                }
            }

            if(!usedFacilities.isEmpty()){
                Log.v(TAG, "refreshing facilities...");
                mController.getFacilitiesModel().refreshFacilities(usedFacilities, isForceUpdate);
            }

            for (int i = 0; i < widgetsToUpdate.size(); i++) {
                WidgetData widgetData = widgetsToUpdate.valueAt(i);
                // if previous state was logged out
                if(isLoggedIn == false) widgetData.handleUserLogin();
                widgetData.update();
            }

            isLoggedIn = true;

            mController.endPersistentConnection();
        } catch(AppException e){
            ErrorCode errCode = e.getErrorCode();
            if(errCode != null){
                Log.e(TAG, e.getSimpleErrorMessage());
                if(errCode instanceof NetworkError && errCode == NetworkError.SRV_BAD_BT || errCode == NetworkError.CL_INTERNET_CONNECTION){
                    // TODO rozsirit aj na jine chyby
                    if(isLoggedIn == true){
                        // for all widgets put to "logout" state
                        for(int i = 0; i < mAvailableWidgets.size(); i++) {
                            WidgetData widgetData = mAvailableWidgets.valueAt(i);
                            widgetData.handleUserLogout();
                        }
                    }

                    isLoggedIn = false;
                }
            }
        }
    }

    /**
     * Handle when widget resizes
     * @param widgetIds
     * @param layoutResource
     */
    private void widgetsChangeLayout(int[] widgetIds, int layoutResource){
        for(int widgetId : widgetIds){
            if(widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) continue;

            WidgetData data = getWidgetData(widgetId);
            if(data == null) continue;

            Log.v(TAG, String.format("change widgetData widgetLayout(%d)", widgetId));
            data.changeLayout(layoutResource);
            data.update();
        }
    }

    /**
     * Actor task request (disables all widgets)
     * @param adapterId
     * @param actorId
     */
    private void changeWidgetActorRequest(String adapterId, String actorId) {
        Log.d(TAG, String.format("changeWidgetActorRequest(%s, %s)", adapterId, actorId));
        if(actorId == null || adapterId == null || actorId.isEmpty() || adapterId.isEmpty()) return;

        // ----- first we need to check what widgets have this actor
        for (int i = 0; i < mAvailableWidgets.size(); i++) {
            WidgetData data = mAvailableWidgets.valueAt(i);
            // for now there are only possible widget devices
            if(!(data instanceof WidgetDeviceData)) continue;

            WidgetDeviceData widgetData = (WidgetDeviceData) data;
            WidgetDevice wDev = widgetData.widgetDevice;
            if(!adapterId.equals(wDev.adapterId) || !actorId.equals(wDev.getId())) continue;

            // first disables so that nobody can change it anymore
            widgetData.widgetDevice.setSwitchDisabled(true);
            widgetData.updateAppWidget();
        }

        // ----- then get the device, change value and run asyncTask
        final Device device = mController.getFacilitiesModel().getDevice(adapterId, actorId);

        // SET NEW VALUE
        BaseValue value = device.getValue();
        if (value instanceof BaseEnumValue) {
            ((BaseEnumValue)value).setNextValue();
        } else {
            Log.e(TAG, "We can't switch actor, which value isn't inherited from BaseEnumValue, yet");
            return;
        }

        ActorActionTask mActorActionTask = new ActorActionTask(mContext);
        mActorActionTask.setListener(new CallbackTask.CallbackTaskListener() {
            @Override
            public void onExecute(boolean success) {
                Log.v(TAG, "Actor's async task end with " + String.valueOf(success));
                // we don't have any response here, cause that manages received broadcast
            }
        });

        mActorActionTask.execute(device);
    }

    /**
     * Actor task result (this is called by broadcast receiver) -> goes through all widgets
     * @param adapterId
     * @param actorId
     */
    private void changeWidgetActorResult(String adapterId, String actorId){
        Log.d(TAG, String.format("changeWidgetActorResult(%s, %s)", adapterId, actorId));
        if(actorId == null || adapterId == null || actorId.isEmpty() || adapterId.isEmpty()) return;

        for (int i = 0; i < mAvailableWidgets.size(); i++) {
            WidgetData data = mAvailableWidgets.valueAt(i);
            // for now there are only possible widget devices
            if(!(data instanceof WidgetDeviceData)) continue;

            WidgetDeviceData widgetData = (WidgetDeviceData) data;
            WidgetDevice wDev = widgetData.widgetDevice;
            if(!adapterId.equals(wDev.adapterId) || !actorId.equals(wDev.getId())) continue;

            // first disables so that nobody can change it anymore
            wDev.setSwitchDisabled(false);
            widgetData.update();
        }
    }

    // -------------------------------------------------------------------- //
    // ------------------------- Managing methods ------------------------- //
    // -------------------------------------------------------------------- //

    /**
     * Set repeating to parameter
     * @param triggerAtMillis
     */
    private void setAlarm(long triggerAtMillis){
        // Set new alarm time
        AlarmManager m = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        m.set(AlarmManager.ELAPSED_REALTIME, triggerAtMillis, getPendingIntentUpdate(mContext));
    }

    /**
     * Stops repeating
     * @param context
     */
    private static void stopAlarm(Context context){
        // cancel frequently refreshing
        AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        m.cancel(getPendingIntentUpdate(context));
    }

    /**
     * Insert widgetData to list of used widgets
     * @param widgetData
     */
    private void widgetAdd(WidgetData widgetData) {
        // add widgetData to service
        mAvailableWidgets.put(widgetData.getWidgetId(), widgetData);
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
    private void widgetDelete(Context context, WidgetData data){
        if(data == null) return;
        int widgetId = data.getWidgetId();
        Log.v(TAG, String.format("delete widgetData(%d)", widgetId));

        data.delete(context);
        mAvailableWidgets.delete(widgetId);
    }

    /**
     * Gets widget data either from list of widgets or instantiating and loading from settings (when OS restarted)
     * @param widgetId id of widget to load
     * @return
     */
    public WidgetData getWidgetData(int widgetId){
        WidgetData widgetData = mAvailableWidgets.get(widgetId);
        if (widgetData == null) {
            String widgetClassName = WidgetData.getSettingClassName(mContext, widgetId);
            if (widgetClassName == null || widgetClassName.isEmpty()) return null;

            // TODO osetrit
            try {
                // instantiate class from string
                widgetData = (WidgetData) Class.forName(widgetClassName).getConstructor(int.class, Context.class, UnitsHelper.class, TimeHelper.class).newInstance(widgetId, mContext, mUnitsHelper, mTimeHelper);
                widgetData.init();
                widgetAdd(widgetData);
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

        // if none, tries the passed ids
        if(widgetIds == null || widgetIds.length == 0){
            widgetIds = appWidgetIds;
        }

        if(widgetIds != null) for (int widgetId : widgetIds) {
            WidgetData widgetData = getWidgetData(widgetId);
            // widget is not added yet (probably only configuration activity is showed)
            if (widgetData == null || !widgetData.widgetInitialized) continue;

            if (first) {
                minInterval = widgetData.widgetInterval;
                nextUpdate = widgetData.getNextUpdate(timeNow);
                first = false;
            } else {
                minInterval = Math.min(minInterval, widgetData.widgetInterval);
                nextUpdate = Math.min(nextUpdate, widgetData.getNextUpdate(timeNow));
            }
        }

        minInterval = Math.max(minInterval, UPDATE_INTERVAL_MIN);
        return first ? 0 : Math.max(nextUpdate, SystemClock.elapsedRealtime() + minInterval * 1000);
    }

    /**
     * !!! If we have any new widget, we need to add it here so that it keeps updating. !!!
     * @return array of widget ids
     */
    private int[] getAllIds() {
        List<Integer> ids = new ArrayList<>();

        // clock widget
        ids.addAll(getWidgetIds(WidgetClockProvider.class));
        // location list
        ids.addAll(getWidgetIds(WidgetLocationListProvider.class));
        // device widget
        ids.addAll(getWidgetIds(WidgetDeviceProvider.class));
        ids.addAll(getWidgetIds(WidgetDeviceProviderMedium.class));
        ids.addAll(getWidgetIds(WidgetDeviceProviderLarge.class));

        int[] arr = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            arr[i] = ids.get(i);
        }

        return arr;
    }

    // -------------------------------------------------------------------- //
    // -------------------------- Helpers methods ------------------------- //
    // -------------------------------------------------------------------- //

    /**
     * Initializes helpers classes when necessary (and unitialize them after not available)
     * @param context
     * @param controller
     */
    private void initHelpers(Context context, Controller controller){
        if(!controller.isLoggedIn()) return;

        SharedPreferences userSettings = controller.getUserSettings();
        if(userSettings == null){
            mUnitsHelper = null;
            mTimeHelper = null;
            return;
        }

        // if it is not initialized
        if(mUnitsHelper == null || mTimeHelper == null) {
            mUnitsHelper = new UnitsHelper(userSettings, context);
            mTimeHelper = new TimeHelper(userSettings);
        }
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // -------------------------------------------------------------------- //
    // ------------------------- Intent factories ------------------------- //
    // -------------------------------------------------------------------- //

    /**
     * Get intent of this class
     * @param context
     * @return
     */
    private static Intent getIntentUpdate(Context context) {
        return new Intent(context, WidgetService.class);
    }

    /**
     * Pending intent of this class
     * @param context
     * @return
     */
    private static PendingIntent getPendingIntentUpdate(Context context){
        final Intent intent = getIntentUpdate(context);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    /**
     * Force intent
     * @param context
     * @param widgetId
     * @return
     */
    public static Intent getIntentForceUpdate(Context context, int widgetId) {
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
    public static PendingIntent getPendingIntentForceUpdate(Context context, int widgetId) {
        final Intent intent = getIntentForceUpdate(context, widgetId);
        return PendingIntent.getService(context, widgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    /**
     * Intent for putting service asleep
     * @param context
     * @param standby
     * @return
     */
    public static Intent getIntentStandBy(Context context, boolean standby){
        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(WidgetService.EXTRA_STANDBY, standby);
        return intent;
    }

    /**
     * Intent for changing actor's state
     * @param context
     * @param actorId
     * @return
     */
    public static Intent getIntentActorChangeRequest(Context context, String actorId, String adapterId){
        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(WidgetService.EXTRA_ACTOR_CHANGE_REQUEST, true);
        intent.putExtra(EXTRA_ACTOR_ID, actorId);
        intent.putExtra(EXTRA_ACTOR_ADAPTER_ID, adapterId);
        return intent;
    }

    /**
     * Pending intent for "onclick" for changing actor's state
     * @param context
     * @param widgetId
     * @param actorId
     * @return
     */
    public static PendingIntent getPendingIntentActorChangeRequest(Context context, int widgetId, String actorId, String adapterId) {
        return getPendingIntentActor(context, widgetId, actorId, adapterId, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Cancels pendingIntent (that means, that actor cannot be clicked)
     * @param context
     * @param widgetId
     * @param actorId
     * @param adapterId
     * @return
     */
    public static PendingIntent cancelPendingIntentActorChangeRequest(Context context, int widgetId, String actorId, String adapterId) {
        return getPendingIntentActor(context, widgetId, actorId, adapterId, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    /**
     * General factory for when actor is changed
     * @param context
     * @param widgetId
     * @param actorId
     * @param adapterId
     * @param flag
     * @return
     */
    private static PendingIntent getPendingIntentActor(Context context, int widgetId, String actorId, String adapterId, int flag){
        Intent intent = getIntentActorChangeRequest(context, actorId, adapterId);
        // requestNum guarantees that PendingIntent is unique
        int requestNum = widgetId + adapterId.hashCode() + actorId.hashCode();
        return PendingIntent.getService(context, requestNum, intent, flag);
    }

    /**
     * Intent for running result of actor change
     * @param context
     * @param adapterId
     * @param actorId
     * @return
     */
    public static Intent getIntentActorChangeResult(Context context, String adapterId, String actorId){
        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(WidgetService.EXTRA_ACTOR_CHANGE_RESULT, true);
        intent.putExtra(EXTRA_ACTOR_ID, actorId);
        intent.putExtra(EXTRA_ACTOR_ADAPTER_ID, adapterId);
        return intent;
    }

    /**
     * When widget is being deleted, calls service to clear data after it's deleted
     * @param context
     * @param widgetIds  is possible to delete more widgets at once
     * @return
     */
    public static Intent getIntentWidgetDelete(Context context, int[] widgetIds) {
        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(WidgetService.EXTRA_DELETE_WIDGET, true);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        return intent;
    }

    /**
     * When widget changes its size, calls service to refresh that widget's layout
     * @param context
     * @param widgetId
     * @param layout  new widget layout
     * @return
     */
    public static Intent getIntentWidgetChangeLayout(Context context, int widgetId, int layout){
        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(EXTRA_CHANGE_LAYOUT, true);
        intent.putExtra(EXTRA_CHANGE_LAYOUT_RESOURCE, layout);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{ widgetId });
        return intent;
    }
}
