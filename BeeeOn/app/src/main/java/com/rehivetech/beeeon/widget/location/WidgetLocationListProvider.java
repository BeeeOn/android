package com.rehivetech.beeeon.widget.location;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.SensorDetailActivity;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.WidgetListService;
import com.rehivetech.beeeon.widget.WidgetData;
import com.rehivetech.beeeon.widget.WidgetProvider;
import com.rehivetech.beeeon.widget.WidgetService;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetLocationListProvider extends WidgetProvider {
    private static String TAG = WidgetLocationListProvider.class.getSimpleName();

    public static final String OPEN_DETAIL_ACTION = "com.rehivetech.beeeon.widget.locationlist.OPEN_DETAIL_ACTION";
    public static final String EXTRA_ITEM_DEV_ID = "com.rehivetech.beeeon.widget.locationlist.ITEM_DEV_ID";
    public static final String EXTRA_ITEM_ADAPTER_ID = "com.rehivetech.beeeon.widget.locationlist.ITEM_ADAPTER_ID";

    public static final String EXTRA_LOCATION_ID = "com.rehivetech.beeeon.widget.locationlist.LOCATON_ID";
    public static final String EXTRA_LOCATION_ADAPTER_ID = "com.rehivetech.beeeon.widget.locationlist.LOCATON_ADAPTER_ID";

    protected WidgetLocationData mWidgetData;
    protected Location mLocation;
    protected Intent mRemoteViewsFactoryIntent;

    /**
     * CAN'T USE ANY OF PROPERTIES !!! (NOT INITIALIZED HERE)
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // open detail activity of chosen device from list
        if (intent.getAction().equals(OPEN_DETAIL_ACTION)) {
            Intent detailIntent = new Intent(context, SensorDetailActivity.class);
            detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            detailIntent.putExtra(SensorDetailActivity.EXTRA_DEVICE_ID, intent.getStringExtra(EXTRA_ITEM_DEV_ID));
            detailIntent.putExtra(SensorDetailActivity.EXTRA_ADAPTER_ID, intent.getStringExtra(EXTRA_ITEM_ADAPTER_ID));
            context.startActivity(detailIntent);
        }
        super.onReceive(context, intent);
    }

    // ---------- methods which uses local properties

    @Override
    public void initialize(Context context, WidgetData data){
        super.initialize(context, data);
        mWidgetData = (WidgetLocationData) data;

        // sets onclick "listeners"
        mRemoteViews.setOnClickPendingIntent(R.id.refresh, mRefreshPendingIntent);
        mRemoteViews.setOnClickPendingIntent(R.id.options, mConfigurationPendingIntent);

        // Here we setup the intent which points to the StackViewService which will
        // provide the views for this collection.
        mRemoteViewsFactoryIntent = new Intent(mContext, WidgetListService.class);
        mRemoteViewsFactoryIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
        mRemoteViewsFactoryIntent.setData(Uri.parse(mRemoteViewsFactoryIntent.toUri(Intent.URI_INTENT_SCHEME)));

        mRemoteViewsFactoryIntent.putExtra(EXTRA_LOCATION_ID, mWidgetData.locationId);
        mRemoteViewsFactoryIntent.putExtra(EXTRA_LOCATION_ADAPTER_ID, mWidgetData.adapterId);

        // OnCLickListener
        Intent clickIntent = new Intent(mContext, WidgetLocationListProvider.class);
        clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        clickIntent.putExtra(mWidgetManager.EXTRA_APPWIDGET_IDS, getAllIds(mContext)); // TODO -> allWidgetIds

        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.name, pendingIntent);

        setCompatRemoteAdapter(mRemoteViewsFactoryIntent);

        // The empty view is displayed when the collection has no items. It should be a sibling
        // of the collection view.

        mRemoteViews.setEmptyView(R.id.widget_sensor_list_view, R.id.empty_view);
    }

    @Override
    public boolean prepare() {
        Log.d(TAG, String.format("prepare(%d)", mWidgetId));
        if(mWidgetData.locationId.isEmpty() || mWidgetData.adapterId.isEmpty()) return false;

        mLocation = Utils.getFromList(mWidgetData.locationId, WidgetService.usedLocations);
        if(mLocation == null){
            //location = new Location();
            // TODO change in service
            // TODO need to probably reloadLocations or something!
            mLocation = mController.getLocationsModel().getLocation(mWidgetData.adapterId, mWidgetData.locationId);
            if(mLocation == null) return false;
            // TODO what if location not found ??? because of not logged in user

            WidgetService.usedLocations.add(mLocation);
        }

		return true;
    }

    @Override
    public void whenUserLogout(){
        Log.d(TAG, String.format("whenUserLogout(%d)", mWidgetId));

        mRemoteViews.setTextViewText(R.id.name, "");
        mRemoteViews.setTextViewText(R.id.empty_view, mContext.getString(R.string.widget_configuration_login_first));
        mWidgetManager.updateAppWidget(mWidgetId, mRemoteViews);
    }

    @Override
    public void whenUserLogin(){
        Log.d(TAG, String.format("whenUserLogin(%d)", mWidgetId));
        mRemoteViews.setTextViewText(R.id.empty_view, mContext.getString(R.string.widget_location_list_empty));
        mWidgetManager.updateAppWidget(mWidgetId, mRemoteViews);
    }

    @Override
    public void changeData() {
		Log.d(TAG, "changeData()");

		if(mWidgetData.locationId.isEmpty() || mWidgetData.adapterId.isEmpty()) return;

        mWidgetData.locationIcon = mLocation.getIconResource();
        mWidgetData.locationName = mLocation.getName();
        mWidgetData.adapterId = mLocation.getAdapterId();
        mWidgetData.lastUpdate = SystemClock.elapsedRealtime();

        mWidgetData.saveData(mContext);

        Log.v(TAG, String.format("Updating widget (%d) with fresh data", mWidgetData.getWidgetId()));
	}

    @Override
    public void setValues(){
        Log.d(TAG, String.format("setValues(%d)", mWidgetId));

        mRemoteViews.setTextViewText(R.id.name, mWidgetData.locationName);
        mRemoteViews.setImageViewResource(R.id.icon, mWidgetData.locationIcon);

        // Here we setup the a pending intent template. Individuals items of a collection
        // cannot setup their own pending intents, instead, the collection as a whole can
        // setup a pending intent template, and the individual items can set a fillInIntent
        // to create unique before on an item to item basis.
        Intent toastIntent = new Intent(mContext, WidgetLocationListProvider.class);
        toastIntent.setAction(WidgetLocationListProvider.OPEN_DETAIL_ACTION);
        toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);

        mRemoteViewsFactoryIntent.setData(Uri.parse(mRemoteViewsFactoryIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent toastPendingIntent = PendingIntent.getBroadcast(mContext, 0, toastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setPendingIntentTemplate(R.id.widget_sensor_list_view, toastPendingIntent);


        mWidgetManager.notifyAppWidgetViewDataChanged(mWidgetId, R.id.layout);
        // update widget
        mWidgetManager.updateAppWidget(mWidgetId, mRemoteViews);
    }
}