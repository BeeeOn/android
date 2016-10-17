package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.dashboard.DashboardModuleSelectAdapter;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.BaseItem;
import com.rehivetech.beeeon.gui.view.FloatingActionButton;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleType;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.util.PreferencesHelper;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author martin
 * @since 7.2.16
 */
public abstract class BaseAddDashBoardItemFragment extends BaseApplicationFragment implements DashboardModuleSelectAdapter.ItemClickListener {

	protected static final String ARG_GATE_ID = "gate_id";
	protected static final String ARG_INDEX = "index";

	protected int mIndex;
	protected String mGateId;

	FloatingActionButton mButtonDone;
	@Nullable RecyclerView mRecyclerView;

	protected DashboardModuleSelectAdapter mAdapter;

	// probably has to be here because twice binding
	private Unbinder mBaseUnbinder;

	protected static void fillBaseArgs(Bundle args, int index, String gateId) {
		args.putInt(ARG_INDEX, index);
		args.putString(ARG_GATE_ID, gateId);
	}

	@Override
	@CallSuper
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();

		if (args != null) {
			mGateId = args.getString(ARG_GATE_ID);
			mIndex = args.getInt(ARG_INDEX);
		}
	}

	@Override
	@CallSuper
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mButtonDone = ButterKnife.findById(view, R.id.fragment_add_dashboard_item_button_done);
		mRecyclerView = ButterKnife.findById(view, R.id.fragment_add_dashboard_item_recyclerview);

		mAdapter = new DashboardModuleSelectAdapter(mActivity, this);
		GridLayoutManager layoutManager = new GridLayoutManager(mActivity, 2);
		layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				return mAdapter.getItemViewType(position) == DashboardModuleSelectAdapter.LAYOUT_TYPE_MODULE ? 1 : 2;
			}
		});

		if (mRecyclerView != null) {
			mRecyclerView.setLayoutManager(layoutManager);
			mRecyclerView.setAdapter(mAdapter);
		}
	}

	protected void fillAdapter(boolean withEnumTypes, @Nullable ModuleType filterBy) {
		Controller controller = Controller.getInstance(mActivity);
		boolean withoutUnavailable = PreferencesHelper.getBoolean(mActivity, controller.getUserSettings(), R.string.pref_hide_unavailable_modules_key);
		List<Device> devices = controller.getDevicesModel().getDevicesByGate(mGateId);

		List<Object> items = new ArrayList<>();

		for (Device device : devices) {

			List<String> groups = device.getModulesGroups(mActivity);
			List<Object> subItems = new ArrayList<>();

			if (groups.size() > 1) {

				for (String group : groups) {
					List<Module> modules = device.getModulesByGroupName(mActivity, group, withoutUnavailable);
					List<Object> subList = new ArrayList<>();

					for (Module module : modules) {

						if (module.getValue() instanceof EnumValue && !withEnumTypes) {
							continue;
						}

						if (filterBy != null && module.getType() != filterBy) {
							continue;
						}

						String moduleAbsoluteId = Utils.getAbsoluteModuleId(device.getId(), module.getId());
						subList.add(new DashboardModuleSelectAdapter.ModuleItem(moduleAbsoluteId, mGateId));
					}

					if (subList.size() > 0) {
						subItems.add(new DashboardModuleSelectAdapter.HeaderItem(group, DashboardModuleSelectAdapter.HeaderItem.ITEM_TYPE_DEVICE_GROUP));
						subItems.addAll(subList);
					}
				}
			} else {
				List<Module> modules = device.getVisibleModules(withoutUnavailable);

				for (Module module : modules) {

					if (module.getValue() instanceof EnumValue && !withEnumTypes) {
						continue;
					}

					if (filterBy != null && module.getType() != filterBy) {
						continue;
					}

					String moduleAbsoluteId = Utils.getAbsoluteModuleId(device.getId(), module.getId());
					subItems.add(new DashboardModuleSelectAdapter.ModuleItem(moduleAbsoluteId, mGateId));
				}
			}

			if (subItems.size() > 0) {
				items.add(new DashboardModuleSelectAdapter.HeaderItem(device.getName(mActivity), DashboardModuleSelectAdapter.HeaderItem.ITEM_TYPE_DEVICE_NAME));
				items.addAll(subItems);
			}
		}

		mAdapter.setItems(items);
	}

	/**
	 * Finishes activity by adding result to wrapper fragment/activity
	 *
	 * @param item which item was added
	 */
	protected void finishActivity(BaseItem item) {
		Intent data = new Intent();
		data.putExtra(DashboardPagerFragment.EXTRA_ADD_ITEM, item);
		data.putExtra(DashboardPagerFragment.EXTRA_INDEX, mIndex);
		mActivity.setResult(Activity.RESULT_OK, data);
		mActivity.finish();
	}

	@Override
	public void onItemClick(String absoluteModuleId) {

	}
}