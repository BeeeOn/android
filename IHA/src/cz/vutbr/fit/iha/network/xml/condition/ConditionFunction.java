package cz.vutbr.fit.iha.network.xml.condition;

//new drop
public abstract class ConditionFunction{

	public enum FunctionType {
		EQ("eq"), 		// equal
		GT("gt"), 		// greater than
		GE("ge"), 		// greater or equal
		LT("lt"), 		// lesser than
		LE("le"), 		// lesser or equal
		BTW("btw"), 	// betqween
		CHG("chg"), 	// change
		DP("dp"), 		// dew point
		TIME("time"), 	// time
		GEO("geo"),		// geofence
		UNKNOWN("");

		private final String mValue;

		private FunctionType(String value) {
			mValue = value;
		}

		public String getValue() {
			return mValue;
		}

		public static FunctionType fromValue(String value) {
			for (FunctionType item : values()) {
				if (value.equalsIgnoreCase(item.getValue()))
					return item;
			}
			throw new IllegalArgumentException("Invalid FunctionType value");
		}
	}
	
	public ConditionFunction(){}
	
	protected FunctionType mType;
	
	public FunctionType getFuncType(){
		return mType;
	}
}