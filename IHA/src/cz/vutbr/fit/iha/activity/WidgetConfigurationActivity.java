package cz.vutbr.fit.iha.activity;

import java.util.ArrayList;
import java.util.List;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Window;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.adapter.device.RefreshInterval;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.widget.SensorWidgetProvider;
import cz.vutbr.fit.iha.widget.WidgetData;
import cz.vutbr.fit.iha.widget.WidgetUpdateService;

public class WidgetConfigurationActivity extends BaseActivity {

	private static final String TAG = WidgetConfigurationActivity.class.getSimpleName();

	private WidgetData mWidgetData;

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
			int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			mWidgetData = new WidgetData(appWidgetId);
		}

		// no valid ID, so bail out
		if (mWidgetData == null || mWidgetData.getWidgetId() == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
			return;
		}
		
		// if the user press BACK, do not add any widget
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetData.getWidgetId());
		setResult(RESULT_CANCELED, resultValue);

		setContentView(R.layout.activity_widget_configuration);		
	}
	
	@Override
	public void onResume() {
		super.onResume();

		mController = Controller.getInstance(getApplicationContext());

		mAdapters = mController.getAdapters();
		if (mAdapters.isEmpty()) {
			if (!mController.isLoggedIn() && !triedLoginAlready) {
				// If user is not logged in we redirect to LoginActivity
				triedLoginAlready = true;
				Toast.makeText(this, R.string.widget_configuration_login_first, Toast.LENGTH_LONG).show();
				BaseApplicationActivity.redirectToLogin(this);
			} else if (mController.isLoggedIn()) {
				// Otherwise he is logged in but has no sensors, we quit completely
				Toast.makeText(this, R.string.widget_configuration_no_adapters, Toast.LENGTH_LONG).show();
				finish();
			}

			return;
		} else {
			triedLoginAlready = false;
		}
		
		if (!isInitialized) {
			isInitialized = true;

			initLayout();
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
	private void initLayout() {
		initButtons();
		initSpinners();
		initSeekbar();
	}
	
	private void initSeekbar() {
		final TextView intervalText = (TextView) findViewById(R.id.interval_widget);
		final SeekBar seekbar = (SeekBar) findViewById(R.id.interval_widget_seekbar);
		
		// Set Max value by length of array with values
		seekbar.setMax(RefreshInterval.values().length - 1);
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				String interval = RefreshInterval.values()[progress].getStringInterval(WidgetConfigurationActivity.this);
				intervalText.setText(interval);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// Nothing to do here
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// Nothing to do here
			}

		});

	}

	private void initButtons() {
		// Cancel button - close window without adding widget
		((Button) findViewById(R.id.btn_cancel)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Cancel clicked");
				
				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetData.getWidgetId());
				setResult(RESULT_CANCELED, resultValue);
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
				firstUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { mWidgetData.getWidgetId() });
				WidgetConfigurationActivity.this.sendBroadcast(firstUpdate);

				// return the original widget ID, found in onCreate()
				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetData.getWidgetId());
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
				
				Log.d(TAG, String.format("Selected adapter %s", adapter.getName()));

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
				TextView intervalText = (TextView) findViewById(R.id.interval_sensor);
				intervalText.setText(device.getFacility().getRefresh().getStringInterval(WidgetConfigurationActivity.this));
				
				Log.d(TAG, String.format("Selected device %s", device.getName()));
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				TextView interval = (TextView) findViewById(R.id.interval_sensor);
				interval.setText("");
				
				Log.d(TAG, "Selected no device");
			}

		});
	}

	private void loadSettings() {
		mWidgetData.loadData(this);

		Spinner spinAdapter = (Spinner) findViewById(R.id.adapter);
		Spinner spinSensor = (Spinner) findViewById(R.id.sensor);
		
		String adapterId = mWidgetData.deviceAdapterId;
		String deviceId = mWidgetData.deviceId;
		
		if (!adapterId.isEmpty()) {
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

		if (!adapterId.isEmpty() && !deviceId.isEmpty()) {
			for (int i = 0; i < mDevices.size(); i++) {
				if (mDevices.get(i).getId().equals(deviceId)) {
					spinSensor.setSelection(i);
					break;
				}
			}
		}
		
		SeekBar seekbar = (SeekBar) findViewById(R.id.interval_widget_seekbar);
		int interval = mWidgetData.interval;
		interval = Math.max(interval, WidgetUpdateService.UPDATE_INTERVAL_MIN);
		seekbar.setProgress(RefreshInterval.fromInterval(interval).getIntervalIndex());
	}

	private boolean saveSettings() {
		Spinner spinner = (Spinner) findViewById(R.id.adapter);
		Adapter adapter = (Adapter) spinner.getSelectedItem();
		if (adapter == null) {
			Toast.makeText(this, R.string.widget_configuration_select_adapter, Toast.LENGTH_LONG).show();
			return false;
		}
		
		spinner = (Spinner) findViewById(R.id.sensor);
		BaseDevice device = (BaseDevice) spinner.getSelectedItem();
		if (device == null) {
			Toast.makeText(this, R.string.widget_configuration_select_device, Toast.LENGTH_LONG).show();
			return false;
		}

		SeekBar seekbar = (SeekBar) findViewById(R.id.interval_widget_seekbar);
		RefreshInterval refresh = RefreshInterval.values()[seekbar.getProgress()];
		int interval = refresh.getInterval();
		
		mWidgetData.interval = Math.max(interval, WidgetUpdateService.UPDATE_INTERVAL_MIN);
		mWidgetData.deviceAdapterId = adapter.getId();
		mWidgetData.deviceId = device.getId();
		mWidgetData.initialized = true;
		mWidgetData.saveData(this);

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
