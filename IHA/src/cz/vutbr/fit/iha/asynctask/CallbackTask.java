package cz.vutbr.fit.iha.asynctask;

import android.os.AsyncTask;

public abstract class CallbackTask<Params> extends AsyncTask<Params, Void, Boolean> {

	private CallbackTaskListener mListener;
	
	public interface CallbackTaskListener {
		public void onExecute(boolean success);
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
		return doInBackground(params[0]);
	}
	
	protected abstract Boolean doInBackground(Params param);
	
	@Override
	protected final void onPostExecute(Boolean success) {
		mListener.onExecute(success);
	}

}