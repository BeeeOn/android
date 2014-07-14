package cz.vutbr.fit.iha.activity;

import java.text.DateFormat;
import java.util.Date;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnGenericMotionListener;
import android.view.View.OnHoverListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.controller.Controller;
//import android.widget.LinearLayout;
import cz.vutbr.fit.iha.view.CustomViewPager;

public class SensorDetailFragment extends SherlockFragment {

	private Controller mController;
	private static final String TAG = "SensorDetailFragment";
	
	// GUI elements
	private TextView sName;
	private EditText sNameEdit;
	private TextView sLocation;
	private TextView sValue;
	private TextView sTime;
	private ImageView sIcon;
	private TextView sRefreshTimeText;
	private SeekBar sRefreshTimeValue;
	private LinearLayout sGraphLayout;
	private RelativeLayout mLayout;
	
	private SensorDetailActivity mActivity;
	private GraphView mGraphView; 
	//private CustomViewPager mPager;
	
	public static final String ARG_PAGE = "page";
	
	private String mPageNumber;
	
	private boolean mWasTapLayout = false;
	private boolean mWasTapGraph = false;
	// 
	ActionMode mMode;
	
	
	// Array for refresh time constant
	// 1sec, 5sec, 10sec, 20sec , 30sec, 1min, 5min, 10min, 15min, 30,min, 1h, 2h,3h,4h, 8h, 12h, 24h
	private int[] sRefreshTimeSeekBarValues = { 1, 5, 10, 30, 60, 300, 600, 900, 1800,
			3600, 7200, 10800, 14400, 28800, 43200, 86400 };
			
