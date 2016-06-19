package com.rehivetech.beeeon.gui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.OverviewGraphItem;
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

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by martin on 19.3.16.
 */
public class DashboardOverviewGraphDetailFragment extends BaseDashboardDetailFragment {

	@BindView(R.id.fragment_dashboard_detail_axis_unit)
	TextView mUnit;
	@BindView(R.id.fragment_dashboard_detail_graph)
	BarChart mChart;

	private BarDataSet mDataSet;

	private OverviewGraphItem mItem;
	private DateTimeFormatter mFormatter;
	private ChartHelper.ChartLoadListener mChartLoadListener = new ChartHelper.ChartLoadListener() {
		@Override
		public void onChartLoaded(DataSet dataset, List<String> xValues) {
			List<String> xValuesCustom = ChartHelper.getWeekDays(mActivity);

			BarData data = mChart.getBarData() == null ? new BarData(xValuesCustom) : mChart.getBarData();

			if (dataset.getYVals().size() < 2 && mChart.getBarData() == null) {
				mChart.setNoDataText(getString(R.string.chart_helper_chart_no_data));
				mChart.invalidate();
				return;
			}

			data.addDataSet((BarDataSet) dataset);
			mChart.setData(data);
			mChart.getXAxis().setLabelsToSkip(0);
			mChart.invalidate();
			mUnit.setVisibility(View.VISIBLE);
		}
	};

	public static DashboardOverviewGraphDetailFragment newInstance(OverviewGraphItem item) {

		Bundle args = new Bundle();
		args.putParcelable(ARG_DASHBOARD_ITEM, item);
		DashboardOverviewGraphDetailFragment fragment = new DashboardOverviewGraphDetailFragment();
		fragment.setArguments(args);
		return fragment;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			mItem = args.getParcelable(ARG_DASHBOARD_ITEM);
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_dashboard_detail_overview_week_graph, container, false);
		mUnbinder = ButterKnife.bind(this, view);
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Controller controller = Controller.getInstance(mActivity);
		Module module = controller.getDevicesModel().getModule(mItem.getGateId(), mItem.getAbsoluteModuleId());

		if (module == null) {
			mActivity.finish();
			return;
		}

		mActivity.setToolbarTitle(mItem.getName());
		mActivity.setupRefreshIcon(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadCharData();
			}
		});

		SharedPreferences prefs = controller.getUserSettings();
		TimeHelper timeHelper = Utils.getTimeHelper(prefs);
		mFormatter = timeHelper != null
				? timeHelper.getFormatter(ChartHelper.GRAPH_DATE_TIME_FORMAT, controller.getGatesModel().getGate(mItem.getGateId()))
				: DateTimeFormat.forPattern(ChartHelper.GRAPH_DATE_TIME_FORMAT).withZone(DateTimeZone.getDefault());

		UnitsHelper unitsHelper = Utils.getUnitsHelper(prefs, mActivity);

		if (unitsHelper != null) {
			mUnit.setText(unitsHelper.getStringUnit(module.getValue()));
		}

		prepareChart(module);
	}

	@Override
	public void onResume() {
		super.onResume();
		GoogleAnalyticsManager.getInstance().logScreen(GoogleAnalyticsManager.DASHBOARD_OVERVIEW_GRAPH_DETAIL_SCREEN);
		loadCharData();
	}

	private void prepareChart(Module module) {
		mDataSet = new BarDataSet(new ArrayList<BarEntry>(), mItem.getName());
		ChartHelper.prepareChart(mChart, mActivity, module.getValue(), null, null, false, true);

		// prepare axis bottom
		ChartHelper.prepareXAxis(mActivity, mChart.getXAxis(), null, XAxis.XAxisPosition.BOTTOM, false);

		//prepare axis left
		ChartHelper.prepareYAxis(mActivity, module.getValue(), mChart.getAxisLeft(), null, YAxis.YAxisLabelPosition.OUTSIDE_CHART, true, false, 5);
		//disable right axis
		mChart.getAxisRight().setEnabled(false);

		mChart.setMaxVisibleValueCount(0);
		ChartHelper.prepareDataSet(mActivity, module.getValue(), mDataSet, true, true,
				ContextCompat.getColor(mActivity, R.color.beeeon_primary), ContextCompat.getColor(mActivity, R.color.beeeon_accent), false);
		mDataSet.setDrawValues(false);
	}

	private void loadCharData() {
		mChart.clear();
		mUnit.setVisibility(View.INVISIBLE);
		mChart.setNoDataText(mActivity.getString(R.string.chart_helper_chart_loading));
		String[] ids = Utils.parseAbsoluteModuleId(mItem.getAbsoluteModuleId());

		ChartHelper.loadChartData(mActivity, mDataSet, mItem.getGateId(), ids[0], ids[1],
				ChartHelper.RANGE_WEEK, mItem.getDataType(), ModuleLog.DataInterval.DAY, mChartLoadListener, mFormatter);
	}
}
