package cz.vutbr.fit.intelligenthomeanywhere.activity;

import java.lang.reflect.Field;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.ViewPagerAdapter;


public class MainFragment extends SherlockFragment{
	
	 @Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container,
	      Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.viewpager_main, container, false);
	    // Locate the ViewPager in viewpager_main.xml
	    ViewPager mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
	    // Set the ViewPagerAdapter into ViewPager
	    mViewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager()));
	    return view;
	  }

	  @Override
	  public void onDetach() {
	    super.onDetach();
	    try {
	      Field childFragmentManager = Fragment.class
	          .getDeclaredField("mChildFragmentManager");
	      childFragmentManager.setAccessible(true);
	      childFragmentManager.set(this, null);
	    } catch (NoSuchFieldException e) {
	      throw new RuntimeException(e);
	    } catch (IllegalAccessException e) {
	      throw new RuntimeException(e);
	    }
	  }
}
