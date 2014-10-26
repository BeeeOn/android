package cz.vutbr.fit.iha.network.xml.condition;

import cz.vutbr.fit.iha.adapter.device.Device;

//new drop
public class ChangeFunc extends ConditionFunction{
	private Device mDevice;
	
	public ChangeFunc(Device device){
		mDevice = device;
	}
	
	public Device getDevice(){
		return mDevice;
	}
}