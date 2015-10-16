package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
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
	private ViewPager mPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_base_guide);

		List<IntroImageFragment.ImageTextPair> pairs = new ArrayList<>();
		pairs.add(new IntroImageFragment.ImageTextPair(R.drawable.beeeon_logo_white_border,R.string.intro_tut_intro_text_1, R.string.intro_tut_intro_title));
		pairs.add(new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_intro_2,R.string.intro_tut_intro_text_2, R.string.intro_tut_intro_title));
		pairs.add(new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_intro_3,R.string.intro_tut_intro_text_3, R.string.intro_tut_intro_title));
		pairs.add(new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_intro_4, R.string.intro_tut_intro_text_4, R.string.intro_tut_intro_title));
		pairs.add(new IntroImageFragment.ImageTextPair(R.drawable.beeeon_tutorial_intro_5, R.string.intro_tut_intro_text_5, R.string.intro_tut_intro_title));

		IntroFragmentPagerAdapter adapter = new IntroFragmentPagerAdapter(getSupportFragmentManager(),pairs,null);

		mPager = (ViewPager) findViewById(R.id.base_guide_intro_pager);
		mPager.setAdapter(adapter);

		CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.base_guide_intro_indicator);
		indicator.setViewPager(mPager);

		indicator.setPageColor(0x88FFFFFF);
		indicator.setFillColor(0xFFFFFFFF);
		indicator.setStrokeColor(0x88FFFFFF);

		initLayout();
	}

	private void initLayout() {
		// Get buttons
		Button skipBtn = (Button) findViewById(R.id.base_guide_add_gate_skip_button);
		Button cancelBtn = (Button) findViewById(R.id.base_guide_add_gate_cancel_button);
		Button nextBtn = (Button) findViewById(R.id.base_guide_add_gate_next_button);

		skipBtn.setVisibility(View.INVISIBLE);
		cancelBtn.setVisibility(View.INVISIBLE);

		nextBtn.setOnClickListener(new View.OnClickListener() {
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
		((Button) findViewById(R.id.base_guide_add_gate_next_button)).setText(this.getString(R.string.intro_base_guide_btn_next));
	}

	public void setLastFragmentBtn() {
		((Button) findViewById(R.id.base_guide_add_gate_next_button)).setText(this.getString(R.string.intro_tut_btn_start_app));
	}
}
