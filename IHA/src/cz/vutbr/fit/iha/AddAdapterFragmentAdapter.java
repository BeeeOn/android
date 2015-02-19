package cz.vutbr.fit.iha;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.viewpagerindicator.IconPagerAdapter;

import cz.vutbr.fit.iha.activity.fragment.AddAdapterFragment;
import cz.vutbr.fit.iha.activity.fragment.IntroImageFragment;

public class AddAdapterFragmentAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
    protected static final String[] CONTENT = new String[] { "Welcome", "to", "IHA", "Test", };
    protected static final int[] ICONS = new int[] {
            R.drawable.loc_bath_room,
            R.drawable.loc_garden,
            R.drawable.loc_wc,
            R.drawable.loc_dinner_room
    };

    private int mCount = 4;

    public AddAdapterFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
    	switch (position) {
    	case 1:
    		return IntroImageFragment.newInstance(R.drawable.iha_tutorial_aa_first_step,"Please connect your adapter to the AC power");
    	case 0:
    		return IntroImageFragment.newInstance(R.drawable.iha_tutorial_aa_second_step,"Please connect your adapter to the Internet");
    	case 2:
    		return IntroImageFragment.newInstance(R.drawable.iha_tutorial_aa_third_step,"In the next step, please give name to your adapter and scan QR code ");
    	case 3:
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
      return AddAdapterFragmentAdapter.CONTENT[position % CONTENT.length];
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