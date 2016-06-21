package com.rehivetech.beeeon.gui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.dialog.ConfirmDialog;
import com.rehivetech.beeeon.gui.fragment.DeviceEditFragment;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.RemoveDeviceTask;
import com.rehivetech.beeeon.threading.task.SaveDeviceTask;

/**
 * @author David Kozak
 * @since 15.9.2015
 */
public class DeviceEditActivity extends BaseApplicationActivity implements ConfirmDialog.ConfirmDialogListener {
	private static final String TAG = DeviceEditActivity.class.getSimpleName();

	private String mGateId;
	private String mDeviceId;

	@Nullable
	private DeviceEditFragment mFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_edit);
		setupToolbar(R.string.empty, INDICATOR_DISCARD);

		Intent intent = getIntent();
		mGateId = intent.getStringExtra(DeviceDetailActivity.EXTRA_GATE_ID);
		mDeviceId = intent.getStringExtra(DeviceDetailActivity.EXTRA_DEVICE_ID);

		if (mGateId == null || mDeviceId == null) {
			Toast.makeText(this, R.string.device_edit_toast_not_specified_gate_or_device, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (savedInstanceState == null) {
			mFragment = DeviceEditFragment.newInstance(mGateId, mDeviceId);
			getSupportFragmentManager().beginTransaction().replace(R.id.device_edit_fragment_holder, mFragment).commit();
		} else {
			mFragment = (DeviceEditFragment) getSupportFragmentManager().findFragmentById(R.id.device_edit_fragment_holder);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.device_edit_action_delete:
				Controller controller = Controller.getInstance(this);
				Device device = controller.getDevicesModel().getDevice(mGateId, mDeviceId);
				if (device == null) {
					Log.e(TAG, "No device found!");
					return true;
				}

				String title = getString(R.string.module_list_dialog_title_unregister_device, device.getName(this));
				String message = getString(R.string.module_list_dialog_message_unregister_device);
				ConfirmDialog.confirm(this, title, message, R.string.module_list_btn_unregister, ConfirmDialog.TYPE_DELETE_DEVICE, mDeviceId);
				return true;
			case R.id.device_edit_action_save:
				if (mFragment != null) {
					Device.DataPair pair = mFragment.getNewDataPair();
					doEditDeviceTask(pair);
				}
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void doEditDeviceTask(Device.DataPair pair) {
		SaveDeviceTask saveDeviceTask = new SaveDeviceTask(this);
		saveDeviceTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					Toast.makeText(DeviceEditActivity.this, R.string.device_edit_toast_editing_was_successful, Toast.LENGTH_SHORT).show();
					finish();
				}
			}
		});
		callbackTaskManager.executeTask(CallbackTaskManager.PROGRESS_DIALOG, saveDeviceTask, pair);
	}

	private void doRemoveDeviceTask(Device device) {
		final RemoveDeviceTask removeDeviceTask = new RemoveDeviceTask(this);
		removeDeviceTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					Toast.makeText(DeviceEditActivity.this, R.string.device_edit_toast_removing_was_successful, Toast.LENGTH_SHORT).show();
					finish();
				}
			}
		});
		callbackTaskManager.executeTask(removeDeviceTask, device);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_device_edit_menu, menu);
		return true;
	}

	@Override
	public void onConfirm(int confirmType, String dataId) {
		if (confirmType == ConfirmDialog.TYPE_DELETE_DEVICE) {
			doRemoveDeviceTask(Controller.getInstance(this).getDevicesModel().getDevice(mGateId, dataId));
		}
	}
}
