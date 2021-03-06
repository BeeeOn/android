package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.adapter.IntroFragmentPagerAdapter;
import com.rehivetech.beeeon.gui.fragment.IntroImageFragment;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class IntroActivity extends AppCompatActivity {
	@BindView(R.id.base_guide_intro_pager) ViewPager mPager;
	@BindView(R.id.base_guide_intro_indicator) CirclePageIndicator mIndicator;

	@BindView(R.id.base_guide_add_gate_skip_button) Button mButtonSkip;
	@BindView(R.id.base_guide_add_gate_cancel_button) Button mButtonCancel;
	@BindView(R.id.base_guide_add_gate_next_button) Button mButtonNext;
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
				lastPageChange = isLastFragment() && state == ViewPager.SCROLL_STATE_DRAGGING;
			}
		});

		mIndicator.setViewPager(mPager);
		mIndicator.setPageColor(ContextCompat.getColor(this, R.color.beeeon_white_transparent));
		mIndicator.setFillColor(ContextCompat.getColor(this, R.color.white));
		mIndicator.setStrokeColor(ContextCompat.getColor(this, R.color.beeeon_white_transparent));

		mButtonSkip.setVisibility(View.INVISIBLE);
		mButtonCancel.setVisibility(View.INVISIBLE);
	}

	@OnClick(R.id.base_guide_add_gate_next_button)
	public void onClickButtonNext() {
		if (!isLastFragment()) {
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

	/**
	 * Finishes this activity with transition (swipe right) -> to LoginActivity
	 */
	private void finishWithAnimation() {
		finish();
		overridePendingTransition(R.anim.right_in, R.anim.right_out);
	}

	/**
	 * Checks if actual fragment is the last one
	 *
	 * @return if is last
	 */
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
