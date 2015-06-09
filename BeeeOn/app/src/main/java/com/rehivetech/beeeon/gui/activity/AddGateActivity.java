package com.rehivetech.beeeon.gui.activity;

import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.IntroFragmentPagerAdapter;
import com.rehivetech.beeeon.gui.fragment.AddGateFragment;
import com.rehivetech.beeeon.gui.fragment.IntroImageFragment;
import com.rehivetech.beeeon.util.Log;

import java.util.Arrays;
import java.util.List;

public class AddGateActivity extends BaseGuideActivity implements AddGateFragment.OnAddGateListener {

	private static final String TAG = AddGateActivity.class.getSimpleName();

	@Override
	public void onCodeScanned() {
		// automatically clicks the next button
		onLastFragmentActionNext();
	}

	@Override
	protected void onLastFragmentActionNext() {
		AddGateFragment fragment = (AddGateFragment) mPagerAdapter.getFinalFragment();
		if (fragment == null) {
			Log.e(TAG, "AddGateActivity.onLastFragmentActionNext() return null fragment");
			return;
		}
		fragment.doAction();
	}

	@Override
	protected IntroFragmentPagerAdapter initPagerAdapter() {
		// creating list of objects that will be used as params for the constructor of AddingUniversalFragment
		List<IntroImageFragment.ImageTextPair> pairs = Arrays.asList(
				new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_aa_first_step, R.string.tut_add_gate_text_1, R.string.addadapter_title),
				new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_aa_second_step, R.string.tut_add_gate_text_2, R.string.addadapter_title),
				new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_aa_third_step, R.string.tut_add_gate_text_3, R.string.addadapter_title)
		);
		return new IntroFragmentPagerAdapter(getSupportFragmentManager(),pairs,new AddGateFragment());
	}

	@Override
	protected void closeActivity() {
		SharedPreferences prefs = Controller.getInstance(this).getUserSettings();
		if (prefs != null) {
			prefs.edit().putBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_GATE, true).apply();
		}
		super.closeActivity();
	}

	@Override
	protected int getLastPageNextTextResource() {
		return R.string.tutorial_add;
	}
}
