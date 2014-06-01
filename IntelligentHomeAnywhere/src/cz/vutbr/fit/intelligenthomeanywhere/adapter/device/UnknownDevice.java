package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
/**
 * Class that extends BaseDevice for all unknown device type
 * @author ThinkDeep
 *
 */
public class UnknownDevice extends BaseDevice {

	private String mValue;
	
	@Override
	public void setValue(String value){
		mValue = value;
	}
	
	@Override
	public int getType() {
		return Constants.TYPE_UNKNOWN;
	}
	
	@Override
	public int getTypeStringResource() {
		return R.string.unknown;
	}
	
	@Override
	public int getTypeIconResource() {
		return R.drawable.dev_unknown;
	}

	@Override
	public int getUnitStringResource() {
		return 0; // TODO: get unknown "???" unit?
	}

	@Override
	public int getRawIntValue() {
		return Integer.MAX_VALUE;
	}

	@Override
	public float getRawFloatValue() {
		return 0;
	}
	
	@Override
	public void setValue(int value) {
		Integer.toString(value);
	}
	
	@Override
	public String getStringValue() {
		return mValue;
	}

}
