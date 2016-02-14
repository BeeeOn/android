package com.rehivetech.beeeon.gui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.dialog.AddLocationDialog;
import com.rehivetech.beeeon.gui.dialog.ConfirmDialog;
import com.rehivetech.beeeon.gui.fragment.DeviceEditFragment;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.AddLocationTask;
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

		setupToolbar(R.string.empty);
		if (mActionBar != null) {
			mActionBar.setHomeButtonEnabled(true);
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setHomeAsUpIndicator(R.drawable.ic_action_cancel);
		}

		Intent intent = getIntent();
		mGateId = intent.getStringExtra(DeviceDetailActivity.EXTRA_GATE_ID);
		mDeviceId = intent.getStringExtra(DeviceDetailActivity.EXTRA_DEVICE_ID);

		if (mGateId == null || mDeviceId == null) {
			Toast.makeText(this, R.string.device_edit_toast_not_specified_gate_or_device, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (savedInstanceState == null) {
			DeviceEditFragment fragment = DeviceEditFragment.newInstance(mGateId, mDeviceId);
			getSupportFragmentManager().beginTransaction().replace(R.id.device_edit_frament_holder, fragment).commit();
		}
	}

	@Override
	public void onFragmentAttached(Fragment fragment) {
		super.onFragmentAttached(fragment);
		try {
			mFragment = (DeviceEditFragment) fragment;
		} catch (ClassCastException e) {
			throw new ClassCastException(String.format("%s must be DeviceEditFragment", fragment.toString()));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
			case R.id.device_edit_action_delete:
				String title = getString(R.string.device_edit_delete_device_confirm_dialog_title);
				String message = getString(R.string.device_edit_delete_device_confirm_dialog_text);
				ConfirmDialog.confirm(this, title, message, R.string.activity_fragment_menu_btn_remove, ConfirmDialog.TYPE_DELETE_DEVICE, mDeviceId);
				break;
			case R.id.device_edit_action_save:
				if (mFragment != null) {
					Device.DataPair pair = mFragment.getNewDataPair();
					doEditDeviceTask(pair);
				}
				break;
		}
		return false;
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
		callbackTaskManager.executeTask(saveDeviceTask, pair, CallbackTaskManager.ProgressIndicator.PROGRESS_DIALOG);
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
