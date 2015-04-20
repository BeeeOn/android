package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;
import android.widget.RemoteViews;

import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;

/**
 * @author mlyko
 */
public class WidgetDevicePersistence extends WidgetPersistence{


	public WidgetDevicePersistence(Context context, int widgetId, int offset, int view, UnitsHelper unitsHelper, TimeHelper timeHelper) {
		super(context, widgetId, offset, view, unitsHelper, timeHelper);
	}

	@Override
	public String getPrefFileName() {
		return "widget_%d_dev_%d";
	}

	@Override
	public void load() {

	}

	@Override
	public void configure(Object obj, Adapter adapter) {

	}

	@Override
	public void save() {

	}

	@Override
	public void change(Object obj, Adapter adapter) {

	}

	@Override
	public void initValueView(RemoteViews parentRV) {

	}

	@Override
	public void updateValueView(String cachedFormat) {

	}
}
