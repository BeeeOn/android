package cz.vutbr.fit.iha;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import cz.vutbr.fit.iha.activity.GraphOfSensors;
import cz.vutbr.fit.iha.activity.ListOfDevices;

public class ViewPagerAdapter extends FragmentPagerAdapter {

	// Declare the number of ViewPager pages
	final int PAGE_COUNT = 2;
	private String titles[];;

	public ViewPagerAdapter(FragmentManager fm, Context ctx) {
		super(fm);
		titles = ctx.getResources().getStringArray(R.array.title_of_main_fragments);
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {

		// Open FragmentTab2.java
		case 0:
			ListOfDevices fragmenttab2 = new ListOfDevices();
			return fragmenttab2;

			// Open FragmentTab1.java
		case 1:
			GraphOfSensors fragmenttab1 = new GraphOfSensors();
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
