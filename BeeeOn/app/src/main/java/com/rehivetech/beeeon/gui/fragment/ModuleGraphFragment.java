package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.ModuleGraphActivity;
import com.rehivetech.beeeon.gui.view.VerticalChartLegend;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.GetModuleLogTask;
import com.rehivetech.beeeon.util.ChartHelper;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * @author martin on 18.8.2015.
 */
public class ModuleGraphFragment extends BaseApplicationFragment {
	private static final String TAG = ModuleGraphFragment.class.getSimpleName();

	private static final String KEY_GATE_ID = "gate_id";
	private static final String KEY_DEVICE_ID = "device_id";
	private static final String KEY_MODULE_ID = "module_id";

	private static final String GRAPH_DATE_TIME_FORMAT = "dd.MM. kk:mm";
	private static final String LOG_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private String mGateId;
	private String mDeviceId;
	private String mModuleId;

	private ModuleGraphActivity mActivity;

	private UnitsHelper mUnitsHelper;
	private TimeHelper mTimeHelper;

	private View mView;
	private CombinedChart mChart;
	private DataSet mDataSet;
	private VerticalChartLegend mLegend;
	private Button mShowLegendButton;
	private StringBuffer mYlabels = new StringBuffer();

	public static ModuleGraphFragment newInstance(String gateId, String deviceId, String moduleId) {
		Bundle args = new Bundle();
		args.putString(KEY_GATE_ID, gateId);
		args.putString(KEY_DEVICE_ID, deviceId);
		args.putString(KEY_MODULE_ID, moduleId);

		ModuleGraphFragment fragment = new ModuleGraphFragment();
		fragment.setArguments(args);
		return fragment;
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mActivity = (ModuleGraphActivity) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		mGateId = args.getString(KEY_GATE_ID);
		mDeviceId = args.getString(KEY_DEVICE_ID);
		mModuleId = args.getString(KEY_MODULE_ID);

		// UserSettings can be null when user is not logged in!
		SharedPreferences prefs = Controller.getInstance(mActivity).getUserSettings();
		mUnitsHelper = (prefs == null) ? null : new UnitsHelper(prefs, mActivity);
		mTimeHelper = (prefs == null) ? null : new TimeHelper(prefs);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_module_graph, container, false);
		mChart = (CombinedChart) mView.findViewById(R.id.module_graph_chart);
		mLegend = (VerticalChartLegend) mView.findViewById(R.id.module_graph_legend);

		mShowLegendButton = (Button) mView.findViewById(R.id.module_graph_show_legend_btn);
		mShowLegendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SimpleDialogFragment.createBuilder(mActivity, getFragmentManager())
						.setTitle(getString(R.string.chart_helper_chart_y_axis))
						.setMessage(mYlabels.toString())
						.setNeutralButtonText("close")
						.show();
			}
		});
		return mView;
	}

	@Override
	public void onResume() {
		super.onResume();
		addGraphView();
		doLoadGraphData();
	}

	private void addGraphView() {
		Controller controller = Controller.getInstance(mActivity);
		Module module = controller.getDevicesModel().getDevice(mGateId, mDeviceId).getModuleById(mModuleId);
		BaseValue baseValue = module.getValue();
		boolean barchart = baseValue instanceof EnumValue;

		String unit = mUnitsHelper.getStringUnit(baseValue);
		String deviceName = module.getDevice().getName(mActivity);
		String moduleName = module.getName(mActivity);

		//set chart
		ChartHelper.prepareChart(mChart, mActivity, baseValue, mYlabels, controller);
//		mChart.setFillFormatter(new CustomFillFormatter());

		if (barchart) {
			mDataSet = new BarDataSet(new ArrayList<BarEntry>(), String.format("%s - %s", deviceName, moduleName));
		} else {
			mDataSet = new LineDataSet(new ArrayList<com.github.mikephil.charting.data.Entry>(), String.format("%s - %s [%s]", deviceName, moduleName, unit));
			mShowLegendButton.setVisibility(View.GONE);
		}
		//set dataset style
		ChartHelper.prepareDataSet(mDataSet, barchart, true, getResources().getColor(R.color.beeeon_primary_medium));
	}

	@SuppressWarnings("unchecked")
	public void fillGraph(ModuleLog log, Module module) {
		boolean barGraph = (module.getValue() instanceof EnumValue);
		Gate gate = Controller.getInstance(mActivity).getGatesModel().getGate(mGateId);
		if (mChart == null) {
			return;
		}

		SortedMap<Long, Float> values = log.getValues();
		int size = values.size();
		ArrayList<String> xVals = new ArrayList<>();

		List<BarEntry> barEntries = null;
		List<Entry> lineEntries = null;
		if (barGraph) {
			barEntries = ((BarDataSet) mDataSet).getYVals();
		} else {
			lineEntries = ((LineDataSet) mDataSet).getYVals();
		}
		DateTimeFormatter fmt = mTimeHelper.getFormatter(GRAPH_DATE_TIME_FORMAT, gate);

		Log.d(TAG, String.format("Filling graph with %d values. Min: %.1f, Max: %.1f", size, log.getMinimum(), log.getMaximum()));

		int i = 0;
		for (Map.Entry<Long, Float> entry : values.entrySet()) {
			Long dateMillis = entry.getKey();
			float value = Float.isNaN(entry.getValue()) ? log.getMinimum() : entry.getValue();
			xVals.add(fmt.print(dateMillis));
			if (barGraph) {
				barEntries.add(new BarEntry(value, i++));
			} else {
				lineEntries.add(new Entry(value, i++));
			}
			// This shouldn't happen, only when some other thread changes this values object - can it happen?
			if (i >= size)
				break;
		}

		mDataSet.notifyDataSetChanged();
		CombinedData data = new CombinedData(xVals);
		if (barGraph) {
			BarData barData = new BarData(xVals, (BarDataSet) mDataSet);
			data.setData(barData);
		} else {
			LineData lineData = new LineData(xVals, (LineDataSet) mDataSet);
			data.setData(lineData);
		}
		mChart.setData(data);
		mChart.invalidate();
		Log.d(TAG, "Filling graph finished");
		mChart.animateY(2000);

		if (mLegend != null) {
			mLegend.setChartDatasets(mChart.getData().getDataSets());
			mLegend.invalidate();
			mLegend.setPadding(0, 0, 0, getResources().getDimensionPixelOffset(R.dimen.customview_text_padding));
		}
	}

	protected void doLoadGraphData() {
		final Module module = Controller.getInstance(mActivity).getDevicesModel().getDevice(mGateId, mDeviceId).getModuleById(mModuleId);
		if (module == null) {
			Log.e(TAG, "Can't load module for loading graph data");
			return;
		}

		DateTime end = DateTime.now(DateTimeZone.UTC);
		DateTime start = end.minusWeeks(1);

		DateTimeFormatter fmt = DateTimeFormat.forPattern(LOG_DATE_TIME_FORMAT).withZoneUTC();

		GetModuleLogTask getModuleLogTask = new GetModuleLogTask(mActivity);
		final ModuleLog.DataPair pair = new ModuleLog.DataPair( //
				module, // module
				new Interval(start, end), // interval from-to
				ModuleLog.DataType.AVERAGE, // type
				(module.getValue() instanceof EnumValue) ? ModuleLog.DataInterval.RAW : ModuleLog.DataInterval.TEN_MINUTES); // interval

		getModuleLogTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				fillGraph(Controller.getInstance(mActivity).getModuleLogsModel().getModuleLog(pair), module);
			}
		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(getModuleLogTask, pair);
	}
}
