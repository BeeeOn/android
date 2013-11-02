package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
/**
 * Class that extends BaseDevice for pressure meter
 * @author ThinkDeep
 *
 */
public class PressureDevice extends BaseDevice {

	private int mValue;
	
	@Override
	public void setValue(int value){
		mValue = value;
	}
	
	@Override
	public int getType() {
		return Constants.TYPE_PRESSURE;
	}
	
	@Override
	public int getTypeStringResource() {
		return R.string.pressure;
	}
	
	@Override
	public int getTypeIconResource() {
		// TODO return icon resource
		return 0;
	}

	@Override
	public int getUnitStringResource() {
		return R.string.pressure_Pa;
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
