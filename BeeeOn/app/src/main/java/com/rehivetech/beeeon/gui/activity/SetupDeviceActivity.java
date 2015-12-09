package com.rehivetech.beeeon.gui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.SetupDeviceFragmentAdapter;
import com.rehivetech.beeeon.gui.fragment.SetupDeviceFragment;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask.ICallbackTaskListener;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.SaveDeviceTask;
import com.viewpagerindicator.CirclePageIndicator;

public class SetupDeviceActivity extends BaseApplicationActivity {
	private static final String TAG = SetupDeviceActivity.class.getSimpleName();

	public static final String EXTRA_GATE_ID = "gate_id";

	private String mGateId;

	private SetupDeviceFragment mFragment;

	private SetupDeviceFragmentAdapter mAdapter;

	private ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_base_guide);

		setupToolbar(R.string.device_setup_title_setup_device);

		mGateId = getIntent().getStringExtra(EXTRA_GATE_ID);
		if (mGateId == null) {
			Toast.makeText(this, R.string.gate_detail_toast_not_specified_gate, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		mAdapter = new SetupDeviceFragmentAdapter(getSupportFragmentManager(), mGateId);

		mViewPager = (ViewPager) findViewById(R.id.base_guide_intro_pager);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOffscreenPageLimit(mAdapter.getCount());
		mViewPager.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.base_guide_intro_indicator);
		indicator.setViewPager(mViewPager);
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
				TextView newLocation = mFragment.getNewLocation();
				Spinner newIconSpinner = mFragment.getNewIconSpinner();
				Device newDevice = Controller.getInstance(SetupDeviceActivity.this).getUninitializedDevicesModel().getUninitializedDevicesByGate(mGateId).get(0);

				Location location;
				if (spinner.getSelectedItemPosition() == spinner.getCount() - 1) {
					// last location - means new one, so check its name
					if (newLocation == null || newLocation.length() < 1) {
						Toast.makeText(SetupDeviceActivity.this, getString(R.string.device_setup_toast_need_module_location_name), Toast.LENGTH_LONG).show();
						return;
					}
					if ((newIconSpinner.getAdapter().getItem(newIconSpinner.getSelectedItemPosition())).equals(Location.LocationIcon.UNKNOWN)) {
						Toast.makeText(SetupDeviceActivity.this, getString(R.string.activity_module_edit_setup_device_toast_location_icon), Toast.LENGTH_LONG).show();
						return;
					}

					location = new Location(Location.NEW_LOCATION_ID, newLocation.getText().toString(), mGateId, ((Location.LocationIcon) newIconSpinner.getAdapter().getItem(newIconSpinner.getSelectedItemPosition())).getId());
				} else {
					location = (Location) spinner.getSelectedItem();
				}

				// Set location to device
				newDevice.setLocationId(location.getId());

				// Save that device
				Log.d(TAG, String.format("InitializeDevice - device: %s, loc: %s", newDevice.getId(), location.getId()));
				doInitializeDeviceTask(new Device.DataPair(newDevice, location, true));
			}
		});
	}

	public void setFragment(SetupDeviceFragment fragment) {
		mFragment = fragment;

	}

	public SetupDeviceFragmentAdapter getAdapter() {
		return mAdapter;
	}

	public ViewPager getViewPager() {
		return mViewPager;
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
