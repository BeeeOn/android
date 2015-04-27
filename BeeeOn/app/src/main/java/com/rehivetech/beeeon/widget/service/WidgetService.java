package com.rehivetech.beeeon.widget.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.SparseArray;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.asynctask.ActorActionTask;
import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ErrorCode;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.BooleanValue;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.data.WidgetClockData;
import com.rehivetech.beeeon.widget.data.WidgetData;
import com.rehivetech.beeeon.widget.persistence.WidgetDevicePersistence;
import com.rehivetech.beeeon.widget.receivers.WidgetClockProvider;
import com.rehivetech.beeeon.widget.receivers.WidgetDeviceProvider;
import com.rehivetech.beeeon.widget.receivers.WidgetDeviceProviderLarge;
import com.rehivetech.beeeon.widget.receivers.WidgetDeviceProviderMedium;
import com.rehivetech.beeeon.widget.receivers.WidgetGraphProvider;
import com.rehivetech.beeeon.widget.receivers.WidgetLocationListProvider;
import com.rehivetech.beeeon.widget.receivers.WidgetProvider;

import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author mlyko
 */
public class WidgetService extends Service {
    private final static String TAG = WidgetService.class.getSimpleName();

    public static final RefreshInterval UPDATE_INTERVAL_DEFAULT = RefreshInterval.SEC_30;
    public static final RefreshInterval UPDATE_INTERVAL_MIN = RefreshInterval.SEC_10;
    public static final RefreshInterval UPDATE_INTERVAL_WEATHER_MIN = RefreshInterval.MIN_30;

    private static final String EXTRA_START_UPDATING =              "com.rehivetech.beeeon.start_updating";
    private static final String EXTRA_FORCE_UPDATE =                "com.rehivetech.beeeon.force_update";
    private static final String EXTRA_ACTOR_CHANGE_REQUEST =        "com.rehivetech.beeeon.actor_change_request";
    private static final String EXTRA_ACTOR_CHANGE_RESULT =         "com.rehivetech.beeeon.actor_change_result";
    private static final String EXTRA_ACTOR_ID =                    "com.rehivetech.beeeon.actor_ids";
    private static final String EXTRA_DELETE_WIDGET =               "com.rehivetech.beeeon.delete_widget";
    private static final String EXTRA_CHANGE_LAYOUT =               "com.rehivetech.beeeon.change_layout";
    private static final String EXTRA_CHANGE_LAYOUT_MIN_WIDTH =     "com.rehivetech.beeeon.change_layout_min_width";
    private static final String EXTRA_CHANGE_LAYOUT_MIN_HEIGHT =    "com.rehivetech.beeeon.change_layout_min_height";
    private static final String EXTRA_WIDGETS_SHOULD_RELOAD =       "com.rehivetech.beeeon.widget_should_reload";
    private static final String EXTRA_ACTOR_ADAPTER_ID =            "com.rehivetech.beeeon.actor_adapter_id";

    // when finding all widgets with the same actor
    private static final int UPDATE_LAYOUT = 0;
    private static final int UPDATE_WHOLE = 1;

    // list of available widgets
    // TODO maybe make as static and public method for getting only "now" available widgets
    private SparseArray<WidgetData> mAvailableWidgets = new SparseArray<>();

    private Context mContext;
    private Controller mController;
    private AppWidgetManager mAppWidgetManager;
    // helpers
    private UnitsHelper mUnitsHelper;
    private TimeHelper mTimeHelper;

    private Calendar mCalendar;

    private boolean isServiceRunning = false;

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

