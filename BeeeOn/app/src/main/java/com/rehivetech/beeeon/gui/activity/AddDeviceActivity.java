package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.AddDeviceFragment;

public class AddDeviceActivity extends BaseApplicationActivity {

	private static final String TAG = AddDeviceActivity.class.getSimpleName();

	public static final String EXTRA_GATE_ID = "gate_id";
	public static final String EXTRA_ACTION_STATE = "action_state";

	public static final int ACTION_INITIAL = 0;
	public static final int ACTION_SEARCH = 1;
	public static final int ACTION_SETUP = 2;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_add_device);

		setupToolbar(0);
		if (mActionBar != null) {
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setDisplayShowTitleEnabled(false);
		}

		Bundle args = getIntent().getExtras();

		String gateId = args.getString(EXTRA_GATE_ID);
		int action = args.getInt(EXTRA_ACTION_STATE);

		Fragment fragment = null;
		switch (action) {
			case ACTION_INITIAL:
				fragment = AddDeviceFragment.newInstance();
				break;
			case ACTION_SEARCH:
				break;
			case ACTION_SETUP:

				break;
			default:
				throw new UnsupportedOperationException("AddDeviceActivity - unsupported action");
		}

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().replace(R.id.activity_add_device_container, fragment).commit();
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home: {
				finish();
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}
}