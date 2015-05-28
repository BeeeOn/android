package com.rehivetech.beeeon.activity;

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
import com.rehivetech.beeeon.SetupSensorFragmentAdapter;
import com.rehivetech.beeeon.activity.fragment.SetupSensorFragment;
import com.rehivetech.beeeon.asynctask.CallbackTask.CallbackTaskListener;
import com.rehivetech.beeeon.asynctask.InitializeFacilityTask;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.pair.InitializeFacilityPair;
import com.rehivetech.beeeon.util.Log;
import com.viewpagerindicator.CirclePageIndicator;

public class SetupSensorActivity extends BaseApplicationActivity {
	private static final String TAG = SetupSensorActivity.class.getSimpleName();
	
	private Controller mController;
	private Adapter mPairAdapter;
	
	private SetupSensorFragmentAdapter mAdapter;
	private ViewPager mPager;
	private CirclePageIndicator mIndicator;
	
	private SetupSensorFragment mFragment;
	

	private ProgressDialog mProgress;
	
	private Spinner mSpinner;
	private ListView mListOfName;
	private TextView mNewLocation;
	private Spinner mNewIconSpinner;
	
	private Button mSkip;
	private Button mCancel;
	private Button mNext;
	

	private boolean mFirstUse = true;
	
	private Activity mActivity;
    private Toolbar mToolbar;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.title_activity_setup_sensor);
            setSupportActionBar(mToolbar);
        }
		
		// Get controller
		mController = Controller.getInstance(this);
		mPairAdapter = mController.getActiveAdapter();
		
		mActivity = this;
		
		mAdapter = new SetupSensorFragmentAdapter(getSupportFragmentManager());

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mPager = (ViewPager)findViewById(R.id.intro_pager);
		mPager.setAdapter(mAdapter);
		mPager.setOffscreenPageLimit(mAdapter.getCount());
		
		mIndicator = (CirclePageIndicator)findViewById(R.id.intro_indicator);
		mIndicator.setViewPager(mPager);
		mIndicator.setVisibility(View.GONE);
		
		// Prepare progress dialog
		mProgress = new ProgressDialog(mActivity);
		mProgress.setMessage(getString(R.string.progress_saving_data));
		mProgress.setCancelable(false);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		
		initButtons();
	}
	
	
	private void initButtons() {
		mSkip = (Button) findViewById(R.id.add_adapter_skip);
		mCancel = (Button) findViewById(R.id.add_adapter_cancel);
		mNext = (Button) findViewById(R.id.add_adapter_next);
		
		mSkip.setVisibility(View.INVISIBLE);
		
		mCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
				setResult(Constants.ADD_SENSOR_CANCELED);
				finish();
			}
		});
		mNext.setText(getString(R.string.save));
		mNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mSpinner = mFragment.getSpinner();
				mListOfName = mFragment.getListOfName();
				mNewLocation = mFragment.getNewLocation();
				mNewIconSpinner = mFragment.getNewIconSpinner();
				Device newDevice = mController.getUninitializedDevicesModel().getUninitializedDevicesByAdapter(mPairAdapter.getId()).get(0);

				// Controll if Names arent empty
				for (int i = 0; i < newDevice.getModules().size(); i++) {
					// Get new names from EditText
					String name = ((EditText) mListOfName.getChildAt(i).findViewById(R.id.setup_sensor_item_name)).getText().toString();
					Log.d(TAG, "Name of " + i + " is" + name);
					if (name.isEmpty()) {
						Toast.makeText(mActivity, getString(R.string.toast_empty_sensor_name), Toast.LENGTH_LONG).show();
						return;
					}
					// Set this new name to sensor
					newDevice.getModules().get(i).setName(name);

				}

				Location location = null;
				// last location - means new one
				if (mSpinner.getSelectedItemPosition() == mSpinner.getCount() - 1) {

					// check new location name
					if (mNewLocation != null && mNewLocation.length() < 1) {
						Toast.makeText(mActivity, getString(R.string.toast_need_sensor_location_name), Toast.LENGTH_LONG).show();
						return;
					}
					if ((mNewIconSpinner.getAdapter().getItem(mNewIconSpinner.getSelectedItemPosition())).equals(Location.LocationIcon.UNKNOWN)) {
						Toast.makeText(mActivity, getString(R.string.toast_need_sensor_location_icon), Toast.LENGTH_LONG).show();
						return;
					}

					location = new Location(Location.NEW_LOCATION_ID, mNewLocation.getText().toString(), mPairAdapter.getId(), ((Location.LocationIcon) mNewIconSpinner.getAdapter().getItem(mNewIconSpinner.getSelectedItemPosition())).getId());

				} else {
					location = (Location) mSpinner.getSelectedItem();
				}

				// Set location to mDevice
				newDevice.setLocationId(location.getId());

				// Set that mDevice was initialized
				newDevice.setInitialized(true);
				// Show progress bar for saving
				mProgress.show();

				// Save that mDevice
				Log.d(TAG, String.format("InitializeFacility - mDevice: %s, loc: %s", newDevice.getId(), location.getId()));
				doInitializeFacilityTask(new InitializeFacilityPair(newDevice, location));
			}
		});
		
		
	}
	
	public void setBtnLastPage() {
		mSkip.setVisibility(View.INVISIBLE);
		mNext.setText(mActivity.getString(R.string.addsensor_send_pair));
	}

	public void resetBtn() {
		mSkip.setVisibility(View.VISIBLE);
		mNext.setText("NEXT");
	}


	public void setFragment(SetupSensorFragment fragment) {
		mFragment = fragment;
	}

	private void doInitializeFacilityTask(final InitializeFacilityPair pair) {
		InitializeFacilityTask initializeFacilityTask = new InitializeFacilityTask(this);

		initializeFacilityTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {

				mProgress.cancel();
				if (success) {
					Toast.makeText(mActivity, R.string.toast_new_sensor_added, Toast.LENGTH_LONG).show();

					Intent intent = new Intent();
					intent.putExtra(Constants.SETUP_SENSOR_ACT_LOC, pair.location.getId());
					setResult(Constants.SETUP_SENSOR_SUCCESS, intent);
					//HIDE keyboard
					InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
					finish();
				}
			}

		});

		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(initializeFacilityTask, pair);
	}


}
