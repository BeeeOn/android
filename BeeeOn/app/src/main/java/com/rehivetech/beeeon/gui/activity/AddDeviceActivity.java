package com.rehivetech.beeeon.gui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.AddDeviceFragment;
import com.rehivetech.beeeon.gui.fragment.SearchDeviceFragment;
import com.rehivetech.beeeon.gui.fragment.SetupDeviceFragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AddDeviceActivity extends BaseApplicationActivity {

	private static final String TAG = AddDeviceActivity.class.getSimpleName();

	public static final String EXTRA_GATE_ID = "gate_id";
	public static final String EXTRA_ACTION_STATE = "action_state";
	public static final String EXTRA_DEVICE_INDEX = "device_index";

	@IntDef({ACTION_INITIAL, ACTION_SEARCH, ACTION_SETUP})
	@Retention(RetentionPolicy.CLASS)

	public @interface AddDeviceActivityState {}

	public static final int ACTION_INITIAL = 0;
	public static final int ACTION_SEARCH = 1;
	public static final int ACTION_SETUP = 2;

	@StringRes
	private int mToolbarTitleRes;

	public static Intent prepareAddDeviceActivityIntent(Context context, String gateId,@AddDeviceActivityState int state, int newDeviceIndex) {
		Intent intent = new Intent(context, AddDeviceActivity.class);
		intent.putExtra(EXTRA_GATE_ID, gateId);
		intent.putExtra(EXTRA_ACTION_STATE, state);
		intent.putExtra(EXTRA_DEVICE_INDEX, newDeviceIndex);

		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_add_device);

		Bundle args = getIntent().getExtras();

		String gateId = args.getString(EXTRA_GATE_ID);
		int action = args.getInt(EXTRA_ACTION_STATE);
		int deviceIndex = args.getInt(EXTRA_DEVICE_INDEX);

		Fragment fragment;
		switch (action) {
			case ACTION_INITIAL:
				fragment = AddDeviceFragment.newInstance(gateId);
				mToolbarTitleRes = R.string.device_add_title;
				break;
			case ACTION_SEARCH:
				fragment = SearchDeviceFragment.newInstance(gateId);
				mToolbarTitleRes = R.string.device_search_title;
				break;
			case ACTION_SETUP:
				fragment = SetupDeviceFragment.newInstance(gateId, deviceIndex);
				break;
			default:
				throw new UnsupportedOperationException("AddDeviceActivity - unsupported action");
		}

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().replace(R.id.activity_add_device_container, fragment).commit();
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		setupToolbar(mToolbarTitleRes);
		if (mActionBar != null) {
			mActionBar.setDisplayHomeAsUpEnabled(true);
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