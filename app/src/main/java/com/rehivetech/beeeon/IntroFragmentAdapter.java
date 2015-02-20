package com.rehivetech.beeeon;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.viewpagerindicator.IconPagerAdapter;

import com.rehivetech.beeeon.activity.fragment.IntroImageFragment;

public class IntroFragmentAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
    protected static final String[] CONTENT = new String[] { "Welcome", "to", "IHA", "Test", };
    protected static final int[] ICONS = new int[] {
            R.drawable.loc_bath_room,
            R.drawable.loc_garden,
            R.drawable.loc_wc,
            R.drawable.loc_dinner_room
    };

    private int mCount = 4;

    public IntroFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
    	switch (position) {
    	case 0:
    		return IntroImageFragment.newInstance(R.drawable.dev_emission,"Test 1");
    	case 1:
    		return IntroImageFragment.newInstance(R.drawable.dev_state_closed,"Test 2");
    	case 2:
    		return IntroImageFragment.newInstance(R.drawable.dev_temperature,"Test 3");
    	case 3:
    		return IntroImageFragment.newInstance(R.drawable.dev_pressure,"Test 4");
    	}
		return null;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return IntroFragmentAdapter.CONTENT[position % CONTENT.length];
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