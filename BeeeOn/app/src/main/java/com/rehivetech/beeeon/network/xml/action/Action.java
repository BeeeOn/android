/**
 * 
 */
package com.rehivetech.beeeon.network.xml.action;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.household.device.Device;

/**
 * @author ThinkDeep
 * 
 */
public class Action {

	public enum ActionType implements IIdentifier {
		ACTOR("actor"), //
		NOTIFICATION("notification"), //
		UNKNOWN("");

		private final String mValue;

		ActionType(String value) {
			mValue = value;
		}

		public String getId() {
			return mValue;
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

	public ActionType getType() {
		return mType;
	}

	public void setDevice(Device device) {
		mDevice = device;
	}

	public Device getDevice() {
		return mDevice;
	}

	public void setValue(String value) {
		mValue = value;
	}

	public String getValue() {
		return mValue;
	}

}
