package cz.vutbr.fit.iha.adapter.device;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;

/**
 * Class that extends BaseDevice for illumination meter
 * 
 * @author ThinkDeep
 * 
 */
public class IlluminationDevice extends BaseDevice {

	@Override
	public int getType() {
		return Constants.TYPE_ILLUMINATION;
	}

	@Override
	public int getTypeStringResource() {
		return R.string.dev_illumination_type;
	}

	@Override
	public int getTypeIconResource() {
		return R.drawable.dev_illumination;
	}

}
