package com.rehivetech.beeeon.network.xml.condition;

import com.rehivetech.beeeon.adapter.device.Device;

//new drop
public class BetweenFunc extends ConditionFunction {
	private String mMinValue;
	private String mMaxValue;
	private Device mDevice;

	public BetweenFunc(Device device, String minValue, String maxValue) {
		mDevice = device;
		mMinValue = minValue;
		mMaxValue = maxValue;
	}

	public Device getDevice() {
		return mDevice;
	}

	public String getMinValue() {
		return mMinValue;
	}

	public String getMaxValue() {
		return mMaxValue;
	}
}