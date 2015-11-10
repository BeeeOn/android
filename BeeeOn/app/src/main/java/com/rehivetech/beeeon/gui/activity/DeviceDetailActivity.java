package com.rehivetech.beeeon.gui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.fragment.DeviceDetailFragment;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;

/**
 * Class that handle screen with detail of some sensor
 */
public class DeviceDetailActivity extends BaseApplicationActivity implements DeviceDetailFragment.UpdateDevice{

	private static final String TAG = DeviceDetailActivity.class.getSimpleName();

	public static final String EXTRA_GATE_ID = "gate_id";
	public static final String EXTRA_DEVICE_ID = "device_id";
	public static final String EXTRA_MODULE_ID = "module_id"; // NOTE: For future use

	private String mGateId;
	private String mDeviceId;

	private Device mDevice;

	private DeviceDetailFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_detail);

		/* // FIXME: Why this must be in DeviceDetailFragment and it doesn't work when it's here?
		setupToolbar(R.string.empty);
		if (mActionBar != null) {
			mActionBar.setHomeButtonEnabled(true);
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setDisplayShowTitleEnabled(false);
		}*/

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mGateId = bundle.getString(EXTRA_GATE_ID);
			mDeviceId = bundle.getString(EXTRA_DEVICE_ID);
		}

		if (mGateId == null || mDeviceId == null) {
			Toast.makeText(this, R.string.module_detail_toast_not_specified_gate_or_module, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		mDevice = Controller.getInstance(this).getDevicesModel().getDevice(mGateId, mDeviceId);

		if (savedInstanceState == null) {
			mFragment = DeviceDetailFragment.newInstance(mGateId, mDeviceId);
			getSupportFragmentManager().beginTransaction().replace(R.id.device_detail_container, mFragment).commit();
		} else {
			mFragment = (DeviceDetailFragment) getSupportFragmentManager().findFragmentById(R.id.device_detail_container);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;

			case R.id.device_detail_menu_action_edit:
				Intent intent = new Intent(this, DeviceEditActivity.class);
				intent.putExtra(EXTRA_GATE_ID, mGateId);
				intent.putExtra(EXTRA_DEVICE_ID, mDeviceId);
				startActivity(intent);
				break;

		}
		return false;
	}

	public CallbackTask createReloadDevicesTask(boolean forceReload) {

		ReloadGateDataTask reloadDevicesTask = new ReloadGateDataTask(this, forceReload, ReloadGateDataTask.ReloadWhat.DEVICES);

		final Activity activity = this;
		reloadDevicesTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				mDevice = Controller.getInstance(activity).getDevicesModel().getDevice(mGateId, mDeviceId);

				if (mDevice == null) {
					Log.e(TAG, String.format("Device #%s does not exists", mDeviceId));
					activity.finish();
				}

				if (success) {
					mFragment.updateData();
				}
			}
		});
		return reloadDevicesTask;
	}

	@Override
	public Device getDevice() {
		return mDevice;
	}
}
