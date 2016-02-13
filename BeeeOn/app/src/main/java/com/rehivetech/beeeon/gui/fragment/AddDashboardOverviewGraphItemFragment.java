package com.rehivetech.beeeon.gui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.OverviewGraphItem;
import com.rehivetech.beeeon.household.device.Device;

import java.util.List;

/**
 * Created by martin on 9.2.16.
 */
public class AddDashboardOverviewGraphItemFragment extends BaseAddDashBoardItemFragment {

	private Spinner mModulesSpinner;
	private Spinner mDataTypeSpinner;

	public static AddDashboardOverviewGraphItemFragment newInstance(String gateId) {

		Bundle args = new Bundle();
		args.putString(ARG_GATE_ID, gateId);
		AddDashboardOverviewGraphItemFragment fragment = new AddDashboardOverviewGraphItemFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.fragment_add_overview_dashboard_graph_item, container, false);

		mModulesSpinner = (Spinner) view.findViewById(R.id.fragment_add_dashboard_item_module_spinner);
		mDataTypeSpinner = (Spinner) view.findViewById(R.id.fragment_add_dashboard_item_graph_data_type_spinner);
		return view;
	}


	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Controller controller = Controller.getInstance(mActivity);
		List<Device> devices = controller.getDevicesModel().getDevicesByGate(mGateId);

		ArrayAdapter<SpinnerHolder> moduleAdapter = createModulesAdapter(mActivity, android.R.layout.simple_spinner_dropdown_item, devices, false);
		ArrayAdapter<SpinnerDataTypeHolder> mGraphDataTypeAdapter = createGraphDataTypeAdapter(mActivity, android.R.layout.simple_spinner_dropdown_item);

		mModulesSpinner.setAdapter(moduleAdapter);
		mDataTypeSpinner.setAdapter(mGraphDataTypeAdapter);

		mButtonDone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mItemNameEditText.getText().length() == 0) {
					mTextInputLayout.setError(getString(R.string.dashboard_add_graph_name_error));
					return;
				}

				SpinnerHolder holder = (SpinnerHolder) mModulesSpinner.getSelectedItem();
				SpinnerDataTypeHolder dataType = (SpinnerDataTypeHolder) mDataTypeSpinner.getSelectedItem();

				OverviewGraphItem item = new OverviewGraphItem(mItemNameEditText.getText().toString(), mGateId, getModuleAbsoluteId(holder.getDevice().getId(), holder.getModule().getId()), dataType.getDataType());

				Intent data = new Intent();
				data.putExtra(DashboardFragment.EXTRA_ADD_ITEM, item);
				mActivity.setResult(10, data);
				mActivity.finish();
			}
		});

	}
}
