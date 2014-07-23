package cz.vutbr.fit.iha.adapter.device;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
/**
 * Class that extends BaseDevice for temperature meter
 * @author ThinkDeep
 *
 */
public class TemperatureDevice extends BaseDevice {

	private float mValue = Float.MAX_VALUE;
	
	@Override
	public void setValue(int value){
		mValue = value/100f;
	}
	
	@Override
	public int getType() {
		return Constants.TYPE_TEMPERATURE;
	}
	
	@Override
	public int getTypeStringResource() {
		return R.string.dev_temperature_type;
	}
	
	@Override
	public int getTypeIconResource() {
		return R.drawable.dev_temperature;
	}

	@Override
	public int getUnitStringResource() {
		return R.string.dev_temperature_unit;
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
		mValue = Integer.parseInt(value)/100f;
	}
	
	@Override
	public String getStringValue() {
		return Float.toString(mValue);
	}

}
