package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.ViewsBuilder;

/**
 * @author mlyko
 */
public class WidgetLocationPersistence extends WidgetBeeeOnPersistence {

	private static final String PREF_TYPE = "type";

	public String type;

	public WidgetLocationPersistence(Context context, int widgetId, int offset, int boundView, UnitsHelper unitsHelper, TimeHelper timeHelper, WidgetSettings settings) {
		super(context, widgetId, offset, boundView, unitsHelper, timeHelper, settings);
	}

	@Override
	public void load() {
		super.load();
		type = mPrefs.getString(getProperty(PREF_TYPE), "0");        // TODO should be unknown location
	}

	@Override
	public void configure(Object obj, Object obj2) {
		super.configure(obj, obj2);

		Location location = (Location) obj;
		Gate gate = (Gate) obj2;
		if (location == null || gate == null) return;

		id = location.getId();
		name = location.getName();
		type = location.getType();
		gateId = gate.getId();
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
	public void renderView(ViewsBuilder parentBuilder) {
		super.renderView(parentBuilder);
		if (mBoundView == 0) {
			parentBuilder.setTextViewText(R.id.widget_location_name, name);
			parentBuilder.setImage(R.id.widget_location_icon, Utils.getEnumFromId(Location.LocationIcon.class, type, Location.LocationIcon.UNKNOWN).getIconResource(IconResourceType.WHITE));
		} else {
			mBuilder.loadRootView(R.layout.widget_persistence_location);
			mBuilder.setTextViewText(R.id.widget_location_name, name);
			mBuilder.setImage(R.id.widget_location_icon, Utils.getEnumFromId(Location.LocationIcon.class, type, Location.LocationIcon.UNKNOWN).getIconResource(IconResourceType.WHITE));
			parentBuilder.removeAllViews(mBoundView);
			parentBuilder.addView(mBoundView, mBuilder.getRoot());
		}
	}

	public String getType() {
		return type;
	}

}
