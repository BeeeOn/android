package com.rehivetech.beeeon.adapter.device;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.device.values.BaseValue;
import com.rehivetech.beeeon.adapter.device.values.EmissionValue;
import com.rehivetech.beeeon.adapter.device.values.HumidityValue;
import com.rehivetech.beeeon.adapter.device.values.IlluminationValue;
import com.rehivetech.beeeon.adapter.device.values.NoiseValue;
import com.rehivetech.beeeon.adapter.device.values.OnOffValue;
import com.rehivetech.beeeon.adapter.device.values.OpenClosedValue;
import com.rehivetech.beeeon.adapter.device.values.PressureValue;
import com.rehivetech.beeeon.adapter.device.values.TemperatureValue;
import com.rehivetech.beeeon.adapter.device.values.UnknownValue;

/**
 * Device's types
 */
public enum DeviceType {

	TYPE_UNKNOWN(-1, R.string.dev_unknown_type, false, UnknownValue.class), // unknown device

	// =============== Sensors ===============
	TYPE_HUMIDITY(0x01, R.string.dev_humidity_type, false, HumidityValue.class), // humidity meter
	TYPE_PRESSURE(0x02, R.string.dev_pressure_type, false, PressureValue.class), // pressure meter
	TYPE_OPEN_CLOSED(0x03, R.string.dev_open_closed_type, false, OpenClosedValue.class), // open/closed sensor
	TYPE_ON_OFF(0x04, R.string.dev_on_off_type, false, OnOffValue.class), // on/off sensor
	TYPE_ILLUMINATION(0x05, R.string.dev_illumination_type, false, IlluminationValue.class), // illumination meter
	TYPE_NOISE(0x06, R.string.dev_noise_type, false, NoiseValue.class), // noise meter
	TYPE_EMISSION(0x07, R.string.dev_emission_type, false, EmissionValue.class), // emission meter
	// TYPE_POSITION(0x08, R.string.dev_position_type, false, BitArrayValue.class), // position meter
	// 9 is missing, it's racism! :(
	TYPE_TEMPERATURE(0x0A, R.string.dev_temperature_type, false, TemperatureValue.class), // temperature meter

	// =============== Actors ================
	TYPE_ACTOR_ON_OFF(0xA0, R.string.dev_actor_on_off_type, true, OnOffValue.class), // on/off actor
	TYPE_ACTOR_PUSH(0xA1, R.string.dev_actor_push_type, true, UnknownValue.class /*PushValue.class*/), // push (on-only) actor
	TYPE_ACTOR_TOGGLE(0xA2, R.string.dev_actor_toggle_type, true, UnknownValue.class /*ToggleValue.class*/), // toggle actor
	TYPE_ACTOR_RANGE(0xA3, R.string.dev_actor_range_type, true, UnknownValue.class /*RangeValue.class*/), // range actor
	TYPE_ACTOR_RGB(0xA4, R.string.dev_actor_rgb_type, true, UnknownValue.class /*RGBValue.class*/); // RGB actor

	private final int mTypeId;
	private final int mNameRes;
	private final Class<? extends BaseValue> mValueClass;
	private final boolean mIsActor;

	private DeviceType(int id, int nameRes, boolean isActor, Class<? extends BaseValue> valueClass) {
		mTypeId = id;
		mNameRes = nameRes;
		mIsActor = isActor;
		mValueClass = valueClass;
	}

	public int getTypeId() {
		return mTypeId;
	}

	public int getStringResource() {
		return mNameRes;
	}

	public boolean isActor() {
		return mIsActor;
	}

	public Class<? extends BaseValue> getValueClass() {
		return mValueClass;
	}

	public static DeviceType fromTypeId(int typeId) {
		// Get the DeviceType object based on type number
		for (DeviceType item : values()) {
			if (typeId == item.getTypeId()) {
				return item;
			}
		}

		return TYPE_UNKNOWN;
	}

	public static DeviceType fromValue(String value) {
		if (value.isEmpty())
			return TYPE_UNKNOWN;

		return fromTypeId(Integer.parseInt(value));
	}

}
