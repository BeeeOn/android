package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.fragment.DeviceDetailFragment;
import com.rehivetech.beeeon.household.device.Device;

/**
 * Class that handle screen with detail of some sensor
 */
public class DeviceDetailActivity extends BaseApplicationActivity {

	private static final String TAG = DeviceDetailActivity.class.getSimpleName();

	public static final String EXTRA_MODULE_ID = "module_id";
	public static final String EXTRA_GATE_ID = "gate_id";
	public static final String EXTRA_ACTIVE_POS = "act_module_pos";

	private String mActiveGateId;
	private String mActiveModuleId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_detail);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mActiveGateId = bundle.getString(EXTRA_GATE_ID);
			mActiveModuleId = bundle.getString(EXTRA_MODULE_ID);
		}

		if (mActiveGateId == null || mActiveModuleId == null) {
			Toast.makeText(this, R.string.module_detail_toast_not_specified_gate_or_module, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		Device device = Controller.getInstance(this).getDevicesModel().getModule(mActiveGateId, mActiveModuleId).getDevice();

		DeviceDetailFragment deviceDetailFragment = DeviceDetailFragment.newInstance(mActiveGateId, device.getId());
		getSupportFragmentManager().beginTransaction().replace(R.id.device_detail_container, deviceDetailFragment).commit();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;

			case R.id.device_detail_menu_action_edit:
//				Intent intent = new Intent(this, ModuleEditActivity.class);
				break;

		}
		return false;
	}
}
