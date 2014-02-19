package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
/**
 * Class that extends BaseDevice for humidity meter
 * @author ThinkDeep
 *
 */
public class HumidityDevice extends BaseDevice {

	private int mValue = Integer.MAX_VALUE;

	@Override
	public int getType() {
		return Constants.TYPE_HUMIDITY;
	}
	
	@Override
	public int getTypeStringResource() {
		return R.string.humidity;
	}
	
	@Override
	public int getTypeIconResource() {
		return R.drawable.sensor_humidity;
	}

	@Override
	public int getUnitStringResource() {
		return R.string.humidity_percent;
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
