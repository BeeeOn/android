package com.rehivetech.beeeon.gui.activity;

import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.ImageTextPair;
import com.rehivetech.beeeon.gui.adapter.IntroFragmentPagerAdapter;
import com.rehivetech.beeeon.gui.fragment.AddGateFragment;

import java.util.Arrays;
import java.util.List;

public class AddGateActivity extends BaseGuideActivity implements AddGateFragment.OnAddGateListener {

	@Override
	public void onCodeScanned() {
		// automatically clicks the next button
		onLastFragmentActionNext();
	}

	@Override
	protected void onLastFragmentActionNext() {
		AddGateFragment fragment = (AddGateFragment) mPagerAdapter.getFinalFragment();
		if (fragment == null) {
			return;
		}
		fragment.doAction();
	}

	@Override
	protected IntroFragmentPagerAdapter initPagerAdapter() {
		// creating list of objects that will be used as params for the constructor of AddingUniversalFragment
		List<ImageTextPair> pairs = Arrays.asList(
				new ImageTextPair(R.drawable.beeeon_tutorial_aa_second_step,getResources().getString(R.string.tut_add_gate_text_1)),
				new ImageTextPair(R.drawable.beeeon_tutorial_aa_first_step, getResources().getString(R.string.tut_add_gate_text_2)),
				new ImageTextPair(R.drawable.beeeon_tutorial_aa_third_step, getResources().getString(R.string.tut_add_gate_text_3))
		);
		// FragmentManager object is necessary for the contructor of Adding....
		FragmentManager fm = getSupportFragmentManager();
		return new IntroFragmentPagerAdapter(fm,pairs,new AddGateFragment());
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
