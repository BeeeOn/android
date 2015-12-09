package com.rehivetech.beeeon.gui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.SetupDeviceActivity;
import com.rehivetech.beeeon.gui.adapter.DeviceRecycleAdapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.location.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin on 8.12.15.
 */
public class UnPairedDeviceListFragment  extends BaseApplicationFragment implements DeviceRecycleAdapter.IItemClickListener {

	private static final String KEY_GATE_ID = "gate_id";

	private String mGateId;

	private RecyclerView mRecyclerView;
	private DeviceRecycleAdapter mAdapter;

	private ArrayList<Object> mAdapterList;
	private List<Device> mDevicesList;


	public static UnPairedDeviceListFragment newInstance(String gateId) {

		Bundle args = new Bundle();
		args.putString(KEY_GATE_ID, gateId);
		UnPairedDeviceListFragment fragment = new UnPairedDeviceListFragment();
		fragment.setArguments(args);
		return fragment;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGateId = getArguments().getString(KEY_GATE_ID);
	}


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_unpaired_devices_list, container, false);

		((View)container.getParent()).findViewById(R.id.base_guide_add_gate_next_button).setVisibility(View.INVISIBLE);

		mAdapter = new DeviceRecycleAdapter(getActivity(), this);
		mRecyclerView = (RecyclerView) view.findViewById(R.id.unpaired_devices_recyclerview);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		mRecyclerView.setAdapter(mAdapter);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mDevicesList = Controller.getInstance(getContext()).getUninitializedDevicesModel().getUninitializedDevicesByGate(mGateId);

		mAdapterList = new ArrayList<>();

		List<Integer> manufacturers = new ArrayList<>();
		for (Device device : mDevicesList) {
			if (!manufacturers.contains(device.getType().getManufacturerRes())) {
				manufacturers.add(device.getType().getManufacturerRes());
			}
		}

		for (int manufacturer : manufacturers) {
			mAdapterList.add(new Location("", getString(manufacturer), "", ""));  // TODO TEMP
			for (Device device : mDevicesList) {
				if (manufacturer == device.getType().getManufacturerRes()) {
					mAdapterList.add(device);
				}
			}
		}

		mAdapter.updateData(mAdapterList);
	}

	@Override
	public void onRecyclerViewItemClick(int position, int viewType) { //TODO temp
		int index = findDeviceIndex(position);
		SetupDeviceFragment fragment = SetupDeviceFragment.newInstance(index);
		((SetupDeviceActivity) getActivity()).setFragment(fragment);
		getActivity().findViewById(R.id.base_guide_add_gate_next_button).setVisibility(View.VISIBLE);

		((SetupDeviceActivity) getActivity()).getAdapter().showNext(index);
		((SetupDeviceActivity) getActivity()).getViewPager().setCurrentItem(1);
	}

	@Override
	public boolean onRecyclerViewItemLongClick(int position, int viewType) {
		return false;
	}


	private int findDeviceIndex(int recyclerIndex) {
		return mDevicesList.indexOf(mAdapterList.get(recyclerIndex));
	}

}
