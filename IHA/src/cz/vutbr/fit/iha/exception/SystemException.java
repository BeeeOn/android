package cz.vutbr.fit.iha.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import cz.vutbr.fit.iha.R;

public class SystemException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public static SystemException wrap(Throwable exception, ErrorCode errorCode) {
		if (exception instanceof SystemException) {
			SystemException se = (SystemException) exception;
			if (errorCode != null && errorCode != se.getErrorCode()) {
				return new SystemException(exception.getMessage(), exception, errorCode);
			}
			return se;
		} else {
			return new SystemException(exception.getMessage(), exception, errorCode);
		}
	}

	public static SystemException wrap(Throwable exception) {
		return wrap(exception, null);
	}

	private ErrorCode mErrorCode;
	private final Map<String, Object> mProperties = new TreeMap<String, Object>();

	public SystemException(ErrorCode errorCode) {
		mErrorCode = errorCode;
	}

	public SystemException(String message, ErrorCode errorCode) {
		super(message);
		mErrorCode = errorCode;
	}

	public SystemException(Throwable cause, ErrorCode errorCode) {
		super(cause);
		mErrorCode = errorCode;
	}

	public SystemException(String message, Throwable cause, ErrorCode errorCode) {
		super(message, cause);
		mErrorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return mErrorCode;
	}

	public SystemException setErrorCode(ErrorCode errorCode) {
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

	public SystemException set(String name, Object value) {
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
			for (int i = 0; i < trace.length; i++)
				s.println("\tat " + trace[i]);

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
			String key = mErrorCode.getClass().getSimpleName() + "__" + mErrorCode;
			resId = context.getResources().getIdentifier(key, "string", context.getPackageName());
		}

		return context.getString(resId != 0 ? resId : R.string.unknown_error);
	}

}
