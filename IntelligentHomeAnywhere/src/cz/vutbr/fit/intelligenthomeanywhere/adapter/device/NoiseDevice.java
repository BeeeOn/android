package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
/**
 * Class that extends BaseDevice for Noise meter
 * @author ThinkDeep
 *
 */
public class NoiseDevice extends BaseDevice {

	private int mValue;
	
	@Override
	public void setValue(int value){
		mValue = value;
	}
	
	@Override
	public int getType() {
		return Constants.TYPE_NOISE;
	}
	
	@Override
	public int getTypeStringResource() {
		return R.string.noise;
	}
	
	@Override
	public int getTypeIconResource() {
		// TODO return icon resource
		return 0;
	}

	@Override
	public int getUnitStringResource() {
		return R.string.noise_dB;
	}

	@Override
	public int getRawIntValue() {
		return mValue;
	}

	@Override
	public float getRawFloatValue() {
		return mValue;
	}
	
	@Override
	public void setValue(String value) {
		mValue = Integer.parseInt(value);
	}
	
	@Override
	public String getStringValue() {
		return Integer.toString(mValue);
	}
}
