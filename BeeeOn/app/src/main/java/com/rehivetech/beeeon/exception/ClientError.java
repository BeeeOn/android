package com.rehivetech.beeeon.exception;

import com.rehivetech.beeeon.IIdentifier;

public enum ClientError implements IErrorCode, IIdentifier {
	UNKNOWN(0),

	INTERNET_CONNECTION(1),
	SERVER_CONNECTION(2),
	UNKNOWN_HOST(3),
	XML(4),
	SOCKET(5),
	CERTIFICATE(6),
	NO_RESPONSE(7),
	UNEXPECTED_RESPONSE(8);

	private final int mNumber;

	ClientError(int number) {
		mNumber = number;
	}

	@Override
	public int getNumber() {
		return mNumber;
	}

	@Override
	public String getErrorCode() {
		return String.format("C%d", mNumber);
	}

	@Override
	public String getId() {
		return String.valueOf(mNumber);
	}
}
