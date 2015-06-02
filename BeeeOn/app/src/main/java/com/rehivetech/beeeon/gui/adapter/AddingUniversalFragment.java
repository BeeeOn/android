package com.rehivetech.beeeon.gui.adapter;

import android.nfc.Tag;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.rehivetech.beeeon.gui.fragment.IntroImageFragment;

import java.util.List;

/**
 * Created by david on 2.6.15.
 */
public class AddingUniversalFragment extends FragmentPagerAdapter {

	private final List<ImageTextPair> mPairs;
	private final Fragment mFragment;

	public AddingUniversalFragment(FragmentManager fm, List<ImageTextPair> pairs,Fragment fragment) {
		super(fm);
		this.mPairs = pairs;
		this.mFragment = fragment;
	}

	@Override
	public android.support.v4.app.Fragment getItem(int position) {
		if (position < mPairs.size())
			return IntroImageFragment.newInstance(mPairs.get(position).imageRes,mPairs.get(position).text);
		else
			return mFragment;
	}

	@Override
	public int getCount() {
		return mPairs.size() + 1;
	}

	public Fragment getFinalFragment() {
		return mFragment;
	}
}
