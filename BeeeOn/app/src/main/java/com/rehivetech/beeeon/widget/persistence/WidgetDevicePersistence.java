package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceType;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.OnOffValue;
import com.rehivetech.beeeon.household.device.values.OpenClosedValue;
import com.rehivetech.beeeon.util.Compatibility;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.widget.service.WidgetService;

/**
 * @author mlyko
 */
public class WidgetDevicePersistence extends WidgetPersistence{
	private static final String TAG = WidgetDevicePersistence.class.getSimpleName();

	private static final String PREF_ICON = "icon";
	private static final String PREF_TYPE = "type";
	private static final String PREF_RAW_VALUE = "raw_value";
	private static final String PREF_CACHED_VALUE = "cached_value";
	private static final String PREF_CACHED_UNIT = "cached_unit";
	private static final String PREF_LAST_UPDATE_TEXT = "last_update_text";
	private static final String PREF_LAST_UPDATE_TIME = "last_update_time";
	private static final String PREF_REFRESH = "refresh";
	private static final String PREF_ADAPTER_ID = "adapter_id";

	public int typeId;
	public int icon;
	public long lastUpdateTime;
	public String lastUpdateText;
	public int refresh;

	private String rawValue;
	private String cachedValue;
	private String cachedUnit;
	private DeviceType deviceType;
	private BaseValue deviceValue;
	private boolean deviceValueDisabled = false;
	private boolean deviceValueChecked;

	public WidgetDevicePersistence(Context context, int widgetId, int offset, int view, UnitsHelper unitsHelper, TimeHelper timeHelper) {
		super(context, widgetId, offset, view, unitsHelper, timeHelper);
	}

	@Override
	public String getPrefFileName() {
		return "widget_%d_dev_%d";
	}

	@Override
	public void load() {
		SharedPreferences prefs = getSettings();

		id = prefs.getString(PREF_ID, "");
		name = prefs.getString(PREF_NAME, mContext.getString(R.string.placeholder_not_exists));
		icon = prefs.getInt(PREF_ICON, 0);
		adapterId = prefs.getString(PREF_ADAPTER_ID, "");
		typeId = prefs.getInt(PREF_TYPE, DeviceType.TYPE_UNKNOWN.getTypeId());

		rawValue = prefs.getString(PREF_RAW_VALUE, "");
		cachedValue = prefs.getString(PREF_CACHED_VALUE, "");
		cachedUnit = prefs.getString(PREF_CACHED_UNIT, "");

		lastUpdateText = prefs.getString(PREF_LAST_UPDATE_TEXT, "");
		lastUpdateTime = prefs.getLong(PREF_LAST_UPDATE_TIME, 0);
		refresh = prefs.getInt(PREF_REFRESH, 0);

		deviceType = DeviceType.fromTypeId(typeId);
		deviceValue = BaseValue.createFromDeviceType(deviceType);

		// we don't set value when creating new widget
		if(!rawValue.isEmpty()) {
			deviceValue.setValue(rawValue);
		}
	}

	@Override
	public void configure(Object obj, Adapter adapter) {
		Device device = (Device) obj;
		if (device == null) return;

		id = device.getId();
		name = device.getName();
		icon = device.getIconResource();
		adapterId = adapter.getId();
		typeId = device.getType().getTypeId();
	}

	@Override
	public void save() {
		getSettings().edit()
				.putString(PREF_ID, id)
				.putString(PREF_NAME, name)
				.putInt(PREF_ICON, icon)
				.putInt(PREF_TYPE, typeId)

				.putString(PREF_RAW_VALUE, rawValue)
				.putString(PREF_CACHED_VALUE, cachedValue)
				.putString(PREF_CACHED_UNIT, cachedUnit)

				.putString(PREF_LAST_UPDATE_TEXT, lastUpdateText)
				.putLong(PREF_LAST_UPDATE_TIME, lastUpdateTime)
				.putInt(PREF_REFRESH, refresh)
				.putString(PREF_ADAPTER_ID, adapterId)
				.apply();
	}

