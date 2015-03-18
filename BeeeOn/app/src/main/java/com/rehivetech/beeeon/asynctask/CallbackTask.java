package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.exception.AppException;

public abstract class CallbackTask<Params> extends AsyncTask<Params, Void, Boolean> {

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
	protected final void execute(Params param) {
		super.execute(param);
	}

	@Override
	protected final Boolean doInBackground(Params... params) {
		Boolean success = false;
		try {
			success = doInBackground(params[0]);
		} catch (AppException e) {
			mException = e;
		}
		return success;
	}

	protected abstract Boolean doInBackground(Params param);

	@Override
	protected final void onPostExecute(Boolean success) {
		mListener.onExecute(success);

		if (mNotifyErrors && !success) {
			String errorMessage = mException != null ? mException.getTranslatedErrorMessage(mContext) : mContext.getString(R.string.unknown_error);
			Toast.makeText(mContext, errorMessage, Toast.LENGTH_LONG).show();
		}
	}

	public final void setNotifyErrors(boolean notifyErrors) {
		mNotifyErrors = notifyErrors;
	}

	public final AppException getException() {
		return mException;
	}

	public interface CallbackTaskListener {
		public void onExecute(boolean success);
	}

}