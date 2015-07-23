package com.rehivetech.beeeon.widget.service;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.widget.data.WidgetLocationData;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetListService extends RemoteViewsService {
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new ListRemoteViewsFactory(this, intent);
	}
}

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
	private static final String TAG = ListRemoteViewsFactory.class.getSimpleName();

	private List<Device> mDevices;
	private List<Module> mModules;

	private TimeHelper mTimeHelper;
	private UnitsHelper mUnitsHelper;

	private Context mContext;
	private Controller mController;
	private int mWidgetId;

	private String mLocationId;
	private String mLocationGateId;

	public ListRemoteViewsFactory(Context context, Intent intent) {
		mContext = context.getApplicationContext();
		mWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		mModules = new ArrayList<>();

		mLocationId = intent.getStringExtra(WidgetLocationData.EXTRA_LOCATION_ID);
		mLocationGateId = intent.getStringExtra(WidgetLocationData.EXTRA_LOCATION_GATE_ID);
	}

	public void onCreate() {
		Log.d(TAG, "onCreate()");
		// In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
		// for example downloading or creating content etc, should be deferred to onDataSetChanged()
		// or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
		mController = Controller.getInstance(mContext);
		SharedPreferences userSettings = mController.getUserSettings();
		// UserSettings can be null when user is not logged in!
		mUnitsHelper = (userSettings == null) ? null : new UnitsHelper(userSettings, mContext);
		mTimeHelper = (userSettings == null) ? null : new TimeHelper(userSettings);
	}

	public void onDestroy() {
		// In onDestroy() you should tear down anything that was setup for your data source,
		// eg. cursors, connections, etc.
	}

	public int getCount() {
		return mModules.size();
	}

	public RemoteViews getViewAt(int position) {
		// position will always range from 0 to getCount() - 1.

		// We construct a remote views item based on our widget item xml file, and set the  text based on the position.
		RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_service_factory_views_remote_location_list);

		Module module = mModules.get(position);
		if (module == null) {
			Log.d(TAG, "NOT FOUND MODULE BY POS");
			return rv;
		}

		Gate gate = mController.getGatesModel().getGate(module.getDevice().getGateId());

		rv.setTextViewText(R.id.widget_loc_item_name, module.getName(mContext));
		rv.setImageViewResource(R.id.widget_loc_item_icon, module.getIconResource(IconResourceType.DARK));

		rv.setTextViewText(R.id.widget_loc_item_update, mTimeHelper.formatLastUpdate(module.getDevice().getLastUpdate(), gate));
		rv.setTextViewText(R.id.widget_loc_item_value, mUnitsHelper != null ? mUnitsHelper.getStringValueUnit(module.getValue()) : module.getValue().getRawValue());

		// send broadcast to widgetprovider with information about clicked item
		Bundle extras = new Bundle();
		extras.putString(WidgetLocationData.EXTRA_ITEM_DEV_ID, module.getId());
		extras.putString(WidgetLocationData.EXTRA_ITEM_GATE_ID, module.getDevice().getGateId());
		Intent fillInIntent = new Intent();
		fillInIntent.putExtras(extras);
		rv.setOnClickFillInIntent(R.id.widget_loc_item, fillInIntent);
		//*/

		// Return the remote views object.
		return rv;
	}

	public RemoteViews getLoadingView() {
		// You can create a custom loading view (for instance when getViewAt() is slow.) If you
		// return null here, you will get the default loading view.
		return null;
	}

	public int getViewTypeCount() {
		return 1;
	}

	public long getItemId(int position) {
		// TODO dev.hashcode() ?
		return position;
	}

	// TODO nevim co by melo byt (bylo true)
	public boolean hasStableIds() {
		return false;
	}

	public void onDataSetChanged() {
		Log.d(TAG, String.format("onDataSetChanged(%d), locId=%s, adId=%s", mWidgetId, mLocationId, mLocationGateId));

		// TODO problem if logged out
		// TODO problem when changed room
		// TODO checking if new data not all the time refresh

		mController = Controller.getInstance(mContext);
		try {
			mController.getLocationsModel().reloadLocationsByGate(mLocationGateId, false);
			mDevices = mController.getDevicesModel().getDevicesByLocation(mLocationGateId, mLocationId);
		} catch (AppException e) {
			e.printStackTrace();
		}


		Log.d(TAG, String.format("mfacit length = %d", mDevices.size()));
		mModules.clear();
		for (Device fac : mDevices) {
			if (fac == null) continue;

			Log.d("FAC: ", fac.getAllModules().get(0).getName(mContext));
			mModules.addAll(fac.getAllModules());
		}
	}
}