package cz.vutbr.fit.iha.network.xml;

import cz.vutbr.fit.iha.adapter.device.BaseDevice;

//new drop
public class EqualFunc extends ConditionFunction{
	private String mValue;
	private BaseDevice mDevice;
	
	public EqualFunc(BaseDevice device, String value){
		mDevice = device;
		mValue = value;
	}
	
	public BaseDevice getDevice(){
		return mDevice;
	}
	
	public String getValue(){
		return mValue;
	}
}