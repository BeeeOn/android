package com.rehivetech.beeeon.gui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.IntroFragmentPagerAdapter;
import com.rehivetech.beeeon.gui.fragment.AddGateFragment;
import com.rehivetech.beeeon.gui.fragment.IntroImageFragment;

import java.util.Arrays;
import java.util.List;

public class AddGateActivity extends BaseGuideActivity {
	@Override
	protected void onLastFragmentActionNext() {
		//there is nothing to do, but it is required to implement it
	}

	@Override
	protected IntroFragmentPagerAdapter initPagerAdapter() {
		// creating list of objects that will be used as params for the constructor of AddingUniversalFragment
		List<IntroImageFragment.ImageTextPair> pairs = Arrays.asList(
				new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_aa_first_step, R.string.gate_add_tut_add_gate_text_1, R.string.gate_add_tut_add_gate_title_1),
				new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_aa_second_step, R.string.gate_add_tut_add_gate_text_2, R.string.gate_add_tut_add_gate_title_2)
		);
		return new IntroFragmentPagerAdapter(getSupportFragmentManager(), pairs, new AddGateFragment());
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
		mNext.setVisibility(View.INVISIBLE);
		return R.string.gate_add_btn_add;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
}
