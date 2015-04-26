package com.rehivetech.beeeon.exception;

public enum NetworkError implements ErrorCode {

	// FROM CLIENT (use CL_ prefix)
	CL_INTERNET_CONNECTION(-1),
	CL_SERVER_CONNECTION(-2),
	CL_UNKNOWN_HOST(-3),
	CL_XML(-4),
	CL_SOCKET(-5),
	CL_CERTIFICATE(-6),
	
	// UNKNOWN ERROR
	UNKNOWN(0),
	
	// FROM SERVER (use SRV_ prefix)
	SRV_COM_VER_MISMATCH(1),
	SRV_NOT_VALID_USER(2),
	SRV_USER_EXISTS(3),
	SRV_ADAPTER_NOT_EXISTS(5),
	SRV_ADAPTER_NOT_FREE(6),
	SRV_ADAPTER_HAVE_YET(7),
	SRV_BAD_AGREG_FUNC(8),
	SRV_BAD_INTERVAL(9),
	SRV_NOT_CONSISTENT_SENSOR_ADDR(10),
	SRV_BAD_LOCATION_TYPE(11),
	SRV_DAMAGED_XML(12),
	SRV_NO_SUCH_ENTITY(13),
	SRV_BAD_ICON(14),
	SRV_BAD_ACTION(15),
	SRV_LOW_RIGHTS(16),
	SRV_BAD_EMAIL_OR_ROLE(17),
	SRV_BAD_UTC(18),
	SRV_BAD_ACTOR_VALUE(19),
	SRV_BAD_BT(20),
	SRV_IMPROPER_PSWD(21),
	SRV_IMPROPER_NAME_OR_EMAIL(22),
	SRV_PENDING_USER(23),
	SRV_USER_NOT_EXISTS(24),
	SRV_BAD_PSWD(25),
	SRV_SUSPECT_USER(26),
	SRV_INVALID_PROVIDER(27),
	SRV_ADA_SERVER_PROBLEM(100),
	SRV_INVALID_REQUEST(999);

	public static final String PARAM_COM_VER_LOCAL = "Local version";
	public static final String PARAM_COM_VER_SERVER = "Server version";

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
		return UNKNOWN;
	}

}
