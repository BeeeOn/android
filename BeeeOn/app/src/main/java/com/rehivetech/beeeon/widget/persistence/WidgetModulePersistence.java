package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceType;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleType;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.BooleanValue;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.ViewsBuilder;
import com.rehivetech.beeeon.widget.service.WidgetService;

/**
 * Created by Tomáš on 26. 4. 2015.
 */
public class WidgetModulePersistence extends WidgetBeeeOnPersistence {
	private static final String PREF_ICON = "icon";
	private static final String PREF_GATE_ID = "gate_id";
	private static final String PREF_DEVICE_TYPE = "device_type";
	private static final String PREF_MODULE_ABSOLUTE_ID = "module_absolute_id";
	private static final String PREF_RAW_VALUE = "raw_value";
	private static final String PREF_CACHED_VALUE = "cached_value";
	private static final String PREF_CACHED_UNIT = "cached_unit";
	private static final String PREF_LAST_UPDATE_TEXT = "last_update_text";
	private static final String PREF_LAST_UPDATE_TIME = "last_update_time";
	private static final String PREF_REFRESH = "refresh";

	public static final String PREF_LOCATION_ICON = "location_icon";
	public static final String PREF_LOCATION_ID = "location_id";

	public static final int VALUE_UNIT = 1;
	public static final int SWITCHCOMPAT = 2;

	// persistence data
	public int icon;
	public long lastUpdateTime;
	public String lastUpdateText;
	public int refresh;
	// location data (not all the time available)
	public String locationId;
	public int locationIcon;

	private String rawValue;
	private String cachedValue;
	private String cachedUnit;

	// generated data
	private boolean moduleValueDisabled = false;
	private boolean moduleValueChecked;

	public int containerType;

	@Nullable
	private Module mModule;

	public WidgetModulePersistence(Context context, int widgetId, int offset, int boundView, UnitsHelper unitsHelper, TimeHelper timeHelper, WidgetSettings settings) {
		super(context, widgetId, offset, boundView, unitsHelper, timeHelper, settings);
	}

	@Override
	public void load() {
		super.load();

		icon = mPrefs.getInt(getProperty(PREF_ICON), 0);

		locationId = mPrefs.getString(getProperty(PREF_LOCATION_ID), "");
		locationIcon = mPrefs.getInt(getProperty(PREF_LOCATION_ICON), 0);

		rawValue = mPrefs.getString(getProperty(PREF_RAW_VALUE), "");
		cachedValue = mPrefs.getString(getProperty(PREF_CACHED_VALUE), "");
		cachedUnit = mPrefs.getString(getProperty(PREF_CACHED_UNIT), "");

		lastUpdateText = mPrefs.getString(getProperty(PREF_LAST_UPDATE_TEXT), "");
		lastUpdateTime = mPrefs.getLong(getProperty(PREF_LAST_UPDATE_TIME), 0);
		refresh = mPrefs.getInt(getProperty(PREF_REFRESH), 0);

		gateId = mPrefs.getString(getProperty(PREF_GATE_ID), "");
		String typeId = mPrefs.getString(getProperty(PREF_DEVICE_TYPE), "");
		String moduleAbsoluteId = mPrefs.getString(getProperty(PREF_MODULE_ABSOLUTE_ID), "");
		if (!typeId.isEmpty() && !moduleAbsoluteId.isEmpty()) {
			Module.ModuleId moduleId = new Module.ModuleId(gateId, moduleAbsoluteId);

			Device device = Device.createDeviceByType(typeId, moduleId.gateId, moduleId.deviceId);
			mModule = device.getModuleById(moduleId.moduleId);

			// we don't set value when creating new widget
			if (!rawValue.isEmpty()) {
				mModule.setValue(rawValue);
			}
		} else {
			mModule = null;
		}
	}

