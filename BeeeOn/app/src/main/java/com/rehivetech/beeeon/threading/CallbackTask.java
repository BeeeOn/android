package com.rehivetech.beeeon.threading;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;

public abstract class CallbackTask<Params> extends AsyncTask<Params, Void, Boolean> {

	private static final String TAG = CallbackTask.class.getSimpleName();

	private ICallbackTaskListener mListener;
	private ICallbackTaskPreExecuteListener mPreExecuteListener;

	protected final Context mContext;

	private AppException mException;

	private boolean mIsWorking = false;

	public CallbackTask(@NonNull Context context) {
		mContext = context.getApplicationContext();
	}

	public final void setListener(@Nullable ICallbackTaskListener listener) {
		mListener = listener;
	}

	@Nullable
	public final ICallbackTaskListener getListener() {
		return mListener;
	}

	public final void setPreExecuteListener(@Nullable ICallbackTaskPreExecuteListener listener) {
		mPreExecuteListener = listener;
	}

	@Nullable
	public final ICallbackTaskPreExecuteListener getPreExecuteListener() {
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
	}

	public final void cancelTask() {
		this.cancel(true);

		// If task is still working, then we will interrupt the network connection
		if (mIsWorking) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					Controller.getInstance(mContext).interruptConnection();
				}
			}).start();
		}
	}

	@Nullable
	public final AppException getException() {
		return mException;
	}

	public interface ICallbackTaskListener {
		/**
		 * This is executed on UI thread in onPostExecute method.
		 *
		 * @param success
		 */
		void onExecute(boolean success);
	}

	public interface ICallbackTaskPreExecuteListener {
		/**
		 * This is executed on background thread in doInBackground method.
		 */
		void onPreExecute();
	}

}