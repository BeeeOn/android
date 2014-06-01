package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
/**
 * Class that extends BaseDevice for temperature meter
 * @author ThinkDeep
 *
 */
public class TemperatureDevice extends BaseDevice {

	private int mValue = Integer.MAX_VALUE;
	
	@Override
	public void setValue(int value){
		mValue = value;
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
