package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.IntroImageFragment;
import com.viewpagerindicator.IconPagerAdapter;

public class IntroFragmentAdapter extends FragmentPagerAdapter implements IconPagerAdapter {

	private int mCount = 5;
	private final Context mActivity;

	public IntroFragmentAdapter(FragmentManager fm, Context context) {
		super(fm);
		mActivity = context;
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0:
				return IntroImageFragment.newInstance(R.drawable.beeeon_logo_white_icons, R.string.tut_intro_text_1);
			case 1:
				return IntroImageFragment.newInstance(R.drawable.beeeon_tutorial_intro_2, R.string.tut_intro_text_2);
			case 2:
				return IntroImageFragment.newInstance(R.drawable.beeeon_tutorial_intro_3, R.string.tut_intro_text_3);
			case 3:
				return IntroImageFragment.newInstance(R.drawable.beeeon_tutorial_intro_4, R.string.tut_intro_text_4);
			case 4:
				return IntroImageFragment.newInstance(R.drawable.beeeon_tutorial_intro_5, R.string.tut_intro_text_5);
			default:
				return null;

		}
	}

	@Override
	public int getIconResId(int i) {
		return 0;
	}

	@Override
	public int getCount() {
		return mCount;
	}

}