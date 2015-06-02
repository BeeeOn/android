package com.rehivetech.beeeon.gui.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.rehivetech.beeeon.gui.fragment.AddGateFragment;
import com.rehivetech.beeeon.gui.fragment.IntroImageFragment;
import com.viewpagerindicator.IconPagerAdapter;

import java.util.List;

/**
 * Created by david on 2.6.15.
 */
public class AddingUniversalFragment extends FragmentPagerAdapter implements IconPagerAdapter {
	private int PAGES_COUNT;
//	private final Context mActivity;

	private final List<ImageTextPair> mPairs;
	private final android.support.v4.app.Fragment mLastPageFragment;

	private AddGateFragment mAddGateFragment;

	public AddingUniversalFragment(FragmentManager fm, List<ImageTextPair> pairs, android.support.v4.app.Fragment lastPageFragment) {
		super(fm);
		this.PAGES_COUNT = pairs.size();
		if (lastPageFragment == null) this.PAGES_COUNT -= 1;

		this.mLastPageFragment = lastPageFragment;
		this.mPairs = pairs;
	}

	@Override
	public android.support.v4.app.Fragment getItem(int position) {
		switch (position) {
			case 0:
				IntroImageFragment.newInstance(mPairs.get(0).imageRes,mPairs.get(0).text);
			case 1:
				IntroImageFragment.newInstance(mPairs.get(1).imageRes,mPairs.get(1).text);
			case 2:
				if (mLastPageFragment != null && PAGES_COUNT == 2) {
					return mLastPageFragment;
				} else {
					IntroImageFragment.newInstance(mPairs.get(2).imageRes, mPairs.get(2).text);
				}
			case 3:
				if (mLastPageFragment != null && PAGES_COUNT == 3) {
					return mLastPageFragment;
				} else {
					IntroImageFragment.newInstance(mPairs.get(3).imageRes, mPairs.get(3).text);
				}
			case 4:
				if (mLastPageFragment != null && PAGES_COUNT == 4) {
					return mLastPageFragment;
				} else {
					IntroImageFragment.newInstance(mPairs.get(4).imageRes, mPairs.get(4).text);
				}
		}
		return null;
	}

/*
	public IntroFragmentAdapter (FragmentManager fm, Context context) {
		super(fm);
		mActivity = context;
	}
*/

	@Override
	public int getIconResId(int i) {
		return 0;
	}

	@Override
	public int getCount() {
		return PAGES_COUNT;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		android.support.v4.app.Fragment fragment = (android.support.v4.app.Fragment) super.instantiateItem(container, position);
		if (position == PAGES_COUNT - 1) {
			mAddGateFragment = (AddGateFragment) fragment;
		}
		return fragment;
	}

	public AddGateFragment getAddGateFragment() {
		return mAddGateFragment;
	}

}
