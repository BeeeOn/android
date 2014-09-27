package cz.vutbr.fit.iha.network.xml;

import cz.vutbr.fit.iha.adapter.device.BaseDevice;

//new drop
public class ChangeFunc extends ConditionFunction{
	private BaseDevice mDevice;
	
	public ChangeFunc(BaseDevice device){
		mDevice = device;
	}
	
	public BaseDevice getDevice(){
		return mDevice;
	}
}