package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceType;
import com.rehivetech.beeeon.household.device.values.BaseEnumValue;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.OnOffValue;
import com.rehivetech.beeeon.household.device.values.OpenClosedValue;
import com.rehivetech.beeeon.util.Compatibility;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.widget.service.WidgetService;

import java.util.List;

/**
 * @author mlyko
 */
public class WidgetDevice {
	private static final String TAG = WidgetDevice.class.getSimpleName();

	private static final String PREF_FILENAME = "widget_%d_dev_%d";
	private static final String PREF_ID = "id";
	private static final String PREF_NAME = "name";
	private static final String PREF_ICON = "icon";
	private static final String PREF_TYPE = "type";

	private static final String PREF_RAW_VALUE = "raw_value";
	private static final String PREF_CACHED_VALUE = "cached_value";
	private static final String PREF_CACHED_UNIT = "cached_unit";
	private static final String PREF_LAST_UPDATE_TEXT = "last_update_text";
	private static final String PREF_LAST_UPDATE_TIME = "last_update_time";
	private static final String PREF_REFRESH = "refresh";
	private static final String PREF_ADAPTER_ID = "adapter_id";

	public final int offset;
	public String id;
	public String name;
	public int typeId;
	public int icon;
	public long lastUpdateTime;
	public String lastUpdateText;
	public int refresh;
	public String adapterId;
	public int boundView;
	// value
	private String rawValue;
	private String cachedValue;
	private String cachedUnit;
	private int mWidgetId;

	private Context mContext;
	private DeviceType deviceType;
	private BaseValue deviceValue;
	public boolean deviceValueDisabled = false;
	private RemoteViews mParentRemoteViews;
	private RemoteViews mValueRemoteViews;

	private UnitsHelper mUnitsHelper;
	private TimeHelper mTimeHelper;

	public WidgetDevice(Context context, int widgetId, int offset, int view, UnitsHelper unitsHelper, TimeHelper timeHelper) {
		mContext = context.getApplicationContext();
		mWidgetId = widgetId;
		this.offset = offset;
		boundView = view;
		mUnitsHelper = unitsHelper;
		mTimeHelper = timeHelper;
	}

	// ------ METHODS FOR WORKING WITH MORE OBJECTS AT ONCE ------ //
	public static void loadAll(List<WidgetDevice> widgetDevices){
		if(widgetDevices == null) return;
		for(WidgetDevice dev : widgetDevices){
			dev.load();
		}
	}

	public static void saveAll(List<WidgetDevice> widgetDevices){
		if(widgetDevices == null) return;
		for(WidgetDevice dev : widgetDevices){
			dev.save();
		}
	}

	public static void deleteAll(List<WidgetDevice> widgetDevices){
		if(widgetDevices == null) return;
		for(WidgetDevice dev : widgetDevices){
			dev.delete();
		}
	}

	/**
	 * Called when WidgetData loads
	 */
	public void load() {
		SharedPreferences prefs = getSettings();

		id = prefs.getString(PREF_ID, "");
		name = prefs.getString(PREF_NAME, mContext.getString(R.string.placeholder_not_exists));
		icon = prefs.getInt(PREF_ICON, 0);
		typeId = prefs.getInt(PREF_TYPE, DeviceType.TYPE_UNKNOWN.getTypeId());

		rawValue = prefs.getString(PREF_RAW_VALUE, "");
		cachedValue = prefs.getString(PREF_CACHED_VALUE, "");
		cachedUnit = prefs.getString(PREF_CACHED_UNIT, "");

		lastUpdateText = prefs.getString(PREF_LAST_UPDATE_TEXT, "");
		lastUpdateTime = prefs.getLong(PREF_LAST_UPDATE_TIME, 0);
		refresh = prefs.getInt(PREF_REFRESH, 0);
		adapterId = prefs.getString(PREF_ADAPTER_ID, "");

		deviceType = DeviceType.fromTypeId(typeId);
		deviceValue = BaseValue.createFromDeviceType(deviceType);

		// we don't set value when creating new widget
		if(!rawValue.isEmpty()) {
			deviceValue.setValue(rawValue);
		}
	}

	/**
	 * Called when configuration activity ends
	 * @param device
	 * @param adapter
	 */
	public void configure(Device device, Adapter adapter) {
		id = device.getId();
		name = device.getName();
		icon = device.getIconResource();
		adapterId = adapter.getId();
		typeId = device.getType().getTypeId();
	}

