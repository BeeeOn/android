package com.rehivetech.beeeon.gui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.adapter.DeviceModuleAdapter;
import com.rehivetech.beeeon.household.device.Module;

import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author martin on 15.8.2015.
 */
public class DeviceDetailGroupModuleFragment extends BaseDeviceDetailFragment {

	private static final String KEY_GROUP_NAME = "group_name";

	private String mGroupName;

	private DeviceModuleAdapter mModuleAdapter;

	@BindView(R.id.device_detail_modules_list)
	RecyclerView mRecyclerView;

	@BindView(R.id.device_detail_group_module_list_empty_view)
	TextView mEmptyListView;

	public static DeviceDetailGroupModuleFragment newInstance(String gateId, String deviceId, String groupName) {
		Bundle args = new Bundle();
		fillArguments(args, gateId, deviceId);
		args.putString(KEY_GROUP_NAME, groupName);

		DeviceDetailGroupModuleFragment fragment = new DeviceDetailGroupModuleFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGroupName = getArguments().getString(KEY_GROUP_NAME);
		mModuleId = "-1";
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_device_detail_group_module, container, false);

		mUnbinder = ButterKnife.bind(this, view);

		mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
		mModuleAdapter = new DeviceModuleAdapter(mActivity, this);
		mRecyclerView.setAdapter(mModuleAdapter);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mDevice = mDeviceCallback.getDevice();
		updateData();
	}

	@Override
	public void updateData() {
		mDevice = mDeviceCallback.getDevice();

		if (mModuleAdapter != null) {
			List<Module> modules= getModulesByGroup();
			mModuleAdapter.swapModules(modules);

			if (mModuleAdapter.getItemCount() == 0) {
				mRecyclerView.setVisibility(View.GONE);
				mEmptyListView.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * Filter all device modules by group name from bundle arguments
	 * @return list of filtered modules
	 */
	private List<Module> getModulesByGroup() {
		List<Module> modules = mDevice.getVisibleModules(mHideUnavailableModules);
		Iterator<Module> iterator = modules.iterator();
		while (iterator.hasNext()) {
			Module module = iterator.next();
			if (!module.getGroupName(mActivity).equals(mGroupName)) {
				iterator.remove();
			}
		}
		return modules;
	}
}
