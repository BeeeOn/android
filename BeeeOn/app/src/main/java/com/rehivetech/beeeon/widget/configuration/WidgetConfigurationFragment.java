package com.rehivetech.beeeon.widget.configuration;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.IErrorCode;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.data.WidgetData;
import com.rehivetech.beeeon.widget.service.WidgetService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mlyko
 */
public abstract class WidgetConfigurationFragment extends Fragment {
	private static final String TAG = WidgetConfigurationFragment.class.getSimpleName();

	protected List<Module> mModules = new ArrayList<Module>();
	protected List<Location> mLocations = new ArrayList<Location>();

	protected WidgetConfigurationActivity mActivity;
	protected View mView;
	protected Controller mController;

	protected WidgetData mGeneralWidgetdata;
	protected ReloadGateDataTask mReloadTask;

	protected List<Gate> mGates;
	protected Gate mActiveGate;
	protected boolean mGateNeedsToReload;
	protected Spinner mGateSpinner;
	protected RelativeLayout mWidgetWifiLayoutWrapper;
	protected CheckBox mWidgetUpdateWiFiCheckBox;

	// if seekbar should have fewer intervals
	private RefreshInterval mRefreshIntervalMin = WidgetService.UPDATE_INTERVAL_MIN;
	private int mRefreshIntervalLength = RefreshInterval.values().length - mRefreshIntervalMin.getIntervalIndex();

	/**
	 * Set what intervals can be in configuration
	 *
	 * @param minRefresh
	 */
	protected void setRefreshBounds(RefreshInterval minRefresh) {
		mRefreshIntervalMin = minRefresh;
		mRefreshIntervalLength = RefreshInterval.values().length - minRefresh.getIntervalIndex();
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		if (!(getActivity() instanceof WidgetConfigurationActivity)) {
			throw new IllegalStateException(String.format("Activity holding %s must be WidgetConfigurationActivity", TAG));
		}

		mActivity = (WidgetConfigurationActivity) getActivity();
	}

	/**
	 * Every fragment configuration should have its own layout
	 *
	 * @return layout resource
	 */
	protected abstract int getFragmentLayoutResource();

	protected abstract int getFragmentTitle();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(getFragmentLayoutResource(), container, false);
		mActivity.getToolbar().setTitle(getFragmentTitle());
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mGateSpinner = (Spinner) mActivity.findViewById(R.id.widget_config_gateway);
		mGateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Gate gate = mGates.get(position);
				if (gate == null) return;

				doChangeGate(gate.getId(), ReloadGateDataTask.ReloadWhat.DEVICES);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		mWidgetWifiLayoutWrapper = (RelativeLayout) mActivity.findViewById(R.id.widget_config_wifi_wrapper);
		mWidgetUpdateWiFiCheckBox = (CheckBox) mActivity.findViewById(R.id.widget_config_only_wifi);

