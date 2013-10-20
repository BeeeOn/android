package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.XmlDeviceParser;

public class PressureDevice extends BaseDevice {

	@Override
	public int getType() {
		return XmlDeviceParser.TYPE_PRESSURE;
	}
	
	@Override
	public int getTypeStringResource() {
		return R.string.pressure;
	}
	
	@Override
	public int getTypeIconResource() {
		// TODO return icon resource
		return 0;
	}

	@Override
	public int getUnitStringResource() {
		return R.string.pressure_Pa;
	}

}
