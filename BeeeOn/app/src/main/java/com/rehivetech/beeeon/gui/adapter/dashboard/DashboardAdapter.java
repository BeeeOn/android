package com.rehivetech.beeeon.gui.adapter.dashboard;

import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;
import com.rehivetech.beeeon.gui.adapter.RecyclerViewSelectableAdapter;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.ActualValueItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.BaseItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.GraphItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.OverviewGraphItem;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.util.ChartHelper;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;

import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by martin on 15.11.15.
 */
public class DashboardAdapter extends RecyclerViewSelectableAdapter {
	private static final String TAG = DashboardAdapter.class.getSimpleName();

	private static final String GRAPH_DATE_TIME_FORMAT = "dd.MM. HH:mm";

	private static final int VIEW_TYPE_GRAPH = 0;
	private static final int VIEW_TYPE_ACT_VALUE = 1;
	private static final int VIEW_TYPE_GRAPH_OVERVIEW = 2;

	private final TimeHelper mTimeHelper;

	private BaseApplicationActivity mActivity;
	private IItemClickListener mItemClickListener;
	private List<BaseItem> mItems = new ArrayList<>();

	public DashboardAdapter(BaseApplicationActivity activity, IItemClickListener itemClickListener) {
		super(activity);
		mActivity = activity;
		mItemClickListener = itemClickListener;

		SharedPreferences prefs = Controller.getInstance(mActivity).getUserSettings();
		mTimeHelper = (prefs == null) ? null : new TimeHelper(prefs);
	}


	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		switch (viewType) {
			case VIEW_TYPE_GRAPH: {
				View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_item_graph, parent, false);
				return new DashboardGraphViewHolder(view);
			}

