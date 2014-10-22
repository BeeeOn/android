package cz.vutbr.fit.iha;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import cz.vutbr.fit.iha.activity.MainActivity;
import cz.vutbr.fit.iha.activity.fragment.CustomViewFragment;
import cz.vutbr.fit.iha.activity.fragment.SensorListFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

	// Declare the number of ViewPager pages
	final int PAGE_COUNT = 2;
	private String titles[];
	private Context mCtx;

	public ViewPagerAdapter(FragmentManager fm, Context ctx) {
		super(fm);
		titles = ctx.getResources().getStringArray(R.array.title_of_main_fragments);
		mCtx = ctx;
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {

		// Open FragmentTab2.java
		case 0:
			SensorListFragment fragmenttab2 = new SensorListFragment((MainActivity) mCtx);
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
