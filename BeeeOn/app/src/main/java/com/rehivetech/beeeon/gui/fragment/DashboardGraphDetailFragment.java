package com.rehivetech.beeeon.gui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.GraphItem;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
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
 * Created by martin on 20.3.16.
 */
public class DashboardGraphDetailFragment extends BaseDashboardDetailFragment {

	private GraphItem mGraphItem;

	private TextView mUnitLeft;
	private TextView mUnitRight;
	private LineChart mChart;
	private LineDataSet mLineDataSetLeft;
	private LineDataSet mLineDataSetRight;
	private DateTimeFormatter mFormatter;

	private ChartHelper.ChartLoadListener mChartLoadListener = new ChartHelper.ChartLoadListener() {
		@Override
		public void onChartLoaded(DataSet dataset, List<String> xValues) {
			LineData data = mChart.getLineData() == null ? new LineData(xValues) : mChart.getLineData();

			if (dataset.getYVals().size() < 2 && mChart.getLineData() == null) {
				mChart.setNoDataText(getString(R.string.chart_helper_chart_no_data));
				mChart.invalidate();
				return;
			}
			data.addDataSet((ILineDataSet) dataset);
			mChart.setData(data);
			mChart.invalidate();

			if (dataset.getAxisDependency() == YAxis.AxisDependency.LEFT) {
				mUnitLeft.setVisibility(View.VISIBLE);
			} else {
				mUnitRight.setVisibility(View.VISIBLE);
				mChart.getAxisRight().setEnabled(true);
			}
		}
	};

	public static DashboardGraphDetailFragment newInstance(GraphItem item) {
		Bundle args = new Bundle();
		args.putParcelable(ARG_DASHBOARD_ITEM, item);
		DashboardGraphDetailFragment fragment = new DashboardGraphDetailFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			mGraphItem = args.getParcelable(ARG_DASHBOARD_ITEM);
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_dashboard_detail_graph, container, false);
	}


	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mChart = (LineChart) view.findViewById(R.id.fragment_dashboard_detail_graph);
		mUnitLeft = (TextView) view.findViewById(R.id.fragment_dashboard_detail_left_axis_unit);
		mUnitRight = (TextView) view.findViewById(R.id.fragment_dashboard_detail_right_axis_unit);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Controller controller = Controller.getInstance(mActivity);
		Module leftModule = controller.getDevicesModel().getModule(mGraphItem.getGateId(), mGraphItem.getAbsoluteModuleIds().get(0));
		Module rightModule = null;

		if (mGraphItem.getAbsoluteModuleIds().size() > 1) {
			rightModule = controller.getDevicesModel().getModule(mGraphItem.getGateId(), mGraphItem.getAbsoluteModuleIds().get(1));
		}

		if (leftModule == null) {
			mActivity.finish();
			return;
		}

		mActivity.setToolbarTitle(mGraphItem.getName());
		mActivity.setupRefreshIcon(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadCharData();
			}
		});

		SharedPreferences prefs = controller.getUserSettings();
		TimeHelper timeHelper = Utils.getTimeHelper(prefs);
		mFormatter = timeHelper != null
				? timeHelper.getFormatter(ChartHelper.GRAPH_DATE_TIME_FORMAT, controller.getGatesModel().getGate(mGraphItem.getGateId()))
				: DateTimeFormat.forPattern(ChartHelper.GRAPH_DATE_TIME_FORMAT).withZone(DateTimeZone.getDefault());

		UnitsHelper unitsHelper = Utils.getUnitsHelper(prefs, mActivity);

		if (unitsHelper != null) {
			mUnitLeft.setText(unitsHelper.getStringUnit(leftModule.getValue()));

			if (rightModule != null) {
				mUnitRight.setText(unitsHelper.getStringUnit(rightModule.getValue()));
			}
		}

		prepareChart(leftModule, rightModule);
	}

	@Override
	public void onResume() {
		super.onResume();
		loadCharData();
	}

	private void prepareChart(Module leftModule, @Nullable Module rightModule) {
		mLineDataSetLeft = new LineDataSet(new ArrayList<Entry>(), leftModule.getName(mActivity, true));

		ChartHelper.prepareChart(mChart, mActivity, leftModule.getValue(), null, null, false, true);
		mChart.getLegend().setEnabled(true);
		ChartHelper.prepareLegend(mActivity, mChart.getLegend());

		// prepare axis bottom
		ChartHelper.prepareXAxis(mActivity, mChart.getXAxis(), null, XAxis.XAxisPosition.BOTTOM, false);

		//prepare axis left
		int leftAxisColor = Utils.getGraphColor(mActivity, 0);
		ChartHelper.prepareYAxis(mActivity, leftModule.getValue(), mChart.getAxisLeft(), leftAxisColor, YAxis.YAxisLabelPosition.OUTSIDE_CHART, true, false, 5);
		mUnitLeft.setTextColor(leftAxisColor);
		//disable right axis
		mChart.getAxisRight().setEnabled(false);

		if (rightModule != null) {
			int rightAxisColor = Utils.getGraphColor(mActivity, 1);
			ChartHelper.prepareYAxis(mActivity, rightModule.getValue(), mChart.getAxisRight(), rightAxisColor, YAxis.YAxisLabelPosition.OUTSIDE_CHART, true, false, 5);
			mUnitRight.setTextColor(rightAxisColor);
		}

		ChartHelper.prepareDataSet(mActivity, mLineDataSetLeft, false, true,
				Utils.getGraphColor(mActivity, 0), ContextCompat.getColor(mActivity, R.color.beeeon_accent), false);
		mLineDataSetLeft.setAxisDependency(YAxis.AxisDependency.LEFT);

		if (rightModule != null) {
			mLineDataSetRight = new LineDataSet(new ArrayList<Entry>(), rightModule.getName(mActivity, true));

			ChartHelper.prepareDataSet(mActivity, mLineDataSetRight, false, true,
					Utils.getGraphColor(mActivity, 1), ContextCompat.getColor(mActivity, R.color.beeeon_accent), false);

			mLineDataSetRight.setAxisDependency(YAxis.AxisDependency.RIGHT);
		}
	}

	private void loadCharData() {
		mChart.clear();
		mUnitLeft.setVisibility(View.INVISIBLE);
		mUnitRight.setVisibility(View.INVISIBLE);
		mChart.setNoDataText(mActivity.getString(R.string.chart_helper_chart_loading));
		String[] ids = Utils.parseAbsoluteModuleId(mGraphItem.getAbsoluteModuleIds().get(0));

		ChartHelper.loadChartData(mActivity, mLineDataSetLeft, mGraphItem.getGateId(), ids[0], ids[1],
				mGraphItem.getDataRange(), ModuleLog.DataType.AVERAGE, ModuleLog.DataInterval.FIVE_MINUTES, mChartLoadListener, mFormatter);

		if (mLineDataSetRight != null) {
			String[] ids2 = Utils.parseAbsoluteModuleId(mGraphItem.getAbsoluteModuleIds().get(1));
			ChartHelper.loadChartData(mActivity, mLineDataSetRight, mGraphItem.getGateId(), ids2[0], ids2[1],
					mGraphItem.getDataRange(), ModuleLog.DataType.AVERAGE, ModuleLog.DataInterval.FIVE_MINUTES, mChartLoadListener, mFormatter);
		}
	}
}
