package cz.vutbr.fit.iha.activity;

import java.text.DateFormat;
import java.util.Date;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.controller.Controller;

public class SensorDetailFragment extends SherlockFragment {

	private Controller mController;
	private static final String TAG = "SensorDetail";
	
	// GUI elements
	private TextView sName;
	private TextView sLocation;
	private TextView sValue;
	private TextView sTime;
	private ImageView sIcon;
	private TextView sRefreshTimeText;
	private SeekBar sRefreshTimeValue;
	private LinearLayout sGraphLayout;
	
	// Array for refresh time constant
	// 1sec, 5sec, 10sec, 20sec , 30sec, 1min, 5min, 10min, 15min, 30,min, 1h, 2h,3h,4h, 8h, 12h, 24h
	private int[] sRefreshTimeSeekBarValues = { 1, 5, 10, 30, 60, 300, 600, 900, 1800,
			3600, 7200, 10800, 14400, 28800, 43200, 86400 };
			
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Get controller
		mController = Controller.getInstance(getActivity());

		Bundle bundle = this.getArguments();
		String sensorID = bundle.getString("sensorID");

		GetDeviceTask task = new GetDeviceTask();
		task.execute(new String[] { sensorID });

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

		// Set name of sensor
		sName.setText(device.getName());
		// Set name of location
		if (mController != null) {
			Location location = mController.getLocation(device.getLocationId());
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
		// init example series data
		/*GraphViewSeries exampleSeries = new GraphViewSeries(new GraphView.GraphViewData[] {
		    new GraphView.GraphViewData(1, 2.0d)
		    , new GraphView.GraphViewData(2, 1.5d)
		    , new GraphView.GraphViewData(3, 2.5d)
		    , new GraphView.GraphViewData(4, 1.0d)
		    , new GraphView.GraphViewData(4, 1.0d)
		    , new GraphView.GraphViewData(7, 1.0d)
		    , new GraphView.GraphViewData(4, 1.0d)
		    , new GraphView.GraphViewData(1, 1.0d)
		    , new GraphView.GraphViewData(0, 1.0d)
		    , new GraphView.GraphViewData(7, 1.0d)
		    
		});*/
		
		
		
		GraphView graphView = new LineGraphView(
				getView().getContext() // context
		    , "" // heading
		);
		
		graphView.getGraphViewStyle().setVerticalLabelsColor(getResources().getColor(R.color.log_blue2));
		graphView.getGraphViewStyle().setHorizontalLabelsColor(getResources().getColor(R.color.log_blue2));
		graphView.setBackgroundColor(Color.argb(128, 0, 153, 204));//getResources().getColor(R.color.log_blue2));
		
		((LineGraphView) graphView).setDrawBackground(true);
		//graphView.setAlpha(128);
		// draw sin curve
		int num = 150;
		GraphView.GraphViewData[] data = new GraphView.GraphViewData[num];
		double v=0;
		for (int i=0; i<num; i++) {
		  v += 0.2;
		  data[i] = new GraphView.GraphViewData(i, Math.sin(v));
		}
		graphView.addSeries(new GraphViewSeries(data));
		// set view port, start=2, size=40
		graphView.setViewPort(2, 40);
		
		graphView.setScrollable(true);
		// optional - activate scaling / zooming
		graphView.setScalable(true);
		sGraphLayout.addView(graphView);
		
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
	/*
	 * private void initLayout() { LinearLayout mainlayout = (LinearLayout)
	 * findViewById(R.id.sensordetail_scroll); mainlayout.setFocusable(true);
	 * mainlayout.setFocusableInTouchMode(true); mainlayout.requestFocus();
	 * mainlayout.setOrientation(LinearLayout.VERTICAL);
	 * 
	 * TextView txtvwLocationLabel = new TextView(this);
	 * txtvwLocationLabel.setText(mDevice.getLocation().getName());
	 * txtvwLocationLabel
	 * .setTextSize(getResources().getDimension(R.dimen.textsize));
	 * LinearLayout.LayoutParams txtvwLocationParams = new
	 * LinearLayout.LayoutParams
	 * (android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
	 * android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
	 * txtvwLocationParams.setMargins(15, 10, 0, 10);
	 * txtvwLocationLabel.setLayoutParams(txtvwLocationParams);
	 * mainlayout.addView(txtvwLocationLabel);
	 * 
	 * TextView txtvwNameLabel = new TextView(this);
	 * txtvwNameLabel.setId(Constants.NAMELABEL_ID);
	 * txtvwNameLabel.setText(mDevice.getName());
	 * txtvwNameLabel.setTextSize(getResources
	 * ().getDimension(R.dimen.textsize));
	 * txtvwNameLabel.setLayoutParams(txtvwLocationParams);
	 * mainlayout.addView(txtvwNameLabel);
	 * 
	 * if(mDevice.isLogging()){ //TODO: call to server for file with name
	 * device.GetLog(); String filename = getExternalFilesDir(null).getPath() +
	 * Constants.DEMO_LOG_FILENAME; List<String[]> LogFile =
	 * LogLoader(filename); if (LogFile != null) { GraphViewData[] data = new
	 * GraphViewData[LogFile.size()]; int i = 0; for (String[] s : LogFile) {
	 * data[i] = new GraphViewData(i+1,Integer.parseInt(s[2])); i++; }
	 * 
	 * GraphView graph = new LineGraphView(this, "Temperature graph");
	 * graph.getGraphViewStyle
	 * ().setHorizontalLabelsColor(getResources().getColor(R.color.darkblue));
	 * graph
	 * .getGraphViewStyle().setVerticalLabelsColor(getResources().getColor(R
	 * .color.darkblue));
	 * graph.setBackgroundColor(getResources().getColor(R.color.white));
	 * 
	 * GraphViewSeries series = new GraphViewSeries(data);
	 * graph.addSeries(series);
	 * 
	 * ((LineGraphView) graph).setDrawBackground(true); ((LineGraphView)
	 * graph).setBackgroundColor(getResources().getColor(R.color.darkblue));
	 * LinearLayout.LayoutParams graphParams = new
	 * LinearLayout.LayoutParams(android
	 * .view.ViewGroup.LayoutParams.MATCH_PARENT,300);
	 * graphParams.setMargins(10, 0, 10, 0); graph.setLayoutParams(graphParams);
	 * mainlayout.addView(graph); } } else { //DEBUG: this should be used
	 * different View view = new View(this); LinearLayout.LayoutParams
	 * viewParams = new
	 * LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams
	 * .MATCH_PARENT, 250); view.setLayoutParams(viewParams);
	 * mainlayout.addView(view); }
	 * 
	 * switch (mDevice.getType()) { case Constants.TYPE_TEMPERATURE: case
	 * Constants.TYPE_HUMIDITY: case Constants.TYPE_PRESSURE: case
	 * Constants.TYPE_ILLUMINATION: case Constants.TYPE_NOISE: case
	 * Constants.TYPE_EMMISION: //type = Constants.DEVICE_TYPE_TEMP;
	 * 
	 * int unitResource = mDevice.getUnitStringResource(); String unit =
	 * (unitResource > 0) ? " " + getString(unitResource) : "";
	 * 
	 * TextView lastValueLabel = new TextView(this);
	 * lastValueLabel.setText(getString(R.string.sensordetail_last_value) +
	 * mDevice.getStringValue() + unit);
	 * lastValueLabel.setTextSize(getResources(
	 * ).getDimension(R.dimen.textsizesmaller)); LinearLayout.LayoutParams
	 * lastValueParams = new
	 * LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams
	 * .WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
	 * lastValueParams.setMargins(15, 10, 0, 0);
	 * lastValueLabel.setLayoutParams(lastValueParams);
	 * mainlayout.addView(lastValueLabel);
	 * 
	 * break; case Constants.TYPE_STATE:
	 * 
	 * break; case Constants.TYPE_SWITCH:
	 * 
	 * break; default: break; }
	 * 
	 * LinearLayout refreshLayout = new LinearLayout(this);
	 * refreshLayout.setOrientation(LinearLayout.HORIZONTAL);
	 * 
	 * TextView refreshTimeLabel = new TextView(this);
	 * refreshTimeLabel.setText(getString(R.string.sensordetail_refresh_time));
	 * refreshTimeLabel
	 * .setTextSize(getResources().getDimension(R.dimen.textsizesmaller));
	 * LinearLayout.LayoutParams refreshTimeParams = new
	 * LinearLayout.LayoutParams
	 * (android.view.ViewGroup.LayoutParams.WRAP_CONTENT
	 * ,android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
	 * refreshTimeParams.gravity = Gravity.CENTER_VERTICAL;
	 * refreshTimeLabel.setLayoutParams(refreshTimeParams);
	 * 
	 * LinearLayout.LayoutParams refreshLayoutParams = new
	 * LinearLayout.LayoutParams
	 * (android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
	 * android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
	 * refreshLayoutParams.setMargins(15, 0, 0, 0);
	 * refreshLayout.setLayoutParams(refreshLayoutParams);
	 * refreshLayout.addView(refreshTimeLabel);
	 * 
	 * NumberPicker numberPicker = new NumberPicker(this);
	 * numberPicker.setId(Constants.NUMBERPICKER_IDII);
	 * LinearLayout.LayoutParams numberPickerParams = new
	 * LinearLayout.LayoutParams(80,
	 * android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
	 * numberPickerParams.setMargins(25, 0, 0, 0);
	 * numberPicker.setLayoutParams(numberPickerParams);
	 * numberPicker.setRange(-1, 100);
	 * numberPicker.setCurrent(GetRightTimeValue(mDevice.getRefresh()));
	 * numberPicker.setOnChangeListener(new OnChangedListener(){
	 * 
	 * @Override public void onChanged(NumberPicker picker, int oldVal,int
	 * newVal) { int ID = Constants.NUMBERPICKER_ID; TextView timeUnit =
	 * (TextView)findViewById(ID); String unit = timeUnit.getText().toString();
	 * if(unit.equals("s")){ if(newVal > 99){ picker.setCurrent(1);
	 * timeUnit.setText("m"); }else if(newVal <= 1){ picker.setCurrent(1); }
	 * }else if(unit.equals("m")){ if(newVal > 99){ picker.setCurrent(1);
	 * timeUnit.setText("h"); }else if(newVal < 1){ picker.setCurrent(99);
	 * timeUnit.setText("s"); } }else if(unit.equals("h")){ if(newVal > 99){
	 * picker.setCurrent(99); }else if(newVal < 1){ picker.setCurrent(99);
	 * timeUnit.setText("m"); } } } });
	 * 
	 * refreshLayout.addView(numberPicker);
	 * 
	 * TextView timeUnitLabel = new TextView(this); int ID =
	 * Constants.NUMBERPICKER_ID; timeUnitLabel.setId(ID);
	 * timeUnitLabel.setText(GetRightTimeUnit(mDevice.getRefresh()));
	 * timeUnitLabel.setTextSize(getResources().getDimension(R.dimen.textsize));
	 * LinearLayout.LayoutParams tULparams = new
	 * LinearLayout.LayoutParams(android
	 * .view.ViewGroup.LayoutParams.WRAP_CONTENT
	 * ,android.view.ViewGroup.LayoutParams.WRAP_CONTENT); tULparams.gravity =
	 * Gravity.CENTER_VERTICAL; timeUnitLabel.setLayoutParams(tULparams);
	 * refreshLayout.addView(timeUnitLabel);
	 * 
	 * mainlayout.addView(refreshLayout);
	 * 
	 * LinearLayout batteryLayout = new LinearLayout(this);
	 * batteryLayout.setOrientation(LinearLayout.VERTICAL);
	 * batteryLayout.setLayoutParams(new
	 * LinearLayout.LayoutParams(android.view.ViewGroup
	 * .LayoutParams.MATCH_PARENT
	 * ,android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
	 * 
	 * Compatibility.setBackground(batteryLayout,
	 * getResources().getDrawable(R.drawable.shape));
	 * 
	 * TextView batteryState = new TextView(this);
	 * batteryState.setText(getString(R.string.sensordetail_battery_state));
	 * batteryState
	 * .setTextSize(getResources().getDimension(R.dimen.textsizesmaller));
	 * batteryState.setGravity(Gravity.CENTER_HORIZONTAL);
	 * LinearLayout.LayoutParams batteryStateParams = new
	 * LinearLayout.LayoutParams
	 * (android.view.ViewGroup.LayoutParams.MATCH_PARENT
	 * ,android.view.ViewGroup.LayoutParams.MATCH_PARENT);
	 * batteryStateParams.setMargins(0, 5, 0, 0);
	 * batteryState.setLayoutParams(batteryStateParams);
	 * batteryLayout.addView(batteryState);
	 * 
	 * ProgressBar progressBar = new
	 * ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
	 * progressBar.setProgress(mDevice.getBattery());
	 * 
	 * LinearLayout.LayoutParams progressBarParams = new
	 * LinearLayout.LayoutParams(300,
	 * android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
	 * progressBarParams.gravity = Gravity.CENTER_HORIZONTAL;
	 * progressBarParams.setMargins(0, 5, 0, 5);
	 * progressBar.setLayoutParams(progressBarParams);
	 * 
	 * batteryLayout.addView(progressBar);
	 * 
	 * mainlayout.addView(batteryLayout); }
	 */

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
}
