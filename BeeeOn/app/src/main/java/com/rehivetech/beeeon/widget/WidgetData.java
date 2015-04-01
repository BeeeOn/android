package com.rehivetech.beeeon.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.SharedPreferences;

import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.widget.old.WidgetUpdateService;

/**
 * @author mlyko
 */
abstract public class WidgetData {
    protected static final String PREF_FILENAME = "widget_%d";
    protected static final String PREF_CLASS_NAME = "class_name";
    protected static final String PREF_LAYOUT = "layout";
    protected static final String PREF_INTERVAL = "interval";
    protected static final String PREF_LAST_UPDATE = "lastUpdate";
    protected static final String PREF_INITIALIZED = "initialized";
    protected static final String PREF_ADAPTER_ID = "adapter_id";

    protected final int mWidgetId;
    protected String mClassName;

    public int layout;
    public int interval;
    public long lastUpdate;
    public boolean initialized;
    public String adapterId;

    public WidgetProvider widgetProvider;

    public WidgetData(final int widgetId){
        mWidgetId = widgetId;
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

    /**
     * Load all data of this widget
     */
    public void loadData(Context context) {
        SharedPreferences prefs = getSettings(context);

        // get widgetproviderinfo for widget default informations
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        AppWidgetProviderInfo widgetProviderInfo = widgetManager.getAppWidgetInfo(this.getWidgetId());

        // set default widget data
        // TODO nekdy je widgetproviderinfo null a pak to zde pada
        layout = prefs.getInt(PREF_LAYOUT, widgetProviderInfo != null ? widgetProviderInfo.initialLayout : 0);
        interval = prefs.getInt(PREF_INTERVAL, WidgetService.UPDATE_INTERVAL_DEFAULT);
        lastUpdate = prefs.getLong(PREF_LAST_UPDATE, 0);
        initialized = prefs.getBoolean(PREF_INITIALIZED, false);
        // all widgets has adapterId
        adapterId = prefs.getString(PREF_ADAPTER_ID, "");
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
     * Return SharedPreferences for widget
     * NOTE: We don't use Controller to get settings, because widgets doesn't depend on logged in user.
     *
     * @param context
     * @return
     */
    protected SharedPreferences getSettings(Context context) {
        return context.getSharedPreferences(String.format(PREF_FILENAME, mWidgetId), 0);
    }

    public static SharedPreferences getSettings(Context context, int widgetId){
        return context.getSharedPreferences(String.format(PREF_FILENAME, widgetId), 0);
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
