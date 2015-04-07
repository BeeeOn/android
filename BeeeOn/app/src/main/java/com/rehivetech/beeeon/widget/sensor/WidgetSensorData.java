package com.rehivetech.beeeon.widget.sensor;

import android.content.Context;
import android.content.SharedPreferences;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.widget.WidgetData;

/**
 * Class for sensor app widget (1x1, 2x1, 3x1)
 */
public class WidgetSensorData extends WidgetData {
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
    public String deviceValueType;
    public String deviceUnit;
    public long deviceLastUpdateTime;
    public String deviceLastUpdateText;
    public int deviceRefresh;

    public WidgetSensorData(int widgetId) {
        super(widgetId);

        widgetProvider = new WidgetSensorProvider();
        mClassName = WidgetSensorData.class.getName();
    }

    @Override
    public void loadData(Context context){
        super.loadData(context);

        SharedPreferences prefs = getSettings(context);
        deviceId = prefs.getString(PREF_DEVICE_ID, "");
        deviceName = prefs.getString(PREF_DEVICE_NAME, context.getString(R.string.placeholder_not_exists));
        deviceIcon = prefs.getInt(PREF_DEVICE_ICON, 0);
        deviceValue = prefs.getString(PREF_DEVICE_VALUE, "");
        deviceUnit = prefs.getString(PREF_DEVICE_UNIT, "");
        deviceLastUpdateText = prefs.getString(PREF_DEVICE_LAST_UPDATE_TEXT, "");
        deviceLastUpdateTime = prefs.getLong(PREF_DEVICE_LAST_UPDATE_TIME, 0);
        deviceRefresh = prefs.getInt(PREF_DEVICE_REFRESH, 0);
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
