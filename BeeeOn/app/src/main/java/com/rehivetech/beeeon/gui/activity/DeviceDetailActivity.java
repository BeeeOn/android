package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.DeviceDetailFragment;

/**
 * Class that handle screen with detail of some sensor
 */
public class DeviceDetailActivity extends BaseApplicationActivity {

	private static final String TAG = DeviceDetailActivity.class.getSimpleName();

	public static final String EXTRA_DEVICE_ID = "device_id";
	public static final String EXTRA_GATE_ID = "gate_id";

	private String mGateId;
	private String mDeviceId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_detail);

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

		DeviceDetailFragment deviceDetailFragment = DeviceDetailFragment.newInstance(mGateId, mDeviceId);
		getSupportFragmentManager().beginTransaction().replace(R.id.device_detail_container, deviceDetailFragment).commit();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;

			case R.id.device_detail_menu_action_edit:
				// TODO Device edit not implemented yet
				Toast.makeText(this, getString(R.string.toast_not_implemented), Toast.LENGTH_SHORT).show();
				break;

		}
		return false;
	}
}
