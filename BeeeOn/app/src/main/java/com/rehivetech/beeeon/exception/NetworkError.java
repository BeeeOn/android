package com.rehivetech.beeeon.exception;

import com.rehivetech.beeeon.IIdentifier;

public enum NetworkError implements IErrorCode, IIdentifier {
	UNKNOWN(0),

	COM_VER_MISMATCH(1),
	NOT_VALID_USER(2),
	USER_EXISTS(3),
	GATE_NOT_EXISTS(5),
	GATE_NOT_FREE(6),
	GATE_HAVE_YET(7),
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
	BAD_BT(20),
	IMPROPER_PSWD(21),
	IMPROPER_NAME_OR_EMAIL(22),
	PENDING_USER(23),
	USER_NOT_EXISTS(24),
	BAD_PSWD(25),
	SUSPECT_USER(26),
	INVALID_PROVIDER(27),
	NO_EMAIL(28),
	EMAIL_PROVIDER_MISMATCH(29),
	LOGOUT_FAILED(30),
	ADA_SERVER_PROBLEM(100),

	// Errors from UI server
	INVALID_LOCATION_TYPE(50),
	COM_FW_ALGORITHMS_ERROR(200),
	GENERATE_BTOKEN_ERROR(997),
	INVALID_REQUEST(999);

	public static final String PARAM_COM_VER_LOCAL = "Local version";
	public static final String PARAM_COM_VER_SERVER = "Server version";

	private final int mNumber;

	NetworkError(int number) {
		mNumber = number;
	}

	@Override
	public int getNumber() {
		return mNumber;
	}

	@Override
	public String getErrorCode() {
		return String.format("S%d", mNumber);
	}

	@Override
	public String getId() {
		return String.valueOf(mNumber);
	}
}
