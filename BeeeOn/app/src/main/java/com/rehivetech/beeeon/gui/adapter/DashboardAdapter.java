package com.rehivetech.beeeon.gui.adapter;

import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.util.ChartHelper;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.Utils;

import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import static com.rehivetech.beeeon.util.ChartHelper.DataRange;

/**
 * Created by martin on 15.11.15.
 */
public class DashboardAdapter extends RecyclerView.Adapter {
	private static final String TAG = DashboardAdapter.class.getSimpleName();

	private static final String GRAPH_DATE_TIME_FORMAT = "dd.MM. HH:mm";

	private static final int VIEW_TYPE_GRAPH = 0;
	private static final int VIEW_TYPE_ACT_VALUE = 1;

	private final TimeHelper mTimeHelper;

	private BaseApplicationActivity mActivity;
	private List<BaseItem> mItems = new ArrayList<>();

	public DashboardAdapter(BaseApplicationActivity context) {
		mActivity = context;

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

			default:
				break;
		}

		return null;
	}

	@Override
	public int getItemViewType(int position) {
		if (mItems.get(position) instanceof GraphItem) {
			return VIEW_TYPE_GRAPH;
		} else if (mItems.get(position) instanceof ActualValueItem) {
			return VIEW_TYPE_ACT_VALUE;
		}

		return -1;
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
		BaseItem item = mItems.get(position);

		Controller controller = Controller.getInstance(mActivity);

		int viewType = holder.getItemViewType();
		switch (viewType) {
			case VIEW_TYPE_GRAPH: {
				final GraphItem graphItem = (GraphItem) item;
				Gate gate = controller.getGatesModel().getGate(graphItem.getGateId());
				Device device = controller.getDevicesModel().getDevice(graphItem.getGateId(), graphItem.getDeviceId());
				((DashboardGraphViewHolder) holder).mGraphName.setText(item.getName());
				final DateTimeFormatter dateTimeFormatter = mTimeHelper.getFormatter(GRAPH_DATE_TIME_FORMAT, gate);

				final LineChart chart = ((DashboardGraphViewHolder) holder).mChart;
				chart.clear();

				ChartHelper.prepareChart(chart, mActivity, null, null, null, false);
				ChartHelper.prepareXAxis(mActivity, chart.getXAxis(), null, XAxis.XAxisPosition.BOTTOM, false);
				ChartHelper.prepareYAxis(mActivity, null, chart.getAxisLeft(), Utils.getGraphColor(mActivity, 0), YAxis.YAxisLabelPosition.OUTSIDE_CHART, false, true);

				if (graphItem.getModules().size() > 1) {
					ChartHelper.prepareYAxis(mActivity, null, chart.getAxisRight(), Utils.getGraphColor(mActivity, 1), YAxis.YAxisLabelPosition.OUTSIDE_CHART, false, true);
				} else {
					chart.getAxisRight().setEnabled(false);
				}

				YAxis.AxisDependency axisDependency = YAxis.AxisDependency.LEFT;
				List<String> modules = graphItem.getModules();
				for (int i = 0; i < modules.size(); i++) {

					final LineDataSet dataSet = new LineDataSet(new ArrayList<Entry>(), modules.get(i));
					dataSet.setAxisDependency(axisDependency);
					ChartHelper.prepareDataSet(mActivity, dataSet, false, true, Utils.getGraphColor(mActivity, i), ContextCompat.getColor(mActivity, R.color.beeeon_accent));

					ChartHelper.ChartLoadListener chartLoadListener = new ChartHelper.ChartLoadListener() {
						@Override
						public void onChartLoaded(DataSet dataset, List<String> xValues) {
							LineData lineData;
							if (chart.getLineData() != null) {
								lineData = chart.getLineData();
							} else {
								lineData = new LineData(xValues);
							}
							lineData.addDataSet(dataSet);
							chart.setData(lineData);
							chart.invalidate();
						}
					};

					ChartHelper.loadChartData(mActivity, controller, dataSet, graphItem.getGateId(), graphItem.getDeviceId(), modules.get(i), graphItem.getDataRange(),
							ModuleLog.DataType.AVERAGE, ModuleLog.DataInterval.RAW, chartLoadListener, dateTimeFormatter);

					axisDependency = YAxis.AxisDependency.RIGHT;
				}

				((DashboardGraphViewHolder) holder).mLastUpdate.setText(mTimeHelper.formatLastUpdate(device.getLastUpdate(), gate));
				break;
			}
			case VIEW_TYPE_ACT_VALUE: {
				final ActualValueItem actualValueItem = (ActualValueItem) item;
				ActualValueViewHolder viewHolder = (ActualValueViewHolder) holder;
				viewHolder.mLabel.setText(actualValueItem.getName());
				Device device = controller.getDevicesModel().getDevice(actualValueItem.getGateId(), actualValueItem.getDeviceId());
				device.getLastUpdate();
				Module module = device.getModuleById(actualValueItem.getModuleId());
				viewHolder.mIcon.setImageResource(module.getIconResource(IconResourceType.DARK));
				viewHolder.mValue.setText(String.format("%.2f %s", module.getValue().getDoubleValue(), "°C"));
				viewHolder.mLastUpdate.setText(mTimeHelper.formatLastUpdate(device.getLastUpdate(), controller.getGatesModel().getGate(actualValueItem.getGateId())));
				break;
			}

		}
	}

	@Override
	public int getItemCount() {
		return mItems.size();
	}

	public class DashboardGraphViewHolder extends RecyclerView.ViewHolder {
		public final TextView mGraphName;
		public final LineChart mChart;
		public final TextView mLastUpdate;

		public DashboardGraphViewHolder(View itemView) {
			super(itemView);
			mGraphName = (TextView) itemView.findViewById(R.id.dashboard_item_graph_name);
			mChart = (LineChart) itemView.findViewById(R.id.dashboard_item_graph_chart);
			mLastUpdate = (TextView) itemView.findViewById(R.id.dashboard_item_graph_last_update_value);
		}
	}

	public class ActualValueViewHolder extends RecyclerView.ViewHolder {
		public final ImageView mIcon;
		public final TextView mLabel;
		public final TextView mValue;
		public final TextView mLastUpdate;


		public ActualValueViewHolder(View itemView) {
			super(itemView);

			mIcon = (ImageView) itemView.findViewById(R.id.dashboard_item_act_value_icon);
			mLabel = (TextView) itemView.findViewById(R.id.dashboard_item_act_value_label);
			mValue = (TextView) itemView.findViewById(R.id.dashboard_item_act_value_value);
			mLastUpdate = (TextView) itemView.findViewById(R.id.dashboard_item_act_value_las_update_value);

		}
	}

	public void addItem(BaseItem item) {
		mItems.add(item);
		notifyDataSetChanged();
	}

	public static class BaseItem {
		private String mName;
		private String mGateId;
		private String mDeviceId;

		public BaseItem(String name, String gateId, String deviceId) {
			mName = name;
			mGateId = gateId;
			mDeviceId = deviceId;
		}

		public String getGateId() {
			return mGateId;
		}

		public String getDeviceId() {
			return mDeviceId;
		}

		public String getName() {
			return mName;
		}
	}

	public static class GraphItem extends BaseItem {
		private List<String> mModules;

		@DataRange
		private int mDataRange;

		public GraphItem(String name, String gateId, String deviceId, List<String> modules, @DataRange int range) {
			super(name, gateId, deviceId);

			mModules = modules;
			mDataRange = range;
		}

		public List<String> getModules() {
			return mModules;
		}

		@DataRange
		public int getDataRange() {
			return mDataRange;
		}
	}

	public static class ActualValueItem extends BaseItem {

		private String mModuleId;

		public ActualValueItem(String name, String gateId, String deviceId, String moduleId) {
			super(name, gateId, deviceId);

			mModuleId = moduleId;
		}

		public String getModuleId() {
			return mModuleId;
		}
	}

}
