package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.DeviceModuleAdapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;

import java.util.Iterator;
import java.util.List;

/**
 * @author martin on 15.8.2015.
 */
public class DeviceDetailGroupModuleFragment extends BaseApplicationFragment {

	private static final String TAG = DeviceDetailGroupModuleFragment.class.getSimpleName();

	private static final String KEY_GROUP_NAME = "group_name";
	private static final String KEY_GATE_ID = "gate_id";
	private static final String KEY_DEVICE_ID = "device_id";

	private String mGateId;
	private String mDeviceId;
	private String mGroupName;

	private Context mContext;
	private View mView;
	private DeviceModuleAdapter mModuleAdapter;

	private RecyclerView mRecyclerView;
	private TextView mEmptyListView;

	public static DeviceDetailGroupModuleFragment newInstance(String gateId, String deviceId, String groupName) {
		Bundle args = new Bundle();
		args.putString(KEY_GATE_ID, gateId);
		args.putString(KEY_DEVICE_ID, deviceId);
		args.putString(KEY_GROUP_NAME, groupName);

		DeviceDetailGroupModuleFragment fragment = new DeviceDetailGroupModuleFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mGateId = getArguments().getString(KEY_GATE_ID);
		mDeviceId = getArguments().getString(KEY_DEVICE_ID);
		mGroupName = getArguments().getString(KEY_GROUP_NAME);
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_device_detail_group_module, container, false);

		mEmptyListView = (TextView) mView.findViewById(R.id.device_detrail_module_list_empty_view);
		initLayout();
		return mView;
	}

	private List<Module> getModulesByGroupName(Device device) {
		List<Module> modules = device.getAllModules();
		Iterator<Module> moduleIterator = modules.iterator();

		while (moduleIterator.hasNext()) {
			Module module = moduleIterator.next();
			if (!module.getGroupName(mContext).equals(mGroupName)) {
				moduleIterator.remove();
			}
		}
		return modules;
	}

	private void initLayout() {
		List<Module> modules = Controller.getInstance(mContext).getDevicesModel().getDevice(mGateId, mDeviceId).getAllModules();
		Iterator<Module> iterator = modules.iterator();
		while (iterator.hasNext()) {
			Module module = iterator.next();
			if (!module.getGroupName(mContext).equals(mGroupName)) {
				iterator.remove();
			}
		}
		mRecyclerView = (RecyclerView) mView.findViewById(R.id.device_detail_modules_list);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
		mModuleAdapter = new DeviceModuleAdapter(mContext, modules);
		mRecyclerView.setAdapter(mModuleAdapter);

		if (mModuleAdapter.getItemCount() == 0) {
			mRecyclerView.setVisibility(View.GONE);
			mEmptyListView.setVisibility(View.VISIBLE);
		}

	}
}
