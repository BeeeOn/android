package cz.vutbr.fit.intelligenthomeanywhere.activity;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.XmlDeviceParser;
import cz.vutbr.fit.intelligenthomeanywhere.widget.SensorWidgetProvider;
import cz.vutbr.fit.intelligenthomeanywhere.widget.WidgetUpdateService;

public class WidgetConfigurationActivity extends Activity {

	private static final String TAG = WidgetConfigurationActivity.class.getSimpleName();

	private int mAppWidgetId = 0;
	private Adapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        
        // no valid ID, so bail out
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
        
        // if the user press BACK, do not add any widget
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_CANCELED, resultValue);

        // TODO: rewrite better with use of proper class for working with such data
        mAdapter = XmlDeviceParser.fromFile(Constants.DEMO_COMMUNICATION);
        if (mAdapter == null || mAdapter.devices.size() == 0) {
        	// TODO: use string from resources
        	Toast.makeText(this, "No sensors available.\nTry to run application first.", Toast.LENGTH_LONG).show();
        	finish();
        	return;
        }
        
        setContentView(R.layout.activity_widget_configuration);
		
		initButtons();
        initSpinner();
        loadSettings();
	}
	
	/**
	 * Initialize listeners
	 */
	private void initButtons() {
		// Cancel button - close window without adding widget
		((Button)findViewById(R.id.btn_cancel)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                Log.d(TAG, "Cancel clicked");
				finish();
			}
		});
		
		// Add button - save widget and his settings
		((Button)findViewById(R.id.btn_add)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "OK clicked");
				
				if (!saveSettings())
		        	return;

		        Intent firstUpdate = new Intent(WidgetConfigurationActivity.this, SensorWidgetProvider.class);
		        firstUpdate.setAction("android.appwidget.action.APPWIDGET_UPDATE");
		        firstUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{mAppWidgetId});
		        WidgetConfigurationActivity.this.sendBroadcast(firstUpdate);
		        
                // return the original widget ID, found in onCreate()
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
			}
		});
	}

	private void initSpinner() {
        Spinner s = (Spinner)findViewById(R.id.sensor);
        ArrayAdapter<?> arrayAdapter = new ArrayAdapter<BaseDevice>(this, android.R.layout.simple_spinner_dropdown_item, mAdapter.devices);
        s.setAdapter(arrayAdapter);
	}
	
	private void loadSettings() {
		SharedPreferences settings = SensorWidgetProvider.getSettings(WidgetConfigurationActivity.this, mAppWidgetId);
		
		String address = settings.getString(Constants.WIDGET_PREF_DEVICE, "");
		if (address != "") {
			Spinner s = (Spinner)findViewById(R.id.sensor);

			for (int i = 0; i < mAdapter.devices.size(); i++) {
				BaseDevice device = mAdapter.devices.get(i);
				if (device.getAddress().equals(address)) {
					s.setSelection(i);
					break;
				}
			}
		}		
		
		EditText i = (EditText)findViewById(R.id.interval);
		int interval = settings.getInt(Constants.WIDGET_PREF_INTERVAL, WidgetUpdateService.UPDATE_INTERVAL_DEFAULT);
		interval = Math.max(interval, WidgetUpdateService.UPDATE_INTERVAL_MIN);		
		i.setText(Integer.toString(interval));
	}
	
	private boolean saveSettings() {
		SharedPreferences settings = SensorWidgetProvider.getSettings(WidgetConfigurationActivity.this, mAppWidgetId);
		SharedPreferences.Editor editor = settings.edit();
		
		Spinner spinner = (Spinner)findViewById(R.id.sensor);
		BaseDevice device = (BaseDevice)spinner.getSelectedItem();
		if (device == null) {
			// TODO: use string from resources
			Toast.makeText(this, "Select sensor from list", Toast.LENGTH_LONG).show();
			return false;
		}
		
		EditText edit = (EditText)findViewById(R.id.interval);
		String i = edit.getText().toString();
		if (i == null || i.length() == 0) {
			// TODO: use string from resources
			Toast.makeText(this, "Set update interval", Toast.LENGTH_LONG).show();
			return false;
		}
		
		int interval = Integer.parseInt(i);
		interval = Math.max(interval, WidgetUpdateService.UPDATE_INTERVAL_MIN);
		
		editor.putString(Constants.WIDGET_PREF_DEVICE, device.getAddress());	
		editor.putInt(Constants.WIDGET_PREF_INTERVAL, interval);
		editor.putBoolean(Constants.WIDGET_PREF_INITIALIZED, true);
        editor.commit();
        
        return true;
	}

}
