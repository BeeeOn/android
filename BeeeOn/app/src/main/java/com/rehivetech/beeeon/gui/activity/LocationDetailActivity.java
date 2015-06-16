package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.LocationDetailFragment;

public class LocationDetailActivity extends BaseApplicationActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_detail_wraper);



		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.beeeon_logo_white);
		LocationDetailFragment fragment = new LocationDetailFragment();
		fragment.setArguments(getIntent().getExtras());
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		ft.replace(R.id.location_detail_wraper, fragment);
		ft.commit();
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