package com.rehivetech.beeeon.widget.data;

import android.content.Context;
import android.util.Log;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.widget.persistence.WidgetModulePersistence;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Class for sensor app widget (1x1, 2x1, 3x1)
 */
public class WidgetModuleData extends WidgetData {

	protected List<Object> mDevices;

	/**
	 * Constructing object holding information about widget (instantiating in config activity and then in service)
	 *
	 * @param widgetId
	 * @param context
	 * @param unitsHelper
	 * @param timeHelper
	 */
	public WidgetModuleData(int widgetId, Context context, UnitsHelper unitsHelper, TimeHelper timeHelper) {
		super(widgetId, context, unitsHelper, timeHelper);

		widgetModules = new ArrayList<>();
		widgetModules.add(new WidgetModulePersistence(mContext, mWidgetId, 0, R.id.widget_value_container, unitsHelper, timeHelper, settings));

		mDevices = new ArrayList<>();
	}

	// ----------------------------------------------------------- //
	// ---------------- MANIPULATING PERSISTENCE ----------------- //
	// ----------------------------------------------------------- //

	@Override
	public void load() {
		super.load();
		WidgetModulePersistence.loadAll(widgetModules);
	}

	@Override
	public void init() {
		mDevices.clear();
		for (WidgetModulePersistence dev : widgetModules) {
			Module module = dev.getModule();
			if (dev.getId().isEmpty() || module == null) {
				Timber.i("Could not retrieve module from widget %s ",  String.valueOf(mWidgetId));
				continue;
			}

			mDevices.add(module.getDevice());
		}
	}

	@Override
	public void save() {
		super.save();
		WidgetModulePersistence.saveAll(widgetModules);
	}

	// ----------------------------------------------------------- //
	// ------------------------ RENDERING ------------------------ //
	// ----------------------------------------------------------- //

	@Override
	protected void renderLayout() {
		// -------------------- initialize layout
		mBuilder.setOnClickListener(R.id.widget_options, mConfigurationPendingIntent);
		mBuilder.setOnClickListener(R.id.widget_last_update, mRefreshPendingIntent);
		mBuilder.setOnClickListener(R.id.widget_refresh, mRefreshPendingIntent);

		if (widgetGateId.isEmpty()) return;

		// -------------------- render layout
		// updates all inside devices
		boolean isOnlyOne = true;
		for (WidgetModulePersistence dev : widgetModules) {
			// detail activity
			mBuilder.setOnClickListener(R.id.widget_module_icon, startDetailActivityPendingIntent(mContext, mWidgetId + dev.getOffset(), widgetGateId, dev.getId()));
			mBuilder.setOnClickListener(R.id.widget_module_name, startDetailActivityPendingIntent(mContext, mWidgetId + dev.getOffset(), widgetGateId, dev.getId()));

			// when only 1 module is in the widget - we assume that we need icon and name
			if (isOnlyOne) {
				mBuilder.setImage(R.id.widget_module_icon, dev.icon == 0 ? R.drawable.ic_val_unknown : dev.icon);
				mBuilder.setTextViewText(R.id.widget_module_name, dev.getName());
				isOnlyOne = false;
			}

			// render view based on if is cached information
			dev.renderView(mBuilder, getIsCached(), "");

			switch (widgetLayout) {
				case R.layout.widget_data_module_3x1:
				case R.layout.widget_data_module_2x1:
					mBuilder.setTextViewText(R.id.widget_last_update, getIsCached() ? String.format("%s %s", mContext.getString(R.string.widget_module_data_widget_cached), dev.lastUpdateText) : dev.lastUpdateText);
					break;
				case R.layout.widget_data_module_1x1:
					dev.setValueUnitSize(R.dimen.abc_text_size_caption_material);
					break;
			}
		}
	}

	// ----------------------------------------------------------- //
	// ---------------------- FAKE HANDLERS ---------------------- //
	// ----------------------------------------------------------- //

	@Override
	public boolean handleUpdateData() {
		int updated = 0;
		Controller controller = Controller.getInstance(mContext);
		Gate gate = controller.getGatesModel().getGate(widgetGateId);
		if (gate == null) return false;

		for (WidgetModulePersistence dev : widgetModules) {
			Module module = controller.getDevicesModel().getModule(widgetGateId, dev.getId());
			if (module != null) {
				dev.configure(module, gate);
			}
			updated++;
		}

		if (updated > 0) {
			// update last update to "now"
			widgetLastUpdate = getTimeNow();
			widgetGateId = gate.getId();

			// Save fresh data
			this.save();
			Timber.v("Updating widget (%d) with fresh data", getWidgetId());
		} else {
			// TODO show some kind of icon
			Timber.v("Updating widget (%d) with cached data", getWidgetId());
		}

		return updated > 0;
	}

	@Override
	public void handleResize(int minWidth, int minHeight) {
		super.handleResize(minWidth, minHeight);

		int layout;
		// 1 cell
		if (minWidth < 170) {
			layout = R.layout.widget_data_module_1x1;
		}
		// 2 cells
		else if (minWidth < 200) {
			layout = R.layout.widget_data_module_2x1;
		}
		// 3 cells
		else {
			layout = R.layout.widget_data_module_3x1;
		}

		changeLayout(layout);
	}

	// ----------------------------------------------------------- //
	// ------------------------- GETTERS ------------------------- //
	// ----------------------------------------------------------- //

	@Override
	public List<Object> getObjectsToReload() {
		return mDevices;
	}

	@Override
	public String getClassName() {
		return WidgetModuleData.class.getName();
	}

}