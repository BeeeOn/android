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

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ClientError;
import com.rehivetech.beeeon.exception.IErrorCode;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.BooleanValue;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.pair.LogDataPair;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.ActorActionTask;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.data.WidgetClockData;
import com.rehivetech.beeeon.widget.data.WidgetData;
import com.rehivetech.beeeon.widget.persistence.WidgetModulePersistence;
import com.rehivetech.beeeon.widget.persistence.WidgetWeatherPersistence;
import com.rehivetech.beeeon.widget.receivers.WidgetClockProvider;
import com.rehivetech.beeeon.widget.receivers.WidgetGraphProvider;
import com.rehivetech.beeeon.widget.receivers.WidgetLocationListProvider;
import com.rehivetech.beeeon.widget.receivers.WidgetModuleProvider;
import com.rehivetech.beeeon.widget.receivers.WidgetModuleProviderLarge;
import com.rehivetech.beeeon.widget.receivers.WidgetModuleProviderMedium;
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

	private static final String EXTRA_START_UPDATING = "com.rehivetech.beeeon.start_updating";
	private static final String EXTRA_FORCE_UPDATE = "com.rehivetech.beeeon.force_update";
	private static final String EXTRA_ACTOR_CHANGE_REQUEST = "com.rehivetech.beeeon.actor_change_request";
	private static final String EXTRA_ACTOR_ID = "com.rehivetech.beeeon.actor_ids";
	private static final String EXTRA_DELETE_WIDGET = "com.rehivetech.beeeon.delete_widget";
	private static final String EXTRA_CHANGE_LAYOUT = "com.rehivetech.beeeon.change_layout";
	private static final String EXTRA_CHANGE_LAYOUT_MIN_WIDTH = "com.rehivetech.beeeon.change_layout_min_width";
	private static final String EXTRA_CHANGE_LAYOUT_MIN_HEIGHT = "com.rehivetech.beeeon.change_layout_min_height";
	private static final String EXTRA_WIDGETS_SHOULD_RELOAD = "com.rehivetech.beeeon.widget_should_reload";
	private static final String EXTRA_ACTOR_GATE_ID = "com.rehivetech.beeeon.actor_adapter_id";

	// when finding all widgets with the same actor
	private static final int UPDATE_LAYOUT = 0;
	private static final int UPDATE_WHOLE = 1;

	// list of available widgets
	// TODO maybe make as static and public method for getting only "now" available widgets
	private SparseArray<WidgetData> mAvailableWidgets = new SparseArray<>();

	// managing variables
	private Context mContext;
	private Controller mController;
	private UnitsHelper mUnitsHelper;
	private TimeHelper mTimeHelper;
	private Calendar mCalendar;
	private WeatherProvider mWeatherProvider;
	// broadcast intents
	private IntentFilter mIntentFilter = recreateIntentFilter();
	private boolean mClockFilterReg = false;
	// for checking if user is logged in (we presume that for the first time he is)
	private boolean isCached = false;
	// for updating on wifi only
	private int mNetworkType = -1;
	// next update in elapsed realtime
	private int mStandbyWidgets = 0;
	private boolean mServiceIsStandby = false;

	// -------------------------------------------------------------------- //
	// --------------- Main methods (entry points) of service ------------- //
	// -------------------------------------------------------------------- //

	/**
	 * When startin updating widgets, we can tell that these widgets should be reloaded
	 *
	 * @param context
	 * @param appWidgetIds
	 * @param widgetShouldReload
	 */
	public static void startUpdating(Context context, int[] appWidgetIds, boolean widgetShouldReload) {
		Log.d(TAG, String.format("startUpdating(%b)", widgetShouldReload));
		final Intent intent = getIntentUpdate(context);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		intent.putExtra(EXTRA_START_UPDATING, true);
		intent.putExtra(EXTRA_WIDGETS_SHOULD_RELOAD, widgetShouldReload);
		context.startService(intent);
	}

	/**
	 * Place to start the service from widget provider
	 *
	 * @param context
	 * @param appWidgetIds
	 */
	public static void startUpdating(Context context, int[] appWidgetIds) {
		startUpdating(context, appWidgetIds, false);
	}

	/**
	 * When no widgets are available, stops the service
	 *
	 * @param context
	 */
	public void stopAlarmAndService(Context context) {
		Log.d(TAG, "stopAlarmAndService()");
		// stop alarm - no more updates
		stopAlarm();
		// stop this service
		context.stopService(getIntentUpdate(context));
	}

	/**
	 * Initializes things only first time service is started
	 */
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate()");
		super.onCreate();

		mContext = getApplicationContext();
		mCalendar = Calendar.getInstance(mContext.getResources().getConfiguration().locale);
		mController = Controller.getInstance(mContext);
		mNetworkType = Utils.getNetworkConnectionType(mContext);
		mWeatherProvider = new WeatherProvider(mContext); // TODO should be as model in controler in the future

		// Creates broadcast receiver which bridges broadcasts to appwidgets for handling time and screen
		registerReceiver(mServiceReceiver, mIntentFilter);
	}

	/**
	 * Cleaning up cause service is stopped
	 */
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		super.onDestroy();
		// removes all widgets in list
		mAvailableWidgets.clear();

		unregisterReceiver(mServiceReceiver);
	}

	/**
	 * Entry point of service .. widgets can sent Intent with specific data and pass them to service
	 *
	 * @param intent  Intent which can specify what action should service do now
	 * @param flags
	 * @param startId
	 * @return
	 */
	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		mController = Controller.getInstance(mContext);
		initHelpers(mContext, mController);

		boolean isStartUpdating = false, isForceUpdate = false;

		// -------------- get widget Ids
		final int[] appWidgetIds = intent != null ? intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS) : new int[]{};

		if (intent != null) {
			// -------------- async task for changing widget actor
			boolean isActorChangeRequest = intent.getBooleanExtra(EXTRA_ACTOR_CHANGE_REQUEST, false);
			if (isActorChangeRequest) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						String gateId = intent.getStringExtra(EXTRA_ACTOR_GATE_ID);
						String actorId = intent.getStringExtra(EXTRA_ACTOR_ID);
						changeWidgetActorRequest(gateId, actorId);
					}
				}).start();
				return START_STICKY;
			}

			// -------------- onAppWidgetOptionsChanged (changing widgetLayout)
			boolean isChangeLayout = intent.getBooleanExtra(EXTRA_CHANGE_LAYOUT, false);
			if (isChangeLayout) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						int layoutMinWidth = intent.getIntExtra(EXTRA_CHANGE_LAYOUT_MIN_WIDTH, 0);
						int layoutMinHeight = intent.getIntExtra(EXTRA_CHANGE_LAYOUT_MIN_HEIGHT, 0);
						resizeWidget(appWidgetIds, layoutMinWidth, layoutMinHeight);
					}
				}).start();

				return START_STICKY;
			}

			// -------------- widget deletion
			boolean isDeleteWidget = intent.getBooleanExtra(EXTRA_DELETE_WIDGET, false);
			if (isDeleteWidget) {
				widgetsDelete(appWidgetIds);
				// sets new alarm manager after calculation
				setNewAlarmOrStopSelf(new int[]{}, Utils.getNetworkConnectionType(mContext));
				return START_STICKY;
			}

			// -------------- force update
			isForceUpdate = intent.getBooleanExtra(EXTRA_FORCE_UPDATE, false);

			// -------------- start updating
			isStartUpdating = intent.getBooleanExtra(EXTRA_START_UPDATING, false);
			if (isStartUpdating) {
				isForceUpdate = true;
			}
		}

		Log.v(TAG, String.format("isServiceStandBy = %b | networkType = %d", mServiceIsStandby, mNetworkType));

		// calculate it first time always or only when force update
		if (isStartUpdating || !isForceUpdate) {
			// sets new alarm manager after calculation
			if (!setNewAlarmOrStopSelf(appWidgetIds, Utils.getNetworkConnectionType(mContext))) return START_STICKY;
		}

		final boolean forceUpdating = isForceUpdate;

		// update widgets
		new Thread(new Runnable() {
			@Override
			public void run() {
				updateWidgets(intent, forceUpdating, appWidgetIds);
			}
		}).start();
		return START_STICKY;
	}

	// -------------------------------------------------------------------- //
	// ------------------------ Running in own threads -------------------- //
	// -------------------------------------------------------------------- //

	/**
	 * Method calls all widgets which needs to be updated
	 *
	 * @param isForceUpdate
	 * @param allWidgetIds
	 */
	private void updateWidgets(Intent intent, boolean isForceUpdate, int[] allWidgetIds) {
		Log.d(TAG, "updateWidgets()");

		// if there are no widgets passed by intent, try to get all widgets
		if (allWidgetIds == null || allWidgetIds.length == 0) allWidgetIds = getAllWidgetIds();

		// -------------- reload widget if necessary
		boolean isShouldReload = intent != null && intent.getBooleanExtra(EXTRA_WIDGETS_SHOULD_RELOAD, false);

		long timeNow = SystemClock.elapsedRealtime();
		SparseArray<WidgetData> widgetsToUpdate = new SparseArray<>();
		List<Object> widgetsObjectsToReload = new ArrayList<>();

		// update all widgets ... NOTE: can't contain any network request !
		for (int widgetId : allWidgetIds) {
			// ziska bud z pole pouzitych dat nebo instanciuje z disku
			WidgetData widgetData = getWidgetData(widgetId);
			if (widgetData == null) {
				Log.i(TAG, String.format("WidgetData(%d) doesn't exist & couldn't be created", widgetId));
				continue;
			}

			// reload data (after configuration and if objects already exists)
			if (isShouldReload) widgetData.reload();

			// Ignore uninitialized widgets
			if (!widgetData.widgetInitialized) {
				Log.v(TAG, String.format("Ignoring widget %d (not widgetInitialized)", widgetId));
				continue;
			}

			// if widget has setting to update only on wifi - skip if not this
			if (widgetData.widgetWifiOnly && mNetworkType != ConnectivityManager.TYPE_WIFI && !isForceUpdate) {
				Log.v(TAG, String.format("Ignoring widget %d - updates only on wifi", widgetId, mNetworkType));
				continue;
			}

			// Don't update widgets until their interval elapsed or we have force update
			if (!isForceUpdate && !widgetData.isExpired(timeNow)) {
				Log.v(TAG, String.format("Ignoring widget %d (not expired nor forced)", widgetId));
				continue;
			}

			// if preparation of widget is successfull, remember this widget
			widgetsToUpdate.put(widgetId, widgetData);
			widgetsObjectsToReload.addAll(widgetData.getObjectsToReload());
		}

		try {
			if (widgetsToUpdate.size() == 0) {
				Log.i(TAG, "No widget to update from server..");
				return;
			}

			// Reload adapters to have data about Timezone offset
			mController.getGatesModel().reloadGates(false);

			List<Device> usedDevices = new ArrayList<>();
			List<WidgetWeatherPersistence> usedWeatherData = new ArrayList<>();

			// check if any objects need to refresh
			if (!widgetsObjectsToReload.isEmpty()) {
				for (Object refObj : widgetsObjectsToReload) {
					if (refObj instanceof Device) {
						Device fac = (Device) refObj;
						// already there, skip
						if (Utils.getFromList(fac.getId(), usedDevices) != null) continue;
						usedDevices.add((Device) refObj);
					} else if (refObj instanceof Location) {
						Location loc = (Location) refObj;
						mController.getLocationsModel().reloadLocationsByGate(loc.getGateId(), false); // TODO load only necessary locations
					} else if (refObj instanceof LogDataPair) {
						LogDataPair logPair = (LogDataPair) refObj;
						mController.getModuleLogsModel().reloadModuleLog(logPair);
					} else if (refObj instanceof WidgetWeatherPersistence) {
						// skips city with id which is already in used data
						if (Utils.getFromList(((WidgetWeatherPersistence) refObj).getId(), usedWeatherData) != null) continue;
						usedWeatherData.add((WidgetWeatherPersistence) refObj);
					}
				}
			}

			// updates all devices in once
			if (!usedDevices.isEmpty()) {
				Log.v(TAG, "refreshing devices...");
				mController.getDevicesModel().refreshDevices(usedDevices, isForceUpdate);
			}

			// updates all weather data
			if (!usedWeatherData.isEmpty()) {
				this.updateWeatherData(usedWeatherData);
			}

			for (int i = 0; i < widgetsToUpdate.size(); i++) {
				WidgetData widgetData = widgetsToUpdate.valueAt(i);
				// if previous state was logged out
				if (isCached || widgetData.getIsCached()) widgetData.handleSetNotCached();
				widgetData.handleUpdateData();
				widgetData.renderWidget();
			}

			isCached = false;
		} catch (AppException e) {
			IErrorCode errCode = e.getErrorCode();
			if (errCode != null) {
				Log.e(TAG, e.getSimpleErrorMessage());
				setAllWidgetsCached();
				// we know, that if internet connection changes, we start updating again
				if (errCode instanceof ClientError && e.getErrorCode() == ClientError.INTERNET_CONNECTION) {
					stopAlarm();
				}
			}
		}

		// NOTE: we always have to render widgets if anything changes (even if not updated but connection changed or sth) !!!
	}

	/**
	 * Updates all weather persistences
	 *
	 * @param weatherDatas
	 */
	private void updateWeatherData(List<WidgetWeatherPersistence> weatherDatas) {
		for (WidgetWeatherPersistence weather : weatherDatas) {
			Log.v(TAG, "updating clock weather....");
			// TODO should check if city changed
			if (weather.cityName.isEmpty()) continue;

			JSONObject json = mWeatherProvider.getWeatherByCityId(weather.id);
			if (json == null) {
				weather.handleFailedUpdate();
				continue;
			}

			// setup weather
			weather.configure(json, null);
		}
	}

	/**
	 * Handle resizing widget (called from onAppWidgetOptionsChanged)
	 *
	 * @param widgetIds       widget Ids which were changed (mostly only 1 widget)
	 * @param layoutMinWidth  new widget width
	 * @param layoutMinHeight new widget height
	 */
	private void resizeWidget(int[] widgetIds, int layoutMinWidth, int layoutMinHeight) {
		// if there are no widgets passed by intent, does nothing
		if (widgetIds == null || widgetIds.length == 0) return;
		Log.d(TAG, "resizeWidget()");

		for (int widgetId : widgetIds) {
			WidgetData widgetData = getWidgetData(widgetId);
			if (widgetData == null) continue;
			widgetData.handleResize(layoutMinWidth, layoutMinHeight);
			// if widget changed its layout, render again
			if (widgetData.widgetLayoutChanged) {
				widgetData.renderWidget();
				widgetData.widgetLayoutChanged = false;
			}
		}
	}

	/**
	 * Actor task request (disables all widgets)
	 *
	 * @param gateId
	 * @param actorId
	 */
	private void changeWidgetActorRequest(final String gateId, final String actorId) {
		Log.d(TAG, String.format("changeWidgetActorRequest(%s, %s)", gateId, actorId));
		if (actorId == null || gateId == null || actorId.isEmpty() || gateId.isEmpty()) return;

		// ----- first get the module and change value
		final Module module = mController.getDevicesModel().getModule(gateId, actorId);
		if (module == null || !module.getType().isActor()) {
			Log.e(TAG, "MODULE NOT actor OR NOT FOUND --> probably need to refresh controller");
			return;
		}

		// ----- check if value is boolean
		BaseValue value = module.getValue();
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
		performWidgetActorChange(UPDATE_LAYOUT, gateId, actorId, true, newValue);

		// ----- and finally run asyncTask
		final ActorActionTask actorActionTask = new ActorActionTask(mContext);
		actorActionTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				// NOTE: we don't have any response here, cause that manages received broadcast
				if (!success) {
					// if not successfull, put back the state
					performWidgetActorChange(UPDATE_LAYOUT, gateId, actorId, false, oldValue);
				}
			}
		});
		actorActionTask.execute(module);
	}

	/**
	 * Actor task result (this is called by broadcast receiver) -> goes through all widgets
	 *
	 * @param gateId
	 * @param actorId
	 */
	private void changeWidgetActorResult(String gateId, String actorId) {
		Log.d(TAG, String.format("changeWidgetActorResult(%s, %s)", gateId, actorId));
		if (actorId == null || gateId == null || actorId.isEmpty() || gateId.isEmpty()) return;

		// does not set value cause updates whole widget (and there asks for new value)
		performWidgetActorChange(UPDATE_WHOLE, gateId, actorId, false, false);
	}


	// -------------------------------------------------------------------- //
	// ------------------------- Managing methods ------------------------- //
	// -------------------------------------------------------------------- //

	/**
	 * When system has low memory - delete saved widget data (they will be restored from persistence when needed)
	 */
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Log.i(TAG, "onLowMemory()");
		mAvailableWidgets.clear();
	}

	/**
	 * Happens when user logout or no internet connection or problem with updating from server
	 */
	private void setAllWidgetsCached() {
		if (isCached) {
			Log.i(TAG, "already cached data, skipping...");
			return;
		}
		setWidgetsCached(getAllWidgetIds());
		isCached = true;
	}

	/**
	 * Can set cached to widgets specified by id
	 *
	 * @param widgetIds
	 */
	private void setWidgetsCached(int[] widgetIds) {
		// for all widgets put to "logout" state
		for (int widgetId : widgetIds) {
			WidgetData widgetData = getWidgetData(widgetId);
			if (widgetData == null) continue;
			widgetData.handleSetCached();
			widgetData.renderWidget();
		}
	}

	/**
	 * Goes through all widgets, find widgets with the specified actor
	 * and perform partial (only changes layout) or whole update
	 *
	 * @param perform    UPDATE_LAYOUT or UPDATE_WHOLE
	 * @param gateId
	 * @param actorId
	 * @param isDisabled if should be switch disabled
	 * @param isValueOn  when UPDATE_WHOLE this does nothing
	 */
	private void performWidgetActorChange(int perform, String gateId, String actorId, boolean isDisabled, boolean isValueOn) {
		int[] allWidgetIds = getAllWidgetIds();
		if (allWidgetIds.length == 0 || actorId == null || gateId == null || actorId.isEmpty() || gateId.isEmpty()) return;
		for (int widgetId : allWidgetIds) {
			WidgetData data = getWidgetData(widgetId);
			// skips not compatible widgets
			if (data == null || data.widgetModules == null || data.widgetModules.isEmpty()) continue;

			int updatedActors = 0;
			// go through all devices in that widget
			for (WidgetModulePersistence wDev : data.widgetModules) {
				if (!gateId.equals(wDev.getGateId()) || !actorId.equals(wDev.getId())) continue;

				if (perform == UPDATE_LAYOUT) {
					wDev.setSwitchChecked(isValueOn);
				}

				// disables so that nobody can change it anymore
				wDev.setSwitchDisabled(isDisabled);
				updatedActors++;
			}

			// if any actor found, update whole widget
			if (updatedActors > 0) {
				if (perform == UPDATE_WHOLE) {
					data.handleUpdateData();
				}

				data.renderWidget();
			}
		}
	}

	/**
	 * Insert widgetData to list of used widgets
	 *
	 * @param widgetData
	 */
	private void widgetAdd(WidgetData widgetData) {
		// add widgetData to service
		mAvailableWidgets.put(widgetData.getWidgetId(), widgetData);
	}

	/**
	 * Deletes widgets data by Ids
	 *
	 * @param widgetIds
	 */
	private void widgetsDelete(int[] widgetIds) {
		for (int widgetId : widgetIds) {
			if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) continue;
			WidgetData data = getWidgetData(widgetId);
			if (data == null) continue;
			Log.v(TAG, String.format("delete widgetData(%d)", widgetId));

			data.delete();
			mAvailableWidgets.delete(widgetId);
		}
	}

	/**
	 * Gets widget data either from list of widgets or instantiating and loading from settings (when OS restarted)
	 *
	 * @param widgetId id of widget to load
	 * @return
	 */
	public WidgetData getWidgetData(int widgetId) {
		WidgetData widgetData = mAvailableWidgets.get(widgetId);
		if (widgetData == null) {
			String widgetClassName = WidgetData.getSettingClassName(mContext, widgetId);
			if (widgetClassName == null || widgetClassName.isEmpty()) return null;
			try {
				// instantiate class from string
				widgetData = (WidgetData) Class.forName(widgetClassName).getConstructor(int.class, Context.class, UnitsHelper.class, TimeHelper.class).newInstance(widgetId, mContext, mUnitsHelper, mTimeHelper);
				Log.v(TAG, "instantiated new widgetData " + widgetId);
				widgetData.load();
				// if its clock widget -> we put instance of calendar inside
				if (widgetData instanceof WidgetClockData) {
					widgetData.initAdvanced(mCalendar);
				} else {
					widgetData.init();
				}
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
	 * Set repeating to parameter
	 *
	 * @param triggerAtMillis
	 */
	private void setAlarm(long triggerAtMillis) {
		// Set new alarm time
		Log.d(TAG, String.format("Next update in %d seconds", (int) (triggerAtMillis - SystemClock.elapsedRealtime()) / 1000));
		AlarmManager m = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		m.set(AlarmManager.ELAPSED_REALTIME, triggerAtMillis, getPendingIntentUpdate(mContext));
	}

	/**
	 * Stops repeating
	 */
	private void stopAlarm() {
		// cancel frequently refreshing
		Log.d(TAG, "stopping alarm..");
		AlarmManager m = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		m.cancel(getPendingIntentUpdate(mContext));
	}

	/**
	 * Sets new alarm after calculations of widgets
	 *
	 * @param appWidgetIds array of widget ids -> if NULL - check all widgets
	 * @return if service is still required
	 */
	public boolean setNewAlarmOrStopSelf(int[] appWidgetIds, int networkType) {
		mNetworkType = networkType;

		// set alarm for next update
		long nextUpdate = calcNextUpdate(appWidgetIds);

		if (nextUpdate > 0) {
			setAlarm(nextUpdate);
			return true;
		} else {
			if (mStandbyWidgets > 0) {
				Log.v(TAG, "No planned next update (cause standby widgets)");
				return true;
			} else {
				Log.d(TAG, "No planned next update..");
				this.stopAlarmAndService(mContext);
				return false;
			}
		}
	}

	/**
	 * Get next time to update widgets
	 *
	 * @return
	 */
	private long calcNextUpdate(int[] appWidgetIds) {
		Log.v(TAG, "calculating next update...");
		int minInterval = 0;
		long nextUpdate = 0, timeNow = SystemClock.elapsedRealtime();
		boolean first = true, existsClockWidget = false, receiverChanged = false;
		mStandbyWidgets = 0;

		// first gets IDs of all widgets
		int[] widgetIds = getAllWidgetIds();

		// if none, tries the passed ids
		if (widgetIds == null || widgetIds.length == 0) {
			widgetIds = appWidgetIds;
		}

		if (widgetIds != null) for (int widgetId : widgetIds) {
			WidgetData widgetData = getWidgetData(widgetId);
			// widget is not added yet (probably only configuration activity is showed)
			if (widgetData == null || !widgetData.widgetInitialized) continue;
			// if updating on wifi - skip calculating
			if (widgetData.widgetWifiOnly && mNetworkType != ConnectivityManager.TYPE_WIFI) {
				mStandbyWidgets++;
				continue;
			}

			// because of registering broadcasts for time
			if (widgetData instanceof WidgetClockData) {
				existsClockWidget = true;
			}

			if (first) {
				minInterval = widgetData.widgetInterval;
				nextUpdate = widgetData.getNextUpdate(timeNow);
				first = false;
			} else {
				minInterval = Math.min(minInterval, widgetData.widgetInterval);
				nextUpdate = Math.min(nextUpdate, widgetData.getNextUpdate(timeNow));
			}
		}
		// if found any clock widget and not receiving time broadcasts
		if (existsClockWidget && !mClockFilterReg) {
			mCalendar.setTime(new Date());
			addTimeFilters();
			receiverChanged = true;
		}
		// if not found any clock widget and receiving time broadcasts
		// if no more widgets -> unregister when destroying service
		else if (!existsClockWidget && mClockFilterReg && !first) {
			recreateIntentFilter();
			receiverChanged = true;
		}

		// if receiver changed, register it again
		if (receiverChanged) restartReceiver();

		// count next update
		minInterval = Math.max(minInterval, UPDATE_INTERVAL_MIN.getInterval());
		return first ? 0 : Math.max(nextUpdate, SystemClock.elapsedRealtime() + minInterval * 1000);
	}

	/**
	 * !!! If we have any new widget, we need to add it here so that it keeps updating. !!!
	 *
	 * @return array of widget ids
	 */
	private int[] getAllWidgetIds() {
		List<Integer> ids = new ArrayList<>();

		// clock widget
		ids.addAll(getWidgetIds(WidgetClockProvider.class));
		// location list
		ids.addAll(getWidgetIds(WidgetLocationListProvider.class));
		// module widget
		ids.addAll(getWidgetIds(WidgetModuleProvider.class));
		ids.addAll(getWidgetIds(WidgetModuleProviderMedium.class));
		ids.addAll(getWidgetIds(WidgetModuleProviderLarge.class));
		// graph widget
		ids.addAll(getWidgetIds(WidgetGraphProvider.class));

		return Utils.convertIntegers(ids);
	}

	/**
	 * Gets all active clock widgets and update their time
	 * Also sets time in calendar to current
	 */
	private void updateClockWidgets() {
		List<Integer> clockIds = getWidgetIds(WidgetClockProvider.class);
		mCalendar.setTime(new Date());
		for (Integer clockId : clockIds) {
			WidgetData data = getWidgetData(clockId);
			if (data == null || !(data instanceof WidgetClockData)) continue;
			((WidgetClockData) data).handleClockUpdate();
		}
	}

	// -------------------------------------------------------------------- //
	// -------------------------- Helpers methods ------------------------- //
	// -------------------------------------------------------------------- //

	/**
	 * Initializes helpers classes when necessary (and unitialize them after not available)
	 *
	 * @param context
	 * @param controller
	 */
	private void initHelpers(Context context, Controller controller) {
		SharedPreferences userSettings = controller.getUserSettings();
		if (userSettings == null) {
			mUnitsHelper = null;
			mTimeHelper = null;
			return;
		}

		if (mUnitsHelper == null || mTimeHelper == null) {
			mUnitsHelper = new UnitsHelper(userSettings, context);
			mTimeHelper = new TimeHelper(userSettings);
		}
	}

	/**
	 * Gets ids of one widget provider
	 *
	 * @param cls class of widget provider
	 * @return widget ids list
	 */
	private List<Integer> getWidgetIds(Class<?> cls) {
		List<Integer> arr = new ArrayList<>();
		for (int i : WidgetProvider.getWidgetIdsByClass(mContext, cls)) {
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
	 *
	 * @param context
	 * @return
	 */
	private static Intent getIntentUpdate(Context context) {
		return new Intent(context, WidgetService.class);
	}

	/**
	 * Pending intent of this class
	 *
	 * @param context
	 * @return
	 */
	private static PendingIntent getPendingIntentUpdate(Context context) {
		final Intent intent = getIntentUpdate(context);
		return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}

	/**
	 * Force intent
	 *
	 * @param context
	 * @param widgetIds
	 * @return
	 */
	public static Intent getIntentForceUpdate(Context context, int[] widgetIds) {
		Intent intent = new Intent(context, WidgetService.class);
		intent.putExtra(WidgetService.EXTRA_FORCE_UPDATE, true);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
		return intent;
	}

	/**
	 * Force pending intent
	 *
	 * @param context
	 * @param widgetId
	 * @return
	 */
	public static PendingIntent getPendingIntentForceUpdate(Context context, int widgetId) {
		final Intent intent = getIntentForceUpdate(context, new int[]{widgetId});
		return PendingIntent.getService(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	/**
	 * Intent for changing actor's state
	 *
	 * @param context
	 * @param actorId
	 * @return
	 */
	public static Intent getIntentActorChangeRequest(Context context, String actorId, String gateId) {
		Intent intent = new Intent(context, WidgetService.class);
		intent.putExtra(WidgetService.EXTRA_ACTOR_CHANGE_REQUEST, true);
		intent.putExtra(EXTRA_ACTOR_ID, actorId);
		intent.putExtra(EXTRA_ACTOR_GATE_ID, gateId);
		return intent;
	}

	/**
	 * Pending intent for "onclick" for changing actor's state
	 *
	 * @param context
	 * @param widgetId
	 * @param actorId
	 * @return
	 */
	public static PendingIntent getPendingIntentActorChangeRequest(Context context, int widgetId, String actorId, String gateId) {
		return getPendingIntentActor(context, widgetId, actorId, gateId, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	/**
	 * Cancels pendingIntent (that means, that actor cannot be clicked)
	 *
	 * @param context
	 * @param widgetId
	 * @param actorId
	 * @param gateId
	 * @return
	 */
	public static PendingIntent cancelPendingIntentActorChangeRequest(Context context, int widgetId, String actorId, String gateId) {
		return getPendingIntentActor(context, widgetId, actorId, gateId, PendingIntent.FLAG_CANCEL_CURRENT);
	}

	/**
	 * General factory for when actor is changed
	 *
	 * @param context
	 * @param widgetId
	 * @param actorId
	 * @param gateId
	 * @param flag
	 * @return
	 */
	private static PendingIntent getPendingIntentActor(Context context, int widgetId, String actorId, String gateId, int flag) {
		Intent intent = getIntentActorChangeRequest(context, actorId, gateId);
		// requestNum guarantees that PendingIntent is unique
		int requestNum = widgetId + gateId.hashCode() + actorId.hashCode();
		return PendingIntent.getService(context, requestNum, intent, flag);
	}

	/**
	 * When widget is being deleted, calls service to clear data after it's deleted
	 *
	 * @param context
	 * @param widgetIds is possible to delete more widgets at once
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
	 *
	 * @param context
	 * @param widgetId
	 * @return
	 */
	public static Intent getIntentWidgetChangeLayout(Context context, int widgetId, int minWidth, int minHeight) {
		Intent intent = new Intent(context, WidgetService.class);
		intent.putExtra(EXTRA_CHANGE_LAYOUT, true);
		intent.putExtra(EXTRA_CHANGE_LAYOUT_MIN_WIDTH, minWidth);
		intent.putExtra(EXTRA_CHANGE_LAYOUT_MIN_HEIGHT, minHeight);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetId});
		return intent;
	}

	// -------------------------------------------------------------------- //
	// ------------------------- Broadcast receiver ----------------------- //
	// -------------------------------------------------------------------- //


	/**
	 * Recreates intent filter and add default actions
	 *
	 * @return IntentFilter
	 */
	private IntentFilter recreateIntentFilter() {
		Log.v(TAG, "recreateIntentFilter()");
		mClockFilterReg = false;
		mIntentFilter = new IntentFilter();
		addDefaultFilters();
		return mIntentFilter;
	}

	/**
	 * Default intents getting by broadcasts
	 */
	private void addDefaultFilters() {
		// --- maintaing broadcasts
		mIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
		mIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		mIntentFilter.addAction(Intent.ACTION_LOCALE_CHANGED);
		mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		// --- beeeOn broadcasts
		mIntentFilter.addAction(Constants.BROADCAST_USER_LOGIN);
		mIntentFilter.addAction(Constants.BROADCAST_USER_LOGOUT);
		mIntentFilter.addAction(Constants.BROADCAST_ACTOR_CHANGED);
		mIntentFilter.addAction(Constants.BROADCAST_PREFERENCE_CHANGED);
	}

	/**
	 * Intents for updating time
	 */
	private void addTimeFilters() {
		Log.v(TAG, "addTimeFilters()");
		mClockFilterReg = true;
		// --- time broadcasts
		mIntentFilter.addAction(Intent.ACTION_TIME_TICK);
		mIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		mIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
	}

	/**
	 * Restarts the receiver with actual mIntentFilter
	 */
	private void restartReceiver() {
		unregisterReceiver(mServiceReceiver);
		registerReceiver(mServiceReceiver, mIntentFilter);
	}

	private final BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
		// this states could service be in
		private static final int SERVICE_NOT_CHANGED = 0;
		private static final int SERVICE_PAUSE = 1;
		private static final int SERVICE_UPDATE = 2;
		private static final int SERVICE_UPDATE_FORCED = 3;
		private static final int SERVICE_STANDBY = 4;


		@Override
		public void onReceive(Context context, Intent intent) {
			int serviceState = SERVICE_NOT_CHANGED;
			String receivedAction = intent.getAction();
			Log.d(TAG, "broadcastReceived: " + receivedAction);

			switch (receivedAction) {
				// if time was changed (any way), update clock widgets
				case Intent.ACTION_TIME_TICK:
				case Intent.ACTION_TIME_CHANGED:
				case Intent.ACTION_TIMEZONE_CHANGED:
					updateClockWidgets();
					break;

				// if locale changed -> need to use different language in common strings
				case Intent.ACTION_LOCALE_CHANGED:
					mCalendar = Calendar.getInstance(mContext.getResources().getConfiguration().locale);
					WidgetClockData.reloadWeekDays();
					break;

				// if any actor value was changed, tell the service to refresh widget with that module
				case Constants.BROADCAST_ACTOR_CHANGED:
					final String gateId = intent.getStringExtra(Constants.BROADCAST_EXTRA_ACTOR_CHANGED_GATE_ID);
					final String actorId = intent.getStringExtra(Constants.BROADCAST_EXTRA_ACTOR_CHANGED_ID);

					if (gateId == null || gateId.isEmpty() || actorId == null || actorId.isEmpty()) {
						Log.e(TAG, "Not all data received from actor change broadcast");
						return;
					}

					// redrawing all actors in widgets asynchronously
					new Thread(new Runnable() {
						@Override
						public void run() {
							changeWidgetActorResult(gateId, actorId);
						}
					}).start();
					break;

				// if screen went on, update clocks + tell the service
				case Intent.ACTION_SCREEN_ON:
					updateClockWidgets();
					if (mController.isLoggedIn()) {
						// if user is logged in - update service
						serviceState = SERVICE_UPDATE;
					}
					break;

				// if screen went off, tell the service + restart broadcast receiver
				case Intent.ACTION_SCREEN_OFF:
					recreateIntentFilter();
					restartReceiver();
					serviceState = SERVICE_STANDBY;
					break;

				// when user logouts
				case Constants.BROADCAST_USER_LOGOUT:
					serviceState = SERVICE_PAUSE;
					break;

				// when user login
				case Constants.BROADCAST_USER_LOGIN:
					serviceState = SERVICE_UPDATE;
					break;
				// changed preferences to show different units
				case Constants.BROADCAST_PREFERENCE_CHANGED:
					serviceState = SERVICE_UPDATE_FORCED;
					break;

				// connection changed
				case ConnectivityManager.CONNECTIVITY_ACTION:
					int networkType = Utils.getNetworkConnectionType(context);
					// is connected to the internet
					if (networkType > -1) {
						// only changed connection, recalculate widgets cause some can have update only on wi-fi
						if (networkType != mNetworkType) {
							serviceState = SERVICE_UPDATE;
						}
					} else {
						serviceState = SERVICE_PAUSE;
					}
					mNetworkType = networkType;
					break;
			}

			// if anything changed state of service, do it
			switch (serviceState) {
				case SERVICE_PAUSE:
					setAllWidgetsCached();
				case SERVICE_STANDBY:
					mServiceIsStandby = true;
					stopAlarm();
					Log.i(TAG, "stopped updating service...");
					break;

				case SERVICE_UPDATE_FORCED:
					context.startService(getIntentForceUpdate(context, new int[]{}));
					break;

				case SERVICE_UPDATE:
					mServiceIsStandby = false;
					context.startService(getIntentUpdate(context));
					break;
			}
		}
	};
}
