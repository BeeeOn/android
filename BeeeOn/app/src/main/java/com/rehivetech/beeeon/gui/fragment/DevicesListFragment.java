package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.DeviceRecycleAdapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DevicesListFragment extends BaseApplicationFragment implements DeviceRecycleAdapter.IItemClickListener {
	private static final String TAG = DevicesListFragment.class.getSimpleName();

	private static final String KEY_LOC_ID = "location_id";
	private static final String KEY_GATE_ID = "gate_id";

	private RecyclerView mRecyclerView;
	private SwipeRefreshLayout mSwipeRefreshLayout;

	private ArrayList<Object> mDevicesLocations = new ArrayList<>();
	private DeviceRecycleAdapter mDeviceAdapter;

	private String mActiveGateId;

	public static DevicesListFragment newInstance(String gateId){
		DevicesListFragment fragment = new DevicesListFragment();
		Bundle args = new Bundle();
		args.putString(KEY_GATE_ID, gateId);
		//args.putString(KEY_LOC_ID, locId);
		fragment.setArguments(args);
		return fragment;
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
		mActiveGateId = getArguments().getString(KEY_GATE_ID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_devices_list, container, false);

		mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.devices_list_swiperefresh);
		mSwipeRefreshLayout = mActivity.setupSwipeLayout(mSwipeRefreshLayout, new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				updateData();
			}
		});

		mDeviceAdapter = new DeviceRecycleAdapter(this);
		mRecyclerView = (RecyclerView) rootView.findViewById(R.id.devices_list_recyclerview);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		mRecyclerView.setAdapter(mDeviceAdapter);

		//View emptyView = rootView.findViewById(R.id.recyclerview_forecast_empty);


		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		updateData();
	}

	@Override
	public void onResume() {
		super.onResume();
		Controller controller = Controller.getInstance(getActivity());

		Gate gate = controller.getGatesModel().getGate(mActiveGateId);
		if(gate == null){
			Toast.makeText(getActivity(), R.string.toast_not_specified_gate, Toast.LENGTH_LONG).show();
			return;
		}

		doReloadDevicesTask(gate.getId(), false);
	}

	/**
	 * Async task for refreshing data
	 * @param gateId
	 * @param forceRefresh
	 */
	private void doReloadDevicesTask(String gateId, boolean forceRefresh){
		ReloadGateDataTask reloadGateDataTask = new ReloadGateDataTask(getActivity(), forceRefresh, ReloadGateDataTask.ReloadWhat.DEVICES);
		reloadGateDataTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if(!success) return;

				// stop refreshing
				updateData();
			}
		});

		mActivity.callbackTaskManager.executeTask(reloadGateDataTask, gateId);
	}

	/**
	 * Refresh layout with new data
	 */
	private void updateData() {
		Controller controller = Controller.getInstance(getActivity());

		ArrayList<Object> devicesLocations = new ArrayList<>();
		for(Location loc : controller.getLocationsModel().getLocationsByGate(mActiveGateId)){
			List<Device> devicesInLocation = controller.getDevicesModel().getDevicesByLocation(mActiveGateId, loc.getId());
			// if no devices in this location, skip
			if(devicesInLocation.isEmpty()) continue;

			devicesLocations.add(loc);
			for(Device dev : devicesInLocation){
				devicesLocations.add(dev);
			}
		}

		mDeviceAdapter.updateData(devicesLocations);


		/*
		mDevicesLocations = Arrays.asList(
				new Location(Location.NEW_LOCATION_ID, "Moje super lokace", "64206", getString(R.string.loc_living_room)),
				Device.createDeviceByType(DeviceType.TYPE_0.getId(), "64206",  "100:00:FF:000:FF0"),
				Device.createDeviceByType(DeviceType.TYPE_1.getId(), "64206",  "100:00:FF:000:FF0"),
				Device.createDeviceByType(DeviceType.TYPE_2.getId(), "64206",  "100:00:FF:000:FF0"),
				Device.createDeviceByType(DeviceType.TYPE_0.getId(), "64206",  "100:00:FF:000:FF0"),
				new Location(Location.NEW_LOCATION_ID, "super lokace", "64206", getString(R.string.loc_garden)),
				Device.createDeviceByType(DeviceType.TYPE_1.getId(), "64206",  "100:00:FF:000:FF0"),
				Device.createDeviceByType(DeviceType.TYPE_2.getId(), "64206",  "100:00:FF:000:FF0"),
				Device.createDeviceByType(DeviceType.TYPE_0.getId(), "64206",  "100:00:FF:000:FF0"),
				Device.createDeviceByType(DeviceType.TYPE_1.getId(), "64206",  "100:00:FF:000:FF0"),
				new Location(Location.NEW_LOCATION_ID, "M lokace", "64206", getString(R.string.loc_living_room)),
				Device.createDeviceByType(DeviceType.TYPE_2.getId(), "64206",  "100:00:FF:000:FF0"),
				Device.createDeviceByType(DeviceType.TYPE_0.getId(), "64206",  "100:00:FF:000:FF0"),
				Device.createDeviceByType(DeviceType.TYPE_1.getId(), "64206",  "100:00:FF:000:FF0"),
				Device.createDeviceByType(DeviceType.TYPE_2.getId(), "64206",  "100:00:FF:000:FF0")
		);
		*/

		mSwipeRefreshLayout.setRefreshing(false);
	}

	@Override
	public void onRecyclerViewItemClick() {

	}

	@Override
	public boolean onRecyclerViewItemLongClick() {
		mActivity.startSupportActionMode(new ActionModeEditModules());
		return false;
	}

	private class ActionModeEditModules implements ActionMode.Callback {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.actionmode_delete, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			if (item.getItemId() == R.id.action_delete) {
				//String title = getString(R.string.confirm_remove_title, mSelectedItem.getName());
				//String message = getString(R.string.confirm_remove_watchdog_message);
				//ConfirmDialog.confirm(mActivity, title, message, R.string.button_remove, ConfirmDialog.TYPE_DELETE_WATCHDOG, mSelectedItem.getId());
			}

			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {

		}
	}
}
