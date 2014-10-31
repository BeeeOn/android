package cz.vutbr.fit.iha.customview;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.Device;
import cz.vutbr.fit.iha.adapter.device.DeviceLog;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataInterval;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataType;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.pair.LogDataPair;

public class GraphsCustomView {

	private static final String TAG = GraphsCustomView.class.getSimpleName();
	
	private Context mContext;
	private Controller mController;
	
	private GetDeviceLogTask mGetDeviceLogTask;
	
	private SparseArray<List<Device>> mDevices = new SparseArray<List<Device>>();
	private SparseArray<List<DeviceLog>> mLogs = new SparseArray<List<DeviceLog>>();
	
	public GraphsCustomView(Context context) {
		mContext = context;
		mController = Controller.getInstance(context);
	}
	
	private void prepareDevices() {
		for (Adapter adapter : mController.getAdapters()) {
			for (Facility facility: mController.getFacilitiesByAdapter(adapter.getId())) {
				for (Device device : facility.getDevices()) {
					List<Device> devices = mDevices.get(device.getType().getTypeId());
					if (devices == null) {
						devices = new ArrayList<Device>();
						mDevices.put(device.getType().getTypeId(), devices);
					}
					
					devices.add(device);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void prepareLogs() {
		for (int i=0; i<mDevices.size(); i++) {
			mGetDeviceLogTask = new GetDeviceLogTask();
			mGetDeviceLogTask.execute(mDevices.valueAt(i));
		}
	}

	private class GetDeviceLogTask extends AsyncTask<List<Device>, Void, List<DeviceLog>> {
		@Override
		protected List<DeviceLog> doInBackground(List<Device>... devices) {
			List<Device> list = devices[0]; // expects only one device list at a time is sent there
			
			DateTime end = DateTime.now(DateTimeZone.UTC);
			DateTime start = end.minusWeeks(1);
			
			List<DeviceLog> logs = new ArrayList<DeviceLog>();
			
			for (Device device : list) {
				LogDataPair pair = new LogDataPair( //
						device, // device
						new Interval(start, end), // interval from-to
						DataType.AVERAGE, // type
						DataInterval.HOUR); // interval

				logs.add(mController.getDeviceLog(pair.device, pair));	
			}
			
			return logs;
		}

		@Override
		protected void onPostExecute(List<DeviceLog> logs) {
			//fillGraph(logs);
		}

	}
	
}
