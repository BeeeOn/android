package com.rehivetech.beeeon.widget.configuration;


import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.arrayadapter.LocationArrayAdapter;
import com.rehivetech.beeeon.asynctask.ReloadAdapterDataTask;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.data.WidgetLocationData;
import com.rehivetech.beeeon.widget.persistence.WidgetLocationPersistence;
import com.rehivetech.beeeon.widget.service.WidgetService;

/**
 * @author mlyko
 */
public class WidgetLocationFragment extends WidgetConfigurationFragment {
	private static final String TAG = WidgetLocationFragment.class.getSimpleName();

	protected Spinner mAdapterSpinner;
	protected SeekBar mWidgetUpdateSeekBar;

	protected WidgetLocationData mWidgetData;

	private WidgetLocationPersistence mWidgetLocation;

	private Spinner mLocationSpinner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGeneralWidgetdata = new WidgetLocationData(mActivity.getWidgetId(), mActivity, null, null);
		mWidgetData = (WidgetLocationData) mGeneralWidgetdata;
		mWidgetLocation = mWidgetData.widgetLocation;
	}

	protected int getFragmentLayoutResource(){
		return R.layout.fragment_widget_location;
	}
	protected int getFragmentTitle(){
		return R.string.widget_configuration_widget_location;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAdapterSpinner = (Spinner) mActivity.findViewById(R.id.widget_config_gateway);
		mWidgetUpdateSeekBar = (SeekBar) mActivity.findViewById(R.id.widget_config_interval);
		initWidgetUpdateIntervalLayout();

		mLocationSpinner = (Spinner) mActivity.findViewById(R.id.widget_config_location);

		mAdapterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Adapter adapter = mAdapters.get(position);
				if (adapter == null) return;

				// TODO change to load only LOCATIONS
				doChangeAdapter(adapter.getId(), ReloadAdapterDataTask.ReloadWhat.FACILITIES);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	@Override
	protected void onAllAdaptersReload() {
		super.onAllAdaptersReload();
		// adapter spinner
		ArrayAdapter<?> arrayAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_dropdown_item, mAdapters);
		mAdapterSpinner.setAdapter(arrayAdapter);
	}

	@Override
	protected void onFragmentResume() {
		super.onFragmentResume();
		int selectedAdapterIndex = selectAdapter(mWidgetData.adapterId);
		if(selectedAdapterIndex == mAdapterSpinner.getSelectedItemPosition()){
			doChangeAdapter(mActiveAdapter.getId(), ReloadAdapterDataTask.ReloadWhat.FACILITIES);
		}
		else {
			mAdapterSpinner.setSelection(selectedAdapterIndex);
		}

		updateIntervalLayout();
	}

	/**
	 * Updates layout and expects to have all data fresh
	 */
	protected void updateLayout() {
		// fill sensor spinner
		LocationArrayAdapter dataAdapter = new LocationArrayAdapter(mActivity, R.layout.custom_spinner_item, mLocations);
		dataAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
		mLocationSpinner.setAdapter(dataAdapter);

		// set selection
		int foundIndex = Utils.getObjectIndexFromList(mWidgetLocation.getId(), mLocations);
		if(foundIndex != -1) mLocationSpinner.setSelection(foundIndex);
	}

	/**
	 * Initializes widget update interval seekbar and text
	 */
	protected void initWidgetUpdateIntervalLayout() {
		// Set Max value by length of array with values
		mWidgetUpdateSeekBar.setMax(RefreshInterval.values().length - 1);
		// set interval
		mWidgetUpdateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				setIntervalWidgetText(progress);
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

	protected void updateIntervalLayout(){
		int interval = Math.max(mWidgetData.widgetInterval, WidgetService.UPDATE_INTERVAL_MIN);
		int intervalIndex = RefreshInterval.fromInterval(interval).getIntervalIndex();
		mWidgetUpdateSeekBar.setProgress(intervalIndex);
		// set text of seekbar
		setIntervalWidgetText(intervalIndex);
	}

	/**
	 * Sets widget interval text
	 * @param intervalIndex index in seekbar
	 */
	protected void setIntervalWidgetText(int intervalIndex) {
		TextView intervalText = (TextView) mActivity.findViewById(R.id.widget_config_interval_text);
		String interval = RefreshInterval.values()[intervalIndex].getStringInterval(mActivity);
		intervalText.setText(interval);
	}

	@Override
	protected boolean saveSettings() {
		Adapter adapter = (Adapter) mAdapterSpinner.getSelectedItem();
		if (adapter == null) {
			Toast.makeText(mActivity, R.string.widget_configuration_select_adapter, Toast.LENGTH_LONG).show();
			return false;
		}

		Location location = (Location) mLocationSpinner.getSelectedItem();
		if (location == null) {
			Toast.makeText(mActivity, R.string.widget_configuration_select_location, Toast.LENGTH_LONG).show();
			return false;
		}

		mWidgetLocation.configure(location, adapter);

		//sets widgetdata
		RefreshInterval refresh = RefreshInterval.values()[mWidgetUpdateSeekBar.getProgress()];
		mWidgetData.configure(mActivity.isAppWidgetEditing(), Math.max(refresh.getInterval(), WidgetService.UPDATE_INTERVAL_MIN), adapter);

		return true;
	}
}
