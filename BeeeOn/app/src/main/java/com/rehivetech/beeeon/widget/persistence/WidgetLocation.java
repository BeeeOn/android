package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;

import java.util.List;

/**
 * @author mlyko
 */
public class WidgetLocation {
	public static final String PREF_FILENAME = "widget_%d_loc_%d";

	private static final String PREF_ID = "id";
	private static final String PREF_NAME = "name";
	private static final String PREF_TYPE = "type";
	private static final String PREF_ADAPTER_ID = "adapter_id";

	public String id;
	public String name;
	public int type;
	public String adapterId;

	public final int deviceOffset;
	public int boundView;

	private Context mContext;
	private int mWidgetId;

	public WidgetLocation(Context context, int widgetId, int offset, int view) {
		mContext = context.getApplicationContext();
		mWidgetId = widgetId;
		deviceOffset = offset;
		boundView = view;
	}

	public SharedPreferences getSettings() {
		return mContext.getSharedPreferences(String.format(PREF_FILENAME, mWidgetId, deviceOffset), Context.MODE_PRIVATE);
	}

	public void load() {
		SharedPreferences prefs = getSettings();

		id = prefs.getString(PREF_ID, "");
		name = prefs.getString(PREF_NAME, mContext.getString(R.string.placeholder_not_exists));
		type = prefs.getInt(PREF_TYPE, 0);
		adapterId = prefs.getString(PREF_ADAPTER_ID, "");
	}

	public void save() {
		getSettings().edit()
				.putString(PREF_ID, id)
				.putString(PREF_NAME, name)
				.putInt(PREF_TYPE, type)
				.putString(PREF_ADAPTER_ID, adapterId)
				.apply();
	}

	public void change(Location location, Adapter adapter, UnitsHelper unitsHelper, TimeHelper timeHelper) {
		if (location != null) {
			id = location.getId();
			name = location.getName();
			type = location.getType();
			adapterId = adapter.getId();
		}
	}

	public void delete(){
		getSettings().edit().clear().commit();
	}

	// ------ METHODS FOR WORKING WITH MORE OBJECTS AT ONCE ------ //

	public static void loadAll(List<WidgetLocation> widgetObjs){
		if(widgetObjs == null) return;
		for(WidgetLocation dev : widgetObjs){
			dev.load();
		}
	}

	public static void saveAll(List<WidgetLocation> widgetObjs){
		if(widgetObjs == null) return;
		for(WidgetLocation dev : widgetObjs){
			dev.save();
		}
	}

	public static void deleteAll(List<WidgetLocation> widgetObjs){
		if(widgetObjs == null) return;
		for(WidgetLocation dev : widgetObjs){
			dev.delete();
		}
	}
}
