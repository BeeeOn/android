package com.rehivetech.beeeon.gui.fragment;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.dashboard.DashboardModuleSelectAdapter;
import com.rehivetech.beeeon.gui.view.FloatingActionButton;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.util.UnavailableModules;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 *  Created by martin on 7.2.16.
 */
public abstract class BaseAddDashBoardItemFragment extends BaseApplicationFragment implements DashboardModuleSelectAdapter.ItemClickListener {

	protected static final String ARG_GATE_ID = "gate_id";

	protected String mGateId;

	protected CardView mMinimum;
	protected CardView mAverage;
	protected CardView mMaximum;
	protected FloatingActionButton mButtonDone;

	protected DashboardModuleSelectAdapter mAdapter;

	@Override
	@CallSuper
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();

		if (args != null) {
			mGateId = args.getString(ARG_GATE_ID);
		}
	}

	@Override
	@CallSuper
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.fragment_add_dashboard_item_recyclerview);
		mAdapter = new DashboardModuleSelectAdapter(mActivity, this);
		GridLayoutManager layoutManager = new GridLayoutManager(mActivity, 2);
		layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				return mAdapter.getItemViewType(position) == DashboardModuleSelectAdapter.LAYOUT_TYPE_MODULE ? 1 : 2;
			}
		});
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setAdapter(mAdapter);

		mButtonDone = (FloatingActionButton) view.findViewById(R.id.fragment_add_dashboard_item_button_done);
	}

	protected void fillAdapter(boolean withEnumTypes) {
		Controller controller = Controller.getInstance(mActivity);
		boolean withoutUnavailable = UnavailableModules.fromSettings(controller.getUserSettings());
		List<Device> devices = controller.getDevicesModel().getDevicesByGate(mGateId);

		List<Object> items = new ArrayList<>();

		for (Device device : devices) {
			items.add(new DashboardModuleSelectAdapter.HeaderItem(device.getName(mActivity), DashboardModuleSelectAdapter.HeaderItem.ITEM_TYPE_DEVICE_NAME));
			List<String> groups = device.getModulesGroups(mActivity);

			if (groups.size() > 1) {

				for (String group : groups) {
					List<Module> modules = device.getModulesByGroupName(mActivity, group, withoutUnavailable);
					List<Object> subList = new ArrayList<>();

					for (Module module : modules) {

						if (module.getValue() instanceof EnumValue && !withEnumTypes) {
							continue;
						}

						String moduleAbsoluteId = Utils.getAbsoluteModuleId(device.getId(), module.getId());
						subList.add(new DashboardModuleSelectAdapter.ModuleItem(moduleAbsoluteId, mGateId));
					}

					if (subList.size() > 0) {
						items.add(new DashboardModuleSelectAdapter.HeaderItem(group, DashboardModuleSelectAdapter.HeaderItem.ITEM_TYPE_DEVICE_GROUP));
						items.addAll(subList);
					}
				}
			} else {
				List<Module> modules = device.getVisibleModules(withoutUnavailable);

				for (Module module : modules) {

					if (module.getValue() instanceof EnumValue && !withEnumTypes) {
						continue;
					}

					String moduleAbsoluteId = Utils.getAbsoluteModuleId(device.getId(), module.getId());
					items.add(new DashboardModuleSelectAdapter.ModuleItem(moduleAbsoluteId, mGateId));
				}
			}
		}

		mAdapter.setItems(items);
	}

	protected ModuleLog.DataType getDataTypeBySelectedItem() {

		if (mMinimum.isSelected()) {
			return ModuleLog.DataType.MINIMUM;

		} else if (mAverage.isSelected()) {
			return ModuleLog.DataType.AVERAGE;

		} else {
			return ModuleLog.DataType.MAXIMUM;
		}
	}

	@Override
	public void onItemClick(String absoluteModuleId) {

	}
}