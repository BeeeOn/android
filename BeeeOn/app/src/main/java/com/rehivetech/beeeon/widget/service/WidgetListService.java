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

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Facility;
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
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = ListRemoteViewsFactory.class.getSimpleName();

    private List<Facility> mFacilities;
    private List<Device> mDevices;

    private TimeHelper mTimeHelper;
    private UnitsHelper mUnitsHelper;

    private Context mContext;
    private Controller mController;
    private int mWidgetId;

    private String mLocationId;
    private String mLocationAdapterId;

    public ListRemoteViewsFactory(Context context, Intent intent) {
        mContext = context.getApplicationContext();
        mWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        mDevices = new ArrayList<>();

        mLocationId = intent.getStringExtra(WidgetLocationData.EXTRA_LOCATION_ID);
        mLocationAdapterId = intent.getStringExtra(WidgetLocationData.EXTRA_LOCATION_ADAPTER_ID);
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
        return mDevices.size();
    }

    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.

        // We construct a remote views item based on our widget item xml file, and set the  text based on the position.
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_location_list_item);

        Device dev = mDevices.get(position);
        if(dev == null){
            Log.d(TAG, "NOT FOUND DEVICE BY POS");
            return rv;
        }

        Adapter adapter = mController.getAdaptersModel().getAdapter(dev.getFacility().getAdapterId());

        rv.setTextViewText(R.id.widget_loc_item_name, dev.getName());
        rv.setImageViewResource(R.id.widget_loc_item_icon, dev.getIconResource());

        rv.setTextViewText(R.id.widget_loc_item_update, mTimeHelper.formatLastUpdate(dev.getFacility().getLastUpdate(), adapter));
        rv.setTextViewText(R.id.widget_loc_item_value, mUnitsHelper != null ? mUnitsHelper.getStringValueUnit(dev.getValue()) : dev.getValue().getRawValue());

        // send broadcast to widgetprovider with information about clicked item
        Bundle extras = new Bundle();
        extras.putString(WidgetLocationData.EXTRA_ITEM_DEV_ID, dev.getId());
        extras.putString(WidgetLocationData.EXTRA_ITEM_ADAPTER_ID, dev.getFacility().getAdapterId());
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.widget_loc_item, fillInIntent);


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
        Log.d(TAG, String.format("onDataSetChanged(%d), locId=%s, adId=%s", mWidgetId, mLocationId, mLocationAdapterId));

        // TODO problem if logged out
        // TODO problem when changed room
        // TODO checking if new data not all the time refresh

        mController = Controller.getInstance(mContext);
        try {
            mController.getLocationsModel().reloadLocationsByAdapter(mLocationAdapterId, false);
            mFacilities = mController.getFacilitiesModel().getFacilitiesByLocation(mLocationAdapterId, mLocationId);
        }
        catch(AppException e){
            e.printStackTrace();
        }


        Log.d(TAG, String.format("mfacit length = %d", mFacilities.size()));
        mDevices.clear();
        for(Facility fac : mFacilities){
            if(fac == null) continue;

            Log.d("FAC: ", fac.getDevices().get(0).getName());
            mDevices.addAll(fac.getDevices());
        }
    }
}