package com.rehivetech.beeeon.widget.data;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.widget.RemoteViews;

import com.rehivetech.beeeon.activity.MainActivity;
import com.rehivetech.beeeon.activity.SensorDetailActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.widget.ViewsBuilder;
import com.rehivetech.beeeon.widget.configuration.WidgetConfigurationActivity;
import com.rehivetech.beeeon.widget.persistence.WidgetDevicePersistence;
import com.rehivetech.beeeon.widget.persistence.WidgetSettings;
import com.rehivetech.beeeon.widget.service.WidgetService;

import java.util.List;

/**
 * @author mlyko
 */
abstract public class WidgetData {
    private static final String TAG = WidgetData.class.getSimpleName();

    public static final String EXTRA_WIDGET_ID = "com.rehivetech.beeeon.widget.widget_id";

    public static final String PREF_FILENAME = "widget_%d";
    protected static final String PREF_CLASS_NAME = "widget_class_name";
    protected static final String PREF_LAYOUT = "widget_layout";
    protected static final String PREF_INTERVAL = "widget_interval";
    protected static final String PREF_LAST_UPDATE = "widget_last_update";
    protected static final String PREF_INITIALIZED = "widget_initialized";
    protected static final String PREF_ADAPTER_ID = "adapter_id";
    private static final String PREF_USER_ID = "widget_user_id";

    private String mUserId;
    protected final int mWidgetId;
    public List<WidgetDevicePersistence> widgetDevices;

    public int widgetLayout;
    public int widgetInterval;
    public long widgetLastUpdate;
    public boolean widgetInitialized;
    public String adapterId;

    public boolean isCached = true;

    protected Context mContext;
    protected Controller mController;
    protected AppWidgetManager mWidgetManager;
    protected AppWidgetProviderInfo mWidgetProviderInfo;
    protected SharedPreferences mPrefs;
    protected UnitsHelper mUnitsHelper;
    protected TimeHelper mTimeHelper;

    protected PendingIntent mRefreshPendingIntent;
    protected PendingIntent mConfigurationPendingIntent;
    protected ViewsBuilder mBuilder;

    public WidgetSettings settings;

    /**
     * Constructor for configuration activity
     * @param widgetId
     * @param context
     */
    public WidgetData(final int widgetId, Context context, UnitsHelper unitsHelper, TimeHelper timeHelper){
        mWidgetId = widgetId;
        mContext = context.getApplicationContext();
        mWidgetManager = AppWidgetManager.getInstance(mContext.getApplicationContext());
        mWidgetProviderInfo = mWidgetManager.getAppWidgetInfo(mWidgetId);
        mController = Controller.getInstance(mContext);
        mPrefs = getSettings(mContext, mWidgetId);
        mUnitsHelper = unitsHelper;
        mTimeHelper = timeHelper;

        mBuilder = new ViewsBuilder(mContext);
        settings = WidgetSettings.getSettings(mContext, mWidgetId);
    }

    /**
     * Initialize method called after constructor in service
     * NOTE: created this way because of complicated constructor calls
     */
    public abstract void init();

    /**
     * Configuration activity calls this when finished
     * If any property is not save() it won't last cause object is destroyed after configuration activity
     * @param adapter
     * @param isEditing
     * @param interval  updating interval
     * @param adapter
     */
    public void configure(boolean isEditing, int interval, Adapter adapter){
        Log.d(TAG, String.format("configure(%b)", isEditing));

        widgetLastUpdate = 0;
        widgetInitialized = true;
        widgetInterval = interval;
        adapterId = adapter.getId();

        this.save();
    }

    /**
     * After configuration activity if editing, this reloads already created widget
     */
    public void reload(){
        Log.d(TAG, "reload()");
        this.load();
        init();
    }

    /**
     * Gets new data and updates widget
     * CALLED ONLY WHEN CONNECTION TO THE SERVER AVAILABLE
     */
    public void update() {
        Log.d(TAG, "update()");

        // change actual widget's data
        if(!updateData()){
            // TODO
        }

        // Update widget
        renderLayout();

        // request widget redraw
        renderAppWidget();
    }

    /**
     * Deletes all settings (even childrens)
     */
    public void delete(Context context){
        mPrefs.edit().clear().apply();
        settings.delete();
    }