	@Override
	public void configure(Object obj1, Object obj2) {
		super.configure(obj1, obj2);

		if (!(obj1 instanceof Module) || !(obj2 instanceof Gate)) return;
		Module module = (Module) obj1;
		Gate gate = (Gate) obj2;

		id = module.getModuleId().absoluteId;
		name = module.getName(mContext);
		icon = module.getIconResource(IconResourceType.WHITE);
		gateId = gate.getId();
		mGateRole = gate.getRole().getId();
		mModule = module;

		Device device = module.getDevice();

		mUserRole = Utils.getEnumFromId(User.Role.class, mGateRole, User.Role.Guest);
		lastUpdateTime = device.getLastUpdate() == null ? 0 : device.getLastUpdate().getMillis();

		RefreshInterval deviceRefresh = device.getRefresh();
		if (deviceRefresh != null) {
			refresh = deviceRefresh.getInterval();
		}

		// value is saving as raw (for recreating) and cached (for when user is logged out)
		rawValue = module.getValue().getRawValue();

		// when user is logged in, save last known value as cached value
		if (mUnitsHelper != null) {
			cachedValue = mUnitsHelper.getStringValue(module.getValue());
			cachedUnit = mUnitsHelper.getStringUnit(module.getValue());
		}

		// Check if we can format module's last update (timeHelper is null when user is not logged in)
		if (mTimeHelper != null) {
			// NOTE: This should use always absolute time, because widgets aren't updated so often
			lastUpdateText = mTimeHelper.formatLastUpdate(device.getLastUpdate(), gate);
		}
	}

	@Override
	public void configure(Object obj1, Object obj2, Object obj3) {
		configure(obj1, obj2);

		if (!(obj3 instanceof Location)) return;
		Location location = (Location) obj3;

		locationId = location.getId();
		locationIcon = location.getIconResource(IconResourceType.WHITE);
	}

	@Override
	public void save() {
		super.save();

		if (mModule == null)
			return;

		mPrefs.edit()
				.putInt(getProperty(PREF_ICON), icon)

				.putString(getProperty(PREF_DEVICE_TYPE), mModule.getDevice().getType().getId())
				.putString(getProperty(PREF_MODULE_ABSOLUTE_ID), mModule.getModuleId().absoluteId)

				.putString(getProperty(PREF_LOCATION_ID), locationId)
				.putInt(getProperty(PREF_LOCATION_ICON), locationIcon)

				.putString(getProperty(PREF_RAW_VALUE), rawValue)
				.putString(getProperty(PREF_CACHED_VALUE), cachedValue)
				.putString(getProperty(PREF_CACHED_UNIT), cachedUnit)

				.putString(getProperty(PREF_LAST_UPDATE_TEXT), lastUpdateText)
				.putLong(getProperty(PREF_LAST_UPDATE_TIME), lastUpdateTime)
				.putInt(getProperty(PREF_REFRESH), refresh)
				.apply();
	}

	@Override
	public void delete() {
		super.delete();

		mPrefs.edit()
				.remove(getProperty(PREF_ICON))

				.remove(getProperty(PREF_DEVICE_TYPE))
				.remove(getProperty(PREF_MODULE_ABSOLUTE_ID))

				.remove(getProperty(PREF_LOCATION_ID))
				.remove(getProperty(PREF_LOCATION_ICON))

				.remove(getProperty(PREF_RAW_VALUE))
				.remove(getProperty(PREF_CACHED_VALUE))
				.remove(getProperty(PREF_CACHED_UNIT))

				.remove(getProperty(PREF_LAST_UPDATE_TEXT))
				.remove(getProperty(PREF_LAST_UPDATE_TIME))
				.remove(getProperty(PREF_REFRESH))
				.apply();
	}

	@Override
	public void renderView(ViewsBuilder parentBuilder, boolean isCached, String cachedString) {
		super.renderView(parentBuilder, isCached, cachedString);
		if (mBoundView == 0) return;

		if (mModule == null) return;

		Controller controller = Controller.getInstance(mContext);

		if (mModule.isActuator() && controller.isUserAllowed(mUserRole) && mModule.getValue() instanceof BooleanValue) {
			containerType = SWITCHCOMPAT;

			BooleanValue moduleValue = (BooleanValue) mModule.getValue();

			mBuilder.loadRootView(R.layout.widget_persistence_module_switchcompat);
			mBuilder.setOnClickListener(R.id.widget_switchcompat, WidgetService.getPendingIntentActorChangeRequest(mContext, mWidgetId, getId(), gateId));
			moduleValueChecked = moduleValue.isActiveValue(BooleanValue.TRUE);

			if (mIsCached) {
				setSwitchDisabled(true, false);
			} else {
				if (moduleValueDisabled) {
					setSwitchDisabled(true);
				} else {
					setSwitchDisabled(false);
					boolean isOn = moduleValue.isActiveValue(BooleanValue.TRUE);
					setSwitchChecked(isOn);
				}
			}
		} else {
			containerType = VALUE_UNIT;
			mBuilder.loadRootView(R.layout.widget_persistence_module_value_unit);

			// if location set, show the icon
			if (locationIcon > 0) mBuilder.setImage(R.id.widget_module_icon, locationIcon);

			if (mIsCached) {
				mBuilder.setTextViewText(R.id.widget_module_value, getValue());
				mBuilder.setTextViewText(R.id.widget_module_unit, getUnit() + cachedString);
			} else {
				mBuilder.setTextViewText(R.id.widget_module_value, getValue());
				mBuilder.setTextViewText(R.id.widget_module_unit, getUnit());
			}
		}

		parentBuilder.removeAllViews(mBoundView);
		parentBuilder.addView(mBoundView, mBuilder.getRoot());
	}

