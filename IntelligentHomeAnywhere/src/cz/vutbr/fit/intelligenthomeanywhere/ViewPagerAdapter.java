package cz.vutbr.fit.intelligenthomeanywhere;

import cz.vutbr.fit.intelligenthomeanywhere.activity.GraphOfSensors;
import cz.vutbr.fit.intelligenthomeanywhere.activity.ListOfSensors;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

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