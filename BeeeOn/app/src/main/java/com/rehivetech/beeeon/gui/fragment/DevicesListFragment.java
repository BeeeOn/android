package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.avast.android.dialogs.iface.IPositiveButtonDialogListener;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.AddDeviceActivity;
import com.rehivetech.beeeon.gui.activity.AddGateActivity;
import com.rehivetech.beeeon.gui.activity.ModuleDetailActivity;
import com.rehivetech.beeeon.gui.adapter.DeviceRecycleAdapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.threading.task.RemoveDeviceTask;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;
import net.i2p.android.ext.floatingactionbutton.FloatingActionsMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A placeholder fragment containing a simple view.
 */
public class DevicesListFragment extends BaseApplicationFragment implements DeviceRecycleAdapter.IItemClickListener, IPositiveButtonDialogListener {
	@SuppressWarnings("unused")
	private static final String TAG = DevicesListFragment.class.getSimpleName();

	private static final String KEY_LOC_ID = "location_id";
	private static final String KEY_GATE_ID = "gate_id";

	private SwipeRefreshLayout mSwipeRefreshLayout;
	private TextView mNoItemsTextView;
	private Button mRefreshButton;

	private DeviceRecycleAdapter mDeviceAdapter;
	private String mActiveGateId;
	private @Nullable ActionMode mActionMode;

	/**
	 * This way instead of constructor so data is passed properly
	 * @param gateId
	 * @return
	 */
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

