package com.rehivetech.beeeon.widget.configuration;


import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.arrayadapter.ModuleArrayAdapter;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.data.WidgetGraphData;
import com.rehivetech.beeeon.widget.persistence.WidgetModulePersistence;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * @author mlyko
 */
public class WidgetGraphFragment extends WidgetConfigurationFragment {
	private static final String TAG = WidgetGraphFragment.class.getSimpleName();


	protected WidgetGraphData mWidgetData;

	private WidgetModulePersistence mWidgetModule;

	private Spinner mModuleSpinner;
	private RadioGroup mGapGroup;
	private RefreshInterval mWidgetRefreshInterval;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGeneralWidgetdata = new WidgetGraphData(mActivity.getWidgetId(), mActivity, null, null);
		mWidgetData = (WidgetGraphData) mGeneralWidgetdata;
		mWidgetModule = mWidgetData.widgetModules.get(0);
	}

	protected int getFragmentLayoutResource() {
		return R.layout.fragment_widget_graph;
	}

	protected int getFragmentTitle() {
		return R.string.widget_configuration_widget_graph;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mModuleSpinner = (Spinner) mActivity.findViewById(R.id.widget_config_device);
		mGapGroup = (RadioGroup) mActivity.findViewById(R.id.widget_config_graph_gap);
		mGapGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.widget_gap_daily:
						mWidgetData.widgetLogData.gap = ModuleLog.DataInterval.HOUR.getSeconds();
						mWidgetData.widgetLogData.intervalStart = DateTime.now(DateTimeZone.UTC).minusDays(1).getMillis();
						mWidgetRefreshInterval = RefreshInterval.MIN_30;
						break;

					case R.id.widget_gap_monthly:
						mWidgetData.widgetLogData.gap = ModuleLog.DataInterval.DAY.getSeconds();
						mWidgetData.widgetLogData.intervalStart = DateTime.now(DateTimeZone.UTC).minusMonths(1).getMillis();
						mWidgetRefreshInterval = RefreshInterval.HOUR_24;     // TODO maybe could be longer
						break;

					default:
					case R.id.widget_gap_weekly:
						mWidgetData.widgetLogData.gap = ModuleLog.DataInterval.HOUR.getSeconds();
						mWidgetData.widgetLogData.intervalStart = DateTime.now(DateTimeZone.UTC).minusWeeks(1).getMillis();
						mWidgetRefreshInterval = RefreshInterval.HOUR_12;
						break;
				}

				mWidgetData.widgetLogData.gapRadioId = checkedId;
			}
		});
	}

	@Override
	protected void onFragmentResume() {
		super.onFragmentResume();

		mGapGroup.check(mWidgetData.widgetLogData.gapRadioId);
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
		Gate gate = (Gate) mGateSpinner.getSelectedItem();
		if (gate == null) {
			Toast.makeText(mActivity, R.string.widget_configuration_select_gate, Toast.LENGTH_LONG).show();
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
				mWidgetRefreshInterval.getInterval(),
				mWidgetUpdateWiFiCheckBox.isChecked(),
				gate);
		return true;
	}
}
