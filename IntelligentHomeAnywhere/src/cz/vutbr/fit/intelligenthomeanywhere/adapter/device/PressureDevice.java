package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.XmlDeviceParser;

public class PressureDevice extends BaseDevice {

	@Override
	public int getType() {
		return XmlDeviceParser.TYPE_PRESSURE;
	}
	
	@Override
	public int getTypeString() {
		return R.string.pressure;
	}
}
