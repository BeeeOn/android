package cz.vutbr.fit.iha.network.xml.condition;

import cz.vutbr.fit.iha.adapter.device.Device;

//new drop
public class GreaterThanFunc extends ConditionFunction{
	private String mValue;
	private Device mDevice;
	
	public GreaterThanFunc(Device device, String value){
		mDevice = device;
		mValue = value;
	}
	
	public Device getDevice(){
		return mDevice;
	}
	
	public String getValue(){
		return mValue;
	}
}