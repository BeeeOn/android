package cz.vutbr.fit.iha.adapter.device;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.values.OpenClosedValue;

/**
 * Class that extends BaseDevice for State sensor
 * 
 * @author ThinkDeep
 * 
 */
public class StateDevice extends BaseDevice {

	@Override
	public DeviceType getType() {
		return DeviceType.TYPE_OPEN_CLOSED;
	}

	@Override
	public int getTypeStringResource() {
		return R.string.dev_state_type;
	}

	@Override
	public int getTypeIconResource() {
		return ((OpenClosedValue)getValue()).isActive() ? R.drawable.dev_state_open : R.drawable.dev_state_closed;
	}

}
