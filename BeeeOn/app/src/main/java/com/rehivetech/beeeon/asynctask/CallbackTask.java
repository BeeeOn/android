package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.util.Log;

public abstract class CallbackTask<Params> extends AsyncTask<Params, Void, Boolean> {

	private static final String TAG = CallbackTask.class.getSimpleName();

	private CallbackTaskListener mListener;
	private CallbackTaskPreExecuteListener mPreExecuteListener;

	protected final Context mContext;

	private AppException mException;

	private boolean mNotifyErrors = true; // TODO: keep it enabled by default?

	private boolean mIsWorking = false;

	public CallbackTask(@NonNull Context context) {
		mContext = context.getApplicationContext();
	}

	public final void setListener(@Nullable CallbackTaskListener listener) {
		mListener = listener;
	}

	@Nullable
	public final CallbackTaskListener getListener() {
		return mListener;
	}

	public final void setPreExecuteListener(@Nullable CallbackTaskPreExecuteListener listener) {
		mPreExecuteListener = listener;
	}

	@Nullable
	public final CallbackTaskPreExecuteListener getPreExecuteListener() {
		return mPreExecuteListener;
	}


	@SuppressWarnings("unchecked")
	public final void execute(Params param) {
		super.execute(param);
	}

	public final void execute() {
		super.execute();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		if (mPreExecuteListener != null) {
			mPreExecuteListener.onPreExecute();
		}
	}

	@Override
	protected final Boolean doInBackground(Params... params) {
		if (params.length > 1) {
			Log.w(TAG, "Given %d parameters, but CallbackTask can use only one.");
		}

		mIsWorking = true;

		Boolean success = false;
		try {
			success = doInBackground(params.length > 0 ? params[0] : null);
		} catch (AppException e) {
			// Remember exception so caller can get it via calling getException()
			mException = e;
			// And print it to log, so we know that something happened
			Log.e(CallbackTask.this.getClass().getSimpleName(), e.getSimpleErrorMessage());
		}

		mIsWorking = false;
		return success;
	}

	protected abstract Boolean doInBackground(Params param);

	@Override
	protected final void onPostExecute(Boolean success) {
		if (mListener != null) {
			mListener.onExecute(success);
		}

		// TODO: uncomment this after checking functionality
		if (mNotifyErrors && mException != null/* && !isCancelled()*/) {
			Toast.makeText(mContext, mException.getTranslatedErrorMessage(mContext), Toast.LENGTH_LONG).show();
		}
	}

	public final void cancelTask() {
		this.cancel(true);

		// If task is still working, then we will interrupt the network connection
		if (mIsWorking) {
			Controller.getInstance(mContext).interruptConnection();
		}
	}

	public final void setNotifyErrors(boolean notifyErrors) {
		mNotifyErrors = notifyErrors;
	}

	@Nullable
	public final AppException getException() {
		return mException;
	}

	public interface CallbackTaskListener {
		/**
		 * This is executed on UI thread in onPostExecute method.
		 *
		 * @param success
		 */
		void onExecute(boolean success);
	}

	public interface CallbackTaskPreExecuteListener {
		/**
		 * This is executed on background thread in doInBackground method.
		 */
		void onPreExecute();
	}

}