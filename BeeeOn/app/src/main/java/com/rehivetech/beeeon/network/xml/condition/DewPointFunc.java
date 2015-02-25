package com.rehivetech.beeeon.network.xml.condition;

import com.rehivetech.beeeon.adapter.device.Device;

//new drop
public class DewPointFunc extends ConditionFunction {
	private Device mTempDevice;
	private Device mHumiDevice;

	public DewPointFunc(Device tempDevice, Device humiDevice) {
		mTempDevice = tempDevice;
		mHumiDevice = humiDevice;
	}

	public Device getTempDevice() {
		return mTempDevice;
	}

	public Device getHumiDevice() {
		return mHumiDevice;
	}
}