package com.rehivetech.beeeon.activity.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.BaseSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.SensorDetailActivity;
import com.rehivetech.beeeon.activity.SensorEditActivity;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceLog;
import com.rehivetech.beeeon.household.device.DeviceLog.DataInterval;
import com.rehivetech.beeeon.household.device.DeviceLog.DataType;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.device.values.BaseEnumValue;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.OnOffValue;
import com.rehivetech.beeeon.household.device.values.OpenClosedValue;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.asynctask.ActorActionTask;
import com.rehivetech.beeeon.asynctask.CallbackTask.CallbackTaskListener;
import com.rehivetech.beeeon.asynctask.GetDeviceLogTask;
import com.rehivetech.beeeon.asynctask.ReloadFacilitiesTask;
import com.rehivetech.beeeon.asynctask.SaveDeviceTask;
import com.rehivetech.beeeon.asynctask.SaveFacilityTask;
import com.rehivetech.beeeon.controller.Controller;
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

public class SensorDetailFragment extends Fragment {

	private Controller mController;
	private static final String TAG = SensorDetailFragment.class.getSimpleName();
	private static final int EDIT_NONE = 0;

	public static final String ARG_SEN_ID = "sensorid";
	public static final String ARG_CUR_PAGE = "currentpage";
	public static final String ARG_SEL_PAGE = "selectedpage";
	public static final String ARG_LOC_ID = "locationid";
	public static final String ARG_ADAPTER_ID = "adapterid";

	// GUI elements
	private TextView mName;
	private TextView mLocation;
	private TextView mValue;
	private SwitchCompat mValueSwitch;
	private TextView mTime;
	private ImageView mIcon;
	private TextView mRefreshTimeText;
	private FloatingActionButton mFABedit;
	private TextView mGraphLabel;
	private LinearLayout mGraphLayout;
	private GraphView mGraphView;
	private TextView mBattery;
	private TextView mSignal;

	private SensorDetailActivity mActivity;

	private Device mDevice;
	private Adapter mAdapter;
	
	private SaveDeviceTask mSaveDeviceTask;
	private GetDeviceLogTask mGetDeviceLogTask;
	private SaveFacilityTask mSaveFacilityTask;
	private ActorActionTask mActorActionTask;
	
	private UnitsHelper mUnitsHelper;
	private TimeHelper mTimeHelper;

	private String mDeviceID;
	private String mLocationID;
	private int mCurPageNumber;
	private int mSelPageNumber;
	private String mAdapterId;

	private boolean mWasTapGraph = false;
	private int mEditMode = EDIT_NONE;

	private BaseSeries<DataPoint>  mGraphSeries;
	

	private static final String GRAPH_DATE_TIME_FORMAT = "dd.MM. kk:mm";
	private static final String LOG_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private SwipeRefreshLayout mSwipeLayout;
	private View mView;
	private SensorDetailActivity.ScreenSlidePagerAdapter mFragmentAdapter;
	private ReloadFacilitiesTask mReloadFacilitiesTask;


