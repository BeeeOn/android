package com.rehivetech.beeeon.gui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.fragment.ModuleGraphFragment;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.util.ChartHelper;
import com.rehivetech.beeeon.util.UnitsHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author martin on 18.8.2015.
 */
public class ModuleGraphActivity extends BaseApplicationActivity {
	private final static String TAG = ModuleGraphActivity.class.getSimpleName();

	public static final String EXTRA_GATE_ID = "gate_id";
	public static final String EXTRA_DEVICE_ID = "device_id";
	public static final String EXTRA_MODULE_ID = "module_id";

	private String mGateId;
	private String mDeviceId;
	private String mModuleId;

	private TabLayout mTabLayout;
	private ViewPager mViewPager;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_module_graph);

		Bundle bundle = getIntent().getExtras();

		if (bundle != null) {
			mGateId = bundle.getString(EXTRA_GATE_ID);
			mDeviceId = bundle.getString(EXTRA_DEVICE_ID);
			mModuleId = bundle.getString(EXTRA_MODULE_ID);
		}


		if (mGateId == null || mModuleId == null) {
			Toast.makeText(this, R.string.module_detail_toast_not_specified_gate_or_module, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		Controller controller = Controller.getInstance(this);
		Module module = controller.getDevicesModel().getDevice(mGateId, mDeviceId).getModuleById(mModuleId);

		SharedPreferences prefs = controller.getUserSettings();
		UnitsHelper unitsHelper = new UnitsHelper(prefs, this);

		String moduleUnit = unitsHelper.getStringUnit(module.getValue());
		
		if (moduleUnit.length() > 0) {
			moduleUnit = String.format("[%s]", moduleUnit);
		}

		String toolbarTitle = String.format("%s %s",module.getName(this), moduleUnit);

		Toolbar toolbar = (Toolbar) findViewById(R.id.beeeon_toolbar);
		toolbar.setTitle(toolbarTitle);
		setSupportActionBar(toolbar);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(true);
		}

		mTabLayout = (TabLayout) findViewById(R.id.module_graph_tab_layoout);
		mViewPager = (ViewPager) findViewById(R.id.module_graph_view_pager);

		setupViewPager();
//		ModuleGraphFragment moduleGraphFragment = ModuleGraphFragment.newInstance(mGateId, mDeviceId, mModuleId);
//		getSupportFragmentManager().beginTransaction().replace(R.id.module_graph_container, moduleGraphFragment).commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
		}
		return false;
	}

	private void setupViewPager() {
		GraphPagerAdapter adapter = new GraphPagerAdapter(getSupportFragmentManager());


		for (int dataRange : ChartHelper.ALL_RANGES) {
			ModuleGraphFragment fragment = ModuleGraphFragment.newInstance(mGateId, mDeviceId, mModuleId, dataRange);
			adapter.addFragment(fragment, getString(getIntervalString(dataRange)));
		}

		mViewPager.setAdapter(adapter);
		mTabLayout.setupWithViewPager(mViewPager);
	}

	private class GraphPagerAdapter extends FragmentPagerAdapter {

		private final List<Fragment> mFragments = new ArrayList<>();
		private final List<String> mFragmentTitles = new ArrayList<>();

		public GraphPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mFragmentTitles.get(position);
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

		public void addFragment(Fragment fragment, String title) {
			mFragments.add(fragment);
			mFragmentTitles.add(title);
		}
	}

	private
	@StringRes
	int getIntervalString(@ChartHelper.DataRange int interval) {
		switch (interval) {
			case ChartHelper.RANGE_HOUR:
				return R.string.graph_range_hour;
			case ChartHelper.RANGE_DAY:
				return R.string.graph_range_day;
			case ChartHelper.RANGE_WEEK:
				return R.string.graph_range_week;
			case ChartHelper.RANGE_MONTH:
				return R.string.graph_range_month;
		}
		return -1;
	}
}
