package com.rehivetech.beeeon.gui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.dialog.ConfirmDialog;
import com.rehivetech.beeeon.gui.fragment.DeviceEditFragment;
import com.rehivetech.beeeon.household.device.Device;

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
				Device newDevice = mFragment.getNewDevice();
				if (newDevice == null) {
					Toast.makeText(this, R.string.device_edit_toast_device_not_edited_successfully, Toast.LENGTH_SHORT).show();
					break;
				}
				doEditDeviceTask(newDevice);
				finish();
				break;

		}
		return false;
	}

	private void doEditDeviceTask(Device device) {
		//TODO implement the task
		Toast.makeText(this, "Editing task", Toast.LENGTH_SHORT).show();
	}

	private void doUnregisterDeviceTask(String deviceId) {
		//TODO implement
		Toast.makeText(this, "Removing Task", Toast.LENGTH_SHORT).show();
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
			doUnregisterDeviceTask(dataId);
			finish();
		}
	}
}
