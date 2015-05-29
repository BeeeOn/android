package com.rehivetech.beeeon.gui.fragment;

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
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.device.ModuleLog.DataInterval;
import com.rehivetech.beeeon.household.device.ModuleLog.DataType;
import com.rehivetech.beeeon.household.device.values.BaseEnumValue;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.pair.LogDataPair;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.GetModulesLogsTask;
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

	private SparseArray<List<Module>> mModules = new SparseArray<List<Module>>();
	// private SparseArray<List<ModuleLog>> mLogs = new SparseArray<List<ModuleLog>>();
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

		prepareModules();
		loadData();

		return view;
	}

	private void addGraph(final Module module, final UnitsHelper unitsHelper, final TimeHelper timeHelper, final DateTimeFormatter fmt) {
		// Inflate layout
		LayoutInflater inflater = getLayoutInflater(null);
		View row = inflater.inflate(R.layout.custom_graph_item, mLayout, false);
		// Create and set graphView
		GraphView graphView = (GraphView) row.findViewById(R.id.graph);
		GraphViewHelper.prepareGraphView(graphView, mActivity, module, fmt, unitsHelper); // empty heading
		LegendView legend = (LegendView) row.findViewById(R.id.legend);
		legend.setDrawBackground(true);
		legend.setIconRound(10f);

		// Set title
		TextView tv = (TextView) row.findViewById(R.id.graph_label);
		tv.setText(getString(module.getType().getStringResource()));

		mGraphs.put(module.getType().getTypeId(), graphView);
		mLegends.put(module.getType().getTypeId(), legend);

		// Add whole item to global layout
		mLayout.addView(row);
	}

	private void fillGraph(ModuleLog log, Module module) {

		GraphView graphView = mGraphs.get(module.getType().getTypeId());
		if (graphView == null) {
			return;
		}

		Random random = new Random();
		int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));

		// for (ModuleLog log : logs) {

		// GraphViewSeriesStyle seriesStyleBlue = new
		// GraphViewSeriesStyle(mContext.getResources().getColor(R.color.beeeon_primary_cyan), 2);
		// GraphViewSeriesStyle seriesStyleGray = new
		// GraphViewSeriesStyle(getResources().getColor(R.color.light_gray),2);

		BaseSeries<DataPoint> graphSeries;
		if (module.getValue() instanceof BaseEnumValue) {
			graphSeries = new BarGraphSeries<>(new DataPoint[]{new DataPoint(0, 0),});
			graphView.setDrawPointer(false);
		} else {
			graphSeries = new LineGraphSeries<>(new DataPoint[]{new DataPoint(0, 0),});
			((LineGraphSeries) graphSeries).setThickness(4);
		}
		graphSeries.setTitle(module.getName());
		graphSeries.setColor(color);

		graphView.addSeries(graphSeries);

		LegendView legend = mLegends.get(module.getType().getTypeId());
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

	private void prepareModules() {
		Gate gate = mController.getActiveGate();
		if (gate == null)
			return;

		// Prepare helpers
		final UnitsHelper unitsHelper = new UnitsHelper(mController.getUserSettings(), mActivity);
		final TimeHelper timeHelper = new TimeHelper(mController.getUserSettings());
		final DateTimeFormatter fmt = timeHelper.getFormatter(mGraphDateTimeFormat, gate);

		// Prepare data
		Log.d(TAG, String.format("Preparing custom view for gate %s", gate.getId()));

		for (Device device : mController.getDevicesModel().getDevicesByGate(gate.getId())) {
			Log.d(TAG, String.format("Preparing mDevice with %d modules", device.getModules().size()));

			for (Module module : device.getModules()) {
				Log.d(TAG, String.format("Preparing module %s (type %d)", module.getName(), module.getType().getTypeId()));

				List<Module> modules = mModules.get(module.getType().getTypeId());
				if (modules == null) {
					modules = new ArrayList<Module>();
					mModules.put(module.getType().getTypeId(), modules);
					addGraph(module, unitsHelper, timeHelper, fmt);
				}

				modules.add(module);
			}
		}
	}

	private void loadData() {
		DateTime end = DateTime.now(DateTimeZone.UTC);
		DateTime start = end.minusDays(3);// end.minusWeeks(1);

		for (int i = 0; i < mModules.size(); i++) {
			// Prepare data for this graph
			final List<LogDataPair> pairs = new ArrayList<>();

			for (Module module : mModules.valueAt(i)) {
				LogDataPair pair = new LogDataPair( //
						module, // module
						new Interval(start, end), // interval from-to
						DataType.AVERAGE, // type
						DataInterval.TEN_MINUTES); // interval

				pairs.add(pair);
			}

			// If modules list is empty, just continue
			if (pairs.isEmpty()) {
				continue;
			}

			// Prepare and run the reload logs task
			GetModulesLogsTask getModulesLogsTask = new GetModulesLogsTask(mActivity);

			getModulesLogsTask.setListener(new CallbackTask.ICallbackTaskListener() {
				@Override
				public void onExecute(boolean success) {
					// Remember type of graph we're downloading data for
					int typeId = pairs.get(0).module.getType().getTypeId();

					for (LogDataPair pair : pairs) {
						ModuleLog log = mController.getModuleLogsModel().getModuleLog(pair);
						fillGraph(log, pair.module);
					}

					// Hide loading label for this graph
					GraphView graphView = mGraphs.get(typeId);
					graphView.setLoading(false);
					//graphView.animateY(2000);
				}
			});

			// Execute and remember task so it can be stopped automatically
			mActivity.callbackTaskManager.executeTask(getModulesLogsTask, pairs);
		}
	}

}
