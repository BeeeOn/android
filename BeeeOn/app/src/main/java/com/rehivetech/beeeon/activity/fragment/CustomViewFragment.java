package com.rehivetech.beeeon.activity.fragment;

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
import com.jjoe64.graphview.LegendView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.BaseSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceLog;
import com.rehivetech.beeeon.household.device.DeviceLog.DataInterval;
import com.rehivetech.beeeon.household.device.DeviceLog.DataType;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.device.values.BaseEnumValue;
import com.rehivetech.beeeon.base.TrackFragment;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.LogDataPair;
import com.rehivetech.beeeon.util.GraphViewHelper;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedMap;

public class CustomViewFragment extends TrackFragment {

	private SparseArray<List<Device>> mDevices = new SparseArray<List<Device>>();
	// private SparseArray<List<DeviceLog>> mLogs = new SparseArray<List<DeviceLog>>();
	private SparseArray<GraphView> mGraphs = new SparseArray<GraphView>();
	private SparseArray<LegendView> mLegends = new SparseArray<>();

	private String mGraphDateTimeFormat = "dd.MM. kk:mm";

	private LinearLayout mLayout;

	private static final String TAG = CustomViewFragment.class.getSimpleName();

	private Controller mController;

	public CustomViewFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mController = Controller.getInstance(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.graphofsensors, container, false);

		mLayout = (LinearLayout) view.findViewById(R.id.container);

		prepareDevices();
		loadData();

		return view;
	}

	private void addGraph(final Device device, final UnitsHelper unitsHelper, final TimeHelper timeHelper, final DateTimeFormatter fmt) {
		// Inflate layout
		LayoutInflater inflater = getLayoutInflater(null);
		View row = inflater.inflate(R.layout.custom_graph_item, mLayout, false);
		// Create and set graphView
		GraphView graphView = (GraphView) row.findViewById(R.id.graph);
		GraphViewHelper.prepareGraphView(graphView, getActivity(), device, fmt, unitsHelper); // empty heading
		LegendView legend = (LegendView) row.findViewById(R.id.legend);
		legend.setDrawBackground(true);
		legend.setIconRound(10f);

		// Set title
		TextView tv = (TextView) row.findViewById(R.id.graph_label);
		tv.setText(getString(device.getType().getStringResource()));

		mGraphs.put(device.getType().getTypeId(), graphView);
		mLegends.put(device.getType().getTypeId(), legend);

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

		// GraphViewSeriesStyle seriesStyleBlue = new
		// GraphViewSeriesStyle(mContext.getResources().getColor(R.color.beeeon_primary_cyan), 2);
		// GraphViewSeriesStyle seriesStyleGray = new
		// GraphViewSeriesStyle(getResources().getColor(R.color.light_gray),2);

		BaseSeries<DataPoint> graphSeries;
		if (device.getValue() instanceof BaseEnumValue) {
			graphSeries = new BarGraphSeries<>(new DataPoint[]{new DataPoint(0, 0),});
		} else {
			graphSeries = new LineGraphSeries<>(new DataPoint[]{new DataPoint(0, 0),});
			((LineGraphSeries)graphSeries).setThickness(4);
			((LineGraphSeries)graphSeries).setDrawCubicLine(true);
		}
		graphSeries.setTitle(device.getName());
		graphSeries.setColor(color);

		graphView.addSeries(graphSeries);

		LegendView legend = mLegends.get(device.getType().getTypeId());
		legend.initLegendSeries(graphView.getSeries());
		legend.setDrawBackground(false);
		legend.setSeriesPosition(LegendView.SeriesPosition.HORIZONTAL);
		legend.invalidate();


		SortedMap<Long, Float> values = log.getValues();
		int size = values.size();
		DataPoint[] data = new DataPoint[size];
		
		Log.d(TAG, String.format("Filling graph with %d values. Min: %.1f, Max: %.1f", size, log.getMinimum(), log.getMaximum()));

		int i = 0;
		for (Entry<Long, Float> entry : values.entrySet()) {
			Long dateMillis = entry.getKey();
			float value = Float.isNaN(entry.getValue()) ? log.getMinimum() : entry.getValue();

			data[i++] = new DataPoint(dateMillis, value);
			
			// This shouldn't happen, only when some other thread changes this values object - can it happen?
			if (i >= size)
				break;
		}
		
		Log.d(TAG, "Filling graph finished");

		graphSeries.resetData(data);
		graphView.getViewport().setXAxisBoundsManual(true);

	}

	private void prepareDevices() {
		Adapter adapter = mController.getActiveAdapter();
		if (adapter == null)
			return;

		// Prepare helpers
		final UnitsHelper unitsHelper = new UnitsHelper(mController.getUserSettings(), getActivity());
		final TimeHelper timeHelper = new TimeHelper(mController.getUserSettings());
		final DateTimeFormatter fmt = timeHelper.getFormatter(mGraphDateTimeFormat, adapter);

		// Prepare data
		Log.d(TAG, String.format("Preparing custom view for adapter %s", adapter.getId()));

		for (Facility facility : mController.getFacilitiesByAdapter(adapter.getId())) {
			Log.d(TAG, String.format("Preparing facility with %d devices", facility.getDevices().size()));

			for (Device device : facility.getDevices()) {
				Log.d(TAG, String.format("Preparing device %s (type %d)", device.getName(), device.getType().getTypeId()));

				List<Device> devices = mDevices.get(device.getType().getTypeId());
				if (devices == null) {
					devices = new ArrayList<Device>();
					mDevices.put(device.getType().getTypeId(), devices);
					addGraph(device, unitsHelper, timeHelper, fmt);
				}

				devices.add(device);
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

				// Load log data if needed
				mController.getDeviceLogsModel().reloadDeviceLog(pair);

				// Get loaded log data (TODO: this could be done in gui)
				result.put(device, mController.getDeviceLogsModel().getDeviceLog(pair));
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
			graphView.setLoading(false);
			graphView.animateY(2000);
		}

	}

}
