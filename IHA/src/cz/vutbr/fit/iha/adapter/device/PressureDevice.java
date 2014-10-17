package cz.vutbr.fit.iha.adapter.device;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;

/**
 * Class that extends BaseDevice for pressure meter
 * 
 * @author ThinkDeep
 * 
 */
public class PressureDevice extends BaseDevice {

	@Override
	public int getType() {
		return Constants.TYPE_PRESSURE;
	}

	@Override
	public int getTypeStringResource() {
		return R.string.dev_pressure_type;
	}

	@Override
	public int getTypeIconResource() {
		return R.drawable.dev_pressure;
	}
	
}
