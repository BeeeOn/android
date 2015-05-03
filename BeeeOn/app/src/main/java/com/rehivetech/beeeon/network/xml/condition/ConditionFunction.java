package com.rehivetech.beeeon.network.xml.condition;

import com.rehivetech.beeeon.IIdentifier;

//new drop
public abstract class ConditionFunction {

	public enum FunctionType implements IIdentifier {
		EQ("eq"), // equal
		GT("gt"), // greater than
		GE("ge"), // greater or equal
		LT("lt"), // lesser than
		LE("le"), // lesser or equal
		BTW("btw"), // betqween
		CHG("chg"), // change
		DP("dp"), // dew point
		TIME("time"), // time
		GEO("geo"), // geofence
		UNKNOWN("");

		private final String mValue;

		FunctionType(String value) {
			mValue = value;
		}

		public String getId() {
			return mValue;
		}
	}

	public ConditionFunction() {
	}

	protected FunctionType mType;

	public FunctionType getFuncType() {
		return mType;
	}
}