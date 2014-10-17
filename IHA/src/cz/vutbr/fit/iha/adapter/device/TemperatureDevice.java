package cz.vutbr.fit.iha.adapter.device;

import cz.vutbr.fit.iha.R;

/**
 * Class that extends BaseDevice for temperature meter
 * 
 * @author ThinkDeep
 * 
 */
public class TemperatureDevice extends BaseDevice {

	@Override
	public DeviceType getType() {
		return DeviceType.TYPE_TEMPERATURE;
	}

	@Override
	public int getTypeStringResource() {
		return R.string.dev_temperature_type;
	}

	@Override
	public int getTypeIconResource() {
		return R.drawable.dev_temperature;
	}

}
