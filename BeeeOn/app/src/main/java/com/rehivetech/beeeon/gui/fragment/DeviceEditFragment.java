package com.rehivetech.beeeon.gui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;
import com.rehivetech.beeeon.gui.adapter.LocationArrayAdapter;
import com.rehivetech.beeeon.gui.adapter.RefreshIntervalAdapter;
import com.rehivetech.beeeon.gui.dialog.AddLocationDialog;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.AddLocationTask;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.Utils;

import org.joda.time.DateTime;

import java.util.List;

/**
 * @author David Kozak
 * @since 15.9.2015
 * TODO should rework whole selecting because onItemSelected is firing more than once!
 */
public class DeviceEditFragment extends BaseApplicationFragment implements AddLocationDialog.IAddLocationDialogListener {
	private static final String KEY_GATE_ID = "gateId";
	private static final String KEY_DEVICE_ID = "deviceId";

	private static final int DIALOG_CODE_NEW_LOCATION = 1;
	private static final String STATE_CREATING_LOCATION_DIALOG_SHOWN = "creating_location_dialog_shown";

	@Nullable
	private Location mNewLocation = null;
	@Nullable
	private Device mDevice = null;

	private String mGateId;
	private String mDeviceId;

	private EditText mDeviceNameText;
	private Spinner mLocationSpinner;
	private Spinner mRefreshTimeSpinner;
	private TextView mWarningBattery;
	private TextView mWarningRefresh;

	private boolean mIsShownDialog = false;
	private LocationArrayAdapter mLocationArrayAdapter;
	private TimeHelper mTimeHelper;
	private boolean mFirstSelect = true;

	public static DeviceEditFragment newInstance(String gateId, String deviceId) {
		Bundle args = new Bundle();
		args.putString(KEY_GATE_ID, gateId);
		args.putString(KEY_DEVICE_ID, deviceId);

		DeviceEditFragment fragment = new DeviceEditFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGateId = getArguments().getString(KEY_GATE_ID);
		mDeviceId = getArguments().getString(KEY_DEVICE_ID);

		Controller controller = Controller.getInstance(mActivity);
		mDevice = controller.getDevicesModel().getDevice(mGateId, mDeviceId);

		// Get locations list containing gate locations and also special default locations
		List<Location> locations = LocationArrayAdapter.getLocations(controller.getLocationsModel(), mActivity, mGateId);
		mLocationArrayAdapter = new LocationArrayAdapter(mActivity, locations);

		SharedPreferences prefs = controller.getUserSettings();
		mTimeHelper = Utils.getTimeHelper(prefs);

		if (savedInstanceState != null) {
			mIsShownDialog = savedInstanceState.getBoolean(STATE_CREATING_LOCATION_DIALOG_SHOWN);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_CREATING_LOCATION_DIALOG_SHOWN, mIsShownDialog);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_device_edit, container, false);

		mDeviceNameText = (EditText) view.findViewById(R.id.device_edit_device_name);
		mLocationSpinner = (Spinner) view.findViewById(R.id.device_edit_location_spinner);

		if (mDevice != null && mDevice.getRefresh() != null) {
			// Inflate refreshInterval views
			((ViewStub) view.findViewById(R.id.device_edit_fragment_refresh_view_stub)).inflate();
			mRefreshTimeSpinner = (Spinner) view.findViewById(R.id.device_edit_refresh_spinner);
			mRefreshTimeSpinner.setAdapter(new RefreshIntervalAdapter(mActivity));

			mWarningBattery = (TextView) view.findViewById(R.id.device_edit_warning_refresh_battery);
			mWarningRefresh = (TextView) view.findViewById(R.id.device_edit_warning_refresh);
		}

