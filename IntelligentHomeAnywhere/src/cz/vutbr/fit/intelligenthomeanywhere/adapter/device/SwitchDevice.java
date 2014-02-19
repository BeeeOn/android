package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
/**
 * Class that extends BaseDevice for switch sensor
 * @author ThinkDeep
 *
 */
public class SwitchDevice extends BaseDevice {

	private String mValue;
	
	@Override
	public void setValue(String value){
		mValue = value;
	}
	
	public static final String STATE_ON = "ON";
	public static final String STATE_OFF = "OFF";
	
	@Override
	public int getType() {
		return Constants.TYPE_SWITCH;
	}
	
	@Override
	public String getStringValue() {
		return mValue;
	}
	
	@Override
	public int getTypeStringResource() {
		return R.string.switch_c;
	}
	
	@Override
	public int getTypeIconResource() {
		// TODO return icon resource
		return 0;
	}

	@Override
	public int getUnitStringResource() {
		return 0; // TODO: or "on"/"off" depending on actual value?
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
	
	/**
	 * Return info about active state of device
	 * @return boolean representing active state
	 */
	public boolean isActive() {
		return mValue.equals(STATE_ON);
	}

}
