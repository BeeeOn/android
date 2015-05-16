package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.util.Log;

public abstract class CallbackTask<Params> extends AsyncTask<Params, Void, Boolean> {

	private static final String TAG = CallbackTask.class.getSimpleName();

	private CallbackTaskListener mListener;

	protected final Context mContext;

	private AppException mException;

	private boolean mNotifyErrors = true; // TODO: keep it enabled by default?

	public CallbackTask(Context context) {
		mContext = context;
	}

	public final void setListener(CallbackTaskListener listener) {
		mListener = listener;
	}

	@SuppressWarnings("unchecked")
	public final void execute(Params param) {
		super.execute(param);
	}

	public final void execute() {
		super.execute();
	}

	@Override
	protected final Boolean doInBackground(Params... params) {
		if (params.length > 1) {
			Log.w(TAG, "Given %d parameters, but CallbackTask can use only one.");
		}

		Boolean success = false;
		try {
			success = doInBackground(params.length > 0 ? params[0] : null);
		} catch (AppException e) {
			// Remember exception so caller can get it via calling getException()
			mException = e;
			// And print it to log, so we know that something happened
			Log.e(CallbackTask.this.getClass().getSimpleName(), e.getSimpleErrorMessage());
		}
		return success;
	}

	protected abstract Boolean doInBackground(Params param);

	@Override
	protected final void onPostExecute(Boolean success) {
		mListener.onExecute(success);

		if (mNotifyErrors && mException != null) {
			Toast.makeText(mContext, mException.getTranslatedErrorMessage(mContext), Toast.LENGTH_LONG).show();
		}
	}

	public final void setNotifyErrors(boolean notifyErrors) {
		mNotifyErrors = notifyErrors;
	}

	public final AppException getException() {
		return mException;
	}

	public interface CallbackTaskListener {
		void onExecute(boolean success);
	}

}