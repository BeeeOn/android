package com.rehivetech.beeeon.gui.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;
import com.rehivetech.beeeon.gui.activity.AddDeviceActivity;
import com.rehivetech.beeeon.gui.activity.AddGateActivity;
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;
import com.rehivetech.beeeon.gui.activity.DeviceDetailActivity;
import com.rehivetech.beeeon.gui.activity.DevicesListActivity;
import com.rehivetech.beeeon.gui.adapter.DeviceRecycleAdapter;
import com.rehivetech.beeeon.gui.dialog.ConfirmDialog;
import com.rehivetech.beeeon.gui.view.FloatingActionButton;
import com.rehivetech.beeeon.gui.view.FloatingActionMenu;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.ICallbackTaskFactory;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.threading.task.RemoveDeviceTask;
import com.rehivetech.beeeon.util.PreferencesHelper;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DevicesListFragment extends BaseApplicationFragment implements DeviceRecycleAdapter.IItemClickListener, ConfirmDialog.ConfirmDialogListener {

	//private static final String KEY_LOC_ID = "location_id";
	private static final String KEY_GATE_ID = "gate_id";
	private static final String KEY_SELECTED_ITEMS = "selected_items";
	private static final String DEVICE_LIST_FRAGMENT_AUTO_RELOAD_ID = "deviceListFragmentAutoReload";

	private final ICallbackTaskFactory mICallbackTaskFactory = new ICallbackTaskFactory() {
		@Override
		public CallbackTask createTask() {
			return createReloadDevicesTask(true);
		}

		@Override
		public Object createParam() {
			return mActiveGateId;
		}
	};

	@BindView(R.id.devices_list_fab)
	FloatingActionMenu mFloatingActionMenu;
	@BindView(R.id.devices_list_action_add_device)
	public FloatingActionButton mFabAddDevice;
	@BindView(R.id.devices_list_action_add_gate)
	public FloatingActionButton mFabAddGate;
	@BindView(R.id.devices_list_no_items_text)
	public TextView mNoItemsTextView;
	@BindView(R.id.devices_list_refresh_button)
	public Button mRefreshButton;
	@BindView(R.id.devices_list_recyclerview)
	RecyclerView mDevicesListRecyclerview;

	public @Nullable ActionMode mActionMode;
	private Gate mActiveGate;
	private DeviceRecycleAdapter mDeviceAdapter;
	private @Nullable String mActiveGateId;

	private CallbackTask createReloadDevicesTask(boolean forceReload) {
		if (getActivity() == null)
			return null;

		ReloadGateDataTask reloadGateDataTask = new ReloadGateDataTask(
				getActivity(),
				forceReload,
				mActiveGateId == null
						? ReloadGateDataTask.RELOAD_GATES_AND_ACTIVE_GATE_DEVICES
						: EnumSet.of(ReloadGateDataTask.ReloadWhat.DEVICES));

		reloadGateDataTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (!success)
					return;

				// stop refreshing
				updateData();
			}
		});
		return reloadGateDataTask;
	}

	/**
	 * This way instead of constructor so data is passed properly
	 *
	 * @param gateId
	 * @return
	 */
	public static DevicesListFragment newInstance(String gateId) {
		DevicesListFragment fragment = new DevicesListFragment();
		Bundle args = new Bundle();
		args.putString(KEY_GATE_ID, gateId);
		//args.putString(KEY_LOC_ID, locId);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Get active gate from fragment's arguments
	 *
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mActiveGateId = getArguments().getString(KEY_GATE_ID);
		mActiveGate = Controller.getInstance(getActivity()).getGatesModel().getGate(mActiveGateId);
	}

	private void setAutoReloadDataTimer() {
		SharedPreferences prefs = Controller.getInstance(getActivity()).getUserSettings();
		int period = PreferencesHelper.getInt(mActivity, prefs, R.string.pref_actualization_time_key);

		if (period > 0)    // zero means do not update
			mActivity.callbackTaskManager.executeTaskEvery(mICallbackTaskFactory, DEVICE_LIST_FRAGMENT_AUTO_RELOAD_ID, period);
	}


	/**
	 * Initializes whole layout (need to findView from rootView)
	 *
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 * @return
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_devices_list, container, false);
		mUnbinder = ButterKnife.bind(this, rootView);

		// recyclerview
		mDeviceAdapter = new DeviceRecycleAdapter(getActivity(), this, false);
		mDevicesListRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
		mDevicesListRecyclerview.setItemAnimator(new DefaultItemAnimator());
		mDevicesListRecyclerview.setAdapter(mDeviceAdapter);

		// FAB menu
		mFloatingActionMenu.setClosedOnTouchOutside(true);

		return rootView;
	}

	@OnClick(R.id.devices_list_refresh_button)
	public void onRefreshButtonClick() {
		doReloadDevicesTask(mActiveGateId, true);
	}

	@OnClick(R.id.devices_list_action_add_device)
	public void onClickAddDevice() {
		mFloatingActionMenu.close(false);
		Intent intent = AddDeviceActivity.prepareAddDeviceActivityIntent(mActivity, mActiveGateId, AddDeviceActivity.ACTION_INITIAL, null);
		mActivity.startActivityForResult(intent, Constants.ADD_DEVICE_REQUEST_CODE);
	}

	@OnClick(R.id.devices_list_action_add_gate)
	public void onClickAddGate() {
		mFloatingActionMenu.close(false);
		Intent intent = new Intent(getActivity(), AddGateActivity.class);
		mActivity.startActivityForResult(intent, Constants.ADD_GATE_REQUEST_CODE);
	}

	/**
	 * Updates data (locally cached) && if savedInstance then recreates ActionMode with selected items
	 *
	 * @param savedInstanceState
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		updateData();
		int indicatorType = BaseApplicationActivity.INDICATOR_MENU;
		if (mActivity instanceof DevicesListActivity) {
			indicatorType = BaseApplicationActivity.INDICATOR_BACK;
		}
		mActivity.setupToolbar(R.string.nav_drawer_menu_menu_devices, indicatorType);
		mActivity.setupRefreshIcon(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doReloadDevicesTask(mActiveGateId, true);
			}
		});

		if (savedInstanceState != null) {
			// recreates selected items
			List<Integer> selectedDevices = savedInstanceState.getIntegerArrayList(KEY_SELECTED_ITEMS);
			if (selectedDevices != null && !selectedDevices.isEmpty()) {
				mActionMode = mActivity.startSupportActionMode(new ActionModeEditModules());
				mDeviceAdapter.setSelectedItems(selectedDevices);
			}
		}

		setAutoReloadDataTimer();
	}

	/**
	 * Saves selected items
	 *
	 * @param outState
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mDeviceAdapter == null) return;

		// saves positions of selected items
		outState.putIntegerArrayList(KEY_SELECTED_ITEMS, (ArrayList<Integer>) mDeviceAdapter.getSelectedItems());
	}

	/**
	 * Reloads data
	 */
	@Override
	public void onResume() {
		super.onResume();

		GoogleAnalyticsManager.getInstance().logScreen(GoogleAnalyticsManager.DEVICE_LIST_SCREEN);

		// FIXME: Is this correct or needed at all here?
		Controller controller = Controller.getInstance(getActivity());
		Gate activeGate = controller.getActiveGate();
		if (activeGate == null) {
			return;
		}

		// prevent for showing based on user's role
		mFabAddDevice.setVisibility(controller.isUserAllowed(activeGate.getRole()) ? mFabAddDevice.getVisibility(): View.GONE);

		mActiveGateId = activeGate.getId();
		doReloadDevicesTask(mActiveGateId, false);
	}

	/**
	 * Refresh layout with new data & stops refreshing
	 */
	private void updateData() {
		if (mActiveGateId == null) {
			handleEmptyViewVisibility(false);
			return;
		}

		Controller controller = Controller.getInstance(getActivity());

		boolean haveDevices = false;
		ArrayList<Object> devicesLocations = new ArrayList<>();
		for (Location loc : controller.getLocationsModel().getLocationsByGate(mActiveGateId)) {
			List<Device> devicesInLocation = controller.getDevicesModel().getDevicesByLocation(mActiveGateId, loc.getId());
			// if no devices in this location, skip
			if (devicesInLocation.isEmpty()) continue;

			devicesLocations.add(loc);
			for (Device dev : devicesInLocation) {
				devicesLocations.add(dev);
				haveDevices = true;
			}
		}

		mDeviceAdapter.updateData(devicesLocations);
		handleEmptyViewVisibility(haveDevices);
	}

	/**
	 * Decides to show emptyView
	 * TODO should be set after recycleview remove animation is done
	 *
	 * @param haveDevices
	 */
	private void handleEmptyViewVisibility(boolean haveDevices) {
		if (!haveDevices) {
			mNoItemsTextView.setVisibility(View.VISIBLE);
			mRefreshButton.setVisibility(View.VISIBLE);
		} else {
			mNoItemsTextView.setVisibility(View.GONE);
			mRefreshButton.setVisibility(View.GONE);
		}
	}

	/**
	 * Async task for refreshing data
	 *
	 * @param gateId
	 * @param forceReload
	 */
	private void doReloadDevicesTask(String gateId, boolean forceReload) {
		mActivity.callbackTaskManager.executeTask(createReloadDevicesTask(forceReload), gateId);
	}

	/**
	 * Removes device from list & model
	 *
	 * @param device
	 */
	public void doRemoveDeviceTask(final Device device) {
		// graphicaly deletes item
		final int position = mDeviceAdapter.getFirstSelectedItem();
		final Location tempLoc = handleRemoveDeviceFromList(position);
		RemoveDeviceTask removeDeviceTask = new RemoveDeviceTask(mActivity);
		removeDeviceTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					//handleRemoveDeviceFromList(position,true);
					handleEmptyViewVisibility(mDeviceAdapter.getItemCount() > 0);
					Controller.getInstance(mActivity).removeDeviceFromDashboard(mActiveGateId, device.getId());
					Toast.makeText(mActivity, R.string.activity_fragment_toast_delete_success, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(mActivity, R.string.activity_fragment_toast_delete_fail, Toast.LENGTH_SHORT).show();

					if (tempLoc != null) {
						mDeviceAdapter.addItem(position - 1, tempLoc);
					}
					mDeviceAdapter.addItem(position, device);
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(removeDeviceTask, device);

	}

	/**
	 * Calculates if it's needed to remove Location from list
	 *
	 * @param position Position of deleting item
	 */
	private
	@Nullable
	Location handleRemoveDeviceFromList(int position) {
		int positionToRemove = position;
		Location tempLoc = null;
		if (position > 0) {
			int itemPrevType = mDeviceAdapter.getItemViewType(position - 1);
			// if before is location -> position item is first in location
			if (itemPrevType == DeviceRecycleAdapter.TYPE_HEADER) {
				int itemNextType;
				// if position item is not last (something is below it)
				if (position < (mDeviceAdapter.getItemCount() - 1))
					itemNextType = mDeviceAdapter.getItemViewType(position + 1);
				else
					itemNextType = DeviceRecycleAdapter.TYPE_UNKNOWN;

				// next item is location or it is last item in list (means its only one in location)
				if (itemNextType == DeviceRecycleAdapter.TYPE_HEADER || position >= (mDeviceAdapter.getItemCount() - 1)) {
					positionToRemove -= 1;
					tempLoc = (Location) mDeviceAdapter.getItem(positionToRemove);
					mDeviceAdapter.removeItem(positionToRemove);
				}

			}
		}

		// we have to delete by newly calculated position (because of deletion of header item)
		mDeviceAdapter.removeItem(positionToRemove);

		return tempLoc;
	}

	/**
	 * Handle item click -> start new Activity
	 *
	 * @param position
	 * @param viewType ViewType based on DeviceRecyclerAdapter.getItemViewType()
	 */
	@Override
	public void onRecyclerViewItemClick(int position, int viewType) {
		if (position == RecyclerView.NO_POSITION)
			return;

		// check if we actually clicked DEVICE
		if (viewType != DeviceRecycleAdapter.TYPE_DEVICE) {
			Toast.makeText(getActivity(), R.string.activity_configuration_toast_something_wrong, Toast.LENGTH_LONG).show();
			return;
		}

		Device dev = (Device) mDeviceAdapter.getItem(position);

		// starting detail activity
		Bundle bundle = new Bundle();
		bundle.putString(DeviceDetailActivity.EXTRA_GATE_ID, dev.getGateId());
		bundle.putString(DeviceDetailActivity.EXTRA_DEVICE_ID, dev.getId());
		Intent intent = new Intent(mActivity, DeviceDetailActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	/**
	 * Long clicked on item in recyclerView -> selection (always ONE ITEM)
	 *
	 * @param position
	 * @param viewType
	 * @return
	 */
	@Override
	public boolean onRecyclerViewItemLongClick(int position, int viewType) {
		if (position == RecyclerView.NO_POSITION || mActiveGateId == null)
			return false;

		Controller controller = Controller.getInstance(getActivity());
		// we have to check if user has permission to delete item (so if not, we disable longclick)
		Gate tmpGate = controller.getGatesModel().getGate(mActiveGateId);
		if (tmpGate == null || !controller.isUserAllowed(tmpGate.getRole())) return false;

		if (mActionMode == null) {
			mActionMode = mActivity.startSupportActionMode(new ActionModeEditModules());
		}

		// this means that it's possible to select only one
		mDeviceAdapter.clearSelection();
		mDeviceAdapter.toggleSelection(position);
		return true;
	}

	/**
	 * When confirmed dialog -> Deleting device from list
	 *
	 * @param confirmType which dialog requested confirmation
	 * @param dataId      any string data sent through dialog
	 */
	@Override
	public void onConfirm(int confirmType, String dataId) {
		if (confirmType == ConfirmDialog.TYPE_DELETE_DEVICE) {
			Integer selectedFirst = mDeviceAdapter.getFirstSelectedItem();
			if (mDeviceAdapter.getItemViewType(selectedFirst) == DeviceRecycleAdapter.TYPE_DEVICE) {
				Device dev = (Device) mDeviceAdapter.getItem(selectedFirst);
				doRemoveDeviceTask(dev);
			}

			if (mActionMode != null) {
				mActionMode.finish();
			}
		}
	}


	/**
	 * ActionMode when longclicked item (showing delete button)
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
				if (mDeviceAdapter.getItemViewType(firstSelected) == DeviceRecycleAdapter.TYPE_DEVICE) {
					Device device = (Device) mDeviceAdapter.getItem(firstSelected);
					// shows confirmation dialog
					ConfirmDialog.confirm(
							DevicesListFragment.this,
							getString(R.string.module_list_dialog_title_unregister_device, device.getName(mActivity)),
							getString(R.string.module_list_dialog_message_unregister_device),
							R.string.module_list_btn_unregister,
							ConfirmDialog.TYPE_DELETE_DEVICE,
							device.getId()
					);
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

	public void setActiveGateId(@Nullable String activeGateId) {
		mActiveGateId = activeGateId;
	}
}