	@Override
	public void change(Object obj, Adapter adapter) {
		Device device = (Device) obj;
		if (device == null) return;

		// Get fresh data from device
		icon = device.getIconResource();
		typeId = device.getType().getTypeId();
		name = device.getName();
		id = device.getId();
		adapterId = adapter.getId();
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
	public void initValueView(RemoteViews parentRV) {
		super.initValueView(parentRV);
		if(mBoundView == 0) return;

		if(getType().isActor() && (deviceValue instanceof OnOffValue || deviceValue instanceof OpenClosedValue)){
			mValueRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_include_switchcompat);
			mValueRemoteViews.setOnClickPendingIntent(R.id.widget_switchcompat, WidgetService.getPendingIntentActorChangeRequest(mContext, mWidgetId, getId(), adapterId));
			deviceValueChecked = ((OnOffValue) deviceValue).isActiveValue(OnOffValue.ON);
		}
		else {
			mValueRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_include_value_unit);
		}

		mParentRemoteViews.removeAllViews(mBoundView);
		mParentRemoteViews.addView(mBoundView, mValueRemoteViews);
	}

	@Override
	public void updateValueView(boolean isCached, String cachedFormat) {
		super.updateValueView(isCached, cachedFormat);
		if(mBoundView == 0) return;

		if(deviceValue instanceof OnOffValue || deviceValue instanceof OpenClosedValue){
			if(mIsCached){
				setSwitchDisabled(true, false);
			}
			else {
				if (deviceValueDisabled) {
					setSwitchDisabled(true);
				} else {
					setSwitchDisabled(false);
					boolean isOn = ((OnOffValue) deviceValue).isActiveValue(OnOffValue.ON);
					setSwitchChecked(isOn);
				}
			}
		}
		else {
			if(mIsCached) {
				getValueViews().setTextViewText(R.id.value, getValue());
				getValueViews().setTextViewText(R.id.unit, String.format(cachedFormat, getUnit()));
			}
			else {
				getValueViews().setTextViewText(R.id.value, getValue());
				getValueViews().setTextViewText(R.id.unit, getUnit());
			}
		}
	}

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

	/**
	 * Sets widget switchcompat (imageview)
	 * @param state
	 */
	public void setSwitchChecked(boolean state){
		// if this cannot be switched
		if(!(deviceValue instanceof OnOffValue)) return;

		mValueRemoteViews.setImageViewResource(R.id.widget_switchcompat, state ? R.drawable.switch_on : R.drawable.switch_off);
		deviceValueChecked = state;
	}

	/**
	 * Sets widget switchcompat (imageview) to disable or fallback state
	 * @param disabled
	 * @param prevent   is set - prevents getting changed before calling setSwitchDisabled(false)
	 */
	public void setSwitchDisabled(boolean disabled, boolean prevent){
		// if this cannot be switched
		if(!(deviceValue instanceof OnOffValue)) return;

		Log.d(TAG, "setting switch disabled = " + String.valueOf(disabled));

		if(disabled == true){
			mValueRemoteViews.setImageViewResource(R.id.widget_switchcompat, deviceValueChecked == true ? R.drawable.switch_on_disabled : R.drawable.switch_off_disabled);
			WidgetService.cancelPendingIntentActorChangeRequest(mContext, mWidgetId, getId(), adapterId);
		}
		else{
			mValueRemoteViews.setImageViewResource(R.id.widget_switchcompat, deviceValueChecked == true ? R.drawable.switch_on : R.drawable.switch_off);
			mValueRemoteViews.setOnClickPendingIntent(R.id.widget_switchcompat, WidgetService.getPendingIntentActorChangeRequest(mContext, mWidgetId, getId(), adapterId));
		}

		// prevent from getting updated the value
		if(prevent) deviceValueDisabled = disabled;
	}

	public void setSwitchDisabled(boolean disabled){
		setSwitchDisabled(disabled, true);
	}

	/**
	 * Setup size for value and unit id SP units
	 * @param sizeInSp
	 */
	public void setValueUnitSize(int sizeInSp){
		setTextSize(R.id.value, sizeInSp);
		setTextSize(R.id.unit, sizeInSp);
	}
}
