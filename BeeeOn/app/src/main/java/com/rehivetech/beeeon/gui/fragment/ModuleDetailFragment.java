package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avast.android.dialogs.fragment.ListDialogFragment;
import com.avast.android.dialogs.iface.IListDialogListener;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.FillFormatter;
import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.ModuleDetailActivity;
import com.rehivetech.beeeon.gui.activity.ModuleEditActivity;
import com.rehivetech.beeeon.gui.dialog.NumberPickerDialogFragment;
import com.rehivetech.beeeon.gui.view.VerticalChartLegend;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.device.ModuleLog.DataInterval;
import com.rehivetech.beeeon.household.device.ModuleLog.DataType;
import com.rehivetech.beeeon.household.device.ModuleType;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask.ICallbackTaskListener;
import com.rehivetech.beeeon.threading.task.ActorActionTask;
import com.rehivetech.beeeon.threading.task.GetModuleLogTask;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.util.ChartHelper;
import com.rehivetech.beeeon.util.Log;
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

public class ModuleDetailFragment extends BaseApplicationFragment implements IListDialogListener {
	private static final String TAG = ModuleDetailFragment.class.getSimpleName();

	public static final String EXTRA_GATE_ID = "gate_id";
	public static final String EXTRA_MODULE_ID = "module_id";

	private static final int REQUEST_SET_ACTUATOR = 7894;

	public static final String ARG_CUR_PAGE = "currentpage";
	public static final String ARG_SEL_PAGE = "selectedpage";

	private ModuleDetailActivity mActivity;
	private TimeHelper mTimeHelper;

	// GUI elements
	private TextView mValue;
	private SwitchCompat mValueSwitch;
	private ImageView mIcon;
	private FloatingActionButton mFABedit;
	private CombinedChart mChart;
	private DataSet mDataSet;
	private VerticalChartLegend mLegend;

	private UnitsHelper mUnitsHelper;

	private String mGateId;
	private String mModuleId;

	private static final String GRAPH_DATE_TIME_FORMAT = "dd.MM. kk:mm";
	private static final String LOG_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private SwipeRefreshLayout mSwipeLayout;

	private Button mValueSet;

	public ModuleDetailFragment() {
	}

	public static ModuleDetailFragment newInstance(String gateId, String moduleId) {
		Bundle args = new Bundle();
		args.putString(EXTRA_GATE_ID, gateId);
		args.putString(EXTRA_MODULE_ID, moduleId);

		ModuleDetailFragment fragment = new ModuleDetailFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mActivity = (ModuleDetailActivity) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must be subclass of ModuleDetailActivity");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args == null || !args.containsKey(EXTRA_GATE_ID) || !args.containsKey(EXTRA_MODULE_ID)) {
			Log.e(TAG, "Not specified moduleId as Fragment argument");
			return;
		}

		mGateId = args.getString(EXTRA_GATE_ID);
		mModuleId = args.getString(EXTRA_MODULE_ID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_module_detail, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		doReloadDevicesTask(mGateId, false);
	}

