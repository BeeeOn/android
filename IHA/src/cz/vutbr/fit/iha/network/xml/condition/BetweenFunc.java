package cz.vutbr.fit.iha.network.xml.condition;

import cz.vutbr.fit.iha.adapter.device.BaseDevice;

//new drop
public class BetweenFunc extends ConditionFunction{
	private String mMinValue;
	private String mMaxValue;
	private BaseDevice mDevice;
	
	public BetweenFunc(BaseDevice device, String minValue, String maxValue){
		mDevice = device;
		mMinValue = minValue;
		mMaxValue = maxValue;
	}
	
	public BaseDevice getDevice(){
		return mDevice;
	}
	
	public String getMinValue(){
		return mMinValue;
	}
	
	public String getMaxValue(){
		return mMaxValue;
	}
}