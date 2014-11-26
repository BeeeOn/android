package cz.vutbr.fit.iha.exception;

public enum NetworkError implements ErrorCode {

	// FROM CLIENT
	NO_CONNECTION(-1),
	SERVER_NOT_RESPONDING(-2),
	COM_PROBLEMS(-3),
	XML(-4),
	GOOGLE_TOKEN(-5),
	
	// FROM SERVER
	COM_VER_MISMATCH(1),
	NOT_VALID_USER(2),
	ADAPTER_NOT_EXISTS(5),
	ADAPTER_NOT_FREE(6),
	ADAPTER_HAVE_YET(7),
	BAD_AGREG_FUNC(8),
	BAD_INTERVAL(9),
	NOT_CONSISTENT_SENSOR_ADDR(10),
	BAD_LOCATION_TYPE(11),
	DAMAGED_XML(12),
	NO_SUCH_ENTITY(13),
	BAD_ICON(14),
	BAD_ACTION(15),
	LOW_RIGHTS(16),
	BAD_EMAIL_OR_ROLE(17),
	BAD_UTC(18),
	BAD_ACTOR_VALUE(19),
	BAD_UID(20),
	ADA_SERVER_PROBLEM(100);

	private final int mNumber;

	private NetworkError(int number) {
		mNumber = number;
	}

	@Override
	public int getNumber() {
		return mNumber;
	}
	
	public static NetworkError fromValue(int value) {
		for (NetworkError item : values()) {
			if (value == item.getNumber())
				return item;
		}
		throw new IllegalArgumentException("Invalid NetworkError value");
	}

}
