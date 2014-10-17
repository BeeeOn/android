package cz.vutbr.fit.iha.adapter.device;

import cz.vutbr.fit.iha.adapter.device.values.BaseDeviceValue;
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

	TYPE_UNKNOWN(-1), 		// unknown device
	TYPE_TEMPERATURE(0), 	// temperature meter
	TYPE_HUMIDITY(1), 		// humidity meter
	TYPE_PRESSURE(2), 		// pressure meter
	TYPE_OPEN_CLOSED(3),	// state sensor
	TYPE_ON_OFF(4), 		// switch sensor
	TYPE_ILLUMINATION(5), 	// illumination meter
	TYPE_NOISE(6), 			// noise meter
	TYPE_EMISSION(7); 		// emission meter
	
	private int mTypeId;

	private DeviceType(int id) {
		mTypeId = id;
	}
	
	public int getTypeId() {
		return mTypeId;
	}

	public static DeviceType fromValue(int value) {
		for (DeviceType item : values()) {
			if (value == item.getTypeId())
				return item;
		}
		return TYPE_UNKNOWN;
	}
	
	public BaseDeviceValue createDeviceValue() {
		if (this.equals(TYPE_EMISSION)) {
			return new EmissionValue();
		} else if (this.equals(TYPE_HUMIDITY)) {
			return new HumidityValue();
		} else if (this.equals(TYPE_ILLUMINATION)) {
			return new IlluminationValue();
		} else if (this.equals(TYPE_NOISE)) {
			return new NoiseValue();
		} else if (this.equals(TYPE_PRESSURE)) {
			return new PressureValue();
		} else if (this.equals(TYPE_OPEN_CLOSED)) {
			return new OpenClosedValue();
		} else if (this.equals(TYPE_ON_OFF)) {
			return new OnOffValue();
		} else if (this.equals(TYPE_TEMPERATURE)) {
			return new TemperatureValue();
		} else {
			return new UnknownValue();
		}
	}
	
	public BaseDevice createDevice() {
		if (this.equals(TYPE_EMISSION)) {
			return new EmissionDevice();
		} else if (this.equals(TYPE_HUMIDITY)) {
			return new HumidityDevice();
		} else if (this.equals(TYPE_ILLUMINATION)) {
			return new IlluminationDevice();
		} else if (this.equals(TYPE_NOISE)) {
			return new NoiseDevice();
		} else if (this.equals(TYPE_PRESSURE)) {
			return new PressureDevice();
		} else if (this.equals(TYPE_OPEN_CLOSED)) {
			return new StateDevice();
		} else if (this.equals(TYPE_ON_OFF)) {
			return new SwitchDevice();
		} else if (this.equals(TYPE_TEMPERATURE)) {
			return new TemperatureDevice();
		} else {
			return new UnknownDevice();
		}
	}

}