	public double minimum;
	
	
	/**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static SensorDetailFragment create(String pageNumber) {
    	SensorDetailFragment fragment = new SensorDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public SensorDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getString(ARG_PAGE);
    }
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Get controller
		mController = Controller.getInstance(getActivity());

		mActivity = (SensorDetailActivity) getActivity();
		
		GetDeviceTask task = new GetDeviceTask();
		task.execute(new String[] { mPageNumber });

		View view = inflater.inflate(R.layout.activity_sensor_detail_screen,
				container, false);
		return view;
	}

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
			initLayout(device);

		}
	}

	private void initLayout(BaseDevice device) {
		// Get View for sensor name
		sName = (TextView) getView().findViewById(R.id.sen_detail_name);
		sNameEdit = (EditText) getView().findViewById(R.id.sen_detail_name_edit);
		// Get View for sensor location
		sLocation = (TextView) getView().findViewById(R.id.sen_detail_loc_name);
		// Get View for sensor value
		sValue = (TextView) getView().findViewById(R.id.sen_detail_value);
		// Get View for sensor time
		sTime = (TextView) getView().findViewById(R.id.sen_detail_time);
		// Get Image for sensor
		sIcon = (ImageView) getView().findViewById(R.id.sen_detail_icon);
		// Get TextView for refresh time
		sRefreshTimeText = (TextView) getView().findViewById(R.id.sen_refresh_time);
		// Get SeekBar for refresh time
		sRefreshTimeValue = (SeekBar) getView().findViewById(R.id.sen_refresh_time_seekBar);
		// Set Max value by length of array with values
		sRefreshTimeValue.setMax(sRefreshTimeSeekBarValues.length-1);
		sRefreshTimeValue.setOnSeekBarChangeListener(new OnSeekBarChangeListener() { 

		    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		    	sRefreshTimeText.setText(
		    			getString(R.string.refresh_time)+" "+prepareIntervalText(sRefreshTimeSeekBarValues[progress]));
		    }

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.d(TAG, "Stop select value " +prepareIntervalText(sRefreshTimeSeekBarValues[seekBar.getProgress()]) );
			}
		});
		// Get LinearLayout for graph
		sGraphLayout = (LinearLayout) getView().findViewById(R.id.sen_graph_layout);
		
		// Get RelativeLayout of detail
		mLayout = (RelativeLayout) getView().findViewById(R.id.sensordetail_scroll);
		mLayout.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(mWasTapLayout)
					return true;
				
				mWasTapLayout = true;
				mWasTapGraph = false;
				if(mGraphView != null) {
					mGraphView.setScalable(false);
					mGraphView.setScrollable(false);
					mActivity.setEnableSwipe(true);
					
					onTouch(v,event);
					return true;
				}
				return false;
			}
		});
		
		// Set name of sensor
		sName.setText(device.getName());
		sName.setBackgroundColor(Color.TRANSPARENT);
		sName.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				mMode = getSherlockActivity().startActionMode(new AnActionModeOfEpicProportions());
				sName.setVisibility(View.GONE);
				sNameEdit.setVisibility(View.VISIBLE);
				sNameEdit.setText(sName.getText());
				InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
				return true;
			}
		});
		// Set name of location
		if (mController != null) {
			Location location = mController.getLocationByDevice(device);
			if (location != null)
				sLocation.setText(location.getName());
		} else {
			Log.e(TAG, "mController is null (this shouldn't happen)");
			sLocation.setText(device.getLocationId());
		}
		// Set value of sensor
		sValue.setText(device.getStringValueUnit(getActivity()));
		// Set icon of sensor
		sIcon.setImageResource(device.getTypeIconResource());
		// Set time of sensor
		sTime.setText(setLastUpdate(device.lastUpdate));
		// Set refresh time Text
		sRefreshTimeText.setText( getString(R.string.refresh_time)+" "+prepareIntervalText(device.getRefresh()));
		// Set refresh time SeekBar
		sRefreshTimeValue.setProgress(prepareIntervalValue(device.getRefresh()));

		// Add Graph with history data
		addGraphView();
		// Disable progress bar
		getActivity().setProgressBarIndeterminateVisibility(false);
	}
	
	private void addGraphView() {
		mGraphView = new LineGraphView(
				getView().getContext() // context
		    , "" // heading
		);
		
		minimum = -1.0;
		
		GraphViewSeriesStyle seriesStyle = new GraphViewSeriesStyle(getResources().getColor(R.color.log_blue2),2);

		mGraphView.getGraphViewStyle().setVerticalLabelsColor(getResources().getColor(R.color.log_blue2));
		mGraphView.getGraphViewStyle().setHorizontalLabelsColor(getResources().getColor(R.color.log_blue2));
		mGraphView.setBackgroundColor(Color.argb(128, 0, 153, 204));//getResources().getColor(R.color.log_blue2));
		
		((LineGraphView) mGraphView).setDrawBackground(true);
		//graphView.setAlpha(128);
		// draw sin curve
		int num = 150;
		GraphView.GraphViewData[] data = new GraphView.GraphViewData[num];
		double v=0;
		for (int i=0; i<num; i++) {
		  v += 0.2;
		  if(i > 100 && i <120) {
			  data[i] = new GraphView.GraphViewData(i, -1);
		  }else 
		  {
			  data[i] = new GraphView.GraphViewData(i, Math.sin(v));
		  }
		  
		}

		 
		mGraphView.addSeries(new GraphViewSeries("Graph",seriesStyle,data));
		// set view port, start=2, size=40
		mGraphView.setViewPort(2, 40);
		mGraphView.setManualYAxis(true);
		mGraphView.setManualYAxisBounds(1.0, -1.0);
		
		

		//graphView.setScrollable(true);
		// optional - activate scaling / zooming
		//graphView.setScalable(true);
		sGraphLayout.addView(mGraphView);

		sGraphLayout.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(mWasTapGraph)
					return true;
				
				mWasTapLayout = false;
				mWasTapGraph = true;
				
				Log.d(TAG, "onTouch layout");
				mGraphView.setScrollable(true);
				mGraphView.setScalable(true);
				mActivity.setEnableSwipe(false);
				onTouch(v,event);
				return true;
			}
		});
		
	}

	private CharSequence setLastUpdate(Time lastUpdate) {
		// Last update time data
		Time yesterday = new Time();
		yesterday.setToNow();
		yesterday.set(yesterday.toMillis(true) - 24 * 60 * 60 * 1000); // -24 hours

		// If sync time is more that 24 ago, show only date. Show time otherwise.
		DateFormat dateFormat = yesterday.before(lastUpdate) ? DateFormat.getTimeInstance() : DateFormat.getDateInstance();
		
		Date lastUpdateDate = new Date(lastUpdate.toMillis(true));
		return dateFormat.format(lastUpdateDate);
	}

	private int prepareIntervalValue(int refresh) {
		int index = 0;
		if(refresh == 0 )
			return 0;
		for (int item : sRefreshTimeSeekBarValues ){
			if (item == refresh)
				return index;
			index++;
		}
		return sRefreshTimeSeekBarValues.length-1;
	}

	private String prepareIntervalText(int seconds) {
		int minutes = (int) seconds / 60;
		int hours = (int) seconds / 3600;
		if(hours == 0 ) {
			if(minutes == 0) {
				return String.valueOf(seconds)+ " " +  getString(R.string.second);
			}
			else {
				return String.valueOf(minutes)+ " " +  getString(R.string.minute);
			}
		}
		else {
			return String.valueOf(hours)+" "+ getString(R.string.hour);
		}
	}

	// Menu for Action bar mode - edit
	class AnActionModeOfEpicProportions implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			menu.add("Save").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add("Cancel").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			
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
			if(item.getTitle().equals("Save")) {
				sName.setText(sNameEdit.getText());
			}
			sNameEdit.setVisibility(View.GONE);
			sName.setVisibility(View.VISIBLE);
			
			sNameEdit.clearFocus();
			//getSherlockActivity().getCurrentFocus().clearFocus();
			InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(sNameEdit.getWindowToken(), 0);
			mode.finish();
            return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// TODO Auto-generated method stub
			sNameEdit.clearFocus();
			sNameEdit.setVisibility(View.GONE);
			sName.setVisibility(View.VISIBLE);
			mMode = null;

		}
	}

}
