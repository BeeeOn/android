package cz.vutbr.fit.iha.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
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
import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

import cz.vutbr.fit.iha.LocationArrayAdapter;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.device.DeviceLog;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataInterval;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataType;
import cz.vutbr.fit.iha.adapter.device.RefreshInterval;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.controller.Controller;
//import android.widget.LinearLayout;

public class SensorDetailFragment extends SherlockFragment {

	private Controller mController;
	private static final String TAG = SensorDetailFragment.class.getSimpleName();
	private static final int EDIT_NONE = 0;
	private static final int EDIT_NAME = 1;
	private static final int EDIT_LOC = 2;
	private static final int EDIT_REFRESH_T = 3;

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

	private BaseDevice mDevice;

	private GetDeviceTask mGetDeviceTask;
	private GetDeviceLogTask mGetDeviceLogTask;

	public static final String ARG_PAGE = "page";
	public static final String ARG_CUR_PAGE = "currentpage";
	public static final String ARG_SEL_PAGE = "selectedpage";
	public static final String ARG_LOC_ID = "locationid";
	
	private String mPageNumber;
	private String mLocationID;
	private int mCurPageNumber;
	private int mSelPageNumber;

	private boolean mWasTapLayout = false;
	private boolean mWasTapGraph = false;
	private int mEditMode = EDIT_NONE;

	//
	private ActionMode mMode;

	public double minimum;
	private int mLastProgressRefreshTime;
	
	private GraphViewSeries mGraphSeries;
	
	private String mFormatDateTime = "%Y-%m-%d %H:%M:%S";
	private String mGraphDateFormat = "dd.MM.";
	private String mGraphTimeFormat = "hh:mm";
	private float mTempConstant = (float) 1000;

	/**
	 * Represents "pair" of data required for get device log
	 */
	private class LogDataPair {
		public final BaseDevice device;
		public final String from;
		public final String to;
		public final DataType type;
		public final DataInterval interval;
		
		public LogDataPair(final BaseDevice device, final String from, final String to, final DataType type, final DataInterval interval) {
			this.device = device;
			this.from = from;
			this.to = to;
			this.type = type;
			this.interval = interval;
		}
	}
	
