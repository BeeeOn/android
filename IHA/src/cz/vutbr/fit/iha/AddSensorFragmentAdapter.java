package cz.vutbr.fit.iha;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.viewpagerindicator.IconPagerAdapter;

import cz.vutbr.fit.iha.activity.fragment.AddAdapterFragment;
import cz.vutbr.fit.iha.activity.fragment.AddSensorFragment;
import cz.vutbr.fit.iha.activity.fragment.IntroImageFragment;

public class AddSensorFragmentAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
    protected static final String[] CONTENT = new String[] { "Welcome", "to", "IHA", "Test", };
    protected static final int[] ICONS = new int[] {
            R.drawable.loc_bath_room,
            R.drawable.loc_garden,
            R.drawable.loc_wc,
            R.drawable.loc_dinner_room
    };

    private int mCount = 4;

    public AddSensorFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
    	switch (position) {
    	case 0:
    		// Prepare your sensor
    		return IntroImageFragment.newInstance(R.drawable.dev_emission);
    	case 1:
    		// After start timer , 
    		return IntroImageFragment.newInstance(R.drawable.dev_state_closed);
    	case 2:
    		// shake with your sensor 
    		return IntroImageFragment.newInstance(R.drawable.dev_temperature);
    	case 3:
    		// circle timer 
    		return new AddSensorFragment();
    	}
		return null;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return AddSensorFragmentAdapter.CONTENT[position % CONTENT.length];
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