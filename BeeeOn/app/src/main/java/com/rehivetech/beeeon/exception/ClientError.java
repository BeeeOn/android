package com.rehivetech.beeeon.exception;

import com.rehivetech.beeeon.IIdentifier;

public enum ClientError implements ErrorCode, IIdentifier {

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

	public String getId() {
		return String.format("C%d", mNumber);
	}
}
