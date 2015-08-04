package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.DevicesListFragment;

public class DevicesListActivity extends BaseApplicationActivity {
	public static final String EXTRA_GATE_ID = "gate_id";

	private String mGateId;
	@Nullable private DevicesListFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_devices_list);
		setupToolbar();

		mGateId = getIntent().getStringExtra(EXTRA_GATE_ID);
		if (mGateId == null) {
			Toast.makeText(this, R.string.toast_not_specified_gate, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if(savedInstanceState == null){
			getSupportFragmentManager().beginTransaction().replace(R.id.devices_list_container, DevicesListFragment.newInstance(mGateId)).commit();
		}

	}
}
