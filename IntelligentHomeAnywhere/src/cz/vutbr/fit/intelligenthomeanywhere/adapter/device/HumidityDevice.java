package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;

public class HumidityDevice extends BaseDevice {

	@Override
	public int getType() {
		return Constants.TYPE_HUMIDITY;
	}
	
	@Override
	public int getTypeStringResource() {
		return R.string.humidity;
	}
	
	@Override
	public int getTypeIconResource() {
		// TODO return icon resource
		return 0;
	}

	@Override
	public int getUnitStringResource() {
		return R.string.humidity_percent;
	}

}
