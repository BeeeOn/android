package cz.vutbr.fit.intelligenthomeanywhere.adapter.device;

import android.graphics.Color;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
/**
 * Class that extends BaseDevice for State sensor
 * @author ThinkDeep
 *
 */
public class StateDevice extends BaseDevice {

	private String mValue;
	
	@Override
	public void setValue(String value){
		mValue = value;
	}
	
	public static final String STATE_ON = "ON";
	public static final String STATE_OFF = "OFF";
	
	@Override
	public int getType() {
		return Constants.TYPE_STATE;
	}
	
	@Override
	public int getTypeStringResource() {
		return R.string.switch_s;
	}
	
	@Override
	public int getTypeIconResource() {
		// TODO return icon resource
		return 0;
	}

	@Override
	public int getUnitStringResource() {
		return 0; // TODO: or "open"/"closed" depending on actual value?
	}
	

	@Override
	public int getRawIntValue() {
		return  Integer.MAX_VALUE;
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
	
	/**
	 * Return info about active state of device
	 * @return boolean representing active state
	 */
	public boolean isActive() {
		return mValue.equals(STATE_ON);
	}
	
	/**
	 * Return color by state
	 * @return int representing color GREEN or RED
	 */
	public int getColorByState(){
		return isActive() ? Color.GREEN : Color.RED;
	}
	
	/**
	 * Get resource for human readable string representing state of this device
	 * @return int
	 */
	public int getStateStringResource(){
		return isActive() ? R.string.sensor_open : R.string.sensor_close;
	}

}
