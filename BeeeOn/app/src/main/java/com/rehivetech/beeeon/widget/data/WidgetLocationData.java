package com.rehivetech.beeeon.widget.data;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.widget.persistence.WidgetLocationPersistence;
import com.rehivetech.beeeon.widget.receivers.WidgetLocationListProvider;
import com.rehivetech.beeeon.widget.service.WidgetListService;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for location list app widget (3x2)
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetLocationData extends WidgetData {
	private static final String TAG = WidgetLocationData.class.getSimpleName();

	public static final String OPEN_DETAIL_ACTION = "com.rehivetech.beeeon.widget.locationlist.OPEN_DETAIL_ACTION";
	public static final String EXTRA_ITEM_DEV_ID = "com.rehivetech.beeeon.widget.locationlist.ITEM_DEV_ID";
	public static final String EXTRA_ITEM_GATE_ID = "com.rehivetech.beeeon.widget.locationlist.ITEM_ADAPTER_ID";
	public static final String EXTRA_LOCATION_ID = "com.rehivetech.beeeon.widget.locationlist.LOCATON_ID";
	public static final String EXTRA_LOCATION_GATE_ID = "com.rehivetech.beeeon.widget.locationlist.LOCATON_ADAPTER_ID";

	protected Intent mRemoteViewsFactoryIntent;

	public WidgetLocationPersistence widgetLocation;
	private List<Location> mLocations;

	/**
	 * Constructing object holding information about widget (instantiating in config activity and then in service)
	 *
	 * @param widgetId
	 * @param context
	 * @param unitsHelper
	 * @param timeHelper
	 */
	public WidgetLocationData(int widgetId, Context context, UnitsHelper unitsHelper, TimeHelper timeHelper) {
		super(widgetId, context, unitsHelper, timeHelper);
		widgetLocation = new WidgetLocationPersistence(mContext, mWidgetId, 0, 0, mUnitsHelper, mTimeHelper, settings);
		mLocations = new ArrayList<>();
	}

	// ----------------------------------------------------------- //
	// ---------------- MANIPULATING PERSISTENCE ----------------- //
	// ----------------------------------------------------------- //

	@Override
	public void load() {
		super.load();
		widgetLocation.load();
	}

	@Override
	public void init() {
		mLocations.clear();
		mLocations.add(new Location(widgetLocation.id, widgetLocation.name, widgetGateId, widgetLocation.type));
	}

	@Override
	public void save() {
		super.save();
		widgetLocation.save();
	}

	// ----------------------------------------------------------- //
	// ------------------------ RENDERING ------------------------ //
	// ----------------------------------------------------------- //

	@Override
	protected void renderLayout() {
		// -------------------- initialize layout
		// sets onclick "listeners"
		mBuilder.setOnClickListener(R.id.options, mConfigurationPendingIntent);
		mBuilder.setOnClickListener(R.id.refresh, mRefreshPendingIntent);

		// TODO scroll to location?
		mBuilder.setOnClickListener(R.id.icon, startMainActivityPendingIntent(mContext, widgetGateId));
		mBuilder.setOnClickListener(R.id.menu_empty_listview_login_name_text, startMainActivityPendingIntent(mContext, widgetGateId));

		// onclick listener when clicked on item
		mRemoteViewsFactoryIntent = new Intent(mContext, WidgetListService.class);
		mRemoteViewsFactoryIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
		mRemoteViewsFactoryIntent.setData(Uri.parse(mRemoteViewsFactoryIntent.toUri(Intent.URI_INTENT_SCHEME)));
		mRemoteViewsFactoryIntent.putExtra(EXTRA_LOCATION_ID, widgetLocation.id);
		mRemoteViewsFactoryIntent.putExtra(EXTRA_LOCATION_GATE_ID, widgetGateId);

		mBuilder.setRemoteAdapter(R.id.widget_sensor_list_view, mWidgetId, mRemoteViewsFactoryIntent);
		mBuilder.setEmptyView(R.id.widget_sensor_list_view, R.id.empty_view);

		// -------------------- render layout
		widgetLocation.renderView(mBuilder);

		// intent open detail by item
		Intent openDetailIntent = new Intent(mContext, WidgetLocationListProvider.class);
		openDetailIntent.setAction(WidgetLocationData.OPEN_DETAIL_ACTION);
		openDetailIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);

		mRemoteViewsFactoryIntent.setData(Uri.parse(mRemoteViewsFactoryIntent.toUri(Intent.URI_INTENT_SCHEME)));
		PendingIntent openDetailPendingIntent = PendingIntent.getBroadcast(mContext, 0, openDetailIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.getRoot().setPendingIntentTemplate(R.id.widget_sensor_list_view, openDetailPendingIntent);
	}

	// ----------------------------------------------------------- //
	// ---------------------- FAKE HANDLERS ---------------------- //
	// ----------------------------------------------------------- //

	@Override
	public boolean handleUpdateData() {
		Controller controller = Controller.getInstance(mContext);
		Location location = controller.getLocationsModel().getLocation(widgetGateId, widgetLocation.id);
		if (location == null) {
			Log.v(TAG, String.format("Updating widget (%d) with cached data", getWidgetId()));
			return false;
		}

		Gate gate = controller.getGatesModel().getGate(widgetGateId);
		if (gate == null) return false;
		widgetLocation.configure(location, gate);

		widgetLastUpdate = getTimeNow();
		widgetGateId = gate.getId();

		mWidgetManager.notifyAppWidgetViewDataChanged(mWidgetId, R.id.layout);

		this.save();
		Log.v(TAG, String.format("Updating widget (%d) with fresh data", getWidgetId()));
		return true;
	}

	// ----------------------------------------------------------- //
	// ------------------------- GETTERS ------------------------- //
	// ----------------------------------------------------------- //

	@Override
	public List<Location> getObjectsToReload() {
		return mLocations;
	}

	@Override
	public String getClassName() {
		return WidgetLocationData.class.getName();
	}

}