		mLocationSpinner.setAdapter(mLocationArrayAdapter);
		mLocationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position == mLocationArrayAdapter.getCount() - 1) {
					// the last item of the list is the new room, the callback will call saveNewDevice method which will store it in mNewLocation
					if (!mIsShownDialog) {
						AddLocationDialog.show(mActivity, DeviceEditFragment.this, DIALOG_CODE_NEW_LOCATION);
						mIsShownDialog = true;
					}
				} else {
					// set the actually selected location
					mNewLocation = (Location) parent.getItemAtPosition(position);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				mNewLocation = null;
			}
		});
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		prepareGUI();
	}

	@Override
	public void onResume() {
		super.onResume();
		GoogleAnalyticsManager.getInstance().logScreen(GoogleAnalyticsManager.DEVICE_EDIT_SCREEN);
	}

	public void prepareGUI() {
		Controller controller = Controller.getInstance(mActivity);
		mDevice = controller.getDevicesModel().getDevice(mGateId, mDeviceId);
		if (mDevice == null) return;

		mDeviceNameText.setText(mDevice.getCustomName());
		mDeviceNameText.setHint(mDevice.getType().getNameRes());

		reloadLocationSpinner();
		selectLocation(mDevice.getLocationId());
		setupRefreshInterval();
	}

	/**
	 * Prepares UI for refresh interval configuration
	 */
	private void setupRefreshInterval() {
		if(mDevice == null) return;

		final RefreshInterval refreshInterval = mDevice.getRefresh();
		if (refreshInterval == null || mRefreshTimeSpinner == null) {
			return;
		}

		mRefreshTimeSpinner.setSelection(refreshInterval.getIntervalIndex());
		mRefreshTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (mFirstSelect) {
					mFirstSelect = false;
					return;
				}

				RefreshInterval interval = (RefreshInterval) parent.getAdapter().getItem(position);
				boolean showBatteryWarning = mDevice.getBattery() != null && interval.getInterval() <= RefreshInterval.SEC_10.getInterval();
				boolean showRefreshWarning = mDevice.getRefresh() != null && refreshInterval.getInterval() >= RefreshInterval.MIN_5.getInterval();

				mWarningBattery.setVisibility(showBatteryWarning ? View.VISIBLE : View.GONE);
				mWarningRefresh.setVisibility(showRefreshWarning ? View.VISIBLE : View.GONE);

				DateTime nextWakeUp = mDevice.getLastUpdate();
				if (nextWakeUp != null) {
					nextWakeUp = nextWakeUp.plusSeconds(refreshInterval.getInterval());
				}
				mWarningRefresh.setText(getString(R.string.device_edit_warning_refresh, mTimeHelper.formatLastUpdate(nextWakeUp, Controller.getInstance(mActivity).getGatesModel().getGate(mGateId))));

				if (showBatteryWarning && showRefreshWarning) {
					RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mWarningBattery.getLayoutParams();
					layoutParams.addRule(RelativeLayout.BELOW, mWarningRefresh.getId());
					mWarningBattery.setLayoutParams(layoutParams);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	/**
	 * Is called from DeviceEditActivity when saving device (clicked on menu item)
	 *
	 * @return device data
	 */
	public Device.DataPair getNewDataPair() {
		if (mDevice == null)
			return null;

		mDevice.setCustomName(mDeviceNameText.getText().toString());
		mDevice.setLocationId(mNewLocation != null ? mNewLocation.getId() : Location.NO_LOCATION_ID);

		if (mDevice.getRefresh() != null) {
			RefreshInterval refresh = (RefreshInterval) mRefreshTimeSpinner.getSelectedItem();
			mDevice.setRefresh(refresh);
		}

		return new Device.DataPair(mDevice, mNewLocation, false); // TODO: remove the newLocation parameter and rework creating of default locations
	}

	public void reloadLocationSpinner() {
		Controller controller = Controller.getInstance(mActivity);
		// Get locations list containing gate locations and also special default locations
		List<Location> locations = LocationArrayAdapter.getLocations(controller.getLocationsModel(), mActivity, mGateId);
		mLocationArrayAdapter.setLocations(locations);
	}

	public void selectLocation(@NonNull String locationId) {
		List<Location> locations = mLocationArrayAdapter.getLocationsList();
		mLocationSpinner.setSelection(Utils.getObjectIndexFromList(locationId, locations));
	}

	/**
	 * Listener when location was created (form submitted)
	 *
	 * @param name of location
	 * @param icon of location
	 */
	@Override
	public void onCreateLocation(String name, Location.LocationIcon icon) {
		Location location = new Location(Location.NEW_LOCATION_ID, name, mGateId, icon.getId());

		final AddLocationTask addLocationTask = new AddLocationTask(mActivity);
		addLocationTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (!success) return; // TODO any error?

				Toast.makeText(mActivity, R.string.device_edit_toast_location_was_added, Toast.LENGTH_SHORT).show();
				reloadLocationSpinner();

				Location location = addLocationTask.getNewLocation();
				mNewLocation = location;
				selectLocation((location != null) ? location.getId() : "");
			}
		});

		mIsShownDialog = false;
		mActivity.callbackTaskManager.executeTask(addLocationTask, location, CallbackTaskManager.PROGRESS_DIALOG);
	}

	/**
	 * Listener when location creating was canceled (form cancelled)
	 */
	@Override
	public void onCancelCreatingLocation() {
		mIsShownDialog = false;
		if (mDevice == null) return;
		selectLocation(mDevice.getLocationId());
	}
}
