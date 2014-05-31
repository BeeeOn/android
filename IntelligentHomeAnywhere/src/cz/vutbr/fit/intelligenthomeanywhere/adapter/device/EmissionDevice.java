package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
/**
 * Class that extend BaseDevice for emmision meter
 * @author ThinkDeep
 *
 */
public class EmissionDevice extends BaseDevice {

	private int mValue = Integer.MAX_VALUE;
	
	@Override
	public int getType() {
		return Constants.TYPE_EMMISION;
	}

	@Override
	public int getTypeStringResource() {
		return R.string.emission;
	}

	@Override
	public int getTypeIconResource() {
		return R.drawable.dev_emission;
	}

	@Override
	public int getUnitStringResource() {
		return R.string.emission_ppm;
	}
	
	@Override
	public void setValue(int value){
		mValue = value;
	}
	
	@Override
	public void setValue(String value) {
		mValue = Integer.parseInt(value);
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
	public String getStringValue() {
		return Integer.toString(mValue);
	}

}
