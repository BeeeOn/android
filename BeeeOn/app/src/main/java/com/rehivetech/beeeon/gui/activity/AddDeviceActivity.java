package com.rehivetech.beeeon.gui.activity;

import android.app.Activity;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.IntroFragmentPagerAdapter;
import com.rehivetech.beeeon.gui.fragment.AddDeviceFragment;
import com.rehivetech.beeeon.gui.fragment.IntroImageFragment;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.util.Log;

import java.util.Arrays;
import java.util.List;

public class AddDeviceActivity extends BaseGuideActivity implements AddDeviceFragment.OnAddSensorListener {

	private static final String TAG = AddDeviceActivity.class.getSimpleName();

	@Override
	protected void onLastFragmentActionNext() {
		AddDeviceFragment fragment = (AddDeviceFragment) mPagerAdapter.getFinalFragment();
		if (fragment == null) {
			Log.e(TAG, "AddSensorActivity.onLastFragmentActionNext() return null fragment");
			return;
		}
		fragment.doAction();
	}

	@Override
	protected IntroFragmentPagerAdapter initPagerAdapter() {
		// If there is no gate, then the activity ends immediately
		Gate gate = Controller.getInstance(this).getActiveGate();
		if (gate == null) {
			Toast.makeText(this, R.string.toast_no_adapter, Toast.LENGTH_LONG).show();
			finish();
		}

		//the List and the FragmentManager objects are needed as arguments for the constructor
		List<IntroImageFragment.ImageTextPair> pairs = Arrays.asList(
				new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_as_first_step, R.string.tut_add_sensor_text_1, R.string.tut_add_sensor_title_1),
				new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_as_second_step, R.string.tut_add_sensor_text_2, R.string.tut_add_sensor_title_2)
		);

		return new IntroFragmentPagerAdapter(getSupportFragmentManager(), pairs, AddDeviceFragment.newInstance(gate.getId()));
	}

	@Override
	public void onAddSensor(boolean success) {
		setResult(success ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
		if (success) {
			finish();
		}
	}

	@Override
	protected int getLastPageNextTextResource() {
		return R.string.addsensor_send_pair;
	}
}