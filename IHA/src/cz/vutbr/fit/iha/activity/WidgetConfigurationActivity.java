package cz.vutbr.fit.iha.activity;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.view.Window;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.widget.SensorWidgetProvider;
import cz.vutbr.fit.iha.widget.WidgetUpdateService;

public class WidgetConfigurationActivity extends BaseActivity {

	private static final String TAG = WidgetConfigurationActivity.class.getSimpleName();

	private int mAppWidgetId = 0;

	private List<Adapter> mAdapters = new ArrayList<Adapter>();
	private List<BaseDevice> mDevices = new ArrayList<BaseDevice>();
	
	private boolean isInitialized = false;
	private boolean triedLoginAlready = false;
	
	private Controller mController;
	
	private ChangeAdapterTask mChangeAdapterTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setSupportProgressBarIndeterminate(true);
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		// no valid ID, so bail out
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
			return;
		}
		
		mController = Controller.getInstance(getApplicationContext());

		// if the user press BACK, do not add any widget
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		setResult(RESULT_CANCELED, resultValue);

		setContentView(R.layout.activity_widget_configuration);		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		mAdapters = mController.getAdapters();
		if (mAdapters.isEmpty()) {
			if (!mController.isLoggedIn() && !triedLoginAlready) {
				// If user is not logged in we redirect to LoginActivity
				triedLoginAlready = true;
				Toast.makeText(this, "You must be logged in first.", Toast.LENGTH_LONG).show(); // FIXME: use string from resources
				BaseApplicationActivity.redirectToLogin(this);
			} else {
				// Otherwise he is logged in but has no sensors, we quit completely
				Toast.makeText(this, "You have no adapters and thus no sensors to use in widget.", Toast.LENGTH_LONG).show(); // FIXME: use string from resources
				finish();
			}

			return;
		} else {
			triedLoginAlready = false;
		}
		
		if (!isInitialized) {
			isInitialized = true;

			initButtons();
			initSpinners();
			loadSettings();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (mChangeAdapterTask != null) {
			mChangeAdapterTask.cancel(true);
		}
	}

	/**
	 * Initialize listeners
	 */
	private void initButtons() {
		// Cancel button - close window without adding widget
		((Button) findViewById(R.id.btn_cancel)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Cancel clicked");
				finish();
			}
		});

		// Add button - save widget and his settings
		((Button) findViewById(R.id.btn_add)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "OK clicked");

				if (!saveSettings())
					return;

				Intent firstUpdate = new Intent(WidgetConfigurationActivity.this, SensorWidgetProvider.class);
				firstUpdate.setAction("android.appwidget.action.APPWIDGET_UPDATE");
				firstUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { mAppWidgetId });
				WidgetConfigurationActivity.this.sendBroadcast(firstUpdate);

				// return the original widget ID, found in onCreate()
				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				setResult(RESULT_OK, resultValue);
				finish();
			}
		});
	}

	private void initSpinners() {
		Spinner s = (Spinner) findViewById(R.id.adapter);
		ArrayAdapter<?> arrayAdapter = new ArrayAdapter<Adapter>(this, android.R.layout.simple_spinner_dropdown_item, mAdapters);
		s.setAdapter(arrayAdapter);
		s.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Spinner s = (Spinner) findViewById(R.id.sensor);
				s.setEnabled(false);

				Adapter adapter = mAdapters.get(position);
				
				setProgressBarIndeterminateVisibility(true);
				mChangeAdapterTask = new ChangeAdapterTask();
				mChangeAdapterTask.execute(new String[] { adapter.getId() });
				
				List<Facility> facilities = mController.getFacilitiesByAdapter(adapter.getId());
				
				mDevices.clear();
				for (Facility facility : facilities) {
					mDevices.addAll(facility.getDevices());
				}
				
				Log.d(TAG, "Selected adapter " + adapter.getName());

				ArrayAdapter<?> arrayAdapter = new ArrayAdapter<BaseDevice>(WidgetConfigurationActivity.this, android.R.layout.simple_spinner_dropdown_item, mDevices);
				s.setAdapter(arrayAdapter);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				Log.d(TAG, "Selected no adapter");
				
				mDevices.clear();
			}
			
		});
		
		s = (Spinner) findViewById(R.id.sensor);
		s.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				BaseDevice device = (BaseDevice) parent.getSelectedItem();
				EditText intervalText = (EditText) findViewById(R.id.interval_sensor);
				intervalText.setText(device.getFacility().getRefresh().getStringInterval(WidgetConfigurationActivity.this));
				
				Log.d(TAG, "Selected device " + device.getName());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				EditText interval = (EditText) findViewById(R.id.interval_sensor);
				interval.setText("");
				
				Log.d(TAG, "Selected no device ");
			}

		});
	}

	private void loadSettings() {
		SharedPreferences settings = SensorWidgetProvider.getSettings(WidgetConfigurationActivity.this, mAppWidgetId);

		Spinner spinAdapter = (Spinner) findViewById(R.id.adapter);
		Spinner spinSensor = (Spinner) findViewById(R.id.sensor);
		
		String adapterId = settings.getString(Constants.WIDGET_PREF_DEVICE_ADAPTER_ID, "");
		if (adapterId != "") {
			for (int i = 0; i < mAdapters.size(); i++) {
				if (mAdapters.get(i).getId().equals(adapterId)) {
					spinAdapter.setSelection(i);
					
					List<Facility> facilities = mController.getFacilitiesByAdapter(adapterId);
					
					mDevices.clear();
					for (Facility facility : facilities) {
						mDevices.addAll(facility.getDevices());
					}

					ArrayAdapter<?> arrayAdapter = new ArrayAdapter<BaseDevice>(WidgetConfigurationActivity.this, android.R.layout.simple_spinner_dropdown_item, mDevices);
					spinSensor.setAdapter(arrayAdapter);					

					break;
				}
			}
		}

		String id = settings.getString(Constants.WIDGET_PREF_DEVICE, "");
		if (adapterId != "" && id != "") {
			for (int i = 0; i < mDevices.size(); i++) {
				if (mDevices.get(i).getId().equals(id)) {
					spinSensor.setSelection(i);
					break;
				}
			}
		}
		
		EditText i = (EditText) findViewById(R.id.interval_widget);
		int interval = settings.getInt(Constants.WIDGET_PREF_INTERVAL, WidgetUpdateService.UPDATE_INTERVAL_DEFAULT);
		interval = Math.max(interval, WidgetUpdateService.UPDATE_INTERVAL_MIN);
		i.setText(Integer.toString(interval));
	}

	private boolean saveSettings() {
		SharedPreferences settings = SensorWidgetProvider.getSettings(WidgetConfigurationActivity.this, mAppWidgetId);
		SharedPreferences.Editor editor = settings.edit();

		Spinner spinner = (Spinner) findViewById(R.id.adapter);
		Adapter adapter = (Adapter) spinner.getSelectedItem();
		if (adapter == null) {
			// FIXME: use string from resources
			Toast.makeText(this, "Select adapter from list", Toast.LENGTH_LONG).show();
			return false;
		}
		
		spinner = (Spinner) findViewById(R.id.sensor);
		BaseDevice device = (BaseDevice) spinner.getSelectedItem();
		if (device == null) {
			// FIXME: use string from resources
			Toast.makeText(this, "Select sensor from list", Toast.LENGTH_LONG).show();
			return false;
		}

		EditText edit = (EditText) findViewById(R.id.interval_widget);
		String i = edit.getText().toString();
		if (i == null || i.length() == 0) {
			// FIXME: use string from resources
			Toast.makeText(this, "Set update interval", Toast.LENGTH_LONG).show();
			return false;
		}

		int interval = Integer.parseInt(i);
		interval = Math.max(interval, WidgetUpdateService.UPDATE_INTERVAL_MIN);

		editor.putString(Constants.WIDGET_PREF_DEVICE_ADAPTER_ID, adapter.getId());
		editor.putString(Constants.WIDGET_PREF_DEVICE, device.getId());
		editor.putInt(Constants.WIDGET_PREF_INTERVAL, interval);
		editor.putBoolean(Constants.WIDGET_PREF_INITIALIZED, true);
		editor.commit();

		return true;
	}
	
	private class ChangeAdapterTask extends AsyncTask<String, Void, Boolean> {

		private String adapterId; 
		
		@Override
		protected Boolean doInBackground(String... adapterIds) {
			adapterId = adapterIds[0];
			return mController.reloadFacilitiesByAdapter(adapterId, false);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			List<Facility> facilities = mController.getFacilitiesByAdapter(adapterId);
			
			mDevices.clear();
			for (Facility facility : facilities) {
				mDevices.addAll(facility.getDevices());
			}
			
			ArrayAdapter<?> arrayAdapter = new ArrayAdapter<BaseDevice>(WidgetConfigurationActivity.this, android.R.layout.simple_spinner_dropdown_item, mDevices);
			Spinner s = (Spinner) findViewById(R.id.sensor);
			s.setEnabled(true);
			s.setAdapter(arrayAdapter);
			
			setProgressBarIndeterminateVisibility(false);
		}
	}

}
