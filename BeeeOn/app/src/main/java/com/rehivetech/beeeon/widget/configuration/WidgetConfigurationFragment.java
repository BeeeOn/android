package com.rehivetech.beeeon.widget.configuration;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
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
import com.rehivetech.beeeon.gui.fragment.BaseApplicationFragment;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.data.WidgetData;
import com.rehivetech.beeeon.widget.service.WidgetService;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import timber.log.Timber;

/**
 * @author mlyko
 */
public abstract class WidgetConfigurationFragment extends BaseApplicationFragment {

	protected List<Module> mModules = new ArrayList<>();
	protected List<Location> mLocations = new ArrayList<>();

	protected WidgetConfigurationActivity mActivity;
	protected View mView;

	protected WidgetData mGeneralWidgetdata;
	protected ReloadGateDataTask mReloadTask;

	protected List<Gate> mGates;
	protected Gate mActiveGate;
	protected boolean mGateNeedsToReload;

	protected SeekBar mWidgetUpdateSeekBar;
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

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		try {
			mActivity = (WidgetConfigurationActivity) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString()
					+ " must be subclass of WidgetConfigurationActivity");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	/**
	 * Cancels shown dialog
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mActivity.getDialog() != null) mActivity.getDialog().dismiss();
	}

	/**
	 * Every fragment configuration should have its own layout
	 *
	 * @return layout resource
	 */
	@LayoutRes
	protected abstract int getFragmentLayoutResource();

	@StringRes
	protected abstract int getFragmentTitle();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(getFragmentLayoutResource(), container, false);
		mActivity.setToolbarTitle(getFragmentTitle());
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mGateSpinner = (Spinner) mActivity.findViewById(R.id.widget_config_gateway_spinner);

		TextView gateEmptyView = (TextView) mActivity.findViewById(R.id.widget_config_gateway_emptyview);
		if (gateEmptyView != null) {
			mGateSpinner.setEmptyView(gateEmptyView);
		} else {
			Timber.e("EmptyView for GateWay spinner MISSING!");
		}

		mGateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Gate gate = mGates.get(position);
				if (gate == null) return;
				onBeforeGateChanged();
				doChangeGate(gate.getId());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		mWidgetWifiLayoutWrapper = (RelativeLayout) mActivity.findViewById(R.id.widget_config_wifi_wrapper_layout);
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
	 * Called when spinner selected before actual change of gate
	 */
	protected void onBeforeGateChanged() {
	}

