package cz.vutbr.fit.intelligenthomeanywhere.activity;

import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;

import cz.vutbr.fit.intelligenthomeanywhere.Compatibility;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.controller.Controller;
import cz.vutbr.fit.intelligenthomeanywhere.view.NumberPicker;
import cz.vutbr.fit.intelligenthomeanywhere.view.NumberPicker.OnChangedListener;

public class SensorDetailFragment extends SherlockFragment {
	
	private BaseDevice mDevice;
	private static final String TAG = "SensorDetail";


	@Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,	 Bundle savedInstanceState)
	 {
		
		Bundle bundle = this.getArguments();
        String sensorID = bundle.getString("sensorID");
        mDevice = Controller.getInstance(getActivity()).getDevice(sensorID);
        Log.d(TAG, "ID:" + mDevice.getId() + " Name:" + mDevice.getName());
        
		View view = inflater.inflate(R.layout.activity_sensor_detail_screen, container, false);
		return view;
	 }
	
	/*
	private void initLayout() {
		LinearLayout mainlayout = (LinearLayout) findViewById(R.id.sensordetail_scroll);
		mainlayout.setFocusable(true);
		mainlayout.setFocusableInTouchMode(true);
		mainlayout.requestFocus();
		mainlayout.setOrientation(LinearLayout.VERTICAL);
		
		TextView txtvwLocationLabel = new TextView(this);
		txtvwLocationLabel.setText(mDevice.getLocation().getName());
		txtvwLocationLabel.setTextSize(getResources().getDimension(R.dimen.textsize));
		LinearLayout.LayoutParams txtvwLocationParams = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		txtvwLocationParams.setMargins(15, 10, 0, 10);
		txtvwLocationLabel.setLayoutParams(txtvwLocationParams);
		mainlayout.addView(txtvwLocationLabel);
		
		TextView txtvwNameLabel = new TextView(this);
		txtvwNameLabel.setId(Constants.NAMELABEL_ID);
		txtvwNameLabel.setText(mDevice.getName());
		txtvwNameLabel.setTextSize(getResources().getDimension(R.dimen.textsize));
		txtvwNameLabel.setLayoutParams(txtvwLocationParams);
		mainlayout.addView(txtvwNameLabel);
		
		if(mDevice.isLogging()){
			//TODO: call to server for file with name device.GetLog();
			String filename = getExternalFilesDir(null).getPath() + Constants.DEMO_LOG_FILENAME;
			List<String[]> LogFile = LogLoader(filename);
			if (LogFile != null) {
				GraphViewData[] data = new GraphViewData[LogFile.size()];
				int i = 0;
				for (String[] s : LogFile) {
					data[i] = new GraphViewData(i+1,Integer.parseInt(s[2]));
					i++;
				}

				GraphView graph = new LineGraphView(this, "Temperature graph");
				graph.getGraphViewStyle().setHorizontalLabelsColor(getResources().getColor(R.color.darkblue));
				graph.getGraphViewStyle().setVerticalLabelsColor(getResources().getColor(R.color.darkblue));
				graph.setBackgroundColor(getResources().getColor(R.color.white));
				
				GraphViewSeries series = new GraphViewSeries(data);
				graph.addSeries(series);

				((LineGraphView) graph).setDrawBackground(true);
				((LineGraphView) graph).setBackgroundColor(getResources().getColor(R.color.darkblue));
				LinearLayout.LayoutParams graphParams = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,300);
				graphParams.setMargins(10, 0, 10, 0);
				graph.setLayoutParams(graphParams);
				mainlayout.addView(graph);
			}
		} else {
			//DEBUG: this should be used different
			View view = new View(this);
			LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 250);
			view.setLayoutParams(viewParams);
			mainlayout.addView(view);
		}
		
		switch (mDevice.getType()) {
			case Constants.TYPE_TEMPERATURE:
			case Constants.TYPE_HUMIDITY:
			case Constants.TYPE_PRESSURE:
			case Constants.TYPE_ILLUMINATION:
			case Constants.TYPE_NOISE:
			case Constants.TYPE_EMMISION:
				//type = Constants.DEVICE_TYPE_TEMP;

				int unitResource = mDevice.getUnitStringResource();
				String unit = (unitResource > 0) ? " " + getString(unitResource) : "";
				
				TextView lastValueLabel = new TextView(this);
				lastValueLabel.setText(getString(R.string.sensordetail_last_value) + mDevice.getStringValue() + unit);
				lastValueLabel.setTextSize(getResources().getDimension(R.dimen.textsizesmaller));
				LinearLayout.LayoutParams lastValueParams = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
				lastValueParams.setMargins(15, 10, 0, 0);
				lastValueLabel.setLayoutParams(lastValueParams);
				mainlayout.addView(lastValueLabel);
				
				break;
			case Constants.TYPE_STATE:
				
				break;
			case Constants.TYPE_SWITCH:
				
				break;
			default:
				break;
		}
		
		LinearLayout refreshLayout = new LinearLayout(this);
		refreshLayout.setOrientation(LinearLayout.HORIZONTAL);
		
		TextView refreshTimeLabel = new TextView(this);
		refreshTimeLabel.setText(getString(R.string.sensordetail_refresh_time));
		refreshTimeLabel.setTextSize(getResources().getDimension(R.dimen.textsizesmaller));
		LinearLayout.LayoutParams refreshTimeParams = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		refreshTimeParams.gravity = Gravity.CENTER_VERTICAL;
		refreshTimeLabel.setLayoutParams(refreshTimeParams);
		
		LinearLayout.LayoutParams refreshLayoutParams = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		refreshLayoutParams.setMargins(15, 0, 0, 0);
		refreshLayout.setLayoutParams(refreshLayoutParams);
		refreshLayout.addView(refreshTimeLabel);
		
		NumberPicker numberPicker = new NumberPicker(this);
		numberPicker.setId(Constants.NUMBERPICKER_IDII);
		LinearLayout.LayoutParams numberPickerParams = new LinearLayout.LayoutParams(80, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		numberPickerParams.setMargins(25, 0, 0, 0);
		numberPicker.setLayoutParams(numberPickerParams);
		numberPicker.setRange(-1, 100);
		numberPicker.setCurrent(GetRightTimeValue(mDevice.getRefresh()));
		numberPicker.setOnChangeListener(new OnChangedListener(){
			@Override
			public void onChanged(NumberPicker picker, int oldVal,int newVal) {
				int ID = Constants.NUMBERPICKER_ID;
				TextView timeUnit = (TextView)findViewById(ID);
				String unit = timeUnit.getText().toString();
				if(unit.equals("s")){
					if(newVal > 99){
						picker.setCurrent(1);
						timeUnit.setText("m");
					}else if(newVal <= 1){
						picker.setCurrent(1);
					}
				}else if(unit.equals("m")){
					if(newVal > 99){
						picker.setCurrent(1);
						timeUnit.setText("h");
					}else if(newVal < 1){
						picker.setCurrent(99);
						timeUnit.setText("s");
					}
				}else if(unit.equals("h")){
					if(newVal > 99){
						picker.setCurrent(99);
					}else if(newVal < 1){
						picker.setCurrent(99);
						timeUnit.setText("m");
					}
				}
			}
		});

		refreshLayout.addView(numberPicker);
		
		TextView timeUnitLabel = new TextView(this);
		int ID = Constants.NUMBERPICKER_ID;
		timeUnitLabel.setId(ID);
		timeUnitLabel.setText(GetRightTimeUnit(mDevice.getRefresh()));
		timeUnitLabel.setTextSize(getResources().getDimension(R.dimen.textsize));
		LinearLayout.LayoutParams tULparams = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		tULparams.gravity = Gravity.CENTER_VERTICAL;
		timeUnitLabel.setLayoutParams(tULparams);
		refreshLayout.addView(timeUnitLabel);
		
		mainlayout.addView(refreshLayout);

		LinearLayout batteryLayout = new LinearLayout(this);
		batteryLayout.setOrientation(LinearLayout.VERTICAL);
		batteryLayout.setLayoutParams(new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

		Compatibility.setBackground(batteryLayout, getResources().getDrawable(R.drawable.shape));
		
		TextView batteryState = new TextView(this);
		batteryState.setText(getString(R.string.sensordetail_battery_state));
		batteryState.setTextSize(getResources().getDimension(R.dimen.textsizesmaller));
		batteryState.setGravity(Gravity.CENTER_HORIZONTAL);
		LinearLayout.LayoutParams batteryStateParams = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		batteryStateParams.setMargins(0, 5, 0, 0);
		batteryState.setLayoutParams(batteryStateParams);
		batteryLayout.addView(batteryState);
		
		ProgressBar progressBar = new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
		progressBar.setProgress(mDevice.getBattery());

		LinearLayout.LayoutParams progressBarParams = new LinearLayout.LayoutParams(300, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		progressBarParams.gravity = Gravity.CENTER_HORIZONTAL;
		progressBarParams.setMargins(0, 5, 0, 5);
		progressBar.setLayoutParams(progressBarParams);

		batteryLayout.addView(progressBar);
		
		mainlayout.addView(batteryLayout);
	}
*/
}
