package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;

/**
 * @author mlyko
 */
public class WidgetLocationPersistence extends WidgetPersistence{
	private static final String TAG = WidgetLocationPersistence.class.getSimpleName();

	private static final String PREF_TYPE = "type";

	public int type;

	public WidgetLocationPersistence(Context context, int widgetId, int offset, int boundView, UnitsHelper unitsHelper, TimeHelper timeHelper) {
		super(context, widgetId, offset, boundView, unitsHelper, timeHelper);
	}

	@Override
	public String getPrefFileName() {
		return "widget_%d_loc_%d";
	}

	@Override
	public void load() {
		SharedPreferences prefs = getSettings();

		id = prefs.getString(PREF_ID, "");
		name = prefs.getString(PREF_NAME, mContext.getString(R.string.placeholder_not_exists));
		type = prefs.getInt(PREF_TYPE, 0);
		adapterId = prefs.getString(PREF_ADAPTER_ID, "");
	}

	@Override
	public void configure(Object obj, Adapter adapter) {
		Location location = (Location) obj;
		if (location == null) return;

		id = location.getId();
		name = location.getName();
		type = location.getType();
		adapterId = adapter.getId();
	}

	@Override
	public void save() {
		getSettings().edit()
				.putString(PREF_ID, id)
				.putString(PREF_NAME, name)
				.putInt(PREF_TYPE, type)
				.putString(PREF_ADAPTER_ID, adapterId)
				.apply();
	}

	@Override
	public void change(Object obj, Adapter adapter) {
		Location location = (Location) obj;
		if (location == null) return;

		id = location.getId();
		name = location.getName();
		type = location.getType();
		adapterId = adapter.getId();
	}

	@Override
	public void initValueView(RemoteViews parentRV) {
		super.initValueView(parentRV);
		if(mBoundView == 0) return;

		mValueRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_include_location);
		mParentRemoteViews.removeAllViews(mBoundView);
		mParentRemoteViews.addView(mBoundView, mValueRemoteViews);
	}

	@Override
	public void updateValueView(boolean isCached, String cachedFormat) {
		super.updateValueView(isCached, cachedFormat);
		if(mBoundView == 0) return;

		getValueViews().setTextViewText(R.id.name, getName());
		getValueViews().setImageViewResource(R.id.icon, Location.LocationIcon.fromValue(type).getIconResource());
	}
}
