package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.adapter.IntroFragmentAdapter;
import com.viewpagerindicator.CirclePageIndicator;

public class IntroActivity extends BaseActivity {
	private IntroFragmentAdapter mAdapter;
	private ViewPager mPager;
	private CirclePageIndicator mIndicator;
	private Button mSkip;
	private Button mCancel;
	private Button mNext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);

		mAdapter = new IntroFragmentAdapter(getSupportFragmentManager(), this);

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
