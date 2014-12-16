package cz.vutbr.fit.iha.activity.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.Device;
import cz.vutbr.fit.iha.adapter.device.DeviceLog;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataInterval;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataType;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.base.TrackFragment;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.pair.LogDataPair;
import cz.vutbr.fit.iha.util.GraphViewHelper;
import cz.vutbr.fit.iha.util.Log;
import cz.vutbr.fit.iha.util.TimeHelper;
import cz.vutbr.fit.iha.util.UnitsHelper;

public class CustomViewFragment extends TrackFragment {

	private SparseArray<List<Device>> mDevices = new SparseArray<List<Device>>();
	private SparseArray<List<DeviceLog>> mLogs = new SparseArray<List<DeviceLog>>();
	private SparseArray<GraphView> mGraphs = new SparseArray<GraphView>();

	private String mGraphDateTimeFormat = "dd.MM. kk:mm";

	private LinearLayout mLayout;

	private static final String TAG = CustomViewFragment.class.getSimpleName();

	private Controller mController;
	private Context mContext;

	public CustomViewFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = getActivity().getApplicationContext();
		mController = Controller.getInstance(mContext);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.graphofsensors, container, false);

		mLayout = (LinearLayout) view.findViewById(R.id.container);

		prepareDevices();
		loadData();

		return view;
	}

	private void addGraph(final Device device) {
		// Prepare helpers
		final UnitsHelper unitsHelper = new UnitsHelper(mController.getUserSettings(), mContext);
		final TimeHelper timeHelper = new TimeHelper(mController.getUserSettings());
		final DateTimeFormatter fmt = timeHelper.getFormatter(mGraphDateTimeFormat, null); // Force use local time
																							// because we mix data from
																							// different adapters

		// Create and set graphView
		GraphView graphView = GraphViewHelper.prepareGraphView(mContext, "", device, fmt, unitsHelper); // empty heading
		graphView.setShowLegend(true);

		if (graphView instanceof LineGraphView) {
			((LineGraphView) graphView).setDrawBackground(false);
		}

		// Inflate layout
		LayoutInflater inflater = getLayoutInflater(null);
		View row = inflater.inflate(R.layout.custom_graph_item, mLayout, false);

		// Set title
		TextView tv = (TextView) row.findViewById(R.id.graph_label);
		tv.setText(mContext.getString(device.getType().getStringResource()));

		// Add graph to layout
		((LinearLayout) row.findViewById(R.id.graph_layout)).addView(graphView);
		mGraphs.put(device.getType().getTypeId(), graphView);

		// Add whole item to global layout
		mLayout.addView(row);
	}

	private void fillGraph(DeviceLog log, Device device) {

		GraphView graphView = mGraphs.get(device.getType().getTypeId());
		if (graphView == null) {
			return;
		}

		Random random = new Random();
		int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));

		// for (DeviceLog log : logs) {

		GraphViewSeriesStyle seriesStyle = new GraphViewSeriesStyle(color, 4);

		// GraphViewSeriesStyle seriesStyleBlue = new
		// GraphViewSeriesStyle(mContext.getResources().getColor(R.color.iha_primary_cyan), 2);
		// GraphViewSeriesStyle seriesStyleGray = new
		// GraphViewSeriesStyle(getResources().getColor(R.color.light_gray),2);

		GraphViewSeries graphSeries = new GraphViewSeries(device.getName(), seriesStyle, new GraphViewData[] {
			new GraphView.GraphViewData(0, 0),
		});

		graphView.addSeries(graphSeries);

		int size = log.getValues().size();
		// Log.d(TAG, String.format("Filling graph with %d values. Min: %.1f, Max: %.1f", size, log.getMinimum(),
		// log.getMaximum()));

		int begin;
		GraphView.GraphViewData[] data;

		// Limit amount of showed values
		/*
		 * if (size > MAX_GRAPH_DATA_COUNT) { data = new GraphView.GraphViewData[MAX_GRAPH_DATA_COUNT]; begin = (size -
		 * MAX_GRAPH_DATA_COUNT); } else {
		 */
		data = new GraphView.GraphViewData[size];
		begin = 0;
		// }

		for (int i = begin; i < size; i++) {
			DeviceLog.DataRow row = log.getValues().get(i);

			float value = Float.isNaN(row.value) ? log.getMinimum() : row.value;
			data[i - begin] = new GraphView.GraphViewData(row.dateMillis, value);
			// Log.v(TAG, String.format("Graph value: date(msec): %s, Value: %.1f", fmt.print(row.dateMillis),
			// row.value));
		}

		graphSeries.resetData(data);
	}

	private void prepareDevices() {
		for (Adapter adapter : mController.getAdapters()) {
			Log.d(TAG, String.format("Preparing adapter %s", adapter.getId()));

			for (Facility facility : mController.getFacilitiesByAdapter(adapter.getId())) {
				Log.d(TAG, String.format("Preparing facility with %d device", facility.getDevices().size()));

				for (Device device : facility.getDevices()) {
					Log.d(TAG, String.format("Preparing device %s (type %d)", device.getName(), device.getType().getTypeId()));

					List<Device> devices = mDevices.get(device.getType().getTypeId());
					if (devices == null) {
						devices = new ArrayList<Device>();
						mDevices.put(device.getType().getTypeId(), devices);
						addGraph(device);
					}

					devices.add(device);
				}
			}
		}
	}

	private void loadData() {
		for (int i = 0; i < mDevices.size(); i++) {
			// Load data for this graph
			List<Device> list = mDevices.valueAt(i);
			
			GetDeviceLogTask getDeviceLogTask = new GetDeviceLogTask();
			getDeviceLogTask.execute(list.toArray(new Device[list.size()]));
		}
	}

	private class GetDeviceLogTask extends AsyncTask<Device, Void, Map<Device, DeviceLog>> {

		private int mTypeId = 0;
		
		@Override
		protected Map<Device, DeviceLog> doInBackground(Device... devices) {
			Map<Device, DeviceLog> result = new HashMap<Device, DeviceLog>();
			
			// Remember type of graph we're downloading data for
			mTypeId = devices[0].getType().getTypeId();
			
			for (Device device : devices) {
				DateTime end = DateTime.now(DateTimeZone.UTC);
				DateTime start = end.minusDays(3);// end.minusWeeks(1);

				LogDataPair pair = new LogDataPair( //
						device, // device
						new Interval(start, end), // interval from-to
						DataType.AVERAGE, // type
						DataInterval.HOUR); // interval
				
				result.put(device, mController.getDeviceLog(pair.device, pair));
			}

			return result;
		}

		@Override
		protected void onPostExecute(Map<Device, DeviceLog> logs) {
			// Fill graph with data
			for (Map.Entry<Device, DeviceLog> entry : logs.entrySet()) {
				fillGraph(entry.getValue(), entry.getKey());	
			}
			
			// Hide loading label for this graph
			GraphView graphView = mGraphs.get(mTypeId);
			((View) graphView.getParent().getParent()).findViewById(R.id.graph_loading).setVisibility(View.INVISIBLE);
		}

	}

}
