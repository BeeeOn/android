package com.rehivetech.beeeon.gui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.rehivetech.beeeon.gui.fragment.IntroImageFragment;

import java.util.List;

/**
 * Created by david on 2.6.15.
 */
public class IntroFragmentPagerAdapter extends FragmentPagerAdapter {

	private final List<IntroImageFragment.ImageTextPair> mPairs;
	private Fragment mLastFragment;
	private int mCount;

	public IntroFragmentPagerAdapter(FragmentManager fm, List<IntroImageFragment.ImageTextPair> pairs, Fragment lastFragment) {
		super(fm);
		mPairs = pairs;
		mLastFragment = lastFragment;

		// when there is no extra lastFragment, the size of mPairs == the number of fragments, otherwise, one more is needed
		mCount = (mLastFragment == null) ? mPairs.size() : mPairs.size() + 1;
	}

	@Override
	public android.support.v4.app.Fragment getItem(int position) {
		// if there is last special fragment, his position will be higher than the size of the list
		if (position < mPairs.size()) {
			IntroImageFragment.ImageTextPair pair = mPairs.get(position);
			return IntroImageFragment.newInstance(pair.imageRes, pair.textRes, pair.titleRes);
		} else
			return mLastFragment;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Fragment fragment = (Fragment) super.instantiateItem(container, position);

		if (position == getCount() - 1) {
			mLastFragment = fragment;
		}

		return fragment;
	}

	@Override
	public int getCount() {
		return mCount;
	}

	public Fragment getFinalFragment() {
		return mLastFragment;
	}
}
