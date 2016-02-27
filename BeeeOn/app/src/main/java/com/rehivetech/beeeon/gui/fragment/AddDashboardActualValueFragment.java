package com.rehivetech.beeeon.gui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.dashboard.DashboardModuleSelectAdapter;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.ActualValueItem;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin on 7.2.16.
 */
public class AddDashboardActualValueFragment extends BaseAddDashBoardItemFragment implements DashboardModuleSelectAdapter.ItemClickListener {

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

		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.fragment_add_dashboard_item_recyclerview);
		final DashboardModuleSelectAdapter adapter = new DashboardModuleSelectAdapter(mActivity, this);
		GridLayoutManager gridLayoutManager = new GridLayoutManager(mActivity, 2);
		gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				return adapter.getItemViewType(position) == DashboardModuleSelectAdapter.LAYOUT_TYPE_MODULE ? 1 : 2;
			}
		});
		recyclerView.setLayoutManager(gridLayoutManager);
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		recyclerView.setAdapter(adapter);

		fillAdapter(adapter);

		mButtonDone.setVisibility(View.GONE);
	}

	private void fillAdapter(DashboardModuleSelectAdapter adapter) {
		Controller controller = Controller.getInstance(mActivity);
		List<Device> devices = controller.getDevicesModel().getDevicesByGate(mGateId);

		List<Object> items = new ArrayList<>();

		for (Device device : devices) {
			items.add(new DashboardModuleSelectAdapter.HeaderItem(device.getName(mActivity), DashboardModuleSelectAdapter.HeaderItem.ITEM_TYPE_DEVICE_NAME));
			List<String> groups = device.getModulesGroups(mActivity);

			if (groups.size() > 1) {

				for (String group : groups) {
					items.add(new DashboardModuleSelectAdapter.HeaderItem(group, DashboardModuleSelectAdapter.HeaderItem.ITEM_TYPE_DEVICE_GROUP));
					List<Module> modules = device.getModulesByGroupName(mActivity, group);

					for (Module module : modules) {
						String moduleAbsoluteId = Utils.getAbsoluteModuleId(device.getId(), module.getId());
						items.add(new DashboardModuleSelectAdapter.ModuleItem(moduleAbsoluteId, mGateId));
					}
				}
			} else {
				List<Module> modules = device.getVisibleModules();

				for (Module module : modules) {

					String moduleAbsoluteId = Utils.getAbsoluteModuleId(device.getId(), module.getId());
					items.add(new DashboardModuleSelectAdapter.ModuleItem(moduleAbsoluteId, mGateId));
				}
			}
		}

		adapter.setItems(items);
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
