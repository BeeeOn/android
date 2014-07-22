package cz.vutbr.fit.iha.activity;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

import cz.vutbr.fit.iha.LocationArrayAdapter;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.RefreshInterval;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.adapter.location.Location.DefaultRoom;
import cz.vutbr.fit.iha.controller.Controller;
//import android.widget.LinearLayout;
import cz.vutbr.fit.iha.view.CustomViewPager;

public class SensorDetailFragment extends SherlockFragment {

	private Controller mController;
	private static final String TAG = "SensorDetailFragment";
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
	private LinearLayout mGraphLayout;
	private RelativeLayout mLayout;
	private RelativeLayout mRectangleName;
	private RelativeLayout mRectangleLoc;
	private Spinner mSpinnerLoc;
	
	private SensorDetailActivity mActivity;
	private GraphView mGraphView; 
	//private CustomViewPager mPager;
	
	public static final String ARG_PAGE = "page";
	
	private String mPageNumber;
	
	private boolean mWasTapLayout = false;
	private boolean mWasTapGraph = false;
	private int mEditMode = EDIT_NONE;
	
	// 
	ActionMode mMode;
			
	public double minimum;
	private int mLastProgressRefreshTime;
	
	
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
		
		View view = inflater.inflate(R.layout.activity_sensor_detail_screen,
				container, false);
		
		return view;
	}
	
	@Override
	public void onActivityCreated (Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		GetDeviceTask task = new GetDeviceTask();
		task.execute(new String[] { mPageNumber });
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
		final Context context = SensorDetailFragment.this.getView().getContext(); 
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
		mRefreshTimeText = (TextView) getView().findViewById(R.id.sen_refresh_time);
		// Get SeekBar for refresh time
		mRefreshTimeValue = (SeekBar) getView().findViewById(R.id.sen_refresh_time_seekBar);
		// Set Max value by length of array with values
		mRefreshTimeValue.setMax(RefreshInterval.values().length-1);
		mRefreshTimeValue.setOnSeekBarChangeListener(new OnSeekBarChangeListener() { 

		    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		    	String interval = RefreshInterval.values()[progress].getStringInterval(context);
		    	mRefreshTimeText.setText(String.format(getString(R.string.refresh_time), interval));
		    }

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				mEditMode = EDIT_REFRESH_T;
				mMode = getSherlockActivity().startActionMode(new AnActionModeOfEpicProportions());
				mLastProgressRefreshTime = seekBar.getProgress();
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				String interval = RefreshInterval.values()[seekBar.getProgress()].getStringInterval(context);
				Log.d(TAG, String.format("Stop select value %s", interval));
			}
		});
		// Get LinearLayout for graph
		mGraphLayout = (LinearLayout) getView().findViewById(R.id.sen_graph_layout);
		
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
		mName.setText(device.getName());
		mName.setBackgroundColor(Color.TRANSPARENT);
		mName.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mEditMode = EDIT_NAME;
				mMode = getSherlockActivity().startActionMode(new AnActionModeOfEpicProportions());
				mName.setVisibility(View.GONE);
				mRectangleName.setVisibility(View.GONE);
				mNameEdit.setVisibility(View.VISIBLE);
				mNameEdit.setText(mName.getText());
				InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
				//return true;
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
						mEditMode = EDIT_LOC;
						mMode = getSherlockActivity().startActionMode(new AnActionModeOfEpicProportions());
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
		LocationArrayAdapter dataAdapter = new LocationArrayAdapter(this.getActivity(), R.layout.custom_spinner_item, getLocationsArray());
		dataAdapter.setLayoutInflater(getLayoutInflater(null));
		dataAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);

		mSpinnerLoc.setAdapter(dataAdapter);
		
		// Set value of sensor
		mValue.setText(device.getStringValueUnit(getActivity()));
		// Set icon of sensor
		mIcon.setImageResource(device.getTypeIconResource());
		// Set time of sensor
		mTime.setText(setLastUpdate(device.lastUpdate));
		// Set refresh time Text
    	mRefreshTimeText.setText(getString(R.string.refresh_time, device.getRefresh().getStringInterval(context)));
		// Set refresh time SeekBar
		mRefreshTimeValue.setProgress(device.getRefresh().getIntervalIndex());
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
		mGraphLayout.addView(mGraphView);

		mGraphLayout.setOnTouchListener(new OnTouchListener() {
			
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
	
	
	private  List<Location> getLocationsArray(){
		// Get locations from adapter
		List<Location> locations = mController.getLocations();
		
		// Add "missing" default rooms
		for (DefaultRoom room : Location.defaults) {
			String name = getString(room.rName);
			
			boolean found = false;
			for (Location location : locations) {
				if (location.getName().equals(name)) {
					found = true;
					break;
				}
			}
			
			if (!found) {
				locations.add(new Location(Location.NEW_LOCATION_ID, name, room.type));	
			}
		}
		
		// Sort them
		Collections.sort(locations);
		
		// Add "New location" item
		locations.add(new Location(Location.NEW_LOCATION_ID, getString(R.string.addsensor_new_location_spinner), 0));
		
		return locations;
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

	// Menu for Action bar mode - edit
	class AnActionModeOfEpicProportions implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			//View view = LayoutInflater.from(mActivity).inflate(R.layout.custom_actionmode_item, null);
			//((Button) view.findViewById(R.id.actionmode_button)).
			//menu.add("Save").setActionView(view).setIcon(R.drawable.ic_action_accept).setTitle("Save").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add("Save").setIcon(R.drawable.ic_action_accept).setTitle("Save").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add("Cancel").setIcon(R.drawable.ic_action_cancel).setTitle("Cancel").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			
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
			InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			
			switch(mEditMode) {
			case EDIT_LOC:
				mSpinnerLoc.setVisibility(View.GONE);
				mLocation.setVisibility(View.VISIBLE);
				mRectangleLoc.setVisibility(View.VISIBLE);
				break;
			case EDIT_NAME:
				if(item.getTitle().equals("Save")) {
					mName.setText(mNameEdit.getText());
				} 
				mNameEdit.setVisibility(View.GONE);
				mName.setVisibility(View.VISIBLE);
				mRectangleName.setVisibility(View.VISIBLE);
				
				mNameEdit.clearFocus();
				imm.hideSoftInputFromWindow(mNameEdit.getWindowToken(), 0);
				break;
			case EDIT_REFRESH_T:
				// set actual progress
				
				
				// Was clicked on cancel 
				if(item.getTitle().equals("Cancel")) {
					mRefreshTimeValue.setProgress(mLastProgressRefreshTime);
				}
				break;
			
			default:
				break;
				
			}
			
			
			
						
			mode.finish();
            return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// TODO Auto-generated method stub
			//mName.setText(mNameEdit.getText());
			//mNameEdit.clearFocus();
			//mNameEdit.setVisibility(View.GONE);
			//mName.setVisibility(View.VISIBLE);
			//mMode = null;

		}
	}

}
