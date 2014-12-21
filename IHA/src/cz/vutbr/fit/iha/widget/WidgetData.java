package cz.vutbr.fit.iha.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.SharedPreferences;
import cz.vutbr.fit.iha.R;

public class WidgetData {

	private static final String PREF_FILENAME = "widget_%d";
	private static final String PREF_LAYOUT = "layout";
	private static final String PREF_INTERVAL = "interval";
	private static final String PREF_LAST_UPDATE = "lastUpdate";
	private static final String PREF_INITIALIZED = "initialized";

	private static final String PREF_DEVICE_ID = "device";
	private static final String PREF_DEVICE_NAME = "device_name";
	private static final String PREF_DEVICE_ICON = "device_icon";
	private static final String PREF_DEVICE_VALUE = "device_value";
	private static final String PREF_DEVICE_UNIT = "device_unit";	
	private static final String PREF_DEVICE_ADAPTER_ID = "device_adapter_id";
	private static final String PREF_DEVICE_LAST_UPDATE_TEXT = "device_last_update_text";
	private static final String PREF_DEVICE_LAST_UPDATE_TIME = "device_last_update_time";
	private static final String PREF_DEVICE_REFRESH = "device_refresh";

	private final int mWidgetId;

	public int layout;
	public int interval;
	public long lastUpdate;
	public boolean initialized;

	public String deviceId;
	public String deviceName;
	public int deviceIcon;
	public String deviceValue;
	public String deviceUnit;
	public String deviceAdapterId;
	public long deviceLastUpdateTime;
	public String deviceLastUpdateText;
	public int deviceRefresh;

	/**
	 * Create empty WidgetData object, you must call {@link #loadData(Context)} to fill it
	 * 
	 * @param widgetId
	 */
	public WidgetData(final int widgetId) {
		mWidgetId = widgetId;
	}

	/**
	 * Return SharedPreferences for widget
	 * 
	 * NOTE: We don't use Controller to get settings, because widgets doesn't depend on logged in user.
	 * 
	 * @param context
	 * @param widgetId
	 * @return
	 */
	private SharedPreferences getSettings(Context context) {
		return context.getSharedPreferences(String.format(PREF_FILENAME, mWidgetId), 0);
	}

	public int getWidgetId() {
		return mWidgetId;
	}

	/**
	 * Load all data of this widget
	 * 
	 * @param context
	 */
	public void loadData(Context context) {
		SharedPreferences prefs = getSettings(context);
		
		// need to get initial layout, otherwise causing problems
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);				
		AppWidgetProviderInfo widgetProviderInfo = widgetManager.getAppWidgetInfo(this.getWidgetId());
		layout = prefs.getInt(PREF_LAYOUT, widgetProviderInfo.initialLayout);		
		
		interval = prefs.getInt(PREF_INTERVAL, WidgetUpdateService.UPDATE_INTERVAL_DEFAULT);
		lastUpdate = prefs.getLong(PREF_LAST_UPDATE, 0);
		initialized = prefs.getBoolean(PREF_INITIALIZED, false);

		deviceId = prefs.getString(PREF_DEVICE_ID, "");
		deviceName = prefs.getString(PREF_DEVICE_NAME, context.getString(R.string.placeholder_not_exists));
		deviceIcon = prefs.getInt(PREF_DEVICE_ICON, 0);
		deviceValue = prefs.getString(PREF_DEVICE_VALUE, "");
		deviceUnit = prefs.getString(PREF_DEVICE_UNIT, "");
		deviceAdapterId = prefs.getString(PREF_DEVICE_ADAPTER_ID, "");
		deviceLastUpdateText = prefs.getString(PREF_DEVICE_LAST_UPDATE_TEXT, "");
		deviceLastUpdateTime = prefs.getLong(PREF_DEVICE_LAST_UPDATE_TIME, 0);
		deviceRefresh = prefs.getInt(PREF_DEVICE_REFRESH, 0);
	}

	/**
	 * Save all data of this widget
	 * 
	 * @param context
	 */
	public void saveData(Context context) {
		getSettings(context) //
				.edit() //

				.putInt(PREF_LAYOUT, layout) //
				.putInt(PREF_INTERVAL, interval) //
				.putLong(PREF_LAST_UPDATE, lastUpdate) //
				.putBoolean(PREF_INITIALIZED, initialized) //

				.putString(PREF_DEVICE_ID, deviceId) //
				.putString(PREF_DEVICE_NAME, deviceName) //
				.putInt(PREF_DEVICE_ICON, deviceIcon) //
				.putString(PREF_DEVICE_VALUE, deviceValue) //
				.putString(PREF_DEVICE_UNIT, deviceUnit) //
				.putString(PREF_DEVICE_ADAPTER_ID, deviceAdapterId) //
				.putString(PREF_DEVICE_LAST_UPDATE_TEXT, deviceLastUpdateText) //
				.putLong(PREF_DEVICE_LAST_UPDATE_TIME, deviceLastUpdateTime) //
				.putInt(PREF_DEVICE_REFRESH, deviceRefresh) //

				.commit();
	}

	/**
	 * Save layout of this widget
	 * 
	 * This also fills layout field automatically
	 * 
	 * @param context
	 * @param layout
	 */
	public void saveLayout(Context context, int layout) {
		this.layout = layout;

		getSettings(context) //
				.edit() //
				.putInt(PREF_LAYOUT, layout).commit();
	}

	/**
	 * Delete all data of this widget
	 * 
	 * @param context
	 * @param widgetId
	 */
	public void deleteData(Context context) {
		getSettings(context) //
				.edit() //
				.clear() //
				.commit();
	}

	/**
	 * Because we're saving SystemClock.elapsedRealtime() value, which is time since phone boot, it will be incorrect after phone reboot. This method tries to fix that by reseting lastUpdate time when
	 * it is greater than actual SystemClock.elapsedRealtime() value.
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
