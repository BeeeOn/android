package com.rehivetech.beeeon.gui.activity;

import android.app.Activity;
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
import com.rehivetech.beeeon.gui.adapter.SetupDeviceFragmentAdapter;
import com.rehivetech.beeeon.gui.fragment.SetupDeviceFragment;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask.ICallbackTaskListener;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.SaveDeviceTask;
import com.rehivetech.beeeon.util.Log;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.EnumSet;

public class SetupDeviceActivity extends BaseApplicationActivity {
	private static final String TAG = SetupDeviceActivity.class.getSimpleName();
	private Gate mPairGate;

	private SetupDeviceFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_base_guide);

		setupToolbar(R.string.device_setup_title_setup_device);

		mPairGate = Controller.getInstance(this).getActiveGate();

		SetupDeviceFragmentAdapter adapter = new SetupDeviceFragmentAdapter(getSupportFragmentManager());

		ViewPager viewPager = (ViewPager) findViewById(R.id.base_guide_intro_pager);
		viewPager.setAdapter(adapter);
		viewPager.setOffscreenPageLimit(adapter.getCount());

		CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.base_guide_intro_indicator);
		indicator.setViewPager(viewPager);
		indicator.setVisibility(View.GONE);

		initButtons();
	}


	private void initButtons() {
		Button skipBtn = (Button) findViewById(R.id.base_guide_add_gate_skip_button);
		Button cancelBtn = (Button) findViewById(R.id.base_guide_add_gate_cancel_button);
		Button nextBtn = (Button) findViewById(R.id.base_guide_add_gate_next_button);

		skipBtn.setVisibility(View.INVISIBLE);

		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) SetupDeviceActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});
		nextBtn.setText(getString(R.string.activity_gate_user_setup_device_btn_save));
		nextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Spinner spinner = mFragment.getSpinner();
				ListView listOfName = mFragment.getListOfName();
				TextView newLocation = mFragment.getNewLocation();
				Spinner newIconSpinner = mFragment.getNewIconSpinner();
				Device newDevice = Controller.getInstance(SetupDeviceActivity.this).getUninitializedDevicesModel().getUninitializedDevicesByGate(mPairGate.getId()).get(0);

				// Controll if Names arent empty
				for (int i = 0; i < newDevice.getAllModules().size(); i++) {
					// Get new names from EditText
					String name = ((EditText) listOfName.getChildAt(i).findViewById(R.id.list_module_setup_sensor_item_name)).getText().toString();
					Log.d(TAG, "Name of " + i + " is" + name);
					if (name.isEmpty()) {
						Toast.makeText(SetupDeviceActivity.this, getString(R.string.device_setup_toast_empty_module_name), Toast.LENGTH_LONG).show();
						return;
					}
					// Set this new name to module
					// FIXME: rework this?
					// newDevice.getAllModules().get(i).setName(name);

				}

				Location location = null;
				// last location - means new one
				if (spinner.getSelectedItemPosition() == spinner.getCount() - 1) {

					// check new location name
					if (newLocation != null && newLocation.length() < 1) {
						Toast.makeText(SetupDeviceActivity.this, getString(R.string.device_setup_toast_need_module_location_name), Toast.LENGTH_LONG).show();
						return;
					}
					if ((newIconSpinner.getAdapter().getItem(newIconSpinner.getSelectedItemPosition())).equals(Location.LocationIcon.UNKNOWN)) {
						Toast.makeText(SetupDeviceActivity.this, getString(R.string.activity_module_edit_setup_device_toast_location_icon), Toast.LENGTH_LONG).show();
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

				// Save that mDevice
				Log.d(TAG, String.format("InitializeDevice - mDevice: %s, loc: %s", newDevice.getId(), location.getId()));
				EnumSet<Module.SaveModule> what = EnumSet.of(Module.SaveModule.SAVE_LOCATION, Module.SaveModule.SAVE_NAME, Module.SaveModule.SAVE_INITIALIZED);
				doInitializeDeviceTask(new Device.DataPair(newDevice, location, what));
			}
		});


	}

	public void setFragment(SetupDeviceFragment fragment) {
		mFragment = fragment;
	}

	private void doInitializeDeviceTask(final Device.DataPair pair) {
		SaveDeviceTask initializeDeviceTask = new SaveDeviceTask(this);

		initializeDeviceTask.setListener(new ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (success) {
					Toast.makeText(SetupDeviceActivity.this, R.string.device_setup_toast_new_module_added, Toast.LENGTH_LONG).show();

					Intent intent = new Intent();
					intent.putExtra(Constants.SETUP_DEVICE_ACT_LOC, pair.location.getId());
					setResult(Activity.RESULT_OK, intent);
					//HIDE keyboard
					InputMethodManager imm = (InputMethodManager) SetupDeviceActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
					finish();
				}
			}

		});

		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(initializeDeviceTask, pair, CallbackTaskManager.ProgressIndicator.PROGRESS_DIALOG);
	}


}
