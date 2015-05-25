package com.rehivetech.beeeon.exception;

import com.rehivetech.beeeon.IIdentifier;

public interface ErrorCode extends IIdentifier {

	/**
	 * @return identification number of this error
	 */
	int getNumber();

	/**
	 * @return string code to use in error message for user
	 */
	String getErrorCode();
}
