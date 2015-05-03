package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.ViewsBuilder;

/**
 * @author mlyko
 */
public class WidgetLocationPersistence extends WidgetBeeeOnPersistence {
	private static final String TAG = WidgetLocationPersistence.class.getSimpleName();

	private static final String PREF_TYPE = "type";

	public String type;

	public WidgetLocationPersistence(Context context, int widgetId, int offset, int boundView, UnitsHelper unitsHelper, TimeHelper timeHelper, WidgetSettings settings) {
		super(context, widgetId, offset, boundView, unitsHelper, timeHelper, settings);
	}

	@Override
	public void load() {
		super.load();
		type = mPrefs.getString(getProperty(PREF_TYPE), "0");		// TODO should be unknown location
	}

	@Override
	public void configure(Object obj, Object obj2) {
		super.configure(obj, obj2);

		Location location = (Location) obj;
		Adapter adapter = (Adapter) obj2;
		if (location == null || adapter == null) return;

		id = location.getId();
		name = location.getName();
		type = location.getType();
		adapterId = adapter.getId();
	}

	@Override
	public void save() {
		super.save();
		mPrefs.edit()
				.putString(getProperty(PREF_TYPE), type)
				.apply();
	}

	@Override
	public void delete() {
		super.delete();
		mPrefs.edit()
				.remove(getProperty(PREF_TYPE))
				.apply();
	}

	@Override
	public String getPropertyPrefix() {
		return "location";
	}

	@Override
	public void initView() {
		if(mBoundView == 0) return;

		mBuilder.loadRootView(R.layout.widget_include_location);
	}

	@Override
	public void renderView(ViewsBuilder parentBuilder) {
		super.renderView(parentBuilder);
		if(mBoundView == 0){
			parentBuilder.setTextViewText(R.id.name, name);
			parentBuilder.setImage(R.id.icon, Utils.getEnumFromId(Location.LocationIcon.class, type, Location.LocationIcon.UNKNOWN).getIconResource());
		}
		else {
			mBuilder.setTextViewText(R.id.name, name);
			mBuilder.setImage(R.id.icon, Utils.getEnumFromId(Location.LocationIcon.class, type, Location.LocationIcon.UNKNOWN).getIconResource());
			parentBuilder.removeAllViews(mBoundView);
			parentBuilder.addView(mBoundView, mBuilder.getRoot());
		}
	}

	public String getType() {
		return type;
	}

}