	public SensorDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "OnCreate - Here 1 " + mCurPageNumber);
		mActivity = (SensorDetailActivity) getActivity();
		mController = Controller.getInstance(mActivity);
		mAdapter = mController.getAdapter(mAdapterId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG,"OnCreateView");
		mView = inflater.inflate(R.layout.activity_sensor_detail_screen, container, false);
		Log.d(TAG, String.format("this position: %s , selected item: %s ", mCurPageNumber, mSelPageNumber));
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(savedInstanceState != null) {
			mDeviceID = savedInstanceState.getString(ARG_SEN_ID);
			mAdapterId = savedInstanceState.getString(ARG_ADAPTER_ID);
			mLocationID = savedInstanceState.getString(ARG_LOC_ID);
			mSelPageNumber = savedInstanceState.getInt(ARG_SEL_PAGE);
			mCurPageNumber = savedInstanceState.getInt(ARG_CUR_PAGE);
			mAdapter = mController.getAdapter(mAdapterId);
			mActivity = (SensorDetailActivity) getActivity();
		}
		Log.d(TAG,"OnActivityCreated");
		mDevice = mController.getDevice(mAdapterId, mDeviceID);
		if (mDevice != null) {
			Log.d(TAG, String.format("ID: %s, Name: %s", mDevice.getId(), mDevice.getName()));
			initLayout(mDevice);
		}

		Log.d(TAG, "Here 3 " + mCurPageNumber);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(ARG_SEN_ID,mDeviceID);
		savedInstanceState.putString(ARG_ADAPTER_ID,mAdapterId);
		savedInstanceState.putString(ARG_LOC_ID,mLocationID);
		savedInstanceState.putInt(ARG_CUR_PAGE,mCurPageNumber);
		savedInstanceState.putInt(ARG_SEL_PAGE,mSelPageNumber);
		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			Log.d(TAG,"This fragment is visible - dev "+ mDeviceID);
			doReloadFacilitiesTask(mAdapterId, false);
		}

	}

	@Override
	public void onStop() {
		if (mSaveDeviceTask != null) {
			mSaveDeviceTask.cancel(true);
		}
		if (mGetDeviceLogTask != null) {
			mGetDeviceLogTask.cancel(true);
		}
		super.onStop();
	}

	private void initLayout(Device device) {
		// Get View for sensor name
		mName = (TextView) mView.findViewById(R.id.sen_detail_name);
		// Get View for sensor location
		mLocation = (TextView) mView.findViewById(R.id.sen_detail_loc_name);
		// Get View for sensor value
		mValue = (TextView) mView.findViewById(R.id.sen_detail_value);
		mValueSwitch = (SwitchCompat) mView.findViewById(R.id.sen_detail_value_switch);
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
				intent.putExtra(Constants.GUI_EDIT_SENSOR_ID, mDeviceID);
				mActivity.startActivityForResult(intent, Constants.EDIT_SENSOR_REQUEST_CODE);
			}
		});


		// Set name of sensor
		mName.setText(device.getName());
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
					doActorAction(mDevice);
				}
			});

		}
		// Set name of location
		if (mController != null) {
			Location location = null;

			Adapter adapter = mController.getAdapter(mAdapterId);
			if (adapter != null) {
				location = mController.getLocation(adapter.getId(), device.getFacility().getLocationId());
			}

			if (location != null) {
				mLocation.setText(location.getName());
			}
			if(mController.isUserAllowed(mAdapter.getRole())) {

			}
		} else {
			Log.e(TAG, "mController is null (this shouldn't happen)");
			mLocation.setText(device.getFacility().getLocationId());
		}

		Facility facility = device.getFacility();
		Adapter adapter = mController.getAdapter(facility.getAdapterId());

		// UserSettings can be null when user is not logged in!
		SharedPreferences prefs = mController.getUserSettings();

		mUnitsHelper = (prefs == null) ? null : new UnitsHelper(prefs, getActivity().getApplicationContext());
		mTimeHelper = (prefs == null) ? null : new TimeHelper(prefs);

		// Set value of sensor
		if (mUnitsHelper != null) {
			mValue.setText(mUnitsHelper.getStringValueUnit(device.getValue()));
			BaseValue val = mDevice.getValue();
			if (val instanceof OnOffValue) {
				boolean isOn = ((OnOffValue) val).isActiveValue(OnOffValue.ON);
				mValueSwitch.setChecked(isOn);
			}

		}

		// Set icon of sensor
		mIcon.setImageResource(device.getIconResource());

		// Set time of sensor
		if (mTimeHelper != null) {
			mTime.setText(mTimeHelper.formatLastUpdate(facility.getLastUpdate(), adapter));
		}

		// Set refresh time Text
		mRefreshTimeText.setText(facility.getRefresh().getStringInterval(mActivity));


		// Add Graph with history data
		if (mUnitsHelper != null && mTimeHelper != null) {
			DateTimeFormatter fmt = mTimeHelper.getFormatter(GRAPH_DATE_TIME_FORMAT, adapter);
			//addGraphView(fmt, mUnitsHelper);
		}

		// Set battery
		mBattery.setText(facility.getBattery() + "%");

		// Set signal
		mSignal.setText(facility.getNetworkQuality()+"%");

		// Add Graph
		if (mUnitsHelper != null && mTimeHelper != null) {
			DateTimeFormatter fmt = mTimeHelper.getFormatter(GRAPH_DATE_TIME_FORMAT, adapter);
			addGraphView(fmt, mUnitsHelper);
		}

		// Visible all elements
		visibleAllElements();

		// Disable progress bar
		getActivity().setProgressBarIndeterminateVisibility(false);
	}

	private void visibleAllElements() {
		mView.findViewById(R.id.sen_header).setVisibility(View.VISIBLE);
		mView.findViewById(R.id.sen_first_section).setVisibility(View.VISIBLE);
		mView.findViewById(R.id.sen_second_section).setVisibility(View.VISIBLE);
		mView.findViewById(R.id.sen_third_section).setVisibility(View.VISIBLE);
		mView.findViewById(R.id.sen_sep_1).setVisibility(View.VISIBLE);
		mView.findViewById(R.id.sen_sep_2).setVisibility(View.VISIBLE);


		// Show some controls if this device is an actor
		if (mDevice.getType().isActor() && mController.isUserAllowed(mAdapter.getRole())) {
			BaseValue value = mDevice.getValue();
			
			// For actor values of type on/off, open/closed we show switch button
			if (value instanceof OnOffValue || value instanceof OpenClosedValue) {
				mValueSwitch.setVisibility(View.VISIBLE);
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
				mSwipeLayout.setRefreshing(false);
				doReloadFacilitiesTask(mAdapterId, true);
				//mActivity.getPager().getAdapter().notifyDataSetChanged();
			}
		});
		mSwipeLayout.setColorSchemeColors(  R.color.beeeon_primary_cyan, R.color.beeeon_text_color,R.color.beeeon_secundary_pink);

		// mSpinnerLoc;
		/*if (mGraphView != null) {
			mGraphView.setVisibility(View.VISIBLE);
			mGraphInfo.setVisibility(View.VISIBLE);
		}*/

	}

	private void addGraphView(final DateTimeFormatter fmt, final UnitsHelper unitsHelper) {
		// Create and set graphView
		GraphViewHelper.prepareGraphView(mGraphView, getView().getContext(), mDevice, fmt, unitsHelper); // empty heading

		//mGraphView.setVisibility(View.GONE);
		mGraphView.getViewport().setScrollable(false);
		mGraphView.getViewport().setScalable(false);

		if (mDevice.getValue() instanceof BaseEnumValue) {
			mGraphSeries = new BarGraphSeries<>(new DataPoint[]{new DataPoint(0, 0), new DataPoint(1,1)});
			((BarGraphSeries) mGraphSeries).setSpacing(30);
		} else {
			mGraphSeries =  new LineGraphSeries<>(new DataPoint[]{new DataPoint(0, 0), new DataPoint(1,1)});
			((LineGraphSeries)mGraphSeries).setBackgroundColor(getResources().getColor(R.color.alpha_blue));
			((LineGraphSeries)mGraphSeries).setDrawBackground(true);
			((LineGraphSeries) mGraphSeries).setThickness(2);
//			((LineGraphSeries) mGraphSeries).setDrawCubicLine(true);
		}
		mGraphSeries.setColor(getResources().getColor(R.color.beeeon_primary_cyan));
		mGraphSeries.setTitle("Graph");

		// Add data series
//		mGraphView.addSeries(mGraphSeries);
/*
		mGraphInfo.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// Disable graph if in edit Mode
				if (mEditMode != EDIT_NONE)
					return false;

				if (mWasTapGraph)
					return true;

				mWasTapLayout = false;
				mWasTapGraph = true;

				Log.d(TAG, "onTouch layout");
				mGraphView.getViewport().setScrollable(true);
				mGraphView.getViewport().setScalable(true);
				mActivity.setEnableSwipe(false);
				mGraphInfo.setVisibility(View.GONE);
//				mGraphView.animateY(2000);
				onTouch(v, event);
				return true;
			}
		});*/
	}



	public void fillGraph(DeviceLog log) {
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

		mGraphView.removeAllSeries();
		mGraphSeries.resetData(data);
		mGraphView.addSeries(mGraphSeries);
		mGraphView.getViewport().setXAxisBoundsManual(true);
		if (values.size() > 100 && mGraphSeries instanceof BarGraphSeries) {
			mGraphView.getViewport().setMaxX(mGraphSeries.getHighestValueX());
			mGraphView.getViewport().setMinX(mGraphSeries.getHighestValueX() - TimeUnit.HOURS.toMillis(1));
		}

		mGraphView.setLoading(false);
		mGraphView.animateY(2000);
		//mGraphInfo.setText(getView().getResources().getString(R.string.sen_detail_graph_info));
	}

	public void setSensorID(String id) {
		mDeviceID = id;
	}

	public void setLocationID(String locationId) {
		mLocationID = locationId;
	}

	public void setPosition(int position) {
		mCurPageNumber = position;
	}

	public void setSelectedPosition(int mActiveDevicePosition) {
		mSelPageNumber = mActiveDevicePosition;
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

	protected void doActorAction(final Device device) {
		if (!device.getType().isActor()) {
			return;
		}

		// SET NEW VALUE
		BaseValue value = device.getValue();
		if (value instanceof BaseEnumValue) {
			((BaseEnumValue)value).setNextValue();
		} else {
			Log.e(TAG, "We can't switch actor, which value isn't inherited from BaseEnumValue, yet");
			return;
		}

		mActorActionTask = new ActorActionTask(getActivity().getApplicationContext());
		mActorActionTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				// Get new device
				mDevice = mController.getDevice(device.getFacility().getAdapterId(), device.getId());

				// Set icon of sensor
				mIcon.setImageResource(mDevice.getIconResource());
				// Enable button
				mValueSwitch.setEnabled(true);
				mValue.setText(mUnitsHelper.getStringValueUnit(mDevice.getValue()));
			}

		});
		mActorActionTask.execute(device);
	}

	protected void doReloadFacilitiesTask(final String adapterId, final boolean forceRefresh) {
		mReloadFacilitiesTask = new ReloadFacilitiesTask(mActivity, forceRefresh);

		mReloadFacilitiesTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if(!success){
					Log.d(TAG,"Fragment - Reload failed");
					return;
				}
				Log.d(TAG, "Fragment - Start reload task");
				mDevice = mController.getDevice(adapterId, mDeviceID);
				if (mDevice == null) {
					Log.d(TAG, "Fragment - Stop reload task");
					return;
				}
				initLayout(mDevice);
				doLoadGraphData();
			}

		});

		mReloadFacilitiesTask.execute(adapterId);
	}

	protected void doLoadGraphData() {
		DateTime end = DateTime.now(DateTimeZone.UTC);
		DateTime start = end.minusWeeks(1);

		DateTimeFormatter fmt = DateTimeFormat.forPattern(LOG_DATE_TIME_FORMAT).withZoneUTC();
		Log.d(TAG, String.format("Loading graph data from %s to %s.", fmt.print(start), fmt.print(end)));

		mGetDeviceLogTask = new GetDeviceLogTask(mActivity);
		LogDataPair pair = new LogDataPair( //
				mDevice, // device
				new Interval(start, end), // interval from-to
				DataType.AVERAGE, // type
				(mDevice.getValue() instanceof BaseEnumValue )?DataInterval.RAW:DataInterval.HOUR); // interval
		mGetDeviceLogTask.setListener(new GetDeviceLogTask.CallbackLogTaskListener() {
			@Override
			public void onExecute(DeviceLog result) {
				fillGraph(result);
			}
		});
		mGetDeviceLogTask.execute(new LogDataPair[] { pair });
	}

}
