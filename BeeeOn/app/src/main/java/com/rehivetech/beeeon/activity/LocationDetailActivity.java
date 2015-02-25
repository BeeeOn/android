package com.rehivetech.beeeon.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.view.MenuItem;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.fragment.LocationDetailFragment;
import com.rehivetech.beeeon.base.BaseApplicationActivity;

public class LocationDetailActivity extends BaseApplicationActivity {
	private ViewPager mPager;
	private PagerAdapter mPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_detail_wraper);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher_white);
		LocationDetailFragment fragment = new LocationDetailFragment();
		fragment.setArguments(getIntent().getExtras());
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		ft.replace(R.id.location_detail_wraper, fragment);
		ft.commit();
	}

	@Override
	protected void onAppResume() {
	}

	@Override
	protected void onAppPause() {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return false;
	}

}