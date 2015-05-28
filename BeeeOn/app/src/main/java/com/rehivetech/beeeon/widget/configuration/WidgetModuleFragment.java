package com.rehivetech.beeeon.widget.configuration;


import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.arrayadapter.ModuleArrayAdapter;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.data.WidgetModuleData;
import com.rehivetech.beeeon.widget.persistence.WidgetModulePersistence;

/**
 * @author mlyko
 */
public class WidgetModuleFragment extends WidgetConfigurationFragment {
	private static final String TAG = WidgetModuleFragment.class.getSimpleName();

	protected SeekBar mWidgetUpdateSeekBar;

	protected WidgetModuleData mWidgetData;

	private WidgetModulePersistence mWidgetModule;

	private Spinner mModuleSpinner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGeneralWidgetdata = new WidgetModuleData(mActivity.getWidgetId(), mActivity, null, null);
		mWidgetData = (WidgetModuleData) mGeneralWidgetdata;
		mWidgetModule = mWidgetData.widgetModules.get(0);
	}

	protected int getFragmentLayoutResource() {
		return R.layout.fragment_widget_device;
	}

	protected int getFragmentTitle() {
		return R.string.widget_configuration_widget_device;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mWidgetUpdateSeekBar = (SeekBar) mActivity.findViewById(R.id.widget_config_interval);
		initWidgetUpdateIntervalLayout(mWidgetUpdateSeekBar);

		mModuleSpinner = (Spinner) mActivity.findViewById(R.id.widget_config_device);
	}

	@Override
	protected void onFragmentResume() {
		super.onFragmentResume();

		updateIntervalLayout(mWidgetUpdateSeekBar);
	}

	/**
	 * Updates layout and expects to have all data fresh
	 */
	protected void updateLayout() {
		// fill sensor spinner
		ModuleArrayAdapter dataAdapter = new ModuleArrayAdapter(mActivity, R.layout.custom_spinner2_item, mModules, mLocations);
		dataAdapter.setLayoutInflater(mActivity.getLayoutInflater());
		dataAdapter.setDropDownViewResource(R.layout.custom_spinner2_dropdown_item);

		mModuleSpinner.setAdapter(dataAdapter);
		int foundIndex = Utils.getObjectIndexFromList(mWidgetModule.getId(), mModules);
		if (foundIndex != -1) mModuleSpinner.setSelection(foundIndex);
	}

	@Override
	protected boolean saveSettings() {
		Gate gate = (Gate) mAdapterSpinner.getSelectedItem();
		if (gate == null) {
			Toast.makeText(mActivity, R.string.widget_configuration_select_adapter, Toast.LENGTH_LONG).show();
			return false;
		}

		Module module = (Module) mModuleSpinner.getSelectedItem();
		if (module == null) {
			Toast.makeText(mActivity, R.string.widget_configuration_select_device, Toast.LENGTH_LONG).show();
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
