package cz.vutbr.fit.iha.activity.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.SensorDetailActivity;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.Device;
import cz.vutbr.fit.iha.adapter.device.Device.SaveDevice;
import cz.vutbr.fit.iha.adapter.device.DeviceLog;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataInterval;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataType;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.adapter.device.RefreshInterval;
import cz.vutbr.fit.iha.adapter.device.values.BaseEnumValue;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.arrayadapter.LocationArrayAdapter;
import cz.vutbr.fit.iha.asynctask.CallbackTask.CallbackTaskListener;
import cz.vutbr.fit.iha.asynctask.SaveDeviceTask;
import cz.vutbr.fit.iha.asynctask.SaveFacilityTask;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.pair.LogDataPair;
import cz.vutbr.fit.iha.pair.SaveDevicePair;
import cz.vutbr.fit.iha.pair.SaveFacilityPair;
import cz.vutbr.fit.iha.thread.ToastMessageThread;
import cz.vutbr.fit.iha.util.GraphViewHelper;
import cz.vutbr.fit.iha.util.Log;
import cz.vutbr.fit.iha.util.TimeHelper;
import cz.vutbr.fit.iha.util.UnitsHelper;

//import android.widget.LinearLayout;

public class SensorDetailFragment extends SherlockFragment {

	private Controller mController;
	private static final String TAG = SensorDetailFragment.class.getSimpleName();
	private static final int EDIT_NONE = 0;
	private static final int EDIT_NAME = 1;
	private static final int EDIT_LOC = 2;
	private static final int EDIT_REFRESH_T = 3;

	// Maximum number of items in graph
	private static final int MAX_GRAPH_DATA_COUNT = 500;

	// GUI elements
	private TextView mName;
	private EditText mNameEdit;
	private TextView mLocation;
	private TextView mValue;
	private TextView mTime;
	private ImageView mIcon;
	private TextView mRefreshTimeText;
	private SeekBar mRefreshTimeValue;
	private TextView mGraphLabel;
	private LinearLayout mGraphLayout;
	private ScrollView mLayoutScroll;
	private RelativeLayout mLayoutRelative;
	private RelativeLayout mRectangleName;
	private RelativeLayout mRectangleLoc;
	private Spinner mSpinnerLoc;
	private GraphView mGraphView;
	private TextView mGraphInfo;

	private SensorDetailActivity mActivity;

	private Device mDevice;

	private SaveDeviceTask mSaveDeviceTask;
	private GetDeviceLogTask mGetDeviceLogTask;
	private SaveFacilityTask mSaveFacilityTask;

	public static final String ARG_PAGE = "page";
	public static final String ARG_CUR_PAGE = "currentpage";
	public static final String ARG_SEL_PAGE = "selectedpage";
	public static final String ARG_LOC_ID = "locationid";
	public static final String ARG_ADAPTER_ID = "adapterid";

	private String mPageNumber;
	private String mLocationID;
	private int mCurPageNumber;
	private int mSelPageNumber;
	private String mAdapterId;

	private boolean mWasTapLayout = false;
	private boolean mWasTapGraph = false;
	private int mEditMode = EDIT_NONE;

	//
	private ActionMode mMode;

	private int mLastProgressRefreshTime;

	private GraphViewSeries mGraphSeries;

	private String mGraphDateTimeFormat = "dd.MM. kk:mm";

