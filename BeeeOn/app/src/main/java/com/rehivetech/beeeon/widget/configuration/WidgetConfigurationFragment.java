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
import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.asynctask.ReloadAdapterDataTask;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ErrorCode;
import com.rehivetech.beeeon.exception.NetworkError;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.location.Location;
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
	protected ReloadAdapterDataTask mReloadTask;

	protected List<Adapter> mAdapters;
	protected Adapter mActiveAdapter;
	protected boolean mAdapterNeedsToReload;
	protected Spinner mAdapterSpinner;
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
		mAdapterSpinner = (Spinner) mActivity.findViewById(R.id.widget_config_gateway);
		mAdapterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Adapter adapter = mAdapters.get(position);
				if (adapter == null) return;

				doChangeAdapter(adapter.getId(), ReloadAdapterDataTask.ReloadWhat.FACILITIES);
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
		mReloadTask = new ReloadAdapterDataTask(mActivity, false, ReloadAdapterDataTask.ReloadWhat.ADAPTERS_AND_ACTIVE_ADAPTER);
		mReloadTask.setListener(new CallbackTask.CallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (!success) {
					AppException e = mReloadTask.getException();
					ErrorCode errCode = e != null ? e.getErrorCode() : null;
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
				onAllAdaptersReload();
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
	protected void onAllAdaptersReload() {
		mAdapters = mController.getAdaptersModel().getAdapters();
		mAdapterNeedsToReload = false;
		// adapter spinner refresh
		ArrayAdapter<?> arrayAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_item, mAdapters);
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAdapterSpinner.setAdapter(arrayAdapter);
	}

	/**
	 * Method for redrawing fragment after reload task
	 * NOTE: layout is updated from reload task
	 */
	protected void onFragmentResume() {
		mGeneralWidgetdata.load();

		int selectedAdapterIndex = selectAdapter(mGeneralWidgetdata.widgetAdapterId);
		if (selectedAdapterIndex == mAdapterSpinner.getSelectedItemPosition()) {
			doChangeAdapter(mActiveAdapter.getId(), ReloadAdapterDataTask.ReloadWhat.FACILITIES);
		} else {
			mAdapterSpinner.setSelection(selectedAdapterIndex);
		}

		// we have to check it cause not every widget settings have it
		if (mWidgetUpdateWiFiCheckBox != null) {
			mWidgetUpdateWiFiCheckBox.setChecked(mGeneralWidgetdata.widgetWifiOnly);
		}
	}

	protected abstract void updateLayout();

	/**
	 * After reload task we can get new devices and locations by adapter.
	 * If no adapter set, it selects active adapter in the app
	 *
	 * @param adapterId
	 */
	protected void getAdapterData(String adapterId) {
		if (adapterId.isEmpty()) return;

		mLocations = mController.getLocationsModel().getLocationsByAdapter(adapterId);

		// get all devices by locations (avoiding mDevice without location)
		mModules.clear();
		for (Location loc : mLocations) {
			List<Device> tempFac = mController.getDevicesModel().getDevicesByLocation(adapterId, loc.getId());
			for (Device device : tempFac) {
				mModules.addAll(device.getModules());
			}
		}
	}

	/**
	 * Happens when change adapter in spinner, this reloads data to be from new selected adapter
	 * !! NOTE: if mAdapterNeedsToReload == false Then it skips whole reload task
	 *
	 * @param adapterId
	 */
	protected void doChangeAdapter(final String adapterId, ReloadAdapterDataTask.ReloadWhat whatToReload) {
		if (!mAdapterNeedsToReload) {
			getAdapterData(adapterId);
			updateLayout();
			mAdapterNeedsToReload = true;
			return;
		}

		mReloadTask = new ReloadAdapterDataTask(mActivity, false, whatToReload);
		mReloadTask.setListener(new CallbackTask.CallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				selectAdapter(adapterId);
				getAdapterData(adapterId);
				updateLayout();
				mActivity.getDialog().dismiss();
			}
		});

		mActivity.getDialog().show();
		mReloadTask.execute(adapterId);
	}

	/**
	 * Selects adapter either from list of adapters or if not found as active adapter
	 *
	 * @param adapterId
	 * @return Pair of adapter index in list & Adapter
	 */
	protected int selectAdapter(String adapterId) {
		int mActiveAdapterIndex = 0;
		if (!adapterId.isEmpty()) {
			Pair<Integer, Adapter> indexAdapter = Utils.getIndexAndObjectFromList(adapterId, mAdapters);
			if (indexAdapter == null) {
				mActiveAdapter = mController.getActiveAdapter();
			} else {
				mActiveAdapterIndex = indexAdapter.first;
				mActiveAdapter = indexAdapter.second;
			}
		} else {
			mActiveAdapter = mController.getActiveAdapter();
		}

		return mActiveAdapterIndex;
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
