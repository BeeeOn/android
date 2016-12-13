package com.rehivetech.beeeon.gui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.AbsListView;

import com.avast.android.dialogs.fragment.ListDialogFragment;
import com.avast.android.dialogs.iface.IListDialogListener;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.ModuleGraphActivity;
import com.rehivetech.beeeon.gui.adapter.DeviceModuleAdapter;
import com.rehivetech.beeeon.gui.dialog.NumberPickerDialogFragment;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.units.BaseUnit;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.ActorActionTask;
import com.rehivetech.beeeon.util.PreferencesHelper;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Base class of Device detail fragment which has common methods for showing dialogs and handle updating data
 * @author martin
 * @since 17/10/2016.
 */
public abstract class BaseDeviceDetailFragment extends BaseApplicationFragment implements DeviceModuleAdapter.ItemClickListener, IListDialogListener,
		NumberPickerDialogFragment.SetNewValueListener {

	private static final String KEY_GATE_ID = "gateId";
	private static final String KEY_DEVICE_ID = "deviceId";

	private static final int REQUEST_SET_ACTUATOR = 7894;

	protected DeviceDetailFragment.UpdateDevice mDeviceCallback;

	protected String mGateId;
	protected String mDeviceId;
	protected String mModuleId;

	protected Device mDevice;

	protected TimeHelper mTimeHelper;
	protected boolean mHideUnavailableModules;

	/**
	 * Fills base arguments of fragment
	 * @param args Bundle args
	 * @param gateId ID of current gateway
	 * @param deviceId ID of device
	 */
	protected static void fillArguments(Bundle args, String gateId, String deviceId) {
		args.putString(KEY_GATE_ID, gateId);
		args.putString(KEY_DEVICE_ID, deviceId);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		try {
			mDeviceCallback = (DeviceDetailFragment.UpdateDevice) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement UpdateDevice");
		}
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();

		if (args != null) {
			mGateId = args.getString(KEY_GATE_ID);
			mDeviceId = args.getString(KEY_DEVICE_ID);
		}

		mModuleId = "-1";

		Controller controller = Controller.getInstance(mActivity);

		mDevice = controller.getDevicesModel().getDevice(mGateId, mDeviceId);

		SharedPreferences prefs = controller.getUserSettings();

		mTimeHelper = Utils.getTimeHelper(prefs);
		mHideUnavailableModules = PreferencesHelper.getBoolean(mActivity, prefs, R.string.pref_hide_unavailable_modules_key);
	}

	/**
	 * Handles updating of UI after reload data from server
	 */
	protected abstract void updateData();

	@Override
	public void onItemClick(String moduleId) {
		Intent intent = ModuleGraphActivity.getActivityIntent(mActivity, mGateId, mDeviceId, moduleId);
		startActivity(intent);
	}

	@Override
	public void onButtonChangeState(String moduleId) {
		mModuleId = moduleId;
		showListDialog(moduleId);
	}

	@Override
	public void onButtonSetNewValue(String moduleId) {
		SharedPreferences preferences = Controller.getInstance(mActivity).getUserSettings();
		NumberPickerDialogFragment.showNumberPickerDialog(mActivity, mDevice.getModuleById(moduleId), preferences, this);
	}

	@Override
	public void onSwitchChange(String moduleId) {
		Module module = mDevice.getModuleById(moduleId);

		// SET NEW VALUE
		BaseValue value = module.getValue();
		if (value instanceof EnumValue) {
			((EnumValue) value).setNextValue();
		} else {
			Timber.e("We can't switch actor, which value isn't inherited from EnumValue, yet");
			return;
		}

		doChangeStateModuleTask(module);
	}

	@Override
	public void onListItemSelected(CharSequence value, int number, int requestCode) {
		if (requestCode == REQUEST_SET_ACTUATOR) {
			Module module = mDevice.getModuleById(mModuleId);
			if (module == null) {
				Timber.e("Can't load module for changing its value");
				return;
			}

			module.setValue(String.valueOf(number));
			doChangeStateModuleTask(module);
		}
	}

	@Override
	public void onSetNewValue(String moduleId, String actualValue, BaseUnit.Item unit) {
		Module module = mDevice.getModuleById(moduleId);

		if (module == null) {
			Timber.e("Can't load module for changing its value");
			return;
		}

		//convert value to base unit
		Double convertedValue = module.getValue().getUnit().convertToDefaultValue(unit, Utils.parseDoubleSafely(actualValue, 0d));
		module.setValue(String.valueOf(convertedValue));

		doChangeStateModuleTask(module);
	}

	/**
	 * Show list dialog to set new module value with enum values type
	 * @param moduleId ID of module to set
	 */
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
				.setTargetFragment(BaseDeviceDetailFragment.this, REQUEST_SET_ACTUATOR)
				.show();
	}

	/**
	 * Run async task which set new actor value
	 * @param module ID of module to change value
	 */
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
}
