package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import android.os.AsyncTask;

import com.rehivetech.beeeon.household.device.DeviceLog;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.LogDataPair;

/**
 * ASYNC TASK - for load data for graph
 */
public class GetDeviceLogTask extends AsyncTask<LogDataPair, Void, DeviceLog> {
	private Context mContext;
	private CallbackLogTaskListener mListener;

	public GetDeviceLogTask(Context context){
		mContext = context;
	}

	@Override
	protected DeviceLog doInBackground(LogDataPair... pairs) {
		LogDataPair pair = pairs[0]; // expects only one device at a time is sent there
		Controller controller = Controller.getInstance(mContext);
		// Load log data if needed
		controller.getDeviceLogsModel().reloadDeviceLog(pair);
		return controller.getDeviceLogsModel().getDeviceLog(pair);
	}

	@Override
	protected void onPostExecute(DeviceLog res) {
		mListener.onExecute(res);
	}

	public final void setListener(CallbackLogTaskListener listener) {
		mListener = listener;
	}

	public interface CallbackLogTaskListener {
		public void onExecute(DeviceLog result);
	}
}
