package com.rehivetech.beeeon.gui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.konifar.fab_transformation.FabTransformation;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.fragment.ModuleGraphFragment;
import com.rehivetech.beeeon.gui.view.Slider;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.util.ChartHelper;
import com.rehivetech.beeeon.util.UnitsHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

		Slider slider = (Slider) findViewById(R.id.module_graph_slider);

		Map<ModuleLog.DataInterval, String> intervals = getIntervalString(ModuleLog.DataInterval.values());

		slider.setValues(new ArrayList<>(intervals.values()));

		final FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.module_graph_fab);
		final View transformView = findViewById(R.id.module_graph_footer);
		floatingActionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FabTransformation.Builder builder = FabTransformation.with(floatingActionButton);

				builder.setListener(new FabTransformation.OnTransformListener() {
					@Override
					public void onStartTransform() {

					}

					@Override
					public void onEndTransform() {
						transformView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
					}
				});

				builder.transformTo(transformView);

			}
		});

		Button buttonDone = (Button) findViewById(R.id.module_graph_button_done);
		buttonDone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FabTransformation.Builder builder = FabTransformation.with(floatingActionButton);

				builder.setListener(new FabTransformation.OnTransformListener() {
					@Override
					public void onStartTransform() {
						transformView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.beeeon_accent));

					}

					@Override
					public void onEndTransform() {
					}
				});

				builder.transformFrom(transformView);

			}
		});
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
			adapter.addFragment(fragment, getString(ChartHelper.getIntervalString(dataRange)));
		}

		mViewPager.setAdapter(adapter);
		mTabLayout.setupWithViewPager(mViewPager);
	}

	private Map<ModuleLog.DataInterval, String> getIntervalString(ModuleLog.DataInterval[] intervals) {
		Map<ModuleLog.DataInterval, String> intervalStringMap =new LinkedHashMap<>();

		for (ModuleLog.DataInterval interval : intervals) {

			switch (interval) {
				case RAW:
					intervalStringMap.put(interval, getString(R.string.data_interval_raw));
					break;
				case MINUTE:
					intervalStringMap.put(interval, getString(R.string.data_interval_minute));
					break;
				case FIVE_MINUTES:
					intervalStringMap.put(interval, getString(R.string.data_interval_five_minutes));
					break;
				case TEN_MINUTES:
					intervalStringMap.put(interval, getString(R.string.data_interval_ten_minutes));
					break;
				case HALF_HOUR:
					intervalStringMap.put(interval, getString(R.string.data_interval_half_hour));
					break;
				case HOUR:
					intervalStringMap.put(interval, getString(R.string.data_interval_hour));
					break;
				case DAY:
					intervalStringMap.put(interval, getString(R.string.data_interval_day));
					break;
				case WEEK:
					intervalStringMap.put(interval, getString(R.string.data_interval_week));
					break;
				case MONTH:
					intervalStringMap.put(interval, getString(R.string.data_interval_month));
					break;
			}
		}
		return intervalStringMap;
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


}