	/**
	 * Always get new controller when resumes, cause it can change after login/logout
	 */
	@Override
	public void onResume() {
		super.onResume();
		mGeneralWidgetdata.load();

		String gateId = mGeneralWidgetdata.widgetGateId;
		EnumSet<ReloadGateDataTask.ReloadWhat> whatToReload;
		if (gateId.isEmpty()) {
			gateId = null;
			whatToReload = EnumSet.of(
					ReloadGateDataTask.ReloadWhat.ACTIVE_GATE,
					ReloadGateDataTask.ReloadWhat.DEVICES
			);
		} else {
			whatToReload = EnumSet.of(
					ReloadGateDataTask.ReloadWhat.GATES,
					ReloadGateDataTask.ReloadWhat.DEVICES
			);
		}


		mReloadTask = new ReloadGateDataTask(mActivity, false, whatToReload);
		mReloadTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				// Redraw Activity
				Timber.d("After reload task - go to redraw activity");
				onAllGatesReload();
				// continue to refresh fragment
				onReloadTaskFinished();
			}
		});

		// async reload task
		mActivity.callbackTaskManager.executeTask(CallbackTaskManager.PROGRESS_ICON, mReloadTask, gateId);

		updateIntervalLayout();

		// we have to check it cause not every widget settings have it
		if (mWidgetUpdateWiFiCheckBox != null) {
			mWidgetUpdateWiFiCheckBox.setChecked(mGeneralWidgetdata.widgetWifiOnly);
		}
	}

	/**
	 * When fragment is shown for the first time
	 */
	protected void onAllGatesReload() {
		mGates = Controller.getInstance(getActivity()).getGatesModel().getGates();
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
	protected void onReloadTaskFinished() {
		int selectedGateIndex = selectGate(mGeneralWidgetdata.widgetGateId);
		if (selectedGateIndex == mGateSpinner.getSelectedItemPosition()) {
			doChangeGate(mActiveGate.getId());
		} else {
			mGateSpinner.setSelection(selectedGateIndex);
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
		Controller controller = Controller.getInstance(mActivity);

		mLocations = controller.getLocationsModel().getLocationsByGate(gateId);

		// get all devices by locations (avoiding device without location)
		mModules.clear();
		for (Location loc : mLocations) {
			List<Device> tempFac = controller.getDevicesModel().getDevicesByLocation(gateId, loc.getId());
			for (Device device : tempFac) {
				mModules.addAll(device.getAllModules(false));
			}
		}
	}

	/**
	 * Happens when change gate in spinner, this reloads data to be from new selected gate
	 * !! NOTE: if mGateNeedsToReload == false Then it skips whole reload task
	 *
	 * @param gateId
	 */
	protected void doChangeGate(final String gateId) {
		if (!mGateNeedsToReload) {
			getGateData(gateId);
			updateLayout();
			mGateNeedsToReload = true;
			return;
		}

		mReloadTask = new ReloadGateDataTask(mActivity, false, ReloadGateDataTask.ReloadWhat.DEVICES);
		mReloadTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				selectGate(gateId);
				getGateData(gateId);
				updateLayout();
			}
		});

		mActivity.callbackTaskManager.executeTask(CallbackTaskManager.PROGRESS_ICON, mReloadTask, gateId);
	}

	/**
	 * Selects gate either from list of gates or if not found as active gate
	 *
	 * @param gateId
	 * @return Pair of gate index in list & Gate
	 */
	protected int selectGate(String gateId) {
		int mActiveGateIndex = 0;
		Controller controller = Controller.getInstance(getActivity());
		if (!gateId.isEmpty()) {
			Pair<Integer, Gate> indexGate = Utils.getIndexAndObjectFromList(gateId, mGates);
			if (indexGate == null) {
				mActiveGate = controller.getActiveGate();
			} else {
				mActiveGateIndex = indexGate.first;
				mActiveGate = indexGate.second;
			}
		} else {
			mActiveGate = controller.getActiveGate();
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
		if (item.getItemId() == android.R.id.home) {
			if (mGeneralWidgetdata == null) {
				Timber.e("There should be widgetData !");
				Toast.makeText(mActivity, R.string.activity_configuration_toast_something_wrong, Toast.LENGTH_LONG).show();
				finishConfiguration();
			}

			if (saveSettings()) {
				mActivity.returnIntent(true);
				finishConfiguration();
			} else {
				Timber.e("Could not save widget!");
				// NOTE: need to show Toast or something in saveSettings() !!
			}

			return true;
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
	 */
	protected void updateIntervalLayout() {
		if (mWidgetUpdateSeekBar == null) return;

		int interval = Math.max(mGeneralWidgetdata.widgetInterval, mRefreshIntervalMin.getInterval());
		int intervalIndex = RefreshInterval.fromInterval(interval).getIntervalIndex();
		mWidgetUpdateSeekBar.setProgress(intervalIndex - mRefreshIntervalMin.getIntervalIndex());
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

	/**
	 * Finds module index from list by specified id
	 *
	 * @param moduleId needle
	 * @param modules  haystack
	 * @return index
	 */
	protected static int getModuleIndexFromList(String moduleId, List<Module> modules) {
		int index = 0;
		for (Module mod : modules) {
			if (mod.getModuleId().absoluteId.equals(moduleId)) return index;
			index++;
		}
		return -1;
	}
}