			case VIEW_TYPE_ACT_VALUE: {
				View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_item_act_value, parent, false);
				return new ActualValueViewHolder(view);
			}

			case VIEW_TYPE_GRAPH_OVERVIEW: {
				View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_item_overview_graph, parent, false);
				return new OverviewGraphViewHolder(view);
			}

			default:
				break;
		}

		return null;
	}

	@Override
	public int getItemViewType(int position) {
		BaseItem item = mItems.get(position);

		if (item instanceof GraphItem) {
			return VIEW_TYPE_GRAPH;
		} else if (item instanceof ActualValueItem) {
			return VIEW_TYPE_ACT_VALUE;
		} else if (item instanceof OverviewGraphItem) {
			return VIEW_TYPE_GRAPH_OVERVIEW;
		}
		return -1;
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
		BaseItem item = mItems.get(position);

		Controller controller = Controller.getInstance(mActivity);

		int viewType = holder.getItemViewType();
		switch (viewType) {
			case VIEW_TYPE_GRAPH:
				((DashboardGraphViewHolder) holder).bind(controller, (GraphItem) item, position);
				break;
			case VIEW_TYPE_ACT_VALUE: {
				((ActualValueViewHolder) holder).bind(controller, (ActualValueItem) item, position);
				break;
			}
			case VIEW_TYPE_GRAPH_OVERVIEW: {
				((OverviewGraphViewHolder) holder).bind(controller, (OverviewGraphItem) item, position);
				break;
			}
		}
	}

	@Override
	public int getItemCount() {
		return mItems.size();
	}


	public void addItem(BaseItem item) {
		mItems.add(item);

		notifyItemRangeInserted(0, mItems.size());
	}

	public void addItem(int position, BaseItem item) {
		mItems.add(position, item);
		notifyItemInserted(position);
	}

	public List<BaseItem> getItems() {
		return mItems;
	}

	public void setItems(List<BaseItem> items) {
		mItems = items;
	}

	public BaseItem getItem(int position) {
		return mItems.get(position);
	}


	public void deleteItem(BaseItem item) {
		int position = mItems.indexOf(item);
		mItems.remove(item);
		notifyItemRemoved(position);
	}


	public class DashboardGraphViewHolder extends SelectableViewHolder implements View.OnClickListener, View.OnLongClickListener{
		public final TextView mGraphName;
		public final LineChart mChart;
		public final TextView mLastUpdate;
		public final View mRoot;

		public DashboardGraphViewHolder(View itemView) {
			super(itemView);
			mRoot = itemView;
			mGraphName = (TextView) itemView.findViewById(R.id.dashboard_item_graph_name);
			mChart = (LineChart) itemView.findViewById(R.id.dashboard_item_graph_chart);
			mLastUpdate = (TextView) itemView.findViewById(R.id.dashboard_item_graph_last_update_value);

			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
		}

		public void bind(Controller controller, GraphItem item, int position) {
			Gate gate = controller.getGatesModel().getGate(item.getGateId());
			Module module = controller.getDevicesModel().getModule(item.getGateId(), item.getAbsoluteModuleIds().get(0));

			mGraphName.setText(item.getName());
			mLastUpdate.setText(mTimeHelper.formatLastUpdate(module.getDevice().getLastUpdate(), gate));

			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
				mChart.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}

			mChart.clear();
			prepareChart(item);
			fillChart(controller, item, gate);

			setSelected(isSelected(position));
		}

		private void prepareChart(GraphItem item) {
			ChartHelper.prepareChart(mChart, mActivity, null, null, null, false, false);
			ChartHelper.prepareXAxis(mActivity, mChart.getXAxis(), null, XAxis.XAxisPosition.BOTTOM, false);
			ChartHelper.prepareYAxis(mActivity, null, mChart.getAxisLeft(), Utils.getGraphColor(mActivity, 0), YAxis.YAxisLabelPosition.OUTSIDE_CHART, false, true, 3);


			if (item.getAbsoluteModuleIds().size() > 1) {
				ChartHelper.prepareYAxis(mActivity, null, mChart.getAxisRight(), Utils.getGraphColor(mActivity, 1), YAxis.YAxisLabelPosition.OUTSIDE_CHART, false, true, 3);
			} else {
				mChart.getAxisRight().setEnabled(false);
			}

			mChart.setOnTouchListener(null);
		}

		private void fillChart(Controller controller, GraphItem item, Gate gate) {
			final DateTimeFormatter dateTimeFormatter = mTimeHelper.getFormatter(GRAPH_DATE_TIME_FORMAT, gate);

			YAxis.AxisDependency axisDependency = YAxis.AxisDependency.LEFT;
			List<String> modules = item.getAbsoluteModuleIds();
			for (int i = 0; i < modules.size(); i++) {

				final LineDataSet dataSet = new LineDataSet(new ArrayList<Entry>(), modules.get(i));
				dataSet.setAxisDependency(axisDependency);
				ChartHelper.prepareDataSet(mActivity, dataSet, false, true, Utils.getGraphColor(mActivity, i), ContextCompat.getColor(mActivity, R.color.beeeon_accent), false);

				ChartHelper.ChartLoadListener chartLoadListener = new ChartHelper.ChartLoadListener() {
					@Override
					public void onChartLoaded(DataSet dataset, List<String> xValues) {
						LineData lineData = mChart.getLineData() != null ? mChart.getLineData() : new LineData(xValues);
						lineData.addDataSet(dataSet);

						mChart.setData(lineData);
						mChart.invalidate();
					}
				};
				Module module = controller.getDevicesModel().getModule(gate.getId(), modules.get(i));

				ModuleLog.DataInterval dataInterval = (item.getDataRange() > ChartHelper.RANGE_DAY) ? ModuleLog.DataInterval.HALF_HOUR : ModuleLog.DataInterval.TEN_MINUTES;
				ChartHelper.loadChartData(mActivity, controller, dataSet, item.getGateId(), module.getDevice().getId(), module.getId(), item.getDataRange(),
						ModuleLog.DataType.AVERAGE, dataInterval, chartLoadListener, dateTimeFormatter);

				axisDependency = YAxis.AxisDependency.RIGHT;
			}
		}


		@Override
		protected void setSelectedBackground(boolean isSelected) {
			if (isSelected) {
				mRoot.setBackgroundResource(R.color.gray_material_400);
			} else {
				mRoot.setBackgroundResource(R.color.white);
			}
		}

		@Override
		public void onClick(View v) {

		}

		@Override
		public boolean onLongClick(View v) {
			if(mItemClickListener != null && mItemClickListener.onRecyclerViewItemLongClick(getAdapterPosition(), getItemViewType())){
				v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
				return true;
			}
			return false;
		}
	}

	public class ActualValueViewHolder extends SelectableViewHolder implements View.OnClickListener, View.OnLongClickListener {
		public final ImageView mIcon;
		public final TextView mLabel;
		public final TextView mValue;
		public final TextView mLastUpdate;
		public final View mRoot;


		public ActualValueViewHolder(View itemView) {
			super(itemView);
			mRoot = itemView;
			mIcon = (ImageView) itemView.findViewById(R.id.dashboard_item_act_value_icon);
			mLabel = (TextView) itemView.findViewById(R.id.dashboard_item_act_value_label);
			mValue = (TextView) itemView.findViewById(R.id.dashboard_item_act_value_value);
			mLastUpdate = (TextView) itemView.findViewById(R.id.dashboard_item_act_value_last_update_value);

			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
		}

		public void bind(Controller controller, ActualValueItem item, int position) {
			Module module = controller.getDevicesModel().getModule(item.getGateId(), item.getAbsoluteModuleId());
			SharedPreferences prefs = controller.getUserSettings();
			UnitsHelper unitsHelper = new UnitsHelper(prefs, mActivity);

			mLabel.setText(item.getName());
			mIcon.setImageResource(module.getIconResource(IconResourceType.DARK));
			mValue.setText(String.format("%.2f %s", module.getValue().getDoubleValue(), unitsHelper.getStringUnit(module.getValue())));
			mLastUpdate.setText(mTimeHelper.formatLastUpdate(module.getDevice().getLastUpdate(), controller.getGatesModel().getGate(item.getGateId())));

			setSelected(isSelected(position));
		}

		@Override
		public void onClick(View v) {

		}

		@Override
		public boolean onLongClick(View v) {
			if(mItemClickListener != null && mItemClickListener.onRecyclerViewItemLongClick(getAdapterPosition(), getItemViewType())){
				v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
				return true;
			}
			return false;
		}

		@Override
		protected void setSelectedBackground(boolean isSelected) {
			if (isSelected) {
				mRoot.setBackgroundResource(R.color.gray_material_400);
			} else {
				mRoot.setBackgroundResource(R.color.white);
			}
		}
	}

	public class OverviewGraphViewHolder extends SelectableViewHolder implements View.OnClickListener, View.OnLongClickListener  {

		public final TextView mGraphName;
		public final BarChart mChart;
		public final TextView mLastUpdate;
		public final View mRoot;

		public OverviewGraphViewHolder(View itemView) {
			super(itemView);

			mRoot = itemView;
			mGraphName = (TextView) itemView.findViewById(R.id.dashboard_item_overview_graph_name);
			mChart = (BarChart) itemView.findViewById(R.id.dashboard_item_overview_graph_chart);
			mLastUpdate = (TextView) itemView.findViewById(R.id.dashboard_item_overview_graph_last_update_value);

			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
		}

		public void bind(Controller controller, OverviewGraphItem item, int position) {
			Gate gate = controller.getGatesModel().getGate(item.getGateId());
			Module module = controller.getDevicesModel().getModule(gate.getId(), item.getAbsoluteModuleId());

			mGraphName.setText(item.getName());
			mLastUpdate.setText(mTimeHelper.formatLastUpdate(module.getDevice().getLastUpdate(), gate));

			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
				mChart.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}

			mChart.clear();
			prepareChart();
			fillChart(controller, item, gate, module);

			setSelected(isSelected(position));
		}

		private void prepareChart() {
			ChartHelper.prepareChart(mChart, mActivity, null, null, null, false, false);
			XAxis xAxis = mChart.getXAxis();
			ChartHelper.prepareXAxis(mActivity, xAxis, null, XAxis.XAxisPosition.BOTTOM, false);
			ChartHelper.prepareYAxis(mActivity, null, mChart.getAxisLeft(), Utils.getGraphColor(mActivity, 0), YAxis.YAxisLabelPosition.OUTSIDE_CHART, false, true, 3);

			mChart.getAxisRight().setEnabled(false);

			mChart.setOnTouchListener(null);
		}

		private void fillChart(Controller controller, OverviewGraphItem item, Gate gate, Module module) {
			final DateTimeFormatter dateTimeFormatter = mTimeHelper.getFormatter(GRAPH_DATE_TIME_FORMAT, gate);


			final BarDataSet dataSet = new BarDataSet(new ArrayList<BarEntry>(), item.getAbsoluteModuleId());
			ChartHelper.prepareDataSet(mActivity, dataSet, true, false, Utils.getGraphColor(mActivity, 0), ContextCompat.getColor(mActivity, R.color.beeeon_accent), false);

			ChartHelper.ChartLoadListener chartLoadListener = new ChartHelper.ChartLoadListener() {
				@Override
				public void onChartLoaded(DataSet dataset, List<String> xValues) {
					BarData barData = mChart.getBarData() != null ? mChart.getBarData() : new BarData(xValues);
					barData.addDataSet(dataSet);

					mChart.setData(barData);
					List<String> xValuesCustom = new ArrayList<>(Arrays.asList("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"));
					mChart.getXAxis().setValues(xValuesCustom);
					mChart.invalidate();
				}
			};

			ModuleLog.DataInterval dataInterval = ModuleLog.DataInterval.DAY;
			ChartHelper.loadChartData(mActivity, controller, dataSet, item.getGateId(), module.getDevice().getId(), module.getId(), ChartHelper.RANGE_WEEK,
					item.getDataType(), dataInterval, chartLoadListener, dateTimeFormatter);

		}

		@Override
		public void onClick(View v) {

		}

		@Override
		public boolean onLongClick(View v) {
			if(mItemClickListener != null && mItemClickListener.onRecyclerViewItemLongClick(getAdapterPosition(), getItemViewType())){
				v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
				return true;
			}
			return false;
		}

		@Override
		protected void setSelectedBackground(boolean isSelected) {
			if (isSelected) {
				mRoot.setBackgroundResource(R.color.gray_material_400);
			} else {
				mRoot.setBackgroundResource(R.color.white);
			}
		}
	}
}
