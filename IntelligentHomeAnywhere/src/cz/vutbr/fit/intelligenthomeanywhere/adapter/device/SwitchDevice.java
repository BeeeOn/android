package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.XmlDeviceParser;

public class SwitchDevice extends BaseDevice {

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

}
