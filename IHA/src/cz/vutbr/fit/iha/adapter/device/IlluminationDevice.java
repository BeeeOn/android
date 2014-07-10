package cz.vutbr.fit.iha.adapter.device;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
/**
 * Class that extends BaseDevice for illumination meter
 * @author ThinkDeep
 *
 */
public class IlluminationDevice extends BaseDevice {

	private int mValue = Integer.MAX_VALUE;
	
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

	@Override
	public int getUnitStringResource() {
		return R.string.dev_illumination_unit;
	}
	
	@Override
	public void setValue(int value){
		mValue = value;
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
