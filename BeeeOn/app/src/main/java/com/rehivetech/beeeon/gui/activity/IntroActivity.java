package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.adapter.IntroFragmentPagerAdapter;
import com.rehivetech.beeeon.gui.fragment.IntroImageFragment;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class IntroActivity extends AppCompatActivity {
	private static final String TAG = IntroActivity.class.getSimpleName();
	@Bind(R.id.base_guide_intro_pager) ViewPager mPager;
	@Bind(R.id.base_guide_intro_indicator) CirclePageIndicator mIndicator;

	@Bind(R.id.base_guide_add_gate_skip_button) Button mButtonSkip;
	@Bind(R.id.base_guide_add_gate_cancel_button) Button mButtonCancel;
	@Bind(R.id.base_guide_add_gate_next_button) Button mButtonNext;
	private IntroFragmentPagerAdapter mPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_base_guide);
		ButterKnife.bind(this);

		List<IntroImageFragment.ImageTextPair> pairs = new ArrayList<>();
		pairs.add(new IntroImageFragment.ImageTextPair(R.drawable.beeeon_logo_white_border, R.string.intro_tut_intro_text_1, R.string.intro_tut_intro_title_1));
		pairs.add(new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_intro_2, R.string.intro_tut_intro_text_2, R.string.intro_tut_intro_title_2));
		pairs.add(new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_intro_3, R.string.intro_tut_intro_text_3, R.string.intro_tut_intro_title_3));
		pairs.add(new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_intro_4, R.string.intro_tut_intro_text_4, R.string.intro_tut_intro_title_4));
		pairs.add(new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_intro_5, R.string.intro_tut_intro_text_5, R.string.intro_tut_intro_title_5));

		mPagerAdapter = new IntroFragmentPagerAdapter(getSupportFragmentManager(), pairs, null);
		mPager.setAdapter(mPagerAdapter);

		initLayout();
	}

	private void initLayout() {
		mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			boolean lastPageChange = false;
			boolean wasCalledFinish = false;

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				int lastIndex = mPagerAdapter.getCount() - 1;
				if (!wasCalledFinish && lastPageChange && position == lastIndex) {
					finishWithAnimation();
					wasCalledFinish = true;
				}
			}

			@Override
			public void onPageSelected(int position) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				int currentItem = mPager.getCurrentItem();
				int lastIndex = mPagerAdapter.getCount() - 1;
				lastPageChange = currentItem == lastIndex && state == ViewPager.SCROLL_STATE_DRAGGING;
			}
		});

		mIndicator.setViewPager(mPager);
		mIndicator.setPageColor(0x88FFFFFF);
		mIndicator.setFillColor(0xFFFFFFFF);
		mIndicator.setStrokeColor(0x88FFFFFF);

		mButtonSkip.setVisibility(View.INVISIBLE);
		mButtonCancel.setVisibility(View.INVISIBLE);
	}

	@OnClick(R.id.base_guide_add_gate_next_button)
	public void onClickButtonNext() {
		if (mPager.getCurrentItem() != (mPager.getAdapter().getCount() - 1)) {
			mPager.setCurrentItem(mPager.getCurrentItem() + 1);
		} else {
			finishWithAnimation();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finishWithAnimation();
	}

	private void finishWithAnimation() {
		finish();
		overridePendingTransition(R.anim.right_in, R.anim.right_out);
	}

	public boolean isLastFragment() {
		return (mPager.getCurrentItem() == (mPager.getAdapter().getCount() - 1));
	}

	public void resetBtn() {
		mButtonNext.setText(this.getString(R.string.intro_base_guide_btn_next));
	}

	public void setLastFragmentBtn() {
		mButtonNext.setText(this.getString(R.string.intro_tut_btn_start_app));
	}
}