		// refresh button when no items shown
		mRefreshButton = (Button) rootView.findViewById(R.id.devices_list_refresh_button);
		mRefreshButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doReloadDevicesTask(mActiveGateId, true);
			}
		});

		// no items textview
		mNoItemsTextView = (TextView) rootView.findViewById(R.id.devices_list_no_items_text);

		// recyclerview
		mDeviceAdapter = new DeviceRecycleAdapter(this);
		RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.devices_list_recyclerview);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		recyclerView.setAdapter(mDeviceAdapter);

		// FAB menu
		final FloatingActionsMenu fabMenu = (FloatingActionsMenu) rootView.findViewById(R.id.devices_list_fab);

		// FAB button add device
		FloatingActionButton fabAddDevice = (FloatingActionButton) rootView.findViewById(R.id.devices_list_action_add_device);
		fabAddDevice.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), AddDeviceActivity.class);
				mActivity.startActivityForResult(intent, Constants.ADD_DEVICE_REQUEST_CODE);
				fabMenu.collapse();
			}
		});

		// FAB button add gate
		FloatingActionButton fabAddGate = (FloatingActionButton) rootView.findViewById(R.id.devices_list_action_add_gate);
		fabAddGate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), AddGateActivity.class);
				mActivity.startActivityForResult(intent, Constants.ADD_GATE_REQUEST_CODE);
				fabMenu.collapse();
			}
		});

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		updateData();
	}

	/**
	 * Refresh layout with new data
	 */
	private void updateData() {
		Controller controller = Controller.getInstance(getActivity());

		boolean haveDevices = false;
		ArrayList<Object> devicesLocations = new ArrayList<>();
		for(Location loc : controller.getLocationsModel().getLocationsByGate(mActiveGateId)){
			List<Device> devicesInLocation = controller.getDevicesModel().getDevicesByLocation(mActiveGateId, loc.getId());
			// if no devices in this location, skip
			if(devicesInLocation.isEmpty()) continue;

			devicesLocations.add(loc);
			for(Device dev : devicesInLocation){
				devicesLocations.add(dev);
				haveDevices = true;
			}
		}

		mDeviceAdapter.updateData(devicesLocations);
		mSwipeRefreshLayout.setRefreshing(false);

		handleEmptyViewVisibility(haveDevices);
	}

	/**
	 * Decides to show emptyView
	 * TODO should be set after recycleview remove animation is done
	 * @param haveDevices
	 */
	private void handleEmptyViewVisibility(boolean haveDevices) {
		if(!haveDevices) {
			mNoItemsTextView.setVisibility(View.VISIBLE);
			mRefreshButton.setVisibility(View.VISIBLE);
		}
		else{
			mNoItemsTextView.setVisibility(View.GONE);
			mRefreshButton.setVisibility(View.GONE);
		}
	}


	/**
	 * Reloads data
	 */
	@Override
	public void onResume() {
		super.onResume();
		Controller controller = Controller.getInstance(getActivity());

		Gate gate = controller.getGatesModel().getGate(mActiveGateId);
		if(gate == null){
			Toast.makeText(getActivity(), R.string.gate_detail_toast_not_specified_gate, Toast.LENGTH_LONG).show();
			return;
		}

		doReloadDevicesTask(gate.getId(), false);
	}

	/**
	 * Finishes ActionMode
	 */
	@Override
	public void onPause() {
		super.onPause();
		if(mActionMode != null) mActionMode.finish();
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
	 * Removes device from list & model
	 * @param device
	 * @param position
	 */
	public void doRemoveDeviceTask(final Device device, final int position) {
		// graphicaly deletes item
		//handleRemoveDeviceFromList(position, true, false);
		RemoveDeviceTask removeDeviceTask = new RemoveDeviceTask(mActivity);
		removeDeviceTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				success = new Random(1243567).nextBoolean();
				if (success) {
					handleRemoveDeviceFromList(position,true, true);
					handleEmptyViewVisibility(mDeviceAdapter.getItemCount() > 0);
					Toast.makeText(mActivity, R.string.activity_fragment_toast_delete_success, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(mActivity, R.string.activity_fragment_toast_delete_fail, Toast.LENGTH_SHORT).show();
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(removeDeviceTask, device);

	}

	/**
	 * Calculates if it's needed to remove Location from list
	 * @param position  Position of deleting item
	 */
	private void handleRemoveDeviceFromList(int position, boolean removeItem, boolean removeLocation) {
		int positionToRemove = position;
		if(position > 0 && removeLocation){
			int itemPrevType = mDeviceAdapter.getItemViewType(position - 1);
			// if before is location -> position item is first in location
			if(itemPrevType == DeviceRecycleAdapter.TYPE_LOCATION){
				int itemNextType;
				// if position item is not last (something is below it)
				if(position < (mDeviceAdapter.getItemCount() - 1))
					itemNextType = mDeviceAdapter.getItemViewType(position + 1);
				else
					itemNextType = DeviceRecycleAdapter.TYPE_UNKNOWN;

				// next item is location or it is last item in list (means its only one in location)
				if(itemNextType == DeviceRecycleAdapter.TYPE_LOCATION || position >= (mDeviceAdapter.getItemCount() - 1)){
					positionToRemove -= 1;
					mDeviceAdapter.removeItem(positionToRemove);
				}

			}
		}

		if(removeItem) {
			// we have to delete by newly calculated position (because of deletion of header item)
			mDeviceAdapter.removeItem(positionToRemove);
		}
	}

	/**
	 * Handle item click
	 * @param position
	 * @param viewType
	 */
	@Override
	public void onRecyclerViewItemClick(int position, int viewType) {
		if(viewType != DeviceRecycleAdapter.TYPE_DEVICE){
			Toast.makeText(getActivity(), R.string.activity_configuration_toast_something_wrong, Toast.LENGTH_LONG).show();
			return;
		}

		Device dev = (Device) mDeviceAdapter.getItem(position);

		// starting detail activity
		Bundle bundle = new Bundle();
		bundle.putString(ModuleDetailActivity.EXTRA_GATE_ID, dev.getGateId());
		bundle.putString(ModuleDetailActivity.EXTRA_MODULE_ID, dev.getAllModules().get(0).getAbsoluteId());
		Intent intent = new Intent(mActivity, ModuleDetailActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	/**
	 * Long clicked on item in recyclerView
	 * @param position
	 * @param viewType
	 * @return
	 */
	@Override
	public boolean onRecyclerViewItemLongClick(int position, int viewType) {
		if(mActionMode == null) {
			mActionMode = mActivity.startSupportActionMode(new ActionModeEditModules());
		}

		// this means that it's possible to select only one
		mDeviceAdapter.clearSelection();
		mDeviceAdapter.toggleSelection(position);
		return true;
	}

	/**
	 * When confirmed dialog
	 * @param i requestCode (which dialog)
	 */
	@Override
	public void onPositiveButtonClicked(int i) {
		Integer selectedFirst = mDeviceAdapter.getSelectedItems().get(0);
		if(selectedFirst != null && mDeviceAdapter.getItemViewType(selectedFirst) == DeviceRecycleAdapter.TYPE_DEVICE) {
			Device dev = (Device) mDeviceAdapter.getItem(selectedFirst);
			doRemoveDeviceTask(dev, selectedFirst);
		}

		if (mActionMode != null) {
			mActionMode.finish();
		}
	}

	/**
	 * ActionMode when longclicked item
	 */
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
				int firstSelected = mDeviceAdapter.getFirstSelectedItem();
				if(mDeviceAdapter.getItemViewType(firstSelected) == DeviceRecycleAdapter.TYPE_DEVICE){
					Device dev = (Device) mDeviceAdapter.getItem(firstSelected);
					// shows confirmation dialog
					SimpleDialogFragment.createBuilder(mActivity, mActivity.getSupportFragmentManager())
							.setTitle(String.format(getString(R.string.activity_fragment_menu_dialog_title_remove), dev.getType().getTypeName()))
							.setMessage(R.string.module_list_dialog_message_unregister_device)
							.setNegativeButtonText(R.string.activity_fragment_btn_cancel)
							.setPositiveButtonText(R.string.activity_fragment_menu_btn_remove)
							.setTargetFragment(DevicesListFragment.this, 1)
							.show();
				}
			}
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mDeviceAdapter.clearSelection();
			mActionMode = null;
		}
	}
}
