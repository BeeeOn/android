package com.rehivetech.beeeon.network.xml.condition;

import com.rehivetech.beeeon.adapter.device.Device;

//new drop
public class ChangeFunc extends ConditionFunction {
	private Device mDevice;

	public ChangeFunc(Device device) {
		mDevice = device;
	}

	public Device getDevice() {
		return mDevice;
	}
}