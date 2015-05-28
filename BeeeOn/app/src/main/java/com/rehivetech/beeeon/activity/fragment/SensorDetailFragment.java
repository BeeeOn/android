package com.rehivetech.beeeon.activity.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.avast.android.dialogs.fragment.ListDialogFragment;
import com.avast.android.dialogs.iface.IListDialogListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.BaseSeries;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.point.DataPoint;
import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.SensorDetailActivity;
import com.rehivetech.beeeon.activity.SensorEditActivity;
import com.rehivetech.beeeon.activity.dialog.NumberPickerDialogFragment;
import com.rehivetech.beeeon.asynctask.ActorActionTask;
import com.rehivetech.beeeon.asynctask.CallbackTask.CallbackTaskListener;
import com.rehivetech.beeeon.asynctask.GetModuleLogTask;
import com.rehivetech.beeeon.asynctask.ReloadAdapterDataTask;
import com.rehivetech.beeeon.base.BaseApplicationFragment;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.device.ModuleLog.DataInterval;
import com.rehivetech.beeeon.household.device.ModuleLog.DataType;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.values.BaseEnumValue;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.BoilerOperationModeValue;
import com.rehivetech.beeeon.household.device.values.BoilerOperationTypeValue;
import com.rehivetech.beeeon.household.device.values.BooleanValue;
import com.rehivetech.beeeon.household.device.values.OnOffValue;
import com.rehivetech.beeeon.household.device.values.OpenClosedValue;
import com.rehivetech.beeeon.household.device.values.TemperatureValue;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.pair.LogDataPair;
import com.rehivetech.beeeon.util.GraphViewHelper;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class SensorDetailFragment extends BaseApplicationFragment implements IListDialogListener {

	private static final int REQUEST_BOILER_TYPE = 7894;
	private static final int REQUEST_BOILER_MODE = 1236;
	private Controller mController;
	private static final String TAG = SensorDetailFragment.class.getSimpleName();

	public static final String ARG_SEN_ID = "sensorid";
	public static final String ARG_CUR_PAGE = "currentpage";
	public static final String ARG_SEL_PAGE = "selectedpage";
	public static final String ARG_LOC_ID = "locationid";
	public static final String ARG_ADAPTER_ID = "adapterid";

	private SensorDetailActivity mActivity;

	// GUI elements
	private TextView mName;
	private TextView mLocation;
	private ImageView mLocationIcon;
	private TextView mValue;
	private SwitchCompat mValueSwitch;
	private TextView mTime;
	private ImageView mIcon;
	private TextView mRefreshTimeText;
	private FloatingActionButton mFABedit;
	private GraphView mGraphView;
	private TextView mBattery;
	private TextView mSignal;

	private Module mModule;
	private Adapter mAdapter;

	private UnitsHelper mUnitsHelper;
	private TimeHelper mTimeHelper;

	private String mModuleID;
	private String mLocationID;
	private int mCurPageNumber;
	private int mSelPageNumber;
	private String mAdapterId;


	private BaseSeries<DataPoint>  mGraphSeries;
	

	private static final String GRAPH_DATE_TIME_FORMAT = "dd.MM. kk:mm";
	private static final String LOG_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private SwipeRefreshLayout mSwipeLayout;
	private View mView;
	private SensorDetailActivity.ScreenSlidePagerAdapter mFragmentAdapter;

	private Button mValueSet;

	public SensorDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "OnCreate - Here 1 " + mCurPageNumber);
		mController = Controller.getInstance(mActivity);
		mAdapter = mController.getAdaptersModel().getAdapter(mAdapterId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "OnCreateView");
		mView = inflater.inflate(R.layout.activity_sensor_detail_screen, container, false);
		Log.d(TAG, String.format("this position: %s , selected item: %s ", mCurPageNumber, mSelPageNumber));
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(savedInstanceState != null) {
			mModuleID = savedInstanceState.getString(ARG_SEN_ID);
			mAdapterId = savedInstanceState.getString(ARG_ADAPTER_ID);
			mLocationID = savedInstanceState.getString(ARG_LOC_ID);
			mSelPageNumber = savedInstanceState.getInt(ARG_SEL_PAGE);
			mCurPageNumber = savedInstanceState.getInt(ARG_CUR_PAGE);
			mAdapter = mController.getAdaptersModel().getAdapter(mAdapterId);
		}
		Log.d(TAG, "OnActivityCreated");
		mModule = mController.getDevicesModel().getModule(mAdapterId, mModuleID);
		if (mModule != null) {
			Log.d(TAG, String.format("ID: %s, Name: %s", mModule.getId(), mModule.getName()));
			initLayout(mModule);
		}

		Log.d(TAG, "Here 3 " + mCurPageNumber);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(ARG_SEN_ID, mModuleID);
		savedInstanceState.putString(ARG_ADAPTER_ID,mAdapterId);
		savedInstanceState.putString(ARG_LOC_ID,mLocationID);
		savedInstanceState.putInt(ARG_CUR_PAGE, mCurPageNumber);
		savedInstanceState.putInt(ARG_SEL_PAGE,mSelPageNumber);
		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			Log.d(TAG,"This fragment is visible - dev "+ mModuleID);
			doReloadDevicesTask(mAdapterId, false);
		}

	}

	private void initLayout(Module module) {
		Log.d(TAG,"INIT LAYOUT");
		// Get View for sensor name
		mName = (TextView) mView.findViewById(R.id.sen_detail_name);
		// Get View for sensor location
		mLocation = (TextView) mView.findViewById(R.id.sen_detail_loc_name);
		mLocationIcon = (ImageView) mView.findViewById(R.id.sen_detail_loc_icon);
		// Get View for sensor value
		mValue = (TextView) mView.findViewById(R.id.sen_detail_value);
		mValueSwitch = (SwitchCompat) mView.findViewById(R.id.sen_detail_value_switch);
		mValueSet = (Button) mView.findViewById(R.id.sen_detail_value_set);
		// Get FAB for edit
		mFABedit = (FloatingActionButton) mView.findViewById(R.id.sen_detail_edit_fab);
		// Get View for sensor time
		mTime = (TextView) mView.findViewById(R.id.sen_detail_time);
		// Get Image for sensor
		mIcon = (ImageView) mView.findViewById(R.id.sen_detail_icon);
		// Get TextView for refresh time
		mRefreshTimeText = (TextView) mView.findViewById(R.id.sen_refresh_time_value);
		// Get battery value
		mBattery = (TextView) mView.findViewById(R.id.sen_detail_battery_value);
		// Get signal value
		mSignal = (TextView) mView.findViewById(R.id.sen_detail_signal_value);
		// Get graphView
		mGraphView = (GraphView) mView.findViewById(R.id.sen_graph);

		// Set title selected for animation if is text long
		mName.setSelected(true);
		mLocation.setSelected(true);


		mFABedit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// go to edit senzor
				Log.d(TAG,"Click - edit senzor");
				Intent intent = new Intent(mActivity, SensorEditActivity.class);
				intent.putExtra(Constants.GUI_EDIT_SENSOR_ID, mModuleID);
				mActivity.startActivityForResult(intent, Constants.EDIT_SENSOR_REQUEST_CODE);
			}
		});


		// Set name of sensor
		mName.setText(module.getName());
		mName.setBackgroundColor(Color.TRANSPARENT);
		if(mController.isUserAllowed(mAdapter.getRole())) {

		}
		
		if(mController.isUserAllowed(mAdapter.getRole())) {
			// Set value for Actor
			mValueSwitch.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// Disable button
					mValueSwitch.setEnabled(false);
					doActorAction(mModule);
				}
			});
			final Fragment frg = this;
			if(mModule.getValue() instanceof TemperatureValue) {
				// Set listner for dialog with NumberPicker
				mValueSet.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.d(TAG, "SET TEMPERATURE");
						NumberPickerDialogFragment.show(mActivity, mModule,frg);
					}
				});

			}
			else if(mModule.getValue() instanceof BoilerOperationTypeValue){
				// Set dialog for set Type of  BOILER
				mValueSet.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.d(TAG,"SET BOILER TYPE");
						String[] tmp = new String[] {
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
								.setSelectedItem(((BoilerOperationTypeValue) mModule.getValue()).getActive().getId())
								.setRequestCode(REQUEST_BOILER_TYPE)
								.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE)
								.setConfirmButtonText(R.string.dialog_set_boiler_setaction)
								.setCancelButtonText(R.string.notification_cancel)
								.setTargetFragment(frg,REQUEST_BOILER_TYPE)
								.show();
					}
				});
			}
			else if (mModule.getValue() instanceof  BoilerOperationModeValue) {
				// Set dialog for set Mode of Boiler
				mValueSet.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.d(TAG,"SET BOILER MODE");
						String[] tmp = new String[] {
								getString(R.string.dev_boiler_operation_mode_value_automatic),
								getString(R.string.dev_boiler_operation_mode_value_manual),
								getString(R.string.dev_boiler_operation_mode_value_vacation)
						};

						ListDialogFragment
								.createBuilder(mActivity, mActivity.getSupportFragmentManager())
								.setTitle(getString(R.string.dialog_title_set_bioler_mode))
								.setItems(tmp)
								.setSelectedItem(((BoilerOperationModeValue) mModule.getValue()).getActive().getId())
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
		if (mController != null) {
			Location location = null;

			Adapter adapter = mController.getAdaptersModel().getAdapter(mAdapterId);
			if (adapter != null) {
				location = mController.getLocationsModel().getLocation(adapter.getId(), module.getDevice().getLocationId());
			}

			if (location != null) {
				mLocation.setText(location.getName());
				mLocationIcon.setImageResource(location.getIconResource());
			}
			if(mController.isUserAllowed(mAdapter.getRole())) {

			}
		} else {
			Log.e(TAG, "mController is null (this shouldn't happen)");
			mLocation.setText(module.getDevice().getLocationId());
		}

		Device device = module.getDevice();
		Adapter adapter = mController.getAdaptersModel().getAdapter(device.getAdapterId());

		// UserSettings can be null when user is not logged in!
		SharedPreferences prefs = mController.getUserSettings();

		mUnitsHelper = (prefs == null) ? null : new UnitsHelper(prefs, mActivity);
		mTimeHelper = (prefs == null) ? null : new TimeHelper(prefs);

		// Set value of sensor
		if (mUnitsHelper != null) {
			mValue.setText(mUnitsHelper.getStringValueUnit(module.getValue()));
			BaseValue val = mModule.getValue();
			if (val instanceof OnOffValue) {
				mValueSwitch.setChecked(((BooleanValue) val).isActive());
			}
		}

		// Set icon of sensor
		mIcon.setImageResource(module.getIconResource());

		// Set time of sensor
		if (mTimeHelper != null) {
			mTime.setText(mTimeHelper.formatLastUpdate(device.getLastUpdate(), adapter));
		}

		// Set refresh time Text
		mRefreshTimeText.setText(device.getRefresh().getStringInterval(mActivity));

		// Set battery
		mBattery.setText(device.getBattery() + "%");

		// Set signal
		mSignal.setText(device.getNetworkQuality()+"%");

		// Add Graph
		if (mUnitsHelper != null && mTimeHelper != null && mGraphView.getSeries().size() == 0) {
			DateTimeFormatter fmt = mTimeHelper.getFormatter(GRAPH_DATE_TIME_FORMAT, adapter);
			addGraphView(fmt, mUnitsHelper);
		}

		// Visible all elements
		visibleAllElements();
	}

	private void visibleAllElements() {
		Log.d(TAG,"VISIBLE ALL ELEMENTS");
		//HIDE progress
		mView.findViewById(R.id.sensor_progress).setVisibility(View.GONE);
		// VISIBLE other stuf
		mView.findViewById(R.id.sen_header).setVisibility(View.VISIBLE);
		mView.findViewById(R.id.sen_first_section).setVisibility(View.VISIBLE);
		mView.findViewById(R.id.sen_second_section).setVisibility(View.VISIBLE);
		mView.findViewById(R.id.sen_third_section).setVisibility(View.VISIBLE);
		mView.findViewById(R.id.sen_sep_1).setVisibility(View.VISIBLE);
		mView.findViewById(R.id.sen_sep_2).setVisibility(View.VISIBLE);


		// Show some controls if this module is an actor
		if (mModule.getType().isActor() && mController.isUserAllowed(mAdapter.getRole())) {
			BaseValue value = mModule.getValue();
			
			// For actor values of type on/off, open/closed we show switch button
			if (value instanceof OnOffValue || value instanceof OpenClosedValue ) {
				mValueSwitch.setVisibility(View.VISIBLE);
			}
			else if (value instanceof TemperatureValue || value instanceof BoilerOperationModeValue || value instanceof  BoilerOperationTypeValue) {
				mValueSet.setVisibility(View.VISIBLE);
			}
		}

		if(mController.isUserAllowed(mAdapter.getRole())) {
			mFABedit.setVisibility(View.VISIBLE);
		}

		// Init swipe-refreshig layout
		mSwipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.swipe_container);
		mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				Log.d(TAG, "Refreshing list of sensors");
				doReloadDevicesTask(mAdapterId, true);
			}
		});
		mSwipeLayout.setColorSchemeColors(R.color.beeeon_primary_cyan, R.color.beeeon_text_color, R.color.beeeon_secundary_pink);
	}

	private void addGraphView(final DateTimeFormatter fmt, final UnitsHelper unitsHelper) {
		// Create and set graphView
		GraphViewHelper.prepareGraphView(mGraphView, getView().getContext(), mModule, fmt, unitsHelper); // empty heading

		if (mModule.getValue() instanceof BaseEnumValue) {
			mGraphSeries = new BarGraphSeries<>(new DataPoint[]{new DataPoint(0, 0), new DataPoint(1,1)});
			((BarGraphSeries) mGraphSeries).setSpacing(30);
			mGraphView.setDrawPointer(false);
		} else {
			mGraphSeries =  new LineGraphSeries<>(new DataPoint[]{new DataPoint(0, 0), new DataPoint(1,1)});
			((LineGraphSeries)mGraphSeries).setBackgroundColor(getResources().getColor(R.color.alpha_blue));
			((LineGraphSeries)mGraphSeries).setDrawBackground(true);
			((LineGraphSeries) mGraphSeries).setThickness(2);
		}
		mGraphSeries.setColor(getResources().getColor(R.color.beeeon_primary_cyan));
		mGraphSeries.setTitle("Graph");

		// Add data series
		mGraphView.addSeries(mGraphSeries);

		// touch listener to disable swipe and refresh trough graph
		mGraphView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				int i = motionEvent.getAction();

				if (i == MotionEvent.ACTION_DOWN) {
					mActivity.setEnableSwipe(false);
					mSwipeLayout.setEnabled(false);
				} else if (i == MotionEvent.ACTION_UP) {
					mActivity.setEnableSwipe(true);
					mSwipeLayout.setEnabled(true);
				}
				return false;
			}
		});

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mActivity = (SensorDetailActivity) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must be subclass of SensorDetailActivity");
		}
	}

	public void fillGraph(ModuleLog log) {
		if (mGraphView == null) {
			return;
		}

		SortedMap<Long, Float> values = log.getValues();
		int size = values.size();
		DataPoint[] data = new DataPoint[size];

		Log.d(TAG, String.format("Filling graph with %d values. Min: %.1f, Max: %.1f", size, log.getMinimum(), log.getMaximum()));

		int i = 0;
		for (Entry<Long, Float> entry : values.entrySet()) {
			Long dateMillis = entry.getKey();
			float value = Float.isNaN(entry.getValue()) ? log.getMinimum() : entry.getValue();

			data[i++] = new DataPoint(dateMillis, value);

			// This shouldn't happen, only when some other thread changes this values object - can it happen?
			if (i >= size)
				break;
		}

		Log.d(TAG, "Filling graph finished");

		mGraphSeries.resetData(data);
		mGraphView.getViewport().setXAxisBoundsManual(true);
		if (values.size() > 100 && mGraphSeries instanceof BarGraphSeries) {
			mGraphView.getViewport().setMaxX(mGraphSeries.getHighestValueX());
			mGraphView.getViewport().setMinX(mGraphSeries.getHighestValueX() - TimeUnit.HOURS.toMillis(1));
		}

		mGraphView.setLoading(false);
		//mGraphView.animateY(2000);
		//mGraphInfo.setText(getView().getResources().getString(R.string.sen_detail_graph_info));
	}

	public void setSensorID(String id) {
		mModuleID = id;
	}

	public void setLocationID(String locationId) {
		mLocationID = locationId;
	}

	public void setPosition(int position) {
		mCurPageNumber = position;
	}

	public void setSelectedPosition(int mActiveModulePosition) {
		mSelPageNumber = mActiveModulePosition;
	}

	public void setAdapterID(String mActiveAdapterId) {
		mAdapterId = mActiveAdapterId;
	}

	public void setFragmentAdapter(SensorDetailActivity.ScreenSlidePagerAdapter screenSlidePagerAdapter) {
		mFragmentAdapter = screenSlidePagerAdapter;
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
			((BaseEnumValue)value).setNextValue();
		} else {
			Log.e(TAG, "We can't switch actor, which value isn't inherited from BaseEnumValue, yet");
			return;
		}

		ActorActionTask actorActionTask = new ActorActionTask(mActivity);
		actorActionTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				// Get new module
				mModule = mController.getDevicesModel().getModule(module.getDevice().getAdapterId(), module.getId());

				// Set icon of sensor
				mIcon.setImageResource(mModule.getIconResource());
				// Enable button
				mValueSwitch.setEnabled(true);
				mValue.setText(mUnitsHelper.getStringValueUnit(mModule.getValue()));
			}

		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(actorActionTask, module);
	}

	protected void doReloadDevicesTask(final String adapterId, final boolean forceRefresh) {
		ReloadAdapterDataTask reloadDevicesTask = new ReloadAdapterDataTask(mActivity, forceRefresh, ReloadAdapterDataTask.ReloadWhat.FACILITIES);

		reloadDevicesTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (mSwipeLayout != null) {
					mSwipeLayout.setRefreshing(false);
				}
				if (!success) {
					Log.d(TAG, "Fragment - Reload failed");
					return;
				}
				Log.d(TAG, "Fragment - Start reload task");
				mModule = mController.getDevicesModel().getModule(adapterId, mModuleID);
				if (mModule == null) {
					Log.d(TAG, "Fragment - Stop reload task");
					return;
				}
				initLayout(mModule);
				doLoadGraphData();
			}

		});

		// Remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(reloadDevicesTask, adapterId);
	}

	protected void doLoadGraphData() {
		DateTime end = DateTime.now(DateTimeZone.UTC);
		DateTime start = end.minusWeeks(1);

		DateTimeFormatter fmt = DateTimeFormat.forPattern(LOG_DATE_TIME_FORMAT).withZoneUTC();
		Log.d(TAG, String.format("Loading graph data from %s to %s.", fmt.print(start), fmt.print(end)));

		GetModuleLogTask getModuleLogTask = new GetModuleLogTask(mActivity);
		final LogDataPair pair = new LogDataPair( //
				mModule, // module
				new Interval(start, end), // interval from-to
				DataType.AVERAGE, // type
				(mModule.getValue() instanceof BaseEnumValue) ? DataInterval.RAW : DataInterval.TEN_MINUTES); // interval

		getModuleLogTask.setListener(new CallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				fillGraph(mController.getModuleLogsModel().getModuleLog(pair));
			}
		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(getModuleLogTask, pair);
	}

	protected void doChangeStateModuleTask(final Module module) {
		ActorActionTask changeStateModuleTask = new ActorActionTask(mActivity);

		changeStateModuleTask.setListener(new CallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success)
					mValue.setText(mUnitsHelper.getStringValueUnit(mModule.getValue()));
			}
		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(changeStateModuleTask, module);
	}

	@Override
	public void onListItemSelected(CharSequence value, int number, int requestCode) {
		if(requestCode == REQUEST_BOILER_MODE || requestCode == REQUEST_BOILER_TYPE) {
			Log.d(TAG,"RESULT - SET BOILDER MODE or TYPE val:"+value+" number:"+number);
			mModule.setValue(String.valueOf(number));
			doChangeStateModuleTask(mModule);
		}
	}

	public void onSetTemperatureClick(Double value) {
		Log.d(TAG, "SET TEMPERATURE DO TASK");
		mModule.setValue(String.valueOf(value));
		doChangeStateModuleTask(mModule);
	}
}
