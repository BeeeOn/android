package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.avast.android.dialogs.fragment.ListDialogFragment;
import com.avast.android.dialogs.iface.IListDialogListener;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.DeviceDetailActivity;
import com.rehivetech.beeeon.gui.activity.ModuleGraphActivity;
import com.rehivetech.beeeon.gui.adapter.DeviceModuleAdapter;
import com.rehivetech.beeeon.gui.dialog.NumberPickerDialogFragment;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.ActorActionTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author martin on 15.8.2015.
 */
public class DeviceDetailGroupModuleFragment extends BaseApplicationFragment implements IListDialogListener,
		DeviceModuleAdapter.ItemClickListener, NumberPickerDialogFragment.SetNewValueListener {

	private static final String TAG = DeviceDetailGroupModuleFragment.class.getSimpleName();

	private static final String KEY_GROUP_NAME = "group_name";
	private static final String KEY_GATE_ID = "gate_id";
	private static final String KEY_DEVICE_ID = "device_id";

	private static final int REQUEST_SET_ACTUATOR = 7894;

	private String mGateId;
	private String mDeviceId;
	private String mGroupName;

	private Device mDevice;
	private String mModuleId;

	private DeviceDetailActivity mActivity;
	private DeviceDetailFragment.UpdateDevice mDeviceCallback;
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
		mActivity = (DeviceDetailActivity) activity;
		mDeviceCallback = (DeviceDetailFragment.UpdateDevice) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGateId = getArguments().getString(KEY_GATE_ID);
		mDeviceId = getArguments().getString(KEY_DEVICE_ID);
		mGroupName = getArguments().getString(KEY_GROUP_NAME);
		mModuleId = "-1";
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_device_detail_group_module, container, false);

		mEmptyListView = (TextView) mView.findViewById(R.id.device_detrail_module_list_empty_view);
		initLayout();
		return mView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mDevice = mDeviceCallback.getDevice();
		updateData();
	}

	private void initLayout() {
		mRecyclerView = (RecyclerView) mView.findViewById(R.id.device_detail_modules_list);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
		mModuleAdapter = new DeviceModuleAdapter(mActivity, this);
		mRecyclerView.setAdapter(mModuleAdapter);
	}

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

	@Override
	public void onItemClick(String moduleId) {
		Log.d(TAG, "onItemClick:" + moduleId);
		Bundle args = new Bundle();
		args.putString(ModuleGraphActivity.EXTRA_GATE_ID, mGateId);
		args.putString(ModuleGraphActivity.EXTRA_DEVICE_ID, mDeviceId);
		args.putString(ModuleGraphActivity.EXTRA_MODULE_ID, moduleId);
		Intent intent = new Intent(mActivity, ModuleGraphActivity.class);
		intent.putExtras(args);
		startActivity(intent);
	}

	@Override
	public void onButtonChangeState(String moduleId) {
		Log.d(TAG, "onButtonChangeState");
		mModuleId = moduleId;
		showListDialog(moduleId);
	}

	@Override
	public void onButtonSetNewValue(String moduleId) {
		Log.d(TAG, "onButtonSetNewValue");

		NumberPickerDialogFragment.showNumberPickerDialog(mActivity, mDevice.getModuleById(moduleId), this);
	}

	@Override
	public void onSwitchChange(String moduleId) {
		Log.d(TAG, "onSwitchChange");
		Module module = mDevice.getModuleById(moduleId);
		doActorAction(module);
	}

	@Override
	public void onListItemSelected(CharSequence charSequence, int number, int requestCode) {
		if (requestCode == REQUEST_SET_ACTUATOR) {
			Module module = mDevice.getModuleById(mModuleId);
			if (module == null) {
				Log.e(TAG, "Can't load module for changing its value");
				return;
			}

			module.setValue(String.valueOf(number));
			doChangeStateModuleTask(module);
		}
	}

	private List<Module> getModulesByGroup() {
		List<Module> modules = mDevice.getVisibleModules();
		Iterator<Module> iterator = modules.iterator();
		while (iterator.hasNext()) {
			Module module = iterator.next();
			if (!module.getGroupName(mActivity).equals(mGroupName)) {
				iterator.remove();
			}
		}
		return modules;
	}

	private void showListDialog(String moduleId) {
		Module module = mDevice.getModuleById(moduleId);
		EnumValue value = (EnumValue) module.getValue();
		List<EnumValue.Item> items = value.getEnumItems();

		List<String> namesList = new ArrayList<>();
		for (EnumValue.Item item : items) {
			namesList.add(getString(item.getStringResource()));
		}
		ListDialogFragment
				.createBuilder(mActivity, getFragmentManager())
				.setTitle(module.getName(mActivity))
				.setItems(namesList.toArray(new CharSequence[namesList.size()]))
				.setSelectedItem(value.getActive().getId())
				.setRequestCode(REQUEST_SET_ACTUATOR)
				.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE)
				.setConfirmButtonText(R.string.activity_fragment_btn_set)
				.setCancelButtonText(R.string.activity_fragment_btn_cancel)
				.setTargetFragment(DeviceDetailGroupModuleFragment.this, REQUEST_SET_ACTUATOR)
				.show();
		Log.d(TAG, "dialog is created");
	}

	private void doChangeStateModuleTask(final Module module) {
		ActorActionTask changeStateModuleTask = new ActorActionTask(mActivity);

		changeStateModuleTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					mDevice = Controller.getInstance(mActivity).getDevicesModel().getDevice(mGateId, mDeviceId);
					updateData();
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(changeStateModuleTask, module);
	}

	private void doActorAction(final Module module) {
		if (!module.isActuator()) {
			return;
		}

		// SET NEW VALUE
		BaseValue value = module.getValue();
		if (value instanceof EnumValue) {
			((EnumValue) value).setNextValue();
		} else {
			Log.e(TAG, "We can't switch actor, which value isn't inherited from EnumValue, yet");
			return;
		}

		ActorActionTask actorActionTask = new ActorActionTask(mActivity);
		actorActionTask.setListener(new CallbackTask.ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (success) {
					mDevice = Controller.getInstance(mActivity).getDevicesModel().getDevice(mGateId, mDeviceId);
					updateData();
				}
			}

		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(actorActionTask, module);
	}

	@Override
	public void onSetNewValue(String moduleId, String actualValue) {
		Module module = mDevice.getModuleById(moduleId);
		if (module == null) {
			Log.e(TAG, "Can't load module for changing its value");
			return;
		}

		module.setValue(actualValue);
		doChangeStateModuleTask(module);
	}
}
