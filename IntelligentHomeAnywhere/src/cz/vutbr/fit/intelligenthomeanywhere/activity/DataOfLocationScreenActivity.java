package cz.vutbr.fit.intelligenthomeanywhere.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import cz.vutbr.fit.intelligenthomeanywhere.Compatibility;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Capabilities;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.Device;
import cz.vutbr.fit.intelligenthomeanywhere.view.ToggleButtonOnClickListener;

public class DataOfLocationScreenActivity extends Activity {

	private Capabilities _capabilities;
	private String _clicked;
	private View _pressed = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_of_locacion_screen);
		
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null){
			_capabilities = Constants.GetCapabilities();
			_clicked = bundle.getString(Constants.LOCATION_CLICKED);
			Log.i("clicked ->",_clicked);
		}else
			this.finish();
		
		LinearLayout mainlayout = (LinearLayout)findViewById(R.id.dataoflocation_scroll);
		mainlayout.setOrientation(LinearLayout.VERTICAL);
		
		TextView txtvwTitleLocation = new TextView(this);
		txtvwTitleLocation.setText(_clicked);
		txtvwTitleLocation.setTextSize(getResources().getDimension(R.dimen.textsize));
		LinearLayout.LayoutParams titleLocationParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		titleLocationParams.setMargins(15, 10, 0, 10);
		txtvwTitleLocation.setLayoutParams(titleLocationParams);
		mainlayout.addView(txtvwTitleLocation);
		
		ArrayList<String> names = _capabilities.GetNameByLocation(_clicked);
		if(names != null){
			int ID = Constants.IDLE;
			LinearLayout temperatureLayout = null;
			LinearLayout humidityLayout = null;
			LinearLayout pressureLayout = null;
			LinearLayout switchSensorLayout = null;
			LinearLayout switchControlLayout = null;
			LinearLayout illuminationLayout = null;
			LinearLayout noiseLayout = null;
			LinearLayout emissionLayout = null;
			LinearLayout unknownLayout = null;
			for(String name : names){
				
				Adapter device = _capabilities.GetDeviceByName(name);
				
				RelativeLayout devicelayout = new RelativeLayout(this);
				devicelayout.setPadding(5, 10, 10, 10);
				devicelayout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
				devicelayout.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(v.getContext(),SensorDetailActivity.class);
						intent.putExtra(Constants.DEVICE_CLICKED, ((TextView)((RelativeLayout)v).getChildAt(0)).getText());
						startActivity(intent);
					}
				});
				devicelayout.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						_pressed = v;
						Intent intent = new Intent(getBaseContext(), ChangeDeviceNameActivity.class);
						intent.putExtra(Constants.DEVICE_LONG_PRESS, ((TextView)((RelativeLayout)v).getChildAt(0)).getText());
						startActivity(intent);
						return false;
					}
				});
				TextView txtvwLabelName = new TextView(this);
				txtvwLabelName.setText(name);
				//txtvwLabelName.setId(Constants.DEVICENAMELABEL_ID);
				txtvwLabelName.setTextSize(getResources().getDimension(R.dimen.textsizesmaller));
				RelativeLayout.LayoutParams txtvwLabelParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
				txtvwLabelParams.setMargins(0, 5, 0, 0);
				txtvwLabelName.setLayoutParams(txtvwLabelParams);
				devicelayout.addView(txtvwLabelName);
				
				RelativeLayout.LayoutParams rightObjectParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				rightObjectParams.setMargins(0, 8, 0, 0);
				rightObjectParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				TextView txtview = new TextView(this);
				switch(((Device)device).GetType()){
					case 0: // temperature
						if(temperatureLayout == null){
							temperatureLayout = setLayout(temperatureLayout, ID);
							ID++;
						}
						if(temperatureLayout != null && temperatureLayout.getChildCount() > 0){
							temperatureLayout.addView(getSplitter());
						}
						txtview.setText(((Device) device).deviceDestiny.GetValue() + " " + getString(R.string.temperature_C) );
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						temperatureLayout.addView(devicelayout);
						break;
					case 1: // humidity
						if(humidityLayout == null){
							humidityLayout = setLayout(humidityLayout, ID);
							ID++;
						}
						if(humidityLayout != null && humidityLayout.getChildCount() > 0){
							humidityLayout.addView(getSplitter());
						}
						txtview.setText(((Device) device).deviceDestiny.GetValue() + " " + getString(R.string.humidity_percent));
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						humidityLayout.addView(devicelayout);
						break;
					case 2: // pressure
						if(pressureLayout == null){
							pressureLayout = setLayout(pressureLayout, ID);
							ID++;
						}
						if(pressureLayout != null && pressureLayout.getChildCount() > 0){
							pressureLayout.addView(getSplitter());
						}
						txtview.setText(((Device) device).deviceDestiny.GetValue() + " " + getString(R.string.pressure_Pa));
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						pressureLayout.addView(devicelayout);
						break;
					case 3: // sensor switch
						if(switchSensorLayout == null){
							switchSensorLayout = setLayout(switchSensorLayout, ID);
							ID++;
						}
						if(switchSensorLayout != null && switchSensorLayout.getChildCount() > 0){
							switchSensorLayout.addView(getSplitter());
						}
						txtview.setText(Constants.isOpen(((Device) device).deviceDestiny.GetValue()));
						txtview.setTextColor(Constants.isOn(((Device) device).deviceDestiny.GetValue()));
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						switchSensorLayout.addView(devicelayout);
						break;
					case 4: // controll switch
						txtvwLabelParams.setMargins(0, 20, 0, 0);
						ToggleButton toggle = new ToggleButton(this);
						toggle.setTextOff("OFF");
						toggle.setTextOn("ON");
						toggle.setChecked((((Device)device).deviceDestiny.GetValue().equals("ON") ? true : false));
						toggle.setOnClickListener(new ToggleButtonOnClickListener(device.GetName()));
						if(switchControlLayout == null){
							switchControlLayout = setLayout(switchControlLayout, ID);
						}
						if(switchControlLayout != null && switchControlLayout.getChildCount() > 0){
							switchControlLayout.addView(getSplitter());
						}
						rightObjectParams.setMargins(0, 0, 0, 0);
						toggle.setLayoutParams(rightObjectParams);
						devicelayout.addView(toggle);
						switchControlLayout.addView(devicelayout);
						break;
					case 5: // illumination
						if(illuminationLayout == null){
							illuminationLayout = setLayout(illuminationLayout, ID);
							ID++;
						}
						if(illuminationLayout != null && illuminationLayout.getChildCount() > 0){
							illuminationLayout.addView(getSplitter());
						}
						txtview.setText(((Device) device).deviceDestiny.GetValue() + " " + getString(R.string.illumination_lux));
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						illuminationLayout.addView(devicelayout);
						break;
					case 6: // noise
						if(noiseLayout == null){
							noiseLayout = setLayout(noiseLayout, ID);
							ID++;
						}
						if(noiseLayout != null && noiseLayout.getChildCount() > 0){
							noiseLayout.addView(getSplitter());
						}
						txtview.setText(((Device) device).deviceDestiny.GetValue() + " " + getString(R.string.noise_dB));
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						noiseLayout.addView(devicelayout);
						break;
					case 7: // emission
						if(emissionLayout == null){
							emissionLayout = setLayout(emissionLayout, ID);
							ID++;
						}
						if(emissionLayout != null && emissionLayout.getChildCount() > 0){
							emissionLayout.addView(getSplitter());
						}
						txtview.setText(((Device) device).deviceDestiny.GetValue() + " " + getString(R.string.emission_ppm));
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						emissionLayout.addView(devicelayout);
						break;
					default: // unknown type - possibly new one
						if(unknownLayout == null){
							unknownLayout = setLayout(unknownLayout, ID);
							ID++;
						}
						txtview.setText(getString(R.string.update_needed));
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						unknownLayout.addView(devicelayout);
						break;
				}

			}
			if(temperatureLayout != null)
				mainlayout.addView(temperatureLayout);
			if(humidityLayout != null)
				mainlayout.addView(humidityLayout);
			if(pressureLayout != null)
				mainlayout.addView(pressureLayout);
			if(switchSensorLayout != null)
				mainlayout.addView(switchSensorLayout);
			if(switchControlLayout != null)
				mainlayout.addView(switchControlLayout);
			if(illuminationLayout != null)
				mainlayout.addView(illuminationLayout);
			if(noiseLayout != null)
				mainlayout.addView(noiseLayout);
			if(emissionLayout != null)
				mainlayout.addView(emissionLayout);
			if(unknownLayout != null)
				mainlayout.addView(unknownLayout);
		}else
			this.finish();
	}

	public void onResume(){
		super.onResume();
		
		if(Constants.GetCapabilities().isNewDeviceName()){
			((TextView)((RelativeLayout)_pressed).getChildAt(0)).setText(Constants.GetCapabilities().GetNewDeviceName());
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.data_of_locacion_screen, menu);
		return true;
	}
	
	/**
	 * Setting up layout for group of device type
	 * @param devicelayout quite not used parameter
	 * @param ID identification of layout
	 * @return ready layout
	 */
	private LinearLayout setLayout(LinearLayout devicelayout,int ID){
		devicelayout = new LinearLayout(this);
		devicelayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		params.setMargins(10, 5, 10, 0);
		devicelayout.setLayoutParams(params);
		devicelayout.setId(ID);

		Compatibility.setBackground(devicelayout, getResources().getDrawable(R.drawable.shape));

		return devicelayout;
	}

	/**
	 * Calling for splitter between devices in one group of types
	 * @return View with splitter
	 */
	private View getSplitter(){
		View splitter = new View(this);
		LinearLayout.LayoutParams splitterParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1);
		splitter.setBackgroundColor(getResources().getColor(R.color.white));
		splitter.setLayoutParams(splitterParams);
		return splitter;
	}
}
