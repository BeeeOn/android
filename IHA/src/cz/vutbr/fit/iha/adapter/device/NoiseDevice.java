package cz.vutbr.fit.iha.adapter.device;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;

/**
 * Class that extends BaseDevice for Noise meter
 * 
 * @author ThinkDeep
 * 
 */
public class NoiseDevice extends BaseDevice {

	@Override
	public int getType() {
		return Constants.TYPE_NOISE;
	}

	@Override
	public int getTypeStringResource() {
		return R.string.dev_noise_type;
	}

	@Override
	public int getTypeIconResource() {
		return R.drawable.dev_noise;
	}
	
}
