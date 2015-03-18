package com.rehivetech.beeeon.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import com.rehivetech.beeeon.R;

public class AppException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public static AppException wrap(Throwable exception, ErrorCode errorCode) {
		if (exception instanceof AppException) {
			AppException se = (AppException) exception;
			if (errorCode != null && errorCode != se.getErrorCode()) {
				return new AppException(exception.getMessage(), exception, errorCode);
			}
			return se;
		} else {
			return new AppException(exception.getMessage(), exception, errorCode);
		}
	}

	public static AppException wrap(Throwable exception) {
		return wrap(exception, null);
	}

	private ErrorCode mErrorCode;
	private final Map<String, Object> mProperties = new TreeMap<String, Object>();

	public AppException(ErrorCode errorCode) {
		mErrorCode = errorCode;
	}

	public AppException(String message, ErrorCode errorCode) {
		super(message);
		mErrorCode = errorCode;
	}

	public AppException(Throwable cause, ErrorCode errorCode) {
		super(cause);
		mErrorCode = errorCode;
	}

	public AppException(String message, Throwable cause, ErrorCode errorCode) {
		super(message, cause);
		mErrorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return mErrorCode;
	}

	public AppException setErrorCode(ErrorCode errorCode) {
		mErrorCode = errorCode;
		return this;
	}

	public Map<String, Object> getProperties() {
		return mProperties;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String name) {
		return (T) mProperties.get(name);
	}

	public AppException set(String name, Object value) {
		mProperties.put(name, value);
		return this;
	}

	@Override
	public void printStackTrace(PrintStream s) {
		synchronized (s) {
			printStackTrace(new PrintWriter(s));
		}
	}

	@Override
	public void printStackTrace(PrintWriter s) {
		synchronized (s) {
			s.println(this);
			s.println("\t-------------------------------");
			if (mErrorCode != null) {
				s.println("\t" + mErrorCode + ":" + mErrorCode.getClass().getName());
			}
			for (String key : mProperties.keySet()) {
				s.println("\t" + key + "=[" + mProperties.get(key) + "]");
			}
			s.println("\t-------------------------------");
			StackTraceElement[] trace = getStackTrace();
			for (StackTraceElement aTrace : trace) s.println("\tat " + aTrace);

			Throwable ourCause = getCause();
			if (ourCause != null) {
				ourCause.printStackTrace(s);
			}
			s.flush();
		}
	}

	public String getTranslatedErrorMessage(Context context) {
		int resId = 0;

		if (mErrorCode != null) {
			String key = mErrorCode.getClass().getSimpleName() + "___" + mErrorCode;
			resId = context.getResources().getIdentifier(key, "string", context.getPackageName());
		}

        String errorMessage = context.getString(resId > 0 ? resId : R.string.unknown_error);
        return context.getString(R.string.error_message, mErrorCode.getNumber(), errorMessage);
	}

}
