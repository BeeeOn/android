package com.rehivetech.beeeon.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.SensorDetailActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;

/**
 * @author mlyko
 */
abstract public class WidgetData {
    private static final String TAG = WidgetData.class.getSimpleName();

    protected static final String PREF_FILENAME = "widget_%d";
    protected static final String PREF_CLASS_NAME = "class_name";
    protected static final String PREF_LAYOUT = "layout";
    protected static final String PREF_INTERVAL = "interval";
    protected static final String PREF_LAST_UPDATE = "lastUpdate";
    protected static final String PREF_INITIALIZED = "initialized";
    protected static final String PREF_ADAPTER_ID = "adapter_id";

    protected final int mWidgetId;
    public int layout;
    public int interval;
    public long lastUpdate;
    public boolean initialized;
    public String adapterId;

    public WidgetProvider widgetProvider;

    protected String mClassName;
    protected Context mContext;
    protected Controller mController;
    protected AppWidgetManager mWidgetManager;
    protected AppWidgetProviderInfo mWidgetProviderInfo;
    protected RemoteViews mRemoteViews;
    protected SharedPreferences mPrefs;
    protected SharedPreferences mUserSettings;
    protected UnitsHelper mUnitsHelper;
    protected TimeHelper mTimeHelper;

    protected PendingIntent mRefreshPendingIntent;
    protected PendingIntent mConfigurationPendingIntent;

    public WidgetData(final int widgetId, Context context){
        mWidgetId = widgetId;
        mContext = context.getApplicationContext();
        mController = Controller.getInstance(mContext);
        mWidgetManager = AppWidgetManager.getInstance(mContext.getApplicationContext());
        mWidgetProviderInfo = mWidgetManager.getAppWidgetInfo(widgetId);

        // TODO nevytvaret pro kazdy widget, ale ziskat z rodice
        mPrefs = getSettings(mContext);
        mUserSettings = mController.getUserSettings();
        mUnitsHelper = (mUserSettings == null) ? null : new UnitsHelper(mUserSettings, mContext);
        mTimeHelper = (mUserSettings == null) ? null : new TimeHelper(mUserSettings);

        // TODO zrusit loadData?
        loadData(mContext);

        mRemoteViews = new RemoteViews(context.getPackageName(), this.layout);
    }

    public void initLayout(){
        Log.d(TAG, "initLayout()");

        // refresh onclick
        mRefreshPendingIntent = WidgetService.getForceUpdatePendingIntent(mContext, mWidgetId);

        // configuration onclick
        Intent intent = new Intent(mContext, WidgetConfigurationActivity.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
        mConfigurationPendingIntent = PendingIntent.getActivity(mContext, mWidgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    // methods for managing widgets
    public abstract boolean prepare();

    public abstract void changeData();

    public abstract void setLayoutValues();

    public void updateLayout(){
        // request widget redraw
        mWidgetManager.updateAppWidget(mWidgetId, mRemoteViews);
    }

    public void asyncTask(Object obj){
    }

    public void whenUserLogin() {
    }

    public void whenUserLogout() {
    }

    protected PendingIntent startDetailActivityPendingIntent(Context context, int requestCode, String adapterId, String deviceId) {
        Intent intent = new Intent(context, SensorDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SensorDetailActivity.EXTRA_DEVICE_ID, deviceId);
        intent.putExtra(SensorDetailActivity.EXTRA_ADAPTER_ID, adapterId);
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Load all data of this widget
     */
    public void loadData(Context context) {
        // set default widget data
        layout = mPrefs.getInt(PREF_LAYOUT, mWidgetProviderInfo != null ? mWidgetProviderInfo.initialLayout : 0);
        interval = mPrefs.getInt(PREF_INTERVAL, WidgetService.UPDATE_INTERVAL_DEFAULT);
        lastUpdate = mPrefs.getLong(PREF_LAST_UPDATE, 0);
        initialized = mPrefs.getBoolean(PREF_INITIALIZED, false);
        // all widgets has adapterId
        adapterId = mPrefs.getString(PREF_ADAPTER_ID, "");
    }

    /**
     * Save all data of this widget
     */
    public void saveData(Context context) {
        getSettings(context).edit()
            .putString(PREF_CLASS_NAME, mClassName)
            .putInt(PREF_LAYOUT, layout)
            .putInt(PREF_INTERVAL, interval)
            .putLong(PREF_LAST_UPDATE, lastUpdate)
            .putBoolean(PREF_INITIALIZED, initialized)
            .putString(PREF_ADAPTER_ID, adapterId)
            .commit();

        initLayout();
    }

    /**
     * Save layout of this widget
     * This also fills layout field automatically
     * @param context context
     * @param layout layout resource
     */
    public void saveLayout(Context context, int layout) {
        this.layout = layout;

        getSettings(context).edit().putInt(PREF_LAYOUT, layout).commit();

        mRemoteViews = new RemoteViews(context.getPackageName(), this.layout);
    }

    /**
     * Sets switchcompat (imageview)
     * @param state
     * @param rv
     */
    public void setSwitchChecked(boolean state, RemoteViews rv){
        rv.setImageViewResource(R.id.widget_switchcompat, state ? R.drawable.switch_on : R.drawable.switch_off);
    }

    public void setSwitchDisabled(boolean disabled, boolean fallbackState, RemoteViews rv){
        if(disabled == true){
            rv.setImageViewResource(R.id.widget_switchcompat, R.drawable.switch_disabled);
        }
        else{
            setSwitchChecked(fallbackState, rv);
        }
    }

    /**
     * Returns className by widget id (for instantiating right widget class)
     * @param context
     * @param widgetId
     * @return
     */
    public static String getSettingClassName(Context context, int widgetId){
        SharedPreferences prefs = getSettings(context, widgetId);
        if(prefs == null) return "";
        return prefs.getString(PREF_CLASS_NAME, "");
    }

    public static SharedPreferences getSettings(Context context, int widgetId){
        return context.getSharedPreferences(String.format(PREF_FILENAME, widgetId), 0);
    }

    /**
     * Return SharedPreferences for widget
     * NOTE: We don't use Controller to get settings, because widgets doesn't depend on logged in user.
     *
     * @param context
     * @return
     */
    protected SharedPreferences getSettings(Context context) {
        return context.getSharedPreferences(String.format(PREF_FILENAME, mWidgetId), 0);
    }


    /**
     * Deletes all settings (even childrens)
     */
    public final void deleteData(Context context){
        getSettings(context).edit().clear().commit();
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
     * it will be incorrect after phone reboot. This method tries to fix that by reseting lastUpdate time
     * when it is greater than actual SystemClock.elapsedRealtime() value.
     *
     * @param now Actual SystemClock.elapsedRealtime() value
     */
    private void fixLastUpdate(long now) {
        if (lastUpdate > now) {
            lastUpdate = 0;
        }
    }

    /**
     * Checks if widget is expired and should be redrawn
     *
     * @param now
     *            Actual SystemClock.elapsedRealtime() value to compare
     * @return true if next update time is in the past (or <1000ms in future from now)
     */
    public boolean isExpired(long now) {
        fixLastUpdate(now);
        return (lastUpdate + interval * 1000) - now <= 1000;
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
        return lastUpdate > 0 ? lastUpdate + interval * 1000 : now;
    }

}
