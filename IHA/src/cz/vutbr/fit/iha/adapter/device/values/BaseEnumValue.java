package cz.vutbr.fit.iha.adapter.device.values;

public abstract class BaseEnumValue extends BaseDeviceValue {

	/**
	 * @return Color depending on active value
	 */
	abstract public int getColorByState();
	
	/**
	 * @return Resource for human readable string representing active value
	 */
	abstract public int getStateStringResource(); 

}
