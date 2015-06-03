package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.IntroFragmentPagerAdapter;
import com.viewpagerindicator.CirclePageIndicator;

/**
 * Class that provides the common features for AddSensorAcitivity and AddGateActivity, both Activities inherit from this one
 */
public abstract class AddGadgetActivity extends BaseApplicationActivity{
	protected Button mSkip;
	protected Button Cancel;
	protected Button mNext;

	protected Controller mController;
	protected IntroFragmentPagerAdapter mPagerAdapter;
	protected ViewPager mPager;
	protected CirclePageIndicator mIndicator;
	protected Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceData) {
		super.onCreate(savedInstanceData);
		setContentView(R.layout.activity_intro);

		mPager = (ViewPager) findViewById(R.id.intro_pager);
		mPager.setAdapter(mPagerAdapter);
		mPager.setOffscreenPageLimit(mPagerAdapter.getCount());

		mIndicator = (CirclePageIndicator) findViewById(R.id.intro_indicator);
		mIndicator.setViewPager(mPager);

		mIndicator.setPageColor(0x88FFFFFF);
		mIndicator.setFillColor(0xFFFFFFFF);
		mIndicator.setStrokeColor(0x88FFFFFF);
	}

	protected void initButtons() {
		mSkip = (Button) findViewById(R.id.add_gate_skip);
		Cancel = (Button) findViewById(R.id.add_gate_cancel);
		mNext = (Button) findViewById(R.id.add_gate_next);

		mSkip.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mPager.setCurrentItem(mPagerAdapter.getCount());
			}
		});
	}
}