	/**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static SensorDetailFragment create(String IDSensor, String IDLocation, int position, int selPosition) {
    	SensorDetailFragment fragment = new SensorDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PAGE, IDSensor);
        args.putString(ARG_LOC_ID, IDLocation);
        args.putInt(ARG_CUR_PAGE, position);
        args.putInt(ARG_SEL_PAGE, selPosition);
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
        Log.d(TAG, "Here 1 "+ mCurPageNumber);
    }
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Get controller
		mController = Controller.getInstance(getActivity());

		mActivity = (SensorDetailActivity) getActivity();

		View view = inflater.inflate(R.layout.activity_sensor_detail_screen,
				container, false);
		Log.d(TAG, String.format("this position: %s , selected item: %s ",
				mCurPageNumber, mSelPageNumber));

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mGetDeviceTask = new GetDeviceTask();
		mGetDeviceTask.execute(new String[] { mPageNumber });

		Log.d(TAG, "Here 3 " + mCurPageNumber);
	}

	@Override
	public void onStop() {
		if (mGetDeviceTask != null) {
			mGetDeviceTask.cancel(true);
		}
		if (mGetDeviceLogTask != null) {
			mGetDeviceLogTask.cancel(true);
		}
		super.onStop();
	}

	private void initLayout(BaseDevice device) {
		final Context context = getActivity();// SensorDetailFragment.this.getView().getContext();
		// Get View for sensor name
		mName = (TextView) getView().findViewById(R.id.sen_detail_name);
		mNameEdit = (EditText) getView()
				.findViewById(R.id.sen_detail_name_edit);
		mRectangleName = (RelativeLayout) getView().findViewById(
				R.id.sen_rectangle_name);
		// Get View for sensor location
		mLocation = (TextView) getView().findViewById(R.id.sen_detail_loc_name);
		mRectangleLoc = (RelativeLayout) getView().findViewById(
				R.id.sen_rectangle_loc);
		mSpinnerLoc = (Spinner) getView().findViewById(
				R.id.sen_detail_spinner_choose_location);
		// Get View for sensor value
		mValue = (TextView) getView().findViewById(R.id.sen_detail_value);
		// Get View for sensor time
		mTime = (TextView) getView().findViewById(R.id.sen_detail_time);
		// Get Image for sensor
		mIcon = (ImageView) getView().findViewById(R.id.sen_detail_icon);
		// Get TextView for refresh time
		mRefreshTimeText = (TextView) getView().findViewById(
				R.id.sen_refresh_time);
		// Get SeekBar for refresh time
		mRefreshTimeValue = (SeekBar) getView().findViewById(
				R.id.sen_refresh_time_seekBar);
		// Set Max value by length of array with values
		mRefreshTimeValue.setMax(RefreshInterval.values().length - 1);

		mRefreshTimeValue
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						String interval = RefreshInterval.values()[progress]
								.getStringInterval(context);
						mRefreshTimeText.setText(String.format(
								getString(R.string.refresh_time), interval));
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
							mMode = getSherlockActivity().startActionMode(
									new AnActionModeOfEpicProportions());
							mLastProgressRefreshTime = seekBar.getProgress();
						}

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						String interval = RefreshInterval.values()[seekBar
								.getProgress()].getStringInterval(context);
						Log.d(TAG,
								String.format("Stop select value %s", interval));
					}
				});
		// Get LinearLayout for graph
		mGraphLayout = (LinearLayout) getView().findViewById(
				R.id.sen_graph_layout);
		mGraphLabel = (TextView) getView().findViewById(R.id.sen_graph_name);
		mGraphInfo = (TextView) getView().findViewById(R.id.sen_graph_info);
		// Get RelativeLayout of detail
		mLayoutRelative = (RelativeLayout) getView().findViewById(
				R.id.sensordetail_scroll);
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
		/*mLayoutScroll.setOnTouchListener(new OnTouchListener() {

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
		});*/
		
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
				mMode = getSherlockActivity().startActionMode(
						new AnActionModeOfEpicProportions());
				mName.setVisibility(View.GONE);
				mRectangleName.setVisibility(View.GONE);
				mNameEdit.setVisibility(View.VISIBLE);
				mNameEdit.setText(mName.getText());
				InputMethodManager imm = (InputMethodManager) getSherlockActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
				// return true;
			}
		});

		// Set name of location
		if (mController != null) {
			Location location = mController.getLocationByDevice(device);
			if (location != null)
				mLocation.setText(location.getName());
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
					mMode = getSherlockActivity().startActionMode(
							new AnActionModeOfEpicProportions());
					mSpinnerLoc.setVisibility(View.VISIBLE);
					mLocation.setVisibility(View.GONE);
					mRectangleLoc.setVisibility(View.GONE);
				}
			});
		} else {
			Log.e(TAG, "mController is null (this shouldn't happen)");
			mLocation.setText(device.getLocationId());
		}

		// Set locations to spinner
		LocationArrayAdapter dataAdapter = new LocationArrayAdapter(
				this.getActivity(), R.layout.custom_spinner_item,
				getLocationsArray());
		dataAdapter.setLayoutInflater(getLayoutInflater(null));
		dataAdapter
				.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);

		mSpinnerLoc.setAdapter(dataAdapter);
		mSpinnerLoc.setSelection(getLocationsIndexFromArray(getLocationsArray()));
		
		// Set value of sensor
		mValue.setText(device.getStringValueUnit(getActivity()));
		// Set icon of sensor
		mIcon.setImageResource(device.getTypeIconResource());
		// Set time of sensor
		mTime.setText(setLastUpdate(device.lastUpdate));
		// Set refresh time Text
		mRefreshTimeText.setText(getString(R.string.refresh_time, device
				.getRefresh().getStringInterval(context)));
		// Set refresh time SeekBar
		mRefreshTimeValue.setProgress(device.getRefresh().getIntervalIndex());
		// Add Graph with history data
		addGraphView();

		// Visible all elements
		visibleAllElements();
		
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay()
				.getMetrics(displaymetrics);
		// int height = displaymetrics.heightPixels;
		// int width = displaymetrics.widthPixels;

		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mGraphInfo
				.getLayoutParams();
		// Log.d(TAG, "GraphLayout width x height " +
		// mGraphLayout.getLayoutParams().width + " x "+
		// mGraphLayout.getLayoutParams().height);
		params.setMargins(
				(int) ((displaymetrics.widthPixels / 2) - (70 * displaymetrics.density)),
				(int) ((-120) * displaymetrics.density), 0, 0); // substitute
																// parameters
																// for left,
																// top, right,
																// bottom
		mGraphInfo.setLayoutParams(params);

		// Disable progress bar
		// getActivity().setProgressBarIndeterminateVisibility(false);
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(
				false);
	}

	private void visibleAllElements() {
		mName.setVisibility(View.VISIBLE);
		// mNameEdit;
		mLocation.setVisibility(View.VISIBLE);
		mValue.setVisibility(View.VISIBLE);
		mTime.setVisibility(View.VISIBLE);
		mIcon.setVisibility(View.VISIBLE);
		mRefreshTimeText.setVisibility(View.VISIBLE);
		mRefreshTimeValue.setVisibility(View.VISIBLE);
		mGraphLayout.setVisibility(View.VISIBLE);
		mGraphLabel.setVisibility(View.VISIBLE);
		// mLayout;
		mRectangleName.setVisibility(View.VISIBLE);
		mRectangleLoc.setVisibility(View.VISIBLE);
		// mSpinnerLoc;
		mGraphView.setVisibility(View.VISIBLE);
		mGraphInfo.setVisibility(View.VISIBLE);

	}

	private void addGraphView() {
		mGraphView = new LineGraphView(getView().getContext() // context
				, "" // heading
		);

		minimum = -1.0;
		
		GraphViewSeriesStyle seriesStyleBlue = new GraphViewSeriesStyle(getResources().getColor(R.color.log_blue2),2);
		//GraphViewSeriesStyle seriesStyleGray = new GraphViewSeriesStyle(getResources().getColor(R.color.light_gray),2);

		mGraphView.getGraphViewStyle().setVerticalLabelsColor(getResources().getColor(R.color.log_blue2));
		mGraphView.getGraphViewStyle().setHorizontalLabelsColor(getResources().getColor(R.color.log_blue2));
		//mGraphView.getGraphViewStyle().setVerticalLabelsWidth(60);
		//mGraphView.getGraphViewStyle().setNumHorizontalLabels(2);
		mGraphView.setBackgroundColor(getResources().getColor(R.color.alpha_blue));//getResources().getColor(R.color.log_blue2));
		
		((LineGraphView) mGraphView).setDrawBackground(true);
		// graphView.setAlpha(128);
		int num = 3;
		GraphView.GraphViewData[] data = new GraphView.GraphViewData[num];
		data[0] = new GraphView.GraphViewData(0, 0);
		data[1] = new GraphView.GraphViewData(1, 0);
		data[2] = new GraphView.GraphViewData(2, 0);

		
		// Get Today 
		Time now = new Time();
		now.setToNow();
		// Get before month
		Time beforeMonth = new Time();
		Calendar cal = GregorianCalendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -7);
			
		beforeMonth.set(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal.get(Calendar.YEAR)); 
		
		Log.d(TAG,"Day: "+ cal.get(Calendar.DAY_OF_MONTH)+ " Month: " + cal.get(Calendar.MONTH));

		Log.d(TAG, String.format("Today: %s, beforeDAY month: %s", now.format(mFormatDateTime), beforeMonth.format(mFormatDateTime)));
		
		mGetDeviceLogTask = new GetDeviceLogTask();
		LogDataPair pair = new LogDataPair (
				mDevice, // device
				beforeMonth.format(mFormatDateTime), // from
				now.format(mFormatDateTime), // to
				DataType.AVERAGE, // type
				DataInterval.HOUR); // interval
		mGetDeviceLogTask.execute(new LogDataPair[] { pair });

		mGraphSeries = new GraphViewSeries("Graph", seriesStyleBlue, data);
		mGraphView
				.addSeries(mGraphSeries);
		mGraphView.setManualYAxis(true);
		mGraphView.setManualYAxisBounds(1.0, 0.0);

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

	}

	private List<Location> getLocationsArray() {
		// Get locations from adapter
		List<Location> locations = mController.getActiveAdapter().getLocations();

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

	private CharSequence setLastUpdate(Time lastUpdate) {
		// Last update time data
		Time yesterday = new Time();
		yesterday.setToNow();
		yesterday.set(yesterday.toMillis(true) - 24 * 60 * 60 * 1000); // -24
																		// hours

		// If sync time is more that 24 ago, show only date. Show time
		// otherwise.
		DateFormat dateFormat = yesterday.before(lastUpdate) ? DateFormat
				.getTimeInstance() : DateFormat.getDateInstance();

		Date lastUpdateDate = new Date(lastUpdate.toMillis(true));
		return dateFormat.format(lastUpdateDate);
	}
	
	
	public void fillGraph(DeviceLog log) {
		
		final DateFormat DateFormatter = new SimpleDateFormat(mGraphDateFormat);
		final DateFormat TimeFormatter = new SimpleDateFormat(mGraphTimeFormat);
		
		mGraphView.setCustomLabelFormatter(new CustomLabelFormatter() {
	        @Override
	        public String formatLabel(double value, boolean isValueX) {
	            // TODO Auto-generated method stub
	            if (isValueX) {
	                Log.d(TAG, "LABEL FORMATER - Value: "+ value +" Date: "+ DateFormatter.format(new Date((long) value))+ " "+TimeFormatter.format(new Date((long) value)));
	                return DateFormatter.format(new Date((long) value))+ " "+TimeFormatter.format(new Date((long) value));
	            }
	            return String.format("%.1f", value);
	        }
	    });
		
		
		int size = log.getValues().size();
		float minimum = log.getMinimumValue();
		float maximum = log.getMaximumValue();
		// Temp controll to bigger than 1 000
		minimum = (float) ((minimum > mTempConstant)?minimum/100.0:minimum);
		maximum = (float) ((maximum > mTempConstant)?maximum/100.0:maximum);
		Log.d(TAG, "LOG1 - Min: " + minimum + " Max: "+maximum);
		float deviation = maximum - minimum;
		float minLimit = (float) (minimum-(deviation*0.2));
		float maxLimit = (float) (maximum+(deviation*0.2));

		float value = 0;
		Calendar cal = Calendar.getInstance();
		GraphView.GraphViewData[] data = new GraphView.GraphViewData[size];
		for (int i = 0; i < size; i++) {
			value = log.getValues().get(i).value;
			//cal.setTimeInMillis(log.getValues().get(i).date.getTime());
			
			if(Float.isNaN(value)) {
				value = minLimit;
			}
			else if(value > 1000 ) {
				value = (float) (value/100.0);
			}
			
			data[i] = new GraphView.GraphViewData(log.getValues().get(i).date.getTime(),value);
			Log.d(TAG, "FILL GRAPH - date(msec):"  +log.getValues().get(i).date.getTime() + " Value: "+value);
		}
		
		Log.d(TAG, "LOG2 - Min: " + minimum + " Max: "+maximum);
		
		// Set maximum as +20% more than deviation and minimum as -20% deviation
		mGraphView.setManualYAxisBounds(maxLimit, (minimum > 0)?minLimit:0);
		//mGraphView.setViewPort(0, 7);
		mGraphSeries.resetData(data);
		mGraphInfo.setText(getView().getResources().getString(R.string.sen_detail_graph_info));
	}

	/*
	 * ================================= ASYNC TASK ===========================
	 */

	/**
	 * Changes selected location and redraws list of adapters there
	 */
	private class GetDeviceTask extends AsyncTask<String, Void, BaseDevice> {
		@Override
		protected BaseDevice doInBackground(String... sensorID) {

			BaseDevice device = mController.getDevice(sensorID[0]);
			Log.d(TAG, "ID:" + device.getId() + " Name:" + device.getName());

			return device;
		}

		@Override
		protected void onPostExecute(BaseDevice device) {
			mDevice = device;
			if (!isCancelled()) {
				initLayout(device);
			}

		}
	}

	/**
	 * Changes selected location and redraws list of adapters there
	 */
	private class UpdateDeviceTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			Log.d(TAG, "ID:" + mDevice.getId() + " Name:" + mDevice.getName());
			return mController.saveDevice(mDevice);
		}

		@Override
		protected void onPostExecute(Boolean ret) {
			if(ret.booleanValue() ){
				Log.d(TAG, "Success save to server");
			} else {
				Log.d(TAG, "Fail save to server");
				// Show failer toast !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			}		
		}

	}
	
	/**
	 * Changes selected location and redraws list of adapters there
	 */
	private class GetDeviceLogTask extends AsyncTask<LogDataPair, Void, DeviceLog> {
		@Override
		protected DeviceLog doInBackground(LogDataPair... pairs) {
			LogDataPair pair = pairs[0]; // expects only one device at a time is sent there
			
			return mController.getDeviceLog(pair.device, pair.from, pair.to, pair.type, pair.interval);
		}

		@Override
		protected void onPostExecute(DeviceLog log) {
			/// FIXME: Kontrola zda neni prazdny 
			//for (DeviceLog.DataRow item : log.getValues()) {
			//	Log.d(TAG, item.debugString());
			//}
			
			fillGraph(log);
		}

	}

	/*
	 * ============================= ACTION MODE ==============================
	 */

	// Menu for Action bar mode - edit
	class AnActionModeOfEpicProportions implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			// View view =
			// LayoutInflater.from(mActivity).inflate(R.layout.custom_actionmode_item,
			// null);
			// ((Button) view.findViewById(R.id.actionmode_button)).
			// menu.add("Save").setActionView(view).setIcon(R.drawable.ic_action_accept).setTitle("Save").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add("Save").setIcon(R.drawable.ic_action_accept)
					.setTitle("Save")
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add("Cancel").setIcon(R.drawable.ic_action_cancel)
					.setTitle("Cancel")
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			// TODO Auto-generated method stub
			InputMethodManager imm = (InputMethodManager) getSherlockActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);

			switch (mEditMode) {
			case EDIT_LOC:
				mSpinnerLoc.setVisibility(View.GONE);
				mLocation.setVisibility(View.VISIBLE);
				mRectangleLoc.setVisibility(View.VISIBLE);
				mRefreshTimeValue.setEnabled(true);
				break;
			case EDIT_NAME:
				if (item.getTitle().equals("Save")) {
					mName.setText(mNameEdit.getText());

					// Set new name to device
					mDevice.setName(mNameEdit.getText().toString());
					// Update device to server
					UpdateDeviceTask task = new UpdateDeviceTask();
					task.execute();
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
				if(item.getTitle().equals("Cancel")) {
					mRefreshTimeValue.setProgress(mLastProgressRefreshTime);
				}
				else {
					// set actual progress
					mDevice.setRefresh(RefreshInterval.values()[mRefreshTimeValue.getProgress()]);
					
					Log.d(TAG, "Refresh time "+ mDevice.getRefresh().getStringInterval(mActivity));
					// Update device to server
					UpdateDeviceTask task = new UpdateDeviceTask();
					task.execute();
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
			//mRefreshTimeValue.setProgress(mLastProgressRefreshTime);
			mEditMode = EDIT_NONE;
			// enable SeekBar
			mRefreshTimeValue.setEnabled(true);
		}
	}



}