    private final static IntentFilter sIntentFilter;
    static{
        sIntentFilter = new IntentFilter();
        sIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
        sIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        sIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        sIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        sIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        sIntentFilter.addAction(Intent.ACTION_LOCALE_CHANGED);
        sIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        // --- beeeOn broadcasts
        sIntentFilter.addAction(ActorActionTask.ACTION_ACTOR_CHANGED);
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
        mCalendar = Calendar.getInstance(mContext.getResources().getConfiguration().locale);

        // Creates broadcast receiver which bridges broadcasts to appwidgets for handling time and screen
        Log.v(TAG, "registeringBroadcastReceiver()");

        registerReceiver(mServiceReceiver, sIntentFilter);
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
        unregisterReceiver(mServiceReceiver);
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

        boolean isStartUpdating = false, isForceUpdate = false;
        int[] appWidgetIds = new int[]{};

        if(intent != null) {
            // -------------- get widget Ids
            appWidgetIds  = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

            // -------------- async task for changing widget actor
            boolean isActorChangeRequest = intent.getBooleanExtra(EXTRA_ACTOR_CHANGE_REQUEST, false);
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
            boolean isActorChangeResult = intent.getBooleanExtra(EXTRA_ACTOR_CHANGE_RESULT, false);
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

            // -------------- widget deletion
            boolean isDeleteWidget = intent.getBooleanExtra(EXTRA_DELETE_WIDGET, false);
            if(isDeleteWidget){
                widgetsDelete(appWidgetIds);
            }

            // -------------- force update
            isForceUpdate = intent.getBooleanExtra(EXTRA_FORCE_UPDATE, false);

            // -------------- start updating
            isStartUpdating = intent.getBooleanExtra(EXTRA_START_UPDATING, false);
            if(isStartUpdating){
                isForceUpdate = true;
            }
        }

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

        final boolean forceUpdating = isForceUpdate;
        final int[] updatingWidgetIds = appWidgetIds;

        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWidgets(intent, forceUpdating, updatingWidgetIds);
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
    private void updateWidgets(Intent intent, boolean isForceUpdate, int[] allWidgetIds) {
        Log.d(TAG, "updateWidgets()");

        // if there are no widgets passed by intent, try to get all widgets
        if(allWidgetIds == null || allWidgetIds.length == 0) allWidgetIds = getAllIds();

        boolean isShouldReload = false, isChangeLayout = false;
        int layoutMinWidth = 0, layoutMinHeight = 0;

        if(intent != null){
            // -------------- reload widget if necessary
            isShouldReload = intent.getBooleanExtra(EXTRA_WIDGETS_SHOULD_RELOAD, false);
            // -------------- onAppWidgetOptionsChanged (changing widgetLayout)
            isChangeLayout = intent.getBooleanExtra(EXTRA_CHANGE_LAYOUT, false);
            layoutMinWidth = intent.getIntExtra(EXTRA_CHANGE_LAYOUT_MIN_WIDTH, 0);
            layoutMinHeight = intent.getIntExtra(EXTRA_CHANGE_LAYOUT_MIN_HEIGHT, 0);
        }

        long timeNow = SystemClock.elapsedRealtime();
        SparseArray<WidgetData> widgetsToUpdate = new SparseArray<>();
        List<Object> widgetReferredObjects = new ArrayList<>();

        // update all widgets
        // NOTE: can't contain any network request !
        for(int widgetId : allWidgetIds) {
            // ziska bud z pole pouzitych dat nebo instanciuje z disku
            WidgetData widgetData = getWidgetData(widgetId);
            if(widgetData == null){
                Log.w(TAG, String.format("WidgetData(%d) doesn't exist & couldn't be created", widgetId));
                continue;
            }

            // reload data (after configuration and if objects already exists)
            if(isShouldReload){
                widgetData.reload();
            }

            // creates new RemoteViews and sets every pendingIntents etc
            widgetData.initLayout();

            // updates layout for this widget
            if(isChangeLayout){
                widgetData.handleResize(layoutMinWidth, layoutMinHeight);
                isForceUpdate = true;
                widgetData.renderAppWidget();
            }

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

            // TODO group it
            if(widgetData instanceof WidgetClockData){
                this.updateWeatherData((WidgetClockData) widgetData);
            }
        }

        try{
            mController.beginPersistentConnection();

            // Reload adapters to have data about Timezone offset
            mController.getAdaptersModel().reloadAdapters(false);

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

    private void updateWeatherData(final WidgetClockData widgetData){
        Log.v(TAG, "updating clock weather....");
        // TODO should check if city changed

        if(widgetData.weather.cityName.isEmpty()) return;

        new Thread(){
            public void run(){
                final JSONObject json = RemoteWeatherFetch.getJSON(mContext, widgetData.weather.cityName);
                if(json == null){
                    Log.i(TAG, mContext.getString(R.string.place_not_found));
                } else {
                    widgetData.updateWeather(json);
                    widgetData.renderWeather();
                    widgetData.renderAppWidget();
                }
            }
        }.start();
    }

    /**
     * Actor task request (disables all widgets)
     * @param adapterId
     * @param actorId
     */
    private void changeWidgetActorRequest(final String adapterId, final String actorId) {
        Log.d(TAG, String.format("changeWidgetActorRequest(%s, %s)", adapterId, actorId));
        if(actorId == null || adapterId == null || actorId.isEmpty() || adapterId.isEmpty()) return;

        // ----- first get the device and change value
        final Device device = mController.getFacilitiesModel().getDevice(adapterId, actorId);
        if(device == null || !device.getType().isActor()){
            Log.e(TAG, "DEVICE NOT actor OR NOT FOUND --> probably need to refresh controller");
            return;
        }

        // ----- check if value is boolean
        BaseValue value = device.getValue();
        if (!(value instanceof BooleanValue)) {
            Log.e(TAG, "We can't switch actor, which value isn't inherited from BaseEnumValue, yet");
            return;
        }

        BooleanValue boolVal = (BooleanValue) value;
        final boolean oldValue = boolVal.isActiveValue(BooleanValue.TRUE);
        // ----- then temporary set new value
        boolVal.setNextValue();
        final boolean newValue = boolVal.isActiveValue(BooleanValue.TRUE);

        // ----- then we need to check what widgets have this actor
        performWidgetActorChange(UPDATE_LAYOUT, adapterId, actorId, true, newValue);

        // ----- and finally run asyncTask
        final ActorActionTask actorActionTask = new ActorActionTask(mContext);
        actorActionTask.setListener(new CallbackTask.CallbackTaskListener() {
            @Override
            public void onExecute(boolean success) {
                // NOTE: we don't have any response here, cause that manages received broadcast
                if (!success) {
                    // if not successfull, put back the state
                    performWidgetActorChange(UPDATE_LAYOUT, adapterId, actorId, false, oldValue);
                }
            }
        });
        actorActionTask.execute(device);
    }

    /**
     * Actor task result (this is called by broadcast receiver) -> goes through all widgets
     * @param adapterId
     * @param actorId
     */
    private void changeWidgetActorResult(String adapterId, String actorId){
        Log.d(TAG, String.format("changeWidgetActorResult(%s, %s)", adapterId, actorId));
        if(actorId == null || adapterId == null || actorId.isEmpty() || adapterId.isEmpty()) return;

        // does not set value cause updates whole widget (and there asks for new value)
        performWidgetActorChange(UPDATE_WHOLE, adapterId, actorId, false, false);
    }


    // -------------------------------------------------------------------- //
    // ------------------------- Managing methods ------------------------- //
    // -------------------------------------------------------------------- //

    /**
     * Goes through all widgets, find widgets with the specified actor
     * and perform partial (only changes layout) or whole update
     * @param perform       UPDATE_LAYOUT or UPDATE_WHOLE
     * @param adapterId
     * @param actorId
     * @param isDisabled    if should be switch disabled
     * @param isValueOn     when UPDATE_WHOLE this does nothing
     */
    private void performWidgetActorChange(int perform, String adapterId, String actorId, boolean isDisabled, boolean isValueOn){
        if(actorId == null || adapterId == null || actorId.isEmpty() || adapterId.isEmpty()) return;

        for (int i = 0; i < mAvailableWidgets.size(); i++) {
            WidgetData data = mAvailableWidgets.valueAt(i);
            // skips not compatible widgets
            if(data.widgetDevices == null || data.widgetDevices.isEmpty()) continue;

            int updatedActors = 0;
            // go through all devices in that widget
            for(WidgetDevicePersistence wDev : data.widgetDevices){
                if(!adapterId.equals(wDev.getAdapterId()) || !actorId.equals(wDev.getId())) continue;

                if(perform == UPDATE_LAYOUT){
                    wDev.setSwitchChecked(isValueOn);
                }

                // disables so that nobody can change it anymore
                wDev.setSwitchDisabled(isDisabled);
                updatedActors++;
            }

            // if any actor found, update whole widget
            if(updatedActors > 0){
                if(perform == UPDATE_LAYOUT)
                    data.renderAppWidget();
                else
                    data.update();
            }
        }
    }

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
            try {
                // instantiate class from string
                widgetData = (WidgetData) Class.forName(widgetClassName).getConstructor(int.class, Context.class, UnitsHelper.class, TimeHelper.class).newInstance(widgetId, mContext, mUnitsHelper, mTimeHelper);
                widgetData.load();
                // TODO nejak zobecnit?
                if(widgetData instanceof WidgetClockData){
                    ((WidgetClockData) widgetData).injectObject(mCalendar);
                }
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

        minInterval = Math.max(minInterval, UPDATE_INTERVAL_MIN.getInterval());
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
        // graph widget
        ids.addAll(getWidgetIds(WidgetGraphProvider.class));

        int[] arr = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            arr[i] = ids.get(i);
        }

        return arr;
    }

    /**
     * Gets all active clock widgets and update their time
     * Also sets time in calendar to current
     */
    private void updateClockWidgets(){
        List<Integer> clockIds = getWidgetIds(WidgetClockProvider.class);
        mCalendar.setTime(new Date());
        for(Integer clockId : clockIds){
            WidgetData data = mAvailableWidgets.get(clockId);
            if(data == null || !(data instanceof WidgetClockData)) continue;
            ((WidgetClockData) data).onUpdateClock(mCalendar);
        }
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
        List<Integer> arr = new ArrayList<>();
        for(int i : WidgetProvider.getWidgetIdsByClass(mContext, cls)){
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
        return PendingIntent.getService(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
     * @return
     */
    public static Intent getIntentWidgetChangeLayout(Context context, int widgetId, int minWidth, int minHeight){
        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(EXTRA_CHANGE_LAYOUT, true);
        intent.putExtra(EXTRA_CHANGE_LAYOUT_MIN_WIDTH, minWidth);
        intent.putExtra(EXTRA_CHANGE_LAYOUT_MIN_HEIGHT, minHeight);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{ widgetId });
        return intent;
    }

    // -------------------------------------------------------------------- //
    // ------------------------- Broadcast receiver ----------------------- //
    // -------------------------------------------------------------------- //

    private final BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String receivedAction = intent.getAction();
            Log.d(TAG, "broadcastReceived: " + receivedAction);

            // if time was changed (any way), update clock widgets
            if (receivedAction.equals(Intent.ACTION_TIME_TICK) ||
                receivedAction.equals(Intent.ACTION_TIME_CHANGED) ||
                receivedAction.equals(Intent.ACTION_TIMEZONE_CHANGED)){
                updateClockWidgets();
            }
            else if(receivedAction.equals(Intent.ACTION_LOCALE_CHANGED)){
                mCalendar = Calendar.getInstance(mContext.getResources().getConfiguration().locale);
                WidgetClockData.reloadWeekDays();
                // TODO update clocks?
            }
            // if screen went on, update clocks + tell the service
            else if(receivedAction.equals(Intent.ACTION_SCREEN_ON)){
                updateClockWidgets();
                context.startService(WidgetService.getIntentUpdate(context));
            }
            // if screen went off, tell the service
            else if(receivedAction.equals(Intent.ACTION_SCREEN_OFF)){
                Log.d(TAG, "service getting standby...");
                stopAlarm(context);
            }
            // if any actor value was changed, tell the service to refresh widget with that device
            else if(receivedAction.equals(ActorActionTask.ACTION_ACTOR_CHANGED)){
                // TODO prepsat s kontextem...
                String adapterId = intent.getStringExtra(ActorActionTask.EXTRA_ACTOR_CHANGED_ADAPTER_ID);
                String actorId = intent.getStringExtra(ActorActionTask.EXTRA_ACTOR_CHANGED_ID);

                if(adapterId == null || adapterId.isEmpty() || actorId == null || actorId.isEmpty()) return;
                context.startService(WidgetService.getIntentActorChangeResult(context, adapterId, actorId));

			/*
			// update location widget if exists
			int[] locationWidgetsIds = WidgetProvider.getWidgetIdsByClass(context, WidgetLocationListProvider.class);
			if(locationWidgetsIds != null && locationWidgetsIds.length > 0){
				AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
				// TODO
				//widgetManager.notifyAppWidgetViewDataChanged();
			}
			//*/
            }
		/*
		// TODO somehow not getting isNoConnectivity
		else if(receivedAction.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
			boolean isNoConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
			boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
			Log.d(TAG, String.format("IS Connectivity = %b, isFailover = %b", isNoConnectivity, isFailover));
		}
		//*/

            // TODO mit zmenu stavu prihlaseni jako broadcast?
        }
    };
}