	@Override
	public String getPropertyPrefix() {
		return "module";
	}

	// ----------------------------------------------------------- //
	// ---------------------- GUI CHANGERS ----------------------- //
	// ----------------------------------------------------------- //

	/**
	 * Setup size for value and unit id SP units
	 *
	 * @param dimensionResource
	 */
	public void setValueUnitSize(int dimensionResource) {
		if (containerType != VALUE_UNIT) return;

		mBuilder.setTextViewTextSize(R.id.widget_module_value, dimensionResource);
		mBuilder.setTextViewTextSize(R.id.widget_module_unit, dimensionResource);
	}

	public void setValueUnitColor(int colorResource) {
		if (containerType != VALUE_UNIT) return;

		mBuilder.setTextViewColor(R.id.widget_module_value, colorResource);
		mBuilder.setTextViewColor(R.id.widget_module_unit, colorResource);
	}

	/**
	 * Sets widget switchcompat (imageview)
	 *
	 * @param state
	 */
	public void setSwitchChecked(boolean state) {
		if (mModule == null)
			return;

		// if this cannot be switched
		if (containerType != SWITCHCOMPAT || !(mModule.getValue() instanceof BooleanValue)) return;

		mBuilder.setSwitchChecked(state);
		moduleValueChecked = state;
	}

	/**
	 * Sets widget switchcompat (imageview) to disable or fallback state
	 *
	 * @param disabled
	 * @param prevent  is set - prevents getting changed before calling setSwitchDisabled(false)
	 */
	public void setSwitchDisabled(boolean disabled, boolean prevent) {
		if (mModule == null)
			return;

		// if this cannot be switched
		if (containerType != SWITCHCOMPAT || !(mModule.getValue() instanceof BooleanValue)) return;

		if (disabled) {
			mBuilder.setSwitchDisabled(true, moduleValueChecked);
			WidgetService.cancelPendingIntentActorChangeRequest(mContext, mWidgetId, getId(), gateId);
		} else {
			mBuilder.setSwitchDisabled(false, moduleValueChecked);
			mBuilder.setOnClickListener(R.id.widget_switchcompat, WidgetService.getPendingIntentActorChangeRequest(mContext, mWidgetId, getId(), gateId));
		}

		// prevent from getting updated the value
		if (prevent) moduleValueDisabled = disabled;
	}

	public void setSwitchDisabled(boolean disabled) {
		setSwitchDisabled(disabled, true);
	}


	// ----------------------------------------------------------- //
	// ---------------------- GETTERS ---------------------------- //
	// ----------------------------------------------------------- //

	@Nullable
	public Module getModule() {
		return mModule;
	}

	/**
	 * If user logged in, gets valueUnit from UnitsHelper, otherwise cached valueUnit
	 *
	 * @return
	 */
	public String getValueUnit() {
		if (mModule != null && mUnitsHelper != null) {
			return mUnitsHelper.getStringValueUnit(mModule.getValue());
		}

		return String.format("%s %s", cachedValue, cachedUnit);
	}

	/**
	 * If user is logged in, gets value from UnitsHelper, otherwise cached value
	 *
	 * @return
	 */
	public String getValue() {
		if (mModule != null && mUnitsHelper != null) {
			return mUnitsHelper.getStringValue(mModule.getValue());
		}

		return cachedValue;
	}

	/**
	 * If user is logged in, gets unit from UnitsHelper, otherwise cached unit
	 *
	 * @return
	 */
	public String getUnit() {
		if (mModule != null && mUnitsHelper != null) {
			return mUnitsHelper.getStringUnit(mModule.getValue());
		}

		return cachedUnit;
	}
}
