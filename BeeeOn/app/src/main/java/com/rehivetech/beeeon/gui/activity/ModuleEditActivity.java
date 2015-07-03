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
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.SaveDeviceTask;
import com.rehivetech.beeeon.util.Log;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModuleEditActivity extends BaseApplicationActivity {
	private static final String TAG = ModuleEditActivity.class.getSimpleName();

	public static final String EXTRA_GATE_ID = "gate_id";
	public static final String EXTRA_MODULE_ID = "module_id";

	private String mModuleId;
	private String mGateId;

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
					.replace(R.id.container, PlaceholderFragment.newInstance(mGateId, mModuleId), PlaceholderFragment.TAG)
					.commit();
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
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		Controller controller = Controller.getInstance(this);
		//noinspection SimplifiableIfStatement
		if (id == R.id.action_save) {
			Set<Module.SaveModule> what = new HashSet<>();

			Module module = controller.getDevicesModel().getModule(mGateId, mModuleId);
			Device device = module.getDevice();

			PlaceholderFragment fragment = (PlaceholderFragment) getSupportFragmentManager().findFragmentByTag(PlaceholderFragment.TAG);
			if (fragment == null)
				return false;

			if (!fragment.getName().equals(module.getName())) {
				what.add(Module.SaveModule.SAVE_NAME);
				module.setName(fragment.getName());
			}

			if (!fragment.getRefreshTimeSeekBar().equals(device.getRefresh())) {
				what.add(Module.SaveModule.SAVE_REFRESH);
				device.setRefresh(fragment.getRefreshTimeSeekBar());
			}

			if (!fragment.getLocationId().equals(device.getLocationId())) {
				what.add(Module.SaveModule.SAVE_LOCATION);
				if (fragment.isSetNewRoom()) {
					Location location;
					if (fragment.isSetNewCustomRoom()) {
						if (fragment.getNewLocIcon().equals(Location.LocationIcon.UNKNOWN)) {
							Toast.makeText(this, getString(R.string.toast_need_module_location_icon), Toast.LENGTH_LONG).show();
							return false;
						}
						// Create new custom room
						location = new Location(Location.NEW_LOCATION_ID, fragment.getNewLocationName(), mGateId, fragment.getNewLocIcon().getId());
					} else {
						location = fragment.getLocation();
					}
					// Send request for new loc ..
					doSaveDeviceWithNewLocation(new Device.DataPair(device, location, EnumSet.copyOf(what)));
					return true;
				} else {
					device.setLocationId(fragment.getLocationId());
				}
			}

			if (what.isEmpty()) {
				// nothing changed
				setResult(Activity.RESULT_OK);
				finish();
			} else if (!fragment.isSetNewRoom()) {
				doSaveDeviceTask(new Device.DataPair(device, EnumSet.copyOf(what)));
			}
			return true;
		} else if (id == android.R.id.home) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}

		return super.onOptionsItemSelected(item);
	}

	private void doSaveDeviceWithNewLocation(Device.DataPair pair) {
		SaveDeviceTask saveDeviceTask = new SaveDeviceTask(this);

		saveDeviceTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					Log.d(TAG, "Success save to server");
					Toast.makeText(ModuleEditActivity.this, R.string.toast_success_save_data, Toast.LENGTH_LONG).show();
					setResult(Activity.RESULT_OK);
					finish();
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		// And don't show progressbar because in this activity is showing progress dialog
		callbackTaskManager.executeTask(saveDeviceTask, pair, CallbackTaskManager.ProgressIndicator.PROGRESS_DIALOG);
	}

	public void doSaveDeviceTask(Device.DataPair pair) {
		SaveDeviceTask saveDeviceTask = new SaveDeviceTask(this);

		saveDeviceTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					Log.d(TAG, "Success save to server");
					Toast.makeText(ModuleEditActivity.this, R.string.toast_success_save_data, Toast.LENGTH_LONG).show();
					setResult(Activity.RESULT_OK);
					finish();
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		// And don't show progressbar because in this activity is showing progress dialog
		callbackTaskManager.executeTask(saveDeviceTask, pair, CallbackTaskManager.ProgressIndicator.PROGRESS_DIALOG);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		private static final String TAG = PlaceholderFragment.class.getSimpleName();

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

		public PlaceholderFragment() {}

		public static PlaceholderFragment newInstance(String gateId, String moduleId) {
			Bundle args = new Bundle();
			args.putString(EXTRA_GATE_ID, gateId);
			args.putString(EXTRA_MODULE_ID, moduleId);

			PlaceholderFragment fragment = new PlaceholderFragment();
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

				mName.setText(module.getName());
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

		public RefreshInterval getRefreshTimeSeekBar() {
			return RefreshInterval.values()[mRefreshTimeSeekBar.getProgress()];
		}

		public String getLocationId() {
			return ((Location) mLocationSpinner.getAdapter().getItem(mLocationSpinner.getSelectedItemPosition())).getId();
		}

		public String getName() {
			return mName.getText().toString();
		}

		public String getNewLocationName() {
			return mNewLocationName.getText().toString();
		}

		public Location.LocationIcon getNewLocIcon() {
			return (Location.LocationIcon) mNewLocationIconSpinner.getAdapter().getItem(mNewLocationIconSpinner.getSelectedItemPosition());
		}

		public Location getLocation() {
			return (Location) mLocationSpinner.getAdapter().getItem(mLocationSpinner.getSelectedItemPosition());
		}

		public boolean isSetNewRoom() {
			return ((Location) mLocationSpinner.getAdapter().getItem(mLocationSpinner.getSelectedItemPosition())).getId().equals(Location.NEW_LOCATION_ID);
		}

		public boolean isSetNewCustomRoom() {
			return (mLocationSpinner.getSelectedItemPosition() == mLocationSpinner.getAdapter().getCount() - 1);
		}
	}
}