package com.rehivetech.beeeon.gui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.device.ModuleLog.DataInterval;
import com.rehivetech.beeeon.household.device.ModuleLog.DataType;
import com.rehivetech.beeeon.household.device.values.BaseEnumValue;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.GetModulesLogsTask;
import com.rehivetech.beeeon.util.ChartHelper;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;

public class CustomViewFragment extends BaseApplicationFragment {

	private SparseArray<List<Module>> mModules = new SparseArray<>();
	// private SparseArray<List<ModuleLog>> mLogs = new SparseArray<List<ModuleLog>>();
	private SparseArray<BarLineChartBase> mCharts = new SparseArray<>();

	private String mGraphDateTimeFormat = "dd.MM. kk:mm";

	private LinearLayout mLayout;

	private static final String TAG = CustomViewFragment.class.getSimpleName();

	public CustomViewFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.graphofsensors, container, false);

		mLayout = (LinearLayout) view.findViewById(R.id.container);

		prepareModules();
		loadData();

		return view;
	}

	private void addChart(final Module module) {
		// Inflate Layout
		LayoutInflater inflater = getLayoutInflater(null);
		View row = inflater.inflate(R.layout.custom_graph_item, mLayout, false);
		// Create and set chart
		BarLineChartBase chart;
		if (module.getValue() instanceof BaseEnumValue) {
			chart = new BarChart(mActivity);
		} else {
			chart = new LineChart(mActivity);
		}
		LinearLayout chartLayout = (LinearLayout) row.findViewById(R.id.graph_layout);
		chartLayout.setVisibility(View.INVISIBLE);
		chart.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400));
		chartLayout.addView(chart);
		ChartHelper.prepareChart(chart, mActivity, module.getValue(), chartLayout, Controller.getInstance(mActivity));
		chartLayout.setVisibility(View.VISIBLE);


		// Set title
		TextView tv = (TextView) row.findViewById(R.id.graph_label);
		tv.setText(getString(module.getType().getStringResource()));

		mCharts.put(module.getType().getTypeId(), chart);

		// Add whole item to global mLayout
		mLayout.addView(row);
	}

	@SuppressWarnings("unchecked")
	private void fillGraph(ModuleLog log, Module module) {
		Controller controller = Controller.getInstance(mActivity);
		final UnitsHelper unitsHelper = new UnitsHelper(controller.getUserSettings(), mActivity);
		final TimeHelper timeHelper = new TimeHelper(controller.getUserSettings());
		final DateTimeFormatter fmt = timeHelper.getFormatter(mGraphDateTimeFormat, controller.getActiveGate());

		boolean isBarChart = (module.getValue() instanceof BaseEnumValue);

		//get chart
		BarLineChartBase chart = mCharts.get(module.getType().getTypeId());
		if (chart == null) {
			return;
		}

		//set random color
		Random random = new Random();
		int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));


		String unit = unitsHelper.getStringUnit(module.getValue());
		String name = mActivity.getString(module.getTypeStringResource());

		List dataSetList;
		ArrayList<String> xVals;
		if (chart.getData() != null) {
			ChartData data = chart.getData();
			xVals = (ArrayList<String>) data.getXVals();

			if (isBarChart) {
				dataSetList = (List<BarDataSet>)(data.getDataSets());
			} else {
				dataSetList = (List<LineDataSet>)(chart.getData().getDataSets());
			}
		} else {
			xVals = new ArrayList<>();
			if (isBarChart) {
				dataSetList = new ArrayList<BarDataSet>();
			} else {
				dataSetList = new ArrayList<LineDataSet>();
			}
		}


		List<BarEntry> barEntries = null;
		List<Entry> lineEntries = null;
		DataSet dataSet;
		if (isBarChart) {
			barEntries = new ArrayList<>();
			dataSet = new BarDataSet(barEntries, name);
		} else {
			lineEntries = new ArrayList<>();
			dataSet = new LineDataSet(lineEntries, String.format("%s [%s]", name, unit));
			((LineDataSet)dataSet).setDrawCircles(false);
			((LineDataSet)dataSet).setLineWidth(2f);
		}
		dataSet.setColor(color);
		dataSet.setValueFormatter(ChartHelper.getValueFormatterInstance(module.getValue(),mActivity,controller));
		dataSetList.add(dataSet);

		SortedMap<Long, Float> values = log.getValues();
		int size = values.size();

		Log.d(TAG, String.format("Filling graph with %d values. Min: %.1f, Max: %.1f", size, log.getMinimum(), log.getMaximum()));
		boolean setXvals = xVals.isEmpty();
		int i = 0;
		for (Map.Entry<Long, Float> entry : values.entrySet()) {
			Long dateMillis = entry.getKey();
			float value = Float.isNaN(entry.getValue()) ? log.getMinimum() : entry.getValue();
			if (setXvals) {
				xVals.add(fmt.print(dateMillis));
			}
			if (isBarChart) {
				barEntries.add(new BarEntry(value, i++));
			} else {
				lineEntries.add(new Entry(value, i++));
			}
			// This shouldn't happen, only when some other thread changes this values object - can it happen?
			if (i >= size)
				break;
		}
		dataSet.notifyDataSetChanged();

		if (isBarChart) {
			BarData  barData = new BarData(xVals, dataSetList);
			chart.setData(barData);
		} else {
			LineData lineData = new LineData(xVals, dataSetList);
			chart.setData(lineData);
		}
		chart.invalidate();

		Log.d(TAG, "Filling graph finished");
	}

	private void prepareModules() {
		Controller controller = Controller.getInstance(getActivity());
		Gate gate = controller.getActiveGate();
		if (gate == null)
			return;


		// Prepare data
		Log.d(TAG, String.format("Preparing custom view for gate %s", gate.getId()));

		for (Device device : controller.getDevicesModel().getDevicesByGate(gate.getId())) {
			Log.d(TAG, String.format("Preparing mDevice with %d modules", device.getModules().size()));

			for (Module module : device.getModules()) {
				Log.d(TAG, String.format("Preparing module %s (type %d)", module.getName(), module.getType().getTypeId()));

				List<Module> modules = mModules.get(module.getType().getTypeId());
				if (modules == null) {
					modules = new ArrayList<>();
					mModules.put(module.getType().getTypeId(), modules);
					addChart(module);
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
			final List<ModuleLog.DataPair> pairs = new ArrayList<>();

			for (Module module : mModules.valueAt(i)) {
				ModuleLog.DataPair pair = new ModuleLog.DataPair( //
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

					for (ModuleLog.DataPair pair : pairs) {
						ModuleLog log = Controller.getInstance(getActivity()).getModuleLogsModel().getModuleLog(pair);
						fillGraph(log, pair.module);
					}

					// start chart animation
					BarLineChartBase chart = mCharts.get(typeId);
					chart.animateXY(2000, 2000);
				}
			});

			// Execute and remember task so it can be stopped automatically
			mActivity.callbackTaskManager.executeTask(getModulesLogsTask, pairs);
		}
	}

}
