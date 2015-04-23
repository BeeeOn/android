package com.rehivetech.beeeon.widget.data;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.Compatibility;
import com.rehivetech.beeeon.util.Log;
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
    public static final String EXTRA_ITEM_ADAPTER_ID = "com.rehivetech.beeeon.widget.locationlist.ITEM_ADAPTER_ID";
    public static final String EXTRA_LOCATION_ID = "com.rehivetech.beeeon.widget.locationlist.LOCATON_ID";
    public static final String EXTRA_LOCATION_ADAPTER_ID = "com.rehivetech.beeeon.widget.locationlist.LOCATON_ADAPTER_ID";


    protected Intent mRemoteViewsFactoryIntent;

    public WidgetLocationPersistence widgetLocation;
    private List<Location> mLocations;

    public WidgetLocationData(int widgetId, Context context, UnitsHelper unitsHelper, TimeHelper timeHelper){
        super(widgetId, context, unitsHelper, timeHelper);
        widgetLocation = new WidgetLocationPersistence(mContext, mWidgetId, 0, 0, mUnitsHelper, mTimeHelper);
        mLocations = new ArrayList<>();
    }

    @Override
    public void init() {
        mLocations.clear();
        mLocations.add(new Location(widgetLocation.getId(), widgetLocation.getName(), adapterId, widgetLocation.type));
    }

    @Override
    public void load() {
        super.load();
        widgetLocation.load();
    }

    @Override
    protected void save() {
        super.save();
        widgetLocation.save();
    }

    @Override
    public void delete(Context context) {
        super.delete(context);
        widgetLocation.delete();
    }

    @Override
    public List<Location> getReferredObj() {
        return mLocations;
    }

    @Override
    public void initLayout() {
        super.initLayout();

        // sets onclick "listeners"
        mRemoteViews.setOnClickPendingIntent(R.id.options, mConfigurationPendingIntent);
        mRemoteViews.setOnClickPendingIntent(R.id.refresh, mRefreshPendingIntent);

        // TODO scroll to location?
        mRemoteViews.setOnClickPendingIntent(R.id.icon, startMainActivityPendingIntent(mContext, adapterId));
        mRemoteViews.setOnClickPendingIntent(R.id.name, startMainActivityPendingIntent(mContext, adapterId));

        // onclick listener when clicked on item
        mRemoteViewsFactoryIntent = new Intent(mContext, WidgetListService.class);
        mRemoteViewsFactoryIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
        mRemoteViewsFactoryIntent.setData(Uri.parse(mRemoteViewsFactoryIntent.toUri(Intent.URI_INTENT_SCHEME)));
        mRemoteViewsFactoryIntent.putExtra(EXTRA_LOCATION_ID, widgetLocation.getId());
        mRemoteViewsFactoryIntent.putExtra(EXTRA_LOCATION_ADAPTER_ID, adapterId);

        Compatibility.setRemoteAdapter(mRemoteViews, mWidgetId, mRemoteViewsFactoryIntent, R.id.widget_sensor_list_view);
        mRemoteViews.setEmptyView(R.id.widget_sensor_list_view, R.id.empty_view);
    }

    @Override
    protected boolean updateData() {
        Location location = mController.getLocationsModel().getLocation(adapterId, widgetLocation.getId());
        if(location == null) {
            Log.v(TAG, String.format("Updating widget (%d) with cached data", getWidgetId()));
            return false;
        }

        Adapter adapter = mController.getAdaptersModel().getAdapter(adapterId);
        widgetLocation.change(location, adapter);

        widgetLastUpdate = getTimeNow();
        adapterId = adapter.getId();

        mWidgetManager.notifyAppWidgetViewDataChanged(mWidgetId, R.id.layout);

        this.save();
        Log.v(TAG, String.format("Updating widget (%d) with fresh data", getWidgetId()));
        return true;
    }

    @Override
    protected void updateLayout() {
        mRemoteViews.setTextViewText(R.id.name, widgetLocation.getName());
        mRemoteViews.setImageViewResource(R.id.icon, Location.LocationIcon.fromValue(widgetLocation.type).getIconResource());

        // intent open detail by item
        Intent openDetailIntent = new Intent(mContext, WidgetLocationListProvider.class);
        openDetailIntent.setAction(WidgetLocationData.OPEN_DETAIL_ACTION);
        openDetailIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);

        mRemoteViewsFactoryIntent.setData(Uri.parse(mRemoteViewsFactoryIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent openDetailPendingIntent = PendingIntent.getBroadcast(mContext, 0, openDetailIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setPendingIntentTemplate(R.id.widget_sensor_list_view, openDetailPendingIntent);
    }

    @Override
    public String getClassName() {
        return WidgetLocationData.class.getName();
    }

}
