package com.rehivetech.beeeon.gui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.SetupSensorFragmentAdapter;
import com.rehivetech.beeeon.gui.fragment.SetupSensorFragment;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask.ICallbackTaskListener;
import com.rehivetech.beeeon.threading.task.SaveDeviceTask;
import com.rehivetech.beeeon.util.Log;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.EnumSet;

public class SetupSensorActivity extends BaseApplicationActivity {
	private static final String TAG = SetupSensorActivity.class.getSimpleName();
	private Gate mPairGate;

	private SetupSensorFragment mFragment;
	private ProgressDialog mProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_base_guide);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			toolbar.setTitle(R.string.title_activity_setup_sensor);
			setSupportActionBar(toolbar);
		}

		mPairGate = Controller.getInstance(this).getActiveGate();

		SetupSensorFragmentAdapter adapter = new SetupSensorFragmentAdapter(getSupportFragmentManager());

		ViewPager viewPager = (ViewPager) findViewById(R.id.intro_pager);
		viewPager.setAdapter(adapter);
		viewPager.setOffscreenPageLimit(adapter.getCount());

		CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.intro_indicator);
		indicator.setViewPager(viewPager);
		indicator.setVisibility(View.GONE);

		// Prepare progress dialog
		mProgress = new ProgressDialog(this);
		mProgress.setMessage(getString(R.string.progress_saving_data));
		mProgress.setCancelable(false);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		initButtons();
	}


	private void initButtons() {
		Button skipBtn = (Button) findViewById(R.id.add_gate_skip);
		Button cancelBtn = (Button) findViewById(R.id.add_gate_cancel);
		Button nextBtn = (Button) findViewById(R.id.add_gate_next);

		skipBtn.setVisibility(View.INVISIBLE);

		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) SetupSensorActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});
		nextBtn.setText(getString(R.string.save));
		nextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Spinner spinner = mFragment.getSpinner();
				ListView listOfName = mFragment.getListOfName();
				TextView newLocation = mFragment.getNewLocation();
				Spinner newIconSpinner = mFragment.getNewIconSpinner();
				Device newDevice = Controller.getInstance(SetupSensorActivity.this).getUninitializedDevicesModel().getUninitializedDevicesByGate(mPairGate.getId()).get(0);

				// Controll if Names arent empty
				for (int i = 0; i < newDevice.getModules().size(); i++) {
					// Get new names from EditText
					String name = ((EditText) listOfName.getChildAt(i).findViewById(R.id.setup_sensor_item_name)).getText().toString();
					Log.d(TAG, "Name of " + i + " is" + name);
					if (name.isEmpty()) {
						Toast.makeText(SetupSensorActivity.this, getString(R.string.toast_empty_sensor_name), Toast.LENGTH_LONG).show();
						return;
					}
					// Set this new name to sensor
					newDevice.getModules().get(i).setName(name);

				}

				Location location = null;
				// last location - means new one
				if (spinner.getSelectedItemPosition() == spinner.getCount() - 1) {

					// check new location name
					if (newLocation != null && newLocation.length() < 1) {
						Toast.makeText(SetupSensorActivity.this, getString(R.string.toast_need_sensor_location_name), Toast.LENGTH_LONG).show();
						return;
					}
					if ((newIconSpinner.getAdapter().getItem(newIconSpinner.getSelectedItemPosition())).equals(Location.LocationIcon.UNKNOWN)) {
						Toast.makeText(SetupSensorActivity.this, getString(R.string.toast_need_sensor_location_icon), Toast.LENGTH_LONG).show();
						return;
					}

					location = new Location(Location.NEW_LOCATION_ID, newLocation.getText().toString(), mPairGate.getId(), ((Location.LocationIcon) newIconSpinner.getAdapter().getItem(newIconSpinner.getSelectedItemPosition())).getId());

				} else {
					location = (Location) spinner.getSelectedItem();
				}

				// Set location to mDevice
				newDevice.setLocationId(location.getId());

				// Set that mDevice was initialized
				newDevice.setInitialized(true);
				// Show progress bar for saving
				mProgress.show();

				// Save that mDevice
				Log.d(TAG, String.format("InitializeDevice - mDevice: %s, loc: %s", newDevice.getId(), location.getId()));
				EnumSet<Module.SaveModule> what = EnumSet.of(Module.SaveModule.SAVE_LOCATION, Module.SaveModule.SAVE_NAME, Module.SaveModule.SAVE_INITIALIZED);
				doInitializeDeviceTask(new Device.DataPair(newDevice, location, what));
			}
		});


	}

	public void setFragment(SetupSensorFragment fragment) {
		mFragment = fragment;
	}

	private void doInitializeDeviceTask(final Device.DataPair pair) {
		SaveDeviceTask initializeDeviceTask = new SaveDeviceTask(this);

		initializeDeviceTask.setListener(new ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {

				mProgress.cancel();
				if (success) {
					Toast.makeText(SetupSensorActivity.this, R.string.toast_new_sensor_added, Toast.LENGTH_LONG).show();

					Intent intent = new Intent();
					intent.putExtra(Constants.SETUP_SENSOR_ACT_LOC, pair.location.getId());
					setResult(Activity.RESULT_OK, intent);
					//HIDE keyboard
					InputMethodManager imm = (InputMethodManager) SetupSensorActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
					finish();
				}
			}

		});

		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(initializeDeviceTask, pair);
	}


}
