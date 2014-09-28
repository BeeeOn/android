package cz.vutbr.fit.iha.network.xml.condition;

import cz.vutbr.fit.iha.adapter.device.BaseDevice;

//new drop
public class DewPointFunc extends ConditionFunction{
	private BaseDevice mTempDevice;
	private BaseDevice mHumiDevice;
	
	public DewPointFunc(BaseDevice tempDevice, BaseDevice humiDevice){
		mTempDevice = tempDevice;
		mHumiDevice = humiDevice;
	}
	
	public BaseDevice getTempDevice(){
		return mTempDevice;
	}
	
	public BaseDevice getHumiDevice(){
		return mHumiDevice;
	}
}