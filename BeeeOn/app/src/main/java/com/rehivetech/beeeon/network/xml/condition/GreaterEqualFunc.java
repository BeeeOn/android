package com.rehivetech.beeeon.network.xml.condition;

import com.rehivetech.beeeon.adapter.device.Device;

//new drop
public class GreaterEqualFunc extends ConditionFunction {
	private String mValue;
	private Device mDevice;

	public GreaterEqualFunc(Device device, String value) {
		mDevice = device;
		mValue = value;
	}

	public Device getDevice() {
		return mDevice;
	}

	public String getValue() {
		return mValue;
	}
}