package com.rehivetech.beeeon.widget.data;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

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
 * Created by Tomáš on 29. 4. 2015.
 */
public abstract class WidgetData {
	private static final String TAG = WidgetData.class.getSimpleName();

	// preference file name
	public static final String PREF_FILENAME = "widget_%d";
	// preference keys of widget object
	protected static final String PREF_CLASS_NAME = "widget_class_name";
	protected static final String PREF_LAYOUT = "widget_layout";
	protected static final String PREF_INTERVAL = "widget_interval";
	protected static final String PREF_LAST_UPDATE = "widget_last_update";
	protected static final String PREF_INITIALIZED = "widget_initialized";
	protected static final String PREF_ADAPTER_ID = "widget_adapter_id";
	protected static final String PREF_USER_ID = "widget_user_id";
	protected static final String PREF_WIFI_ONLY = "widget_wifi_only";

	// public properties
	public int widgetLayout;
	public boolean widgetLayoutChanged = false;
	public int widgetInterval;
	public long widgetLastUpdate;
	public boolean widgetInitialized;
	public String widgetAdapterId;
	public boolean widgetWifiOnly;
	public WidgetSettings settings;

	// if widget has any devices in layout, we can ensure that widget's layout is update across all widgets (e.g. actor)
	public List<WidgetDevicePersistence> widgetModules;

	// private managing variables
	protected Context mContext;
	protected Controller mController;
	protected AppWidgetManager mWidgetManager;
	protected AppWidgetProviderInfo mWidgetProviderInfo;
	protected SharedPreferences mPrefs;
	protected UnitsHelper mUnitsHelper;
	protected TimeHelper mTimeHelper;
	protected ViewsBuilder mBuilder;

	protected final int mWidgetId;
	private boolean mIsCached = false;
	private String mUserId;

	// default pending intents
	protected PendingIntent mRefreshPendingIntent;
	protected PendingIntent mConfigurationPendingIntent;

	/**
	 * Constructing object holding information about widget (instantiating in config activity and then in service)
	 * @param widgetId
	 * @param context
	 * @param unitsHelper
	 * @param timeHelper
	 */
	public WidgetData(final int widgetId, Context context, UnitsHelper unitsHelper, TimeHelper timeHelper){
		mWidgetId = widgetId;
		mContext = context.getApplicationContext();
		mWidgetManager = AppWidgetManager.getInstance(mContext);
		mWidgetProviderInfo = mWidgetManager.getAppWidgetInfo(mWidgetId);
		mController = Controller.getInstance(mContext);
		mPrefs = getSettings(mContext, mWidgetId);
		mUnitsHelper = unitsHelper;
		mTimeHelper = timeHelper;

		mBuilder = new ViewsBuilder(mContext);
		settings = WidgetSettings.getSettings(mContext, mWidgetId);
	}


	// ----------------------------------------------------------- //
	// ---------------- MANIPULATING PERSISTENCE ----------------- //
	// ----------------------------------------------------------- //

	/**
	 * Load all data of this widget - it's called only when opening configuration activity or reload() needed
	 */
	public void load(){
		Log.d(TAG, "load()");
		// set default widget data
		widgetLayout = mPrefs.getInt(PREF_LAYOUT, mWidgetProviderInfo != null ? mWidgetProviderInfo.initialLayout : 0); // TODO sometimes providerInfo is null
		widgetInterval = mPrefs.getInt(PREF_INTERVAL, WidgetService.UPDATE_INTERVAL_DEFAULT.getInterval());
		widgetLastUpdate = mPrefs.getLong(PREF_LAST_UPDATE, 0);
		widgetInitialized = mPrefs.getBoolean(PREF_INITIALIZED, false);
		widgetAdapterId = mPrefs.getString(PREF_ADAPTER_ID, "");
		widgetWifiOnly = mPrefs.getBoolean(PREF_WIFI_ONLY, false);
		mUserId = mPrefs.getString(PREF_USER_ID, mController.getActualUser().getId());
		// load widget's settings (color scheme e.g)
		settings.load();
	}

	/**
	 * Initialize method called after constructor in service
	 * NOTE: created this way because of complicated constructor calls (not working well with inheritance)
	 */
	public abstract void init();

	/**
	 * Method same as init() but injecting some kind of Object into widget data
	 * @param obj
	 */
	public void initAdvanced(Object obj){}

	/**
	 * If widget is editing, this is called after configuration activity ends - reload already created widget
	 */
	public final void reload(){
		Log.d(TAG, "reload()");
		this.load();
		init();
	}

	/**
	 * Configuration activity calls this when finished
	 * If any property is not save() it won't last cause object is destroyed after configuration activity
	 * @param adapter
	 * @param isEditing
	 * @param interval  updating interval
	 * @param isWifiOnly
	 * @param adapter
	 */
	public void configure(boolean isEditing, int interval, boolean isWifiOnly, Adapter adapter){
		Log.d(TAG, String.format("configure(%b)", isEditing));

		widgetLastUpdate = 0;
		widgetInitialized = true;
		widgetInterval = interval;
		widgetAdapterId = adapter.getId();
		widgetWifiOnly = isWifiOnly;
		this.save();
	}

	/**
	 * Manages change of layout and indicating flag about it so that service can render widget
	 * @param layoutResource
	 */
	public final void changeLayout(int layoutResource){
		// if not found, dont change anything
		if(layoutResource == 0 || layoutResource == this.widgetLayout) return;
		this.widgetLayoutChanged = true;
		this.widgetLayout = layoutResource;
		save();
	}

