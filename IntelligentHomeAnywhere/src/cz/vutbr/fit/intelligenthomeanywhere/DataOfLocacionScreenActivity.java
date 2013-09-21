package cz.vutbr.fit.intelligenthomeanywhere;

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

public class DataOfLocacionScreenActivity extends Activity {

	private Capabilities _capabilities;
	private String _clicked;
	
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
			for(String name : names){
				
				Adapter device = _capabilities.GetDeviceByName(name);
				
				RelativeLayout devicelayout = new RelativeLayout(this);
				devicelayout.setPadding(5, 10, 10, 0);
				devicelayout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,73));
				devicelayout.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(v.getContext(),SensorDetailActivity.class);
						intent.putExtra(Constants.DEVICE_CLICKED, ((TextView)((RelativeLayout)v).getChildAt(0)).getText());
						startActivity(intent);
					}
				});
				
				TextView txtvwLabelName = new TextView(this);
				txtvwLabelName.setText(name);
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
						txtview.setText(((Device) device).deviceDestiny.GetValue() + " °C");
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						temperatureLayout.addView(devicelayout);
						break;
					case 1: // humidity
						if(humidityLayout == null){
							humidityLayout = setLayout(humidityLayout, ID);
							ID++;
						}
						txtview.setText(((Device) device).deviceDestiny.GetValue() + " %");
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						humidityLayout.addView(devicelayout);
						break;
					case 2: // pressure
						if(pressureLayout == null){
							pressureLayout = setLayout(pressureLayout, ID);
							ID++;
						}
						txtview.setText(((Device) device).deviceDestiny.GetValue() + " hPa");
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						pressureLayout.addView(devicelayout);
						break;
					case 3: // sensor switch
						if(switchSensorLayout == null){
							switchSensorLayout = setLayout(switchSensorLayout, ID);
							ID++;
						}
						txtview.setText(Constants.isOpen(((Device) device).deviceDestiny.GetValue()));
						txtview.setTextColor(Constants.isOn(((Device) device).deviceDestiny.GetValue()));
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						switchSensorLayout.addView(devicelayout);
						break;
					case 4: // controll switch
						ToggleButton toggle = new ToggleButton(this);
						toggle.setTextOff("OFF");
						toggle.setTextOn("ON");
						toggle.setChecked((((Device)device).deviceDestiny.GetValue().equals("ON") ? true : false));
						toggle.setOnClickListener(new ToggleButtonOnClickListener(device.GetName()));
						if(switchControlLayout == null){
							switchControlLayout = setLayout(switchControlLayout, ID);
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
						txtview.setText(((Device) device).deviceDestiny.GetValue() + " lux");
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						illuminationLayout.addView(devicelayout);
						break;
					case 6: // noise
						if(noiseLayout == null){
							noiseLayout = setLayout(noiseLayout, ID);
							ID++;
						}
						txtview.setText(((Device) device).deviceDestiny.GetValue() + " dB");
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						noiseLayout.addView(devicelayout);
						break;
					case 7: // emission
						if(emissionLayout == null){
							emissionLayout = setLayout(emissionLayout, ID);
							ID++;
						}
						txtview.setText(((Device) device).deviceDestiny.GetValue() + " ppm");
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						emissionLayout.addView(devicelayout);
						break;
					default: // unknown type - possibly new one
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
		}else
			this.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.data_of_locacion_screen, menu);
		return true;
	}
	
	private LinearLayout setLayout(LinearLayout devicelayout,int ID){
		devicelayout = new LinearLayout(this);
		devicelayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		params.setMargins(10, 5, 10, 0);
		devicelayout.setLayoutParams(params);
		devicelayout.setId(ID);
		//XXX: setBackground from API 16
		devicelayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape));
		devicelayout.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(),SensorDetailActivity.class);
				intent.putExtra(Constants.DEVICE_CLICKED, ((TextView)((LinearLayout)v).getChildAt(0)).getText());
				startActivity(intent);
			}
		});
		return devicelayout;
	}

}
