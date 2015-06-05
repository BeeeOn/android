package com.rehivetech.beeeon.gui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.rehivetech.beeeon.gui.fragment.IntroImageFragment;

import java.util.List;

/**
 * Created by david on 2.6.15.
 */
public class IntroFragmentPagerAdapter extends FragmentPagerAdapter {

	private final List<IntroImageFragment.ImageTextPair> mPairs;
	private final Fragment mFragment;

	public IntroFragmentPagerAdapter(FragmentManager fm, List<IntroImageFragment.ImageTextPair> pairs, Fragment fragment) {
		super(fm);
		this.mPairs = pairs;
		this.mFragment = fragment;
	}

	@Override
	public android.support.v4.app.Fragment getItem(int position) {
		// if there is last special fragment, his position will be higher than the size of the list
		if (position < mPairs.size())
			return IntroImageFragment.newInstance(mPairs.get(position).imageRes, mPairs.get(position).textRes);
		else
			return mFragment;
	}

	@Override
	public int getCount() {
		// when there is no extra fragment, the size of mPairs == the number of fragments, otherwise, one more is needed
		if (mFragment == null)
			return mPairs.size();
		else
			return mPairs.size() + 1;
	}

	public Fragment getFinalFragment() {
		return mFragment;
	}
}
