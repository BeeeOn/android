package cz.vutbr.fit.iha.adapter.device;

import cz.vutbr.fit.iha.R;

/**
 * Class that extends BaseDevice for humidity meter
 * 
 * @author ThinkDeep
 * 
 */
public class HumidityDevice extends BaseDevice {

	@Override
	public DeviceType getType() {
		return DeviceType.TYPE_HUMIDITY;
	}

	@Override
	public int getTypeStringResource() {
		return R.string.dev_humidity_type;
	}

	@Override
	public int getTypeIconResource() {
		return R.drawable.dev_humidity;
	}

}
