package com.rehivetech.beeeon.widget.data;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.widget.RemoteViews;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.SensorDetailActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.widget.configuration.WidgetConfiguration;
import com.rehivetech.beeeon.widget.configuration.WidgetConfigurationActivity;
import com.rehivetech.beeeon.widget.service.WidgetService;

import java.util.List;

/**
 * @author mlyko
 */
abstract public class WidgetData {
    private static final String TAG = WidgetData.class.getSimpleName();

    public static final String EXTRA_WIDGET_ID = "com.rehivetech.beeeon.widget.widget_id";

    protected static final String PREF_FILENAME = "widget_%d";
    protected static final String PREF_CLASS_NAME = "widget_class_name";
    protected static final String PREF_LAYOUT = "widget_layout";
    protected static final String PREF_INTERVAL = "widget_interval";
    protected static final String PREF_LAST_UPDATE = "widget_last_update";
    protected static final String PREF_INITIALIZED = "widget_initialized";
    protected static final String PREF_ADAPTER_ID = "adapter_id";
    private static final String PREF_USER_ID = "widget_user_id";

    private String mUserId;
    protected final int mWidgetId;

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
    protected RemoteViews mRemoteViews;
    protected SharedPreferences mPrefs;
    protected UnitsHelper mUnitsHelper;
    protected TimeHelper mTimeHelper;

    protected PendingIntent mRefreshPendingIntent;
    protected PendingIntent mConfigurationPendingIntent;

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
    }

    /**
     * Initialize method called after constructor in service
     * NOTE: created this way because of complicated constructor calls
     */
    public void init(){
        Log.d(TAG, "init()");
        initLayout();
    }

    /**
     * Configuration activity calls this when finished
     * If any property is not save() it won't last cause object is destroyed after configuration activity
     * @param isEditing
     * @param interval  updating interval
     * @param aId   adapter id
     */
    public void configure(boolean isEditing, int interval, String aId){
        Log.d(TAG, String.format("configure(%b)", isEditing));

        widgetLastUpdate = 0;
        widgetInitialized = true;
        widgetInterval = interval;
        adapterId = aId;

        this.save();
    }

    /**
     * After configuration activity if editing, this reloads already created widget
     */
    public void reload(){
        Log.d(TAG, "reload()");
        this.load();
        initLayout();
    }

    /**
     * Gets new data and updates widget
     */
    public void update() {
        Log.d(TAG, "update()");
        isCached = false;
        // change actual widget's data
        if(!updateData()){
            isCached = true;
        }

        // Update widget
        updateLayout();

        // request widget redraw
        updateAppWidget();
    }

    /**
     * Deletes all settings (even childrens)
     */
    public void delete(Context context){
        mPrefs.edit().clear().apply();
    }

    /**
     * Load all data of this widget
     */
    protected void load() {
        Log.d(TAG, "load()");
        // set default widget data
        widgetLayout = mPrefs.getInt(PREF_LAYOUT, mWidgetProviderInfo != null ? mWidgetProviderInfo.initialLayout : 0); // TODO sometimes providerInfo is null
        widgetInterval = mPrefs.getInt(PREF_INTERVAL, WidgetService.UPDATE_INTERVAL_DEFAULT);
        widgetLastUpdate = mPrefs.getLong(PREF_LAST_UPDATE, 0);
        widgetInitialized = mPrefs.getBoolean(PREF_INITIALIZED, false);
        // all widgets has these params
        adapterId = mPrefs.getString(PREF_ADAPTER_ID, "");
        mUserId = mPrefs.getString(PREF_USER_ID, mController.getActualUser().getId());
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
    }

    /**
     * Initializes remoteViews, click listeners, etc
     */
    protected void initLayout(){
        Log.d(TAG, "initLayout()");
        mRemoteViews = new RemoteViews(mContext.getPackageName(), this.widgetLayout);

        // refresh onclick
        mRefreshPendingIntent = WidgetService.getForceUpdatePendingIntent(mContext, mWidgetId);

        // configuration onclick
        mConfigurationPendingIntent = startConfigurationActivityPendingIntent(mContext, mWidgetId);
    }

    /**
     * Changes widgetLayout of this widget and initializes it again
     * @param layout widgetLayout resource
     */
    public void changeLayout(int layout) {
        this.widgetLayout = layout;
        save();
        initLayout();
    }

    /**
     * Request widget redraw
     */
    public void updateAppWidget(){
        mWidgetManager.updateAppWidget(mWidgetId, mRemoteViews);
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

    protected abstract void updateLayout();

    public void asyncTask(Object obj){
    }

    /**
     * Handle when user goes online
     */
    public void handleUserLogin() {
    }

    /**
     * Handle when user goes offline (or connection not available)
     * e.g. Load cached data
     */
    public void handleUserLogout() {
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
     * @param requestCode must be unique if want to open different detail activity
     * @param adapterId
     * @param deviceId
     * @return
     */
    public static PendingIntent startDetailActivityPendingIntent(Context context, int requestCode, String adapterId, String deviceId) {
        Intent intent = new Intent(context, SensorDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SensorDetailActivity.EXTRA_DEVICE_ID, deviceId);
        intent.putExtra(SensorDetailActivity.EXTRA_ADAPTER_ID, adapterId);
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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

    public abstract WidgetConfiguration createConfiguration(WidgetConfigurationActivity activity, boolean isWidgetEditing);
}
