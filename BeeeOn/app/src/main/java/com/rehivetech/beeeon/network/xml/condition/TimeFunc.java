package com.rehivetech.beeeon.network.xml.condition;

//new drop
public class TimeFunc extends ConditionFunction {
	private String mTime;

	public TimeFunc(String time) {
		mTime = time;
	}

	public String getTime() {
		return mTime;
	}
}