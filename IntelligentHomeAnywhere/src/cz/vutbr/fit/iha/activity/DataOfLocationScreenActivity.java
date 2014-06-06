package cz.vutbr.fit.iha.activity;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import cz.vutbr.fit.iha.Compatibility;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.device.StateDevice;
import cz.vutbr.fit.iha.adapter.device.SwitchDevice;
import cz.vutbr.fit.iha.adapter.device.BaseDevice.SaveDevice;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.view.ToggleButtonOnClickListener;

/**
 * Class that handle screen with location data
 * @author ThinkDeep
 * @author Robyer
 *
 */
public class DataOfLocationScreenActivity extends Activity {

	private Controller mController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_of_locacion_screen);
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle == null) {
			this.finish();
			return;
		}
		
		mController = Controller.getInstance(this);

		String location = bundle.getString(Constants.LOCATION_CLICKED);
		Log.i("clicked ->",location);
		
		LinearLayout mainlayout = (LinearLayout)findViewById(R.id.dataoflocation_scroll);
		mainlayout.setOrientation(LinearLayout.VERTICAL);
		
		TextView txtvwTitleLocation = new TextView(this);
		txtvwTitleLocation.setText(location);
		txtvwTitleLocation.setTextSize(getResources().getDimension(R.dimen.textsize));
		LinearLayout.LayoutParams titleLocationParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		titleLocationParams.setMargins(15, 10, 0, 10);
		txtvwTitleLocation.setLayoutParams(titleLocationParams);
		mainlayout.addView(txtvwTitleLocation);
		
		List<BaseDevice> devices = mController.getDevicesByLocation(location);
		if (devices != null) {
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
			for (final BaseDevice device : devices) {
				
				RelativeLayout devicelayout = new RelativeLayout(this);
				devicelayout.setPadding(5, 10, 10, 10);
				devicelayout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
				
				final TextView txtvwLabelName = new TextView(this);
				txtvwLabelName.setText(device.getName());
				//txtvwLabelName.setId(Constants.DEVICENAMELABEL_ID);
				txtvwLabelName.setTextSize(getResources().getDimension(R.dimen.textsizesmaller));
				RelativeLayout.LayoutParams txtvwLabelParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
				txtvwLabelParams.setMargins(0, 5, 0, 0);
				txtvwLabelName.setLayoutParams(txtvwLabelParams);
				devicelayout.addView(txtvwLabelName);
				
				devicelayout.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(v.getContext(),SensorDetailActivity.class);
						intent.putExtra(Constants.DEVICE_CLICKED, device.getId());
						startActivity(intent);
					}
				});
				devicelayout.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						renameDevice(device, txtvwLabelName);

						return true;
					}
				});
				
				RelativeLayout.LayoutParams rightObjectParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				rightObjectParams.setMargins(0, 8, 0, 0);
				rightObjectParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				TextView txtview = new TextView(this);
				switch(device.getType()){
					case Constants.TYPE_TEMPERATURE:
						if(temperatureLayout == null){
							temperatureLayout = setLayout(temperatureLayout, ID);
							ID++;
						}
						if(temperatureLayout != null && temperatureLayout.getChildCount() > 0){
							temperatureLayout.addView(getSplitter());
						}
						txtview.setText(device.getStringValue() + " " + getString(device.getUnitStringResource()));
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						temperatureLayout.addView(devicelayout);
						break;
					case Constants.TYPE_HUMIDITY:
						if(humidityLayout == null){
							humidityLayout = setLayout(humidityLayout, ID);
							ID++;
						}
						if(humidityLayout != null && humidityLayout.getChildCount() > 0){
							humidityLayout.addView(getSplitter());
						}
						txtview.setText(device.getStringValue() + " " + getString(device.getUnitStringResource()));
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						humidityLayout.addView(devicelayout);
						break;
					case Constants.TYPE_PRESSURE:
						if(pressureLayout == null){
							pressureLayout = setLayout(pressureLayout, ID);
							ID++;
						}
						if(pressureLayout != null && pressureLayout.getChildCount() > 0){
							pressureLayout.addView(getSplitter());
						}
						txtview.setText(device.getStringValue() + " " + getString(device.getUnitStringResource()));
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						pressureLayout.addView(devicelayout);
						break;
					case Constants.TYPE_STATE:
						if(switchSensorLayout == null){
							switchSensorLayout = setLayout(switchSensorLayout, ID);
							ID++;
						}
						if(switchSensorLayout != null && switchSensorLayout.getChildCount() > 0){
							switchSensorLayout.addView(getSplitter());
						}
						txtview.setText(getString(((StateDevice)device).getStateStringResource()));
						txtview.setTextColor(((StateDevice)device).getColorByState());
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						switchSensorLayout.addView(devicelayout);
						break;
					case Constants.TYPE_SWITCH:
						txtvwLabelParams.setMargins(0, 20, 0, 0);
						ToggleButton toggle = new ToggleButton(this);
						toggle.setTextOff("OFF"); // FIXME: use string resources
						toggle.setTextOn("ON"); // FIXME: use string resources
						toggle.setChecked(((SwitchDevice)device).isActive());
						toggle.setOnClickListener(new ToggleButtonOnClickListener((SwitchDevice)device));
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
					case Constants.TYPE_ILLUMINATION:
						if(illuminationLayout == null){
							illuminationLayout = setLayout(illuminationLayout, ID);
							ID++;
						}
						if(illuminationLayout != null && illuminationLayout.getChildCount() > 0){
							illuminationLayout.addView(getSplitter());
						}
						txtview.setText(device.getStringValue() + " " + getString(device.getUnitStringResource()));
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						illuminationLayout.addView(devicelayout);
						break;
					case Constants.TYPE_NOISE:
						if(noiseLayout == null){
							noiseLayout = setLayout(noiseLayout, ID);
							ID++;
						}
						if(noiseLayout != null && noiseLayout.getChildCount() > 0){
							noiseLayout.addView(getSplitter());
						}
						txtview.setText(device.getStringValue() + " " + getString(device.getUnitStringResource()));
						txtview.setLayoutParams(rightObjectParams);
						devicelayout.addView(txtview);
						noiseLayout.addView(devicelayout);
						break;
					case Constants.TYPE_EMMISION:
						if(emissionLayout == null){
							emissionLayout = setLayout(emissionLayout, ID);
							ID++;
						}
						if(emissionLayout != null && emissionLayout.getChildCount() > 0){
							emissionLayout.addView(getSplitter());
						}
						txtview.setText(device.getStringValue() + " " + getString(device.getUnitStringResource()));
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

	protected void renameDevice(final BaseDevice device, final TextView view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(DataOfLocationScreenActivity.this);
		
		// TODO: use better layout than just single EditText
		final EditText edit = new EditText(DataOfLocationScreenActivity.this);
		edit.setText(device.getName());
		edit.selectAll();
		// TODO: show keyboard automatically
		
		builder.setCancelable(false)
			.setView(edit)
			.setTitle("Rename device")
			.setNegativeButton("Cancel", null)
			.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String newName = edit.getText().toString(); 
					device.setName(newName);
					
					// TODO: show loading while saving new name to server
					boolean saved = mController.saveDevice(device, SaveDevice.SAVE_NAME);
					
					String message = saved
							? String.format("Device was renamed to '%s'", device.getName())
							: "Device wasn't renamed due to error";
							
					Toast.makeText(DataOfLocationScreenActivity.this, message, Toast.LENGTH_LONG).show();
					
					// Redraw item in list
					view.setText(newName);
				}
			});
		
		AlertDialog dialog = builder.create();
		dialog.show();
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
