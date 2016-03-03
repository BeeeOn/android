package com.rehivetech.beeeon.gui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.IntroFragmentPagerAdapter;
import com.rehivetech.beeeon.gui.fragment.IntroImageFragment;

import java.util.Arrays;
import java.util.List;

public class AddGateActivity extends BaseGuideActivity {
	GateScannerFragment mFragment;

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

		mFragment = new GateScannerFragment();
		return new IntroFragmentPagerAdapter(getSupportFragmentManager(), pairs, mFragment);
	}

	@Override
	protected void onCreate(Bundle savedInstanceData) {
		super.onCreate(savedInstanceData);

		mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				if(position == (mPagerAdapter.getCount() - 1)){
					mFragment.startCamera();
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
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

//	@Override
//	protected void onPageActionNext(int pagePosition) {
//		super.onPageActionNext(pagePosition);
//
//		Log.d("XXXXXXX", String.valueOf(pagePosition));
//	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
}
