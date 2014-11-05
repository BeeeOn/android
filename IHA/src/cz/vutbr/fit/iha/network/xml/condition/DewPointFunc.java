package cz.vutbr.fit.iha.network.xml.condition;

import cz.vutbr.fit.iha.adapter.device.Device;

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