    /**
     * Load all data of this widget
     */
    public void load() {
        Log.d(TAG, "load()");
        // set default widget data
        widgetLayout = mPrefs.getInt(PREF_LAYOUT, mWidgetProviderInfo != null ? mWidgetProviderInfo.initialLayout : 0); // TODO sometimes providerInfo is null
        widgetInterval = mPrefs.getInt(PREF_INTERVAL, WidgetService.UPDATE_INTERVAL_DEFAULT);
        widgetLastUpdate = mPrefs.getLong(PREF_LAST_UPDATE, 0);
        widgetInitialized = mPrefs.getBoolean(PREF_INITIALIZED, false);
        // all widgets has these params
        adapterId = mPrefs.getString(PREF_ADAPTER_ID, "");
        mUserId = mPrefs.getString(PREF_USER_ID, mController.getActualUser().getId());
        // color schemes of widgets (not all widgets use this, but could in the future)
        settings.load();
    }

    /**
     * Save all data of this widget
     */
    protected void save() {
        Log.d(TAG, "save()");
        mPrefs.edit()
                .putString(PREF_CLASS_NAME, getClassName())
                .putInt(PREF_LAYOUT, widgetLayout)
                .putInt(PREF_INTERVAL, widgetInterval)
                .putLong(PREF_LAST_UPDATE, widgetLastUpdate)
                .putString(PREF_ADAPTER_ID, adapterId)
                .putString(PREF_USER_ID, mUserId)
                .putBoolean(PREF_INITIALIZED, widgetInitialized)
                .apply();

        settings.save();
    }

    /**
     * Initializes remoteViews, click listeners, etc
     */
    public void initLayout(){
        Log.d(TAG, "initLayout()");

        mBuilder.loadRootView(this.widgetLayout);

        // refresh onclick
        mRefreshPendingIntent = WidgetService.getPendingIntentForceUpdate(mContext, mWidgetId);

        // configuration onclick
        mConfigurationPendingIntent = startConfigurationActivityPendingIntent(mContext, mWidgetId);
    }

    /**
     * Changes widgetLayout of this widget and initializes it again
     * @param layout widgetLayout resource
     */
    public void changeLayout(int layout) {
        Log.v(TAG, String.format("changeLayout(%d)", mWidgetId));
        this.widgetLayout = layout;
        save();
        //init();
        initLayout();
    }

    /**
     * Request widget redraw
     */
    public void renderAppWidget(){
        mWidgetManager.updateAppWidget(mWidgetId, mBuilder.getRoot());
    }

    /**
     * Used for passing objects to service so that it can refresh them (list because some widgets can have more of them)
     * @return
     */
    public abstract List<?> getReferredObj();

    /**
     * Tries to update data
     * @return if any data were updated
     */
    protected abstract boolean updateData();

    protected abstract void renderLayout();

    /**
     * Handle when user goes online
     */
    public void handleUserLogin() {
        isCached = false;
    }

    /**
     * Handle when user goes offline (or connection not available)
     * e.g. Load cached data
     */
    public void handleUserLogout() {
        isCached = true;
    }

