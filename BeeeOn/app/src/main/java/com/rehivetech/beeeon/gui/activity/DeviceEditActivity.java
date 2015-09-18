package com.rehivetech.beeeon.gui.activity;

import android.content.Intent;
import android.os.Bundle;
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
 * Created by david on 15.9.15.
 */
public class DeviceEditActivity extends BaseApplicationActivity implements ConfirmDialog.ConfirmDialogListener {

	private String mGateId;
	private String mDeviceId;

	private DeviceEditFragment mFragment;

	private static final String TAG = DeviceEditActivity.class.getSimpleName();

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
			Toast.makeText(this, R.string.device_edit_toast_not_specified_gate_or_module, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		mFragment = new DeviceEditFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.device_edit_frament_holder, mFragment).commit();
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
				Device.DataPair pair = mFragment.getNewDataPair();
				if (pair == null) {
					Toast.makeText(this, R.string.device_edit_toast_device_not_edited_successfully, Toast.LENGTH_SHORT).show();
					break;
				}
				doEditDeviceTask(pair);
				finish();
				break;

		}
		return false;
	}

	private void doEditDeviceTask(Device.DataPair pair) {
		SaveDeviceTask saveDeviceTask = new SaveDeviceTask(this);
		saveDeviceTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success)
					Toast.makeText(DeviceEditActivity.this, "Editing task successfull", Toast.LENGTH_SHORT).show();
			}
		});
		callbackTaskManager.executeTask(saveDeviceTask, pair, CallbackTaskManager.ProgressIndicator.PROGRESS_DIALOG);
	}

	private void doRemoveDeviceTask(Device device) {
		final RemoveDeviceTask removeDeviceTask = new RemoveDeviceTask(this);
		removeDeviceTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success)
					Toast.makeText(DeviceEditActivity.this, "Removing Task", Toast.LENGTH_SHORT).show();

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


	public String getmGateId() {
		return mGateId;
	}

	public String getmDeviceId() {
		return mDeviceId;
	}

	@Override
	public void onConfirm(int confirmType, String dataId) {
		if (confirmType == ConfirmDialog.TYPE_DELETE_DEVICE) {

			doRemoveDeviceTask(Controller.getInstance(this).getDevicesModel().getDevice(mGateId, dataId));
			finish();
		}
	}
}
