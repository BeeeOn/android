package com.rehivetech.beeeon.household.device;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.BatteryValue;
import com.rehivetech.beeeon.household.device.values.EmissionValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.household.device.values.HumidityValue;
import com.rehivetech.beeeon.household.device.values.IlluminationValue;
import com.rehivetech.beeeon.household.device.values.NoiseValue;
import com.rehivetech.beeeon.household.device.values.PressureValue;
import com.rehivetech.beeeon.household.device.values.RefreshValue;
import com.rehivetech.beeeon.household.device.values.RssiValue;
import com.rehivetech.beeeon.household.device.values.TemperatureValue;
import com.rehivetech.beeeon.household.device.values.UnknownValue;

/**
 * Module types
 */
public enum ModuleType {

	TYPE_ENUM(0x01, R.string.dev_enum_type, EnumValue.class, false),
	TYPE_TEMPERATURE(0x02, R.string.dev_temperature_type, TemperatureValue.class, false),
	TYPE_HUMIDITY(0x03, R.string.dev_temperature_type, HumidityValue.class, false),
	TYPE_PRESSURE(0x04, R.string.dev_pressure_type, PressureValue.class, false),
	TYPE_LIGHT(0x05, R.string.dev_illumination_type, IlluminationValue.class, false),
	TYPE_NOISE(0x06, R.string.dev_noise_type, NoiseValue.class, false),
	TYPE_CO2(0x07, R.string.dev_emission_type, EmissionValue.class, false),
	TYPE_BATTERY(0x08, R.string.devices__type_battery, BatteryValue.class, true),
	TYPE_RSSI(0x09, R.string.devices__type_rssi, RssiValue.class, true),
	TYPE_REFRESH(0x0A, R.string.devices__type_refresh, RefreshValue.class, true),

	TYPE_UNKNOWN(-1, R.string.dev_unknown_type, UnknownValue.class, false);

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
		if (value.isEmpty())
			return TYPE_UNKNOWN;

		return fromTypeId(Integer.parseInt(value));
	}

}
