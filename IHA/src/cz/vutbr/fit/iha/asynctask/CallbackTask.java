package cz.vutbr.fit.iha.asynctask;

import android.os.AsyncTask;

public abstract class CallbackTask<Params> extends AsyncTask<Params, Void, Boolean> {

	private CallbackTaskListener mListener;
	
	public interface CallbackTaskListener {
		public void onExecute(boolean success);
	}
	
	public void setListener(CallbackTaskListener listener) {
		mListener = listener;
	}
	
	@Override
	protected void onPostExecute(Boolean success) {
		mListener.onExecute(success);
	}

}