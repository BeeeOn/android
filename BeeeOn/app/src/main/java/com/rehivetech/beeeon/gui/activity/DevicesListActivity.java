package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.DevicesListFragment;

public class DevicesListActivity extends BaseApplicationActivity {

	@Nullable private DevicesListFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_devices_list);
		setupToolbar();

		if(savedInstanceState == null){
			getSupportFragmentManager().beginTransaction().replace(R.id.devices_list_container, DevicesListFragment.newInstance()).commit();
		}

	}
}
