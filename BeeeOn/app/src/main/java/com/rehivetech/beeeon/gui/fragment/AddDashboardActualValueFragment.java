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
import com.rehivetech.beeeon.gui.adapter.dashboard.items.ActualValueItem;
import com.rehivetech.beeeon.household.device.Device;

import java.util.List;

/**
 * Created by martin on 7.2.16.
 */
public class AddDashboardActualValueFragment extends BaseAddDashBoardItemFragment {

	private Spinner mModuleSpinner;

	public static AddDashboardActualValueFragment newInstance(String gateId) {
		Bundle args = new Bundle();
		args.putString(ARG_GATE_ID, gateId);
		AddDashboardActualValueFragment fragment = new AddDashboardActualValueFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.fragment_add_dashboard_actual_value_item, container, false);

		mModuleSpinner = (Spinner) view.findViewById(R.id.fragment_add_dashboard_item_module_spinner);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		List<Device> devices = Controller.getInstance(mActivity).getDevicesModel().getDevicesByGate(mGateId);
		ArrayAdapter<SpinnerHolder> modulesAdapter = createModulesAdapter(mActivity, android.R.layout.simple_spinner_dropdown_item, devices, false);

		mModuleSpinner.setAdapter(modulesAdapter);

		mButtonDone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mItemNameEditText.getText().length() == 0) {
					mTextInputLayout.setError(getString(R.string.dashboard_add_graph_name_error));
					return;
				}

				SpinnerHolder selectedItem = (SpinnerHolder) mModuleSpinner.getSelectedItem();

				ActualValueItem item = new ActualValueItem(mItemNameEditText.getText().toString(), mGateId, selectedItem.getDevice().getId(), selectedItem.getModule().getId());

				Intent data = new Intent();
				data.putExtra(DashboardFragment.EXTRA_ADD_ITEM, item);
				mActivity.setResult(10, data);
				mActivity.finish();
			}
		});
	}
}
