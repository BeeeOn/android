package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;
import android.util.TypedValue;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceType;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.BooleanValue;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.widget.ViewsBuilder;
import com.rehivetech.beeeon.widget.service.WidgetService;

/**
 * Created by Tomáš on 26. 4. 2015.
 */
public class WidgetDevicePersistence extends WidgetBeeeOnPersistence {
	private static final String PREF_ICON = "icon";
	private static final String PREF_TYPE = "type";
	private static final String PREF_RAW_VALUE = "raw_value";
	private static final String PREF_CACHED_VALUE = "cached_value";
	private static final String PREF_CACHED_UNIT = "cached_unit";
	private static final String PREF_LAST_UPDATE_TEXT = "last_update_text";
	private static final String PREF_LAST_UPDATE_TIME = "last_update_time";
	private static final String PREF_REFRESH = "refresh";

	// persistence data
	public int type;
	public int icon;
	public long lastUpdateTime;
	public String lastUpdateText;
	public int refresh;
	private String rawValue;
	private String cachedValue;
	private String cachedUnit;

	// generated data
	private DeviceType deviceType;
	private BaseValue deviceValue;
	private boolean deviceValueDisabled = false;
	private boolean deviceValueChecked;

	public WidgetDevicePersistence(Context context, int widgetId, int offset, int boundView, UnitsHelper unitsHelper, TimeHelper timeHelper, WidgetSettings settings) {
		super(context, widgetId, offset, boundView, unitsHelper, timeHelper, settings);
	}

	@Override
	public void load() {
		super.load();

		icon = mPrefs.getInt(getProperty(PREF_ICON), 0);
		type = mPrefs.getInt(getProperty(PREF_TYPE), DeviceType.TYPE_UNKNOWN.getTypeId());

		rawValue = mPrefs.getString(getProperty(PREF_RAW_VALUE), "");
		cachedValue = mPrefs.getString(getProperty(PREF_CACHED_VALUE), "");
		cachedUnit = mPrefs.getString(getProperty(PREF_CACHED_UNIT), "");

		lastUpdateText = mPrefs.getString(getProperty(PREF_LAST_UPDATE_TEXT), "");
		lastUpdateTime = mPrefs.getLong(getProperty(PREF_LAST_UPDATE_TIME), 0);
		refresh = mPrefs.getInt(getProperty(PREF_REFRESH), 0);

		deviceType = DeviceType.fromTypeId(type);
		deviceValue = BaseValue.createFromDeviceType(deviceType);

		// we don't set value when creating new widget
		if(!rawValue.isEmpty()) {
			deviceValue.setValue(rawValue);
		}
	}

	@Override
	public void configure(Object obj1, Object obj2) {
		super.configure(obj1, obj2);

		Device device = (Device) obj1;
		Adapter adapter = (Adapter) obj2;
		if (device == null || adapter == null) return;

		id = device.getId();
		name = device.getName();
		icon = device.getIconResource();
		adapterId = adapter.getId();
		adapterRole = adapter.getRole().getValue();
		type = device.getType().getTypeId();

		lastUpdateTime = device.getFacility().getLastUpdate().getMillis();
		refresh = device.getFacility().getRefresh().getInterval();

		deviceType = device.getType();
		// value is saving as raw (for recreating) and cached (for when user is logged out)
		rawValue = device.getValue().getRawValue();
		deviceValue.setValue(device.getValue().getRawValue());

		// when user is logged in, save last known value as cached value
		if(mUnitsHelper != null){
			cachedValue = mUnitsHelper.getStringValue(device.getValue());
			cachedUnit = mUnitsHelper.getStringUnit(device.getValue());
		}

		// Check if we can format device's last update (timeHelper is null when user is not logged in)
		if (mTimeHelper != null) {
			// NOTE: This should use always absolute time, because widgets aren't updated so often
			lastUpdateText = mTimeHelper.formatLastUpdate(device.getFacility().getLastUpdate(), adapter);
		}
	}

	@Override
	public void save() {
		super.save();

		mPrefs.edit()
				.putInt(getProperty(PREF_ICON), icon)
				.putInt(getProperty(PREF_TYPE), type)

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
				.remove(getProperty(PREF_TYPE))

				.remove(getProperty(PREF_RAW_VALUE))
				.remove(getProperty(PREF_CACHED_VALUE))
				.remove(getProperty(PREF_CACHED_UNIT))

				.remove(getProperty(PREF_LAST_UPDATE_TEXT))
				.remove(getProperty(PREF_LAST_UPDATE_TIME))
				.remove(getProperty(PREF_REFRESH))
				.apply();
	}

	@Override
	public void initView() {
		if(mBoundView == 0) return;

		User.Role role = User.Role.fromString(adapterRole);
		Controller mController = Controller.getInstance(mContext);

		if(getType().isActor() && mController.isUserAllowed(role) && deviceValue instanceof BooleanValue){
			mBuilder.loadRootView(R.layout.widget_include_switchcompat);
			mBuilder.setOnClickListener(R.id.widget_switchcompat, WidgetService.getPendingIntentActorChangeRequest(mContext, mWidgetId, getId(), adapterId));
			deviceValueChecked = ((BooleanValue) deviceValue).isActiveValue(BooleanValue.TRUE);
		}
		else {
			mBuilder.loadRootView(R.layout.widget_include_value_unit);
		}
	}

