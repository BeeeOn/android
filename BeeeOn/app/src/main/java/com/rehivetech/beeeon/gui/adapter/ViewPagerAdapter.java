package com.rehivetech.beeeon.gui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Viewpager for different dashboard views
 *
 * @author mlyko
 * @since 13.06.16
 */
public class ViewPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
	private final List<Fragment> mFragments = new ArrayList<>();
	private final List<String> mFragmentTitles = new ArrayList<>();
	private final FragmentManager mFragmentManager;

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

	/**
	 * Removes fragment on position
	 *
	 * @param index position to delete fragment on
	 */
	public void removeFragment(int index) {
		mFragments.remove(index);
		mFragmentTitles.remove(index);
		notifyDataSetChanged();
	}

	public void removeAll() {
		mFragments.clear();
		mFragmentTitles.clear();
		notifyDataSetChanged();
	}
}
