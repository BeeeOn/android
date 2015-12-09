package com.rehivetech.beeeon.gui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.adapter.IntroFragmentPagerAdapter;
import com.viewpagerindicator.CirclePageIndicator;

/**
 * Class that provides the common features for AddDeviceAcitivity and AddGateActivity, both Activities inherit from this one
 */
public abstract class BaseGuideActivity extends BaseApplicationActivity{
	protected Button mSkip;
	protected Button Cancel;
	protected Button mNext;

	protected IntroFragmentPagerAdapter mPagerAdapter;
	protected ViewPager mPager;
	protected CirclePageIndicator mIndicator;

	@Override
	protected void onCreate(Bundle savedInstanceData) {
		super.onCreate(savedInstanceData);

		mPagerAdapter = initPagerAdapter();
		if (mPagerAdapter == null) {
			finish();
			return;
		}

		setContentView(R.layout.activity_base_guide);

		mPager = (ViewPager) findViewById(R.id.base_guide_intro_pager);
		mPager.setAdapter(mPagerAdapter);
		mPager.setOffscreenPageLimit(mPagerAdapter.getCount());

		mIndicator = (CirclePageIndicator) findViewById(R.id.base_guide_intro_indicator);
		mIndicator.setViewPager(mPager);

		mIndicator.setPageColor(0x88FFFFFF);
		mIndicator.setFillColor(0xFFFFFFFF);
		mIndicator.setStrokeColor(0x88FFFFFF);
		mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				if (position == mPagerAdapter.getCount() - 1) {
					mSkip.setVisibility(View.INVISIBLE);
					mNext.setText(getLastPageNextTextResource());
				} else {
					mSkip.setVisibility(View.VISIBLE);
					mNext.setVisibility(View.VISIBLE);
					mNext.setText(R.string.intro_base_guide_btn_next);
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		initButtons();
	}

	protected void initButtons() {
		mSkip = (Button) findViewById(R.id.base_guide_add_gate_skip_button);
		Cancel = (Button) findViewById(R.id.base_guide_add_gate_cancel_button);
		mNext = (Button) findViewById(R.id.base_guide_add_gate_next_button);

		mSkip.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mPager.setCurrentItem(mPagerAdapter.getCount());
			}
		});


		Cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				closeActivity();
			}
		});

		mNext.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mPager.getCurrentItem() == mPagerAdapter.getCount() - 1) {
					onLastFragmentActionNext();
				} else {
					mPager.setCurrentItem(mPager.getCurrentItem() + 1);
				}
			}
		});
	}

	protected void closeActivity() {
		setResult(Activity.RESULT_CANCELED);
		finish();
	}

	protected abstract void onLastFragmentActionNext();

	protected abstract IntroFragmentPagerAdapter initPagerAdapter();

	protected abstract int getLastPageNextTextResource();
}
