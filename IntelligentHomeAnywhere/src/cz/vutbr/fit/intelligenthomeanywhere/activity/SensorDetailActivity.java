package cz.vutbr.fit.intelligenthomeanywhere.activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import cz.vutbr.fit.intelligenthomeanywhere.Compatibility;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.view.NumberPicker;
import cz.vutbr.fit.intelligenthomeanywhere.view.NumberPicker.OnChangedListener;
import cz.vutbr.fit.intelligenthomeanywhere.R;

/**
 * Class that handle screen with detail of some sensor
 * @author ThinkDeep
 *
 */
public class SensorDetailActivity extends Activity {

	//private int type = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor_detail_screen);
		
		String name = this.getIntent().getExtras().getString(Constants.DEVICE_CLICKED);
		BaseDevice device = Constants.getAdapter().getDeviceByName(name);
		Log.i("Click on",device.getName());
		
		LinearLayout mainlayout = (LinearLayout) findViewById(R.id.sensordetail_scroll);
		mainlayout.setFocusable(true);
		mainlayout.setFocusableInTouchMode(true);
		mainlayout.requestFocus();
		mainlayout.setOrientation(LinearLayout.VERTICAL);
		
		TextView txtvwLocationLabel = new TextView(this);
		txtvwLocationLabel.setText(device.getLocation());
		txtvwLocationLabel.setTextSize(getResources().getDimension(R.dimen.textsize));
		LinearLayout.LayoutParams txtvwLocationParams = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		txtvwLocationParams.setMargins(15, 10, 0, 10);
		txtvwLocationLabel.setLayoutParams(txtvwLocationParams);
		mainlayout.addView(txtvwLocationLabel);
		
		TextView txtvwNameLabel = new TextView(this);
		txtvwNameLabel.setId(Constants.NAMELABEL_ID);
		txtvwNameLabel.setText(name);
		txtvwNameLabel.setTextSize(getResources().getDimension(R.dimen.textsize));
		txtvwNameLabel.setLayoutParams(txtvwLocationParams);
		mainlayout.addView(txtvwNameLabel);
		
		if(device.isLogging()){
			//TODO: call to server for file with name device.GetLog(); 
			List<String[]> LogFile = LogLoader(Constants.DEMO_LOGFILE);
			if(LogFile != null){
				GraphViewData[] data = new GraphViewData[LogFile.size()];
				int i = 0;
				for(String[] s : LogFile){
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
		}else{
			//DEBUG: this should be used different
			View view = new View(this);
			LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 250);
			view.setLayoutParams(viewParams);
			mainlayout.addView(view);
		}
		
		switch(device.getType()){
			case Constants.TYPE_TEMPERATURE:
			case Constants.TYPE_HUMIDITY:
			case Constants.TYPE_PRESSURE:
			case Constants.TYPE_ILLUMINATION:
			case Constants.TYPE_NOISE:
			case Constants.TYPE_EMMISION:
				//type = Constants.DEVICE_TYPE_TEMP;

				int unitResource = device.getUnitStringResource();
				String unit = (unitResource > 0) ? " " + getString(unitResource) : "";
				
				TextView lastValueLabel = new TextView(this);
				lastValueLabel.setText(getString(R.string.sensordetail_last_value) + device.getStringValue() + unit);
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
		numberPicker.setCurrent(GetRightTimeValue(device.getRefresh()));
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
		timeUnitLabel.setText(GetRightTimeUnit(device.getRefresh()));
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
		progressBar.setProgress(device.getBattery());

		LinearLayout.LayoutParams progressBarParams = new LinearLayout.LayoutParams(300, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		progressBarParams.gravity = Gravity.CENTER_HORIZONTAL;
		progressBarParams.setMargins(0, 5, 0, 5);
		progressBar.setLayoutParams(progressBarParams);

		batteryLayout.addView(progressBar);
		
		mainlayout.addView(batteryLayout);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sensor_detail, menu);
		return true;
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		
		/*switch(type){
			case Constants.DEVICE_TYPE_TEMP:*/
				TextView name = (TextView)findViewById(Constants.NAMELABEL_ID);
				BaseDevice device = Constants.getAdapter().getDeviceByName(name.getText().toString());
				device.setRefresh(GetRightTimeValueInSecs());
		//}
	}
	
	/**
	 * Method for choosing unit by seconds value
	 * @param timeInSecs refresh interval in seconds
	 * @return first letter of time unit (s - seconds, m - minutes, h - hours)
	 */
	private String GetRightTimeUnit(int timeInSecs){
		if(timeInSecs >= 99){ // minutes
			if(timeInSecs >= 99*60){ // hours
				return "h";
			}else{
				return "m";
			}
		}else
			return "s";
	}

	/**
	 * Method for calculate current time to seconds
	 * @return refresh time in seconds
	 */
	private int GetRightTimeValueInSecs(){
		int result = 0;
		int ID = Constants.NUMBERPICKER_ID;
		TextView timeUnit = (TextView)findViewById(ID);
		String unit = timeUnit.getText().toString();
		NumberPicker picker = (NumberPicker)findViewById(Constants.NUMBERPICKER_IDII);
		if(unit.equals("s")){
			result = picker.getCurrent();
		}else if(unit.equals("m")){
			result = picker.getCurrent()*60;
		}else if(unit.equals("h")){
			result = picker.getCurrent()*60*60;
		}
		return result;
	}
	
	/**
	 * Method that calculate transfer between time units
	 * @param timeInSecs value of seconds
	 * @return new value of time, in right unit
	 */
	private int GetRightTimeValue(int timeInSecs){
		int result = 0;
		if(timeInSecs >= 99){ // minutes
			if(timeInSecs >= 99*60){ // hours
				result = (timeInSecs/60)/60;
				if(result > 99)
					return 99;
			}else {
				result = timeInSecs/60;
			}
		}else
			return timeInSecs;
		
		return result;
	}
	
	/**
	 * Open sensor demo file and load it
	 * @param filename of sensor demo log
	 * @return List of String array with content of log file
	 */
	private List<String[]> LogLoader(String filename){
		File file = new File(filename);
		try{
			BufferedReader in  = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = null;
			List<String[]> result = new ArrayList<String[]>();
			while((line = in.readLine()) != null){
				String[] splited = line.split("\\s+");
				result.add(splited);
			}	
			in.close();
			return result;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

}
