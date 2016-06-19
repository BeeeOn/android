package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import com.rehivetech.beeeon.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Viewpager for different dashboard views
 *
 * @author mlyko
 * @since 13.06.16
 */
public class ViewPagerAdapter extends android.support.v4.app.FragmentStatePagerAdapter {
	private List<Fragment> mFragments = new ArrayList<>();
	private List<String> mFragmentTitles = new ArrayList<>();
	private FragmentManager mFragmentManager;

	public ViewPagerAdapter(FragmentManager fm) {
		super(fm);
		mFragmentManager = fm;
	}

	@Override
	public Fragment getItem(int position) {
		return mFragments.get(position);
	}

	@Override
	public int getCount() {
		return mFragments.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mFragmentTitles.get(position);
	}

	public Fragment getActiveFragment(ViewPager container) {
		String name = makeFragmentName(container.getId(), container.getCurrentItem());
		return mFragmentManager.findFragmentByTag(name);
	}

	private static String makeFragmentName(int viewId, int index) {
		return "android:switcher:" + viewId + ":" + index;
	}

	public void addFragment(Fragment fragment, String title) {
		mFragments.add(fragment);
		mFragmentTitles.add(title);
		notifyDataSetChanged();
	}

	public void removeFragment(int position) {
		mFragments.remove(position);
		mFragmentTitles.remove(position);
		notifyDataSetChanged();
	}

	public void setFragmentTitles(List<String> titles) {
		mFragmentTitles = titles;
		notifyDataSetChanged();
	}

	@Override
	public int getItemPosition(Object object) {
		int index = mFragments.indexOf((Fragment) object);

		if (index == -1)
			return POSITION_NONE;
		else
			return index;
	}
}