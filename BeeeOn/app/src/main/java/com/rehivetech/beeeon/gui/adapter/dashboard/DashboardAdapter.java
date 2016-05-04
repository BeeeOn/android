package com.rehivetech.beeeon.gui.adapter.dashboard;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;
import com.rehivetech.beeeon.gui.adapter.RecyclerViewSelectableAdapter;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.ActualValueItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.BaseItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.GraphItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.OverviewGraphItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.VentilationItem;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.util.ChartHelper;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by martin on 15.11.15.
 */
public class DashboardAdapter extends RecyclerViewSelectableAdapter {
	private static final String TAG = DashboardAdapter.class.getSimpleName();

	private static final String GRAPH_DATE_TIME_FORMAT = "dd.MM. HH:mm";

	public static final int VIEW_TYPE_GRAPH = 0;
	public static final int VIEW_TYPE_ACT_VALUE = 1;
	public static final int VIEW_TYPE_GRAPH_OVERVIEW = 2;
	public static final int VIEW_TYPE_VENTILATION = 3;

	private final TimeHelper mTimeHelper;
	private final UnitsHelper mUnitsHelper;

	private BaseApplicationActivity mActivity;
	private IItemClickListener mItemClickListener;
	private ActionModeCallback mActionModeCallback;
	private List<BaseItem> mItems = new ArrayList<>();

