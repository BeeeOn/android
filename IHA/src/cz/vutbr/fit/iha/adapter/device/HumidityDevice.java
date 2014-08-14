package cz.vutbr.fit.iha.adapter.device;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
/**
 * Class that extends BaseDevice for humidity meter
 * @author ThinkDeep
 *
 */
public class HumidityDevice extends BaseDevice {

	private float mValue = Float.MAX_VALUE;

	@Override
	public int getType() {
		return Constants.TYPE_HUMIDITY;
	}
	
	@Override
	public int getTypeStringResource() {
		return R.string.dev_humidity_type;
	}
	
	@Override
	public int getTypeIconResource() {
		return R.drawable.dev_humidity;
	}

	@Override
	public int getUnitStringResource() {
		return R.string.dev_humidity_unit;
	}
	
	@Override
	public void setValue(int value){
		//TODO: check 100
		mValue = value/100f;
	}
	

	@Override
	public int getRawIntValue() {
		return (int)mValue;
	}

	@Override
	public float getRawFloatValue() {
		return mValue;
	}

	@Override
	public void setValue(String value) {
		mValue = Float.parseFloat(value);
		
	}
	
	@Override
	public String getStringValue() {
		return Float.toString(mValue);
	}

}
