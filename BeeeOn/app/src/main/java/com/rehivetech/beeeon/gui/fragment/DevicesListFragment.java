package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.DeviceRecycleAdapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceType;
import com.rehivetech.beeeon.network.DemoData;

import net.i2p.android.ext.floatingactionbutton.FloatingActionsMenu;

import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DevicesListFragment extends BaseApplicationFragment {
	private static final String TAG = DevicesListFragment.class.getSimpleName();

	private static final String LCTN = "lastlocation";
	private static final String GATE_ID = "lastGateId";

	private RecyclerView mRecyclerView;
	private RecyclerView.Adapter mDeviceAdapter;
	private List<Device> mDevices;

	private FloatingActionsMenu mFloatingActionsMenu;

	private String mActiveLocationId;
	private String mActiveGateId;

	public DevicesListFragment() {
	}

	public static DevicesListFragment newInstance(){
		return new DevicesListFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		/*try {
			mCallback = (OnGateDetailsButtonsClickedListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(String.format("%s must implement onGateDetailsButtonsClickedListener", activity.toString()));
		}
		*/
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mActiveLocationId = savedInstanceState.getString(LCTN);
			mActiveGateId = savedInstanceState.getString(GATE_ID);
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_devices_list, container, false);

		//mActivity.setupSwipeLayout()

		mRecyclerView = (RecyclerView) rootView.findViewById(R.id.devices_list_recyclerview);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

		//View emptyView = rootView.findViewById(R.id.recyclerview_forecast_empty);

		mDevices = Arrays.asList(
				Device.createDeviceByType(DeviceType.TYPE_0.getId(), "64206",  "100:00:FF:000:FF0"),
				Device.createDeviceByType(DeviceType.TYPE_1.getId(), "64206",  "100:00:FF:000:FF0"),
				Device.createDeviceByType(DeviceType.TYPE_2.getId(), "64206",  "100:00:FF:000:FF0"),
				Device.createDeviceByType(DeviceType.TYPE_0.getId(), "64206",  "100:00:FF:000:FF0"),
				Device.createDeviceByType(DeviceType.TYPE_1.getId(), "64206",  "100:00:FF:000:FF0"),
				Device.createDeviceByType(DeviceType.TYPE_2.getId(), "64206",  "100:00:FF:000:FF0"),
				Device.createDeviceByType(DeviceType.TYPE_0.getId(), "64206",  "100:00:FF:000:FF0"),
				Device.createDeviceByType(DeviceType.TYPE_1.getId(), "64206",  "100:00:FF:000:FF0"),
				Device.createDeviceByType(DeviceType.TYPE_2.getId(), "64206",  "100:00:FF:000:FF0"),
				Device.createDeviceByType(DeviceType.TYPE_0.getId(), "64206",  "100:00:FF:000:FF0"),
				Device.createDeviceByType(DeviceType.TYPE_1.getId(), "64206",  "100:00:FF:000:FF0"),
				Device.createDeviceByType(DeviceType.TYPE_2.getId(), "64206",  "100:00:FF:000:FF0")
		);

		mDeviceAdapter = new DeviceRecycleAdapter(mDevices);
		mRecyclerView.setAdapter(mDeviceAdapter);

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		updateData();
	}

	private void updateData() {
		Controller controller = Controller.getInstance(getActivity());



	}
}
