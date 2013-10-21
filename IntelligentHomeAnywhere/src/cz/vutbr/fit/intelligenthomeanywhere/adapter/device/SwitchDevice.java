package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.XmlDeviceParser;

public class SwitchDevice extends BaseDevice {

	public static final String STATE_ON = "ON";
	public static final String STATE_OFF = "OFF";
	
	@Override
	public int getType() {
		return XmlDeviceParser.TYPE_SWITCH;
	}
	
	@Override
	public int getTypeStringResource() {
		return R.string.switch_c;
	}
	
	@Override
	public int getTypeIconResource() {
		// TODO return icon resource
		return 0;
	}

	@Override
	public int getUnitStringResource() {
		return 0; // TODO: or "on"/"off" depending on actual value?
	}
	
	/**
	 * Return info about active state of device
	 * @return boolean representing active state
	 */
	public boolean isActive() {
		return mValue.equals(STATE_ON);
	}

}
