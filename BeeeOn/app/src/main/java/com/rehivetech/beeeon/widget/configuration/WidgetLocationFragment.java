package com.rehivetech.beeeon.widget.configuration;


import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.adapter.LocationArrayAdapter;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.data.WidgetLocationData;
import com.rehivetech.beeeon.widget.persistence.WidgetLocationPersistence;

/**
 * @author mlyko
 */
public class WidgetLocationFragment extends WidgetConfigurationFragment {
	private static final String TAG = WidgetLocationFragment.class.getSimpleName();

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

	protected int getFragmentLayoutResource() {
		return R.layout.fragment_widget_location;
	}

	protected int getFragmentTitle() {
		return R.string.widget_configuration_widget_location;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mWidgetUpdateSeekBar = (SeekBar) mActivity.findViewById(R.id.widget_config_interval);
		initWidgetUpdateIntervalLayout(mWidgetUpdateSeekBar);

		mLocationSpinner = (Spinner) mActivity.findViewById(R.id.widget_config_location);
        TextView locationEmptyView = (TextView) mActivity.findViewById(R.id.widget_config_location_emptyview);
        mLocationSpinner.setEmptyView(locationEmptyView);
	}

    @Override
    protected void onBeforeGateChanged() {
        super.onBeforeGateChanged();
        mLocationSpinner.setAdapter(null);
    }

    /**
	 * Updates layout and expects to have all data fresh
	 */
	protected void updateLayout() {
		// fill sensor spinner
		LocationArrayAdapter dataAdapter = new LocationArrayAdapter(mActivity, R.layout.spinner_icon_item, mLocations);
		dataAdapter.setDropDownViewResource(R.layout.spinner_icon_dropdown_item);
		mLocationSpinner.setAdapter(dataAdapter);

		// set selection
		int foundIndex = Utils.getObjectIndexFromList(mWidgetLocation.getId(), mLocations);
		if (foundIndex != -1) mLocationSpinner.setSelection(foundIndex);
	}

	@Override
	protected boolean saveSettings() {
		Gate gate = (Gate) mGateSpinner.getSelectedItem();
		if (gate == null) {
			Toast.makeText(mActivity, R.string.widget_configuration_select_gate, Toast.LENGTH_LONG).show();
			return false;
		}

		Location location = (Location) mLocationSpinner.getSelectedItem();
		if (location == null) {
			Toast.makeText(mActivity, R.string.widget_configuration_select_location, Toast.LENGTH_LONG).show();
			return false;
		}

		mWidgetLocation.configure(location, gate);

		//sets widgetdata
		mWidgetData.configure(mActivity.isAppWidgetEditing(), getRefreshSeconds(mWidgetUpdateSeekBar.getProgress()), false, gate);
		return true;
	}
}
