package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.ModuleGraphActivity;
import com.rehivetech.beeeon.gui.view.ChartMarkerView;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.util.ChartHelper;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;

import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author martin on 18.8.2015.
 */
public class ModuleGraphFragment extends BaseApplicationFragment implements ModuleGraphActivity.ChartSettingListener{
	private static final String TAG = ModuleGraphFragment.class.getSimpleName();

	private static final String KEY_GATE_ID = "gate_id";
	private static final String KEY_DEVICE_ID = "device_id";
	private static final String KEY_MODULE_ID = "module_id";
	private static final String KEY_DATA_RANGE = "data_range";

	private String mGateId;
	private String mDeviceId;
	private String mModuleId;
	private @ChartHelper.DataRange int mRange;

	private ModuleGraphActivity mActivity;

	private UnitsHelper mUnitsHelper;
	private TimeHelper mTimeHelper;

	private CombinedChart mChart;
	private DataSet mDataSetMin;
	private DataSet mDataSetAvg;
	private DataSet mDataSetMax;

	//	private Button mShowLegendButton;
	private StringBuffer mYlabels = new StringBuffer();

	private ChartHelper.ChartLoadListener mChartLoadCallback = new ChartHelper.ChartLoadListener() {

		@Override
		public void onChartLoaded(DataSet dataSet, List<String> xValues) {
			CombinedData combinedData;

			List<String> xvalsTmp = new ArrayList<>(xValues);

			if (mChart.getData() != null) {
				combinedData = mChart.getData();
				combinedData.getXVals().clear();
				combinedData.getXVals().addAll(xvalsTmp);

			} else {
				combinedData = new CombinedData(xvalsTmp);
			}

			if (dataSet instanceof BarDataSet) {
				BarData data = (combinedData.getBarData() == null) ? new BarData(xvalsTmp) : combinedData.getBarData();
				data.addDataSet((BarDataSet) dataSet);
				combinedData.setData(data);

			} else {
				LineData data = (combinedData.getLineData() == null) ? new LineData(xvalsTmp) : combinedData.getLineData();
				data.addDataSet((LineDataSet) dataSet);
				combinedData.setData(data);
			}

			mChart.setData(combinedData);
			mChart.invalidate();

			mActivity.setMinValue(String.format("%.2f", dataSet.getYMin()));
			mActivity.setMaxValue(String.format("%.2f", dataSet.getYMax()));

			Log.d(TAG, String.format("dataSet added: %s",dataSet.getLabel()));
		}
	};

