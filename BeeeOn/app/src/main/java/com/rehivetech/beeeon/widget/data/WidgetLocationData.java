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
import com.rehivetech.beeeon.widget.persistence.WidgetLocation;
import com.rehivetech.beeeon.widget.receivers.WidgetLocationListProvider;
import com.rehivetech.beeeon.widget.service.WidgetListService;
import com.rehivetech.beeeon.widget.configuration.WidgetConfiguration;
import com.rehivetech.beeeon.widget.configuration.WidgetConfigurationActivity;
import com.rehivetech.beeeon.widget.configuration.WidgetLocationConfiguration;

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

    public WidgetLocation widgetLocation;
    private List<Location> mLocations;

    public WidgetLocationData(int widgetId, Context context, UnitsHelper unitsHelper, TimeHelper timeHelper){
        super(widgetId, context, unitsHelper, timeHelper);
        widgetLocation = new WidgetLocation(mContext, mWidgetId, 0, 0); // TODO add view;
        mLocations = new ArrayList<>();
        load();
    }

    @Override
    protected void load() {
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
    protected void initLayout() {
        super.initLayout();

        // sets onclick "listeners"
        mRemoteViews.setOnClickPendingIntent(R.id.refresh, mRefreshPendingIntent);
        mRemoteViews.setOnClickPendingIntent(R.id.options, mConfigurationPendingIntent);

        mRemoteViewsFactoryIntent = new Intent(mContext, WidgetListService.class);
        mRemoteViewsFactoryIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
        mRemoteViewsFactoryIntent.setData(Uri.parse(mRemoteViewsFactoryIntent.toUri(Intent.URI_INTENT_SCHEME)));
        mRemoteViewsFactoryIntent.putExtra(EXTRA_LOCATION_ID, widgetLocation.id);
        mRemoteViewsFactoryIntent.putExtra(EXTRA_LOCATION_ADAPTER_ID, adapterId);

        // TODO pridat click listener pro otevreni lokace

        // OnCLickListener
        Intent clickIntent = new Intent(mContext, WidgetLocationListProvider.class);
        clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{mWidgetId});

        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.name, pendingIntent);

        Compatibility.setRemoteAdapter(mRemoteViews, mWidgetId, mRemoteViewsFactoryIntent, R.id.widget_sensor_list_view);
        mRemoteViews.setEmptyView(R.id.widget_sensor_list_view, R.id.empty_view);

        mLocations.clear();
        mLocations.add(new Location(widgetLocation.id, widgetLocation.name, adapterId, widgetLocation.type));
    }

    @Override
    protected boolean updateData() {
        Location location = mController.getLocationsModel().getLocation(adapterId, widgetLocation.id);
        if(location != null){
            Adapter adapter = mController.getAdaptersModel().getAdapter(adapterId);
            widgetLocation.change(location, adapter, mUnitsHelper, mTimeHelper);

            widgetLastUpdate = getTimeNow();
            adapterId = adapter.getId();

            mWidgetManager.notifyAppWidgetViewDataChanged(mWidgetId, R.id.layout);

            this.save();
            Log.v(TAG, String.format("Updating widget (%d) with fresh data", getWidgetId()));
            return true;
        }

        Log.v(TAG, String.format("Updating widget (%d) with cached data", getWidgetId()));
        return false;
    }

    @Override
    protected void updateLayout() {
        mRemoteViews.setTextViewText(R.id.name, widgetLocation.name);
        mRemoteViews.setImageViewResource(R.id.icon, Location.LocationIcon.fromValue(widgetLocation.type).getIconResource());

        // Here we setup the a pending intent template. Individuals items of a collection
        // cannot setup their own pending intents, instead, the collection as a whole can
        // setup a pending intent template, and the individual items can set a fillInIntent
        // to create unique before on an item to item basis.
        Intent toastIntent = new Intent(mContext, WidgetLocationListProvider.class);
        toastIntent.setAction(WidgetLocationData.OPEN_DETAIL_ACTION);
        toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);

        mRemoteViewsFactoryIntent.setData(Uri.parse(mRemoteViewsFactoryIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent toastPendingIntent = PendingIntent.getBroadcast(mContext, 0, toastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setPendingIntentTemplate(R.id.widget_sensor_list_view, toastPendingIntent);
    }

    @Override
    public WidgetConfiguration createConfiguration(WidgetConfigurationActivity activity, boolean isWidgetEditing) {
        return new WidgetLocationConfiguration(this, activity, isWidgetEditing);

    }

    @Override
    public String getClassName() {
        return WidgetLocationData.class.getName();
    }

}
