package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;

public class UnknownDevice extends BaseDevice {

	@Override
	public int getType() {
		return Constants.TYPE_UNKNOWN;
	}
	
	@Override
	public int getTypeStringResource() {
		return R.string.unknown;
	}
	
	@Override
	public int getTypeIconResource() {
		// TODO return icon resource
		return 0;
	}

	@Override
	public int getUnitStringResource() {
		return 0; // TODO: get unknown "???" unit?
	}

}
