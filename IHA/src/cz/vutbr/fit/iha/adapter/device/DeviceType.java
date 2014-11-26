package cz.vutbr.fit.iha.adapter.device;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.values.BaseValue;
import cz.vutbr.fit.iha.adapter.device.values.EmissionValue;
import cz.vutbr.fit.iha.adapter.device.values.HumidityValue;
import cz.vutbr.fit.iha.adapter.device.values.IlluminationValue;
import cz.vutbr.fit.iha.adapter.device.values.NoiseValue;
import cz.vutbr.fit.iha.adapter.device.values.OnOffActorValue;
import cz.vutbr.fit.iha.adapter.device.values.OnOffValue;
import cz.vutbr.fit.iha.adapter.device.values.OpenClosedValue;
import cz.vutbr.fit.iha.adapter.device.values.PressureValue;
import cz.vutbr.fit.iha.adapter.device.values.TemperatureValue;
import cz.vutbr.fit.iha.adapter.device.values.UnknownValue;

/**
 * Device's types
 */
public enum DeviceType {

	TYPE_UNKNOWN(-1, R.string.dev_unknown_type), // unknown device
	
	TYPE_HUMIDITY(1, R.string.dev_humidity_type), // humidity meter
	TYPE_PRESSURE(2, R.string.dev_pressure_type), // pressure meter
	TYPE_OPEN_CLOSED(3, R.string.dev_state_type), // state sensor
	TYPE_ON_OFF(4, R.string.dev_switch_type), // switch sensor
	TYPE_ILLUMINATION(5, R.string.dev_illumination_type), // illumination meter
	TYPE_NOISE(6, R.string.dev_noise_type), // noise meter
	TYPE_EMISSION(7, R.string.dev_emission_type), // emission meter
	//8 is missing :(
	TYPE_TEMPERATURE(10, R.string.dev_temperature_type), // temperature meter
	
	TYPE_ACTOR_ON_OFF(160, R.string.dev_temperature_type); // test actor

	private final int mTypeId;
	private final int mNameRes;

	private DeviceType(int id, int nameRes) {
		mTypeId = id;
		mNameRes = nameRes;
	}

	public int getTypeId() {
		return mTypeId;
	}

	public int getStringResource() {
		return mNameRes;
	}

	public static DeviceType fromValue(int value) {
		for (DeviceType item : values()) {
			if (value == item.getTypeId())
				return item;
		}
		return TYPE_UNKNOWN;
	}

	public static Device createDeviceFromType(int typeId) {
		DeviceType type = DeviceType.fromValue(typeId);
		BaseValue value = createDeviceValue(type);

		return new Device(type, value);
	}

	public static BaseValue createDeviceValue(DeviceType type) {
		switch (type) {
		case TYPE_EMISSION:
			return new EmissionValue();
		case TYPE_HUMIDITY:
			return new HumidityValue();
		case TYPE_ILLUMINATION:
			return new IlluminationValue();
		case TYPE_NOISE:
			return new NoiseValue();
		case TYPE_PRESSURE:
			return new PressureValue();
		case TYPE_OPEN_CLOSED:
			return new OpenClosedValue();
		case TYPE_ON_OFF:
			return new OnOffValue();
		case TYPE_TEMPERATURE:
			return new TemperatureValue();
		case TYPE_ACTOR_ON_OFF:
			return new OnOffActorValue();
		default:
			return new UnknownValue();
		}
	}

}
