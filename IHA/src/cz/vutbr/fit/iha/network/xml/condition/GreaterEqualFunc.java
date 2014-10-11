package cz.vutbr.fit.iha.network.xml.condition;

import cz.vutbr.fit.iha.adapter.device.BaseDevice;

//new drop
public class GreaterEqualFunc extends ConditionFunction{
	private String mValue;
	private BaseDevice mDevice;
	
	public GreaterEqualFunc(BaseDevice device, String value){
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