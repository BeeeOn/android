package com.rehivetech.beeeon.widget.configuration;


import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.adapter.ModuleArrayAdapter;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.data.WidgetModuleData;
import com.rehivetech.beeeon.widget.persistence.WidgetModulePersistence;

/**
 * @author mlyko
 */
public class WidgetModuleFragment extends WidgetConfigurationFragment {
	private static final String TAG = WidgetModuleFragment.class.getSimpleName();

	protected WidgetModuleData mWidgetData;

	private WidgetModulePersistence mWidgetModule;

	private Spinner mModuleSpinner;

	@Override
	protected int getFragmentLayoutResource() {
		return R.layout.fragment_widget_module;
	}

	@Override
	protected int getFragmentTitle() {
		return R.string.widget_module_widget_configuration_device;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mGeneralWidgetdata = new WidgetModuleData(mActivity.getWidgetId(), mActivity, null, null);
		mWidgetData = (WidgetModuleData) mGeneralWidgetdata;
		mWidgetModule = mWidgetData.widgetModules.get(0);

		mWidgetUpdateSeekBar = (SeekBar) mActivity.findViewById(R.id.widget_config_interval);
		initWidgetUpdateIntervalLayout(mWidgetUpdateSeekBar);

		mModuleSpinner = (Spinner) mActivity.findViewById(R.id.widget_config_device);
        TextView moduleEmptyView = (TextView) mActivity.findViewById(R.id.widget_config_device_emptyview);
        mModuleSpinner.setEmptyView(moduleEmptyView);
    }

    @Override
    protected void onBeforeGateChanged() {
        super.onBeforeGateChanged();
        mModuleSpinner.setAdapter(null);
    }

	/**
	 * Updates layout and expects to have all data fresh
	 */
	@Override
	protected void updateLayout() {
		// fill sensor spinner
		ModuleArrayAdapter dataAdapter = new ModuleArrayAdapter(mActivity, R.layout.item_spinner_module_icon_twoline, mModules, mLocations);
		dataAdapter.setLayoutInflater(mActivity.getLayoutInflater());
		dataAdapter.setDropDownViewResource(R.layout.item_spinner_module_icon_twoline_dropdown);

		mModuleSpinner.setAdapter(dataAdapter);
		int foundIndex = getModuleIndexFromList(mWidgetModule.getId(), mModules);
		if (foundIndex != -1) mModuleSpinner.setSelection(foundIndex);
	}

	@Override
	protected boolean saveSettings() {
		Gate gate = (Gate) mGateSpinner.getSelectedItem();
		if (gate == null) {
			Toast.makeText(mActivity, R.string.widget_clock_location_module_widget_toast_select_gate, Toast.LENGTH_LONG).show();
			return false;
		}

		Module module = (Module) mModuleSpinner.getSelectedItem();
		if (module == null) {
			Toast.makeText(mActivity, R.string.widget_clock_module_widget_toast_select_device, Toast.LENGTH_LONG).show();
			return false;
		}

		mWidgetModule.configure(module, gate);
		//sets widgetdata
		mWidgetData.configure(
				mActivity.isAppWidgetEditing(),
				getRefreshSeconds(mWidgetUpdateSeekBar.getProgress()),
				mWidgetUpdateWiFiCheckBox.isChecked(),
				gate);

		return true;
	}
}
