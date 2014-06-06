package cz.vutbr.fit.iha;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import cz.vutbr.fit.iha.activity.GraphOfSensors;
import cz.vutbr.fit.iha.activity.ListOfSensors;

public class ViewPagerAdapter extends FragmentPagerAdapter {

	 // Declare the number of ViewPager pages
	  final int PAGE_COUNT = 2;
	  private String titles[] = new String[] { "List ", "Graph" };

	  public ViewPagerAdapter(FragmentManager fm) {
	    super(fm);
	  }

	  @Override
	  public Fragment getItem(int position) {
	    switch (position) {

	    // Open FragmentTab2.java
	    case 0:
	    	ListOfSensors fragmenttab2 = new ListOfSensors();
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
