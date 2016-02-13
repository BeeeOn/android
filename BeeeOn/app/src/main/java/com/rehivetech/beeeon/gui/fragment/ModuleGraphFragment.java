package com.rehivetech.beeeon.gui.fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.ModuleGraphActivity;
import com.rehivetech.beeeon.gui.view.ModuleGraphMarkerView;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.util.ChartHelper;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
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

	private int mRefreshInterval = 1; // in seconds

	private ModuleGraphActivity mActivity;

	private RelativeLayout mRootLayout;

	private UnitsHelper mUnitsHelper;
	private TimeHelper mTimeHelper;
	private DateTimeFormatter mFormatter;

	private BarLineChartBase mChart;
	private DataSet mDataSetMin;
	private DataSet mDataSetAvg;
	private DataSet mDataSetMax;

	private StringBuffer mYlabels = new StringBuffer();

	private ChartHelper.ChartLoadListener mChartLoadCallback = new ChartHelper.ChartLoadListener() {

		@Override
		public void onChartLoaded(DataSet dataSet, List<String> xValues) {

			if (dataSet instanceof BarDataSet) {
				BarData data = ((BarChart) mChart).getBarData() == null ? new BarData(xValues) : ((BarChart) mChart).getBarData();

				if (dataSet.getYVals().size() < 2 && ((BarChart) mChart).getBarData() == null) {
					mChart.setNoDataText(getString(R.string.chart_helper_chart_no_data));
					mChart.invalidate();

					mActivity.setMinValue("");
					mActivity.setMaxValue("");
					return;
				}

				data.addDataSet((BarDataSet) dataSet);
				((BarChart) mChart).setData(data);

			} else {
				LineData data = ((LineChart) mChart).getLineData() == null ? new LineData(xValues) : ((LineChart) mChart).getLineData();

				if (dataSet.getYVals().size() < 2 && ((LineChart) mChart).getLineData() == null) {
					mChart.setNoDataText(getString(R.string.chart_helper_chart_no_data));
					mChart.invalidate();

					mActivity.setMinValue("");
					mActivity.setMaxValue("");
					return;
				}

				data.addDataSet((LineDataSet) dataSet);
				((LineChart) mChart).setData(data);

				updateMinMaxTexts();
			}

			ChartHelper.setDataSetCircles(dataSet, mChart.getViewPortHandler(), mChart.getData().getYValCount(), getResources().getInteger(R.integer.graph_number_circles));
			ChartHelper.setDrawDataSetValues(dataSet, mChart.getViewPortHandler(), mChart.getXValCount(), getResources().getInteger(R.integer.graph_values_count));

			mChart.invalidate();

			mActivity.setRequestRedrawActiveFragmentCalled(false);

			Log.d(TAG, String.format("dataSet added: %s", dataSet.getLabel()));
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		mGateId = args.getString(KEY_GATE_ID);
		mDeviceId = args.getString(KEY_DEVICE_ID);
		mModuleId = args.getString(KEY_MODULE_ID);
		//noinspection ResourceType
		mRange = args.getInt(KEY_DATA_RANGE);


		// UserSettings can be null when user is not logged in!
		Controller controller = Controller.getInstance(mActivity);
		SharedPreferences prefs = controller.getUserSettings();
		mUnitsHelper = (prefs == null) ? null : new UnitsHelper(prefs, mActivity);
		mTimeHelper = (prefs == null) ? null : new TimeHelper(prefs);

		mFormatter = mTimeHelper != null
				? mTimeHelper.getFormatter(ChartHelper.GRAPH_DATE_TIME_FORMAT, controller.getGatesModel().getGate(mGateId))
				: DateTimeFormat.forPattern(ChartHelper.GRAPH_DATE_TIME_FORMAT).withZone(DateTimeZone.getDefault());
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_module_graph, container, false);

		mRootLayout = (RelativeLayout) view.findViewById(R.id.module_graph_layout);



		mActivity.setShowLegendButtonOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SimpleDialogFragment.createBuilder(mActivity, getFragmentManager())
						.setTitle(getString(R.string.chart_helper_chart_y_axis))
						.setMessage(mYlabels.toString())
						.setNeutralButtonText("close")
						.show();
			}
		});
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
		String unit = mUnitsHelper.getStringUnit(baseValue);

		mYlabels = new StringBuffer();

		if (barchart) {
			mChart = new BarChart(mActivity);
			ChartHelper.prepareChart(mChart, mActivity, baseValue, mYlabels, null, false, true);
		} else {
			mChart = new LineChart(mActivity);
			ModuleGraphMarkerView markerView = new ModuleGraphMarkerView(mActivity, R.layout.util_chart_module_markerview, (LineChart) mChart, unit);
			ChartHelper.prepareChart(mChart, mActivity, baseValue, mYlabels, markerView, false, true);
		}

		mChart.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mRootLayout.addView(mChart);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mChart.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}

		// prepare axis bottom
		ChartHelper.prepareXAxis(mActivity, mChart.getXAxis(), null, XAxis.XAxisPosition.BOTTOM, false);
		//prepare axis left
		ChartHelper.prepareYAxis(mActivity, module.getValue(), mChart.getAxisLeft(), null, YAxis.YAxisLabelPosition.OUTSIDE_CHART, true, false, 5);
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
		ChartHelper.prepareDataSet(mActivity, mDataSetAvg, barchart, true, Utils.getGraphColor(mActivity, 0), ContextCompat.getColor(mActivity, R.color.beeeon_accent), true);
		ChartHelper.prepareDataSet(mActivity, mDataSetMin, barchart, true, Utils.getGraphColor(mActivity, 1), ContextCompat.getColor(mActivity, R.color.beeeon_accent), true);
		ChartHelper.prepareDataSet(mActivity, mDataSetMax, barchart, true, Utils.getGraphColor(mActivity, 2), ContextCompat.getColor(mActivity, R.color.beeeon_accent), true);

	}

	@Override
	public void onChartSettingChanged(boolean drawMin, boolean drawAvg, boolean drawMax, ModuleLog.DataInterval dataGranularity) {
		mChart.clear();
		mChart.setNoDataText(getString(R.string.chart_helper_chart_loading));

		mDataSetMin.clear();
		mDataSetAvg.clear();
		mDataSetMax.clear();

		if (drawMax) {
			ChartHelper.loadChartData(mActivity, Controller.getInstance(mActivity), mDataSetMax, mGateId, mDeviceId, mModuleId, mRange,
					ModuleLog.DataType.MAXIMUM, dataGranularity, mChartLoadCallback, mFormatter);
		}
		if (drawAvg) {
			ChartHelper.loadChartData(mActivity, Controller.getInstance(mActivity), mDataSetAvg, mGateId, mDeviceId, mModuleId, mRange,
					ModuleLog.DataType.AVERAGE, dataGranularity, mChartLoadCallback, mFormatter);
		}

		if (drawMin) {
			ChartHelper.loadChartData(mActivity, Controller.getInstance(mActivity), mDataSetMin, mGateId, mDeviceId, mModuleId, mRange,
					ModuleLog.DataType.MINIMUM, dataGranularity, mChartLoadCallback, mFormatter);
		}
	}

	private void updateMinMaxTexts() {
		float minValue;
		float maxValue;

		if (mDataSetMin.getYVals().size() > 0) {
			minValue = mDataSetMin.getYMin();
		} else if (mDataSetAvg.getYVals().size() > 0) {
			minValue = mDataSetAvg.getYMin();
		} else {
			minValue = mDataSetMax.getYMin();
		}

		if (mDataSetMax.getYVals().size() > 0) {
			maxValue = mDataSetMax.getYMax();
		} else if (mDataSetAvg.getYVals().size() > 0) {
			maxValue = mDataSetAvg.getYMax();
		} else {
			maxValue = mDataSetMin.getYMax();
		}

		mActivity.setMinValue(String.format("%.2f", minValue));
		mActivity.setMaxValue(String.format("%.2f", maxValue));
	}
}