	public static ModuleGraphFragment newInstance(String gateId, String deviceId, String moduleId, @ChartHelper.DataRange int range) {
		Bundle args = new Bundle();
		args.putString(KEY_GATE_ID, gateId);
		args.putString(KEY_DEVICE_ID, deviceId);
		args.putString(KEY_MODULE_ID, moduleId);
		args.putInt(KEY_DATA_RANGE, range);

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
		//noinspection ResourceType
		mRange = args.getInt(KEY_DATA_RANGE);


		// UserSettings can be null when user is not logged in!
		SharedPreferences prefs = Controller.getInstance(mActivity).getUserSettings();
		mUnitsHelper = (prefs == null) ? null : new UnitsHelper(prefs, mActivity);
		mTimeHelper = (prefs == null) ? null : new TimeHelper(prefs);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_module_graph, container, false);
		mChart = (CombinedChart) view.findViewById(R.id.module_graph_chart);



//		mShowLegendButton = (Button) view.findViewById(R.id.module_graph_show_legend_btn);
//		mShowLegendButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				SimpleDialogFragment.createBuilder(mActivity, getFragmentManager())
//						.setTitle(getString(R.string.chart_helper_chart_y_axis))
//						.setMessage(mYlabels.toString())
//						.setNeutralButtonText("close")
//						.show();
//			}
//		});
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		Device device = Controller.getInstance(mActivity).getDevicesModel().getDevice(mGateId, mDeviceId);
		if (device == null) {
			Log.e(TAG, String.format("Device #%s does not exists", mDeviceId));
			mActivity.finish();
		}
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		addGraphView();
	}

	private void addGraphView() {
		Controller controller = Controller.getInstance(mActivity);
		Module module = controller.getDevicesModel().getDevice(mGateId, mDeviceId).getModuleById(mModuleId);
		BaseValue baseValue = module.getValue();
		boolean barchart = baseValue instanceof EnumValue;

		String deviceName = module.getDevice().getName(mActivity);
		String moduleName = module.getName(mActivity);

		//set chart

		DateTimeFormatter formatter = mTimeHelper.getFormatter(ChartHelper.GRAPH_DATE_TIME_FORMAT, controller.getGatesModel().getGate(mGateId));
		MarkerView markerView = new ChartMarkerView(mActivity, R.layout.util_chart_markerview, mChart);

		ChartHelper.prepareChart(mChart, mActivity, baseValue, mYlabels, markerView, false);

		// prepare axis bottom
		ChartHelper.prepareXAxis(mActivity, mChart.getXAxis(), formatter, null, XAxis.XAxisPosition.BOTTOM, false);
		//prepare axis left
		ChartHelper.prepareYAxis(mActivity, module.getValue(), mChart.getAxisLeft(), null, YAxis.YAxisLabelPosition.OUTSIDE_CHART, true, false);
		//disable right axis
		mChart.getAxisRight().setEnabled(false);

		mChart.setDrawBorders(false);

		String dataSetMinName = String.format("%s - %s min", deviceName, moduleName);
		String dataSetAvgName = String.format("%s - %s avg", deviceName, moduleName);
		String dataSetMaxName = String.format("%s - %s max", deviceName, moduleName);

		if (barchart) {
			mDataSetMin = new BarDataSet(new ArrayList<BarEntry>(), dataSetMinName);
			mDataSetAvg = new BarDataSet(new ArrayList<BarEntry>(), dataSetAvgName);
			mDataSetMax = new BarDataSet(new ArrayList<BarEntry>(), dataSetMaxName);
		} else {
			mDataSetMin = new LineDataSet(new ArrayList<com.github.mikephil.charting.data.Entry>(), dataSetMinName);
			mDataSetAvg = new LineDataSet(new ArrayList<com.github.mikephil.charting.data.Entry>(), dataSetAvgName);
			mDataSetMax = new LineDataSet(new ArrayList<com.github.mikephil.charting.data.Entry>(), dataSetMaxName);
//			mShowLegendButton.setVisibility(View.GONE);
		}
		//set dataset style
		ChartHelper.prepareDataSet(mActivity, mDataSetAvg, barchart, true, Utils.getGraphColor(mActivity, 0), ContextCompat.getColor(mActivity, R.color.beeeon_accent));
		ChartHelper.prepareDataSet(mActivity, mDataSetMin, barchart, true, Utils.getGraphColor(mActivity, 1), ContextCompat.getColor(mActivity, R.color.beeeon_accent));
		ChartHelper.prepareDataSet(mActivity, mDataSetMax, barchart, true, Utils.getGraphColor(mActivity, 2), ContextCompat.getColor(mActivity, R.color.beeeon_accent));

	}

	@Override
	public void onChartSettingChanged(boolean drawMin, boolean drawAvg, boolean drawMax, ModuleLog.DataInterval dataGranularity) {
		mChart.clear();

		if (drawMax) {
			ChartHelper.loadChartData(mActivity, Controller.getInstance(mActivity), mDataSetMax, mGateId, mDeviceId, mModuleId, mRange,
					ModuleLog.DataType.MAXIMUM, dataGranularity, mChartLoadCallback);
		}
		if (drawAvg) {
			ChartHelper.loadChartData(mActivity, Controller.getInstance(mActivity), mDataSetAvg, mGateId, mDeviceId, mModuleId, mRange,
					ModuleLog.DataType.AVERAGE, dataGranularity, mChartLoadCallback);
		}

		if (drawMin) {
			ChartHelper.loadChartData(mActivity, Controller.getInstance(mActivity), mDataSetMin, mGateId, mDeviceId, mModuleId, mRange,
					ModuleLog.DataType.MINIMUM, dataGranularity, mChartLoadCallback);
		}
	}
}
