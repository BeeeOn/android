package com.rehivetech.beeeon.gui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.dashboard.DashboardModuleSelectAdapter;
import com.rehivetech.beeeon.gui.view.FloatingActionButton;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.util.ChartHelper;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 *  Created by martin on 7.2.16.
 */
public abstract class BaseAddDashBoardItemFragment extends BaseApplicationFragment implements DashboardModuleSelectAdapter.ItemClickListener {

	protected static final String ARG_GATE_ID = "gate_id";

	protected String mGateId;

	protected TextInputLayout mTextInputLayout;
	protected EditText mItemNameEditText;

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

						if (module.getValue() instanceof EnumValue && !withEnumTypes) {
							continue;
						}

						String moduleAbsoluteId = Utils.getAbsoluteModuleId(device.getId(), module.getId());
						items.add(new DashboardModuleSelectAdapter.ModuleItem(moduleAbsoluteId, mGateId));
					}
				}
			} else {
				List<Module> modules = device.getVisibleModules();

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

	/**
	 * Create adapter with all modules from devices
	 * @param context app context
	 * @param layout layout resource id for item
	 * @param devicesList list of all devices
	 * @param addEmptyFirst if true, empty item will be added
	 * @return adapter
	 */
	public ArrayAdapter<SpinnerHolder> createModulesAdapter(Context context, @LayoutRes int layout, List<Device> devicesList, boolean addEmptyFirst, boolean withEnums) {
		ArrayAdapter<SpinnerHolder> adapter = new ArrayAdapter<>(context, layout);

		if (addEmptyFirst) {
			adapter.add(new SpinnerHolder(null,null));
		}

		for (Device device : devicesList) {
			for (Module module : device.getAllModules(false)) {

				if (!withEnums && (module.getValue() instanceof EnumValue)) {
					continue;
				}

				adapter.add(new SpinnerHolder(device, module));
			}
		}
		return adapter;
	}

	/**
	 * Create adapter with graph ranges
	 * @param context app context
	 * @param layout layout resource id for item
	 * @return adapter
	 */
	public ArrayAdapter<String> createGraphRangeAdapter(Context context, @LayoutRes int layout) {
		ArrayAdapter<String> graphRangeAdapter = new ArrayAdapter<>(context, layout);
		for (int range : ChartHelper.ALL_RANGES) {
			graphRangeAdapter.add(getString(ChartHelper.getIntervalString(range)));
		}
		return graphRangeAdapter;
	}


	/**
	 * Create adapter with graph data types
	 * @param context app context
	 * @param layout layout resource id for item
	 * @return adapter
	 */
	public ArrayAdapter<SpinnerDataTypeHolder> createGraphDataTypeAdapter(Context context, @LayoutRes int layout) {

		ArrayAdapter<SpinnerDataTypeHolder> graphDataType = new ArrayAdapter<>(context, layout);
		graphDataType.add(new SpinnerDataTypeHolder(ModuleLog.DataType.MINIMUM));
		graphDataType.add(new SpinnerDataTypeHolder(ModuleLog.DataType.AVERAGE));
		graphDataType.add(new SpinnerDataTypeHolder(ModuleLog.DataType.MAXIMUM));

		return graphDataType;
	}

	/**
	 * Create module absolute id
	 * @param deviceId device id
	 * @param moduleId module id
	 * @return created id
	 */
	protected String getModuleAbsoluteId(String deviceId, String moduleId) {
		return String.format("%s---%s",deviceId, moduleId);
	}

	@Override
	public void onItemClick(String absoluteModuleId) {

	}

	/**
	 * Holder for spinner module item
	 */
	protected final class SpinnerHolder {

		private Device mDevice;
		private Module mModule;

		public SpinnerHolder(@Nullable Device device, @Nullable Module module) {
			mDevice = device;
			mModule = module;
		}

		public Device getDevice() {
			return mDevice;
		}

		public Module getModule() {
			return mModule;
		}

		@Override
		public String toString() {
			if (mDevice == null || mModule == null) {
				return "";
			}
			return String.format("%s - %s", mDevice.getName(mActivity), mModule.getName(mActivity));
		}
	}

	/**
	 * Holder for spinner graph data type
	 */
	protected final class SpinnerDataTypeHolder {

		private ModuleLog.DataType mDataType;

		public SpinnerDataTypeHolder(ModuleLog.DataType dataType) {
			mDataType = dataType;
		}

		public ModuleLog.DataType getDataType() {
			return mDataType;
		}

		@Override
		public String toString() {
			int stringRes = 0;
			switch (mDataType) {
				case MINIMUM:
					stringRes = R.string.data_type_min;
					break;
				case AVERAGE:
					stringRes = R.string.data_type_avg;
					break;
				case MAXIMUM:
					stringRes = R.string.data_type_max;
					break;
			}
			return getString(stringRes);
		}
	}
}