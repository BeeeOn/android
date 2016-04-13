package com.rehivetech.beeeon.gcm.analytics;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Map;

/**
 * Created by martin on 5.4.16.
 */
public class GoogleAnalyticsManager {

	public static final String ADD_DASHBOARD_ACTUAL_VALUE_SCREEN = "AddDashboardActualValueScreen";
	public static final String ADD_DASHBOARD_GRAPH_ITEM_SCREEN = "AddDashboardGraphItemScreen";
	public static final String ADD_DASHBOARD_ITEM_SCREEN = "AddDashboardItemScreen";
	public static final String ADD_DASHBOARD_OVERVIEW_GRAPH_ITEM_SCREEN = "AddDashboardOverviewGraphItemScreen";
	public static final String ADD_DASHBOARD_VENTILATION_HELPER_SCREEN = "AddDahboardVentilationHelperScreen";
	public static final String ADD_DEVICE_SCREEN = "AddDeviceScreen";
	public static final String ADD_GATE_SCREEN = "AddGateScreen";
	public static final String ADD_GATE_USER_SCREEN = "AddGateUserScreen";
	public static final String DASHBOARD_SCREEN= "DashboardScreen";
	public static final String DASHBOARD_GRAPH_DETAIL_SCREEN = "DashboardGraphDetailScreen";
	public static final String DASHBOARD_OVERVIEW_GRAPH_DETAIL_SCREEN = "DashboardOverviewGraphDetailScreen";
	public static final String DEVICE_EDIT_SCREEN = "DeviceEditScreen";
	public static final String GATE_DETAIL_SCREEN = "GateDetailScreen";
	public static final String GATE_EDIT_SCREEN = "GateEditScreen";
	public static final String INTRO_SCREEN = "IntroScreen";
	public static final String MODULE_GRAPH_DETAIL_SCREEN = "ModuleGraphDetailScreen";
	public static final String NOTIFICATION_LIST_SCREEN = "NotificationScreen";
	public static final String SEARCH_DEVICE_SCREEN = "SearchDeviceScreen";
	public static final String SETTINGS_SCREEN = "SettingsScreen";
	public static final String SETUP_DEVICE_SCREEN = "SetupDeviceScreen";
	public static final String DEVICE_LIST_SCREEN = "DeviceListScreen";
	public static final String DEVICE_DETAIL_SCREEN = "DeviceDetailScreen";

	//----- EVENT CATEGORIES -------
	public static final String EVENT_CATEGORY_DASHBOARD = "Dashboard";
		public static final String EVENT_CATEGORY_MODULE_GRAPH_DETAIL = "ModuleGraphDetail";

	//----- EVENT ACTIONS ----------
	public static final String EVENT_ACTION_ADD_ITEM = "addItem";
	public static final String EVENT_ACTION_DETAIL_CLICK = "detailClick";

	public static final String EVENT_ACTION_SELECT_TAB = "selectTab";
	public static final String EVENT_ACTION_OPEN_GRAPH_SETTINGS = "openGraphSettings";

	//----- EVENT LABELS -----------
	public static final String DASHBOARD_ADD_ACTUAL_VALUE_ITEM = "ActualValueItem";
	public static final String DASHBOARD_ADD_GRAPH_ITEM = "GraphItem";
	public static final String DASHBOARD_ADD_GRAPH_OVERVIEW_ITEM = "GraphOverViewItem";
	public static final String DASHBOARD_ADD_VENTILATION_ITEM = "VentilationItem";

	public static final String DASHBOARD_DETAIL_CLICK_ACTUAL_VALUE_ITEM = "ActualValueItemDetailClick";
	public static final String DASHBOARD_DETAIL_CLICK_GRAPH_ITEM = "GraphItemDetailClick";
	public static final String DASHBOARD_DETAIL_CLICK_GRAPH_OVERVIEW_ITEM = "GraphOverviewItemDetailClick";


	private static GoogleAnalyticsManager sInstance;
	private Tracker mTracker;

	public static GoogleAnalyticsManager getInstance() {
		if (sInstance == null) {
			synchronized (GoogleAnalyticsManager.class) {
				if (sInstance == null) {
					sInstance = new GoogleAnalyticsManager();
				}
			}
		}
		return sInstance;
	}

	private GoogleAnalyticsManager() {

	}

	public void init(Context context, String trackerId) {
		mTracker = GoogleAnalytics.getInstance(context).newTracker(trackerId);

		mTracker.setSessionTimeout(300);

		// Report uncaught exceptions
		mTracker.enableExceptionReporting(true);
		mTracker.enableAutoActivityTracking(false);
	}

	public void logScreen(String screenName) {
		if (mTracker != null) {
			mTracker.setScreenName(screenName);
			mTracker.send(new HitBuilders.ScreenViewBuilder().build());
		}
	}

	public void logEvent(String eventCategory, String eventAction, String eventLabel) {
		if (mTracker != null) {
			Map<String, String> event = new HitBuilders.EventBuilder()
					.setCategory(eventCategory)
					.setAction(eventAction)
					.setLabel(eventLabel)
					.build();

			mTracker.send(event);
		}
	}
}
