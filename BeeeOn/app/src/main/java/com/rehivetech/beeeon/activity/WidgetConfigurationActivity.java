package com.rehivetech.beeeon.activity;

import java.util.ArrayList;
import java.util.List;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.asynctask.CallbackTask.CallbackTaskListener;
import com.rehivetech.beeeon.asynctask.ReloadFacilitiesTask;
import com.rehivetech.beeeon.base.BaseActivity;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.widget.WidgetData;
import com.rehivetech.beeeon.widget.WidgetUpdateService;

public class WidgetConfigurationActivity extends BaseActivity {

	private static final String TAG = WidgetConfigurationActivity.class.getSimpleName();

	private WidgetData mWidgetData;

	private List<Adapter> mAdapters = new ArrayList<Adapter>();
	private List<Device> mDevices = new ArrayList<Device>();

	private boolean isInitialized = false;
	private boolean triedLoginAlready = false;

	private WidgetConfigurationActivity context;
	
	private Controller mController;

	private ReloadFacilitiesTask mReloadFacilitiesTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setSupportProgressBarIndeterminate(true);
        super.onCreate(savedInstanceState);

		context = this;
		
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

		mController = Controller.getInstance(this);

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
	public void onStop() {
		super.onStop();

		if (mReloadFacilitiesTask != null) {
			mReloadFacilitiesTask.cancel(true);
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

	private void setIntervalWidgetText(int intervalIndex) {
		TextView intervalText = (TextView) findViewById(R.id.interval_widget);
		String interval = RefreshInterval.values()[intervalIndex].getStringInterval(WidgetConfigurationActivity.this);
		intervalText.setText(interval);
	}

	private void initSeekbar() {
		SeekBar seekbar = (SeekBar) findViewById(R.id.interval_widget_seekbar);

		// Set Max value by length of array with values
		seekbar.setMax(RefreshInterval.values().length - 1);
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				setIntervalWidgetText(progress);
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

				// not working way
				/*
				Intent firstUpdate = new Intent(WidgetConfigurationActivity.this, SensorWidgetProvider.class);
				firstUpdate.setAction("android.appwidget.action.APPWIDGET_UPDATE");
				firstUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { mWidgetData.getWidgetId() });
				WidgetConfigurationActivity.this.sendBroadcast(firstUpdate);
				//*/

				// workaround which is working -> will be replaced in advanced widgets
				WidgetUpdateService.startUpdating(context, new int[] { mWidgetData.getWidgetId() });				
				
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
				Adapter adapter = mAdapters.get(position);

				Log.d(TAG, String.format("Selected adapter %s", adapter.getName()));
				doChangeAdapter(adapter.getId(), "");
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
				Device device = (Device) parent.getSelectedItem();
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

					ArrayAdapter<?> arrayAdapter = new ArrayAdapter<Device>(WidgetConfigurationActivity.this, android.R.layout.simple_spinner_dropdown_item, mDevices);
					spinSensor.setAdapter(arrayAdapter);

					break;
				}
			}

			doChangeAdapter(adapterId, deviceId);
		}

		SeekBar seekbar = (SeekBar) findViewById(R.id.interval_widget_seekbar);
		int interval = mWidgetData.interval;
		interval = Math.max(interval, WidgetUpdateService.UPDATE_INTERVAL_MIN);
		int intervalIndex = RefreshInterval.fromInterval(interval).getIntervalIndex();
		seekbar.setProgress(intervalIndex);
		setIntervalWidgetText(intervalIndex);
	}

	private boolean saveSettings() {
		Spinner spinner = (Spinner) findViewById(R.id.adapter);
		Adapter adapter = (Adapter) spinner.getSelectedItem();
		if (adapter == null) {
			Toast.makeText(this, R.string.widget_configuration_select_adapter, Toast.LENGTH_LONG).show();
			return false;
		}

		spinner = (Spinner) findViewById(R.id.sensor);
		Device device = (Device) spinner.getSelectedItem();
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

	private void doChangeAdapter(final String adapterId, final String activeDeviceId) {
		mReloadFacilitiesTask = new ReloadFacilitiesTask(getApplicationContext(), false);

		mReloadFacilitiesTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				List<Facility> facilities = mController.getFacilitiesByAdapter(adapterId);

				mDevices.clear();
				for (Facility facility : facilities) {
					mDevices.addAll(facility.getDevices());
				}

				ArrayAdapter<?> arrayAdapter = new ArrayAdapter<Device>(WidgetConfigurationActivity.this, android.R.layout.simple_spinner_dropdown_item, mDevices);
				Spinner s = (Spinner) findViewById(R.id.sensor);
				s.setEnabled(true);
				s.setAdapter(arrayAdapter);

				if (!activeDeviceId.isEmpty()) {
					for (int i = 0; i < mDevices.size(); i++) {
						if (mDevices.get(i).getId().equals(activeDeviceId)) {
							s.setSelection(i);
							break;
						}
					}
				}

				setProgressBarIndeterminateVisibility(false);
			}
		});

		Spinner s = (Spinner) findViewById(R.id.sensor);
		s.setEnabled(false);

		setProgressBarIndeterminateVisibility(true);
		mReloadFacilitiesTask.execute(adapterId);
	}

}
