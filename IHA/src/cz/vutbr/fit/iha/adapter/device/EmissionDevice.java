package cz.vutbr.fit.iha.adapter.device;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;

/**
 * Class that extend BaseDevice for emmision meter
 * 
 * @author ThinkDeep
 * 
 */
public class EmissionDevice extends BaseDevice {

	@Override
	public int getType() {
		return Constants.TYPE_EMMISION;
	}

	@Override
	public int getTypeStringResource() {
		return R.string.dev_emission_type;
	}

	@Override
	public int getTypeIconResource() {
		return R.drawable.dev_emission;
	}

}