	/**
	 * Factory method for this fragment class. Constructs a new fragment for the given page number.
	 */
	public static SensorDetailFragment create(String IDSensor, String IDLocation, int position, int selPosition, String adapterId) {
		SensorDetailFragment fragment = new SensorDetailFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PAGE, IDSensor);
		args.putString(ARG_LOC_ID, IDLocation);
		args.putInt(ARG_CUR_PAGE, position);
		args.putInt(ARG_SEL_PAGE, selPosition);
		args.putString(ARG_ADAPTER_ID, adapterId);
		fragment.setArguments(args);
		return fragment;
	}

	public SensorDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPageNumber = getArguments().getString(ARG_PAGE);
		mLocationID = getArguments().getString(ARG_LOC_ID);
		mSelPageNumber = getArguments().getInt(ARG_SEL_PAGE);
		mCurPageNumber = getArguments().getInt(ARG_CUR_PAGE);
		mAdapterId = getArguments().getString(ARG_ADAPTER_ID);
		Log.d(TAG, "Here 1 " + mCurPageNumber);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mActivity = (SensorDetailActivity) getActivity();

		// Get controller
		mController = Controller.getInstance(mActivity.getApplicationContext());

		View view = inflater.inflate(R.layout.activity_sensor_detail_screen, container, false);
		Log.d(TAG, String.format("this position: %s , selected item: %s ", mCurPageNumber, mSelPageNumber));

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mDevice = mController.getDevice(mAdapterId, mPageNumber);
		if (mDevice != null) {
			Log.d(TAG, String.format("ID: %s, Name: %s", mDevice.getId(), mDevice.getName()));
			initLayout(mDevice);
		}

		Log.d(TAG, "Here 3 " + mCurPageNumber);
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
		final Context context = getActivity();// SensorDetailFragment.this.getView().getContext();
		// Get View for sensor name
		mName = (TextView) getView().findViewById(R.id.sen_detail_name);
		mNameEdit = (EditText) getView().findViewById(R.id.sen_detail_name_edit);
		mRectangleName = (RelativeLayout) getView().findViewById(R.id.sen_rectangle_name);
		// Get View for sensor location
		mLocation = (TextView) getView().findViewById(R.id.sen_detail_loc_name);
		mRectangleLoc = (RelativeLayout) getView().findViewById(R.id.sen_rectangle_loc);
		mSpinnerLoc = (Spinner) getView().findViewById(R.id.sen_detail_spinner_choose_location);
		// Get View for sensor value
		mValue = (TextView) getView().findViewById(R.id.sen_detail_value);
		// Get View for sensor time
		mTime = (TextView) getView().findViewById(R.id.sen_detail_time);
		// Get Image for sensor
		mIcon = (ImageView) getView().findViewById(R.id.sen_detail_icon);
		// Get TextView for refresh time
		mRefreshTimeText = (TextView) getView().findViewById(R.id.sen_refresh_time_value);
		// Get SeekBar for refresh time
		mRefreshTimeValue = (SeekBar) getView().findViewById(R.id.sen_refresh_time_seekBar);
		// Set title selected for animation if is text long
		mName.setSelected(true);
		mLocation.setSelected(true);
		// Set Max value by length of array with values
		mRefreshTimeValue.setMax(RefreshInterval.values().length - 1);

		mRefreshTimeValue.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				String interval = RefreshInterval.values()[progress].getStringInterval(context);
				mRefreshTimeText.setText(" " + interval);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// Set variable if this first touch
				if (mEditMode != EDIT_NONE)
					return;

				if (mEditMode != EDIT_REFRESH_T) {
					mEditMode = EDIT_REFRESH_T;
					// Disable Swipe
					mActivity.setEnableSwipe(false);
					mMode = getSherlockActivity().startActionMode(new AnActionModeOfSensorEdit());
					mLastProgressRefreshTime = seekBar.getProgress();
				}

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				String interval = RefreshInterval.values()[seekBar.getProgress()].getStringInterval(context);
				Log.d(TAG, String.format("Stop select value %s", interval));
			}
		});
		// Get LinearLayout for graph
		mGraphLayout = (LinearLayout) getView().findViewById(R.id.sen_graph_layout);
		mGraphLabel = (TextView) getView().findViewById(R.id.sen_graph_name);
		mGraphInfo = (TextView) getView().findViewById(R.id.sen_graph_info);
		// Get RelativeLayout of detail
		mLayoutRelative = (RelativeLayout) getView().findViewById(R.id.sensordetail_scroll);
		mLayoutScroll = (ScrollView) getView().findViewById(R.id.sensordetail_layout);
		mLayoutRelative.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// Disable graph if in edit Mode
				if (mEditMode != EDIT_NONE)
					return false;

				if (mWasTapLayout)
					return true;

				mWasTapLayout = true;
				mWasTapGraph = false;
				if (mGraphView != null) {
					mGraphView.setScalable(false);
					mGraphView.setScrollable(false);
					mActivity.setEnableSwipe(true);
					mGraphInfo.setVisibility(View.VISIBLE);
					onTouch(v, event);
					return true;
				}
				return false;
			}
		});
		/*
		 * mLayoutScroll.setOnTouchListener(new OnTouchListener() {
		 * 
		 * @Override public boolean onTouch(View v, MotionEvent event) { // Disable graph if in edit Mode if (mEditMode != EDIT_NONE) return false;
		 * 
		 * if (mWasTapLayout) return true;
		 * 
		 * mWasTapLayout = true; mWasTapGraph = false; if (mGraphView != null) { mGraphView.setScalable(false); mGraphView.setScrollable(false); mActivity.setEnableSwipe(true);
		 * mGraphInfo.setVisibility(View.VISIBLE); onTouch(v, event); return true; } return false; } });
		 */

		// Set name of sensor
		mName.setText(device.getName());
		mName.setBackgroundColor(Color.TRANSPARENT);
		mName.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mEditMode != EDIT_NONE)
					return;
				// Disable SeekBar
				mRefreshTimeValue.setEnabled(false);
				// Disable SwipeGesture
				mActivity.setEnableSwipe(false);

				mEditMode = EDIT_NAME;
				mMode = getSherlockActivity().startActionMode(new AnActionModeOfSensorEdit());
				mName.setVisibility(View.GONE);
				mRectangleName.setVisibility(View.GONE);
				mNameEdit.setVisibility(View.VISIBLE);
				mNameEdit.setText(mName.getText());
				InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
				// return true;
			}
		});

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

			mLocation.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mEditMode != EDIT_NONE)
						return;
					// Disable SeekBar
					mRefreshTimeValue.setEnabled(false);

					// Disable Swipe
					mActivity.setEnableSwipe(false);

					mEditMode = EDIT_LOC;
					mMode = getSherlockActivity().startActionMode(new AnActionModeOfSensorEdit());
					mSpinnerLoc.setVisibility(View.VISIBLE);
					mLocation.setVisibility(View.GONE);
					mRectangleLoc.setVisibility(View.GONE);

					// open Spinner
					mSpinnerLoc.performClick();
				}
			});
		} else {
			Log.e(TAG, "mController is null (this shouldn't happen)");
			mLocation.setText(device.getFacility().getLocationId());
		}

		// Set locations to spinner
		LocationArrayAdapter dataAdapter = new LocationArrayAdapter(this.getActivity(), R.layout.custom_spinner_item, getLocationsArray());
		dataAdapter.setLayoutInflater(getLayoutInflater(null));
		dataAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);

		mSpinnerLoc.setAdapter(dataAdapter);
		mSpinnerLoc.setSelection(getLocationsIndexFromArray(getLocationsArray()));

		Facility facility = device.getFacility();
		Adapter adapter = mController.getAdapter(facility.getAdapterId());

		// UserSettings can be null when user is not logged in!
		SharedPreferences prefs = mController.getUserSettings();

		UnitsHelper unitsHelper = (prefs == null) ? null : new UnitsHelper(prefs, getActivity().getApplicationContext());
		TimeHelper timeHelper = (prefs == null) ? null : new TimeHelper(prefs);

		// Set value of sensor
		if (unitsHelper != null) {
			mValue.setText(unitsHelper.getStringValueUnit(device.getValue()));
		}

		// Set icon of sensor
		mIcon.setImageResource(device.getIconResource());

		// Set time of sensor
		if (timeHelper != null) {
			mTime.setText(timeHelper.formatLastUpdate(facility.getLastUpdate(), adapter));
		}

		// Set refresh time Text
		mRefreshTimeText.setText(" " + facility.getRefresh().getStringInterval(context));

		// Set refresh time SeekBar
		mRefreshTimeValue.setProgress(facility.getRefresh().getIntervalIndex());

		// Add Graph with history data
		if (unitsHelper != null && timeHelper != null) {
			DateTimeFormatter fmt = timeHelper.getFormatter(mGraphDateTimeFormat, adapter);
			addGraphView(fmt, unitsHelper);
		}

		// Visible all elements
		visibleAllElements();

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		// int height = displaymetrics.heightPixels;
		// int width = displaymetrics.widthPixels;

		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mGraphInfo.getLayoutParams();
		// Log.d(TAG, "GraphLayout width x height " +
		// mGraphLayout.getLayoutParams().width + " x "+
		// mGraphLayout.getLayoutParams().height);

		// substitute parameters for left, top, right, bottom
		params.setMargins((int) ((displaymetrics.widthPixels / 2) - (70 * displaymetrics.density)), (int) ((-120) * displaymetrics.density), 0, 0);
		mGraphInfo.setLayoutParams(params);

		// Disable progress bar
		// getActivity().setProgressBarIndeterminateVisibility(false);
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
	}

	private void visibleAllElements() {
		mName.setVisibility(View.VISIBLE);
		// mNameEdit;
		mLocation.setVisibility(View.VISIBLE);
		mValue.setVisibility(View.VISIBLE);
		mTime.setVisibility(View.VISIBLE);
		mIcon.setVisibility(View.VISIBLE);
		mRefreshTimeText.setVisibility(View.VISIBLE);
		((TextView) getView().findViewById(R.id.sen_refresh_time)).setVisibility(View.VISIBLE);
		mRefreshTimeValue.setVisibility(View.VISIBLE);
		mGraphLayout.setVisibility(View.VISIBLE);
		mGraphLabel.setVisibility(View.VISIBLE);
		// mLayout;
		mRectangleName.setVisibility(View.VISIBLE);
		mRectangleLoc.setVisibility(View.VISIBLE);
		// mSpinnerLoc;
		if (mGraphView != null) {
			mGraphView.setVisibility(View.VISIBLE);
			mGraphInfo.setVisibility(View.VISIBLE);
		}

	}

	private void addGraphView(final DateTimeFormatter fmt, final UnitsHelper unitsHelper) {
		// Create and set graphView
		mGraphView = GraphViewHelper.prepareGraphView(getView().getContext(), "", mDevice, fmt, unitsHelper); // empty heading
		
		mGraphView.setVisibility(View.GONE);
		mGraphView.setScrollable(false);
		mGraphView.setScalable(false);
		
		if (mGraphView instanceof LineGraphView) {
			mGraphView.setBackgroundColor(getResources().getColor(R.color.alpha_blue));// getResources().getColor(R.color.log_blue2));
			((LineGraphView) mGraphView).setDrawBackground(true);
		}
		// graphView.setAlpha(128);

		// Add data series
		GraphViewSeriesStyle seriesStyleBlue = new GraphViewSeriesStyle(getResources().getColor(R.color.iha_primary_cyan), 2);
		// GraphViewSeriesStyle seriesStyleGray = new GraphViewSeriesStyle(getResources().getColor(R.color.light_gray),2);

		mGraphSeries = new GraphViewSeries("Graph", seriesStyleBlue, new GraphViewData[] { new GraphView.GraphViewData(0, 0), });
		mGraphView.addSeries(mGraphSeries);
		
		if (!(mDevice.getValue() instanceof BaseEnumValue)) {
			mGraphView.setManualYAxisBounds(1.0, 0.0);
		}

		mGraphLayout.addView(mGraphView);

		mGraphLayout.setOnTouchListener(new OnTouchListener() {

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
				mGraphView.setScrollable(true);
				mGraphView.setScalable(true);
				mActivity.setEnableSwipe(false);
				mGraphInfo.setVisibility(View.GONE);
				onTouch(v, event);
				return true;
			}
		});

		loadGraphData();
	}

	private void loadGraphData() {
		DateTime end = DateTime.now(DateTimeZone.UTC);
		DateTime start = end.minusWeeks(1);

		Log.d(TAG, String.format("Loading graph data from %s to %s.", start, end));

		mGetDeviceLogTask = new GetDeviceLogTask();
		LogDataPair pair = new LogDataPair( //
				mDevice, // device
				new Interval(start, end), // interval from-to
				DataType.AVERAGE, // type
				DataInterval.HOUR); // interval
		mGetDeviceLogTask.execute(new LogDataPair[] { pair });
	}

	private List<Location> getLocationsArray() {
		// Get locations from adapter
		List<Location> locations = new ArrayList<Location>();

		Adapter adapter = mController.getActiveAdapter();
		if (adapter != null) {
			locations = mController.getLocations(adapter.getId());
		}

		// Sort them
		Collections.sort(locations);

		return locations;
	}

	private int getLocationsIndexFromArray(List<Location> locations) {
		int index = 0;
		for (Location room : locations) {
			if (room.getId().equalsIgnoreCase(mLocationID)) {
				return index;
			}
			index++;
		}
		return index;
	}

	public void fillGraph(DeviceLog log) {
		if (mGraphView == null) {
			return;
		}

		// NOTE: This formatter is only for Log, correct timezone from app setting doesn't matter here
		final DateTimeFormatter fmt = DateTimeFormat.forPattern(mGraphDateTimeFormat);

		int size = log.getValues().size();
		Log.d(TAG, String.format("Filling graph with %d values. Min: %.1f, Max: %.1f", size, log.getMinimum(), log.getMaximum()));

		int begin;
		GraphView.GraphViewData[] data;

		// Limit amount of showed values
		if (size > MAX_GRAPH_DATA_COUNT) {
			data = new GraphView.GraphViewData[MAX_GRAPH_DATA_COUNT];
			begin = (size - MAX_GRAPH_DATA_COUNT);
		} else {
			data = new GraphView.GraphViewData[size];
			begin = 0;
		}

		for (int i = begin; i < size; i++) {
			DeviceLog.DataRow row = log.getValues().get(i);

			float value = Float.isNaN(row.value) ? log.getMinimum() : row.value;
			data[i - begin] = new GraphView.GraphViewData(row.dateMillis, value);
			// Log.v(TAG, String.format("Graph value: date(msec): %s, Value: %.1f", fmt.print(row.dateMillis), row.value));
		}

		Log.d(TAG, "Filling graph finished");

		// Set maximum as +10% more than deviation
		if (!(mDevice.getValue() instanceof BaseEnumValue)) {
			mGraphView.setManualYAxisBounds(log.getMaximum() + log.getDeviation() * 0.1, log.getMinimum());
		}
		// mGraphView.setViewPort(0, 7);
		mGraphSeries.resetData(data);
		mGraphInfo.setText(getView().getResources().getString(R.string.sen_detail_graph_info));
	}

	/*
	 * ================================= ASYNC TASK ===========================
	 */

	private void doSaveDeviceTask(SaveDevicePair pair) {
		mSaveDeviceTask = new SaveDeviceTask(getActivity().getApplicationContext());
		mSaveDeviceTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (mActivity.getProgressDialog() != null)
					mActivity.getProgressDialog().dismiss();
				if (success) {
					Log.d(TAG, "Success save to server");
					// Change GUI
					mActivity.redraw();
				} else {
					Log.d(TAG, "Fail save to server");
				}
				int messageId = success ? R.string.toast_success_save_data : R.string.toast_fail_save_data;
				Log.d(TAG, mActivity.getString(messageId));
				new ToastMessageThread(mActivity, messageId).start();
			}
		});

		mSaveDeviceTask.execute(pair);
	}

	public void doSaveFacilityTask(SaveFacilityPair pair) {
		mSaveFacilityTask = new SaveFacilityTask(mActivity);
		mSaveFacilityTask.setListener(new CallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (mActivity.getProgressDialog() != null)
					mActivity.getProgressDialog().dismiss();
				if (success) {
					Log.d(TAG, "Success save to server");
					// Change GUI
					mActivity.redraw();
				} else {
					Log.d(TAG, "Fail save to server");
				}
				int messageId = success ? R.string.toast_success_save_data : R.string.toast_fail_save_data;
				Log.d(TAG, mActivity.getString(messageId));
				new ToastMessageThread(mActivity, messageId).start();
			}
		});
		mSaveFacilityTask.execute(pair);
	}

	/**
	 * Changes selected location and redraws list of adapters there
	 */
	private class GetDeviceLogTask extends AsyncTask<LogDataPair, Void, DeviceLog> {
		@Override
		protected DeviceLog doInBackground(LogDataPair... pairs) {
			LogDataPair pair = pairs[0]; // expects only one device at a time is sent there

			return mController.getDeviceLog(pair.device, pair);
		}

		@Override
		protected void onPostExecute(DeviceLog log) {
			fillGraph(log);
		}

	}

	/*
	 * ============================= ACTION MODE ==============================
	 */

	// Menu for Action bar mode - edit
	class AnActionModeOfSensorEdit implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			menu.add("Save").setIcon(R.drawable.iha_ic_action_accept).setTitle("Save").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add("Cancel").setIcon(R.drawable.iha_ic_action_cancel).setTitle("Cancel").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

			switch (mEditMode) {
			case EDIT_LOC:
				mSpinnerLoc.setVisibility(View.GONE);
				mLocation.setVisibility(View.VISIBLE);
				mRectangleLoc.setVisibility(View.VISIBLE);
				mRefreshTimeValue.setEnabled(true);
				if (item.getTitle().equals("Save")) {
					// Progress dialog
					if (mActivity.getProgressDialog() != null)
						mActivity.getProgressDialog().show();
					// Set new location in facility
					mDevice.getFacility().setLocationId(((Location) mSpinnerLoc.getSelectedItem()).getId());
					// Update device to server
					doSaveFacilityTask(new SaveFacilityPair(mDevice.getFacility(), EnumSet.of(SaveDevice.SAVE_LOCATION)));

				}
				break;
			case EDIT_NAME:
				if (item.getTitle().equals("Save")) {
					mName.setText(mNameEdit.getText());

					// Set new name to device
					mDevice.setName(mNameEdit.getText().toString());

					// Update device to server
					doSaveDeviceTask(new SaveDevicePair(mDevice, EnumSet.of(SaveDevice.SAVE_NAME)));
				}
				mNameEdit.setVisibility(View.GONE);
				mName.setVisibility(View.VISIBLE);
				mRectangleName.setVisibility(View.VISIBLE);

				mNameEdit.clearFocus();
				imm.hideSoftInputFromWindow(mNameEdit.getWindowToken(), 0);
				mRefreshTimeValue.setEnabled(true);
				break;
			case EDIT_REFRESH_T:
				// Was clicked on cancel
				if (item.getTitle().equals("Cancel")) {
					mRefreshTimeValue.setProgress(mLastProgressRefreshTime);
				} else {
					// set actual progress
					mDevice.getFacility().setRefresh(RefreshInterval.values()[mRefreshTimeValue.getProgress()]);
					// Progress dialog
					if (mActivity.getProgressDialog() != null)
						mActivity.getProgressDialog().show();
					Log.d(TAG, "Refresh time " + mDevice.getFacility().getRefresh().getStringInterval(mActivity));
					// Update device to server
					doSaveDeviceTask(new SaveDevicePair(mDevice, EnumSet.of(SaveDevice.SAVE_REFRESH)));
				}
				break;

			default:
				break;

			}

			mEditMode = EDIT_NONE;
			// enable SeekBar

			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// Controll mode and set default values
			switch (mEditMode) {
			case EDIT_REFRESH_T:
				mRefreshTimeValue.setProgress(mLastProgressRefreshTime);
				break;
			}
			mActivity.setEnableSwipe(true);

			mSpinnerLoc.setVisibility(View.GONE);
			mLocation.setVisibility(View.VISIBLE);
			mRectangleLoc.setVisibility(View.VISIBLE);
			mNameEdit.setVisibility(View.GONE);
			mName.setVisibility(View.VISIBLE);
			mRectangleName.setVisibility(View.VISIBLE);
			mNameEdit.clearFocus();
			InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mNameEdit.getWindowToken(), 0);
			// mRefreshTimeValue.setProgress(mLastProgressRefreshTime);
			mEditMode = EDIT_NONE;
			// enable SeekBar
			mRefreshTimeValue.setEnabled(true);
		}
	}

}
