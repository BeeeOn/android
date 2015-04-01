package com.rehivetech.beeeon.widget.location;

import android.content.Context;
import android.content.SharedPreferences;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.widget.WidgetData;

/**
 * Class for location list app widget (3x2)
 */
public class WidgetLocationData extends WidgetData {
    private static final String PREF_LOCATION_ID = "location";
    private static final String PREF_LOCATION_NAME = "device_name";
    private static final String PREF_LOCATION_ICON = "device_icon";

    // publicly accessible properties of widget
    public String locationId;
    public String locationName;
    public int locationIcon;

    public WidgetLocationData(int widgetId) {
        super(widgetId);
        widgetProvider = new WidgetLocationListProvider();
        mClassName = WidgetLocationData.class.getName();
    }

    @Override
    public void loadData(Context context) {
        super.loadData(context);

        SharedPreferences prefs = getSettings(context);
        locationId = prefs.getString(PREF_LOCATION_ID, "");
        locationName = prefs.getString(PREF_LOCATION_NAME, context.getString(R.string.placeholder_not_exists));
        locationIcon = prefs.getInt(PREF_LOCATION_ICON, 0);
    }

    @Override
    public void saveData(Context context) {
        super.saveData(context);

        getSettings(context).edit()
                .putString(PREF_LOCATION_ID, locationId)
                .putString(PREF_LOCATION_NAME, locationName)
                .putInt(PREF_LOCATION_ICON, locationIcon)
                .commit();
    }
}
