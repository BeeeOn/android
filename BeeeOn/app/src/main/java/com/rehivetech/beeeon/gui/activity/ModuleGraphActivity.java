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
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.rehivetech.beeeon.util.Utils;

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

	private static final String OUT_STATE_CHECK_BOX_MIN = "check_box_min";
	private static final String OUT_STATE_CHECK_BOX_MAX = "check_box_max";
	private static final String OUT_STET_CHECK_BOX_AVG = "check_box_avg";
	private static final String OUT_STATE_SLIDER_PROGRESS = "slider_progress";

	private String mGateId;
	private String mDeviceId;
	private String mModuleId;

	private TextView mMinValue;
	private TextView mMaxValue;
	private TextView mActValue;

	private TabLayout mTabLayout;
	private ViewPager mViewPager;

	private Slider mSlider;
	private AppCompatCheckBox mCheckBoxMin;
	private AppCompatCheckBox mCheckBoxAvg;
	private AppCompatCheckBox mCheckBoxMax;

	private Button mButtonCancel;
	private Button mButtonDone;
	private FloatingActionButton mFab;

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

		mMinValue = (TextView) findViewById(R.id.module_graph_min_value);
		mMaxValue = (TextView) findViewById(R.id.module_graph_max_value);
		mActValue = (TextView) findViewById(R.id.module_graph_act_value);

		mTabLayout = (TabLayout) findViewById(R.id.module_graph_tab_layoout);
		mViewPager = (ViewPager) findViewById(R.id.module_graph_view_pager);

		mSlider = (Slider) findViewById(R.id.module_graph_slider);

		mCheckBoxMin = (AppCompatCheckBox) findViewById(R.id.module_graph_checkbox_min);
		mCheckBoxAvg = (AppCompatCheckBox) findViewById(R.id.module_graph_checkbox_avg);
		mCheckBoxMax = (AppCompatCheckBox) findViewById(R.id.module_graph_checkbox_max);

		((TextView) findViewById(R.id.module_graph_text_min)).setTextColor(Utils.getGraphColor(this, 1));
		((TextView) findViewById(R.id.module_graph_text_avg)).setTextColor(Utils.getGraphColor(this, 0));
		((TextView) findViewById(R.id.module_graph_text_max)).setTextColor(Utils.getGraphColor(this, 2));

		mFab = (FloatingActionButton) findViewById(R.id.module_graph_fab);
		mButtonCancel = (Button) findViewById(R.id.module_graph_button_cancel);
		mButtonDone = (Button) findViewById(R.id.module_graph_button_done);

		setupViewPager();

		Map<ModuleLog.DataInterval, String> intervals = getIntervalString(ModuleLog.DataInterval.values());

		mSlider.setValues(new ArrayList<>(intervals.values()));
		mSlider.setProgress(2);  // default dataInterval 5 minutes

		final View transformView = findViewById(R.id.module_graph_footer);
		mFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FabTransformation.Builder builder = FabTransformation.with(mFab);

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

		mButtonCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FabTransformation.Builder builder = FabTransformation.with(mFab);

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

		mButtonDone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FabTransformation.Builder builder = FabTransformation.with(mFab);

				builder.setListener(new FabTransformation.OnTransformListener() {
					@Override
					public void onStartTransform() {
						transformView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.beeeon_accent));

					}

					@Override
					public void onEndTransform() {
						redrawActiveFragment();
					}
				});

				builder.transformFrom(transformView);

			}
		});


		if (savedInstanceState != null) {
			mCheckBoxMin.setChecked(savedInstanceState.getBoolean(OUT_STATE_CHECK_BOX_MIN));
			mCheckBoxAvg.setChecked(savedInstanceState.getBoolean(OUT_STET_CHECK_BOX_AVG));
			mCheckBoxMax.setChecked(savedInstanceState.getBoolean(OUT_STATE_CHECK_BOX_MAX));

			mSlider.setProgress(savedInstanceState.getInt(OUT_STATE_SLIDER_PROGRESS));
		} else {
			mCheckBoxAvg.setChecked(true);
		}

		updateActValue();
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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(OUT_STATE_CHECK_BOX_MIN, mCheckBoxMin.isChecked());
		outState.putBoolean(OUT_STET_CHECK_BOX_AVG, mCheckBoxAvg.isChecked());
		outState.putBoolean(OUT_STATE_CHECK_BOX_MAX, mCheckBoxMax.isChecked());
		outState.putInt(OUT_STATE_SLIDER_PROGRESS, mSlider.getProgress());
	}

	private void setupViewPager() {
		GraphPagerAdapter adapter = new GraphPagerAdapter(getSupportFragmentManager());


		for (int dataRange : ChartHelper.ALL_RANGES) {
			ModuleGraphFragment fragment = ModuleGraphFragment.newInstance(mGateId, mDeviceId, mModuleId, dataRange);
			adapter.addFragment(fragment, getString(ChartHelper.getIntervalString(dataRange)));
		}

		mViewPager.setAdapter(adapter);
		mTabLayout.setupWithViewPager(mViewPager);

		mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				redrawActiveFragment();
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
	}

	private void updateActValue() {
		Double actValue = Controller.getInstance(this).getDevicesModel().getDevice(mGateId, mDeviceId).getModuleById(mModuleId).getValue().getDoubleValue();
		mActValue.setText(String.format("%.2f", actValue));
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

	private ModuleLog.DataInterval getIntervalBySliderProgress() {

		switch (mSlider.getProgress()) {
			case 1:
				return ModuleLog.DataInterval.MINUTE;
			case 2:
				return ModuleLog.DataInterval.FIVE_MINUTES;
			case 3:
				return ModuleLog.DataInterval.TEN_MINUTES;
			case 4:
				return ModuleLog.DataInterval.HALF_HOUR;
			case 5:
				return ModuleLog.DataInterval.HOUR;
			case 6:
				return ModuleLog.DataInterval.DAY;
			case 7:
				return ModuleLog.DataInterval.WEEK;
			case 8:
				return ModuleLog.DataInterval.MONTH;
		}

		return ModuleLog.DataInterval.RAW;
	}

	private void redrawActiveFragment() {
		ModuleGraphFragment currentFragment = (ModuleGraphFragment) ((GraphPagerAdapter) mViewPager.getAdapter()).getActiveFragment(mViewPager, mViewPager.getCurrentItem());

		currentFragment.onChartSettingChanged(mCheckBoxMin.isChecked(), mCheckBoxAvg.isChecked(), mCheckBoxMax.isChecked(), getIntervalBySliderProgress());
	}

	public void setMinValue(String minValue) {
		mMinValue.setText(minValue);
	}

	public void setMaxValue(String maxValue) {
		mMaxValue.setText(maxValue);
	}

	private static class GraphPagerAdapter extends FragmentPagerAdapter {

		private final List<Fragment> mFragments = new ArrayList<>();
		private final List<String> mFragmentTitles = new ArrayList<>();
		private final FragmentManager mFragmentManager;

		public GraphPagerAdapter(FragmentManager fm) {
			super(fm);
			mFragmentManager = fm;
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

		public Fragment getActiveFragment(ViewPager container, int position) {
			String name = makeFragmentName(container.getId(), position);
			return  mFragmentManager.findFragmentByTag(name);
		}

		private static String makeFragmentName(int viewId, int index) {
			return "android:switcher:" + viewId + ":" + index;
		}
	}


	public interface ChartSettingListener {
		void onChartSettingChanged(boolean drawMin, boolean drawAvg, boolean drawMax, ModuleLog.DataInterval dataGranularity);
	}
}
