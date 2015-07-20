package com.rehivetech.beeeon.gui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
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

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.LocationArrayAdapter;
import com.rehivetech.beeeon.gui.adapter.LocationIconAdapter;
import com.rehivetech.beeeon.gui.fragment.BaseApplicationFragment;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.SaveDeviceTask;
import com.rehivetech.beeeon.util.Log;

import java.util.EnumSet;
import java.util.List;

public class ModuleEditActivity extends BaseApplicationActivity {
	private static final String TAG = ModuleEditActivity.class.getSimpleName();

	public static final String EXTRA_GATE_ID = "gate_id";
	public static final String EXTRA_MODULE_ID = "module_id";

	private String mModuleId;
	private String mGateId;
	private ModuleEditFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_module_edit);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			toolbar.setTitle(R.string.title_activity_module_edit);
			setSupportActionBar(toolbar);
		}
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeAsUpIndicator(R.drawable.ic_action_cancel);
		}

		Intent intent = getIntent();
		mModuleId = intent.getStringExtra(EXTRA_MODULE_ID);
		mGateId = intent.getStringExtra(EXTRA_GATE_ID);

		if (mModuleId == null || mGateId == null) {
			Log.e(TAG, "Not specified module to edit.");
			finish();
			return;
		}

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, ModuleEditFragment.newInstance(mGateId, mModuleId), ModuleEditFragment.TAG)
					.commit();
		}
	}

	@Override
	public void onFragmentAttached(Fragment fragment) {
		super.onFragmentAttached(fragment);
		try {
			mFragment = (ModuleEditFragment) fragment;
		} catch (ClassCastException e) {
			throw new ClassCastException(String.format("%s must be ModuleEditFragment", fragment.toString()));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_module_edit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_save: {
				if (mFragment == null) {
					return false;
				}

				Device.DataPair pair = mFragment.getSaveDataPair();
				if (pair == null) {
					return false;
				} else if (pair.what.isEmpty()) {
					// nothing changed
					finish();
				} else {
					doSaveDeviceTask(pair);
				}

				return true;
			}
			case android.R.id.home: {
				finish();
				break;
			}
		}

		return super.onOptionsItemSelected(item);
	}

	public void doSaveDeviceTask(Device.DataPair pair) {
		SaveDeviceTask saveDeviceTask = new SaveDeviceTask(this);

		saveDeviceTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					Log.d(TAG, "Success save to server");
					Toast.makeText(ModuleEditActivity.this, R.string.toast_success_save_data, Toast.LENGTH_LONG).show();
					finish();
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		// And don't show progressbar because in this activity is showing progress dialog
		callbackTaskManager.executeTask(saveDeviceTask, pair, CallbackTaskManager.ProgressIndicator.PROGRESS_DIALOG);
	}

	public static class ModuleEditFragment extends BaseApplicationFragment {
		private static final String TAG = ModuleEditFragment.class.getSimpleName();

		private static final String SAVE_NAME = "name";
		private static final String SAVE_LOCATION = "location";
		private static final String SAVE_NEW_LOCATION_ICON = "new_location_icon";
		private static final String SAVE_NEW_LOCATION_NAME = "new_location_name";
		private static final String SAVE_REFRESH = "refresh";

		private ModuleEditActivity mActivity;

		private String mModuleId;
		private String mGateId;

		/** Content views */
		private Spinner mLocationSpinner;
		private EditText mName;
		private SeekBar mRefreshTimeSeekBar;
		private TextView mRefreshTimeText;
		private Spinner mNewLocationIconSpinner;
		private TextView mNewLocationName;
		private View mNewLocationLayout;

		public ModuleEditFragment() {}

		public static ModuleEditFragment newInstance(String gateId, String moduleId) {
			Bundle args = new Bundle();
			args.putString(EXTRA_GATE_ID, gateId);
			args.putString(EXTRA_MODULE_ID, moduleId);

			ModuleEditFragment fragment = new ModuleEditFragment();
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);

			try {
				mActivity = (ModuleEditActivity) activity;
			} catch (ClassCastException e) {
				throw new ClassCastException(activity.toString()
						+ " must be subclass of ModuleEditActivity");
			}
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			Bundle args = getArguments();
			if (args == null || !args.containsKey(EXTRA_GATE_ID) || !args.containsKey(EXTRA_MODULE_ID)) {
				Log.e(TAG, "Not specified moduleId as Fragment argument");
				return;
			}

			mGateId = args.getString(EXTRA_GATE_ID);
			mModuleId = args.getString(EXTRA_MODULE_ID);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_module_edit, container, false);

			mName = (EditText) view.findViewById(R.id.sen_edit_name);
			mLocationSpinner = (Spinner) view.findViewById(R.id.sen_edit_location);
			mRefreshTimeSeekBar = (SeekBar) view.findViewById(R.id.sen_edit_refreshtime);
			mRefreshTimeText = (TextView) view.findViewById(R.id.sen_edit_refreshtime_val);
			mNewLocationIconSpinner = (Spinner) view.findViewById(R.id.sen_edit_new_loc_icon);
			mNewLocationName = (TextView) view.findViewById(R.id.sen_edit_new_loc_text);

			mNewLocationLayout = view.findViewById(R.id.sen_edit_third_section);

			return view;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			initLayout();

			if (savedInstanceState != null) {
				// Fill remembered values
				mName.setText(savedInstanceState.getString(SAVE_NAME));
				mLocationSpinner.setSelection(savedInstanceState.getInt(SAVE_LOCATION));
				mNewLocationIconSpinner.setSelection(savedInstanceState.getInt(SAVE_NEW_LOCATION_ICON));
				mNewLocationName.setText(savedInstanceState.getString(SAVE_NEW_LOCATION_NAME));
				mRefreshTimeSeekBar.setProgress(savedInstanceState.getInt(SAVE_REFRESH));
			} else {
				// Fill default values
				Module module = Controller.getInstance(mActivity).getDevicesModel().getModule(mGateId, mModuleId);
				if (module == null)
					return;

				Device device = module.getDevice();
				LocationArrayAdapter adapter = (LocationArrayAdapter) mLocationSpinner.getAdapter();

				mName.setText(module.getName(mActivity));
				mLocationSpinner.setSelection(getLocationsIndexFromArray(adapter.getLocations(), device.getLocationId()));
				mRefreshTimeSeekBar.setProgress(device.getRefresh().getIntervalIndex());
				mRefreshTimeText.setText(" " + device.getRefresh().getStringInterval(mActivity));
			}
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);

			outState.putString(SAVE_NAME, mName.getText().toString());
			outState.putInt(SAVE_LOCATION, mLocationSpinner.getSelectedItemPosition());
			outState.putInt(SAVE_NEW_LOCATION_ICON, mNewLocationIconSpinner.getSelectedItemPosition());
			outState.putString(SAVE_NEW_LOCATION_NAME, mNewLocationName.getText().toString());
			outState.putInt(SAVE_REFRESH, mRefreshTimeSeekBar.getProgress());
		}

		private void initLayout() {
			// Location adapter
			LocationArrayAdapter dataAdapter = new LocationArrayAdapter(mActivity, R.layout.custom_spinner_item);
			dataAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
			mLocationSpinner.setAdapter(dataAdapter);

			// Set listener to (un)hide layout for adding new location
			mLocationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if (position == mLocationSpinner.getCount() - 1) {
						// show new location
						mNewLocationLayout.setVisibility(View.VISIBLE);
					} else {
						// hide input for new location
						mNewLocationLayout.setVisibility(View.GONE);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					//hideInputForNewLocation(true);
				}
			});

			// Icon adapter
			LocationIconAdapter iconAdapter = new LocationIconAdapter(mActivity, R.layout.custom_spinner_icon_item);
			iconAdapter.setDropDownViewResource(R.layout.custom_spinner_icon_dropdown_item);
			mNewLocationIconSpinner.setAdapter(iconAdapter);

			// Set max value by length of array with values
			mRefreshTimeSeekBar.setMax(RefreshInterval.values().length - 1);
			mRefreshTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					String interval = RefreshInterval.values()[progress].getStringInterval(mActivity);
					mRefreshTimeText.setText(" " + interval);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});
		}

		private int getLocationsIndexFromArray(List<Location> locations, String locationId) {
			int index = 0;
			for (Location room : locations) {
				if (room.getId().equalsIgnoreCase(locationId)) {
					return index;
				}
				index++;
			}
			return index;
		}


		/** Helpers for getting content data */

		private RefreshInterval getRefreshTimeSeekBar() {
			return RefreshInterval.values()[mRefreshTimeSeekBar.getProgress()];
		}

		private String getLocationId() {
			return ((Location) mLocationSpinner.getAdapter().getItem(mLocationSpinner.getSelectedItemPosition())).getId();
		}

		private String getName() {
			return mName.getText().toString();
		}

		private String getNewLocationName() {
			return mNewLocationName.getText().toString();
		}

		private Location.LocationIcon getNewLocIcon() {
			return (Location.LocationIcon) mNewLocationIconSpinner.getAdapter().getItem(mNewLocationIconSpinner.getSelectedItemPosition());
		}

		private Location getLocation() {
			return (Location) mLocationSpinner.getAdapter().getItem(mLocationSpinner.getSelectedItemPosition());
		}

		private boolean isSetNewRoom() {
			return ((Location) mLocationSpinner.getAdapter().getItem(mLocationSpinner.getSelectedItemPosition())).getId().equals(Location.NEW_LOCATION_ID);
		}

		private boolean isSetNewCustomRoom() {
			return (mLocationSpinner.getSelectedItemPosition() == mLocationSpinner.getAdapter().getCount() - 1);
		}

		public Device.DataPair getSaveDataPair() {
			Module module = Controller.getInstance(mActivity).getDevicesModel().getModule(mGateId, mModuleId);
			if (module == null) {
				Log.e(TAG, String.format("Can't get module id=%s", mModuleId));
				return null;
			}

			Device device = module.getDevice();

			EnumSet<Module.SaveModule> what = EnumSet.noneOf(Module.SaveModule.class);

			/* // FIXME: rework this
			if (!getName().equals(module.getName())) {
				what.add(Module.SaveModule.SAVE_NAME);
				module.setName(getName());
			}

			if (!getRefreshTimeSeekBar().equals(device.getRefresh())) {
				what.add(Module.SaveModule.SAVE_REFRESH);
				device.setRefresh(getRefreshTimeSeekBar());
			}*/

			if (!getLocationId().equals(device.getLocationId())) {
				what.add(Module.SaveModule.SAVE_LOCATION);
				if (isSetNewRoom()) {
					Location location;
					if (isSetNewCustomRoom()) {
						if (getNewLocIcon().equals(Location.LocationIcon.UNKNOWN)) {
							Toast.makeText(mActivity, getString(R.string.toast_need_module_location_icon), Toast.LENGTH_LONG).show();
							return null;
						}
						// Create new custom room
						location = new Location(Location.NEW_LOCATION_ID, getNewLocationName(), mGateId, getNewLocIcon().getId());
					} else {
						location = getLocation();
					}
					// Send request for new loc ..
					return new Device.DataPair(device, location, what);
				} else {
					device.setLocationId(getLocationId());
				}
			}

			return new Device.DataPair(device, what);
		}
	}
}