	/**
	 * Save all data of this widget
	 */
	public void save(){
		Log.d(TAG, "save()");

		settings.save();

		mPrefs.edit()
				.putString(PREF_CLASS_NAME, getClassName())
				.putInt(PREF_LAYOUT, widgetLayout)
				.putInt(PREF_INTERVAL, widgetInterval)
				.putLong(PREF_LAST_UPDATE, widgetLastUpdate)
				.putString(PREF_ADAPTER_ID, widgetAdapterId)
				.putString(PREF_USER_ID, mUserId)
				.putBoolean(PREF_INITIALIZED, widgetInitialized)
				.putBoolean(PREF_WIFI_ONLY, widgetWifiOnly)
				.apply();
	}

	/**
	 * Deletes preference file (that means all settings of the widget - even children's)
	 */
	public void delete(){
		mPrefs.edit().clear().apply();
	}

	// ----------------------------------------------------------- //
	// ------------------------ RENDERING ------------------------ //
	// ----------------------------------------------------------- //

	/**
	 * Can be called from outside to refresh widget (always needs to recreate whole widget)
	 */
	synchronized public final void renderWidget(){
		Log.d(TAG, "renderWidget()");
		initLayout();
		renderLayout();
		renderAppWidget();
	}

	/**
	 * Initializes layout of this widget
	 */
	private void initLayout(){
		// creates new remoteviews (NOTE: necessary cause it's just bunch of actions so if we don't do that, there are redundant actions)
		mBuilder.loadRootView(this.widgetLayout);
		// refresh onclick
		mRefreshPendingIntent = WidgetService.getPendingIntentForceUpdate(mContext, mWidgetId);
		// configuration onclick
		mConfigurationPendingIntent = startConfigurationActivityPendingIntent(mContext, mWidgetId);
	}

	/**
	 * Renders layout of specified widget
	 */
	protected abstract void renderLayout();

	/**
	 * Request widget redraw
	 */
	private void renderAppWidget(){
		mWidgetManager.updateAppWidget(mWidgetId, mBuilder.getRoot());
	}

	// ----------------------------------------------------------- //
	// ---------------------- FAKE HANDLERS ---------------------- //
	// ----------------------------------------------------------- //

	/**
	 * Updates data and persistence
	 * @return if any data were updated
	 */
	public abstract boolean handleUpdateData();

	public void handleSetNotCached(){
		Log.v(TAG, String.format("handleSetNotCached(%d)", mWidgetId));
		mIsCached = false;
	}

	public void handleSetCached(){
		Log.v(TAG, String.format("handleSetCached(%d)", mWidgetId));
		mIsCached = true;
	}

	/**
	 * Changes widgetLayout of this widget based on new width or height
	 * @param minWidth
	 * @param minHeight
	 */
	public void handleResize(int minWidth, int minHeight) {
		Log.v(TAG, String.format("handleResize(%d) [%d | %d]", mWidgetId, minWidth, minHeight));
	}

	// ----------------------------------------------------------- //
	// ----------------- MANAGING UPDATE TIME -------------------- //
	// ----------------------------------------------------------- //
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
	 * @param now Actual SystemClock.elapsedRealtime() value to compare
	 * @return
	 */
	public long getNextUpdate(long now) {
		fixLastUpdate(now);
		return widgetLastUpdate > 0 ? widgetLastUpdate + widgetInterval * 1000 : now;
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

	// ----------------------------------------------------------- //
	// ------------------------- GETTERS ------------------------- //
	// ----------------------------------------------------------- //

	public String getUserId() {
		return mUserId;
	}

	/**
	 * Used for passing objects to service so that it can refresh them (list because some widgets can have more of them)
	 * @return
	 */
	public abstract List<?> getObjectsToReload();

	/**
	 * So that we can distinguish classes
	 * @return
	 */
	public abstract String getClassName();

	/**
	 * Whether widget was not update from network last time it was updagin
	 * @return
	 */
	public boolean getIsCached() {
		return mIsCached;
	}

	/**
	 * Wrapper for getting elapsed realtime
	 * @return
	 */
	protected long getTimeNow(){
		return SystemClock.elapsedRealtime();
	}

	/**
	 * Returns widgetId for reading only
	 * @return
	 */
	public int getWidgetId() {
		return mWidgetId;
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

	// ----------------------------------------------------------- //
	// -------------------- INTENT FACTORIES --------------------- //
	// ----------------------------------------------------------- //

	/**
	 * PendingIntent for opening configuration activity for widget
	 * @param context
	 * @param widgetId
	 * @return
	 */
	public static PendingIntent startConfigurationActivityPendingIntent(Context context, int widgetId){
		Intent intent = new Intent(context, WidgetConfigurationActivity.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		intent.putExtra(WidgetConfigurationActivity.EXTRA_WIDGET_EDITING, true);
		return PendingIntent.getActivity(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	/**
	 * PendingIntent for opening detail of module
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

	/**
	 * Is called in location widget cause he can setup pending intents directly
	 * @param context
	 * @param adapterId
	 * @param deviceId
	 * @return
	 */
	public static Intent startDetailActivityIntent(Context context, String adapterId, String deviceId){
		Intent intent = new Intent(context, SensorDetailActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(SensorDetailActivity.EXTRA_DEVICE_ID, deviceId);
		intent.putExtra(SensorDetailActivity.EXTRA_ADAPTER_ID, adapterId);
		return intent;
	}

	/**
	 * Starts main activity of the application
	 * TODO should open main activity with specified adapter and location to scroll on
	 * @param context
	 * @return
	 */
	public static PendingIntent startMainActivityPendingIntent(Context context, String adapterId){
		Intent intent = new Intent(context, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(MainActivity.ADAPTER_ID, adapterId);

		return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
}
