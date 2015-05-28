package com.rehivetech.beeeon;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.rehivetech.beeeon.gui.fragment.CustomViewFragment;
import com.rehivetech.beeeon.gui.fragment.SensorListFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

	// Declare the number of ViewPager pages
	final int PAGE_COUNT = 2;
	private String titles[];

	public ViewPagerAdapter(FragmentManager fm, Context context) {
		super(fm);
		titles = context.getResources().getStringArray(R.array.title_of_main_fragments);
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {

			// Open FragmentTab2.java
			case 0:
				SensorListFragment fragmenttab2 = new SensorListFragment();
				return fragmenttab2;

			// Open FragmentTab1.java
			case 1:
				CustomViewFragment fragmenttab1 = new CustomViewFragment();
				return fragmenttab1;

		}
		return null;
	}

	public CharSequence getPageTitle(int position) {
		return titles[position];
	}

	@Override
	public int getCount() {
		return PAGE_COUNT;
	}

}