	/**
	 * Called when data are changed in WidgetData
	 * @param device
	 * @param adapter
	 */
	public void change(Device device, Adapter adapter) {
		if (device != null) {
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
	}

	/**
	 * After changing data (when WidgetData .save() )
	 */
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

	/**
	 * Deletes data from SharedPreferences
	 */
	public void delete(){
		getSettings().edit().clear().commit();
	}

	public DeviceType getType(){
		return deviceType;
	}

	/**
	 * Initializes which type will be (actor / sensor)
	 * @param parentRV
	 */
	public void initValueView(RemoteViews parentRV){
		Log.d(TAG, "initValueView()");
		mParentRemoteViews = parentRV;

		if(getType().isActor() && (deviceValue instanceof OnOffValue || deviceValue instanceof OpenClosedValue)){
			mValueRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_switchcompat);
			mValueRemoteViews.setOnClickPendingIntent(R.id.widget_switchcompat, WidgetService.getPendingIntentActorChangeRequest(mContext, mWidgetId, getId(), adapterId));
		}
		else {
			mValueRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_value_unit);
		}

		mParentRemoteViews.removeAllViews(boundView);
		mParentRemoteViews.addView(boundView, mValueRemoteViews);
	}

	/**
	 * Updates value layout with gotten data (either cached or from UnitsHelper)
	 * @param cachedFormat	Format for specifying what looks like when cached data -> available only %s
	 */
	public void updateValueView(String cachedFormat){
		Log.d(TAG, "updateValueView()");
		boolean isCached = !cachedFormat.isEmpty();

		if(deviceValue instanceof OnOffValue || deviceValue instanceof OpenClosedValue){
			// TODO when cached -> disable?
			if(deviceValueDisabled) {
				setSwitchDisabled(true);
			}
			else {
				boolean isOn = ((OnOffValue) deviceValue).isActiveValue(OnOffValue.ON);
				setSwitchChecked(isOn);
			}
		}
		else {
			if(isCached) {
				getValueViews().setTextViewText(R.id.value, getValue());
				getValueViews().setTextViewText(R.id.unit, String.format(cachedFormat, getUnit()));
			}
			else {
				getValueViews().setTextViewText(R.id.value, getValue());
				getValueViews().setTextViewText(R.id.unit, getUnit());
			}
		}
	}

	public void updateValueView(){
		updateValueView("");
	}

	/**
	 * Sets widget switchcompat (imageview)
	 * @param state
	 */
	public void setSwitchChecked(boolean state){
		// if this cannot be switched
		if(!(deviceValue instanceof OnOffValue)) return;

		mValueRemoteViews.setImageViewResource(R.id.widget_switchcompat, state ? R.drawable.switch_on : R.drawable.switch_off);
	}

	/**
	 * Sets widget switchcompat (imageview) to disable or fallback state
	 * @param disabled
	 */
	public void setSwitchDisabled(boolean disabled){
		// if this cannot be switched
		if(!(deviceValue instanceof OnOffValue)) return;

		Log.d(TAG, "setting switch disabled = " + String.valueOf(disabled));

		boolean isOn = ((OnOffValue) deviceValue).isActiveValue(OnOffValue.ON);

		if(disabled == true){
			mValueRemoteViews.setImageViewResource(R.id.widget_switchcompat, isOn == true ? R.drawable.switch_on_disabled : R.drawable.switch_off_disabled);
			WidgetService.cancelPendingIntentActorChangeRequest(mContext, mWidgetId, getId(), adapterId);
		}
		else{
			setSwitchChecked(isOn);
			mValueRemoteViews.setOnClickPendingIntent(R.id.widget_switchcompat, WidgetService.getPendingIntentActorChangeRequest(mContext, mWidgetId, getId(), adapterId));
		}

		// prevent from getting updated the value
		deviceValueDisabled = disabled;
	}

	public RemoteViews getValueViews(){
		return mValueRemoteViews;
	}

	public String getId() {
		return id;
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

	public SharedPreferences getSettings() {
		return mContext.getSharedPreferences(String.format(PREF_FILENAME, mWidgetId, offset), Context.MODE_PRIVATE);
	}

	private String doLocalActorAction(){
		if(!getType().isActor()) return "";

		if(deviceValue instanceof BaseEnumValue){
			((BaseEnumValue)deviceValue).setNextValue();
		} else {
			Log.e(TAG, "We can't switch actor, which value isn't inherited from BaseEnumValue, yet");
			return "";
		}

		return deviceValue.getRawValue();
	}

	/**
	 * Setup size for value and unit id SP units
	 * @param sizeInSp
	 */
	public void setValueUnitSize(int sizeInSp){
		Compatibility.setTextViewTextSize(mContext, getValueViews(), R.id.value, TypedValue.COMPLEX_UNIT_SP, sizeInSp);
		Compatibility.setTextViewTextSize(mContext, getValueViews(), R.id.unit, TypedValue.COMPLEX_UNIT_SP, sizeInSp);
	}
}
