package com.rehivetech.beeeon.gui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.dashboard.DashboardModuleSelectAdapter;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.OverviewGraphItem;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;

/**
 * Created by martin on 9.2.16.
 */
public class AddDashboardOverviewGraphItemFragment extends BaseAddDashBoardItemFragment {

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

		return inflater.inflate(R.layout.fragment_add_overview_dashboard_graph_item, container, false);
	}


	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		fillAdapter(false);

		mAdapter.selectFirstModuleItem();

		mMinimum = (CardView) view.findViewById(R.id.fragment_add_dashboard_item_graph_type_min);
		mAverage = (CardView) view.findViewById(R.id.fragment_add_dashboard_item_graph_type_avg);
		mMaximum = (CardView) view.findViewById(R.id.fragment_add_dashboard_item_graph_type_max);

		mAverage.setSelected(true);

		CardView.OnClickListener cardClickListener = new CardView.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMinimum.setSelected(false);
				mAverage.setSelected(false);
				mMaximum.setSelected(false);
				v.setSelected(true);
			}
		};

		mMinimum.setOnClickListener(cardClickListener);
		mMaximum.setOnClickListener(cardClickListener);
		mAverage.setOnClickListener(cardClickListener);


		mButtonDone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				int selectedItem = mAdapter.getFirstSelectedItem();
				DashboardModuleSelectAdapter.ModuleItem moduleItem = (DashboardModuleSelectAdapter.ModuleItem) mAdapter.getItem(selectedItem);

				Controller controller = Controller.getInstance(mActivity);

				Module module = controller.getDevicesModel().getModule(mGateId, moduleItem.getAbsoluteId());
				ModuleLog.DataType dataType = getDataTypeBySelectedItem();

				OverviewGraphItem item = new OverviewGraphItem(module.getName(mActivity, true), mGateId, moduleItem.getAbsoluteId(), dataType);

				Intent data = new Intent();
				data.putExtra(DashboardFragment.EXTRA_ADD_ITEM, item);
				mActivity.setResult(10, data);
				mActivity.finish();
			}
		});

	}


}
