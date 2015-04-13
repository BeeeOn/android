package com.rehivetech.beeeon.widget.device;

import android.content.Context;
import android.os.SystemClock;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.OnOffValue;
import com.rehivetech.beeeon.household.device.values.OpenClosedValue;
import com.rehivetech.beeeon.util.Compatibility;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.widget.WidgetData;
import com.rehivetech.beeeon.widget.WidgetService;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Class for sensor app widget (1x1, 2x1, 3x1)
 */
public class WidgetDeviceData extends WidgetData {
    private static final String TAG = WidgetDeviceData.class.getSimpleName();

    private static final String PREF_DEVICE_ID = "device";
    private static final String PREF_DEVICE_NAME = "device_name";
    private static final String PREF_DEVICE_ICON = "device_icon";
    private static final String PREF_DEVICE_VALUE = "device_value";
    private static final String PREF_DEVICE_UNIT = "device_unit";
    private static final String PREF_DEVICE_LAST_UPDATE_TEXT = "device_last_update_text";
    private static final String PREF_DEVICE_LAST_UPDATE_TIME = "device_last_update_time";
    private static final String PREF_DEVICE_REFRESH = "device_refresh";

    // publicly accessible properties of widget
    public String deviceId;
    public String deviceName;
    public int deviceIcon;
    public String deviceValue;
    public String deviceUnit;
    public long deviceLastUpdateTime;
    public String deviceLastUpdateText;
    public int deviceRefresh;

    private Facility mFacility;

    private RemoteViews mValueRemoteViews;

    public WidgetDeviceData(int widgetId, Context context) {
        super(widgetId, context);
        mClassName = WidgetDeviceData.class.getName();
    }

    @Override
    public Facility getReferredObj() {
        return mFacility;
    }

