package com.rehivetech.beeeon.activity;

import android.app.ProgressDialog;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.arrayadapter.LocationArrayAdapter;
import com.rehivetech.beeeon.arrayadapter.LocationIconAdapter;
import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.asynctask.SaveDeviceTask;
import com.rehivetech.beeeon.asynctask.SaveFacilityTask;
import com.rehivetech.beeeon.asynctask.SaveFacilityWithNewLocTask;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.SaveFacilityPair;
import com.rehivetech.beeeon.pair.SaveFacilityWithNewLocPair;
import com.rehivetech.beeeon.util.Log;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SensorEditActivity extends BaseApplicationActivity {

	public static final String EXTRA_DEVICE_ID = "device_id";
	private static final String TAG = SensorEditActivity.class.getSimpleName();

	private Toolbar mToolbar;
	private String mDeviceId;
	private SensorEditActivity mActivity;
	private SaveDeviceTask mSaveDeviceTask;
	private SaveFacilityTask mSaveFacilityTask;
	private ProgressDialog mProgress;
	private Controller mController;
	private PlaceholderFragment mFragment;
	private SaveFacilityWithNewLocTask mSaveFacilityWithNewLocTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor_edit);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if (mToolbar != null) {
			mToolbar.setTitle(R.string.title_activity_sensor_edit);
			setSupportActionBar(mToolbar);
		}
		mDeviceId = getIntent().getStringExtra(Constants.GUI_EDIT_SENSOR_ID);
		if(mDeviceId == null && savedInstanceState != null) {
			mDeviceId = savedInstanceState.getString(EXTRA_DEVICE_ID);
		}
		mFragment = new PlaceholderFragment();
		mFragment.setDeviceID(mDeviceId);
		if(savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, mFragment)
					.commit();
		}
		mActivity = this;
		mController = Controller.getInstance(this);

		// Prepare progress dialog
		mProgress = new ProgressDialog(this);
		mProgress.setMessage(getString(R.string.progress_saving_data));
		mProgress.setCancelable(false);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(EXTRA_DEVICE_ID, mDeviceId);

		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_sensor_edit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_save) {
			Set<Device.SaveDevice> what = new HashSet<>() ;
			Adapter adapter = mController.getActiveAdapter();
			if(adapter == null)
				return  false;
			Device device = mController.getFacilitiesModel().getDevice(adapter.getId(),mDeviceId);
			Facility facility = device.getFacility();

			if(!mFragment.getName().equals(device.getName())) {
				what.add(Device.SaveDevice.SAVE_NAME);
				device.setName(mFragment.getName());
			}

			if(!mFragment.getRefreshTime().equals(facility.getRefresh())) {
				what.add(Device.SaveDevice.SAVE_REFRESH);
				facility.setRefresh(mFragment.getRefreshTime());
			}
			if(!mFragment.getLocationId().equals(facility.getLocationId())) {
				what.add(Device.SaveDevice.SAVE_LOCATION);
				if(mFragment.isSetNewRoom()) {
					Location location;
					if(mFragment.isSetNewCustomRoom()) {
						// Create new custom room
						location = new Location(Location.NEW_LOCATION_ID, mFragment.getNewLocName(), adapter.getId(), mFragment.getNewLocIcon().getId());
					}
					else {
						location = mFragment.getLocation();
					}
					// Send request for new loc ..
					doSaveFacilityWithNewLocation(new SaveFacilityWithNewLocPair(facility,location,EnumSet.copyOf(what)));
					return true;
				} else {
					facility.setLocationId(mFragment.getLocationId());
				}
			}
			if(what.isEmpty()) { // nothing change
				setResult(Constants.EDIT_SENSOR_SUCCESS);
				finish();
				return true;
			}

			if(!mFragment.isSetNewRoom())
				doSaveFacilityTask(new SaveFacilityPair(facility,EnumSet.copyOf(what)));

			return true;
		}
		else if (id == android.R.id.home){
			setResult(Constants.EDIT_SENSOR_CANCELED);
			finish();
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onAppResume() {

	}

	@Override
	protected void onAppPause() {
		if(mProgress != null)
			mProgress.dismiss();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mSaveDeviceTask != null)
			mSaveDeviceTask.cancel(true);
		if(mSaveFacilityTask != null)
			mSaveFacilityTask.cancel(true);
	}

	public ProgressDialog getProgressDialog() {
		return mProgress;
	}

	/*
	 * ASYNC TASK - SAVE
	 */

	private void doSaveFacilityWithNewLocation(SaveFacilityWithNewLocPair pair) {
		mProgress.show();
		mSaveFacilityWithNewLocTask = new SaveFacilityWithNewLocTask(mActivity);
		mSaveFacilityWithNewLocTask.setListener(new CallbackTask.CallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (mActivity.getProgressDialog() != null)
					mActivity.getProgressDialog().dismiss();
				if (success) {
					Log.d(TAG, "Success save to server");
					Toast.makeText(mActivity, R.string.toast_success_save_data, Toast.LENGTH_LONG).show();
					setResult(Constants.EDIT_SENSOR_SUCCESS);
					finish();
				} else {
					Log.d(TAG, "Fail save to server");
					Toast.makeText(mActivity, R.string.toast_fail_save_data, Toast.LENGTH_LONG).show();
				}
			}
		});
		mSaveFacilityWithNewLocTask.execute(pair);

	}

	public void doSaveFacilityTask(SaveFacilityPair pair) {
		mProgress.show();
		mSaveFacilityTask = new SaveFacilityTask(mActivity);
		mSaveFacilityTask.setListener(new CallbackTask.CallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (mActivity.getProgressDialog() != null)
					mActivity.getProgressDialog().dismiss();
				if (success) {
					Log.d(TAG, "Success save to server");
					Toast.makeText(mActivity, R.string.toast_success_save_data, Toast.LENGTH_LONG).show();
					setResult(Constants.EDIT_SENSOR_SUCCESS);
					finish();
				} else {
					Log.d(TAG, "Fail save to server");
					Toast.makeText(mActivity, R.string.toast_fail_save_data, Toast.LENGTH_LONG).show();
				}
			}
		});
		mSaveFacilityTask.execute(pair);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		private static final String TAG = PlaceholderFragment.class.getSimpleName();

		public static final String EXTRA_DEV_ID = "device_id";
		public static final String EXTRA_ACT_NAME ="EXTRA_ACT_NAME";
		public static final String EXTRA_ACT_LOC = "EXTRA_ACT_LOC";
		public static final String EXTRA_ACT_NEW_IC_LOC = "EXTRA_ACT_NEW_IC_LOC";
		public static final String EXTRA_ACT_NEW_LOC = "EXTRA_ACT_NEW_LOC";
		public static final String EXTRA_ACT_REFRESH = "EXTRA_ACT_REFRESH";

		private SensorEditActivity mActivity;
		private View mView;
		private Spinner mSpinner;
		private EditText mName;
		private SeekBar mRefreshTime;
		private String mDeviceID;
		private Controller mController;
		private Device mDevice;
		private TextView mRefreshTimeVal;
		private Facility mFacility;
		private Adapter mAdapter;
		private String mLocationId;
		private Spinner mNewIconSpinner;
		private TextView mNewLocName;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			mView = inflater.inflate(R.layout.fragment_sensor_edit, container, false);
			return mView;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			if(savedInstanceState != null) {
				mDeviceID = savedInstanceState.getString(EXTRA_DEV_ID);
			}
			// Get activity
			mActivity = (SensorEditActivity) getActivity();
			mController = Controller.getInstance(mActivity);
			mAdapter = mController.getActiveAdapter();
			if(mAdapter == null)
				return;
			mDevice = mController.getFacilitiesModel().getDevice(mAdapter.getId(), mDeviceID);
			if(mDevice == null)
				return;
			mFacility = mDevice.getFacility();
			mLocationId = mFacility.getLocationId();
			initLayout();
			if(savedInstanceState != null) {
				mName.setText(savedInstanceState.getString(EXTRA_ACT_NAME));
				mSpinner.setSelection(savedInstanceState.getInt(EXTRA_ACT_LOC));
				mNewIconSpinner.setSelection(savedInstanceState.getInt(EXTRA_ACT_NEW_IC_LOC));
				mNewLocName.setText(savedInstanceState.getString(EXTRA_ACT_NEW_LOC));
				mRefreshTime.setProgress(savedInstanceState.getInt(EXTRA_ACT_REFRESH));
			}
		}
		@Override
		public void onSaveInstanceState(Bundle savedInstanceState) {
			// DeviceId what I edit
			savedInstanceState.putString(EXTRA_DEV_ID, mDeviceID);
			// Actualy filled data
			savedInstanceState.putString(EXTRA_ACT_NAME,mName.getText().toString());
			savedInstanceState.putInt(EXTRA_ACT_LOC,mSpinner.getSelectedItemPosition());
			savedInstanceState.putInt(EXTRA_ACT_NEW_IC_LOC,mNewIconSpinner.getSelectedItemPosition());
			savedInstanceState.putString(EXTRA_ACT_NEW_LOC,mNewLocName.getText().toString());
			savedInstanceState.putInt(EXTRA_ACT_REFRESH,mRefreshTime.getProgress());

			// Always call the superclass so it can save the view hierarchy state
			super.onSaveInstanceState(savedInstanceState);
		}

		private void initLayout() {
			// Get name of sensor
			mName = (EditText) mView.findViewById(R.id.sen_edit_name);
			// Get spinner for locations
			mSpinner = (Spinner) mView.findViewById(R.id.sen_edit_location);
			// Get seekbar for refresh time
			mRefreshTime = (SeekBar) mView.findViewById(R.id.sen_edit_refreshtime);
			mRefreshTimeVal = (TextView) mView.findViewById(R.id.sen_edit_refreshtime_val);
			// Get Spiner of icons for new location
			mNewIconSpinner = (Spinner) mView.findViewById(R.id.sen_edit_new_loc_icon);
			mNewLocName = (TextView) mView.findViewById(R.id.sen_edit_new_loc_text);

			// Set locations to spinner
			LocationArrayAdapter dataAdapter = new LocationArrayAdapter(mActivity, R.layout.custom_spinner_item);
			// Set layout to DataAdapter for locations
			dataAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);

			// ICON adapter
			LocationIconAdapter iconAdapter = new LocationIconAdapter(mActivity, R.layout.custom_spinner_icon_item);
			iconAdapter.setDropDownViewResource(R.layout.custom_spinner_icon_dropdown_item);

			// Set listener for hide or unhide layout for add new location
			mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if (position == mSpinner.getCount() - 1) {
						// show new location
						showNewLocation(true);
					} else {
						// hide input for new location
						showNewLocation(false);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					//hideInputForNewLocation(true);
				}
			});

			mSpinner.setAdapter(dataAdapter);
			mSpinner.setSelection(getLocationsIndexFromArray(dataAdapter.getLocations()));

			mNewIconSpinner.setAdapter(iconAdapter);

			mName.setText(mDevice.getName());
			// Set Max value by length of array with values
			mRefreshTime.setMax(RefreshInterval.values().length - 1);
			mRefreshTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					String interval = RefreshInterval.values()[progress].getStringInterval(mActivity);
					mRefreshTimeVal.setText(" " + interval);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {

				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					String interval = RefreshInterval.values()[seekBar.getProgress()].getStringInterval(mActivity);
					Log.d(TAG, String.format("Stop select value %s", interval));
				}
			});
			// Set refresh time Text
			mRefreshTimeVal.setText(" " + mFacility.getRefresh().getStringInterval(mActivity));

			// Set refresh time SeekBar
			mRefreshTime.setProgress(mFacility.getRefresh().getIntervalIndex());

		}

		private void showNewLocation(boolean b) {
			mView.findViewById(R.id.sen_edit_third_section).setVisibility((b)?View.VISIBLE:View.GONE);
		}

		private int getLocationsIndexFromArray(List<Location> locations) {
			int index = 0;
			for (Location room : locations) {
				if (room.getId().equalsIgnoreCase(mLocationId)) {
					return index;
				}
				index++;
			}
			return index;
		}

		public void setDeviceID(String deviceId) {
			mDeviceID = deviceId;
		}

		public RefreshInterval getRefreshTime() {
			return RefreshInterval.values()[mRefreshTime.getProgress()];
		}

		public String getLocationId() {
			return ((Location)mSpinner.getAdapter().getItem(mSpinner.getSelectedItemPosition())).getId();
		}

		public String getName() {
			return mName.getText().toString();
		}

		public String getNewLocName() {
			return mNewLocName.getText().toString();
		}

		public Location.LocationIcon getNewLocIcon() {
			return (Location.LocationIcon)mNewIconSpinner.getAdapter().getItem(mNewIconSpinner.getSelectedItemPosition());
		}

		public Location getLocation() {
			return (Location)mSpinner.getAdapter().getItem(mSpinner.getSelectedItemPosition());
		}

		public boolean isSetNewRoom() {
			return ((Location)mSpinner.getAdapter().getItem(mSpinner.getSelectedItemPosition())).getId().equals(Location.NEW_LOCATION_ID);
		}

		public boolean isSetNewCustomRoom() {
			return (mSpinner.getSelectedItemPosition() == mSpinner.getAdapter().getCount()-1);
		}
	}
}