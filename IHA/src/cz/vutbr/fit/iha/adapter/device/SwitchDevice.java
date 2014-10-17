package cz.vutbr.fit.iha.adapter.device;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.values.OpenClosedValue;

/**
 * Class that extends BaseDevice for switch sensor
 * 
 * @author ThinkDeep
 * 
 */
public class SwitchDevice extends BaseDevice {

	@Override
	public DeviceType getType() {
		return DeviceType.TYPE_ON_OFF;
	}

	@Override
	public int getTypeStringResource() {
		return R.string.dev_switch_type;
	}

	@Override
	public int getTypeIconResource() {
		return ((OpenClosedValue)getValue()).isActive() ? R.drawable.dev_switch_on : R.drawable.dev_switch_off;
	}

}
