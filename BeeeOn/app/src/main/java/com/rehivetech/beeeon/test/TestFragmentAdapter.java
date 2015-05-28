package com.rehivetech.beeeon.test;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.fragment.AddAdapterFragment;
import com.viewpagerindicator.IconPagerAdapter;

public class TestFragmentAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
	protected static final String[] CONTENT = new String[]{"Welcome", "to", "BeeeOn", "Test",};
	protected static final int[] ICONS = new int[]{
			R.drawable.loc_bath_room,
			R.drawable.loc_garden,
			R.drawable.loc_wc,
			R.drawable.loc_dinner_room
	};

	private int mCount = CONTENT.length;

	public TestFragmentAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 1:

				break;
			case 2:

				break;
			case 3:

				break;
			case 4:
				return new AddAdapterFragment();
		}
		return null;
	}

	@Override
	public int getCount() {
		return mCount;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return TestFragmentAdapter.CONTENT[position % CONTENT.length];
	}

	@Override
	public int getIconResId(int index) {
		return ICONS[index % ICONS.length];
	}

	public void setCount(int count) {
		if (count > 0 && count <= 10) {
			mCount = count;
			notifyDataSetChanged();
		}
	}
}