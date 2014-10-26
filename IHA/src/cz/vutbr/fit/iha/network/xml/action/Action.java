/**
 * 
 */
package cz.vutbr.fit.iha.network.xml.action;

import cz.vutbr.fit.iha.adapter.device.Device;


/**
 * @author ThinkDeep
 *
 */
public class Action {
	
	public enum ActionType {
		ACTOR("actor"),
		NOTIFICATION("notification"),
		UNKNOWN("");

		private final String mValue;

		private ActionType(String value) {
			mValue = value;
		}

		public String getValue() {
			return mValue;
		}

		public static ActionType fromValue(String value) {
			for (ActionType item : values()) {
				if (value.equalsIgnoreCase(item.getValue()))
					return item;
			}
			throw new IllegalArgumentException("Invalid FunctionType value");
		}
	}
	
	private ActionType mType;
	private Device mDevice;
	private String mValue;
	
	/**
	 * 
	 */
	public Action(ActionType type) {
		mType = type;
	}
	
	public ActionType getType(){
		return mType;
	}
	
	public void setDevice(Device device){
		mDevice = device;
	}
	
	public Device getDevice(){
		return mDevice;
	}
	
	public void setValue(String value){
		mValue = value;
	}
	
	public String getValue(){
		return mValue;
	}

}