		if (mWidgetWifiLayoutWrapper != null && mWidgetUpdateWiFiCheckBox != null) {
			mWidgetWifiLayoutWrapper.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mWidgetUpdateWiFiCheckBox.setChecked(!mWidgetUpdateWiFiCheckBox.isChecked());
				}
			});
		}
	}

	/**
	 * Always get new controller when resumes, cause it can change after login/logout
	 */
	@Override
	public void onResume() {
		super.onResume();
		mController = Controller.getInstance(mActivity);

		// reloads all gateways and actual one
		mReloadTask = new ReloadGateDataTask(mActivity, false, ReloadGateDataTask.ReloadWhat.GATES_AND_ACTIVE_GATE);
		mReloadTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (!success) {
					AppException e = mReloadTask.getException();
					IErrorCode errCode = e != null ? e.getErrorCode() : null;
					if (errCode != null) {
						if (errCode instanceof NetworkError && errCode == NetworkError.BAD_BT) {
							BaseApplicationActivity.redirectToLogin(mActivity);
							Toast.makeText(mActivity, e.getTranslatedErrorMessage(mActivity), Toast.LENGTH_LONG).show();
							return;
						} else {
							Toast.makeText(mActivity, e.getTranslatedErrorMessage(mActivity), Toast.LENGTH_LONG).show();
							finishConfiguration();
							return;
						}
					}
				}

				// Redraw Activity
				Log.d(TAG, "After reload task - go to redraw activity");
				onAllGatesReload();
				// continue to refresh fragment
				onFragmentResume();
				if (mActivity.getDialog() != null) mActivity.getDialog().dismiss();
			}
		});

		if (mActivity.getDialog() != null) mActivity.getDialog().show();
		mReloadTask.execute();
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause()");
	}

	/**
	 * Cancels async task or shown dialog
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mActivity.getDialog() != null) mActivity.getDialog().dismiss();

		if (mReloadTask != null) mReloadTask.cancel(true);

		//finishConfiguration();
	}

	/**
	 * When fragment is shown for the first time
	 */
	protected void onAllGatesReload() {
		mGates = mController.getGatesModel().getGates();
		mGateNeedsToReload = false;
		// gate spinner refresh
		ArrayAdapter<?> arrayAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_item, mGates);
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mGateSpinner.setAdapter(arrayAdapter);
	}

	/**
	 * Method for redrawing fragment after reload task
	 * NOTE: layout is updated from reload task
	 */
	protected void onFragmentResume() {
		mGeneralWidgetdata.load();

		int selectedGateIndex = selectGate(mGeneralWidgetdata.widgetGateId);
		if (selectedGateIndex == mGateSpinner.getSelectedItemPosition()) {
			doChangeGate(mActiveGate.getId(), ReloadGateDataTask.ReloadWhat.DEVICES);
		} else {
			mGateSpinner.setSelection(selectedGateIndex);
		}

		// we have to check it cause not every widget settings have it
		if (mWidgetUpdateWiFiCheckBox != null) {
			mWidgetUpdateWiFiCheckBox.setChecked(mGeneralWidgetdata.widgetWifiOnly);
		}
	}

	protected abstract void updateLayout();

	/**
	 * After reload task we can get new devices and locations by gate.
	 * If no gate set, it selects active gate in the app
	 *
	 * @param gateId
	 */
	protected void getGateData(String gateId) {
		if (gateId.isEmpty()) return;

		mLocations = mController.getLocationsModel().getLocationsByGate(gateId);

		// get all devices by locations (avoiding mDevice without location)
		mModules.clear();
		for (Location loc : mLocations) {
			List<Device> tempFac = mController.getDevicesModel().getDevicesByLocation(gateId, loc.getId());
			for (Device device : tempFac) {
				mModules.addAll(device.getModules());
			}
		}
	}

	/**
	 * Happens when change gate in spinner, this reloads data to be from new selected gate
	 * !! NOTE: if mGateNeedsToReload == false Then it skips whole reload task
	 *
	 * @param gateId
	 */
	protected void doChangeGate(final String gateId, ReloadGateDataTask.ReloadWhat whatToReload) {
		if (!mGateNeedsToReload) {
			getGateData(gateId);
			updateLayout();
			mGateNeedsToReload = true;
			return;
		}

		mReloadTask = new ReloadGateDataTask(mActivity, false, whatToReload);
		mReloadTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				selectGate(gateId);
				getGateData(gateId);
				updateLayout();
				mActivity.getDialog().dismiss();
			}
		});

		mActivity.getDialog().show();
		mReloadTask.execute(gateId);
	}

	/**
	 * Selects gate either from list of gates or if not found as active gate
	 *
	 * @param gateId
	 * @return Pair of gate index in list & Gate
	 */
	protected int selectGate(String gateId) {
		int mActiveGateIndex = 0;
		if (!gateId.isEmpty()) {
			Pair<Integer, Gate> indexGate = Utils.getIndexAndObjectFromList(gateId, mGates);
			if (indexGate == null) {
				mActiveGate = mController.getActiveGate();
			} else {
				mActiveGateIndex = indexGate.first;
				mActiveGate = indexGate.second;
			}
		} else {
			mActiveGate = mController.getActiveGate();
		}

		return mActiveGateIndex;
	}

	/**
	 * Clicked on actionbar button SAVE
	 *
	 * @param item
	 * @return
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (mGeneralWidgetdata == null) {
					Log.e(TAG, "There should be widgetData !");
					finishConfiguration();
				}

				if (!saveSettings()) {
					Log.e(TAG, "Could not save widget!");
					finishConfiguration();
				}

				mActivity.returnIntent(true);
				finishConfiguration();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * When clicked on save widget
	 *
	 * @return
	 */
	protected abstract boolean saveSettings();

	/**
	 * When done configurating, calling this so whole fragment and activity finishes
	 */
	public void finishConfiguration() {
		if (mActivity.isReturnResult()) {
			startWidgetOk();
		} else {
			startWidgetCancel();
		}

		mActivity.finishActivity();
	}

	/**
	 * Runs when clicked "ok" to done creation of widget
	 * !!! Starts the service !!!
	 */
	protected void startWidgetOk() {
		WidgetService.startUpdating(mActivity, new int[]{mActivity.getWidgetId()}, mActivity.isAppWidgetEditing());
	}

	/**
	 * When configuration does not finish with success calls this
	 */
	protected void startWidgetCancel() {
	}

	// -------------------------------------------------------------------- //
	// ---------------------- Widget interval methods --------------------- //
	// -------------------------------------------------------------------- //

	/**
	 * Initializes widget update interval seekbar and text
	 */
	protected void initWidgetUpdateIntervalLayout(SeekBar updateIntervalSeekbar) {
		// Set Max value by length of array with values

		updateIntervalSeekbar.setMax(mRefreshIntervalLength - 1);

		// set interval
		updateIntervalSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				setIntervalWidgetText(progress + mRefreshIntervalMin.getIntervalIndex());
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

	/**
	 * Updates the seekbar and text
	 *
	 * @param updateIntervalSeekbar
	 */
	protected void updateIntervalLayout(SeekBar updateIntervalSeekbar) {
		int interval = Math.max(mGeneralWidgetdata.widgetInterval, mRefreshIntervalMin.getInterval());
		int intervalIndex = RefreshInterval.fromInterval(interval).getIntervalIndex();
		updateIntervalSeekbar.setProgress(intervalIndex - mRefreshIntervalMin.getIntervalIndex());
		// set text of seekbar
		setIntervalWidgetText(intervalIndex);
	}

	/**
	 * Sets widget interval text
	 *
	 * @param intervalIndex index in seekbar
	 */
	protected void setIntervalWidgetText(int intervalIndex) {
		TextView intervalText = (TextView) mActivity.findViewById(R.id.widget_config_interval_text);
		if (intervalText == null) return;

		String interval = RefreshInterval.values()[intervalIndex].getStringInterval(mActivity);
		intervalText.setText(interval);
	}

	/**
	 * Get refresh seconds based on custom seekbar interval
	 *
	 * @param progressIndex
	 * @return number of seconds
	 */
	protected int getRefreshSeconds(int progressIndex) {
		RefreshInterval refreshInterval = RefreshInterval.values()[progressIndex + mRefreshIntervalMin.getIntervalIndex()];
		return Math.max(refreshInterval.getInterval(), mRefreshIntervalMin.getInterval());
	}
}