    @Override
    public void initLayout() {
        super.initLayout();

        // sets onclick "listeners"
        mRemoteViews.setOnClickPendingIntent(R.id.value, mRefreshPendingIntent);
        mRemoteViews.setOnClickPendingIntent(R.id.last_update, mRefreshPendingIntent);
        mRemoteViews.setOnClickPendingIntent(R.id.refresh, mRefreshPendingIntent);

        // configuration
        mRemoteViews.setOnClickPendingIntent(R.id.options, mConfigurationPendingIntent);

        // open detail activity on click to icon
        if (adapterId.length() > 0 && deviceId.length() > 0) {
            mRemoteViews.setOnClickPendingIntent(R.id.icon, startDetailActivityPendingIntent(mContext, mWidgetId, adapterId, deviceId));
        }

        String[] ids = deviceId.split(Device.ID_SEPARATOR, 2);

        if(mFacility == null) {
            mFacility = new Facility();
        }

        mFacility.setAdapterId(adapterId);
        mFacility.setAddress(ids[0]);

        // TODO nevim jestli toot by se nemelo menit kazdy cyklus
        mFacility.setLastUpdate(new DateTime(deviceLastUpdateTime, DateTimeZone.UTC));
        mFacility.setRefresh(RefreshInterval.fromInterval(deviceRefresh));

        Device dev = Device.createFromDeviceTypeId(ids[1]);
        mFacility.addDevice(dev);

        if(dev.getType().isActor()){
            BaseValue value = dev.getValue();

            if(value instanceof OnOffValue || value instanceof OpenClosedValue){
                mValueRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_switchcompat);
                mValueRemoteViews.setOnClickPendingIntent(R.id.widget_switchcompat, WidgetService.getActorChangePendingIntent(mContext, mWidgetId));
            }
        }
        else{
            mValueRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_value_unit);
        }

        mRemoteViews.removeAllViews(R.id.value_container);

        if(mValueRemoteViews != null) {
            mRemoteViews.addView(R.id.value_container, mValueRemoteViews);
        }
    }

    @Override
    public void changeData() {
        long timeNow = SystemClock.elapsedRealtime();

        Adapter adapter = mController.getAdaptersModel().getAdapter(adapterId);
        Device device = mController.getFacilitiesModel().getDevice(adapterId, deviceId);

        if (device != null) {
            // Get fresh data from device
            deviceIcon = device.getIconResource();
            deviceName = device.getName();
            adapterId = device.getFacility().getAdapterId();
            deviceId = device.getId();
            lastUpdate = timeNow;
            deviceLastUpdateTime = device.getFacility().getLastUpdate().getMillis();
            deviceRefresh = device.getFacility().getRefresh().getInterval();

            // Check if we can format device's value (unitsHelper is null when user is not logged in)
            if (mUnitsHelper != null) {
                deviceValue = mUnitsHelper.getStringValue(device.getValue());
                deviceUnit = mUnitsHelper.getStringUnit(device.getValue());
            }

            // Check if we can format device's last update (timeHelper is null when user is not logged in)
            if (mTimeHelper != null) {
                // NOTE: This should use always absolute time, because widgets aren't updated so often
                deviceLastUpdateText = mTimeHelper.formatLastUpdate(device.getFacility().getLastUpdate(), adapter);
            }

            // Save fresh data
            saveData(mContext);

            Log.v(TAG, String.format("Updating widget (%d) with fresh data", getWidgetId()));
        }
        else {
            // TODO bug -> pridava se (cached) porad
            // NOTE: just temporary solution until it will be showed better on widget
            deviceLastUpdateText = String.format("%s %s", deviceLastUpdateText, mContext.getString(R.string.widget_cached));

            Log.v(TAG, String.format("Updating widget (%d) with cached data", getWidgetId()));
        }
    }

    @Override
    public void setLayoutValues() {
        Log.d(TAG, String.format("setLayoutValues(%d)", mWidgetId));

        mRemoteViews.setImageViewResource(R.id.icon, deviceIcon == 0 ? R.drawable.dev_unknown : deviceIcon);
        mRemoteViews.setTextViewText(R.id.name, deviceName);

        // TODO temporary solution
        if(deviceValue.equals(mContext.getString(R.string.dev_enum_value_on))) {
            setSwitchChecked(true, mValueRemoteViews);
        }
        else if(deviceValue.equals(mContext.getString(R.string.dev_enum_value_off))) {
            setSwitchChecked(false, mValueRemoteViews);
        }
        else {
            mValueRemoteViews.setTextViewText(R.id.value, deviceValue);
            mValueRemoteViews.setTextViewText(R.id.unit, deviceUnit);
        }


        switch(layout){
            case R.layout.widget_sensor_3x1:
            case R.layout.widget_sensor_2x1:
                // For classic (= not-small) layout of widget, set also lastUpdate
                mRemoteViews.setTextViewText(R.id.last_update, deviceLastUpdateText);

                Compatibility.setTextViewTextSize(mContext, mValueRemoteViews, R.id.value, TypedValue.COMPLEX_UNIT_SP, 16);
                Compatibility.setTextViewTextSize(mContext, mValueRemoteViews, R.id.unit, TypedValue.COMPLEX_UNIT_SP, 16);

                break;

            default:
                break;
        }

        //*/
        updateLayout();
    }

    @Override
    public void asyncTask(Object obj) {
        Log.d(TAG, "asyncTask()");

        Device dev = (Device) obj;

        // TODO temporary
        if(deviceValue.equals(mContext.getString(R.string.dev_enum_value_on))){
            setSwitchChecked(false, mRemoteViews);
        }
        // is set to "off"
        else if(deviceValue.equals(mContext.getString(R.string.dev_enum_value_off))) {
            setSwitchChecked(true, mRemoteViews);
        }

        Log.d(TAG, mUnitsHelper.getStringValueUnit(dev.getValue()));

        deviceValue = mUnitsHelper.getStringValueUnit(dev.getValue());
        saveData(mContext);

        // request widget redraw
        mWidgetManager.updateAppWidget(getWidgetId(), mRemoteViews);
    }

    @Override
    public void loadData(Context context){
        super.loadData(context);

        deviceId = mPrefs.getString(PREF_DEVICE_ID, "");
        deviceName = mPrefs.getString(PREF_DEVICE_NAME, context.getString(R.string.placeholder_not_exists));
        deviceIcon = mPrefs.getInt(PREF_DEVICE_ICON, 0);
        deviceValue = mPrefs.getString(PREF_DEVICE_VALUE, "");
        deviceUnit = mPrefs.getString(PREF_DEVICE_UNIT, "");
        deviceLastUpdateText = mPrefs.getString(PREF_DEVICE_LAST_UPDATE_TEXT, "");
        deviceLastUpdateTime = mPrefs.getLong(PREF_DEVICE_LAST_UPDATE_TIME, 0);
        deviceRefresh = mPrefs.getInt(PREF_DEVICE_REFRESH, 0);
    }

    @Override
    public void saveData(Context context){
        super.saveData(context);

        getSettings(context).edit()
            .putString(PREF_DEVICE_ID, deviceId)
            .putString(PREF_DEVICE_NAME, deviceName)
            .putInt(PREF_DEVICE_ICON, deviceIcon)
            .putString(PREF_DEVICE_VALUE, deviceValue)
            .putString(PREF_DEVICE_UNIT, deviceUnit)
            .putString(PREF_DEVICE_LAST_UPDATE_TEXT, deviceLastUpdateText)
            .putLong(PREF_DEVICE_LAST_UPDATE_TIME, deviceLastUpdateTime)
            .putInt(PREF_DEVICE_REFRESH, deviceRefresh)
            .commit();
    }
}