	private void initLayout() {
		View view = getView();
		if(view == null){
			return;
		}

		// Get View for module location
		TextView location = (TextView) view.findViewById(R.id.module_detail_loc_name);
		ImageView locationIcon = (ImageView) view.findViewById(R.id.module_detail_loc_icon);
		// Get View for module value
		mValue = (TextView) view.findViewById(R.id.module_detail_header_value);
		mValueSwitch = (SwitchCompat) view.findViewById(R.id.module_detail_header_value_switch);
		mValueSet = (Button) view.findViewById(R.id.module_detail_header_value_set_button);
		// Get FAB for edit
		mFABedit = (FloatingActionButton) view.findViewById(R.id.module_detail_edit_fab);
		// Get View for module time
		TextView time = (TextView) view.findViewById(R.id.module_detail_time);
		// Get Image for module
		mIcon = (ImageView) view.findViewById(R.id.module_detail_header_icon);
		// Get TextView for refresh time
		TextView refreshTimeText = (TextView) view.findViewById(R.id.module_detail_refresh_time_value);
		// Get battery value
		TextView batteryText = (TextView) view.findViewById(R.id.module_detail_battery_value);
		// Get signal value
		TextView signalText = (TextView) view.findViewById(R.id.module_detail_signal_value);
		// Get chart view
		mChart = (CombinedChart) view.findViewById(R.id.module_detail_chart);

		// Set title selected for animation if is text long
		location.setSelected(true);

		mFABedit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// go to edit senzor
				Intent intent = new Intent(mActivity, ModuleEditActivity.class);
				intent.putExtra(ModuleEditActivity.EXTRA_GATE_ID, mGateId);
				intent.putExtra(ModuleEditActivity.EXTRA_MODULE_ID, mModuleId);
				mActivity.startActivity(intent);
			}
		});


		Controller controller = Controller.getInstance(mActivity);

		final Gate gate = controller.getGatesModel().getGate(mGateId);
		final Module module = controller.getDevicesModel().getModule(mGateId, mModuleId);

		if (gate == null || module == null) {
			Log.e(TAG, "Can't load gate or module.");
			return;
		}
		final Device device = module.getDevice();

		// Set name of module
		Toolbar toolbar = (Toolbar) mActivity.findViewById(R.id.beeeon_toolbar);
		toolbar.setTitle(module.getName(mActivity));

		if (controller.isUserAllowed(gate.getRole())) {
			// Set value for Actor
			mValueSwitch.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// Disable button
					mValueSwitch.setEnabled(false);
					doActorAction(module);
				}
			});

			if (module.getValue() instanceof EnumValue) {
				mValueSet.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						EnumValue value = (EnumValue) module.getValue();
						List<EnumValue.Item> items = value.getEnumItems();

						List<String> namesList = new ArrayList<>();
						for (EnumValue.Item item : items) {
							namesList.add(getString(item.getStringResource()));
						}

						ListDialogFragment
								.createBuilder(mActivity, mActivity.getSupportFragmentManager())
								.setTitle(getString(R.string.number_picker_dialog_dialog_title_actuator_set_value))
								.setItems(namesList.toArray(new CharSequence[namesList.size()]))
								.setSelectedItem(value.getActive().getId())
								.setRequestCode(REQUEST_SET_ACTUATOR)
								.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE)
								.setConfirmButtonText(R.string.activity_fragment_btn_set)
								.setCancelButtonText(R.string.activity_fragment_btn_cancel)
								.setTargetFragment(ModuleDetailFragment.this, REQUEST_SET_ACTUATOR)
								.show();
					}
				});
			} else {
				// FIXME: support all kinds of value types and units etc.
				// BaseValue value = mModule.getValue();

				mValueSet.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.d(TAG, "SET TEMPERATURE");
						NumberPickerDialogFragment.show(mActivity, module, ModuleDetailFragment.this);
					}
				});
			}
		}

		// Set name of location
		Location tmp_location = controller.getLocationsModel().getLocation(gate.getId(), module.getDevice().getLocationId());
		if (tmp_location != null) {
			location.setText(tmp_location.getName());
			locationIcon.setImageResource(tmp_location.getIconResource());
		}

		// UserSettings can be null when user is not logged in!
		SharedPreferences prefs = controller.getUserSettings();
		if (prefs == null) {
			// Can't continue without preferences
			Log.e(TAG, "User is not logged in, getUserSettings() return null");
			return;
		}

		mUnitsHelper = new UnitsHelper(prefs, mActivity);
		mTimeHelper = new TimeHelper(prefs);

		// Set value of module
		mValue.setText(mUnitsHelper.getStringValueUnit(module.getValue()));
		// FIXME: rework this better
		if (module.getValue() instanceof EnumValue) {
			EnumValue value = (EnumValue) module.getValue();
			List<EnumValue.Item> items = value.getEnumItems();
			if (items.size() == 2) {
				int index = items.indexOf(value.getActive());
				mValueSwitch.setChecked(index == 1);
			}
		}

		// Set icon of module
		mIcon.setImageResource(module.getIconResource(IconResourceType.WHITE));

		// Set time of module
		if (mTimeHelper != null) {
			time.setText(mTimeHelper.formatLastUpdate(device.getLastUpdate(), gate));
		}

		// Set refresh time Text
		RefreshInterval refresh = device.getRefresh();
		if (refresh != null) {
			refreshTimeText.setText(refresh.getStringInterval(mActivity));
		}

		// Set battery
		Integer battery = device.getBattery();
		if (battery != null) {
			batteryText.setText(battery + "%");
		}

		// Set signal
		Integer rssi = device.getRssi();
		if (rssi != null) {
			signalText.setText(rssi + "%");
		}

		// Add Graph
		addGraphView(module);

		// Visible all elements
		visibleAllElements(module, gate);
	}

	private void visibleAllElements(@NonNull Module module, @NonNull Gate gate) {
		Controller controller = Controller.getInstance(mActivity);

		View view = getView();

		//HIDE progress
		view.findViewById(R.id.module_detail_progress_layout).setVisibility(View.GONE);
		// VISIBLE other stuff
		view.findViewById(R.id.module_detail_header_layout).setVisibility(View.VISIBLE);
		view.findViewById(R.id.module_detail_first_section_layout).setVisibility(View.VISIBLE);
		view.findViewById(R.id.module_detail_second_section_layout).setVisibility(View.VISIBLE);
		view.findViewById(R.id.module_detail_third_section_layout).setVisibility(View.VISIBLE);


		// Show some controls if this module is an actor
		if (module.isActuator() && controller.isUserAllowed(gate.getRole())) {
			BaseValue value = module.getValue();

			// FIXME: rework this better
			// Show specific control view
			if (value instanceof EnumValue && ((EnumValue) value).getEnumItems().size() == 2) {
				// For enum actuators with 2 values show a switch Button
				mValueSwitch.setVisibility(View.VISIBLE);
			} else {
				// For other actuators show value set button
				mValueSet.setVisibility(View.VISIBLE);
			}
		}

		if (controller.isUserAllowed(gate.getRole())) {
			mFABedit.setVisibility(View.VISIBLE);
		}

		// Init swipe-refreshig layout
		mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.module_detail_swipe_layout);
		mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				doReloadDevicesTask(mGateId, true);
			}
		});
		mSwipeLayout.setColorSchemeColors(R.color.beeeon_primary, R.color.beeeon_primary_text, R.color.beeeon_accent);
	}

	private void addGraphView(@NonNull final Module module) {
		Controller controller = Controller.getInstance(mActivity);
		BaseValue baseValue = module.getValue();
		boolean barchart = baseValue instanceof EnumValue;
		LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.module_detail_third_section_layout);
		String unit = mUnitsHelper.getStringUnit(baseValue);
		String name = getString(module.getTypeStringResource());

		//set chart
		ChartHelper.prepareChart(mChart, mActivity, baseValue, layout, controller);
		mChart.setFillFormatter(new CustomFillFormatter());


		if (barchart) {
			mDataSet = new BarDataSet(new ArrayList<BarEntry>(), name);
		} else {


			mDataSet = new LineDataSet(new ArrayList<com.github.mikephil.charting.data.Entry>(),String.format("%s [%s]",name, unit));
		}
		//set dataset style
		ChartHelper.prepareDataSet(mDataSet,  barchart, true, getResources().getColor(R.color.beeeon_primary_medium));

		int viewCount  = layout.getChildCount();
		View view = layout.getChildAt(viewCount - 1);
		if (!(view instanceof VerticalChartLegend)) {
//			set legend title
			int padding = getResources().getDimensionPixelOffset(R.dimen.customview_text_padding);
			TextView legendTitle = new TextView(mActivity);
			legendTitle.setTextAppearance(mActivity, R.style.TextAppearance_AppCompat_Subhead);
			legendTitle.setText(getString(R.string.fragment_module_detail_custom_view_chart_legend));
			legendTitle.setPadding(0, padding, 0, padding);
			layout.addView(legendTitle);

			//set legend
			mLegend = new VerticalChartLegend(mActivity);
			mLegend.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			layout.addView(mLegend);
			layout.invalidate();
		}
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
			barEntries = ((BarDataSet)mDataSet).getYVals();
		} else {
			lineEntries = ((LineDataSet)mDataSet).getYVals();
		}
		DateTimeFormatter fmt = mTimeHelper.getFormatter(GRAPH_DATE_TIME_FORMAT, gate);

		Log.d(TAG, String.format("Filling graph with %d values. Min: %.1f, Max: %.1f", size, log.getMinimum(), log.getMaximum()));

		int i = 0;
		for (Map.Entry<Long, Float> entry : values.entrySet()) {
			Long dateMillis = entry.getKey();
			float value = Float.isNaN(entry.getValue()) ? log.getMinimum() : entry.getValue();
			xVals.add(fmt.print(dateMillis));
			if (barGraph) {
				barEntries.add(new BarEntry(value,i++));
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
			BarData  barData = new BarData(xVals, (BarDataSet)mDataSet);
			data.setData(barData);
		} else {
			if (mDataSet.getYVals().size() < 50) {
				((LineDataSet) mDataSet).setDrawCubic(true);
				((LineDataSet) mDataSet).setCubicIntensity(0.05f);
			}
			LineData lineData = new LineData(xVals, (LineDataSet)mDataSet);
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

		mActivity.findViewById(R.id.module_detail_third_section_layout).invalidate();
	}
	/*
	 * ================================= ASYNC TASK ===========================
	 */

	protected void doActorAction(final Module module) {
		if (!module.isActuator()) {
			return;
		}

		// SET NEW VALUE
		BaseValue value = module.getValue();
		if (value instanceof EnumValue) {
			((EnumValue) value).setNextValue();
		} else {
			Log.e(TAG, "We can't switch actor, which value isn't inherited from EnumValue, yet");
			return;
		}

		ActorActionTask actorActionTask = new ActorActionTask(mActivity);
		actorActionTask.setListener(new ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				// Get new module
				Module module = Controller.getInstance(mActivity).getDevicesModel().getModule(mGateId, mModuleId);

				// Set new data
				mIcon.setImageResource(module.getIconResource(IconResourceType.WHITE));
				mValueSwitch.setEnabled(true);
				mValue.setText(mUnitsHelper.getStringValueUnit(module.getValue()));
			}

		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(actorActionTask, module);
	}

	protected void doReloadDevicesTask(final String gateId, final boolean forceRefresh) {
		ReloadGateDataTask reloadDevicesTask = new ReloadGateDataTask(mActivity, forceRefresh, ReloadGateDataTask.ReloadWhat.DEVICES);

		reloadDevicesTask.setListener(new ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (mSwipeLayout != null) {
					mSwipeLayout.setRefreshing(false);
				}
				if (success) {
					initLayout();
					doLoadGraphData();
				}
			}
		});

		// Remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(reloadDevicesTask, gateId);
	}

	protected void doLoadGraphData() {
		final Module module = Controller.getInstance(mActivity).getDevicesModel().getModule(mGateId, mModuleId);
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
				DataType.AVERAGE, // type
				(module.getValue() instanceof EnumValue) ? DataInterval.RAW : DataInterval.TEN_MINUTES); // interval

		getModuleLogTask.setListener(new ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				fillGraph(Controller.getInstance(mActivity).getModuleLogsModel().getModuleLog(pair),module);
			}
		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(getModuleLogTask, pair);
	}

	protected void doChangeStateModuleTask(final Module module) {
		ActorActionTask changeStateModuleTask = new ActorActionTask(mActivity);

		changeStateModuleTask.setListener(new ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					mValue.setText(mUnitsHelper.getStringValueUnit(module.getValue()));
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(changeStateModuleTask, module);
	}

	@Override
	public void onListItemSelected(CharSequence value, int number, int requestCode) {
		if (requestCode == REQUEST_SET_ACTUATOR || requestCode == REQUEST_SET_ACTUATOR) {
			Module module = Controller.getInstance(mActivity).getDevicesModel().getModule(mGateId, mModuleId);
			if (module == null) {
				Log.e(TAG, "Can't load module for changing its value");
				return;
			}

			module.setValue(String.valueOf(number));
			doChangeStateModuleTask(module);
		}
	}

	public void onSetTemperatureClick(Double value) {
		Module module = Controller.getInstance(mActivity).getDevicesModel().getModule(mGateId, mModuleId);
		if (module == null) {
			Log.e(TAG, "Can't load module for changing its value");
			return;
		}

		module.setValue(String.valueOf(value));
		doChangeStateModuleTask(module);
	}

	/**
	 * Custom fill formatter which allow fill chart from bottom
	 */
	protected class CustomFillFormatter implements FillFormatter {

		@Override
		public float getFillLinePosition(LineDataSet dataSet, LineData data, float chartMaxY, float chartMinY) {
			return chartMinY;
		}
	}
}
