package cz.vutbr.fit.iha.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import cz.vutbr.fit.iha.R;

public class WidgetData {

	private static final String TAG = SensorWidgetProvider.class.getSimpleName();

	private static final String PREF_FILENAME = "widget_%d";
	private static final String PREF_LAYOUT = "layout";
	private static final String PREF_INTERVAL = "interval";
	private static final String PREF_LAST_UPDATE = "lastUpdate";
	private static final String PREF_INITIALIZED = "initialized";

	private static final String PREF_DEVICE_ID = "device";
	private static final String PREF_DEVICE_NAME = "device_name";
	private static final String PREF_DEVICE_ICON = "device_icon";
	private static final String PREF_DEVICE_VALUE = "device_value";
	private static final String PREF_DEVICE_ADAPTER_ID = "device_adapter_id";
	private static final String PREF_DEVICE_LAST_UPDATE = "device_last_update";

	private final int mWidgetId;

	public int layout;
	public int interval;
	public long lastUpdate;
	public boolean initialized;

	public String deviceId;
	public String deviceName;
	public int deviceIcon;
	public String deviceValue;
	public String deviceAdapterId;
	public String deviceLastUpdate;

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
		Log.v(TAG, String.format("Loading widget (%d) data from cache", mWidgetId));

		SharedPreferences prefs = getSettings(context);

		layout = prefs.getInt(PREF_LAYOUT, R.layout.widget_sensor);
		interval = prefs.getInt(PREF_INTERVAL, WidgetUpdateService.UPDATE_INTERVAL_DEFAULT);
		lastUpdate = prefs.getLong(PREF_LAST_UPDATE, 0);
		initialized = prefs.getBoolean(PREF_INITIALIZED, false);

		deviceId = prefs.getString(PREF_DEVICE_ID, "");
		deviceName = prefs.getString(PREF_DEVICE_NAME, context.getString(R.string.placeholder_not_exists));
		deviceIcon = prefs.getInt(PREF_DEVICE_ICON, 0);
		deviceValue = prefs.getString(PREF_DEVICE_VALUE, "");
		deviceAdapterId = prefs.getString(PREF_DEVICE_ADAPTER_ID, "");
		deviceLastUpdate = prefs.getString(PREF_DEVICE_LAST_UPDATE, "");
	}

	/**
	 * Save all data of this widget
	 * 
	 * @param context
	 */
	public void saveData(Context context) {
		Log.v(TAG, String.format("Saving widget (%d) data to cache", mWidgetId));

		SharedPreferences prefs = getSettings(context);

		prefs.edit() //

			.putInt(PREF_LAYOUT, layout) //
			.putInt(PREF_INTERVAL, interval) //
			.putLong(PREF_LAST_UPDATE, lastUpdate) //
			.putBoolean(PREF_INITIALIZED, initialized) //

			.putString(PREF_DEVICE_ID, deviceId) //
			.putString(PREF_DEVICE_NAME, deviceName) //
			.putInt(PREF_DEVICE_ICON, deviceIcon) //
			.putString(PREF_DEVICE_VALUE, deviceValue) //
			.putString(PREF_DEVICE_ADAPTER_ID, deviceAdapterId) //
			.putString(PREF_DEVICE_LAST_UPDATE, deviceLastUpdate) //

			.commit();
	}
	
	/**
	 * Save time of last update of this widget
	 * 
	 * This also fills lastUpdate and deviceLastUpdate fields automatically
	 * 
	 * @param context
	 * @param now
	 * @param deviceLastUpdate
	 */
	public void saveLastUpdate(Context context, long lastUpdate, String deviceLastUpdate) {
		this.lastUpdate = lastUpdate;
		this.deviceLastUpdate = deviceLastUpdate;
		
		getSettings(context) //
			.edit() //
			.putLong(PREF_LAST_UPDATE, lastUpdate) //
			.putString(PREF_DEVICE_LAST_UPDATE, deviceLastUpdate) //
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
			.putInt(PREF_LAYOUT, layout)
			.commit();
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
	 * Checks if widget is expired and should be redrawn
	 * 
	 * @param now Actual SystemClock.elapsedRealtime() value to compare
	 * @return true if next update time is in the past (or <1000ms in future from now)
	 */
	public boolean isExpired(long now) {
		return (lastUpdate + interval * 1000) - now <= 1000;
	}
	
	/**
	 * Calculates time of next update
	 * 
	 * @param now Actual SystemClock.elapsedRealtime() value to compare
	 * @return
	 */
	public long getNextUpdate(long now) {
		return lastUpdate > 0 ? lastUpdate + interval * 1000 : now;
	}

}
