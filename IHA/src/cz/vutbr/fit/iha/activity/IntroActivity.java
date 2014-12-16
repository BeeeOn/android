package cz.vutbr.fit.iha.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.CirclePageIndicator;

import cz.vutbr.fit.iha.IntroFragmentAdapter;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.base.BaseApplicationActivity;

public class IntroActivity extends BaseApplicationActivity {
	private IntroFragmentAdapter mAdapter;
	private ViewPager mPager;
	private CirclePageIndicator mIndicator;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);
		
		mAdapter = new IntroFragmentAdapter(getSupportFragmentManager());
		
		mPager = (ViewPager)findViewById(R.id.intro_pager);
		mPager.setAdapter(mAdapter);
		
		mIndicator = (CirclePageIndicator)findViewById(R.id.intro_indicator);
		mIndicator.setViewPager(mPager);
		
		mIndicator.setPageColor(0x88000000);
		mIndicator.setFillColor(0xFF000000);
		mIndicator.setStrokeColor(0x88000000);
	}
	
	
	@Override
	protected void onAppResume() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onAppPause() {
		// TODO Auto-generated method stub

	}

}
