package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.XmlDeviceParser;

public class IlluminationDevice extends BaseDevice {

	@Override
	public int getType() {
		return XmlDeviceParser.TYPE_ILLUMINATION;
	}
	
	@Override
	public int getTypeString() {
		return R.string.illumination;
	}
}
