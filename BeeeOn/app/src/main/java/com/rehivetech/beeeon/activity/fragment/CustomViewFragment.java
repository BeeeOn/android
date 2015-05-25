package com.rehivetech.beeeon.activity.fragment;

import android.graphics.Color;
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
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.point.DataPoint;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.asynctask.GetDevicesLogsTask;
import com.rehivetech.beeeon.base.BaseApplicationFragment;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceLog;
import com.rehivetech.beeeon.household.device.DeviceLog.DataInterval;
import com.rehivetech.beeeon.household.device.DeviceLog.DataType;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.device.values.BaseEnumValue;
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
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedMap;

public class CustomViewFragment extends BaseApplicationFragment {

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

		mController = Controller.getInstance(mActivity);
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
		GraphViewHelper.prepareGraphView(graphView, mActivity, device, fmt, unitsHelper); // empty heading
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
			graphView.setDrawPointer(false);
		} else {
			graphSeries = new LineGraphSeries<>(new DataPoint[]{new DataPoint(0, 0),});
			((LineGraphSeries)graphSeries).setThickness(4);
		}
		graphSeries.setTitle(device.getName());
		graphSeries.setColor(color);

		graphView.addSeries(graphSeries);

		LegendView legend = mLegends.get(device.getType().getTypeId());
		legend.initLegendSeries(graphView.getSeries());
		legend.setDrawBackground(false);
		legend.setSeriesPosition(LegendView.SeriesPosition.VERTICAL);
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
		final UnitsHelper unitsHelper = new UnitsHelper(mController.getUserSettings(), mActivity);
		final TimeHelper timeHelper = new TimeHelper(mController.getUserSettings());
		final DateTimeFormatter fmt = timeHelper.getFormatter(mGraphDateTimeFormat, adapter);

		// Prepare data
		Log.d(TAG, String.format("Preparing custom view for adapter %s", adapter.getId()));

		for (Facility facility : mController.getFacilitiesModel().getFacilitiesByAdapter(adapter.getId())) {
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
		DateTime end = DateTime.now(DateTimeZone.UTC);
		DateTime start = end.minusDays(3);// end.minusWeeks(1);

		for (int i = 0; i < mDevices.size(); i++) {
			// Prepare data for this graph
			final List<LogDataPair> pairs = new ArrayList<>();

			for (Device device : mDevices.valueAt(i)) {
				LogDataPair pair = new LogDataPair( //
						device, // device
						new Interval(start, end), // interval from-to
						DataType.AVERAGE, // type
						DataInterval.TEN_MINUTES); // interval

				pairs.add(pair);
			}

			// If devices list is empty, just continue
			if (pairs.isEmpty()) {
				continue;
			}

			// Prepare and run the reload logs task
			GetDevicesLogsTask getDevicesLogsTask = new GetDevicesLogsTask(mActivity);

			getDevicesLogsTask.setListener(new CallbackTask.CallbackTaskListener() {
				@Override
				public void onExecute(boolean success) {
					// Remember type of graph we're downloading data for
					int typeId = pairs.get(0).device.getType().getTypeId();

					for (LogDataPair pair : pairs) {
						DeviceLog log = mController.getDeviceLogsModel().getDeviceLog(pair);
						fillGraph(log, pair.device);
					}

					// Hide loading label for this graph
					GraphView graphView = mGraphs.get(typeId);
					graphView.setLoading(false);
					//graphView.animateY(2000);
				}
			});

			// Execute and remember task so it can be stopped automatically
			mActivity.callbackTaskManager.executeTask(getDevicesLogsTask, pairs);
		}
	}

}
