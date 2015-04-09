package com.rehivetech.beeeon.widget;

import java.util.ArrayList;
import java.util.List;

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
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.widget.location.WidgetLocationData;
import com.rehivetech.beeeon.widget.location.WidgetLocationListProvider;

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

    public class WidgetListItem {
        public String text;
        public WidgetListItem(String text) {
            this.text = text;
        }
    }

    private List<WidgetListItem> mWidgetListItems = new ArrayList<WidgetListItem>();

    private Context mContext;
    private Controller mController;
    private int mWidgetId;

    private String mLocationId;
    private String mLocationAdapterId;

    public ListRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mController = Controller.getInstance(context);
        mWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        mDevices = new ArrayList<>();

        mLocationId = intent.getStringExtra(WidgetLocationListProvider.EXTRA_LOCATION_ID);
        mLocationAdapterId = intent.getStringExtra(WidgetLocationListProvider.EXTRA_ITEM_ADAPTER_ID);

        SharedPreferences userSettings = mController.getUserSettings();

        // UserSettings can be null when user is not logged in!
        mUnitsHelper = (userSettings == null) ? null : new UnitsHelper(userSettings, mContext);
        mTimeHelper = (userSettings == null) ? null : new TimeHelper(userSettings);
    }

    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
    }

    public void onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        mWidgetListItems.clear();
    }

    public int getCount() {
        return mDevices.size();
    }

    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.

        // We construct a remote views item based on our widget item xml file, and set the
        // text based on the position.
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
        extras.putString(WidgetLocationListProvider.EXTRA_ITEM_DEV_ID, dev.getId());
        extras.putString(WidgetLocationListProvider.EXTRA_ITEM_ADAPTER_ID, dev.getFacility().getAdapterId());
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.widget_loc_item, fillInIntent);

        // You can do heaving lifting in here, synchronously. For example, if you need to
        // process an image, fetch something from the network, etc., it is ok to do it here,
        // synchronously. A loading view will show up in lieu of the actual contents in the
        // interim.

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
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        Log.d(TAG, String.format("onDataSetChanged(%d)", mWidgetId));

        // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
        // on the collection view corresponding to this factory. You can do heaving lifting in
        // here, synchronously. For example, if you need to process an image, fetch something
        // from the network, etc., it is ok to do it here, synchronously. The widget will remain
        // in its current state while work is being done here, so you don't need to worry about
        // locking up the widget.

        // TODO problem if logged out
        // TODO problem when changed room
        // TODO checking if new data not all the time refresh

;

        mFacilities = mController.getFacilitiesModel().getFacilitiesByLocation(mLocationAdapterId, mLocationId);
        for(Facility fac : mFacilities){
            if(fac == null) continue;

            Log.d("FAC: ", fac.getDevices().get(0).getName());
            mDevices.addAll(fac.getDevices());
        }
    }
}