    /**
     * PendingIntent for opening configuration activity for widget
     * @param context
     * @param widgetId
     * @return
     */
    protected static PendingIntent startConfigurationActivityPendingIntent(Context context, int widgetId){
        Intent intent = new Intent(context, WidgetConfigurationActivity.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.putExtra(WidgetConfigurationActivity.EXTRA_WIDGET_EDITING, true);
        return PendingIntent.getActivity(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * PendingIntent for opening detail of device
     * @param context
     * @param widgetId
     * @param adapterId
     * @param deviceId
     * @return
     */
    public static PendingIntent startDetailActivityPendingIntent(Context context, int widgetId, String adapterId, String deviceId) {
        Intent intent = startDetailActivityIntent(context, adapterId, deviceId);
        int requestNum = widgetId + adapterId.hashCode() + deviceId.hashCode();
        return PendingIntent.getActivity(context, requestNum, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static Intent startDetailActivityIntent(Context context, String adapterId, String deviceId){
        Intent intent = new Intent(context, SensorDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SensorDetailActivity.EXTRA_DEVICE_ID, deviceId);
        intent.putExtra(SensorDetailActivity.EXTRA_ADAPTER_ID, adapterId);
        return intent;
    }

    /**
     * Starts main activity of the application
     * @param context
     * @return
     */
    public static PendingIntent startMainActivityPendingIntent(Context context, String adapterId){
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(MainActivity.ADAPTER_ID, adapterId);

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Returns className by widget id (for instantiating proper widget class)
     * @param context
     * @param widgetId
     * @return
     */
    public static String getSettingClassName(Context context, int widgetId){
        SharedPreferences prefs = getSettings(context, widgetId);
        if(prefs == null) return null;
        return prefs.getString(PREF_CLASS_NAME, "");
    }

    /**
     * Return SharedPreferences for widget
     * NOTE: We don't use Controller to get settings, because widgets doesn't depend on logged in user.
     *
     * @param context
     * @return
     */
    protected static SharedPreferences getSettings(Context context, int widgetId){
        return context.getSharedPreferences(String.format(PREF_FILENAME, widgetId), Context.MODE_PRIVATE);
    }

    /**
     * Returns widgetId for reading only
     * @return
     */
    public int getWidgetId() {
        return mWidgetId;
    }

    /**
     * Because we're saving SystemClock.elapsedRealtime() value, which is time since phone boot,
     * it will be incorrect after phone reboot. This method tries to fix that by reseting widgetLastUpdate time
     * when it is greater than actual SystemClock.elapsedRealtime() value.
     *
     * @param now Actual SystemClock.elapsedRealtime() value
     */
    private void fixLastUpdate(long now) {
        if (widgetLastUpdate > now) {
            widgetLastUpdate = 0;
        }
    }

    /**
     * Checks if widget is expired and should be redrawn
     *
     * @param now Actual SystemClock.elapsedRealtime() value to compare
     * @return true if next update time is in the past (or <1000ms in future from now)
     */
    public boolean isExpired(long now) {
        fixLastUpdate(now);
        return (widgetLastUpdate + widgetInterval * 1000) - now <= 1000;
    }

    /**
     * Calculates time of next update
     *
     * @param now
     *            Actual SystemClock.elapsedRealtime() value to compare
     * @return
     */
    public long getNextUpdate(long now) {
        fixLastUpdate(now);
        return widgetLastUpdate > 0 ? widgetLastUpdate + widgetInterval * 1000 : now;
    }

    protected long getTimeNow(){
        return SystemClock.elapsedRealtime();
    }

    public abstract String getClassName();

    public static RemoteViews buildView(Context context, RemoteViews parentView, int boundView, int layout){
        RemoteViews rv = new RemoteViews(context.getPackageName(), layout);
        parentView.removeAllViews(boundView);
        parentView.addView(boundView, rv);

        return rv;
    }

    /**
     * Known packages for clock applications
     * Got from http://stackoverflow.com/questions/4115649/
     */
    public static String[] CLOCK_PACKAGES = {
            // HTC
            "com.htc.android.worldclock.TimerAlert",//
            "com.htc.android.worldclock.AlarmAlert",//

            // Samsung
            "com.sec.android.app.clockpackage.ClockPackage",//
            "com.sec.android.app.clockpackage.alarm.AlarmAlert",//

            // Motorola
            "com.motorola.blur.alarmclock.AlarmAlert",//
            "com.motorola.blur.alarmclock.AlarmClock",//
            "com.motorola.blur.alarmclock.AlarmTimerAlert",

            // Stock Android Clock
            "com.android.alarmclock.AlarmClock",// 1.5 / 1.6
            "com.android.deskclock.DeskClock",//

            // Sony Ericsson XPERIA X10 Mini Pro, Android 2.1:
            "com.sonyericsson.alarm",
            "com.sonyericsson.alarm.Alarm",

            // Samsung Galaxy S Vibrant, Android 2.2:
            "com.sec.android.app.clockpackage",
            "com.sec.android.app.clockpackage.ClockPackage",

            // Stock Clock, Android 2.1:
            "com.android.alarmclock.AlarmClock",

            // Stock Clock, Android 2.2:
            "com.android.alarmclock.AlarmClock",

            // Stock Clock, Android 2.3 (2.3.1):
            "com.android.deskclock.DeskClock",

            // Stock Clock, Android 2.3.3:
            "com.android.deskclock.DeskClock"
    };

    public static Intent getDefaultClockIntent(Context context) {
        PackageManager pm = context.getPackageManager();
        for (String packageName : CLOCK_PACKAGES) {
            try {
                pm.getPackageInfo(packageName, 0);
                return pm.getLaunchIntentForPackage(packageName);
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        return null;
    }
}
