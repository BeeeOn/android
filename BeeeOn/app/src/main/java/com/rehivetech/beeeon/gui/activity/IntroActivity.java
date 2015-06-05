package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.adapter.IntroFragmentPagerAdapter;
import com.rehivetech.beeeon.gui.fragment.IntroImageFragment;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends BaseActivity {
	private IntroFragmentPagerAdapter mAdapter;
	private ViewPager mPager;
	private CirclePageIndicator mIndicator;
	private Button mSkip;
	private Button mCancel;
	private Button mNext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);

		List<IntroImageFragment.ImageTextPair> pairs = new ArrayList<>();
		pairs.add(new IntroImageFragment.ImageTextPair(R.drawable.beeeon_logo_white_icons,R.string.tut_intro_text_1));
		pairs.add(new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_intro_2,R.string.tut_intro_text_2));
		pairs.add(new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_intro_3,R.string.tut_intro_text_3));
		pairs.add(new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_intro_4,R.string.tut_intro_text_4));
		pairs.add(new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_intro_5,R.string.tut_intro_text_5));

		FragmentManager fm = getSupportFragmentManager();
		mAdapter = new IntroFragmentPagerAdapter(fm,pairs,null);

		mPager = (ViewPager) findViewById(R.id.intro_pager);
		mPager.setAdapter(mAdapter);

		mIndicator = (CirclePageIndicator) findViewById(R.id.intro_indicator);
		mIndicator.setViewPager(mPager);

		mIndicator.setPageColor(0x88FFFFFF);
		mIndicator.setFillColor(0xFFFFFFFF);
		mIndicator.setStrokeColor(0x88FFFFFF);

		initLayout();
	}

	private void initLayout() {
		// Get buttons
		mSkip = (Button) findViewById(R.id.add_gate_skip);
		mCancel = (Button) findViewById(R.id.add_gate_cancel);
		mNext = (Button) findViewById(R.id.add_gate_next);

		mSkip.setVisibility(View.INVISIBLE);
		mCancel.setVisibility(View.INVISIBLE);

		mNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPager.getCurrentItem() != (mPager.getAdapter().getCount() - 1)) {
					mPager.setCurrentItem(mPager.getCurrentItem() + 1);
				} else {
					finish();
				}

			}
		});
	}

	public boolean isLastFragment() {
		return (mPager.getCurrentItem() == (mPager.getAdapter().getCount() - 1));
	}

	public void resetBtn() {
		mNext.setText(this.getString(R.string.tutorial_next));
	}

	public void setLastFragmentBtn() {
		mNext.setText(this.getString(R.string.tutorial_go_to_app));
	}
}