	public DashboardAdapter(BaseApplicationActivity activity, IItemClickListener itemClickListener, ActionModeCallback actionModeCallback) {
		super(activity);
		mActivity = activity;
		mItemClickListener = itemClickListener;
		mActionModeCallback = actionModeCallback;

		SharedPreferences prefs = Controller.getInstance(mActivity).getUserSettings();
		mTimeHelper = Utils.getTimeHelper(prefs);
		mUnitsHelper = Utils.getUnitsHelper(prefs, mActivity);
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

			case VIEW_TYPE_VENTILATION: {
				View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_item_type_ventilation, parent, false);
				return new VentilationViewHolder(view);
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
		} else if (item instanceof VentilationItem) {
			return VIEW_TYPE_VENTILATION;
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
			case VIEW_TYPE_VENTILATION: {
				((VentilationViewHolder) holder).bind(controller, (VentilationItem) item, position);
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
		notifyDataSetChanged();
	}

	public BaseItem getItem(int position) {
		return mItems.get(position);
	}


	public void moveItem(int fromPosition, int toPosition) {
		Collections.swap(mItems, fromPosition, toPosition);
		swapSelectedPosition(fromPosition, toPosition);
		notifyItemMoved(fromPosition, toPosition);
	}

	public void deleteItem(BaseItem item) {
		int position = mItems.indexOf(item);
		mItems.remove(item);
		notifyItemRemoved(position);
	}

	public abstract class BaseDashboardViewHolder extends SelectableViewHolder implements View.OnLongClickListener {
		public final CardView mCardView;

		public BaseDashboardViewHolder(View itemView) {
			super(itemView);
			mCardView = (CardView) itemView;
		}

		@Override
		protected void setSelectedBackground(boolean isSelected) {
			if (isSelected) {
				mCardView.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.gray_material_400));
			} else {
				mCardView.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.white));
			}

		}

		protected boolean handleSelection() {
			if (getSelectedItemCount() > 0) {
				toggleSelection(getAdapterPosition());

				if (getSelectedItemCount() == 0) {
					mActionModeCallback.finishActionMode();
				}
				return true;
			} else {
				return false;
			}
		}

		protected void handleGoogleAnalytics() {
			String analyticsItemName = null;
			switch (getItemViewType()) {
				case VIEW_TYPE_ACT_VALUE:
					analyticsItemName = GoogleAnalyticsManager.DASHBOARD_DETAIL_CLICK_ACTUAL_VALUE_ITEM;
					break;
				case VIEW_TYPE_GRAPH_OVERVIEW:
					analyticsItemName = GoogleAnalyticsManager.DASHBOARD_DETAIL_CLICK_GRAPH_OVERVIEW_ITEM;
					break;
				case VIEW_TYPE_GRAPH:
					analyticsItemName = GoogleAnalyticsManager.DASHBOARD_DETAIL_CLICK_GRAPH_ITEM;
					break;
			}

			GoogleAnalyticsManager.getInstance().logEvent(GoogleAnalyticsManager.EVENT_CATEGORY_DASHBOARD, GoogleAnalyticsManager.EVENT_ACTION_DETAIL_CLICK, analyticsItemName);
		}

		@Override
		public boolean onLongClick(View v) {
			return mItemClickListener != null && mItemClickListener.onRecyclerViewItemLongClick(getAdapterPosition(), getItemViewType());
		}
	}

	public class DashboardGraphViewHolder extends BaseDashboardViewHolder implements View.OnClickListener{
		public final TextView mGraphName;
		public final TextView mLeftAxisUnit;
		public final TextView mRightAxisUnit;
		public final LineChart mChart;
		public final TextView mLastUpdate;

		public DashboardGraphViewHolder(View itemView) {
			super(itemView);
			mGraphName = (TextView) itemView.findViewById(R.id.dashboard_item_graph_name);
			mLeftAxisUnit = (TextView) itemView.findViewById(R.id.dashboard_item_graph_left_axis_unit);
			mRightAxisUnit = (TextView) itemView.findViewById(R.id.dashboard_item_graph_right_axis_unit);
			mChart = (LineChart) itemView.findViewById(R.id.dashboard_item_graph_chart);
			mLastUpdate = (TextView) itemView.findViewById(R.id.dashboard_item_graph_last_update_value);

			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
		}

		public void bind(Controller controller, GraphItem item, int position) {
			Gate gate = controller.getGatesModel().getGate(item.getGateId());
			Module leftModule = controller.getDevicesModel().getModule(item.getGateId(), item.getAbsoluteModuleIds().get(0));
			Module rightModule = (item.getAbsoluteModuleIds().size() > 1) ? controller.getDevicesModel().getModule(item.getGateId(), item.getAbsoluteModuleIds().get(1)) : null;

			mGraphName.setText(item.getName());
			mLastUpdate.setText(mTimeHelper.formatLastUpdate(leftModule.getDevice().getLastUpdate(), gate));

			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
				mChart.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}

			mChart.clear();
			mLeftAxisUnit.setVisibility(View.INVISIBLE);
			mRightAxisUnit.setVisibility(View.INVISIBLE);
			prepareChart(item, leftModule.getValue(), rightModule != null ? rightModule.getValue() : null);
			mChart.setNoDataText(mActivity.getString(R.string.chart_helper_chart_loading));
			fillChart(controller, item, gate);

			setSelected(isSelected(position));
		}

		private void prepareChart(GraphItem item, BaseValue leftBaseValue, @Nullable BaseValue rightBaseValue) {
			ChartHelper.prepareChart(mChart, mActivity, null, null, null, false, false);
			ChartHelper.prepareXAxis(mActivity, mChart.getXAxis(), null, XAxis.XAxisPosition.BOTTOM, false);
			ChartHelper.prepareYAxis(mActivity, leftBaseValue, mChart.getAxisLeft(), Utils.getGraphColor(mActivity, 0), YAxis.YAxisLabelPosition.OUTSIDE_CHART, false, true, 3);


			if (item.getAbsoluteModuleIds().size() > 1) {
				ChartHelper.prepareYAxis(mActivity, rightBaseValue, mChart.getAxisRight(), Utils.getGraphColor(mActivity, 1), YAxis.YAxisLabelPosition.OUTSIDE_CHART, false, true, 3);
				mChart.getAxisRight().setEnabled(true);
			} else {
				mChart.getAxisRight().setEnabled(false);
			}

			mChart.setAutoScaleMinMaxEnabled(true);
			mChart.setOnTouchListener(null);
		}

		private void fillChart(final Controller controller, final GraphItem item, final Gate gate) {
			final DateTimeFormatter dateTimeFormatter = mTimeHelper.getFormatter(GRAPH_DATE_TIME_FORMAT, gate);

			YAxis.AxisDependency axisDependency = YAxis.AxisDependency.LEFT;
			final List<String> modules = item.getAbsoluteModuleIds();
			for (int i = 0; i < modules.size(); i++) {

				final LineDataSet dataSet = new LineDataSet(new ArrayList<Entry>(), modules.get(i));
				dataSet.setAxisDependency(axisDependency);
				ChartHelper.prepareDataSet(mActivity,null, dataSet, false, true, Utils.getGraphColor(mActivity, i), ContextCompat.getColor(mActivity, R.color.beeeon_accent), false);

				ChartHelper.ChartLoadListener chartLoadListener = new ChartHelper.ChartLoadListener() {
					@Override
					public void onChartLoaded(DataSet dataset, List<String> xValues) {
						if (dataset.getYVals().size() > 1) {
							LineData lineData = mChart.getLineData() != null ? mChart.getLineData() : new LineData(xValues);
							lineData.addDataSet(dataSet);

							mChart.setData(lineData);

							mLeftAxisUnit.setVisibility(View.VISIBLE);
							mLeftAxisUnit.setText(mUnitsHelper.getStringUnit(controller.getDevicesModel().getModule(gate.getId(), modules.get(0)).getValue()));
							mLeftAxisUnit.setTextColor(Utils.getGraphColor(mActivity, 0));
							if (modules.size() > 1) {
								mRightAxisUnit.setVisibility(View.VISIBLE);
								mRightAxisUnit.setText(mUnitsHelper.getStringUnit(controller.getDevicesModel().getModule(gate.getId(), modules.get(1)).getValue()));
								mRightAxisUnit.setTextColor(Utils.getGraphColor(mActivity, 1));
							}

						} else {
							if (dataset.getAxisDependency() == YAxis.AxisDependency.LEFT) {
								mLeftAxisUnit.setVisibility(View.GONE);
								mChart.getAxisLeft().setEnabled(false);
							} else {
								mRightAxisUnit.setVisibility(View.GONE);
								mChart.getAxisRight().setEnabled(false);
							}
						}
						mChart.setNoDataText(mActivity.getString(R.string.chart_helper_chart_no_data));
						mChart.invalidate();


					}
				};
				Module module = controller.getDevicesModel().getModule(gate.getId(), modules.get(i));

				ModuleLog.DataInterval dataInterval = (item.getDataRange() > ChartHelper.RANGE_DAY) ? ModuleLog.DataInterval.HALF_HOUR : ModuleLog.DataInterval.TEN_MINUTES;
				ChartHelper.loadChartData(mActivity, dataSet, item.getGateId(), module.getDevice().getId(), module.getId(), item.getDataRange(),
						ModuleLog.DataType.AVERAGE, dataInterval, chartLoadListener, dateTimeFormatter);

				axisDependency = YAxis.AxisDependency.RIGHT;
			}
		}

		@Override
		public void onClick(View v) {
			if (!handleSelection() && mItemClickListener != null) {
				mItemClickListener.onRecyclerViewItemClick(getAdapterPosition(), DashboardAdapter.VIEW_TYPE_GRAPH);
				handleGoogleAnalytics();
			}
		}

	}

	public class ActualValueViewHolder extends BaseDashboardViewHolder implements View.OnClickListener{
		public final ImageView mIcon;
		public final AppCompatImageView mTrend;
		public final TextView mLabel;
		public final TextView mValue;
		public final TextView mLastUpdate;

		public ActualValueViewHolder(View itemView) {
			super(itemView);
			mIcon = (ImageView) itemView.findViewById(R.id.dashboard_item_act_value_icon);
			mTrend = (AppCompatImageView) itemView.findViewById(R.id.dashboard_item_act_value_trend);
			mLabel = (TextView) itemView.findViewById(R.id.dashboard_item_act_value_label);
			mValue = (TextView) itemView.findViewById(R.id.dashboard_item_act_value_value);
			mLastUpdate = (TextView) itemView.findViewById(R.id.dashboard_item_act_value_last_update_value);

			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
		}

		public void bind(Controller controller, ActualValueItem item, int position) {
			Module module = controller.getDevicesModel().getModule(item.getGateId(), item.getAbsoluteModuleId());

			UnitsHelper unitsHelper = Utils.getUnitsHelper(mActivity);

			if (unitsHelper == null) {
				return;
			}

			mLabel.setText(item.getName());
			mIcon.setImageResource(module.getIconResource(IconResourceType.DARK));

			if (module.getValue() instanceof EnumValue) {
				mValue.setText(((EnumValue) module.getValue()).getStateStringResource());
				mTrend.setVisibility(View.GONE);
			} else {
				mValue.setText(String.format("%s %s", unitsHelper.getStringValue(module.getValue()), unitsHelper.getStringUnit(module.getValue())));
				mTrend.setVisibility(View.VISIBLE);
				DateTime end = DateTime.now(DateTimeZone.UTC);
				DateTime start = end.minusHours(1);
				ModuleLog.DataPair pair = new ModuleLog.DataPair(module, new Interval(start, end), ModuleLog.DataType.AVERAGE, ModuleLog.DataInterval.RAW);
				ModuleLog log = Controller.getInstance(mContext).getModuleLogsModel().getModuleLog(pair);

				List<Float> values = new ArrayList<>(log.getValues().values());
				if (!values.isEmpty() && values.size() > 2) {
					float last = values.get(values.size() - 1);
					float prevLast = values.get(values.size() - 2);

					float avg = (last + prevLast) / 2;

					double actValue = module.getValue().getDoubleValue();

					if (avg < actValue) {
						mTrend.setImageResource(R.drawable.ic_trending_up_black_24dp);
					} else if (avg == actValue) {
						mTrend.setImageResource(R.drawable.ic_trending_flat_black_24dp);
					} else {
						mTrend.setImageResource(R.drawable.ic_trending_down_black_24dp);
					}

				}
			}

			mLastUpdate.setText(mTimeHelper.formatLastUpdate(module.getDevice().getLastUpdate(), controller.getGatesModel().getGate(item.getGateId())));

			setSelected(isSelected(position));
		}

		@Override
		public void onClick(View v) {

			if (!handleSelection() && mItemClickListener != null) {
				mItemClickListener.onRecyclerViewItemClick(getAdapterPosition(), DashboardAdapter.VIEW_TYPE_ACT_VALUE);
				handleGoogleAnalytics();
			}
		}
	}

	public class OverviewGraphViewHolder extends BaseDashboardViewHolder implements View.OnClickListener{

		public final TextView mGraphName;
		public final TextView mGraphUnit;
		public final BarChart mChart;
		public final TextView mLastUpdate;

		public OverviewGraphViewHolder(View itemView) {
			super(itemView);

			mGraphName = (TextView) itemView.findViewById(R.id.dashboard_item_overview_graph_name);
			mGraphUnit = (TextView) itemView.findViewById(R.id.dashboard_item_overview_graph_axis_unit);
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
			prepareChart(module.getValue());
			mChart.setNoDataText(mActivity.getString(R.string.chart_helper_chart_loading));
			fillChart(item, gate, module);

			setSelected(isSelected(position));
		}

		private void prepareChart(BaseValue baseValue) {
			ChartHelper.prepareChart(mChart, mActivity, null, null, null, false, false);
			XAxis xAxis = mChart.getXAxis();
			ChartHelper.prepareXAxis(mActivity, xAxis, null, XAxis.XAxisPosition.BOTTOM, false);
			ChartHelper.prepareYAxis(mActivity, baseValue, mChart.getAxisLeft(), Utils.getGraphColor(mActivity, 0), YAxis.YAxisLabelPosition.OUTSIDE_CHART, false, true, 3);

			mChart.getAxisRight().setEnabled(false);

			mChart.setOnTouchListener(null);
		}

		private void fillChart(OverviewGraphItem item, Gate gate, final Module module) {
			final DateTimeFormatter dateTimeFormatter = mTimeHelper.getFormatter(GRAPH_DATE_TIME_FORMAT, gate);


			final BarDataSet dataSet = new BarDataSet(new ArrayList<BarEntry>(), item.getAbsoluteModuleId());
			ChartHelper.prepareDataSet(mActivity, null, dataSet, true, false, Utils.getGraphColor(mActivity, 0), ContextCompat.getColor(mActivity, R.color.beeeon_accent), false);

			ChartHelper.ChartLoadListener chartLoadListener = new ChartHelper.ChartLoadListener() {
				@Override
				public void onChartLoaded(DataSet dataset, List<String> xValues) {

					if (dataSet.getYVals().size() > 1) {
						BarData barData = new BarData(xValues);
						barData.addDataSet(dataSet);

						mChart.setData(barData);

						List<String> xValuesCustom = ChartHelper.getWeekDays(mContext);

						mChart.getXAxis().setValues(xValuesCustom);
						mChart.getXAxis().setLabelsToSkip(0);
						mGraphUnit.setText(mUnitsHelper.getStringUnit(module.getValue()));
						mGraphUnit.setTextColor(Utils.getGraphColor(mActivity, 0));
					}

					mChart.setNoDataText(mActivity.getString(R.string.chart_helper_chart_no_data));
					mChart.invalidate();
				}
			};

			ModuleLog.DataInterval dataInterval = ModuleLog.DataInterval.DAY;
			ChartHelper.loadChartData(mActivity, dataSet, item.getGateId(), module.getDevice().getId(), module.getId(), ChartHelper.RANGE_WEEK,
					item.getDataType(), dataInterval, chartLoadListener, dateTimeFormatter);

		}

		@Override
		public void onClick(View v) {
			if (!handleSelection() && mItemClickListener != null) {
				mItemClickListener.onRecyclerViewItemClick(getAdapterPosition(), DashboardAdapter.VIEW_TYPE_GRAPH_OVERVIEW);
				handleGoogleAnalytics();
			}
		}
	}

	public class VentilationViewHolder extends BaseDashboardViewHolder implements View.OnClickListener{

		final TextView mOutSideTemp;
		final TextView mInsideTemp;
		final ImageView mIcon;

		public VentilationViewHolder(View itemView) {
			super(itemView);

			mOutSideTemp = (TextView) itemView.findViewById(R.id.dashboard_item_ventilation_outside_value);
			mInsideTemp = (TextView) itemView.findViewById(R.id.dashboard_item_ventilation_inside_value);
			mIcon = (ImageView) itemView.findViewById(R.id.dashboard_item_ventilation_icon);

			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
		}

		public void bind(Controller controller, VentilationItem item, int position) {
			float outSideTemp = 0;
			float insideTemp = 0;

			if (item.getOutsideAbsoluteModuleId() == null) {
				outSideTemp = controller.getWeatherModel().getWeather(item.getGateId()).getTemp();

			} else {
				Module module = controller.getDevicesModel().getModule(item.getGateId(), item.getOutsideAbsoluteModuleId());

				if (module != null) {
					outSideTemp = (float) module.getValue().getDoubleValue();
				}
			}

			Module insideModule = controller.getDevicesModel().getModule(item.getGateId(), item.getInSideAbsoluteModuleId());

			if (insideModule != null) {
				insideTemp = (float) insideModule.getValue().getDoubleValue();
				mOutSideTemp.setText(String.format("%s %s", mUnitsHelper.getStringValue(insideModule.getValue(), outSideTemp), mUnitsHelper.getStringUnit(insideModule.getValue())));
				mInsideTemp.setText(mUnitsHelper.getStringValueUnit(insideModule.getValue()));
			}


			Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.oval_primary);

			if (outSideTemp <= insideTemp) {
				mIcon.setImageResource(R.drawable.ic_action_accept);
				drawable = Utils.setDrawableTint(drawable, ContextCompat.getColor(mContext, R.color.green));

			} else {
				drawable = Utils.setDrawableTint(drawable, ContextCompat.getColor(mContext, R.color.red));

				mIcon.setImageResource(R.drawable.ic_action_cancel);
			}

			Utils.setBackgroundImageDrawable(mIcon, drawable);

			setSelected(isSelected(position));
		}

		@Override
		public void onClick(View v) {
			if (!handleSelection() && mItemClickListener != null) {
				mItemClickListener.onRecyclerViewItemClick(getAdapterPosition(), DashboardAdapter.VIEW_TYPE_VENTILATION);
			}
		}
	}

	public interface ActionModeCallback {
		void finishActionMode();
	}
}
