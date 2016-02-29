package com.rehivetech.beeeon.gui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.ActualValueItem;

/**
 * Created by martin on 7.2.16.
 */
public class AddDashboardActualValueFragment extends BaseAddDashBoardItemFragment{

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

		return inflater.inflate(R.layout.fragment_add_dashboard_actual_value_item, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mButtonDone.setVisibility(View.GONE);
	}



	@Override
	public void onItemClick(String absoluteModuleId) {

		Controller controller = Controller.getInstance(mActivity);

		String name = controller.getDevicesModel().getModule(mGateId, absoluteModuleId).getName(mActivity, true);

		ActualValueItem item = new ActualValueItem(name, mGateId, absoluteModuleId);
		Intent data = new Intent();
		data.putExtra(DashboardFragment.EXTRA_ADD_ITEM, item);
		mActivity.setResult(10, data);
		mActivity.finish();
	}
}
