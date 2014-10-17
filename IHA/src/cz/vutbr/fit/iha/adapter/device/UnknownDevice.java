package cz.vutbr.fit.iha.adapter.device;

import cz.vutbr.fit.iha.R;

/**
 * Class that extends BaseDevice for all unknown device type
 * 
 * @author ThinkDeep
 * 
 */
public class UnknownDevice extends BaseDevice {

	@Override
	public DeviceType getType() {
		return DeviceType.TYPE_UNKNOWN;
	}

	@Override
	public int getTypeStringResource() {
		return R.string.dev_unknown_type;
	}

	@Override
	public int getTypeIconResource() {
		return R.drawable.dev_unknown;
	}

}
