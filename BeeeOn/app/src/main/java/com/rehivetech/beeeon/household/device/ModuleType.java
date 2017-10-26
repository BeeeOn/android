package com.rehivetech.beeeon.household.device;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.BatteryValue;
import com.rehivetech.beeeon.household.device.values.RawPercentValue;
import com.rehivetech.beeeon.household.device.values.EmissionValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.household.device.values.HumidityValue;
import com.rehivetech.beeeon.household.device.values.IlluminationValue;
import com.rehivetech.beeeon.household.device.values.NoiseValue;
import com.rehivetech.beeeon.household.device.values.PressureValue;
import com.rehivetech.beeeon.household.device.values.RawFloatValue;
import com.rehivetech.beeeon.household.device.values.RawIntValue;
import com.rehivetech.beeeon.household.device.values.RefreshValue;
import com.rehivetech.beeeon.household.device.values.RssiValue;
import com.rehivetech.beeeon.household.device.values.TemperatureValue;
import com.rehivetech.beeeon.household.device.values.UnknownValue;
import com.rehivetech.beeeon.util.Utils;

/**
 * Module types
 */
public enum ModuleType {

	TYPE_AVAILABILITY(1, R.string.devices__type_availability, EnumValue.class, false),
	TYPE_BATTERY(2, R.string.devices__type_battery, BatteryValue.class, true),
	TYPE_BITMAP(3, R.string.devices__type_bitmap, RawIntValue.class, false),
	TYPE_BRIGHTNESS_PERCENT(4, R.string.empty, RawPercentValue.class, false),
	TYPE_CO2(5, R.string.devices__type_co2, EmissionValue.class, false),
	TYPE_ENUM(6, R.string.devices__type_enum, EnumValue.class, false),
	TYPE_FIRE(7, R.string.devices__type_fire, EnumValue.class, false),
	TYPE_HUMIDITY(8, R.string.devices__type_humidity, HumidityValue.class, false),
	TYPE_LIGHT(9, R.string.devices__type_luminance, IlluminationValue.class, false),
	TYPE_MOTION(10, R.string.devices__type_motion, EnumValue.class, false),
	TYPE_NOISE(11, R.string.devices__type_noise, NoiseValue.class, false),
	TYPE_OPEN_CLOSE(12, R.string.devices__type_open_close, EnumValue.class, false),
	TYPE_ON_OFF(13, R.string.devices__type_on_off, EnumValue.class, false),
	TYPE_PERFORMANCE(14, R.string.devices__type_performance, RawPercentValue.class, false),
	TYPE_PRESSURE(15, R.string.devices__type_pressure, PressureValue.class, false),
	TYPE_RSSI(16, R.string.devices__type_rssi, RssiValue.class, true),
	TYPE_SECURITY_ALERT(17, R.string.devices__type_security_alert, EnumValue.class, true),
	TYPE_SHAKE(18, R.string.devices__type_shake, EnumValue.class, true),
	TYPE_TEMPERATURE(19, R.string.devices__type_temperature, TemperatureValue.class, false),
	TYPE_ULTRAVIOLET(20, R.string.devices__type_ultraviolet, RawIntValue.class, false),

	// XXX: refresh is not used in this way any more but it is needed to compile well
	TYPE_REFRESH(0, R.string.devices__type_refresh, RefreshValue.class, true),

	//TYPE_BITARRAY(0x0B, R.string.devices__type_bitarray, UnknownValue.class, false), // FIXME: Implement this value type
	//TYPE_RAW_FLOAT(0x0D, R.string.devices__type_raw_float, RawFloatValue.class, false),
	//TYPE_LED(0x14, R.string.empty, RawFloatValue.class, false), //FIXME nameRes
	//TYPE_CT_PERCENT(0x15, R.string.empty, RawPercentValue.class, false),
	//TYPE_HUE_COLOR(0x16, R.string.empty, RawIntValue.class, false),
	//TYPE_SATURATION_PERCENT(0x17, R.string.empty, RawPercentValue.class, false),
	//TYPE_PIR_SENSITIVITY(0x1a, R.string.empty, RawIntValue.class, false),


	TYPE_UNKNOWN(-1, R.string.devices__type_unknown, UnknownValue.class, false);

	private final int mTypeId;
	private final int mNameRes;
	private final Class<? extends BaseValue> mValueClass;
	private final boolean mSpecial;

	ModuleType(int id, int nameRes, Class<? extends BaseValue> valueClass, boolean special) {
		mTypeId = id;
		mNameRes = nameRes;
		mValueClass = valueClass;
		mSpecial = special;
	}

	public int getTypeId() {
		return mTypeId;
	}

	public int getStringResource() {
		return mNameRes;
	}

	public Class<? extends BaseValue> getValueClass() {
		return mValueClass;
	}

	public boolean isSpecial() {
		return mSpecial;
	}

	public static ModuleType fromTypeId(int typeId) {
		// Get the ModuleType object based on type number
		for (ModuleType item : values()) {
			if (typeId == item.getTypeId()) {
				return item;
			}
		}

		return TYPE_UNKNOWN;
	}

	public static ModuleType fromValue(String value) {
		return fromTypeId(Utils.parseIntSafely(value, TYPE_UNKNOWN.getTypeId()));
	}

}
