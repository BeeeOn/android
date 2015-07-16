package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import com.rehivetech.beeeon.household.device.values.BaseEnumValue;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.BoilerOperationModeValue;
import com.rehivetech.beeeon.household.device.values.BoilerOperationTypeValue;
import com.rehivetech.beeeon.household.device.values.BooleanValue;
import com.rehivetech.beeeon.household.device.values.OnOffValue;
import com.rehivetech.beeeon.household.device.values.OpenClosedValue;
import com.rehivetech.beeeon.household.device.values.TemperatureValue;
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


	private static final int REQUEST_BOILER_TYPE = 7894;
	private static final int REQUEST_BOILER_MODE = 1236;

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
					+ " must be subclass of SensorDetailActivity");
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
		return inflater.inflate(R.layout.activity_sensor_detail_screen, container, false);
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

		// Get View for sensor location
		TextView location = (TextView) view.findViewById(R.id.sen_detail_loc_name);
		ImageView locationIcon = (ImageView) view.findViewById(R.id.sen_detail_loc_icon);
		// Get View for sensor value
		mValue = (TextView) view.findViewById(R.id.sen_detail_value);
		mValueSwitch = (SwitchCompat) view.findViewById(R.id.sen_detail_value_switch);
		mValueSet = (Button) view.findViewById(R.id.sen_detail_value_set);
		// Get FAB for edit
		mFABedit = (FloatingActionButton) view.findViewById(R.id.sen_detail_edit_fab);
		// Get View for sensor time
		TextView time = (TextView) view.findViewById(R.id.sen_detail_time);
		// Get Image for sensor
		mIcon = (ImageView) view.findViewById(R.id.sen_detail_icon);
		// Get TextView for refresh time
		TextView refreshTimeText = (TextView) view.findViewById(R.id.sen_refresh_time_value);
		// Get battery value
		TextView battery = (TextView) view.findViewById(R.id.sen_detail_battery_value);
		// Get signal value
		TextView signal = (TextView) view.findViewById(R.id.sen_detail_signal_value);
		// Get chart view
		mChart = (CombinedChart) view.findViewById(R.id.sen_graph);

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

		// Set name of sensor
		Toolbar toolbar = (Toolbar) mActivity.findViewById(R.id.toolbar);
		toolbar.setTitle(module.getName());

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
			final Fragment frg = this;
			if (module.getValue() instanceof TemperatureValue) {
				// Set listner for dialog with NumberPicker
				mValueSet.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						NumberPickerDialogFragment.show(mActivity, module, frg);
					}
				});

			} else if (module.getValue() instanceof BoilerOperationTypeValue) {
				// Set dialog for set Type of  BOILER
				mValueSet.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String[] tmp = new String[]{
								getString(R.string.dev_boiler_operation_type_value_off),
								getString(R.string.dev_boiler_operation_type_value_room),
								getString(R.string.dev_boiler_operation_type_value_equiterm),
								getString(R.string.dev_boiler_operation_type_value_stable),
								getString(R.string.dev_boiler_operation_type_value_tuv),
						};

						ListDialogFragment
								.createBuilder(mActivity, mActivity.getSupportFragmentManager())
								.setTitle(getString(R.string.dialog_title_set_bioler_type))
								.setItems(tmp)
								.setSelectedItem(((BoilerOperationTypeValue) module.getValue()).getActive().getId())
								.setRequestCode(REQUEST_BOILER_TYPE)
								.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE)
								.setConfirmButtonText(R.string.dialog_set_boiler_setaction)
								.setCancelButtonText(R.string.notification_cancel)
								.setTargetFragment(frg, REQUEST_BOILER_TYPE)
								.show();
					}
				});
			} else if (module.getValue() instanceof BoilerOperationModeValue) {
				// Set dialog for set Mode of Boiler
				mValueSet.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String[] tmp = new String[]{
								getString(R.string.dev_boiler_operation_mode_value_automatic),
								getString(R.string.dev_boiler_operation_mode_value_manual),
								getString(R.string.dev_boiler_operation_mode_value_vacation)
						};

						ListDialogFragment
								.createBuilder(mActivity, mActivity.getSupportFragmentManager())
								.setTitle(getString(R.string.dialog_title_set_bioler_mode))
								.setItems(tmp)
								.setSelectedItem(((BoilerOperationModeValue) module.getValue()).getActive().getId())
								.setRequestCode(REQUEST_BOILER_MODE)
								.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE)
								.setConfirmButtonText(R.string.dialog_set_boiler_setaction)
								.setCancelButtonText(R.string.notification_cancel)
								.setTargetFragment(frg, REQUEST_BOILER_MODE)
								.show();
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

		mUnitsHelper = (prefs == null) ? null : new UnitsHelper(prefs, mActivity);
		mTimeHelper = (prefs == null) ? null : new TimeHelper(prefs);

		// Set value of sensor
		if (mUnitsHelper != null) {
			mValue.setText(mUnitsHelper.getStringValueUnit(module.getValue()));
			BaseValue val = module.getValue();
			if (val instanceof OnOffValue) {
				mValueSwitch.setChecked(((BooleanValue) val).isActive());
			}
		}

		// Set icon of sensor
		mIcon.setImageResource(module.getIconResource(IconResourceType.WHITE));

		// Set time of sensor
		if (mTimeHelper != null) {
			time.setText(mTimeHelper.formatLastUpdate(device.getLastUpdate(), gate));
		}

		// Set refresh time Text
		refreshTimeText.setText(device.getRefresh().getStringInterval(mActivity));

		// Set battery
		battery.setText(device.getBattery() + "%");

		// Set signal
		signal.setText(device.getNetworkQuality() + "%");

		// Add Graph
		if (mUnitsHelper != null && mTimeHelper != null) {
			addGraphView(module);
		}

		// Visible all elements
		visibleAllElements(module, gate);
	}

	private void visibleAllElements(@NonNull Module module, @NonNull Gate gate) {
		Controller controller = Controller.getInstance(mActivity);

		View view = getView();

		//HIDE progress
		view.findViewById(R.id.sensor_progress).setVisibility(View.GONE);
		// VISIBLE other stuff
		view.findViewById(R.id.sen_header).setVisibility(View.VISIBLE);
		view.findViewById(R.id.sen_first_section).setVisibility(View.VISIBLE);
		view.findViewById(R.id.sen_second_section).setVisibility(View.VISIBLE);
		view.findViewById(R.id.sen_third_section).setVisibility(View.VISIBLE);


		// Show some controls if this module is an actor
		if (module.getType().isActor() && controller.isUserAllowed(gate.getRole())) {
			BaseValue value = module.getValue();

			// For actor values of type on/off, open/closed we show switch button
			if (value instanceof OnOffValue || value instanceof OpenClosedValue) {
				mValueSwitch.setVisibility(View.VISIBLE);
			} else if (value instanceof TemperatureValue || value instanceof BoilerOperationModeValue || value instanceof BoilerOperationTypeValue) {
				mValueSet.setVisibility(View.VISIBLE);
			}
		}

		if (controller.isUserAllowed(gate.getRole())) {
			mFABedit.setVisibility(View.VISIBLE);
		}

		// Init swipe-refreshig layout
		mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
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
		boolean barchart = baseValue instanceof BaseEnumValue;
		LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.sen_third_section);
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
			legendTitle.setText(getString(R.string.chart_legend));
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
		boolean barGraph = (module.getValue() instanceof BaseEnumValue);
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

		mActivity.findViewById(R.id.sen_third_section).invalidate();
	}
	/*
	 * ================================= ASYNC TASK ===========================
	 */

	protected void doActorAction(final Module module) {
		if (!module.getType().isActor()) {
			return;
		}

		// SET NEW VALUE
		BaseValue value = module.getValue();
		if (value instanceof BaseEnumValue) {
			((BaseEnumValue) value).setNextValue();
		} else {
			Log.e(TAG, "We can't switch actor, which value isn't inherited from BaseEnumValue, yet");
			return;
		}

		ActorActionTask actorActionTask = new ActorActionTask(mActivity);
		actorActionTask.setListener(new ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
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
				(module.getValue() instanceof BaseEnumValue) ? DataInterval.RAW : DataInterval.TEN_MINUTES); // interval

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
		if (requestCode == REQUEST_BOILER_MODE || requestCode == REQUEST_BOILER_TYPE) {
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
