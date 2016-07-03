package com.rehivetech.beeeon.util;


import android.content.Context;
import android.support.annotation.IntDef;
import android.support.design.widget.TextInputLayout;
import android.util.Patterns;
import android.widget.EditText;

import com.rehivetech.beeeon.BeeeOnApplication;
import com.rehivetech.beeeon.R;

import timber.log.Timber;

/**
 * @author mlyko
 * @since 25.05.2016
 */
public class Validator {

	@IntDef(flag = true, value = {INTEGER, DOUBLE, EMAIL, IP_ADDRESS, PORT})
	public @interface ValidationType {
	}

	public static final int INTEGER = 1;
	public static final int DOUBLE = 1 << 1;
	public static final int EMAIL = 1 << 2;
	public static final int IP_ADDRESS = 1 << 3;
	public static final int PORT = 1 << 4;

	/**
	 * Validates specified text layout for emptiness and optional additionals
	 *
	 * @param textInputLayout view which will be validated
	 * @param flags           any additional validations
	 * @return success if input was validated correctly
	 */
	public static boolean validate(TextInputLayout textInputLayout, @ValidationType int flags) {
		EditText editText = textInputLayout.getEditText();
		if (editText == null) {
			Timber.w("Inside TextInputLayout is no EditText!");
			return false;
		}

		Context appContext = BeeeOnApplication.getContext();
		boolean isSuccess = false;

		String input = editText.getText().toString().trim();
		if (input.length() == 0) {
			textInputLayout.requestFocus();
			textInputLayout.setError(appContext.getString(R.string.validation_empty));
			return false;
		}

		// validation integer/double
		if ((flags & INTEGER) == INTEGER || (flags & DOUBLE) == DOUBLE) {
			isSuccess = numberValidation(input);
			if (!isSuccess) {
				textInputLayout.requestFocus();
				textInputLayout.setError(appContext.getString(R.string.validation_number));
				return false;
			}
		}

		// port validation
		if ((flags & PORT) == PORT) {
			isSuccess = portValidation(input);
			if (!isSuccess) {
				textInputLayout.requestFocus();
				textInputLayout.setError(appContext.getString(R.string.validation_port));
				return false;
			}
		}

		if ((flags & EMAIL) == EMAIL) {
			if (!android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
				textInputLayout.requestFocus();
				textInputLayout.setError(appContext.getString(R.string.validation_email));
				return false;
			}
		}

		if ((flags & IP_ADDRESS) == IP_ADDRESS) {
			if (!Patterns.IP_ADDRESS.matcher(input).matches()) {
				textInputLayout.requestFocus();
				textInputLayout.setError(appContext.getString(R.string.validation_ip_address));
				return false;
			}
		}

		textInputLayout.setError(null);
		return true;
	}

	/**
	 * Helper for validating many inputs without any flags
	 *
	 * @param inputLayouts text inputs
	 * @return success (if one fails, all fails)
	 */
	public static boolean validateAll(TextInputLayout... inputLayouts) {
		for (TextInputLayout input : inputLayouts) {
			if (!validate(input)) return false;
		}

		return true;
	}

	/**
	 * Validation for number (double or integer)
	 *
	 * @param input string with number
	 * @return success
	 */
	private static boolean numberValidation(String input) {
		try {
			//noinspection ResultOfMethodCallIgnored
			Double.parseDouble(input);
			//noinspection ResultOfMethodCallIgnored
			Integer.parseInt(input);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Validating view without flags
	 *
	 * @param textInputLayout view to validate
	 * @return success
	 */
	public static boolean validate(TextInputLayout textInputLayout) {
		return validate(textInputLayout, 0);
	}

	/**
	 * Validation for port number
	 *
	 * @param input string which should contain port
	 * @return success
	 */
	public static boolean portValidation(String input) {
		try {
			int port = Integer.parseInt(input);
			return !(port <= 0 || port > (Short.MAX_VALUE << 2 + 1));
		} catch (NumberFormatException e) {
			return false;
		}
	}
}

