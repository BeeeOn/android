package com.rehivetech.beeeon.widget.location;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.Compatibility;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.widget.WidgetData;
import com.rehivetech.beeeon.widget.WidgetListService;

/**
 * Class for location list app widget (3x2)
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetLocationData extends WidgetData {
    private static final String TAG = WidgetLocationData.class.getSimpleName();

    private static final String PREF_LOCATION_ID = "location_id";
    private static final String PREF_LOCATION_NAME = "location_name";
    private static final String PREF_LOCATION_TYPE = "location_type";

    public static final String OPEN_DETAIL_ACTION = "com.rehivetech.beeeon.widget.locationlist.OPEN_DETAIL_ACTION";
    public static final String EXTRA_ITEM_DEV_ID = "com.rehivetech.beeeon.widget.locationlist.ITEM_DEV_ID";
    public static final String EXTRA_ITEM_ADAPTER_ID = "com.rehivetech.beeeon.widget.locationlist.ITEM_ADAPTER_ID";
    public static final String EXTRA_LOCATION_ID = "com.rehivetech.beeeon.widget.locationlist.LOCATON_ID";
    public static final String EXTRA_LOCATION_ADAPTER_ID = "com.rehivetech.beeeon.widget.locationlist.LOCATON_ADAPTER_ID";

    
    // publicly accessible properties of widget
    public String locationId;
    public String locationName;
    public int locationType;
    protected Location mLocation;
    protected Intent mRemoteViewsFactoryIntent;

    public WidgetLocationData(int widgetId, Context context) {
        super(widgetId, context);
        mClassName = WidgetLocationData.class.getName();
    }

    @Override
    public void initLayout() {
        super.initLayout();

        // sets onclick "listeners"
        mRemoteViews.setOnClickPendingIntent(R.id.refresh, mRefreshPendingIntent);
        mRemoteViews.setOnClickPendingIntent(R.id.options, mConfigurationPendingIntent);

        // Here we setup the intent which points to the StackViewService which will
        // provide the views for this collection.
        mRemoteViewsFactoryIntent = new Intent(mContext, WidgetListService.class);
        mRemoteViewsFactoryIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
        mRemoteViewsFactoryIntent.setData(Uri.parse(mRemoteViewsFactoryIntent.toUri(Intent.URI_INTENT_SCHEME)));

        mRemoteViewsFactoryIntent.putExtra(EXTRA_LOCATION_ID, locationId);
        mRemoteViewsFactoryIntent.putExtra(EXTRA_LOCATION_ADAPTER_ID, adapterId);

        // OnCLickListener
        Intent clickIntent = new Intent(mContext, WidgetLocationListProvider.class);
        clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // TODO
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { mWidgetId });


        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.name, pendingIntent);

        Compatibility.setRemoteAdapter(mRemoteViews, mWidgetId, mRemoteViewsFactoryIntent, R.id.widget_sensor_list_view);
        mRemoteViews.setEmptyView(R.id.widget_sensor_list_view, R.id.empty_view);

        if(mLocation == null){
            // TODO change in service
            // TODO need to probably reloadLocations or something!
            mLocation = mController.getLocationsModel().getLocation(adapterId, locationId);
            if(mLocation == null) mLocation = new Location(locationId, locationName, adapterId, locationType);
            // TODO what if location not found ??? because of not logged in user
        }

    }

    @Override
    public void whenUserLogout(){
        Log.d(TAG, String.format("whenUserLogout(%d)", mWidgetId));

        mRemoteViews.setTextViewText(R.id.name, "");
        mRemoteViews.setTextViewText(R.id.empty_view, mContext.getString(R.string.widget_configuration_login_first));
        updateLayout();
    }

    @Override
    public void whenUserLogin(){
        Log.d(TAG, String.format("whenUserLogin(%d)", mWidgetId));
        mRemoteViews.setTextViewText(R.id.empty_view, mContext.getString(R.string.widget_location_list_empty));
        updateLayout();
    }

    @Override
    public void changeData() {
        Log.d(TAG, "changeData()");

        if(locationId.isEmpty() || adapterId.isEmpty()) return;

        locationType = mLocation.getType();
        locationName = mLocation.getName();
        adapterId = mLocation.getAdapterId();
        lastUpdate = SystemClock.elapsedRealtime();

        // TODO tady by melo byt notifyappwidgetviewdatachanged

        saveData(mContext);

        Log.v(TAG, String.format("Updating widget (%d) with fresh data", getWidgetId()));
    }

    @Override
    public void setLayoutValues(){
        Log.d(TAG, String.format("setLayoutValues(%d)", mWidgetId));

        mRemoteViews.setTextViewText(R.id.name, locationName);
        mRemoteViews.setImageViewResource(R.id.icon, Location.LocationIcon.fromValue(locationType).getIconResource());

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

        mWidgetManager.notifyAppWidgetViewDataChanged(mWidgetId, R.id.layout);
        updateLayout();
    }

    @Override
    public void loadData(Context context) {
        super.loadData(context);

        locationId = mPrefs.getString(PREF_LOCATION_ID, "");
        locationName = mPrefs.getString(PREF_LOCATION_NAME, context.getString(R.string.placeholder_not_exists));
        locationType = mPrefs.getInt(PREF_LOCATION_TYPE, 0);
    }

    @Override
    public void saveData(Context context) {
        super.saveData(context);

        getSettings(context).edit()
                .putString(PREF_LOCATION_ID, locationId)
                .putString(PREF_LOCATION_NAME, locationName)
                .putInt(PREF_LOCATION_TYPE, locationType)
                .commit();
    }
}
