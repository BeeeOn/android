package cz.vutbr.fit.iha.adapter.device;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.values.BaseValue;
import cz.vutbr.fit.iha.adapter.device.values.EmissionValue;
import cz.vutbr.fit.iha.adapter.device.values.HumidityValue;
import cz.vutbr.fit.iha.adapter.device.values.IlluminationValue;
import cz.vutbr.fit.iha.adapter.device.values.NoiseValue;
import cz.vutbr.fit.iha.adapter.device.values.OnOffValue;
import cz.vutbr.fit.iha.adapter.device.values.OpenClosedValue;
import cz.vutbr.fit.iha.adapter.device.values.PressureValue;
import cz.vutbr.fit.iha.adapter.device.values.TemperatureValue;
import cz.vutbr.fit.iha.adapter.device.values.UnknownValue;

/**
 * Device's types
 */
public enum DeviceType {

	TYPE_UNKNOWN(-1, R.string.dev_unknown_type, false, UnknownValue.class), // unknown device

	// =============== Sensors ===============
	TYPE_HUMIDITY(1, R.string.dev_humidity_type, false, HumidityValue.class), // humidity meter
	TYPE_PRESSURE(2, R.string.dev_pressure_type, false, PressureValue.class), // pressure meter
	TYPE_OPEN_CLOSED(3, R.string.dev_state_type, false, OpenClosedValue.class), // state sensor
	TYPE_ON_OFF(4, R.string.dev_switch_type, false, OnOffValue.class), // switch sensor
	TYPE_ILLUMINATION(5, R.string.dev_illumination_type, false, IlluminationValue.class), // illumination meter
	TYPE_NOISE(6, R.string.dev_noise_type, false, NoiseValue.class), // noise meter
	TYPE_EMISSION(7, R.string.dev_emission_type, false, EmissionValue.class), // emission meter
	// TYPE_POSITION(8, R.string.dev_position_type, false, BitArrayValue.class), // position meter
	// 9 is missing, it's racism! :(
	TYPE_TEMPERATURE(10, R.string.dev_actor_on_off_type, false, TemperatureValue.class), // temperature meter

	// =============== Actors ================
	TYPE_ACTOR_ON_OFF(160, R.string.dev_actor_on_off_type, true, OnOffValue.class); // on/off actor

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

	private Class<? extends BaseValue> getValueClass() {
		return mValueClass;
	}

	public static DeviceType fromValue(int value) {
		for (DeviceType item : values()) {
			if (value == item.getTypeId())
				return item;
		}
		return TYPE_UNKNOWN;
	}

	public static BaseValue createDeviceValue(DeviceType type) {
		try {
			// Try to create and return new BaseValue object
			return type.getValueClass().newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		// If creation failed, create UnknownValue object
		return new UnknownValue();
	}

	public static Device createDeviceFromType(int typeId) {
		DeviceType type = fromValue(typeId);
		BaseValue value = createDeviceValue(type);

		return new Device(type, value);
	}

}
