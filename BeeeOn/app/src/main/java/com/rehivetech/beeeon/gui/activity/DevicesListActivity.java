package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.DevicesListFragment;

public class DevicesListActivity extends BaseApplicationActivity {
	public static final String EXTRA_GATE_ID = "gate_id";
	public static final String FRG_DEVICE_LIST_TAG = "devices_list_fragment";

	private String mGateId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_devices_list);
		
		setupToolbar(R.string.empty);
		if (mActionBar != null) {
			mActionBar.setHomeButtonEnabled(true);
			mActionBar.setDisplayHomeAsUpEnabled(true);
		}

		mGateId = getIntent().getStringExtra(EXTRA_GATE_ID);
		if (mGateId == null) {
			Toast.makeText(this, R.string.module_detail_toast_not_specified_gate_or_module, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if(savedInstanceState == null){
			getSupportFragmentManager()
					.beginTransaction()
					.replace(
						R.id.devices_list_container,
						DevicesListFragment.newInstance(mGateId),
						FRG_DEVICE_LIST_TAG
					)
					.commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// because throught AndroidManifest it's not working on old devices
		if(item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
}
