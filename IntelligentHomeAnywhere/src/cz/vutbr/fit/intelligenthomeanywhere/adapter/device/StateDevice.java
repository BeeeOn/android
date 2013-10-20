package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.XmlDeviceParser;

public class StateDevice extends BaseDevice {

	@Override
	public int getType() {
		return XmlDeviceParser.TYPE_STATE;
	}
	
	@Override
	public int getTypeStringResource() {
		return R.string.switch_s;
	}
	
	@Override
	public int getTypeIconResource() {
		// TODO return icon resource
		return 0;
	}

	@Override
	public int getUnitStringResource() {
		return 0; // TODO: or "open"/"closed" depending on actual value?
	}

}