	@Override
	public void renderView(ViewsBuilder parentBuilder, boolean isCached, String cachedFormat) {
		super.renderView(parentBuilder, isCached, cachedFormat);
		if(mBoundView == 0) return;	// TODO mozna pres super() ?

		User.Role role = User.Role.fromString(adapterRole);
		Controller mController = Controller.getInstance(mContext);

		if(getType().isActor() && mController.isUserAllowed(role) && deviceValue instanceof BooleanValue){
			if(mIsCached){
				setSwitchDisabled(true, false);
			}
			else {
				if (deviceValueDisabled) {
					setSwitchDisabled(true);
				} else {
					setSwitchDisabled(false);
					boolean isOn = ((BooleanValue) deviceValue).isActiveValue(BooleanValue.TRUE);
					setSwitchChecked(isOn);
				}
			}
		}
		else {
			if(mIsCached) {
				mBuilder.setTextViewText(R.id.value, getValue());
				mBuilder.setTextViewText(R.id.unit, String.format(cachedFormat, getUnit()));
			}
			else {
				mBuilder.setTextViewText(R.id.value, getValue());
				mBuilder.setTextViewText(R.id.unit, getUnit());
			}

			if(mWidgetSettings.isColorScheme){
				mBuilder.setTextViewColor(R.id.value, mWidgetSettings.colorSecondary);
				mBuilder.setTextViewColor(R.id.unit, mWidgetSettings.colorSecondary);
			}
		}

		parentBuilder.removeAllViews(mBoundView);
		parentBuilder.addView(mBoundView, mBuilder.getRoot());
	}

	@Override
	public String getPropertyPrefix() {
		return "device";
	}

	// ----------------------------------------------------------- //
	// ---------------------- GUI CHANGERS ----------------------- //
	// ----------------------------------------------------------- //
	/**
	 * Setup size for value and unit id SP units
	 * @param sizeInSp
	 */
	public void setValueUnitSize(int sizeInSp){
		mBuilder.setTextViewTextSize(R.id.value, TypedValue.COMPLEX_UNIT_SP, sizeInSp);
		mBuilder.setTextViewTextSize(R.id.unit, TypedValue.COMPLEX_UNIT_SP, sizeInSp);
	}

	/**
	 * Sets widget switchcompat (imageview)
	 * @param state
	 */
	public void setSwitchChecked(boolean state){
		// if this cannot be switched
		if(!(deviceValue instanceof BooleanValue)) return;

		mBuilder.setSwitchChecked(state);
		deviceValueChecked = state;
	}

	/**
	 * Sets widget switchcompat (imageview) to disable or fallback state
	 * @param disabled
	 * @param prevent   is set - prevents getting changed before calling setSwitchDisabled(false)
	 */
	public void setSwitchDisabled(boolean disabled, boolean prevent) {
		// if this cannot be switched
		if(!(deviceValue instanceof BooleanValue)) return;

		if(disabled == true){
			mBuilder.setSwitchDisabled(true, deviceValueChecked);
			WidgetService.cancelPendingIntentActorChangeRequest(mContext, mWidgetId, getId(), adapterId);
		}
		else{
			mBuilder.setSwitchDisabled(false, deviceValueChecked);
			mBuilder.setOnClickListener(R.id.widget_switchcompat, WidgetService.getPendingIntentActorChangeRequest(mContext, mWidgetId, getId(), adapterId));
		}

		// prevent from getting updated the value
		if(prevent) deviceValueDisabled = disabled;
	}

	public void setSwitchDisabled(boolean disabled){
		setSwitchDisabled(disabled, true);
	}


	// ----------------------------------------------------------- //
	// ---------------------- GETTERS ---------------------------- //
	// ----------------------------------------------------------- //
	public DeviceType getType(){
		return deviceType;
	}

	/**
	 * If user logged in, gets valueUnit from UnitsHelper, otherwise cached valueUnit
	 * @return
	 */
	public String getValueUnit(){
		if(mUnitsHelper != null){
			return mUnitsHelper.getStringValueUnit(deviceValue);
		}

		return String.format("%s %s", cachedValue, cachedUnit);
	}

	/**
	 * If user is logged in, gets value from UnitsHelper, otherwise cached value
	 * @return
	 */
	public String getValue(){
		if(mUnitsHelper != null){
			return mUnitsHelper.getStringValue(deviceValue);
		}

		return cachedValue;
	}

	/**
	 * If user is logged in, gets unit from UnitsHelper, otherwise cached unit
	 * @return
	 */
	public String getUnit(){
		if(mUnitsHelper != null){
			return mUnitsHelper.getStringUnit(deviceValue);
		}

		return cachedUnit;
	}
}
